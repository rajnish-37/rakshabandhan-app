import { timingSafeEqual } from "node:crypto";
import type { FastifyInstance } from "fastify";
import { auth } from "../firebase/admin.js";
import { config } from "../config.js";
import { requireString } from "../security/request-guard.js";
import { ClaimConflictError, ClaimService, ClaimValidationError } from "./claim.service.js";

interface CreateClaimBody {
  upiId: string;
}

interface MarkPaidBody {
  claimId: string;
}

function isAdminAuthorized(request: { headers: { [key: string]: string | string[] | undefined } }): boolean {
  const configuredKey = config.adminApiKey;
  const supplied = request.headers["x-admin-api-key"];
  if (!configuredKey || typeof supplied !== "string") return false;
  const expected = Buffer.from(configuredKey, "utf8");
  const actual = Buffer.from(supplied, "utf8");
  return expected.length === actual.length && timingSafeEqual(expected, actual);
}

function serialize(claim: Awaited<ReturnType<ClaimService["getClaim"]>>) {
  if (!claim) return null;
  return {
    ...claim,
    createdAt: claim.createdAt.toISOString(),
    updatedAt: claim.updatedAt.toISOString(),
    paidAt: claim.paidAt?.toISOString() ?? null,
  };
}

export async function claimRoutes(app: FastifyInstance): Promise<void> {
  const service = new ClaimService();

  app.post<{ Body: CreateClaimBody }>("/claims", async (request, reply) => {
    try {
      const header = request.headers.authorization;
      if (!header?.startsWith("Bearer ")) {
        return reply.code(401).send({ status: "error", message: "Authentication required" });
      }
      const token = header.slice("Bearer ".length).trim();
      if (!token || token.length > 8192) {
        return reply.code(401).send({ status: "error", message: "Authentication required" });
      }

      const decodedToken = await auth.verifyIdToken(token);
      const sisterId = decodedToken.sisterId;
      if (decodedToken.role !== "SISTER" || typeof sisterId !== "string" || !sisterId.trim()) {
        return reply.code(403).send({ status: "error", message: "Sister access is required" });
      }

      const upiId = requireString(request.body?.upiId, "UPI ID", 256);
      const claim = await service.createClaim(decodedToken.uid, sisterId, upiId);
      return reply.code(201).send({ status: "ok", claim: serialize(claim) });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to create gift claim";
      if (error instanceof ClaimConflictError) return reply.code(409).send({ status: "error", message });
      if (error instanceof ClaimValidationError) return reply.code(400).send({ status: "error", message });
      return reply.code(401).send({ status: "error", message: "Authentication required" });
    }
  });

  app.get("/claims/me", async (request, reply) => {
    try {
      const header = request.headers.authorization;
      if (!header?.startsWith("Bearer ")) {
        return reply.code(401).send({ status: "error", message: "Authentication required" });
      }
      const token = header.slice("Bearer ".length).trim();
      const decodedToken = await auth.verifyIdToken(token);
      const sisterId = decodedToken.sisterId;
      if (decodedToken.role !== "SISTER" || typeof sisterId !== "string" || !sisterId.trim()) {
        return reply.code(403).send({ status: "error", message: "Sister access is required" });
      }
      const claim = await service.getClaim(decodedToken.uid, sisterId);
      return reply.code(200).send({ status: "ok", claim: serialize(claim) });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to load gift claim";
      const code = error instanceof ClaimValidationError ? 403 : 401;
      return reply.code(code).send({ status: "error", message: code === 401 ? "Authentication required" : message });
    }
  });

  app.get("/admin/claims/pending", async (request, reply) => {
    if (!isAdminAuthorized(request)) {
      return reply.code(401).send({ status: "error", message: "Admin authorization required" });
    }
    try {
      const claims = await service.pendingClaims();
      return reply.code(200).send({
        status: "ok",
        claims: claims.map((claim) => ({
          ...claim,
          createdAt: claim.createdAt.toISOString(),
          updatedAt: claim.updatedAt.toISOString(),
          paidAt: claim.paidAt?.toISOString() ?? null,
        })),
      });
    } catch {
      return reply.code(500).send({ status: "error", message: "Unable to load pending claims" });
    }
  });

  app.post<{ Body: MarkPaidBody }>("/admin/claims/mark-paid", async (request, reply) => {
    if (!isAdminAuthorized(request)) {
      return reply.code(401).send({ status: "error", message: "Admin authorization required" });
    }
    try {
      const claimId = requireString(request.body?.claimId, "Claim ID", 128);
      const claim = await service.markPaid(claimId, "ADMIN_API_KEY");
      return reply.code(200).send({ status: "ok", claim: serialize(claim) });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to mark claim paid";
      if (error instanceof ClaimConflictError) return reply.code(409).send({ status: "error", message });
      return reply.code(400).send({ status: "error", message });
    }
  });
}

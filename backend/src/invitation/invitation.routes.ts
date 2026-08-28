import type { FastifyInstance } from "fastify";
import { InMemoryRateLimiter, requireEmail, requireString } from "../security/request-guard.js";
import { InvitationService } from "./invitation.service.js";
import { InvitationVerificationService } from "./invitation-verification.service.js";

interface CreateInvitationBody {
  sisterId: string;
  email: string;
}

interface VerifyInvitationBody {
  email: string;
  code: string;
  keyId: string;
  publicKey: string;
}

export async function invitationRoutes(app: FastifyInstance): Promise<void> {
  const service = new InvitationService();
  const verificationService = new InvitationVerificationService();
  const createLimiter = new InMemoryRateLimiter(10, 60_000);
  const verifyLimiter = new InMemoryRateLimiter(8, 60_000);

  app.post<{ Body: CreateInvitationBody }>("/invitations", async (request, reply) => {
    if (!createLimiter.allow(`create:${request.ip}`)) {
      return reply.code(429).send({ status: "error", message: "Too many requests" });
    }
    createLimiter.cleanup();

    try {
      const sisterId = requireString(request.body?.sisterId, "Sister ID", 64);
      const email = requireEmail(request.body?.email);
      const result = await service.createInvitation(sisterId, email);

      return reply.code(201).send({
        invitationId: result.invitation.invitationId,
        sisterId: result.invitation.sisterId,
        email: result.invitation.email,
        status: result.invitation.status,
        expiresAt: result.invitation.expiresAt.toISOString(),
        createdAt: result.invitation.createdAt.toISOString(),
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to create invitation";
      return reply.code(400).send({ status: "error", message });
    }
  });

  app.post<{ Body: VerifyInvitationBody }>("/invitations/verify", async (request, reply) => {
    if (!verifyLimiter.allow(`verify:${request.ip}`)) {
      return reply.code(429).send({ status: "error", message: "Too many verification attempts" });
    }

    verifyLimiter.cleanup();

    try {
      const email = requireEmail(request.body?.email);
      const code = requireString(request.body?.code, "Invitation code", 64).toUpperCase();
      const keyId = requireString(request.body?.keyId, "Device key ID", 128);
      const publicKey = requireString(request.body?.publicKey, "Device public key", 4096);

      const result = await verificationService.verifyInvitation(
        email,
        code,
        keyId,
        publicKey,
      );

      return reply.code(200).send({ status: "verified", ...result });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to verify invitation";
      return reply.code(400).send({ status: "error", message });
    }
  });
}

import type { FastifyInstance } from "fastify";
import { auth } from "../firebase/admin.js";
import { InMemoryRateLimiter, requireString } from "../security/request-guard.js";
import { DeviceAuthService } from "./device-auth.service.js";
import { DeviceService } from "./device.service.js";

interface RegisterDeviceBody {
  keyId: string;
  publicKey: string;
}

interface ChallengeBody {
  keyId: string;
}

interface VerifyChallengeBody {
  keyId: string;
  challengeId: string;
  signature: string;
}

export async function deviceRoutes(app: FastifyInstance): Promise<void> {
  const service = new DeviceService();
  const authService = new DeviceAuthService();
  const challengeLimiter = new InMemoryRateLimiter(30, 60_000);
  const verifyLimiter = new InMemoryRateLimiter(8, 60_000);

  app.post<{ Body: RegisterDeviceBody }>("/devices/register", async (request, reply) => {
    try {
      const header = request.headers.authorization;
      if (!header?.startsWith("Bearer ")) {
        return reply.code(401).send({ status: "error", message: "Firebase ID token is required" });
      }

      const token = header.slice("Bearer ".length).trim();
      if (!token || token.length > 8192) {
        return reply.code(401).send({ status: "error", message: "Firebase ID token is required" });
      }

      const decodedToken = await auth.verifyIdToken(token);
      const keyId = requireString(request.body?.keyId, "Device key ID", 128);
      const publicKey = requireString(request.body?.publicKey, "Device public key", 4096);
      const sisterId = decodedToken.sisterId;

      if (decodedToken.role !== "SISTER" || typeof sisterId !== "string" || !sisterId) {
        return reply.code(403).send({
          status: "error",
          message: "Firebase identity is not authorized for device registration",
        });
      }

      await service.register({
        authUid: decodedToken.uid,
        sisterId,
        keyId,
        publicKey,
      });

      return reply.code(201).send({ status: "registered", sisterId, keyId });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to register device key";
      return reply.code(400).send({ status: "error", message });
    }
  });

  app.post<{ Body: ChallengeBody }>("/devices/challenge", async (request, reply) => {
    if (!challengeLimiter.allow(`challenge:${request.ip}`)) {
      return reply.code(429).send({ status: "error", message: "Too many requests" });
    }

    challengeLimiter.cleanup();

    try {
      const keyId = requireString(request.body?.keyId, "Device key ID", 128);
      const result = await authService.createChallenge(keyId);
      return reply.code(200).send({
        status: "challenge",
        challengeId: result.challengeId,
        challenge: result.challenge,
        expiresAt: result.expiresAt.toISOString(),
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to create device challenge";
      return reply.code(400).send({ status: "error", message });
    }
  });

  app.post<{ Body: VerifyChallengeBody }>("/devices/challenge/verify", async (request, reply) => {
    if (!verifyLimiter.allow(`verify:${request.ip}`)) {
      return reply.code(429).send({ status: "error", message: "Too many authentication attempts" });
    }

    verifyLimiter.cleanup();

    try {
      const keyId = requireString(request.body?.keyId, "Device key ID", 128);
      const challengeId = requireString(request.body?.challengeId, "Challenge ID", 128);
      const signature = requireString(request.body?.signature, "Signature", 4096);
      const result = await authService.verifyChallenge(keyId, challengeId, signature);

      return reply.code(200).send({
        status: "authenticated",
        ...result,
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to verify device challenge";
      return reply.code(401).send({ status: "error", message });
    }
  });
}

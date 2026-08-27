import type { FastifyInstance, FastifyRequest } from "fastify";
import { auth } from "../firebase/admin.js";
import { DeviceService } from "./device.service.js";

interface RegisterDeviceBody {
  keyId: string;
  publicKey: string;
}

function bearerToken(request: FastifyRequest): string {
  const header = request.headers.authorization;
  if (!header?.startsWith("Bearer ")) {
    throw new Error("Firebase ID token is required");
  }

  const token = header.slice("Bearer ".length).trim();
  if (!token) throw new Error("Firebase ID token is required");
  return token;
}

export async function deviceRoutes(app: FastifyInstance): Promise<void> {
  const service = new DeviceService();

  app.post<{ Body: RegisterDeviceBody }>("/devices/register", async (request, reply) => {
    try {
      const decodedToken = await auth.verifyIdToken(bearerToken(request));
      const { keyId, publicKey } = request.body ?? {};
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

      return reply.code(201).send({
        status: "registered",
        sisterId,
        keyId,
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to register device key";
      const statusCode = message === "Firebase ID token is required" ? 401 : 400;
      return reply.code(statusCode).send({
        status: "error",
        message,
      });
    }
  });
}

import type { FastifyInstance } from "fastify";
import { InvitationService } from "./invitation.service.js";
import { InvitationVerificationService } from "./invitation-verification.service.js";

interface CreateInvitationBody {
  sisterId: string;
  email: string;
}

interface VerifyInvitationBody {
  email: string;
  code: string;
}

export async function invitationRoutes(app: FastifyInstance): Promise<void> {
  const service = new InvitationService();
  const verificationService = new InvitationVerificationService();

  app.post<{ Body: CreateInvitationBody }>("/invitations", async (request, reply) => {
    const { sisterId, email } = request.body ?? {};

    try {
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

      return reply.code(400).send({
        status: "error",
        message,
      });
    }
  });

  app.post<{ Body: VerifyInvitationBody }>("/invitations/verify", async (request, reply) => {
    const { email, code } = request.body ?? {};

    try {
      const result = await verificationService.verifyInvitation(email, code);

      return reply.code(200).send({
        status: "verified",
        ...result,
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to verify invitation";

      return reply.code(400).send({
        status: "error",
        message,
      });
    }
  });
}

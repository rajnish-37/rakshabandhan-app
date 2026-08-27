import type { FastifyInstance } from "fastify";
import { InvitationService } from "./invitation.service.js";

interface CreateInvitationBody {
  sisterId: string;
  email: string;
}

export async function invitationRoutes(app: FastifyInstance): Promise<void> {
  const service = new InvitationService();

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
}

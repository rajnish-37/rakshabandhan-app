import { randomUUID } from "node:crypto";
import { generateInvitationCode } from "./invitation-code.service.js";
import { InvitationRepository } from "./invitation.repository.js";
import { EmailService } from "../email/email.service.js";
import type { Invitation } from "./invitation.model.js";

const INVITATION_TTL_MS = 15 * 60 * 1000;

export interface CreateInvitationResult {
  invitation: Invitation;
}

export class InvitationService {
  constructor(
    private readonly repository = new InvitationRepository(),
    private readonly emailService = new EmailService(),
  ) {}

  async createInvitation(
    sisterId: string,
    email: string,
  ): Promise<CreateInvitationResult> {
    const normalizedEmail = email.trim().toLowerCase();

    if (!sisterId.trim()) {
      throw new Error("sisterId is required");
    }

    if (!normalizedEmail || !normalizedEmail.includes("@")) {
      throw new Error("A valid email is required");
    }

    const { rawCode, codeHash } = generateInvitationCode();
    const now = new Date();

    const invitation: Invitation = {
      invitationId: randomUUID(),
      sisterId: sisterId.trim(),
      email: normalizedEmail,
      codeHash,
      status: "PENDING",
      expiresAt: new Date(now.getTime() + INVITATION_TTL_MS),
      createdAt: now,
    };

    await this.repository.create(invitation);

    try {
      await this.emailService.sendInvitation({
        recipientEmail: invitation.email,
        invitationCode: rawCode,
        expiresAt: invitation.expiresAt,
      });
    } catch (error) {
      await this.repository.markRevoked(invitation.invitationId);
      throw new Error("Invitation created but email delivery failed", {
        cause: error,
      });
    }

    return { invitation };
  }
}

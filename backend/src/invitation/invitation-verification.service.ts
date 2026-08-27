import { timingSafeEqual } from "node:crypto";
import { InvitationRepository } from "./invitation.repository.js";
import { hashInvitationCode } from "./invitation-code.service.js";

export interface VerifyInvitationResult {
  invitationId: string;
  sisterId: string;
  email: string;
}

export class InvitationVerificationService {
  constructor(private readonly repository = new InvitationRepository()) {}

  async verifyInvitation(
    email: string,
    code: string,
  ): Promise<VerifyInvitationResult> {
    const normalizedEmail = email.trim().toLowerCase();
    const submittedCode = code.trim().toUpperCase();

    if (!normalizedEmail || !normalizedEmail.includes("@")) {
      throw new Error("A valid email is required");
    }

    if (!submittedCode) {
      throw new Error("Invitation code is required");
    }

    const invitation = await this.repository.findPendingByEmail(normalizedEmail);

    if (!invitation) {
      throw new Error("No pending invitation found");
    }

    if (invitation.expiresAt.getTime() <= Date.now()) {
      throw new Error("Invitation has expired");
    }

    const submittedHash = Buffer.from(hashInvitationCode(submittedCode), "utf8");
    const storedHash = Buffer.from(invitation.codeHash, "utf8");

    if (
      submittedHash.length !== storedHash.length ||
      !timingSafeEqual(submittedHash, storedHash)
    ) {
      throw new Error("Invalid invitation code");
    }

    await this.repository.markRedeemed(invitation.invitationId);

    return {
      invitationId: invitation.invitationId,
      sisterId: invitation.sisterId,
      email: invitation.email,
    };
  }
}

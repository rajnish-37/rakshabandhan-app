import { timingSafeEqual } from "node:crypto";
import { auth } from "../firebase/admin.js";
import { DeviceService } from "../device/device.service.js";
import { InvitationRepository } from "./invitation.repository.js";
import { hashInvitationCode } from "./invitation-code.service.js";
import { SisterAccountService } from "../sister/sister-account.service.js";

export interface VerifyInvitationResult {
  invitationId: string;
  sisterId: string;
  email: string;
  authUid: string;
  customToken: string;
}

export class InvitationVerificationService {
  constructor(
    private readonly repository = new InvitationRepository(),
    private readonly sisterAccountService = new SisterAccountService(),
    private readonly deviceService = new DeviceService(),
  ) {}

  async verifyInvitation(
    email: string,
    code: string,
    keyId: string,
    publicKey: string,
  ): Promise<VerifyInvitationResult> {
    const normalizedEmail = email.trim().toLowerCase();
    const submittedCode = code.trim().toUpperCase();

    if (!normalizedEmail || !normalizedEmail.includes("@")) {
      throw new Error("A valid email is required");
    }

    if (!submittedCode) {
      throw new Error("Invitation code is required");
    }

    if (!keyId.trim() || !publicKey.trim()) {
      throw new Error("Device public key registration data is required");
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

    const account = await this.sisterAccountService.provision(
      invitation.sisterId,
      invitation.email,
    );

    await this.deviceService.register({
      authUid: account.authUid,
      sisterId: invitation.sisterId,
      keyId,
      publicKey,
    });

    const redeemed = await this.repository.markRedeemed(invitation.invitationId);
    if (!redeemed) {
      throw new Error("Invitation has already been redeemed");
    }

    const customToken = await auth.createCustomToken(account.authUid, {
      role: "SISTER",
      sisterId: invitation.sisterId,
    });

    return {
      invitationId: invitation.invitationId,
      sisterId: invitation.sisterId,
      email: invitation.email,
      authUid: account.authUid,
      customToken,
    };
  }
}

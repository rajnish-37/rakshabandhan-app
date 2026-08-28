import { auth } from "../firebase/admin.js";
import { SisterRepository } from "./sister.repository.js";

const SISTER_NAMES: Record<string, string> = {
  Sister_01: "Nisha",
  Sister_02: "Neha",
  Sister_03: "Mona",
  Sister_04: "Khushi",
};

export interface ProvisionSisterAccountResult {
  sisterId: string;
  email: string;
  authUid: string;
}

function isAuthError(error: unknown, code: string): boolean {
  return (
    typeof error === "object" &&
    error !== null &&
    "code" in error &&
    (error as { code?: unknown }).code === code
  );
}

export class SisterAccountService {
  constructor(private readonly repository = new SisterRepository()) {}

  async provision(sisterId: string, email: string): Promise<ProvisionSisterAccountResult> {
    const normalizedEmail = email.trim().toLowerCase();
    const normalizedSisterId = sisterId.trim();
    const sisterName = SISTER_NAMES[normalizedSisterId];

    if (!normalizedSisterId) throw new Error("Sister ID is required");
    if (!sisterName) throw new Error("Unknown sister ID");
    if (!normalizedEmail || !normalizedEmail.includes("@")) {
      throw new Error("A valid email is required");
    }

    const existingProfile = await this.repository.findById(normalizedSisterId);
    let user;

    if (existingProfile) {
      if (existingProfile.email !== normalizedEmail) {
        throw new Error("Sister ID is already linked to a different email");
      }
      user = await auth.getUser(existingProfile.authUid);
      if (user.email?.toLowerCase() !== normalizedEmail) {
        throw new Error("Sister account email does not match the invitation");
      }
    } else {
      try {
        user = await auth.getUserByEmail(normalizedEmail);
      } catch (error) {
        if (!isAuthError(error, "auth/user-not-found")) throw error;
        user = await auth.createUser({ email: normalizedEmail, emailVerified: true, disabled: false });
      }
    }

    const existingSisterId = user.customClaims?.sisterId;
    const existingRole = user.customClaims?.role;
    if (existingSisterId && existingSisterId !== normalizedSisterId) {
      throw new Error("Email is already linked to a different sister ID");
    }
    if (existingRole && existingRole !== "SISTER") {
      throw new Error("Email is already linked to a different account role");
    }
    if (!user.emailVerified) user = await auth.updateUser(user.uid, { emailVerified: true });

    await auth.setCustomUserClaims(user.uid, {
      ...(user.customClaims ?? {}),
      role: "SISTER",
      sisterId: normalizedSisterId,
    });

    const now = new Date();
    await this.repository.upsert({
      sisterId: normalizedSisterId,
      name: sisterName,
      email: normalizedEmail,
      authUid: user.uid,
      status: "ACTIVE",
      createdAt: existingProfile?.createdAt ?? now,
      updatedAt: now,
    });

    return { sisterId: normalizedSisterId, email: normalizedEmail, authUid: user.uid };
  }
}

import { auth } from "../firebase/admin.js";
import { SisterRepository } from "./sister.repository.js";

export interface SisterHomeData {
  sisterId: string;
  email: string;
  name: string;
  enrollmentStatus: "ACTIVE";
}

export class SisterHomeService {
  constructor(private readonly repository = new SisterRepository()) {}

  async getHome(authUid: string, claimedSisterId: string): Promise<SisterHomeData> {
    const sisterId = claimedSisterId.trim();
    if (!authUid || !sisterId) throw new Error("Authenticated sister identity is required");

    const profile = await this.repository.findById(sisterId);
    if (!profile || profile.authUid !== authUid || profile.status !== "ACTIVE") {
      throw new Error("Sister identity is not authorized");
    }

    const user = await auth.getUser(authUid);
    if (user.customClaims?.role !== "SISTER" || user.customClaims?.sisterId !== sisterId) {
      throw new Error("Sister identity is not authorized");
    }

    return {
      sisterId: profile.sisterId,
      email: profile.email,
      name: user.displayName?.trim() || "Sister",
      enrollmentStatus: "ACTIVE",
    };
  }
}

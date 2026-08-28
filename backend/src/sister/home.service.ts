import { auth } from "../firebase/admin.js";
import { ClaimRepository } from "../claim/claim.repository.js";
import { GiftRepository } from "../gift/gift.repository.js";
import { SisterRepository } from "./sister.repository.js";

export interface SisterHomeData {
  sisterId: string;
  email: string;
  name: string;
  enrollmentStatus: "ACTIVE";
  gift: {
    giftId: string;
    amount: number;
    currency: string;
    status: string;
    claimEligible: boolean;
    claimDeadline: Date | null;
  } | null;
  claim: {
    claimId: string;
    sisterId: string;
    sisterName: string;
    sisterEmail: string;
    giftId: string;
    amount: number;
    currency: string;
    upiId: string;
    status: string;
    createdAt: Date;
    updatedAt: Date;
    paidAt: Date | null;
    paidBy: string | null;
  } | null;
}

export class SisterHomeService {
  constructor(
    private readonly repository = new SisterRepository(),
    private readonly giftRepository = new GiftRepository(),
    private readonly claimRepository = new ClaimRepository(),
  ) {}

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

    const gift = await this.giftRepository.findBySisterId(sisterId);
    const claim = gift ? await this.claimRepository.findBySisterAndGift(sisterId, gift.giftId) : null;

    return {
      sisterId: profile.sisterId,
      email: profile.email,
      name: profile.name,
      enrollmentStatus: "ACTIVE",
      gift: gift
        ? {
            giftId: gift.giftId,
            amount: gift.amount,
            currency: gift.currency,
            status: gift.status,
            claimEligible: gift.claimEligible,
            claimDeadline: gift.claimDeadline,
          }
        : null,
      claim,
    };
  }
}

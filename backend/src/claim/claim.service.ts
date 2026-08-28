import { randomUUID } from "node:crypto";
import { auth } from "../firebase/admin.js";
import { GiftRepository } from "../gift/gift.repository.js";
import { SisterRepository } from "../sister/sister.repository.js";
import { ClaimRepository } from "./claim.repository.js";
import type { GiftClaim } from "./claim.model.js";

const UPI_ID_PATTERN = /^[A-Za-z0-9][A-Za-z0-9._-]{1,255}@[A-Za-z][A-Za-z0-9.-]{1,63}$/;

export class ClaimConflictError extends Error {}
export class ClaimValidationError extends Error {}

export class ClaimService {
  constructor(
    private readonly claims = new ClaimRepository(),
    private readonly gifts = new GiftRepository(),
    private readonly sisters = new SisterRepository(),
  ) {}

  async createClaim(authUid: string, claimedSisterId: string, upiIdInput: string): Promise<GiftClaim> {
    const sisterId = claimedSisterId.trim();
    const upiId = upiIdInput.trim().toLowerCase();
    if (!authUid || !sisterId) throw new ClaimValidationError("Authenticated sister identity is required");
    if (!UPI_ID_PATTERN.test(upiId)) throw new ClaimValidationError("Enter a valid UPI ID");

    const profile = await this.sisters.findById(sisterId);
    if (!profile || profile.authUid !== authUid || profile.status !== "ACTIVE") {
      throw new ClaimValidationError("Sister identity is not authorized");
    }

    const user = await auth.getUser(authUid);
    if (user.customClaims?.role !== "SISTER" || user.customClaims?.sisterId !== sisterId) {
      throw new ClaimValidationError("Sister identity is not authorized");
    }

    const gift = await this.gifts.findBySisterId(sisterId);
    if (!gift) throw new ClaimValidationError("No gift is configured for this sister");
    if (gift.status !== "ELIGIBLE" || !gift.claimEligible) {
      throw new ClaimValidationError("This gift is not currently eligible for claiming");
    }
    if (gift.claimDeadline && gift.claimDeadline.getTime() < Date.now()) {
      throw new ClaimValidationError("The gift claim deadline has passed");
    }

    const now = new Date();
    const claim: GiftClaim = {
      claimId: randomUUID(),
      sisterId: profile.sisterId,
      sisterName: profile.name,
      sisterEmail: profile.email,
      giftId: gift.giftId,
      amount: gift.amount,
      currency: gift.currency,
      upiId,
      status: "PENDING",
      createdAt: now,
      updatedAt: now,
      paidAt: null,
      paidBy: null,
    };

    const result = await this.claims.createClaim(claim);
    if (result.kind === "duplicate") {
      if (result.claim.status === "PAID") throw new ClaimConflictError("This gift has already been paid");
      throw new ClaimConflictError("A claim for this gift is already pending");
    }

    return result.claim;
  }

  async getClaim(authUid: string, claimedSisterId: string): Promise<GiftClaim | null> {
    const sisterId = claimedSisterId.trim();
    const profile = await this.sisters.findById(sisterId);
    if (!profile || profile.authUid !== authUid || profile.status !== "ACTIVE") {
      throw new ClaimValidationError("Sister identity is not authorized");
    }
    const gift = await this.gifts.findBySisterId(sisterId);
    if (!gift) return null;
    return this.claims.findBySisterAndGift(sisterId, gift.giftId);
  }

  async pendingClaims(): Promise<GiftClaim[]> {
    return this.claims.findPending();
  }

  async markPaid(claimId: string, paidBy: string): Promise<GiftClaim> {
    const normalizedClaimId = claimId.trim();
    if (!normalizedClaimId) throw new ClaimValidationError("Claim ID is required");
    const normalizedPaidBy = paidBy.trim();
    if (!normalizedPaidBy) throw new ClaimValidationError("Admin identity is required");

    const claim = await this.claims.markPaid(normalizedClaimId, normalizedPaidBy, new Date());
    if (!claim) throw new ClaimConflictError("Claim is not pending or does not exist");
    return claim;
  }
}

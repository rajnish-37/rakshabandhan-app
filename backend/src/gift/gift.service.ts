import { randomUUID } from "node:crypto";
import { GiftRepository } from "./gift.repository.js";
import type { Gift, GiftStatus } from "./gift.model.js";

const CURRENCIES = new Set(["INR"]);
const STATUSES = new Set<GiftStatus>(["PENDING", "ELIGIBLE", "CLAIMED", "DISABLED"]);

export interface ConfigureGiftInput {
  sisterId: string;
  amount: number;
  currency: string;
  status: GiftStatus;
  claimEligible: boolean;
  claimDeadline?: Date | null;
}

export class GiftService {
  constructor(private readonly repository = new GiftRepository()) {}

  async getBySisterId(sisterId: string): Promise<Gift | null> {
    return this.repository.findBySisterId(sisterId);
  }

  async configure(input: ConfigureGiftInput): Promise<Gift> {
    const sisterId = input.sisterId.trim();
    const currency = input.currency.trim().toUpperCase();

    if (!sisterId) throw new Error("Sister ID is required");
    if (!Number.isFinite(input.amount) || input.amount <= 0 || input.amount > 10_000_000) {
      throw new Error("Gift amount must be greater than 0 and at most 10,000,000");
    }
    if (!CURRENCIES.has(currency)) throw new Error("Unsupported gift currency");
    if (!STATUSES.has(input.status)) throw new Error("Invalid gift status");
    if (input.status === "ELIGIBLE" && !input.claimEligible) {
      throw new Error("Eligible gifts must allow claiming");
    }
    if (input.claimDeadline && Number.isNaN(input.claimDeadline.getTime())) {
      throw new Error("Invalid claim deadline");
    }

    const now = new Date();
    return this.repository.upsert({
      giftId: randomUUID(),
      sisterId,
      amount: Math.round(input.amount * 100) / 100,
      currency,
      status: input.status,
      claimEligible: input.claimEligible,
      claimDeadline: input.claimDeadline ?? null,
      createdAt: now,
      updatedAt: now,
    });
  }
}

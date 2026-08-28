export type GiftStatus = "PENDING" | "ELIGIBLE" | "CLAIMED" | "DISABLED";

export interface Gift {
  giftId: string;
  sisterId: string;
  amount: number;
  currency: string;
  status: GiftStatus;
  claimEligible: boolean;
  claimDeadline: Date | null;
  createdAt: Date;
  updatedAt: Date;
}

export type SisterStatus = "ACTIVE";

export interface SisterProfile {
  sisterId: string;
  email: string;
  authUid: string;
  status: SisterStatus;
  createdAt: Date;
  updatedAt: Date;
}

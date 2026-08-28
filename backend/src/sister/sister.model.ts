export type SisterStatus = "ACTIVE";

export interface SisterProfile {
  sisterId: string;
  name: string;
  email: string;
  authUid: string;
  status: SisterStatus;
  createdAt: Date;
  updatedAt: Date;
}

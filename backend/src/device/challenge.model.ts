export interface DeviceChallenge {
  challengeId: string;
  keyId: string;
  challenge: string;
  expiresAt: Date;
  createdAt: Date;
  usedAt?: Date;
}

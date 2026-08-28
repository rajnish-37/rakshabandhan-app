import { createPublicKey, createVerify, randomBytes, randomUUID } from "node:crypto";
import { auth } from "../firebase/admin.js";
import { DeviceChallengeRepository } from "./challenge.repository.js";
import { DeviceRepository } from "./device.repository.js";

const CHALLENGE_TTL_MS = 60_000;

export interface DeviceChallengeResult {
  challengeId: string;
  challenge: string;
  expiresAt: Date;
}

export interface DeviceLoginResult {
  authUid: string;
  sisterId: string;
  customToken: string;
}

export class DeviceAuthService {
  constructor(
    private readonly deviceRepository = new DeviceRepository(),
    private readonly challengeRepository = new DeviceChallengeRepository(),
  ) {}

  async createChallenge(keyId: string): Promise<DeviceChallengeResult> {
    const normalizedKeyId = keyId.trim();
    if (!normalizedKeyId) throw new Error("Device key ID is required");

    const device = await this.deviceRepository.findByKeyId(normalizedKeyId);
    if (!device) throw new Error("Registered device key not found");

    const now = new Date();
    const expiresAt = new Date(now.getTime() + CHALLENGE_TTL_MS);
    const challenge: DeviceChallengeResult = {
      challengeId: randomUUID(),
      challenge: randomBytes(32).toString("base64url"),
      expiresAt,
    };

    await this.challengeRepository.create({
      ...challenge,
      keyId: normalizedKeyId,
      createdAt: now,
    });

    return challenge;
  }

  async verifyChallenge(
    keyId: string,
    challengeId: string,
    signature: string,
  ): Promise<DeviceLoginResult> {
    const normalizedKeyId = keyId.trim();
    const normalizedChallengeId = challengeId.trim();
    if (!normalizedKeyId) throw new Error("Device key ID is required");
    if (!normalizedChallengeId) throw new Error("Challenge ID is required");
    if (!signature.trim()) throw new Error("Signature is required");

    const challenge = await this.challengeRepository.find(normalizedChallengeId);
    if (!challenge || challenge.keyId !== normalizedKeyId) {
      throw new Error("Invalid device challenge");
    }

    if (challenge.expiresAt.getTime() <= Date.now()) {
      throw new Error("Device challenge has expired");
    }

    const device = await this.deviceRepository.findByKeyId(normalizedKeyId);
    if (!device) throw new Error("Registered device key not found");

    const publicKey = createPublicKey({
      key: Buffer.from(device.publicKey, "base64"),
      format: "der",
      type: "spki",
    });

    const verifier = createVerify("SHA256");
    verifier.update(challenge.challenge, "utf8");
    verifier.end();

    let valid = false;
    try {
      valid = verifier.verify(publicKey, Buffer.from(signature, "base64url"));
    } catch {
      throw new Error("Invalid device signature");
    }

    if (!valid) throw new Error("Invalid device signature");

    const consumed = await this.challengeRepository.consume(normalizedChallengeId);
    if (!consumed) throw new Error("Device challenge has already been used");

    const customToken = await auth.createCustomToken(device.authUid, {
      role: "SISTER",
      sisterId: device.sisterId,
    });

    return {
      authUid: device.authUid,
      sisterId: device.sisterId,
      customToken,
    };
  }
}

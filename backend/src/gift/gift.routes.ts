import type { FastifyInstance } from "fastify";
import { requireAdminAuthorization } from "../security/admin-auth.js";
import { requireString } from "../security/request-guard.js";
import { GiftService } from "./gift.service.js";
import type { GiftStatus } from "./gift.model.js";

interface ConfigureGiftBody {
  sisterId: string;
  amount: number;
  currency?: string;
  status?: GiftStatus;
  claimEligible?: boolean;
  claimDeadline?: string | null;
}

function serialize(gift: Awaited<ReturnType<GiftService["getBySisterId"]>>) {
  if (!gift) return null;
  return {
    ...gift,
    claimDeadline: gift.claimDeadline?.toISOString() ?? null,
    createdAt: gift.createdAt.toISOString(),
    updatedAt: gift.updatedAt.toISOString(),
  };
}

export async function giftRoutes(app: FastifyInstance): Promise<void> {
  const service = new GiftService();

  app.put<{ Body: ConfigureGiftBody }>("/admin/gift", async (request, reply) => {
    const authorizationError = requireAdminAuthorization(request);
    if (authorizationError) return reply.code(401).send(authorizationError);

    try {
      const sisterId = requireString(request.body?.sisterId, "Sister ID", 64);
      const amount = Number(request.body?.amount);
      const currency = requireString(request.body?.currency ?? "INR", "Currency", 8);
      const status = request.body?.status ?? "PENDING";
      const claimEligible = request.body?.claimEligible ?? false;
      const claimDeadline = request.body?.claimDeadline ? new Date(request.body.claimDeadline) : null;

      const gift = await service.configure({ sisterId, amount, currency, status, claimEligible, claimDeadline });
      return reply.code(200).send({ status: "ok", gift: serialize(gift) });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to configure gift";
      return reply.code(400).send({ status: "error", message });
    }
  });

  app.get<{ Params: { sisterId: string } }>("/admin/sisters/:sisterId/gift", async (request, reply) => {
    const authorizationError = requireAdminAuthorization(request);
    if (authorizationError) return reply.code(401).send(authorizationError);

    try {
      const sisterId = requireString(request.params.sisterId, "Sister ID", 64);
      const gift = await service.getBySisterId(sisterId);
      return reply.code(200).send({ status: "ok", gift: serialize(gift) });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to load gift";
      return reply.code(400).send({ status: "error", message });
    }
  });
}

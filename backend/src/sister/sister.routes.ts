import type { FastifyInstance } from "fastify";
import { auth } from "../firebase/admin.js";
import { SisterHomeService } from "./home.service.js";
import { SisterRepository } from "./sister.repository.js";

const SISTER_ROSTER = [
  { sisterId: "Sister_01", name: "Nisha" },
  { sisterId: "Sister_02", name: "Neha" },
  { sisterId: "Sister_03", name: "Mona" },
  { sisterId: "Sister_04", name: "Khushi" },
] as const;

export async function sisterRoutes(app: FastifyInstance): Promise<void> {
  const homeService = new SisterHomeService();
  const repository = new SisterRepository();

  app.get("/me", async (request, reply) => {
    try {
      const header = request.headers.authorization;
      if (!header?.startsWith("Bearer ")) {
        return reply.code(401).send({ status: "error", message: "Authentication required" });
      }
      const token = header.slice("Bearer ".length).trim();
      if (!token || token.length > 8192) {
        return reply.code(401).send({ status: "error", message: "Authentication required" });
      }
      const decodedToken = await auth.verifyIdToken(token);
      const role = decodedToken.role;
      const sisterId = decodedToken.sisterId;
      if (role !== "SISTER" || typeof sisterId !== "string" || !sisterId.trim()) {
        return reply.code(403).send({ status: "error", message: "Sister access is required" });
      }
      const home = await homeService.getHome(decodedToken.uid, sisterId);
      return reply.code(200).send({ status: "ok", ...home });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to load home";
      const statusCode = message.includes("authorized") ? 403 : 401;
      return reply.code(statusCode).send({ status: "error", message: statusCode === 401 ? "Authentication required" : message });
    }
  });

  app.get("/admin/sisters", async (_request, reply) => {
    try {
      const profiles = await repository.findAll();
      const byId = new Map(profiles.map((profile) => [profile.sisterId, profile]));

      const sisters = SISTER_ROSTER.map(({ sisterId, name }) => {
        const profile = byId.get(sisterId);
        return {
          sisterId,
          name,
          email: profile?.email ?? "",
          status: profile?.status ?? "NOT_ENROLLED",
        };
      });

      return reply.code(200).send({ status: "ok", sisters });
    } catch {
      return reply.code(500).send({ status: "error", message: "Unable to load sisters" });
    }
  });
}

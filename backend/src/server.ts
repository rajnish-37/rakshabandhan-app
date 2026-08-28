import Fastify from "fastify";
import { config } from "./config.js";
import { checkFirestoreConnection } from "./firebase/health.js";
import { invitationRoutes } from "./invitation/invitation.routes.js";
import { deviceRoutes } from "./device/device.routes.js";
import { sisterRoutes } from "./sister/sister.routes.js";
import { giftRoutes } from "./gift/gift.routes.js";
import { claimRoutes } from "./claim/claim.routes.js";

const app = Fastify({
  logger: true,
  bodyLimit: 64 * 1024,
});

app.addHook("onSend", async (_request, reply) => {
  reply.header("Cache-Control", "no-store");
  reply.header("X-Content-Type-Options", "nosniff");
  reply.header("Referrer-Policy", "no-referrer");
  reply.header("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
});

app.get("/health", async () => ({
  status: "ok",
  service: "rakshabandhan-backend",
}));

app.get("/health/firestore", async (_request, reply) => {
  try {
    await checkFirestoreConnection();
    return { status: "ok", service: "firestore" };
  } catch (error) {
    app.log.error(error, "Firestore health check failed");
    return reply.code(503).send({ status: "error", service: "firestore", message: "Firestore connection failed" });
  }
});

await invitationRoutes(app);
await deviceRoutes(app);
await sisterRoutes(app);
await giftRoutes(app);
await claimRoutes(app);

const start = async (): Promise<void> => {
  try {
    await app.listen({ port: config.port, host: config.host });
  } catch (error) {
    app.log.error(error);
    process.exit(1);
  }
};

await start();

import Fastify from "fastify";
import { config } from "./config.js";
import { checkFirestoreConnection } from "./firebase/health.js";
import { invitationRoutes } from "./invitation/invitation.routes.js";

const app = Fastify({
  logger: true,
});

app.get("/health", async () => ({
  status: "ok",
  service: "rakshabandhan-backend",
}));

app.get("/health/firestore", async (_request, reply) => {
  try {
    await checkFirestoreConnection();
    return {
      status: "ok",
      service: "firestore",
    };
  } catch (error) {
    app.log.error(error, "Firestore health check failed");
    return reply.code(503).send({
      status: "error",
      service: "firestore",
      message: "Firestore connection failed",
    });
  }
});

await invitationRoutes(app);

const start = async (): Promise<void> => {
  try {
    await app.listen({ port: config.port, host: config.host });
  } catch (error) {
    app.log.error(error);
    process.exit(1);
  }
};

await start();

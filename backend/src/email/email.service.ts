import brevo from "@getbrevo/brevo";

export interface InvitationEmailInput {
  recipientEmail: string;
  invitationCode: string;
  expiresAt: Date;
}

export class EmailService {
  private readonly client: brevo.TransactionalEmailsApi;

  constructor() {
    const apiKey = process.env.BREVO_API_KEY;
    const senderEmail = process.env.BREVO_SENDER_EMAIL;
    const senderName = process.env.BREVO_SENDER_NAME;

    if (!apiKey) throw new Error("BREVO_API_KEY is not configured");
    if (!senderEmail) throw new Error("BREVO_SENDER_EMAIL is not configured");
    if (!senderName) throw new Error("BREVO_SENDER_NAME is not configured");

    this.client = new brevo.TransactionalEmailsApi();
    this.client.setApiKey(brevo.TransactionalEmailsApiApiKeys.apiKey, apiKey);
  }

  async sendInvitation(input: InvitationEmailInput): Promise<void> {
    const senderEmail = process.env.BREVO_SENDER_EMAIL!;
    const senderName = process.env.BREVO_SENDER_NAME!;
    const expiresAt = input.expiresAt.toLocaleString("en-IN", {
      dateStyle: "medium",
      timeStyle: "short",
      timeZone: "Asia/Kolkata",
    });

    const email = new brevo.SendSmtpEmail();
    email.sender = { email: senderEmail, name: senderName };
    email.to = [{ email: input.recipientEmail }];
    email.subject = "Your Rakhi Bandhan Invitation";
    email.textContent = [
      "You have received an invitation to join Rakhi Bandhan.",
      "",
      `Your invitation code: ${input.invitationCode}`,
      "",
      `This code expires on ${expiresAt} (IST).`,
      "",
      "Please enter this code in the Rakhi Bandhan app.",
    ].join("\n");

    await this.client.sendTransacEmail(email);
  }
}

package com.zinphraek.leprestigehall.domain.email;

import com.itextpdf.html2pdf.HtmlConverter;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

@Service
public class EmailServiceImplementation implements EmailService {

  @Autowired private JavaMailSender emailSender;
  @Autowired private SpringTemplateEngine templateEngine;
  private final Logger logger = LogManager.getLogger(EmailServiceImplementation.class);

  /**
   * Sends an email using the JavaMailSender and SpringTemplateEngine
   *
   * @param mail The mail object to be sent
   */
  @Async
  @Override
  public void sendEmail(Mail mail) {
    try {
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper =
          new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

      Context context = new Context();
      context.setVariables(mail.getVariables());

      String html = templateEngine.process(mail.getEmailTemplate(), context);
      helper.setTo(mail.getTo());
      helper.setText(html, true);
      helper.setSubject(mail.getSubject());
      helper.setFrom(mail.getFrom());

      if (!mail.getAttachments().isEmpty()) {
        mail.getAttachments().parallelStream()
            .forEach(
                attachment -> {
                  try {
                    helper.addAttachment(
                        attachment.getName(),
                        new ByteArrayResource(convertAttachmentToPdf(attachment).toByteArray()));
                  } catch (Exception e) {
                    logger.error("An error occurred when adding the attachment.");
                    logger.error(e);
                  }
                });
      }
      emailSender.send(message);
      logger.info("Email sent successfully.");
    } catch (Exception e) {
      logger.error("An error occurred when sending the email.");
      logger.error(e);
    }
  }

  /**
   * Convert an attachment to a pdf file.
   *
   * @param attachment The attachment to convert into pdf
   * @return A stream array of byte.
   */
  private ByteArrayOutputStream convertAttachmentToPdf(Attachment attachment) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    Context pdfContext = new Context();

    pdfContext.setVariables(attachment.getVariables());
    String html = templateEngine.process(attachment.getTemplate(), pdfContext);

    HtmlConverter.convertToPdf(html, outputStream);

    return outputStream;
  }
}

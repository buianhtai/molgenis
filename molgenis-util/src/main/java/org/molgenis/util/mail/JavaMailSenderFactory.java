package org.molgenis.util.mail;

import static java.lang.String.format;

import java.util.Properties;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class JavaMailSenderFactory implements MailSenderFactory {
  private static final Logger LOG = LoggerFactory.getLogger(JavaMailSenderFactory.class);

  private static Properties defaultProperties = new Properties();

  public static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
  public static final String MAIL_SMTP_QUITWAIT = "mail.smtp.quitwait";
  public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
  public static final String MAIL_SMTP_FROM_ADDRESS = "mail.from";

  @Override
  public JavaMailSenderImpl createMailSender(MailSettings mailSettings) {
    LOG.trace("createMailSender");
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(mailSettings.getHost());
    mailSender.setPort(mailSettings.getPort());
    mailSender.setProtocol(mailSettings.getProtocol());
    mailSender.setUsername(mailSettings.getUsername());
    mailSender.setPassword(mailSettings.getPassword());
    mailSender.setDefaultEncoding(mailSettings.getDefaultEncoding().name());
    Properties properties = new Properties(defaultProperties);
    defaultProperties.setProperty(MAIL_SMTP_STARTTLS_ENABLE, mailSettings.isStartTlsEnabled());
    defaultProperties.setProperty(MAIL_SMTP_QUITWAIT, mailSettings.isQuitWait());
    defaultProperties.setProperty(MAIL_SMTP_AUTH, mailSettings.isAuthenticationRequired());
    defaultProperties.setProperty(MAIL_SMTP_FROM_ADDRESS, mailSettings.getFromAddress());
    properties.putAll(mailSettings.getJavaMailProperties());
    LOG.debug("Mail properties: {}; defaults: {}", properties, defaultProperties);
    mailSender.setJavaMailProperties(properties);
    return mailSender;
  }

  @Override
  public void validateConnection(MailSettings mailSettings) {
    LOG.info("Validating mail settings...");
    try {
      JavaMailSenderImpl sender = createMailSender(mailSettings);
      sender.testConnection();
      LOG.info("OK.");
    } catch (MessagingException ex) {
      String message = format("Unable to ping to %s", mailSettings.getHost());
      LOG.info(message, ex);
      throw new IllegalStateException(message, ex);
    }
  }
}

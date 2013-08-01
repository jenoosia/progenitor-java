package com.nvx.tools.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mailer {
    
    private static final Logger log = LoggerFactory.getLogger(Mailer.class);
    
    private ExecutorService exec = Executors.newSingleThreadExecutor();
    
    private Session session = null;
    private Transport transport = null;
    
    private static Mailer mailer = null;
    private static Properties props = null;
    private static String defaultSender = "";
    private static boolean init = false;
    
    public static final String SMTP_HOST = "mail.smtp.host";
    public static final String SMTP_PORT = "mail.smtp.port";
    public static final String SMTP_AUTH = "mail.smtp.auth";
    public static final String SMTP_SSL_PORT = "mail.smtp.socketFactory.port";
    public static final String SMTP_SSL_FACTORY = "mail.smtp.socketFactory.class";
    public static final String SMTP_SSL_FACTORY_CLASS = "javax.net.ssl.SSLSocketFactory";
    
    public static final String CUSTOM_USR = "CUSTOM_USR";
    public static final String CUSTOM_PWD = "CUSTOM_PWD";
    
    public static void setDefaultSender(String sender) {
        defaultSender = sender;
    }
    public static String getDefaultSender() {
        return defaultSender;
    }
    
    public static void destroy() {
        if (mailer != null) {
            mailer.session = null;
            mailer.transport = null;
            mailer = null;
        }
        props = null;
        init = false;
    }
    
    public static void setMailerProps(String host, String port, boolean isAuth, 
            boolean isSsl, String user, String password, String defSender) {
        props = new Properties();
        props.put(SMTP_HOST, host);
        props.put(SMTP_PORT, port);
        if (isAuth) {
            props.put(SMTP_AUTH, "true");
            props.put(CUSTOM_USR, user);
            props.put(CUSTOM_PWD, password);
        }
        if (isSsl) {
            props.put(SMTP_SSL_PORT, port);
            props.put(SMTP_SSL_FACTORY, SMTP_SSL_FACTORY_CLASS);
        }
        defaultSender = defSender;
        init = true;
    }
    
    public static boolean isInit() {
        return init;
    }
    
    public static void rebuild() {
        if (props == null) {
            throw new IllegalStateException("Properties has not yet been initialized. Initialize it first.");
        }
        
        mailer = new Mailer();
        
        boolean isAuth = props.get(SMTP_AUTH) != null ? true : false;
        mailer.session = isAuth ? Session.getInstance(props, getAuthenticator(props.getProperty(CUSTOM_USR), props.getProperty(CUSTOM_PWD)))
                : Session.getDefaultInstance(props, null);
    }
    
    public static Mailer getInstance() {
        if (mailer == null) {
            rebuild();
        }
        
        return mailer;
    }

    private static Authenticator getAuthenticator(final String user, final String pwd) {
        return new Authenticator() { 
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pwd);
            }
        };
    }
    
    public void sendEmail(MailProperties mailProps, MailCallback mailCallback) throws MessagingException {
        if (transport == null || !transport.isConnected()) {
            transport = session.getTransport("smtp");
            transport.connect();
        }
        
        this.exec.submit(new MailerTask(mailProps, transport, session, mailCallback));
    }
    
    public class MailerTask implements Callable<String> {
        
        private MailProperties mailProps;
        private Transport transport;
        private Session session;
        private MailCallback callback;
        
        public MailerTask(MailProperties mailProps, Transport transport, 
                Session session, MailCallback callback) {
            this.mailProps = mailProps;
            this.transport = transport;
            this.session = session;
            this.callback = callback;
        }

        @Override
        public String call() throws Exception {
            log.info("Processing mail. Message: {}", mailProps.getTrackingId());
            try {

                MimeMessage message = new MimeMessage(session);
                message.setSubject(mailProps.getSubject());
                message.setFrom(new InternetAddress(mailProps.getSender()));
                
                log.info("Initializing mail body.");
                if (mailProps.isHasAttach()) {
                    List<BodyPart> attachments = new ArrayList<BodyPart>();
                    String[] attachNames = mailProps.getAttachNames();
                    String[] attachPaths = mailProps.getAttachFilePaths();
                    int attachLen = attachNames.length;
                    
                    for (int idx = 0; idx < attachLen; idx++) {
                        log.info("Attaching file: "  + attachNames[idx]);
                        BodyPart attachment = new MimeBodyPart();
                        attachment.setDataHandler(new DataHandler(
                                new FileDataSource(new File(attachPaths[idx]))));
                        attachment.setFileName(attachNames[idx]);
                        attachments.add(attachment);
                    }


                    BodyPart mainBody = new MimeBodyPart();
                    if(mailProps.isBodyHtml()) {
                        mainBody.setContent(mailProps.getBody(), "text/html");
                    } else {
                        mainBody.setText(mailProps.getBody());
                    }
                    
                    MimeMultipart multipart = new MimeMultipart();
                    multipart.addBodyPart(mainBody);
                    for (BodyPart attachment : attachments) {
                        multipart.addBodyPart(attachment);
                    }
                    
                    message.setContent(multipart);
                } else {
                    if (mailProps.isBodyHtml()) {
                        message.setContent(mailProps.getBody(), "text/html");
                    } else {
                        message.setText(mailProps.getBody());
                    }
                }

                for (String toUser : mailProps.getToUsers()) {
                    log.info("Adding recipient(TO) :: {}", toUser);
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(toUser));
                }

                String[] ccUsers = mailProps.getCcUsers();
                if (ccUsers != null && ccUsers.length > 0) {
                    for (String ccUser : ccUsers) {
                        log.info("Adding recipient(CC) :: {}", ccUser);
                        message.addRecipient(Message.RecipientType.CC, new InternetAddress(ccUser));
                    }
                }

                String[] bccUsers = mailProps.getBccUsers();
                if (bccUsers != null && bccUsers.length > 0) {
                    for (String bccUser : bccUsers) {
                        log.info("Adding recipient(BCC) :: {}", bccUser);
                        message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bccUser));
                    }
                }
                
                log.info("Sending email.");
                transport.sendMessage(message, message.getAllRecipients());
                
                log.info("Message sent. Cleaning up.");
                if (callback!=null) {
                    callback.complete(mailProps.getTrackingId(), true);
                }
            } catch (Exception e) {
                log.error("Error occurred while sending a message: " + e.getMessage(), e);
                if (callback!=null) {
                    callback.complete(mailProps.getTrackingId(), false);
                }
            }
            
            return mailProps.getTrackingId();
        }
        
    }
}

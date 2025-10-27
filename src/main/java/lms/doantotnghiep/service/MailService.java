package lms.doantotnghiep.service;

public interface MailService {
    void sendActivationEmail(String to,String name, String subject, String activateLink);
    void sendSuspiciousLoginEmail(String to, String name, String ip, String userAgent, String time);
}

package lms.doantotnghiep.service;

public interface MailService {
    void sendActivationEmail(String to,String name, String subject, String activateLink);

}

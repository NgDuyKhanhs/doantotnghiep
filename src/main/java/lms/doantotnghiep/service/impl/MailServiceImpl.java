package lms.doantotnghiep.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lms.doantotnghiep.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    @Async
    public void sendActivationEmail(String to, String name, String subject, String activateLink) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            // Nội dung email HTML
            String emailContent = "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f9f9f9; padding: 30px; }" +
                    ".container { max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); }" +
                    "h2 { color: #2c3e50; text-align: center; }" +
                    "p { font-size: 16px; color: #333333; line-height: 1.6; }" +
                    ".button { display: inline-block; padding: 12px 25px; font-size: 16px; color: #ffffff; background-color: #3498db; text-decoration: none; border-radius: 6px; margin-top: 20px; }" +
                    ".button:hover { background-color: #2980b9; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<h2>Kích hoạt tài khoản</h2>" +
                    "<p>Chào sinh viên, \""+ name +"\"</p>" +
                    "<p>Vui lòng nhấp vào liên kết dưới đây để kích hoạt tài khoản của bạn:</p>" +
                    "<p style='text-align: center;'><a href=\"" + activateLink + "\" class='button'>Kích hoạt tài khoản</a></p>" +
                    "<p>Nếu bạn không yêu cầu tạo tài khoản, vui lòng bỏ qua email này.</p>" +
                    "<p>Trân trọng!" +
                    "</div>" +
                    "</body>" +
                    "</html>";


            // Thiết lập nội dung email là HTML
            helper.setText(emailContent, true);

            // Gửi email
            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            e.printStackTrace();
            // Xử lý lỗi khi gửi email
        }
    }

    @Override
    @Async
    public void sendSuspiciousLoginEmail(String to, String name, String ip, String userAgent, String time) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("⚠️ Cảnh báo đăng nhập bất thường");

            // Nội dung email HTML
            String emailContent = "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f9f9f9; padding: 30px; }" +
                    ".container { max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); }" +
                    "h2 { color: #e74c3c; text-align: center; }" +
                    "p { font-size: 16px; color: #333333; line-height: 1.6; }" +
                    ".info { background: #f2f2f2; padding: 10px; border-radius: 6px; font-size: 14px; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<h2>⚠️ Cảnh báo đăng nhập bất thường</h2>" +
                    "<p>Xin chào <b>" + name + "</b>,</p>" +
                    "<p>Chúng tôi phát hiện một lần đăng nhập từ thiết bị hoặc địa chỉ IP lạ:</p>" +
                    "<div class='info'>" +
                    "<p><b>Thời gian:</b> " + time + "</p>" +
                    "<p><b>Địa chỉ IP:</b> " + ip + "</p>" +
                    "<p><b>Thiết bị (User-Agent):</b> " + userAgent + "</p>" +
                    "</div>" +
                    "<p>Nếu đây không phải là bạn, vui lòng đổi mật khẩu ngay lập tức để bảo vệ tài khoản.</p>" +
                    "<p>Trân trọng,<br/>Đội ngũ bảo mật LMS</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            helper.setText(emailContent, true);

            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            e.printStackTrace();
            // Xử lý lỗi khi gửi email
        }
    }

}

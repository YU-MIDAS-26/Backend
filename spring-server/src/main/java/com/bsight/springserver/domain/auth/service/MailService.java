package com.bsight.springserver.domain.auth.service;

import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from}")
    private String from;

    public void sendEmailVerificationCode(String to, String code) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            message.setFrom(from);
            message.setRecipients(MimeMessage.RecipientType.TO, to);
            message.setSubject("[B-SIGHT] 이메일 인증번호 안내", "UTF-8");
            message.setContent(createEmailVerificationHtml(code), "text/html; charset=UTF-8");

            javaMailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private String createEmailVerificationHtml(String code) {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <title>B-SIGHT 이메일 인증</title>
                </head>
                <body style="margin:0; padding:0; background:#f4f7f9; font-family:Arial, sans-serif;">
                    <div style="max-width:560px; margin:40px auto; background:#ffffff; border-radius:16px; padding:32px; border:1px solid #e5edf2;">
                        <h2 style="margin:0 0 16px; color:#263845;">B-SIGHT 이메일 인증</h2>
                        <p style="font-size:15px; line-height:1.7; color:#4b5b66;">
                            안녕하세요.<br>
                            B-SIGHT 회원가입을 위한 이메일 인증번호를 안내드립니다.
                        </p>
                        <div style="margin:28px 0; padding:22px; background:#eaf4fa; border-radius:12px; text-align:center;">
                            <div style="font-size:14px; color:#607583; margin-bottom:8px;">인증번호</div>
                            <div style="font-size:34px; font-weight:700; letter-spacing:6px; color:#2f6f91;">
                                %s
                            </div>
                        </div>
                        <p style="font-size:14px; line-height:1.7; color:#6b7c86;">
                            인증번호는 5분 동안만 유효합니다.<br>
                            본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(code);
    }
}
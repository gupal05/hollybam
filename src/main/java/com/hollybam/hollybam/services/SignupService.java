package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_SignupDao;
import com.hollybam.hollybam.dto.GuestDto;
import com.hollybam.hollybam.dto.MemberDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
public class SignupService implements IF_SignupService{
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender mailSender;

    private final IF_SignupDao signupDao;

    public SignupService(IF_SignupDao signupDao) {
        this.signupDao = signupDao;
    }

    @Override
    @Transactional
    public int dupCheckId(String memberId){
        try {
            return signupDao.dupCheckId(memberId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public int signup(MemberDto memberDto) {
        try {
            if(memberDto.getMemberLoginType().equals("web")){
                String encodedPw = passwordEncoder.encode(memberDto.getMemberPw());
                memberDto.setMemberPw(encodedPw);
            }
            return signupDao.signup(memberDto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public void deleteGuestByDi(String uuid){
        signupDao.deleteGuestByDi(uuid);
    }

    @Override
    public int getMemberCode(String memberId){
        return signupDao.getMemberCode(memberId);
    }

    @Override
    public int getSignupCouponCode() {
        return signupDao.getSignupCouponCode();
    }

    @Override
    @Transactional
    public void insertSignupCoupon(int memberCode, int signupCouponCode){
        signupDao.insertSignupCoupon(memberCode, signupCouponCode);
    }

    @Override
    public int isRecodeSignup(String di){
        return signupDao.isRecodeSignup(di);
    }

    @Override
    public String getMemberType(String di){
        return signupDao.getMemberType(di);
    }

    @Override
    public int getGuestCartCount(GuestDto guest){
        return signupDao.getGuestCartCount(guest);
    }

    @Override
    public int getGuestWishCount(GuestDto guest){
        return signupDao.getGuestWishCount(guest);
    }

    @Override
    public int getGuestOrderCount(GuestDto guest){
        return signupDao.getGuestOrderCount(guest);
    }

    @Override
    public int getGuestInquiryCount(GuestDto guest){
        return signupDao.getGuestInquiryCount(guest);
    }

    @Override
    @Transactional
    public void updateGuestToMemberCart(int memberCode, int guestCode){
        signupDao.updateGuestToMemberCart(memberCode, guestCode);
    }

    @Override
    @Transactional
    public void updateGuestToMemberWishList(int memberCode, int guestCode){
        signupDao.updateGuestToMemberWishList(memberCode, guestCode);
    }

    @Override
    @Transactional
    public void updateGuestToMemberOrder(int memberCode, int guestCode){
        signupDao.updateGuestToMemberOrder(memberCode, guestCode);
    }

    @Override
    @Transactional
    public void updateGuestToMemberInquiry(int memberCode, int guestCode){
        signupDao.updateGuestToMemberInquiry(memberCode, guestCode);
    }

    public String mainAuth(String email, HttpSession session) {
        String message = null;

        // 이메일 인증 코드 생성 (랜덤 6자리)
        Random random = new Random();
        int randomInt = random.nextInt(888889)+111111; // 111111~999999까지의 난수 생성

        // 이메일 양식
        String title = "홀리밤 회원가입 인증 이메일 입니다.";
        String content = this.buildEmailTemplate(randomInt);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            String setFrom = "hollybam00@naver.com";
            helper.setFrom(setFrom);
            helper.setTo(email);
            helper.setSubject(title);
            helper.setText(content, true);

            mailSender.send(mimeMessage);
            message = "인증번호가 전송 되었습니다.";
            session.setAttribute("mailCode", randomInt);
        } catch (MessagingException e) {
            e.printStackTrace();
            message = "잠시 후에 다시 시도해주세요.";
        }
        return message;
    }

    private String buildEmailTemplate(int verificationCode) {
        return "<!DOCTYPE html>" +
                "<html lang='ko'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>홀리밤 이메일 인증</title>" +
                "</head>" +
                "<body style='margin: 0; padding: 20px; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", system-ui, sans-serif; background: #f8fafc;'>" +
                "    <div style='width: 100%; max-width: 420px; margin: 0 auto;'>" +
                "        <div style='background: white; border-radius: 16px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); overflow: hidden;'>" +
                "            " +
                "            <!-- 로고 섹션 -->" +
                "            <div style='background: white; padding: 32px 24px 20px; text-align: center; position: relative; border-bottom: 1px solid #f1f5f9;'>" +
                "                <div style='position: absolute; top: 0; left: 0; right: 0; height: 3px; background: linear-gradient(90deg, #EE386D 0%, #ff6b9d 100%);'></div>" +
                "                <img src='https://hollybam-static-image.s3.ap-northeast-2.amazonaws.com/images/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7+2025-07-30+083845.png' " +
                "                     alt='홀리밤' style='width: 120px; height: auto;' />" +
                "            </div>" +
                "            " +
                "            <!-- 메인 콘텐츠 -->" +
                "            <div style='padding: 24px;'>" +
                "                <div style='text-align: center; margin-bottom: 20px;'>" +
                "                    <h1 style='color: #0f172a; margin: 0 0 6px 0; font-size: 22px; font-weight: 700;'>이메일 인증</h1>" +
                "                    <p style='color: #64748b; margin: 0; font-size: 15px;'>회원가입 완료를 위해 아래 인증번호를 입력해주세요</p>" +
                "                </div>" +
                "                " +
                "                <!-- 인증번호 카드 -->" +
                "                <div style='background: #f8fafc; border: 2px solid #e2e8f0; border-radius: 12px; padding: 20px; text-align: center; margin-bottom: 16px; position: relative;'>" +
                "                    <div style='position: absolute; top: -1px; left: 50%; transform: translateX(-50%); background: white; padding: 0 10px; color: #64748b; font-size: 11px; font-weight: 600; text-transform: uppercase;'>인증번호</div>" +
                "                    <div style='color: #0f172a; font-size: 32px; font-weight: 800; letter-spacing: 6px; font-family: \"SF Mono\", ui-monospace, monospace; margin-top: 4px;'>" + verificationCode + "</div>" +
                "                </div>" +
                "                " +
                "                <!-- 안내사항 -->" +
                "                <div style='text-align: center; color: #64748b; font-size: 13px;'>" +
                "                    인증번호는 <strong>10분간</strong> 유효합니다" +
                "                </div>" +
                "            </div>" +
                "            " +
                "            <!-- 푸터 -->" +
                "            <div style='background: #f8fafc; border-top: 1px solid #e2e8f0; padding: 16px 24px; text-align: center;'>" +
                "                <div style='color: #94a3b8; font-size: 12px;'>" +
                "                    <div style='font-weight: 600; margin-bottom: 2px;'>홀리밤 HollyBam</div>" +
                "                    <div>고객센터 1533-4965</div>" +
                "                </div>" +
                "            </div>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    @Override
    public int getMemberCodeByMemberId(String memberId){
        return getMemberCodeByMemberId(memberId);
    }
}

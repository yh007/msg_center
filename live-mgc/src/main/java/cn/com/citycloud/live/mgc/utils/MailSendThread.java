package cn.com.citycloud.live.mgc.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.citycloud.live.mgc.dto.MailDto;
import cn.com.citycloud.live.mgc.mail.MailSendService;

public class MailSendThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MailSendThread.class);
    private static final int SEND_TIMES_MAX = 3; //发送次数限制
    private MailSendService mailSendService;
    private MailDto mailDto;

    public MailSendThread(MailSendService mailSendService, MailDto mailDto) {
        super();
        this.mailSendService = mailSendService;
        this.mailDto = mailDto;
    }

    @Override
    public void run() {
        sendMail(1);
    }
    
    public void sendMail(int times) {
        try {
            if (times > SEND_TIMES_MAX )
                return;
            
            Map<String, Object> templateParameter = new HashMap<String, Object>();
            templateParameter.put("checkUrl", mailDto.getCheckUrl());
            switch (mailDto.getMailType()) {
                case 1 : {
                    templateParameter.put("user", mailDto);
                    mailSendService.sendMail(MailTemplateType.registerCheck, templateParameter, mailDto.getEmail());
                    break;
                }
                case 2 : {
                    templateParameter.put("name", mailDto.getName());
                    mailSendService.sendMail(MailTemplateType.retrivePassword, templateParameter, mailDto.getEmail());
                    break;
                }
                default : break;
            }
            logger.info("发送邮件成功，[times="+times+",name=" + mailDto.getName() + "],[mailType="+mailDto.getMailType() + "],[email="+mailDto.getEmail()+"] !");

        } catch (Exception e) {
            try {
                times++;
                TimeUnit.SECONDS.sleep(10); //等待10秒
            } catch (Exception e1) {
                logger.error("发送邮件失败，等待10秒出错", e1);
            }
            logger.error("邮件发送MQ监听器发送失败【重发，第"+times+"次】", e);
            sendMail(times); //第count次重发
        }
    }
    
    
}

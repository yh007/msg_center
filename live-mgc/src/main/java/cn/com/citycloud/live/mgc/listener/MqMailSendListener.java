package cn.com.citycloud.live.mgc.listener;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cn.com.citycloud.live.mgc.dto.MailDto;
import cn.com.citycloud.live.mgc.mail.MailSendService;
import cn.com.citycloud.live.mgc.utils.MailSendThread;

public class MqMailSendListener implements MessageListener{

    private static final Logger logger = LoggerFactory.getLogger(MqMailSendListener.class);
    
    @Autowired
    private MailSendService mailSendService;
    
    @Resource
    private ThreadPoolTaskExecutor taskExecutor;

    public MqMailSendListener() {

    }

    /**
     * 监听邮件队列
     */
    @Override
    public void onMessage(Message arg0) {
        try {
            ObjectMessage objectMessage = (ObjectMessage) arg0;
            MailDto mailDto = (MailDto) objectMessage.getObject();
            
            //发送邮件
            taskExecutor.execute(new MailSendThread(mailSendService, mailDto));
        } catch (Exception e) {
            logger.error("邮件发送MQ监听器发送失败", e);
        }
    }
    
    
}

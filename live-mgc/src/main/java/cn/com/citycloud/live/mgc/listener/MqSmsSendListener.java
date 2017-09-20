package cn.com.citycloud.live.mgc.listener;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import cn.com.citycloud.live.mgc.dto.SmsDto;
import cn.com.citycloud.live.mgc.sms.service.SmsService;
import cn.com.citycloud.live.mgc.utils.MgcUtil;
import cn.com.citycloud.live.mgc.utils.SmsSendThread;

public class MqSmsSendListener implements MessageListener{

    private static final Logger logger = LoggerFactory.getLogger(MqSmsSendListener.class);
    
    @Resource 
    private SmsService smsService;

    @Resource
    private ThreadPoolTaskExecutor taskExecutor;
    
    public MqSmsSendListener() {

    }
    
    /**
     * 监听短信队列
     */
    @Override
    public void onMessage(Message arg0) {
        try {
            ObjectMessage objectMessage = (ObjectMessage) arg0;
            SmsDto smsDto = (SmsDto) objectMessage.getObject();
            taskExecutor.execute(new SmsSendThread(StringUtils.isEmpty(smsDto.getChannel()) ? MgcUtil.getdefaultGate() : smsDto.getChannel(), smsDto, smsService));
        } catch (Exception e) {
            logger.error("短信发送MQ监听器发送失败", e);
        }
        
    }

    
}

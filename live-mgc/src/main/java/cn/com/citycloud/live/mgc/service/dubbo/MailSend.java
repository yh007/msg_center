package cn.com.citycloud.live.mgc.service.dubbo;

import javax.annotation.Resource;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import cn.com.citycloud.live.common.ErrorCodeEnum;
import cn.com.citycloud.live.common.ResponseVo;
import cn.com.citycloud.live.mgc.dto.MailDto;
import cn.com.citycloud.live.mgc.intf.IMailSend;
import cn.com.citycloud.live.mgc.utils.MgcUtil;

/**
 *  发送邮件,接收邮件发送请求到mq
 * @author yehuan
 */
@Service("mailSend")
public class MailSend implements IMailSend{
    
    private static final Logger logger = LoggerFactory.getLogger(MailSend.class);

    @Resource 
    private JmsTemplate liveMailJmsTemplate;

    @Override
    public ResponseVo<?> sendMail(final MailDto mailDto) {
        ResponseVo<?> responseVo = null;
        try {
            MgcUtil.checkMail(mailDto);
            
            liveMailJmsTemplate.send(new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    ObjectMessage objectMessage = session.createObjectMessage(mailDto);
                    //设置消息为非持久化
                    objectMessage.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
                    return objectMessage;
                }
            });
            logger.info("接收发送邮件请求成功,[name=" + mailDto.getName() + "],[mailType="+mailDto.getMailType() + "],[email="+mailDto.getEmail()+"] !");
            responseVo = ResponseVo.getSuccessResponse();
        } catch (Exception e) {
            responseVo = ErrorCodeEnum.ERROR_MAIL_REQUEST_SEND__ERROR.getResponseVo();
            logger.error("接收发送邮件请求出错！", e);
        }
        return responseVo;
    }


}

package cn.com.citycloud.live.mgc.service.dubbo;

import javax.annotation.Resource;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import cn.com.citycloud.live.common.ErrorCodeEnum;
import cn.com.citycloud.live.common.ResponseVo;
import cn.com.citycloud.live.mgc.dto.SmsDto;
import cn.com.citycloud.live.mgc.intf.ISmsSend;
import cn.com.citycloud.live.mgc.sms.entity.SmsEntity;
import cn.com.citycloud.live.mgc.sms.service.SmsService;
import cn.com.citycloud.live.mgc.utils.MgcUtil;

/**
 *  发送短信,接收短信发送请求到mq
 * @author yehuan
 */
@Service("smsSend")
@Path("sms")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class SmsSend implements ISmsSend{
    
    private static final Logger logger = LoggerFactory.getLogger(SmsSend.class);

    @Resource 
    private JmsTemplate liveSmsJmsTemplate;

    @Resource 
    private SmsService smsService;
    
    @POST
    @Path("send")
    @Override
    public ResponseVo<?> sendSms(final SmsDto smsDto) {
        logger.info("接收发送请求开始: phone[{}],code[{}],channel[{}]", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel());
        ResponseVo<?> responseVo = null;
        try {
            MgcUtil.checkSms(smsDto);

            String templateContent = smsService.getTemplateContent(smsDto.getCode());
            if (StringUtils.isEmpty(templateContent)) {
                logger.error("接收发送请求: phone[{}],code[{}],channel[{}], 短信模板不存在 !", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel());
                return ErrorCodeEnum.ERROR_SMS_TEMPLATE_NOT_EXIST.getResponseVo();
            }
            
            smsDto.setContent(MgcUtil.replaceSmsPlaceholder(templateContent, smsDto.getParams())); //拼装短信内容
            if (smsDto.getContent().indexOf("$param") > -1) {
                logger.error("接收发送请求: phone[{}],code[{}],channel[{}], 模板参数不正确!", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel());
                return ErrorCodeEnum.ERROR_SMS_REQUEST_PARAMS_ERROR.getResponseVo();
            }
            
            //插到mongodb库lives中
            SmsEntity smsEntity = new SmsEntity(smsDto.getPhone(), smsDto.getContent(), smsDto.getCode());
            smsEntity.setCreateTime(MgcUtil.getMongodbCurDate());
            smsService.insert(smsEntity);

            //看开关是否发送
            if (!MgcUtil.SMS_SWITCH_ON.equals(MgcUtil.getSmsConfProperty("sms.send.switch"))) {
                SmsEntity bean = new SmsEntity();
                bean.setId(smsEntity.getId());
                bean.setStatus(MgcUtil.SMS_STATUS_1);
                smsService.update(bean, true);
                return ResponseVo.getSuccessResponse();
            }
            
            //保存刚插入的ID
            if (smsEntity.getId() != null) {
                smsDto.setId(smsEntity.getId());
            } else {
                logger.error("接收发送请求: phone[{}],code[{}],channel[{}], 插入到mongodb出错!", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel());
                throw new Exception("插入到mongodb出错");
            }
            
            liveSmsJmsTemplate.send(new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    ObjectMessage objectMessage = session.createObjectMessage(smsDto);
                    //设置消息为非持久化
                    objectMessage.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
                    logger.info("接收发送请求,消息已发送到队列: phone[{}],code[{}],channel[{}],id[{}]", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel(), smsDto.getId());
                    return objectMessage;
                }
            });
            responseVo = ResponseVo.getSuccessResponse();
            logger.info("接收发送请求结束: phone[{}],code[{}],channel[{}],id[{}]", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel(), smsDto.getId());
        } catch (Exception e) {
            responseVo = ErrorCodeEnum.ERROR_SMS_REQUEST_SEND_ERROR.getResponseVo();
            logger.error("接收发送请求出错: phone[{}],code[{}],channel[{}],id[{}]", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel(), smsDto.getId(), e);
        };
        return responseVo;
    }

}

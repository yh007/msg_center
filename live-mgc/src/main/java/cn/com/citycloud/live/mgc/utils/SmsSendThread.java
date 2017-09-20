package cn.com.citycloud.live.mgc.utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.com.citycloud.live.mgc.dto.SmsDto;
import cn.com.citycloud.live.mgc.sms.entity.SmsEntity;
import cn.com.citycloud.live.mgc.sms.service.SmsService;

public class SmsSendThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SmsSendThread.class);
    private static final int SEND_TIMES_LIMIT = 5; //发送次数限制,暂定5次
    private SmsService smsService;
    private String curGate;
    private SmsDto smsDto;
    private boolean isReSend = false;
    private int reSendCount = 0;

    public SmsSendThread(String curGate, SmsDto smsDto, SmsService smsService) {
        super();
        this.curGate = curGate;
        this.smsDto = smsDto;
        this.smsService = smsService;
    }

    @Override
    public void run() {
        sendSms(curGate);
    }
    
    /**
     * 发送短信
     * @param sendGate 当前网关名
     */
    public void sendSms(String sendGate) {
        if (StringUtils.isEmpty(sendGate)) {
            logger.error("发送短信,所有网关发送失败: phone[{}],code[{}],channel[{}],sendGate[{}],id[{}]", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel(), sendGate, smsDto.getId());
            updateSendFail(null, sendGate);
            return;
        }

        SmsEntity smsEntity = smsService.findOne(smsDto.getId());
        if (smsEntity == null) {
            logger.error("发送短信,mongodb数据丢失: phone[{}],code[{}],channel[{}],sendGate[{}],id[{}]", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel(), sendGate, smsDto.getId());
            return;
        }
        if (smsEntity.getSendTimes() >= SEND_TIMES_LIMIT || reSendCount >= SEND_TIMES_LIMIT) {
            logger.error("发送短信,发送次数超过限制: phone[{}],code[{}],channel[{}],sendGate[{}],id[{}],sendTimes[{}],reSendCount[{}]", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel(), sendGate, smsDto.getId(), smsEntity.getSendTimes(), reSendCount);
            return;
        }
        
        if (MgcUtil.SMS_GATE_WEBCHINESE.equals(sendGate)) {
                NameValuePair[] data = {
                        new NameValuePair("Uid", MgcUtil.getSmsConfProperty("sms.host.name")),
                        new NameValuePair("Key", MgcUtil.getSmsConfProperty("sms.host.key")),
                        new NameValuePair("smsMob", smsDto.getPhone()),
                        new NameValuePair("smsText", smsDto.getContent()) };
                String result = executeHttpRequest(MgcUtil.getSmsConfProperty("sms.host.uri"), data, "gbk", sendGate);
                if (StringUtils.isEmpty(result)) {
                    reSend(smsDto, sendGate);
                    return;
                }
                
                try {
                    //发送成功
                    if (Integer.parseInt(result) == MgcUtil.SMS_STATUS_1) {
                        updateSendSuccess(smsEntity, smsDto, sendGate);
                    } else {
                        smsEntity.setSendTimes(smsEntity.getSendTimes() + 1);//记录发送次数
                        smsService.update(smsEntity, true);
                        logger.error("发送短信失败: result[{}],phone[{}],code[{}],channel[{}],sendGate[{}],id[{}]", result, smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel(), sendGate, smsDto.getId());
                        reSend(smsDto, sendGate);
                    }
                } catch (Exception e) {
                    logger.error("发送短信，修改数据出错", e);
                    updateSendFail(smsEntity, sendGate);
                }
            
        } else if (MgcUtil.SMS_GATE_SMSCN.equals(sendGate)) {
                //全文发送
                NameValuePair[] data = {
                            new NameValuePair("ac", MgcUtil.getSmsConfProperty("sms.smscn.ac")),
                            new NameValuePair("uid", MgcUtil.getSmsConfProperty("sms.smscn.uid")),
                            new NameValuePair("pwd", MgcUtil.getSmsConfProperty("sms.smscn.pwd")),
                            new NameValuePair("mobile",smsDto.getPhone()),
                            new NameValuePair("content",smsDto.getContent())};
                //例: {"stat":"100","message":"发送成功"}, {"stat":"101","message":"发送失败"}
                String result = executeHttpRequest(MgcUtil.getSmsConfProperty("sms.smscn.uri"), data, "utf-8", sendGate);
                if (StringUtils.isEmpty(result)) {
                    reSend(smsDto, sendGate);
                    return;
                }
                try {
                    //发送成功
                    JSONObject jsonObj = JSON.parseObject(result);
                    if (jsonObj.getIntValue("stat") == 100) {
                        updateSendSuccess(smsEntity, smsDto, sendGate);
                    } else {
                        smsEntity.setSendTimes(smsEntity.getSendTimes() + 1);//记录发送次数
                        smsService.update(smsEntity, true);
                        logger.error("发送短信失败: result[{}],phone[{}],code[{}],channel[{}],sendGate[{}],id[{}]", result, smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel(), sendGate, smsDto.getId());
                        reSend(smsDto, sendGate);
                    }
                } catch (Exception e) {
                    logger.error("发送短信，修改数据出错: phone[{}],code[{}],channel[{}],sendGate[{}],id[{}]", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel(), sendGate, smsDto.getId(), e);
                    updateSendFail(smsEntity, sendGate);
                }
                
        } else {
            logger.error("发送短信无此网关, phone[{}],code[{}],channel[{}],sendGate[{}],id[{}]", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel(), sendGate, smsDto.getId());
            updateSendFail(smsEntity, sendGate);
        }
        
    }
    
    public void reSend(SmsDto smsDto, String curSendGate) {
        reSendCount++;
        String sendGate = null;
        if (StringUtils.isEmpty(smsDto.getChannel())) {
            sendGate = MgcUtil.getNextGate(curSendGate);
        } else {
            if (!isReSend) {
                sendGate = MgcUtil.getdefaultGate();
            } else {
                sendGate = MgcUtil.getNextGate(sendGate);
            }
            while (smsDto.getChannel().equals(sendGate)) {
                sendGate = MgcUtil.getNextGate(sendGate);
            }
        }
        isReSend = true;
        logger.info("发送短信-->重发: phone[{}],code[{}],channel[{}],sendGate[{}],id[{}],reSendCount[{}]]", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel(), sendGate, smsDto.getId(), reSendCount);
        sendSms(sendGate);
    }
    
    /**
     * 执行http请求
     */
    public String executeHttpRequest(String uri, NameValuePair[] data, String resEncode, String sendGate) {
        String result = null;
        PostMethod post = new PostMethod(uri);;
        try {
            HttpClient client = new HttpClient();
            post.addRequestHeader("Content-Type","application/x-www-form-urlencoded;charset=utf-8");//在头文件中设置转码,注：或utf8
            post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8");  //smscn一定用
            post.setRequestBody(data);
            client.executeMethod(post);
            result = new String(post.getResponseBodyAsString().getBytes(resEncode), resEncode);
        } catch(Exception e) {
            logger.error("网关发送连接失败: phone[{}],code[{}],channel[{}],sendGate[{}],id[{}]", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel(), sendGate, smsDto.getId(), e);
        } finally {
            post.releaseConnection();
        }
        return result;
    }
    
    public void updateSendSuccess(SmsEntity smsEntity, SmsDto smsDto, String sendGate) {
        smsEntity.setId(smsDto.getId());
        smsEntity.setStatus(MgcUtil.SMS_STATUS_1);
        smsEntity.setSendTimes(smsEntity.getSendTimes() + 1);
        smsEntity.setSendGate(sendGate);
        smsService.update(smsEntity, true);
        logger.info("发送短信成功: phone[{}],code[{}],channel[{}],sendGate[{}],id[{}]", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel(), sendGate, smsDto.getId());
    }
    
    public void updateSendFail(SmsEntity entity, String sendGage) {
        try {
            SmsEntity smsEntity = new SmsEntity();
            smsEntity.setId(smsDto.getId());
            smsEntity.setStatus(MgcUtil.SMS_STATUS_2);
            if (entity != null) {
                smsEntity.setSendTimes(entity.getSendTimes() + 1);
                smsService.update(smsEntity, true);
            } else {
                smsService.update(smsEntity, false);
            }
        } catch (Exception e) {
            logger.error("发送短信,修改发送状态[2:发送失败]出错: phone[{}],code[{}],channel[{}],sendGate[{}],id[{}]", smsDto.getPhone(), smsDto.getCode(), smsDto.getChannel(), sendGage, smsDto.getId(), e);
        }
    }

}

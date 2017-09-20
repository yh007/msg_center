package cn.com.citycloud.live.mgc.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import cn.com.citycloud.live.common.utils.TextValidator;
import cn.com.citycloud.live.mgc.dto.MailDto;
import cn.com.citycloud.live.mgc.dto.SmsDto;

public class MgcUtil {

    private static final Logger logger = LoggerFactory.getLogger(MgcUtil.class);
    
    //邮件相关配置
    public static final int MAIL_TYPE_1 = 1;   //1:注册
    public static final int MAIL_TYPE_2 = 2;   //1:找回密码
    
    //短信相关配置
    public static final String SMS_GATE_WEBCHINESE = "webchinese";
    public static final String SMS_GATE_SMSCN = "smscn";
    public static final int SMS_STATUS_0 = 0;   //待发送
    public static final int SMS_STATUS_1 = 1;   //发送成功
    public static final int SMS_STATUS_2 = 2;   //发送失败
    public static final String SMS_SWITCH_ON = "on";   //开关开
    public static final String SMS_SWITCH_OFF = "off";   //开关开
    public static final int MONGODB_TIMEZONE_INTERVAL = 8;   //mongodb时区间隔
    
    private static Map<String, String> smsConfigInfoMap = new HashMap<String, String>();//短信网关配置信息
    private static List<String> smsSendGateList = new ArrayList<String>();//短信网关发送顺序
    private static Set<Integer> smsValidCodeSet = new LinkedHashSet<Integer>();//保存有效模板编码
    
    private MgcUtil() {
        
    }
    
    /**
     * 验证邮件是否有效
     * @param mailDto 邮件对象
     */
    public static void checkMail(MailDto mailDto) {
        Assert.notNull(mailDto, "邮件信息不能为空");
        Assert.notNull(mailDto.getName(), "用户不能为空");
        Assert.notNull(mailDto.getCheckUrl(), "链接不能为空");
        Assert.notNull(mailDto.getEmail(), "邮箱地址不能为空");
        Assert.isTrue(mailDto.getMailType() == MAIL_TYPE_1 || mailDto.getMailType() == MAIL_TYPE_2, "邮件类型不正确");
    }
    
    /**
     * 验证短信是否有效
     * @param smsDto 短信对象
     */
    public static void checkSms(SmsDto smsDto) {
        Assert.notNull(smsDto, "短信信息不能为空");
        Assert.notNull(smsDto.getPhone(), "手机号不能为空");
        Assert.isTrue(TextValidator.checkCellphone(smsDto.getPhone()), "手机号不合法");
    }
    
    /**
     * 获取网关配置信息
     */
    public static String getSmsConfProperty(String key) {
        return smsConfigInfoMap.get(key);
    }
    
    /**
     * 获取第一个网关
     */
    public static String getdefaultGate() {
        try {
            return smsSendGateList.get(0);
        } catch (Exception e) {
            logger.error("获取第一个网关出错", e);
            return null;
        }
    }
    
    /**
     * 获取下一个网关
     * @param curGate 当前网关
     */
    public static String getNextGate(String curGate) {
        try {
            int index = smsSendGateList.indexOf(curGate);
            if (index == -1 || index == smsSendGateList.size() - 1)
                return null;
            return smsSendGateList.get(index + 1);
        } catch (Exception e) {
            logger.error("获取下一个发送网关出错", e);
            return null;
        }
    }
    
    /**
     * 替换短信模板内容
     */
    public static String replaceSmsPlaceholder(String template, String[] params) {
        if (params == null || params.length == 0) {
            return template;
        }
        for (int i=0; i<params.length; i++) {
            template = template.replace("$param" + i + "$", params[i]);
        }
        return template;
    }
    
    /**
     * 获取的mongdb对应的当前时间
     */
    public static Date getMongodbCurDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, MONGODB_TIMEZONE_INTERVAL);
        return calendar.getTime();
    }

    public static Map<String, String> getSmsConfigInfoMap() {
        return smsConfigInfoMap;
    }

    public static List<String> getSmsSendGateList() {
        return smsSendGateList;
    }

    public static Set<Integer> getSmsValidCodeSet() {
        return smsValidCodeSet;
    }

    public static void main(String[] args) throws HttpException, IOException {
//        System.out.println("-----: " + 
//                sendSms("http://utf8.sms.webchinese.cn", "城云科技", "efe623c0d6b04ae37d65", "15068860843", "您的上网密码为:123123"));
    }

    
}

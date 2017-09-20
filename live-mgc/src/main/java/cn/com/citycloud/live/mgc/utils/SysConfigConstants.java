package cn.com.citycloud.live.mgc.utils;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SysConfigConstants{

    @Value("${sms.host.uri}")
    private String smsHostUri;

    @Value("${sms.host.name}")
    private String smsHostName;

    @Value("${sms.host.key}")
    private String smsHostKey;

    @Value("${sms.send.gate.order}")
    private String smsSendGateOrder;

//    @Value("${sms.send.valid.codes}")
//    private String smsSendValidCodes;

    @Value("${sms.send.switch}")
    private String smsSendSwitch;

    @Value("${sms.smscn.uri}")
    private String smsSmscnUri;
    
    @Value("${sms.smscn.ac}")
    private String smsSmscnAc;
    
    @Value("${sms.smscn.uid}")
    private String smsSmscnUid;
    
    @Value("${sms.smscn.pwd}")
    private String smsSmscnPwd;

    public String getSmsHostUri() {
        return smsHostUri;
    }

    public void setSmsHostUri(String smsHostUri) {
        this.smsHostUri = smsHostUri;
    }

    public String getSmsHostName() {
        return smsHostName;
    }

    public void setSmsHostName(String smsHostName) {
        this.smsHostName = smsHostName;
    }

    public String getSmsHostKey() {
        return smsHostKey;
    }

    public void setSmsHostKey(String smsHostKey) {
        this.smsHostKey = smsHostKey;
    }

    public String getSmsSendGateOrder() {
        return smsSendGateOrder;
    }

    public void setSmsSendGateOrder(String smsSendGateOrder) {
        this.smsSendGateOrder = smsSendGateOrder;
    }

//    public String getSmsSendValidCodes() {
//        return smsSendValidCodes;
//    }
//
//    public void setSmsSendValidCodes(String smsSendValidCodes) {
//        this.smsSendValidCodes = smsSendValidCodes;
//    }

    public String getSmsSendSwitch() {
        return smsSendSwitch;
    }

    public void setSmsSendSwitch(String smsSendSwitch) {
        this.smsSendSwitch = smsSendSwitch;
    }

    public String getSmsSmscnUri() {
        return smsSmscnUri;
    }

    public void setSmsSmscnUri(String smsSmscnUri) {
        this.smsSmscnUri = smsSmscnUri;
    }

    public String getSmsSmscnAc() {
        return smsSmscnAc;
    }

    public void setSmsSmscnAc(String smsSmscnAc) {
        this.smsSmscnAc = smsSmscnAc;
    }

    public String getSmsSmscnUid() {
        return smsSmscnUid;
    }

    public void setSmsSmscnUid(String smsSmscnUid) {
        this.smsSmscnUid = smsSmscnUid;
    }

    public String getSmsSmscnPwd() {
        return smsSmscnPwd;
    }

    public void setSmsSmscnPwd(String smsSmscnPwd) {
        this.smsSmscnPwd = smsSmscnPwd;
    }
    
    @PostConstruct
    public void initConfig() {
        MgcUtil.getSmsConfigInfoMap().put("sms.host.uri", getSmsHostUri().trim());
        MgcUtil.getSmsConfigInfoMap().put("sms.host.name", getSmsHostName().trim());
        MgcUtil.getSmsConfigInfoMap().put("sms.host.key", getSmsHostKey().trim());
        
        MgcUtil.getSmsConfigInfoMap().put("sms.send.gate.order", getSmsSendGateOrder().trim());
//        MgcUtil.getSmsConfigInfoMap().put("sms.send.valid.codes", getSmsSendValidCodes().trim());
        MgcUtil.getSmsConfigInfoMap().put("sms.send.switch", getSmsSendSwitch().trim());
        
        MgcUtil.getSmsConfigInfoMap().put("sms.smscn.uri", getSmsSmscnUri().trim());
        MgcUtil.getSmsConfigInfoMap().put("sms.smscn.ac", getSmsSmscnAc().trim());
        MgcUtil.getSmsConfigInfoMap().put("sms.smscn.uid", getSmsSmscnUid().trim());
        MgcUtil.getSmsConfigInfoMap().put("sms.smscn.pwd", getSmsSmscnPwd().trim());

        for (String str : getSmsSendGateOrder().trim().split(";")) {
            MgcUtil.getSmsSendGateList().add(str);
        }
        
    }
    
}

package cn.com.citycloud.live.mgc.sms.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sms") 
public class SmsEntity {
    
    @Id
    private String id;
    
    private String phone;
    
    private String content;
    
    private int code;       //模板编码
    
    private int status;     //0：待发送（默认），1：发送成功，2：发送失败

    private int sendTimes;  //0：（默认）

    private String sendGate;
    
    private Date createTime;
    
    private Date updateTime;
    
    private String remark;

    public SmsEntity() {
        
    }
    
    public SmsEntity(String phone, String content, int code) {
        super();
        this.phone = phone;
        this.content = content;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSendTimes() {
        return sendTimes;
    }

    public void setSendTimes(int sendTimes) {
        this.sendTimes = sendTimes;
    }

    public String getSendGate() {
        return sendGate;
    }

    public void setSendGate(String sendGate) {
        this.sendGate = sendGate;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


}

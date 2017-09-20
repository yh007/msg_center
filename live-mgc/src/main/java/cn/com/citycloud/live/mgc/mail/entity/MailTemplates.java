package cn.com.citycloud.live.mgc.mail.entity;

import java.io.Serializable;

import cn.com.citycloud.frame.mybatisplus.annotations.IdType;
import cn.com.citycloud.frame.mybatisplus.annotations.TableField;
import cn.com.citycloud.frame.mybatisplus.annotations.TableId;
import cn.com.citycloud.frame.mybatisplus.annotations.TableName;

/**
 *
 * 
 *
 */
@TableName(value = "mail_templates")
public class MailTemplates implements Serializable {

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;

	/**  */
	@TableId(type = IdType.AUTO)
	private String code;

	/**  */
	private String template;

	/**  */
	private String subject;

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTemplate() {
		return this.template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

}

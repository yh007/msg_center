package cn.com.citycloud.live.mgc.mail.entity;

import java.io.Serializable;
import java.util.Date;

import cn.com.citycloud.frame.mybatisplus.annotations.IdType;
import cn.com.citycloud.frame.mybatisplus.annotations.TableField;
import cn.com.citycloud.frame.mybatisplus.annotations.TableId;
import cn.com.citycloud.frame.mybatisplus.annotations.TableName;

/**
 *
 * 
 *
 */
@TableName(value = "mail_tasks")
public class MailTasks implements Serializable {

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;

	/**  */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**  */
	private String template;

	/**  */
	@TableField(value = "subject_resolver")
	private String subjectResolver;

	/**  */
	private String to;

	/**  */
	@TableField(value = "template_parameters")
	private String templateParameters;

	/**  */
	@TableField(value = "send_time")
	private Date sendTime;

	/**  */
	@TableField(value = "send_counts")
	private Integer sendCounts;

	/**  */
	@TableField(value = "error_message")
	private String errorMessage;

	/**  */
	@TableField(value = "last_time")
	private Date lastTime;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTemplate() {
		return this.template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getSubjectResolver() {
		return this.subjectResolver;
	}

	public void setSubjectResolver(String subjectResolver) {
		this.subjectResolver = subjectResolver;
	}

	public String getTo() {
		return this.to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getTemplateParameters() {
		return this.templateParameters;
	}

	public void setTemplateParameters(String templateParameters) {
		this.templateParameters = templateParameters;
	}

	public Date getSendTime() {
		return this.sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public Integer getSendCounts() {
		return this.sendCounts;
	}

	public void setSendCounts(Integer sendCounts) {
		this.sendCounts = sendCounts;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Date getLastTime() {
		return this.lastTime;
	}

	public void setLastTime(Date lastTime) {
		this.lastTime = lastTime;
	}

}

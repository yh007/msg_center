package cn.com.citycloud.live.mgc.utils;

import cn.com.citycloud.live.mgc.mail.template.MailTemplateKey;

/**
 * 邮件模板类型
 * 
 * @created 2015年8月19日
 * @author  huanglj
 */
public enum MailTemplateType implements MailTemplateKey {

	registerCheck("注册验证"),retrivePassword("找回密码");

	private final String subject;

	private MailTemplateType(String subject) {
		this.subject = subject;
	}

	public String subject() {
		return subject;
	}

	@Override
	public String key() {
		return name();
	}

}

package cn.com.citycloud.live.mgc.mail.resolver;

import java.util.Map;

import cn.com.citycloud.live.mgc.mail.entity.MailTemplates;
import cn.com.citycloud.live.mgc.mail.enums.SubjectResolverStrategy;

public abstract interface MailSubjectResolver {
    public static final String TemplateParamKey_MailSubject = "MailSubject";

    public abstract String resolveSubject(MailTemplates paramMailTemplate, Map<String, Object> paramMap);

    public abstract SubjectResolverStrategy getSubjectResolverStrategy();
}

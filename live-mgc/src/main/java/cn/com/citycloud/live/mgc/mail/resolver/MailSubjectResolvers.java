package cn.com.citycloud.live.mgc.mail.resolver;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

import cn.com.citycloud.live.mgc.mail.entity.MailTemplates;
import cn.com.citycloud.live.mgc.mail.enums.SubjectResolverStrategy;

public class MailSubjectResolvers {
    private static Map<SubjectResolverStrategy, MailSubjectResolver> subjectResolvers = new HashMap<SubjectResolverStrategy, MailSubjectResolver>();

    static {
        subjectResolvers.put(SubjectResolverStrategy.Default, new DefaultMailSubjectResolver(
                SubjectResolverStrategy.Default));
        subjectResolvers.put(SubjectResolverStrategy.Override, new OverrideMailSubjectResolver(
                SubjectResolverStrategy.Override));
        subjectResolvers.put(SubjectResolverStrategy.Append, new AppendMailSubjectResolver(
                SubjectResolverStrategy.Append));
    }

    public static MailSubjectResolver use(SubjectResolverStrategy strategy) {
        return (MailSubjectResolver) subjectResolvers.get(strategy);
    }

    public static MailSubjectResolver useDefault() {
        return (MailSubjectResolver) subjectResolvers.get(SubjectResolverStrategy.Default);
    }

    private static String getSubjectFromTemplateParams(Map<String, Object> templateParams) {
        Object subjectObj = templateParams.get("MailSubject");
        String subject = null;
        if (subjectObj != null) {
            String temp = subjectObj.toString();
            if (StringUtils.isNotBlank(temp))
                subject = temp;
        }
        return subject;
    }

    private static abstract class AbstractMailSubjectResolver implements MailSubjectResolver {
        protected SubjectResolverStrategy strategy;

        public SubjectResolverStrategy getSubjectResolverStrategy() {
            return this.strategy;
        }
    }

    private static class AppendMailSubjectResolver extends MailSubjectResolvers.AbstractMailSubjectResolver {
        private static final String Delimiters = "-";

        private AppendMailSubjectResolver(SubjectResolverStrategy strategy) {
            super();
            this.strategy = strategy;
        }

        public String resolveSubject(MailTemplates template, Map<String, Object> templateParams) {
            String subject = template.getSubject();
            String temp = MailSubjectResolvers.getSubjectFromTemplateParams(templateParams);
            if (StringUtils.isNotBlank(temp)) {
                subject = subject + Delimiters + temp;
            }
            return subject;
        }
    }

    private static class DefaultMailSubjectResolver extends MailSubjectResolvers.AbstractMailSubjectResolver {
        private DefaultMailSubjectResolver(SubjectResolverStrategy strategy) {
            super();
            this.strategy = strategy;
        }

        public String resolveSubject(MailTemplates template, Map<String, Object> templateParams) {
            return template.getSubject();
        }
    }

    private static class OverrideMailSubjectResolver extends MailSubjectResolvers.AbstractMailSubjectResolver {
        private OverrideMailSubjectResolver(SubjectResolverStrategy strategy) {
            super();
            this.strategy = strategy;
        }

        public String resolveSubject(MailTemplates template, Map<String, Object> templateParams) {
            String subject = template.getSubject();
            String temp = MailSubjectResolvers.getSubjectFromTemplateParams(templateParams);
            if (StringUtils.isNotBlank(temp))
                subject = temp;
            return subject;
        }
    }
}

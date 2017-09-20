package cn.com.citycloud.live.mgc.mail.template;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.CollectionUtils;

import cn.com.citycloud.live.mgc.mail.dao.MailTemplatesDao;
import cn.com.citycloud.live.mgc.mail.entity.MailTemplates;
import cn.com.citycloud.live.mgc.mail.enums.SubjectResolverStrategy;
import cn.com.citycloud.live.mgc.mail.resolver.MailSubjectResolvers;

public class MailTemplateService {

    @javax.annotation.Resource
    private MailTemplatesDao mailTemplatesImplDao;
    private Map<String, MailTemplates> mailTemplates;
    private VelocityEngine engine;
    private Resource engineConfig;
    private String templateFilePath;

    public void init() {
        initMailTemplates();

        initEngine();
    }

    private void initMailTemplates() {
        this.mailTemplates = new HashMap<String, MailTemplates>();
        List<MailTemplates> listMailTemplate = this.mailTemplatesImplDao.selectAll();
        if (!CollectionUtils.isEmpty(listMailTemplate))
            for (MailTemplates mailTemplate : listMailTemplate) {
                this.mailTemplates.put(mailTemplate.getCode(), mailTemplate);
            }

    }

    private void initEngine() {
        if (this.engineConfig != null) {
            try {
                Properties properties = new Properties();
                properties.load(this.engineConfig.getInputStream());
                this.engine.init(properties);
            } catch (IOException e) {
                throw new RuntimeException("初始化引擎失败,详细信息：" + e.getMessage());
            }
        } else
            this.engine.init();
    }

    private String generateMailText(MailTemplates mailTemplate, Map<String, Object> templateParameters) {
        if ((mailTemplate == null) || (StringUtils.isBlank(mailTemplate.getTemplate())))
            throw new RuntimeException("未指定生成的邮件模板！");
        StringWriter stringWriter = new StringWriter();
        VelocityContext context = new VelocityContext();

        fillTemplateParameter(context, templateParameters);
        try {
            this.engine.mergeTemplate(this.templateFilePath + "/" + StringUtils.trim(mailTemplate.getTemplate()),
                    "UTF-8", context, stringWriter);
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException("没有找到" + mailTemplate.getTemplate() + "邮件模板文件！");
        } catch (ParseErrorException e) {
            throw new RuntimeException("解析" + mailTemplate.getTemplate() + "邮件模板文件出错！");
        } catch (Exception e) {
            throw new RuntimeException("生成邮件模板" + mailTemplate.getTemplate() + "正文失败,详细信息：" + e.getMessage());
        }
        return stringWriter.toString();
    }

    private void fillTemplateParameter(VelocityContext context, Map<String, Object> templateParameters) {
        if (!CollectionUtils.isEmpty(templateParameters)) {
            Set<String> keys = templateParameters.keySet();
            for (String key : keys)
                context.put(key, templateParameters.get(key));
        }
    }

    public VelocityEngine getEngine() {
        return this.engine;
    }

    public void setEngine(VelocityEngine engine) {
        this.engine = engine;
    }

    public Resource getEngineConfig() {
        return this.engineConfig;
    }

    public void setEngineConfig(Resource engineConfig) {
        this.engineConfig = engineConfig;
    }

    public String getTemplateFilePath() {
        return this.templateFilePath;
    }

    public void setTemplateFilePath(String templateFilePath) {
        this.templateFilePath = templateFilePath;
    }

    public MailTemplatesDao getMailTemplatesImplDao() {
        return mailTemplatesImplDao;
    }

    public void setMailTemplatesImplDao(MailTemplatesDao mailTemplatesImplDao) {
        this.mailTemplatesImplDao = mailTemplatesImplDao;
    }

    public Map<String, MailTemplates> getMailTemplates() {
        return this.mailTemplates;
    }

    public void setMailTemplates(Map<String, MailTemplates> mailTemplates) {
        this.mailTemplates = mailTemplates;
    }

    public static class MimeMessageBuilder {
        private MailTemplateService mailTemplateService;
        private JavaMailSender mailSender;
        private List<String> mailTos;
        private String mailFrom;
        private MailTemplateKey mailTemplateKey;
        private Map<String, Object> templateParameters;
        private SubjectResolverStrategy subjectResolverStrategy;
        private boolean sendedBySingle;

        public MimeMessageBuilder(MailTemplateService mailTemplateService) {
            this.mailTemplateService = mailTemplateService;
        }

        public MimeMessageBuilder javaMailSender(JavaMailSender mailSender) {
            this.mailSender = mailSender;
            return this;
        }

        public MimeMessageBuilder mailTos(List<String> mailTos) {
            this.mailTos = mailTos;
            return this;
        }

        public MimeMessageBuilder mailFrom(String mailFrom) {
            this.mailFrom = mailFrom;
            return this;
        }

        public MimeMessageBuilder mailTemplateKey(MailTemplateKey mailTemplateKey) {
            this.mailTemplateKey = mailTemplateKey;
            return this;
        }

        public MimeMessageBuilder templateParameters(Map<String, Object> templateParameters) {
            this.templateParameters = templateParameters;
            return this;
        }

        public MimeMessageBuilder subjectResolverStrategy(SubjectResolverStrategy subjectResolverStrategy) {
            this.subjectResolverStrategy = subjectResolverStrategy;
            return this;
        }

        public MimeMessageBuilder sendedBySingle(boolean sendedBySingle) {
            this.sendedBySingle = sendedBySingle;
            return this;
        }

        public List<MimeMessage> build() {
            List<MimeMessage> mailMessages = new ArrayList<MimeMessage>();
            try {
                if (this.sendedBySingle) {
                    for (String mailTo : this.mailTos) {
                        MimeMessageHelper messageHelper = createMimeMessageNoMailTo();

                        messageHelper.addTo(mailTo);
                        mailMessages.add(messageHelper.getMimeMessage());
                    }
                } else {
                    MimeMessageHelper messageHelper = createMimeMessageNoMailTo();

                    for (String mailTo : this.mailTos) {
                        messageHelper.addTo(mailTo);
                    }
                    mailMessages.add(messageHelper.getMimeMessage());
                }
            } catch (Exception e) {
                throw new RuntimeException("创建邮件消息失败。", e);
            }
            return mailMessages;
        }

        private MimeMessageHelper createMimeMessageNoMailTo() throws MessagingException {
            MimeMessage mailMessage = this.mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true, "UTF-8");

            messageHelper.setFrom(this.mailFrom);
            MailTemplates mailTemplate = (MailTemplates) this.mailTemplateService.getMailTemplates().get(
                    this.mailTemplateKey.key());
            messageHelper.setSubject(getSubject(mailTemplate, this.templateParameters));
            String mailText = this.mailTemplateService.generateMailText(mailTemplate, this.templateParameters);
            messageHelper.setText(mailText, true);
            return messageHelper;
        }

        private String getSubject(MailTemplates mailTemplate, Map<String, Object> templateParameters) {
            if (this.subjectResolverStrategy == null) {
                return MailSubjectResolvers.useDefault().resolveSubject(mailTemplate, templateParameters);
            }

            return MailSubjectResolvers.use(this.subjectResolverStrategy).resolveSubject(mailTemplate,
                    templateParameters);
        }
    }
}

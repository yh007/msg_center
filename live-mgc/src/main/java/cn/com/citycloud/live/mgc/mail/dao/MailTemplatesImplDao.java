package cn.com.citycloud.live.mgc.mail.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import cn.com.citycloud.live.mgc.mail.entity.MailTemplates;
import cn.com.citycloud.live.mgc.utils.MailTemplateType;

@Repository
public class MailTemplatesImplDao implements MailTemplatesDao {

    private static final String mailTemplateSuffix = ".vm";

    @Override
    public List<MailTemplates> selectAll() {
        List<MailTemplates> mailTemplates = new ArrayList<>();
        for (MailTemplateType mailTemplateType : MailTemplateType.values()) {
            MailTemplates mailTemplate = new MailTemplates();
            mailTemplate.setCode(mailTemplateType.key());
            mailTemplate.setSubject(mailTemplateType.subject());
            mailTemplate.setTemplate(mailTemplateType.key() + mailTemplateSuffix);
            mailTemplates.add(mailTemplate);
        }
        return mailTemplates;
    }

    
}

package cn.com.citycloud.live.mgc.mail.dao;

import java.util.List;

import cn.com.citycloud.live.mgc.mail.entity.MailTemplates;

/**
 *
 * MailTemplates 表数据库控制层接口
 *
 */
public interface MailTemplatesDao  {

    List<MailTemplates> selectAll();
    
}
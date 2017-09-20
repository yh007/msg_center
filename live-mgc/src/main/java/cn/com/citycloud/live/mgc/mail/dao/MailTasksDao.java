package cn.com.citycloud.live.mgc.mail.dao;

import java.util.List;

import org.apache.ibatis.session.RowBounds;

import cn.com.citycloud.live.mgc.mail.entity.MailTasks;
import cn.com.citycloud.frame.mybatisplus.mapper.AutoMapper;

/**
 *
 * MailTasks 表数据库控制层接口
 *
 */
public interface MailTasksDao extends AutoMapper<MailTasks> {
    
    void saves(List<MailTasks> paramList);

    void delete(Long paramLong);

    List<MailTasks> selectByPaging(RowBounds pagination);

    void saveFailMessage(MailTasks paramMailTask);

}
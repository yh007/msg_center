package cn.com.citycloud.live.mgc.sms.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.StringUtils;

import cn.com.citycloud.live.common.concurrent.ExecutorManager;
import cn.com.citycloud.live.common.zk.ZkListener;
import cn.com.citycloud.live.mgc.sms.entity.SmsEntity;
import cn.com.citycloud.live.mgc.sms.entity.SmsTemplateEntity;
import cn.com.citycloud.live.mgc.utils.MgcUtil;

@Service("smsService")
public class SmsService implements ZkListener {

    private static Logger LOGGER = LoggerFactory.getLogger(SmsService.class);
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired 
    private SmsTemplateService smsTemplateService;
    
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    private Map<Integer, String> caches = null;

    public List<SmsEntity> findList(int skip, int limit) {
        Query query = new Query();
        query.with(new Sort(new Order(Direction.ASC, "_id")));
        query.skip(skip).limit(limit);
        return this.mongoTemplate.find(query, SmsEntity.class);
    }

    // public List<Sms> findListByAge(int age) {
    // Query query = new Query();
    // query.addCriteria(new Criteria("age").is(age));
    // return this.mongoTemplate.find(query, Sms.class);
    // }

    public SmsEntity findOne(String id) {
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(id));
        return this.mongoTemplate.findOne(query, SmsEntity.class);
    }

    public SmsEntity insert(SmsEntity sms) {
        this.mongoTemplate.insert(sms);
        return sms;
    }

    public void update(SmsEntity sms, boolean isUpdateSendTimes) {
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(sms.getId()));
        Update update = new Update();
        update.set("_id", sms.getId());
        update.set("status", sms.getStatus());
        update.set("updateTime", MgcUtil.getMongodbCurDate());
        if (isUpdateSendTimes)
            update.set("sendTimes", sms.getSendTimes());
        if (StringUtils.isNotEmpty(sms.getSendGate()))
            update.set("sendGate", sms.getSendGate());
        this.mongoTemplate.updateFirst(query, update, SmsEntity.class);
    }
    
    /**
     * 根据key取得值
     * 
     * @param key
     * @return
     */
    public String getTemplateContent(Integer key) {
        if (caches == null) {
            return null;
        }
        try {
            lock.readLock().lock();
            return caches.get(key);
        } catch (Exception e) {
            LOGGER.error("", e);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    @PostConstruct
    public void loadDataDict() {

        ExecutorManager.getInstance().execute(new Runnable() {

            @Override
            public void run() {
                List<SmsTemplateEntity> result = null;
                try {
                    lock.writeLock().lock();
                    caches = new HashMap<Integer, String>();
                    result = smsTemplateService.findAllTemplate();
                    MgcUtil.getSmsValidCodeSet().clear();
                    for (SmsTemplateEntity entity : result) {
                        //有效
                        if (entity.getIsValid() == 1){
                            caches.put(entity.getCode(), entity.getContent());
                            MgcUtil.getSmsValidCodeSet().add(entity.getCode());
                        }
                    }

                } finally {
                    lock.writeLock().unlock();
                }

            }
        });

    }
    
    /*
     * (non-Javadoc)
     * @see cn.com.citycloud.live.common.zk.ZkListener#configurationChangedNotice(java.lang.String, java.lang.String)
     */
    @Override
    public void configurationChangedNotice(String keyName, String key) {
        LOGGER.debug("重新加载短信模板......");
        loadDataDict();
    }

}

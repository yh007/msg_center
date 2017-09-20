package cn.com.citycloud.live.mgc.sms.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import cn.com.citycloud.live.mgc.sms.entity.SmsTemplateEntity;

@Repository
public class SmsTemplateService {

        @Autowired
        private MongoTemplate mongoTemplate; 
      
        public SmsTemplateEntity findByCode(int code) {
            Query query = new Query();
            query.addCriteria(new Criteria("code").is(code));
            return this.mongoTemplate.findOne(query, SmsTemplateEntity.class);
        }
        
        public List<SmsTemplateEntity> findAllTemplate() {
            return this.mongoTemplate.findAll(SmsTemplateEntity.class);
        }


}

package cn.com.citycloud.live.mgc.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.internet.MimeMessage;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import cn.com.citycloud.frame.mybatisplus.plugins.pagination.Pagination;
import cn.com.citycloud.live.mgc.mail.async.AsyncMailHandler;
import cn.com.citycloud.live.mgc.mail.async.TaskQueue;
import cn.com.citycloud.live.mgc.mail.dao.MailTasksDao;
import cn.com.citycloud.live.mgc.mail.entity.MailTasks;
import cn.com.citycloud.live.mgc.mail.enums.SubjectResolverStrategy;
import cn.com.citycloud.live.mgc.mail.exception.AsyncMailHandlerException;
import cn.com.citycloud.live.mgc.mail.exception.MailSendFailureException;
import cn.com.citycloud.live.mgc.mail.template.MailTemplateKey;
import cn.com.citycloud.live.mgc.mail.template.MailTemplateService;

import com.alibaba.fastjson.JSON;

@Service("mailSendService")
public class MailSendService implements AsyncMailHandler {
    private static final Logger log = LoggerFactory.getLogger(MailSendService.class);
    private MailTemplateService mailTemplateService;
    private JavaMailSender mailSender;
    private String mailFrom;

    @Autowired
    private MailTasksDao mailTaskDao;

    @Autowired
    private ThreadPoolTaskExecutor asyncTaskExecutor;
    private boolean useAsyncSend = true;
    private boolean useAsyncHandle;
    private AsyncMailHandler proxyAsyncMailHandler = new DefaultAsyncMailHandler();

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void init() {
        this.mailTemplateService.init();
        if (this.useAsyncHandle) {
            startAsyncHandle();
        }
    }

    public void sendMail(MailTemplateKey mailTemplateKey, Map<String, Object> templateParameter, String... emails) {
        if ((emails == null) || (emails.length == 0)) {
            return;
        }
        List mailTos = new ArrayList();
        for (String email : emails) {
            if ((email != null) && (email.matches("^[\\w\\-\\.]+@[\\w\\-]+(\\.[\\w\\-]+)+$")))
                mailTos.add(email);
            else {
                log.error("邮箱[" + email + "]格式不正确，拒绝发送邮件！");
            }
        }
        if (mailTos.isEmpty()) {
            return;
        }
        sendMail(mailTos, mailTemplateKey, templateParameter == null ? new HashMap() : templateParameter,
                mailTos.size() != 1);
    }

    public void sendMail(List<String> mailTos, MailTemplateKey mailTemplateKey, Map<String, Object> templateParameter,
            boolean sendedBySingle) throws MailSendFailureException {
        if (this.useAsyncSend)
            doAsyncSendMail(mailTos, mailTemplateKey, templateParameter, sendedBySingle,
                    SubjectResolverStrategy.Default);
        else
            doSendMail(mailTos, mailTemplateKey, templateParameter, sendedBySingle, SubjectResolverStrategy.Default);
    }

    public void sendMail(List<String> mailTos, MailTemplateKey mailTemplateKey, Map<String, Object> templateParameter,
            boolean sendedBySingle, SubjectResolverStrategy subjectResolverStrategy) throws MailSendFailureException {
        if (this.useAsyncSend)
            doAsyncSendMail(mailTos, mailTemplateKey, templateParameter, sendedBySingle, subjectResolverStrategy);
        else
            doSendMail(mailTos, mailTemplateKey, templateParameter, sendedBySingle, subjectResolverStrategy);
    }

    private void doSendMail(List<String> mailTos, MailTemplateKey mailTemplateKey,
            Map<String, Object> templateParameters, boolean sendedBySingle,
            SubjectResolverStrategy subjectResolverStrategy) throws MailSendFailureException {
        try {
            validateMailTos(mailTos);

            validateMailTemplateKey(mailTemplateKey);

            validateSubjectResolverStrategy(subjectResolverStrategy);

            List<MimeMessage> mimeMessages = new MailTemplateService.MimeMessageBuilder(this.mailTemplateService)
                    .javaMailSender(this.mailSender).mailFrom(this.mailFrom).mailTos(mailTos)
                    .subjectResolverStrategy(subjectResolverStrategy).templateParameters(templateParameters)
                    .mailTemplateKey(mailTemplateKey).sendedBySingle(sendedBySingle).build();

            for (MimeMessage mimeMessage : mimeMessages)
                this.mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new MailSendFailureException("邮件发送失败。", e);
        }
    }

    private void doAsyncSendMail(List<String> mailTos, MailTemplateKey mailTemplateKey,
            Map<String, Object> templateParameter, boolean sendedBySingle,
            SubjectResolverStrategy subjectResolverStrategy) throws MailSendFailureException {
        if (!this.useAsyncSend) {
            throw new MailSendFailureException("当前邮件发送服务未启用异步邮件处理方式！");
        }
        try {
            validateMailTos(mailTos);

            validateMailTemplateKey(mailTemplateKey);

            validateSubjectResolverStrategy(subjectResolverStrategy);
            MailTasks mailTask = null;
            List mailTasks = new ArrayList();

            if (sendedBySingle) {
                for (String mailTo : mailTos) {
                    mailTask = createMailTaskNoMailto(mailTemplateKey, templateParameter, subjectResolverStrategy);
                    mailTask.setTo(JSON.toJSONString(Collections.singletonList(mailTo)));
                    mailTasks.add(mailTask);
                }
            } else {
                mailTask = createMailTaskNoMailto(mailTemplateKey, templateParameter, subjectResolverStrategy);
                mailTask.setTo(JSON.toJSONString(mailTos));
                mailTasks.add(mailTask);
            }
            this.mailTaskDao.saves(mailTasks);
        } catch (Exception e) {
            throw new MailSendFailureException("邮件发送失败。", e);
        }
    }

    private MailTasks createMailTaskNoMailto(MailTemplateKey mailTemplateKey, Map<String, Object> templateParameters,
            SubjectResolverStrategy subjectResolverStrategy) {
        MailTasks mailTask = new MailTasks();
        mailTask.setTemplate(mailTemplateKey.key());
        mailTask.setSendTime(new Date());
        mailTask.setTemplateParameters(JSON.toJSONString(templateParameters));
        mailTask.setSubjectResolver(subjectResolverStrategy.name());
        return mailTask;
    }

    private void validateMailTos(List<String> mailTos) throws IllegalArgumentException {
        if (CollectionUtils.isEmpty(mailTos))
            throw new IllegalArgumentException("收件人地址不能为空！");
    }

    private void validateMailTemplateKey(MailTemplateKey mailTemplateKey) throws IllegalArgumentException {
        if (mailTemplateKey == null)
            throw new IllegalArgumentException("未指定邮件的模板！");
    }

    private void validateSubjectResolverStrategy(SubjectResolverStrategy subjectResolverStrategy)
            throws IllegalArgumentException {
        if (subjectResolverStrategy == null)
            throw new IllegalArgumentException("邮件主题处理方式不能为空");
    }

    public void startAsyncHandle() throws AsyncMailHandlerException {
        if (this.useAsyncSend)
            this.proxyAsyncMailHandler.startAsyncHandle();
    }

    public void stopAsyncHandle() throws AsyncMailHandlerException {
        if (this.useAsyncSend)
            this.proxyAsyncMailHandler.stopAsyncHandle();
    }

    public MailTemplateService getMailTemplateService() {
        return this.mailTemplateService;
    }

    public void setMailTemplateService(MailTemplateService mailTemplateService) {
        this.mailTemplateService = mailTemplateService;
    }

    public JavaMailSender getMailSender() {
        return this.mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String getMailFrom() {
        return this.mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public MailTasksDao getMailTaskDao() {
        return this.mailTaskDao;
    }

    public void setMailTaskDao(MailTasksDao mailTaskDao) {
        this.mailTaskDao = mailTaskDao;
    }

    public boolean isUseAsyncSend() {
        return this.useAsyncSend;
    }

    public void setUseAsyncSend(boolean useAsyncSend) {
        this.useAsyncSend = useAsyncSend;
    }

    public boolean isUseAsyncHandle() {
        return this.useAsyncHandle;
    }

    public void setUseAsyncHandle(boolean useAsyncHandle) {
        this.useAsyncHandle = useAsyncHandle;
    }

    private class DefaultAsyncMailHandler implements AsyncMailHandler, TaskQueue<MailSendService.WrapedMailTask> {
        private ProduceTaskThread produceTaskThread;
        private DispatchTaskThread dispatchTaskThread;
        private LinkedList<MailSendService.WrapedMailTask> mailTasks = new LinkedList();

        private ReentrantLock lock = new ReentrantLock(true);

        private Condition notEmpty = this.lock.newCondition();

        private Condition allTasksDone = this.lock.newCondition();

        private DefaultAsyncMailHandler() {
        }

        public MailSendService.WrapedMailTask take() {
            MailSendService.WrapedMailTask mailTask = null;
            try {
                this.lock.lock();
                while (this.mailTasks.size() <= 0) {
                    this.notEmpty.await();
                }
                mailTask = (MailSendService.WrapedMailTask) this.mailTasks.removeFirst();
            } catch (Exception e) {
                MailSendService.this.logger.error("", e);
            } finally {
                this.lock.unlock();
            }
            return mailTask;
        }

        public void put(MailSendService.WrapedMailTask mailTask) {
            try {
                this.lock.lock();
                this.mailTasks.addLast(mailTask);
                this.notEmpty.signalAll();
            } finally {
                this.lock.unlock();
            }
        }

        public void startAsyncHandle() throws AsyncMailHandlerException {
            try {
                this.produceTaskThread = new ProduceTaskThread();
                this.dispatchTaskThread = new DispatchTaskThread();
                this.produceTaskThread.start();
                MailSendService.this.logger.info("异步邮件ProduceTaskThread成功启动！");
                this.dispatchTaskThread.start();
                MailSendService.this.logger.info("异步邮件DispatchTaskThread成功启动！");
            } catch (Exception e) {
                throw new AsyncMailHandlerException("启动异步处理任务失败", e);
            }
        }

        public void stopAsyncHandle() throws AsyncMailHandlerException {
            try {
                if (this.produceTaskThread != null) {
                    this.produceTaskThread.shutDown();
                    this.produceTaskThread = null;
                }
                if (this.dispatchTaskThread != null) {
                    this.dispatchTaskThread.shutDown();
                    this.dispatchTaskThread = null;
                }
            } catch (Exception e) {
                throw new AsyncMailHandlerException("终止异步处理任务失败", e);
            }
        }

        private class DispatchTaskThread extends Thread {
            private TaskQueue<MailSendService.WrapedMailTask> taskQueue;
            private volatile boolean isShutDown = false;

            public DispatchTaskThread() {
                this.taskQueue = taskQueue;
            }

            public void shutDown() {
                this.isShutDown = true;
            }

            public void run() {
                MailSendService.WrapedMailTask mailTask = null;

                while (!this.isShutDown) {
                    try {
                        mailTask = (MailSendService.WrapedMailTask) this.taskQueue.take();
                        Future future = MailSendService.this.asyncTaskExecutor.submit(mailTask);
                        mailTask.setFuture(future);
                    } catch (Exception e) {
                        MailSendService.this.logger.error("异步邮件任务分发出现异常，暂时不可用！", e);
                    }
                }
                MailSendService.this.logger.info("DispatchTaskThread正常关闭");
            }
        }

        private class ProduceTaskThread extends Thread {
            private TaskQueue<MailSendService.WrapedMailTask> taskQueue;
            private volatile boolean isShutDown = false;

            public ProduceTaskThread() {
                this.taskQueue = taskQueue;
            }

            public void shutDown() {
                this.isShutDown = true;
            }

            public void run() {
                while (!(this.isShutDown)) {
                    try {
                        
                        List<MailTasks> mailTasks = MailSendService.this.mailTaskDao.selectByPaging(new Pagination(0, 100, false));
                        if (CollectionUtils.isNotEmpty(mailTasks)) {
                            List submitedMailTasks = new ArrayList();

                            for (MailTasks mailTask : mailTasks) {
                                MailSendService.WrapedMailTask wrapedMailTask = new MailSendService.WrapedMailTask(mailTask);
                                this.taskQueue.put(wrapedMailTask);
                                submitedMailTasks.add(wrapedMailTask);
                            }

                            while (submitedMailTasks.size() > 0) {
                                Thread.sleep(100L);
                                for (int i = submitedMailTasks.size() - 1; i >= 0; --i) {
                                    MailSendService.WrapedMailTask wrapedMailTask = (MailSendService.WrapedMailTask) submitedMailTasks
                                            .get(i);
                                    if (wrapedMailTask.isDone()) {
                                        submitedMailTasks.remove(i);
                                    }
                                }
                            }
                        }
                        Thread.sleep(100L);
                    } catch (Exception e) {
                        MailSendService.this.logger.error("异步邮件任务查询出现异常，暂时不可用！", e);
                    }
                }
                MailSendService.this.logger.info("ProduceTaskThread正常关闭");
            }
        }
    }

    private class WrapedMailTask implements Runnable {
        private MailTasks mailTask;
        private Future<?> future;

        public WrapedMailTask(MailTasks mailTask) {
            this.mailTask = mailTask;
        }

        public boolean isDone() {
            return this.future != null ? this.future.isDone() : false;
        }

        public void setFuture(Future<?> future) {
            this.future = future;
        }

        public void run() {
            try {
                List<String> mailTos = JSON.parseArray(this.mailTask.getTo(), String.class);
                Map templateParameter =  JSON.parseObject(this.mailTask.getTemplateParameters(),Map.class);
                SubjectResolverStrategy subjectResolverStrategy = SubjectResolverStrategy.valueOf(this.mailTask
                        .getSubjectResolver());
                final String key = this.mailTask.getTemplate();
                MailTemplateKey mailTemplateKey = new MailTemplateKey() {
                    public String key() {
                        return key;
                    }
                };
                MailSendService.this.doSendMail(mailTos, mailTemplateKey, templateParameter, false,
                        subjectResolverStrategy);

                MailSendService.this.mailTaskDao.delete(this.mailTask.getId());
            } catch (Exception e) {
                this.mailTask.setErrorMessage(e.getMessage());
                MailSendService.this.mailTaskDao.saveFailMessage(this.mailTask);
                MailSendService.this.logger.warn("当前异步邮件任务发送失败，稍后将尝试继续发送:" + this.mailTask, e);
            }
        }
    }
}

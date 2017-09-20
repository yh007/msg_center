/*    */ package cn.com.citycloud.live.mgc.mail.spi;
/*    */ 
/*    */ import org.springframework.mail.javamail.JavaMailSenderImpl;
/*    */ 
/*    */ public class JavaMailSenderImplDeleteSpace extends JavaMailSenderImpl
/*    */ {
/*    */   public void setHost(String host)
/*    */   {
/*  9 */     super.setHost(host == null ? null : host.trim());
/*    */   }
/*    */ 
/*    */   public void setUsername(String username)
/*    */   {
/* 14 */     super.setUsername(username == null ? null : username.trim());
/*    */   }
/*    */ 
/*    */   public void setPassword(String password)
/*    */   {
/* 19 */     super.setPassword(password == null ? null : password.trim());
/*    */   }
/*    */ }


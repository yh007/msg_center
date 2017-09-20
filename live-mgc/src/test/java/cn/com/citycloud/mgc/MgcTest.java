/*
 * Copyright (C), 2015-2016, 城云科技
 * FileName: MgcTest.java
 * Author:   zhaoyi
 * Date:     2016-12-28 上午11:14:23
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package cn.com.citycloud.mgc;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import cn.com.citycloud.live.mgc.dto.SmsDto;
import cn.com.citycloud.live.mgc.utils.MgcUtil;

import com.alibaba.fastjson.JSON;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author zhaoyi
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class MgcTest {

    public static void main(String[] args) throws HttpException, IOException {
//        RestTemplate restTemplate = new RestTemplate();
//        StringBuffer url = new StringBuffer();
//        url.append("http://localhost:8081/live-mgc/sms/send");
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
////      headers.setAccept(Arrays.asList(new MediaType[] { new MediaType("application", "json", Charset.forName("UTF-8")) }));
//        
//        SmsDto smsDto=new SmsDto();
//        smsDto.setCode(1006);
//        smsDto.setPhone("15851810245");
//        
//        HttpEntity<String> requestEntity = new HttpEntity<String>(JSON.toJSONString(smsDto),headers);
//        ResponseEntity<String> exchange = restTemplate.exchange(url.toString(), HttpMethod.POST, requestEntity, String.class);
//        System.out.println(exchange);
        
        NameValuePair[] data = {
                    new NameValuePair("ac", "send"),
                    new NameValuePair("uid", "qianlimu"),
                    new NameValuePair("pwd", "d7b9f54b21594b15d598642b117c8627"),
                    new NameValuePair("mobile","13777559149"),
                    new NameValuePair("content", "【千里目】您正在找回密码，验证码是12345")};
//                    new NameValuePair("template","399993"),
//                    new NameValuePair("content", java.net.URLEncoder.encode("{\"code\":\"测试一下4\"}","utf-8"))};
        //例: {"stat":"100","message":"发送成功"},result: {"stat":"101","message":"发送失败"}
        String result = null;
        PostMethod post = new PostMethod("http://api.sms.cn/sms/");;
            HttpClient client = new HttpClient();
            post.addRequestHeader("Content-Type","application/x-www-form-urlencoded;charset=utf-8");//在头文件中设置转码,注：或utf8
            post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8");  //smscn一定用
            post.setRequestBody(data);
            client.executeMethod(post);
            result = new String(post.getResponseBodyAsString().getBytes("utf-8"), "utf-8");
            System.out.println(result);
            
    }
}

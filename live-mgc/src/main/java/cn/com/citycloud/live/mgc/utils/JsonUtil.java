package cn.com.citycloud.live.mgc.utils;

import com.alibaba.fastjson.JSONObject;

public class JsonUtil {
    
    /**
     * 生成json串
     * @param code  返回状态码 0:失败，1:成功
     * @param msg
     */
    public static String convertJsonString(int code, String msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("msg", msg == null ? "" : msg);
        return jsonObject.toJSONString();
    }
    

}

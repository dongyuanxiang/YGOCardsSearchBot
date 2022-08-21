package com.bysz;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainFunction extends SimpleListenerHost {

    @EventHandler
    private ListeningStatus Group(GroupMessageEvent event) throws IOException, InterruptedException {
        String temp = event.getMessage().contentToString();
        if (temp.equals("test")) {
            event.getSubject().sendMessage("successfully");
        }
        if (temp.equals("测试")) {
            event.getSubject().sendMessage("成功");
        }
        if(temp.length() > 2){
            if(temp.substring(0,2).equals("ck") | temp.substring(0,2).equals("CK")){
                String query = temp.substring(2);
                if(query.substring(0,1).equals(" ")){
                    query = REGEX("^ *",query);
                }
                if(query.equals("作者")){
                    event.getSubject().sendMessage("作者QQ：3113481505\n游戏王QQ群：897732813\nB站个人空间：https://space.bilibili.com/37681307\n感谢您的支持");
                    return ListeningStatus.LISTENING;
                }
                if(query.equals("帮助")){
                    event.getSubject().sendMessage("绰号收集功能源于此表\n【腾讯文档】卡片绰号收集表\nhttps://docs.qq.com/sheet/DQXpiVFJDZm5JWGh5\n提交的绰号在机器人收录后即可使用ck绰号查询\n感谢您的支持");
                    return ListeningStatus.LISTENING;
                }
                System.out.println("正在查询：" + query);
                AlisaApiQuery(query,event);
                return ListeningStatus.LISTENING;
            }
        }
        return ListeningStatus.LISTENING;
    }
    public String REGEX(String ex,String temp){
        Pattern pattern = Pattern.compile(ex);
        Matcher matcher = pattern.matcher(temp);
        return matcher.replaceAll("").trim();
    }
    public void AlisaApiQuery(String query,GroupMessageEvent event) throws IOException, InterruptedException {
        final String AlisaApi = "http://bysz.link/YGOAlisa/";
        System.out.println("正在调用Api进行绰号查询");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(AlisaApi + query).build();
        Response response = client.newCall(request).execute();
        if(response.code() == 200){
            String result = response.body().string();
            if(result.equals("")){
                ApiQuery(query,event);
            }else{
                ApiQuery(result,event);
            }
        }else{
            System.out.println("机器人绰号查询模块出现错误\n错误代码：" + response.code() + "\n请及时联系作者");
        }
    }
    public void ApiQuery(String query,GroupMessageEvent event) throws IOException {
        final String API = "https://ygocdb.com/api/v0/?search=";
        System.out.println("正在调用Api查询");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API + query).build();
        Response response = client.newCall(request).execute();
        if(response.code() == 200){
            String result = response.body().string();
            if(result.length() > 50){
                JSONObject jsonObject1 = JSONObject.parseObject(result);
                JSONArray jsonArray1 = jsonObject1.getJSONArray("result");
                JSONObject jsonObject2 = jsonArray1.getJSONObject(0);
                String id = jsonObject2.getString("id");
                String NWBBSname = jsonObject2.getString("cn_name");
                String CNOCGname = jsonObject2.getString("cnocg_n");
                String text = jsonObject2.getString("text");
                JSONObject jsonObject3 = JSONObject.parseObject(text);
                String types = jsonObject3.getString("types");
                String pdesc = jsonObject3.getString("pdesc");
                String desc = jsonObject3.getString("desc");
                if(pdesc.equals("")){
                    Image image = Contact.uploadImage(event.getSender(),new URL("https://cdn.233.momobako.com/ygopro/pics/" + id + ".jpg").openConnection().getInputStream());
                    event.getSubject().sendMessage(image.plus("NWBBS: " + NWBBSname + "\n" + "CNOCG: " + CNOCGname + "\n" + types + "\n\n" + desc));
                }else{
                    Image image = Contact.uploadImage(event.getSender(),new URL("https://cdn.233.momobako.com/ygopro/pics/" + id + ".jpg").openConnection().getInputStream());
                    event.getSubject().sendMessage(image.plus("NWBBS: " + NWBBSname + "\n" + "CNOCG: " + CNOCGname + "\n" + types + "\n\n"  + pdesc + "\n\n" + desc));
                }
            }else{
                event.getSubject().sendMessage("未查询到任何结果，请尝试更换关键字。\n请勿查询与游戏王无关的事物，一经发现，直接退群\n绰号查询说明及获取更多信息请输入ck帮助");
            }
        }else{
            event.getSubject().sendMessage("机器人内部错误\n" + "错误代码：" + response.code() + "\n请及时联系作者");
        }
    }


}
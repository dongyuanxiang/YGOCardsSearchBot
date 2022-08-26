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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainFunction extends SimpleListenerHost {

    int newVersion = 0;
    int nowVersion = 101;
    int queryCount = 100;
    //监听群聊消息
    @EventHandler
    private ListeningStatus Group(GroupMessageEvent event) throws IOException {
        //获得群聊消息内容
        String temp = event.getMessage().contentToString();
        //用于测试机器人是否成功运行
        if (temp.equals("test")) {
            event.getSubject().sendMessage("successfully");
        }
        if (temp.equals("测试")) {
            event.getSubject().sendMessage("成功");
        }
        //判断群聊内容是否为查卡格式
        if(temp.length() > 2){
            if(temp.substring(0,2).equals("ck") | temp.substring(0,2).equals("CK")){
                //获取想要查的卡名
                String query = temp.substring(2);
                //如果ck后和卡名前有空格，则删除中间的空格
                if(query.substring(0,1).equals(" ")){
                    query = regex("^ *",query);
                }
                //自定义关键词回复
                if(diyReply(query,event)){
                    return ListeningStatus.LISTENING;
                }
                //卡片查询
                System.out.println("正在查询：" + query);
                alisaApiQuery(query,event);
                queryCount ++;
                update(event);
                return ListeningStatus.LISTENING;
            }
        }
        return ListeningStatus.LISTENING;
    }
    /**
     *
     * @param ex 正则表达式
     * @param temp 需要操作的字符串
     * @return 删除命中正则规则的内容
     */
    public String regex(String ex, String temp){
        Pattern pattern = Pattern.compile(ex);
        Matcher matcher = pattern.matcher(temp);
        return matcher.replaceAll("").trim();
    }

    /**
     *
     * @param query ck后的内容
     * @param event 群聊消息事件
     * @return 命中自定义关键词后直接回复，不再进行查询
     */
    public boolean diyReply(String query,GroupMessageEvent event){
        if(query.equals("作者")){
            event.getSubject().sendMessage("作者QQ：3113481505\n游戏王QQ群：897732813\nB站个人空间：https://space.bilibili.com/37681307\n感谢您的支持");
            return true;
        }
        if(query.equals("帮助")){
            event.getSubject().sendMessage("绰号收集功能源于此表\n【腾讯文档】卡片绰号收集表\nhttps://docs.qq.com/sheet/DQXpiVFJDZm5JWGh5\n提交的绰号在机器人收录后即可使用ck绰号查询\n感谢您的支持");
            return true;
        }
        return false;
    }

    /**
     *
     * @param query ck后的内容
     * @param event 群聊消息事件
     * @throws IOException 调用api需要从网络上读取内容，所以可能会有io异常
     */
    public void alisaApiQuery(String query, GroupMessageEvent event) throws IOException {
        //绰号查询api介绍地址 http://www.bysz.link/ygo/search.html
        String alisaApi = "http://www.bysz.link/ygo/search.php?api=true&search=";
        System.out.println("正在调用Api进行绰号查询");
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(alisaApi + query).build();
        Response response = client.newCall(request).execute();
        if(response.code() == 200){
            String result = response.body().string();
            cardApiQuery(result,event);
        }else{
            System.out.println("机器人绰号查询模块出现错误\n错误代码：" + response.code() + "\n请及时联系作者");
            cardApiQuery(query,event);
            event.getSubject().sendMessage("机器人绰号查询模块出现错误\n错误代码：" + response.code() + "\n请及时联系作者");
        }
    }

    /**
     *
     * @param query 卡名
     * @param event 群聊消息事件
     * @throws IOException 调用api需要从网络上读取内容，所以可能会有io异常
     */
    public void cardApiQuery(String query, GroupMessageEvent event) throws IOException {
        String API = "https://ygocdb.com/api/v0/?search=";
        System.out.println("正在调用Api进行卡片查询");
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build();
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
            System.out.println("机器人卡片查询模块出现错误\n" + "错误代码：" + response.code() + "\n请及时联系作者");
            event.getSubject().sendMessage("机器人卡片查询模块出现错误\n" + "错误代码：" + response.code() + "\n请及时联系作者");
        }
    }
    public void update(GroupMessageEvent event) throws IOException {
        if(queryCount > 100){
            System.out.println("正在检查更新");
            String API = "http://www.bysz.link/ygo/botUpdate.html";
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build();
            Request request = new Request.Builder().url(API).build();
            Response response = client.newCall(request).execute();
            if(response.code() == 200){
                String result = response.body().string();
                newVersion = Integer.parseInt(result);
                System.out.println("检查更新成功，最新版本版本号为：" + newVersion);
            }else{
                System.out.println("无法检查更新，请联系作者");
            }
            queryCount = 0;
        }
        if(newVersion > nowVersion){
            System.out.println("检测到有新版本，当前版本随时可能停止服务，请及时更新");
            event.getSubject().sendMessage("检测到有新版本，当前版本随时可能停止服务，请及时更新");
        }
    }

}
package com.bysz;


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
    int nowVersion = 106;
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
            event.getSubject().sendMessage("作者QQ：3113481505\n游戏王QQ群：897732813\nB站个人空间：https://space.bilibili.com/37681307");
            return true;
        }
        if(query.equals("帮助")){
            event.getSubject().sendMessage("输入ck+（卡名/绰号）即可查询卡片信息");
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
            System.out.println("机器人绰号查询功能出现错误\n错误代码：" + response.code() + "\n请及时联系作者");
            cardApiQuery(query,event);
            event.getSubject().sendMessage("机器人绰号查询功能出现错误\n错误代码：" + response.code() + "\n请及时联系作者");
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
            String card = response.body().string();
            if(card.length() > 50){
                String result = card.substring(card.indexOf("\"cid\""),card.indexOf("\"data\"") - 1);

                String id = "0";
                int idIndex = result.indexOf("\"id\"");
                if(idIndex > 0){
                    int idBeginIndex = idIndex + 5;
                    int idEndIndex = result.indexOf(",",idBeginIndex);
                    id = result.substring(idBeginIndex,idEndIndex);
                }

                String cn_name = "暂无";
                int cn_nameIndex = result.indexOf("\"cn_name\"");
                if(cn_nameIndex > 0){
                    int cn_nameBeginIndex = cn_nameIndex + 11;
                    int cn_nameEndIndex = result.indexOf(",",cn_nameBeginIndex) - 1;
                    cn_name = result.substring(cn_nameBeginIndex,cn_nameEndIndex);
                }


                String sc_name = "暂无";
                int sc_nameIndex = result.indexOf("\"sc_name\"");
                if(sc_nameIndex > 0){
                    int sc_nameBeginIndex = sc_nameIndex + 11;
                    int sc_nameEndIndex = result.indexOf(",",sc_nameBeginIndex) - 1;
                    sc_name = result.substring(sc_nameBeginIndex,sc_nameEndIndex);
                }

                String cnocg_n = "暂无";
                int cnocg_nIndex = result.indexOf("\"cnocg_n\"");
                if(cnocg_nIndex > 0){
                    int cnocg_nBeginIndex = cnocg_nIndex + 11;
                    int cnocg_nEndIndex = result.indexOf(",",cnocg_nBeginIndex) - 1;
                    cnocg_n = result.substring(cnocg_nBeginIndex,cnocg_nEndIndex);
                }
                /*
                String jp_ruby = "暂无";
                int jp_rubyIndex = result.indexOf("\"jp_ruby\"");
                if(jp_rubyIndex > 0){
                    int jp_rubyBeginIndex = jp_rubyIndex + 9;
                    int jp_rubyEndIndex = result.indexOf(",",jp_rubyIndex) - 1;
                    jp_ruby = result.substring(jp_rubyBeginIndex,jp_rubyEndIndex);
                }

                String jp_name = "暂无";
                int jp_nameIndex = result.indexOf("\"jp_name\"");
                if(jp_nameIndex > 0){
                    int jp_nameBeginIndex = jp_nameIndex + 9;
                    int jp_nameEndIndex = result.indexOf(",",jp_nameIndex) - 1;
                    jp_name = result.substring(jp_nameBeginIndex,jp_nameEndIndex);
                }

                String en_name = "暂无";
                int en_nameIndex = result.indexOf("\"en_name\"");
                if(en_nameIndex > 0){
                    int en_nameBeginIndex = en_nameIndex + 9;
                    int en_nameEndIndex = result.indexOf(",",en_nameIndex) - 1;
                    en_name = result.substring(en_nameBeginIndex,en_nameEndIndex);
                }

                String wiki_en = "暂无";
                int wiki_enIndex = result.indexOf("\"wiki_en\"");
                if(wiki_enIndex > 0){
                    int wiki_enBeginIndex = wiki_enIndex + 9;
                    int wiki_enEndIndex = result.indexOf(",",wiki_enIndex) - 1;
                    wiki_en = result.substring(wiki_enBeginIndex,wiki_enEndIndex);
                }
                */
                String text = "暂无";
                int textIndex = result.indexOf("\"text\"");
                if(textIndex > 0){
                    int textBeginIndex = textIndex + 7;
                    int textEndIndex = result.length();
                    text = result.substring(textBeginIndex,textEndIndex);
                }

                String types = "暂无";
                int typesIndex = text.indexOf("\"types\"");
                if(typesIndex > 0){
                    int typesBeginIndex = typesIndex + 9;
                    int typesEndIndex = text.indexOf(",",typesBeginIndex) - 1;
                    types = text.substring(typesBeginIndex,typesEndIndex);
                }

                String pdesc = "暂无";
                int pdescIndex = text.indexOf("\"pdesc\"");
                if(typesIndex > 0){
                    int pdescBeginIndex = pdescIndex + 9;
                    int pdescEndIndex = text.indexOf(",",pdescBeginIndex) - 1;
                    pdesc = text.substring(pdescBeginIndex,pdescEndIndex);
                }

                String desc = "暂无";
                int descIndex = text.indexOf("\"desc\"");
                if(descIndex > 0){
                    int descBeginIndex = descIndex + 8;
                    int descEndIndex = text.length() - 2;
                    desc = text.substring(descBeginIndex,descEndIndex);
                }

                while(types.indexOf("\\r") > 0){
                    String temp1 = types.substring(0,types.indexOf("\\r"));
                    String temp2 = types.substring(types.indexOf("\\r") + 2);
                    types = temp1 + "\r" + temp2;
                }
                while(types.indexOf("\\n") > 0){
                    String temp1 = types.substring(0,types.indexOf("\\n"));
                    String temp2 = types.substring(types.indexOf("\\n") + 2);
                    types = temp1 + "\n" + temp2;
                }
                while(pdesc.indexOf("\\r") > 0){
                    String temp1 = pdesc.substring(0,pdesc.indexOf("\\r"));
                    String temp2 = pdesc.substring(pdesc.indexOf("\\r") + 2);
                    pdesc = temp1 + "\r" + temp2;
                }
                while(pdesc.indexOf("\\n") > 0){
                    String temp1 = pdesc.substring(0,pdesc.indexOf("\\n"));
                    String temp2 = pdesc.substring(pdesc.indexOf("\\n") + 2);
                    pdesc = temp1 + "\n" + temp2;
                }
                while(desc.indexOf("\\r") > 0){
                    String temp1 = desc.substring(0,desc.indexOf("\\r"));
                    String temp2 = desc.substring(desc.indexOf("\\r") + 2);
                    desc = temp1 + "\r" + temp2;
                }
                while(desc.indexOf("\\n") > 0){
                    String temp1 = desc.substring(0,desc.indexOf("\\n"));
                    String temp2 = desc.substring(desc.indexOf("\\n") + 2);
                    desc = temp1 + "\n" + temp2;
                }
                if(pdesc.equals("")){
                    Image image = Contact.uploadImage(event.getSender(),new URL("https://cdn.233.momobako.com/ygopro/pics/" + id + ".jpg").openConnection().getInputStream());
                    event.getSubject().sendMessage(image.plus("NWBBS：" + cn_name + "\n" + "简体中文：" + sc_name + "\n" + "CNOCG：" + cnocg_n + "\n" + types + "\n\n" + desc));
                }else{
                    Image image = Contact.uploadImage(event.getSender(),new URL("https://cdn.233.momobako.com/ygopro/pics/" + id + ".jpg").openConnection().getInputStream());
                    event.getSubject().sendMessage(image.plus("NWBBS：" + cn_name + "\n" + "简体中文：" + sc_name + "\n" + "CNOCG：" + cnocg_n + "\n" + types + "\n\n"  + pdesc + "\n\n" + desc));
                }
            }else{
                event.getSubject().sendMessage("未查询到任何结果，请尝试更换关键字。");
            }
        }else{
            System.out.println("机器人卡片查询功能出现错误\n" + "错误代码：" + response.code() + "\n请及时联系作者");
            event.getSubject().sendMessage("机器人卡片查询功能出现错误\n" + "错误代码：" + response.code() + "\n请及时联系作者");
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
                event.getSubject().sendMessage("无法检查更新，请联系作者");
            }
            queryCount = 0;
        }
        if(newVersion > nowVersion){
            System.out.println("检测到有新版本，当前版本随时可能停止服务，请及时更新");
            event.getSubject().sendMessage("检测到有新版本，当前版本随时可能停止服务，请及时更新");
        }
    }

}
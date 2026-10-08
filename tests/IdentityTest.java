package app.qingledger;
import org.json.*;
public class IdentityTest extends ServerTest {
 static String login(LocalServer server,String id,String name)throws Exception{return call("/api/login",new JSONObject().put("code",server.code).put("name",name).put("userId",id),"").data.getString("token");}
 public static void main(String[] args)throws Exception{Memory db=new Memory();LocalServer server=new LocalServer("test".getBytes(),db,0);server.start();port=server.port();try{
 String a=login(server,"aaaaaaaaaaaaaaaa","同名"),b=login(server,"bbbbbbbbbbbbbbbb","同名");JSONObject r=obj("{id:'a',type:'expense',cents:2860,purpose:'午饭',category:'餐饮',date:'2026-09-28',account:'微信',project:'旅行',note:'',created:1234,photos:[]}");
 check(mutate(r,null,a).status==200,"create A");check(mutate(new JSONObject(r.toString()).put("id","b"),null,b).status==200,"create B");check(!db.get("a").getString("authorId").equals(db.get("b").getString("authorId")),"same name distinct identities");
 String a2=login(server,"aaaaaaaaaaaaaaaa","同名");check(mutate(new JSONObject(r.toString()).put("id","a2"),null,a2).status==200&&db.get("a2").getString("authorId").equals(db.get("a").getString("authorId")),"relogin stable identity");
 check(mutate(new JSONObject(r.toString()).put("note","他人修改").put("authorId","bbbbbbbbbbbbbbbb"),db.get("a").getLong("revision"),b).status==200&&db.get("a").getString("authorId").equals("aaaaaaaaaaaaaaaa"),"editor does not steal attribution");
 JSONObject imported=new JSONObject(r.toString()).put("id","imported").put("author","原作者").put("authorId","cccccccccccccccc");check(call("/api/mutate",new JSONObject().put("ops",new JSONArray().put(new JSONObject().put("action","import").put("record",imported))),b).status==200&&db.get("imported").getString("authorId").equals("cccccccccccccccc")&&db.get("imported").getString("author").equals("原作者"),"backup retains attribution");
 JSONObject payload=new JSONObject().put("app",MergeEngine.APP).put("mergeProtocol",1).put("expectedRevision",db.rev).put("records",new JSONArray().put(new JSONObject(imported.toString()).put("id","merged")));check(call("/api/merge",payload,b).status==200&&db.get("merged").getString("authorId").equals("cccccccccccccccc"),"merge retains attribution");
 System.out.println("PASS: stable identity across login, same-name isolation, original author preserved on edit/import/merge.");
 }finally{server.close();}}
}

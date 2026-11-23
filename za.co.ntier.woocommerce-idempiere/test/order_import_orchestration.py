#!/usr/bin/env python3
"""Run the actual order-sync control flow against synthetic Woo and JDBC/ERP doubles."""
from pathlib import Path
import re
import subprocess
import tempfile

BUNDLE = Path(__file__).resolve().parents[1]
PROCESS = BUNDLE / "src/za/co/ntier/processes"
IMPORTS = """import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.sql.*;
import java.lang.reflect.*;
"""

RUNNER = r'''public class Runner {
	public static void main(String[] args)throws Exception{
		for(int id=1;id<=8;id++) Fixture.woo.put(id,new Fixture.Entry());
		Fixture.woo.get(2).mode="false";
		Fixture.woo.get(4).mode="typed";
		Fixture.woo.get(5).mode="commit";
		Fixture.woo.get(6).loseStatus=true;
		Fixture.woo.get(7).loseMeta=true;
		var p=new WooCommerce();
		p.prepare();
		String first=p.doIt();
		Fixture.check(first.contains("5 committed, 3 not confirmed, 2 awaiting"),first);
		Fixture.check(Fixture.orders.equals(Set.of(1,3,6,7,8)),"good→bad→good; typed and commit failure");
		for(int id:List.of(2,4,5))Fixture.check(Fixture.woo.get(id).status.equals("processing") && Fixture.woo.get(id).statusWrites==0,"no remote writes on ERP failure");
		Fixture.check(Fixture.woo.get(6).status.equals("completed")&&!Fixture.receipts.get(6).ack,"lost status response pending");
		Fixture.check(Fixture.woo.get(7).sync.equals("yes")&&!Fixture.receipts.get(7).ack,"lost metadata response pending");
		String second=p.doIt();
		Fixture.check(second.contains("0 committed, 3 not confirmed, 0 awaiting"),second);
		Fixture.check(Fixture.receipts.get(6).ack&&Fixture.receipts.get(7).ack,"completed orders independently retried");
		for(int id:List.of(1,3,6,7,8))Fixture.check(Fixture.woo.get(id).statusWrites==1&&Fixture.woo.get(id).metaWrites==1,"no duplicate status or meta writes");
		Fixture.check(Fixture.orders.size()==5 && Trx.instances.isEmpty(),"no duplicate ERP or leaking fake transactions");
		System.out.println("Exact importer orchestration passed: good→false→good, typed failure, unconfirmed commit, lost Woo status and metadata responses, idempotent retry.");
	}
}
'''
DOUBLES = r'''class PO { int get_ID(){ return 11; } Object get_Value(String k){ return k.equals("ad_org_id") ? 7 : "https://fixture.invalid"; } }
class MOrder extends PO { static final String Table_Name="C_Order"; MOrder(Properties p,int id,String n){} String getDocStatus(){return "DR";} void setDescription(String s){} void saveEx(){} }
class Query { Query(Properties p,String t,String w,String n){} Query setParameters(Object... a){return this;} PO firstOnly(){return new PO();} boolean match(){return false;} }
class DocAction { static final String STATUS_Drafted="DR"; }
class SvrProcess { Logger log=Logger.getLogger("fixture"); protected String doIt() throws Exception{return "";} protected void prepare(){} Properties getCtx(){return new Properties();} void addLog(String s){System.out.println(s);} ProcessInfoParameter[] getParameter(){return new ProcessInfoParameter[0];} }
class ProcessInfoParameter { String getParameterName(){return "";} Object getParameter(){return null;} int getParameterAsInt(){return 0;} }
class AdempiereUserError extends RuntimeException { AdempiereUserError(String s){super(s);} }
class Env { static int getAD_Client_ID(Properties p){return 1;} }
class MBldMtmInstall { MBldMtmInstall(Properties p,int id,String n){} void setStatus(String s){} void setC_BPartner_ID(int i){} void setC_BPartner_Location_ID(int i){} void setName(String s){} void setdate_promised(Timestamp t){} void setComments(String s){} void setis_checkmeasured(boolean b){} void setcontract_received(boolean b){} void saveEx(){} int get_ID(){return 1;} }
class X_zz_woocommerce { static final String Table_Name="defaults"; }
class OAuthConfig {OAuthConfig(String u,String c,String s){}}
enum ApiVersionType {V3}
enum EndpointBaseType { ORDERS; String getValue(){return "orders";} }
interface Woocommerce { Map<?,?> get(String end,int i); List<?> getAll(String end,Map<String,String> p); Map<?,?> update(String end,int id,Map<String,Object>b); }
class WooCommerceAPI implements Woocommerce {
	WooCommerceAPI(OAuthConfig a,ApiVersionType v){}
	private static Map<String,Object> order(int id) {
		Fixture.Entry o=Fixture.woo.get(id);
		return Map.of("id",id,"status",o.status,"line_items",List.of(Map.of("mode",o.mode)),
				"meta_data",List.of(Map.of("id",83,"key","syncedToIdempiere","value",o.sync)));
	}
	public Map<?,?> get(String e,int id){ return order(id); }
	public List<?> getAll(String e,Map<String,String> p){ return Fixture.woo.keySet().stream()
			.filter(id->Fixture.woo.get(id).status.equals(p.get("status"))).map(WooCommerceAPI::order).toList(); }
	public Map<?,?> update(String e, int id, Map<String,Object>b){
		Fixture.check(Fixture.receipts.containsKey(id) && Fixture.orders.contains(id),"Woo write before ERP receipt commit");
		Fixture.Entry o=Fixture.woo.get(id);
		if(b.containsKey("status")){
			o.status="completed"; o.statusWrites++;
			o.sync="no";
			if(o.loseStatus){o.loseStatus=false;throw new IllegalStateException("lost Woo response");}
		} else {
			o.sync="yes";o.metaWrites++;
			if(o.loseMeta){o.loseMeta=false;throw new IllegalStateException("lost Woo response");}
		}
		return get(e,id);
	}
}
class WcOrder {
	final Trx trx;
	WcOrder(Properties p,String n,PO wc){trx=Trx.instances.get(n);}
	void createOrder(Map<?,?> order,String no){trx.order=(Integer)order.get("id");}
	boolean createOrderLine(Map<?,?> line,Map<?,?> order){
		switch((String)line.get("mode")){
		case "false":return false;
		case "typed":throw new FabricSampleMappingError();
		case "commit":trx.failCommit=true; break;
		}
		return true;
	}
	static final class FabricSampleMappingError extends IllegalStateException {}
	int getOrderLineCount(){return 1;}
	int getcOrderID(){return trx.order + 100;}
	int orderTotalOverZero(){return 0;}
	int getOrderBpID(){return 1;}
	int getbPLocationId(){return 1;}
	String getCustomerNote(){return "";}
	void setBldMtmInstallID(int i){}
	void createShippingCharge(Map<?,?> v){}
	void createPosPayment(Map<?,?> v){}
	void completeOrder(){}
}
class Trx {
	static final Map<String,Trx> instances=new HashMap<>();
	static String createTrxName(String s){return s+"-"+UUID.randomUUID();}
	static Trx get(String n,boolean b){Trx t=new Trx(n);instances.put(n,t);return t;}
	final String name; boolean failCommit=false, ack=false; int claim=0,order=0,ackId=0;
	Trx(String n){name=n;}
	Connection getConnection(){return Fixture.proxy(Connection.class,(p,m,a)->null);}
	String getTrxName(){return name;}
	boolean commit(boolean exception)throws SQLException{
		if(failCommit)throw new SQLException("synthetic unconfirmed commit","40001");
		if(ack){Fixture.receipts.get(ackId).ack=true;}
		if(claim>0){Fixture.check(claim==order,"ERP receipt and order must commit together");Fixture.orders.add(order);Fixture.receipts.put(claim,new Fixture.Receipt());}
		claim=0; ack=false;
		return true;
	}
	boolean rollback(boolean b)throws SQLException{claim=0;ack=false;return true;}
	boolean close(){Fixture.check(claim==0 && !ack,"closed active fake transaction");instances.remove(name);return true;}
}
class DB {
	static PreparedStatement prepareStatement(String q,String name) {
		final Map<Integer,Object> params=new java.util.HashMap<>();
		return Fixture.proxy(PreparedStatement.class,(p,m,a)->switch(m.getName()){
			case "setInt","setString","setObject" -> {params.put((Integer)a[0],a[1]);yield null;}
			case "executeUpdate" -> {
				Trx t=Trx.instances.get(name);
				if(q.startsWith("INSERT")) {
					int id=(Integer)params.get(2);
					if(Fixture.receipts.containsKey(id)) yield 0;
					t.claim=id;
				} else if(q.contains("acknowledged_at=CURRENT_TIMESTAMP")) {
					t.ack=true;t.ackId=(Integer)params.get(2);
				}
				yield 1;
			}
			case "executeQuery" -> {
				if(q.contains("FOR UPDATE")) {
					Fixture.Receipt r=Fixture.receipts.get((Integer)params.get(2));
					yield Fixture.rows(r==null?List.of():List.of(r));
				}
				yield Fixture.rows(Fixture.receipts.entrySet().stream().filter(e->!e.getValue().ack).map(Map.Entry::getKey).toList());
			}
			default -> null;
		});
	}
}
class Fixture {
	static class Entry {String status="processing",sync="no",mode="good"; boolean loseStatus,loseMeta; int statusWrites,metaWrites;}
	static class Receipt { boolean ack; }
	static final Map<Integer,Entry> woo=new LinkedHashMap<>();
	static final Map<Integer,Receipt> receipts=new LinkedHashMap<>();
	static final Set<Integer> orders=new HashSet<>();
	static void check(boolean b,String description){if(!b)throw new AssertionError(description);}
	@SuppressWarnings("unchecked") static <T>T proxy(Class<T> iface,InvocationHandler h){return (T)Proxy.newProxyInstance(iface.getClassLoader(),new Class<?>[]{iface},h);}
	static ResultSet rows(List<?> list){
		int[] pos={-1};
		return proxy(ResultSet.class,(p,m,a)->switch(m.getName()){
			case "next" -> ++pos[0]<list.size();
			case "getInt" -> list.get(pos[0]);
			case "getTimestamp" -> ((Receipt)list.get(pos[0])).ack?new Timestamp(0):null;
			default -> null;
		});
	}
}
'''

def main():
    importer = (PROCESS / "WooCommerce.java").read_text(encoding="utf-8")
    importer = re.sub(r"^package.*;$", "", importer, flags=re.M)
    importer = re.sub(r"^import (?!java\.)[^;]*;$", "", importer, flags=re.M)
    importer = importer.replace("public class WooCommerce extends", "class WooCommerce extends")
    k = importer.index("/**")
    ack = (PROCESS / "WooOrderAcknowledgement.java").read_text(encoding="utf-8")
    ack = ack[ack.index("/**"):]
    with tempfile.TemporaryDirectory() as tmpdir:
        source = Path(tmpdir) / "Runner.java"
        source.write_text(IMPORTS + importer[:k] + RUNNER + importer[k:] + ack + DOUBLES, encoding="utf-8")
        subprocess.run(["java", "--add-modules", "java.sql,java.logging", str(source)], check=True)

if __name__ == "__main__":
    main()

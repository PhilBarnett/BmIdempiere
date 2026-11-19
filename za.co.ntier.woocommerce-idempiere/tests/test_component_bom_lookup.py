from pathlib import Path
import sqlite3,re,json
root=Path(__file__).resolve().parents[1]
source=(root/'src/za/co/ntier/model/WooCommerceComponentLookup.java').read_text()
section=source[source.index('static final String PRODUCT_IDS_SQL ='):source.index('public static List<MProduct>')]
sql=''.join(json.loads(token) for token in re.findall(r'\"(?:[^\"\\]|\\.)*\"',section))
c=sqlite3.connect(':memory:')
c.executescript('''
CREATE TABLE M_Product(M_Product_ID INTEGER PRIMARY KEY,AD_Client_ID INTEGER,M_PartType_ID INTEGER,IsActive TEXT);
CREATE TABLE BLD_Product_PartType(BLD_Product_PartType_ID INTEGER PRIMARY KEY,M_Product_ID INTEGER,AD_Client_ID INTEGER,M_PartType_ID INTEGER,IsActive TEXT,is_user_select TEXT,is_select_other_bld_parttype TEXT,Otherbom_M_Parttype_ID INTEGER);
CREATE TABLE PP_Product_BOM(PP_Product_BOM_ID INTEGER PRIMARY KEY,M_Product_ID INTEGER,AD_Client_ID INTEGER,IsActive TEXT,BOMType TEXT);
CREATE TABLE PP_Product_BOMLine(PP_Product_BOMLine_ID INTEGER PRIMARY KEY,PP_Product_BOM_ID INTEGER,M_Product_ID INTEGER,AD_Client_ID INTEGER,IsActive TEXT);
INSERT INTO M_Product VALUES (100,10,1,'Y'),(101,10,20,'Y'),(102,10,20,'Y'),(201,10,30,'Y'),(301,10,40,'Y'),(302,10,40,'Y'),(401,10,50,'Y'),(501,99,40,'Y'),(601,10,40,'Y');
INSERT INTO BLD_Product_PartType VALUES (11,100,10,30,'Y','Y','N',0),(12,100,10,40,'Y','Y','Y',20),(13,100,10,50,'Y','Y','Y',20),(14,100,10,40,'Y','Y','N',0);
INSERT INTO PP_Product_BOM VALUES (1000,100,10,'Y','A'),(1001,101,10,'Y','A'),(1002,102,10,'Y','A'),(1003,101,10,'N','A');
INSERT INTO PP_Product_BOMLine VALUES (1,1000,101,10,'Y'),(2,1000,201,10,'Y'),(3,1001,301,10,'Y'),(4,1001,401,10,'Y'),(5,1002,302,10,'Y'),(6,1001,501,10,'Y'),(7,1003,601,10,'Y');
''')
def rows(selector,parent=100,tenant=10):return [r[0] for r in c.execute(sql,(parent,selector,tenant))]
assert rows(11)==[201],'Direct fabric no longer selectable'
assert rows(12)==[301],'Nested bracket should exclude unrelated track, inactive BOM and foreign tenant'
assert rows(13)==[401],'Nested profile missing or mixed with bracket'
assert rows(14)==[],'Nested BOM leaked into ordinary selector'
assert rows(12,parent=101)==[],'Wrong parent accepted'
assert rows(12,tenant=99)==[],'Cross-tenant lookup accepted'
for table,key,record,column,value in [('M_Product','M_Product_ID',100,'IsActive','N'),('M_Product','M_Product_ID',101,'IsActive','N'),('M_Product','M_Product_ID',301,'IsActive','N'),('PP_Product_BOMLine','PP_Product_BOMLine_ID',1,'IsActive','N'),('PP_Product_BOMLine','PP_Product_BOMLine_ID',3,'IsActive','N'),('PP_Product_BOM','PP_Product_BOM_ID',1000,'IsActive','N'),('PP_Product_BOM','PP_Product_BOM_ID',1001,'IsActive','N'),('PP_Product_BOM','PP_Product_BOM_ID',1001,'BOMType','X'),('BLD_Product_PartType','BLD_Product_PartType_ID',12,'IsActive','N'),('BLD_Product_PartType','BLD_Product_PartType_ID',12,'Otherbom_M_Parttype_ID',21),('BLD_Product_PartType','BLD_Product_PartType_ID',12,'AD_Client_ID',99)]:
 c.execute('SAVEPOINT test')
 c.execute(f'UPDATE {table} SET {column}=? WHERE {key}=?',(value,record))
 assert rows(12)==[],(table,record,column)
 c.execute('ROLLBACK TO test');c.execute('RELEASE test')
c.execute("INSERT INTO PP_Product_BOMLine VALUES (8,1001,301,10,'Y')")
assert rows(12)==[301],'Repeated BOM row duplicated product choice'
migration=(root/'migration/20261120_woocommerce_component_bom_source.sql').read_text()
validation=migration.split('new_code text := $new$',1)[1].split('$new$;',1)[0]
for token,value in [('@M_Product_ID@','100'),('@BLD_Product_PartType_ID@','12'),('@#AD_Client_ID@','10')]:validation=validation.replace(token,value)
assert list(c.execute('SELECT M_Product_ID FROM M_Product WHERE '+validation))==[(301,)],'UI predicate differs from Java resolver'
print('PASS: direct fabric, nested bracket/profile, unrelated tracks, inactive sources, wrong part types, tenant boundaries, duplicates and UI/Java parity')

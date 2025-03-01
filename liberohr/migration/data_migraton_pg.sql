-- Mar 27, 2013 5:59:02 AM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Contract (HR_Contract_ID,NetDays,ValidFrom,Description,Name,Value,AD_Org_ID,Created,CreatedBy,IsActive,Updated,UpdatedBy,AD_Client_ID,HR_Contract_UU) VALUES (1000000,30,TO_TIMESTAMP('2013-03-01 00:00:00','YYYY-MM-DD HH24:MI:SS'),'Monthly','Monthly','Monthly',0,TO_TIMESTAMP('2013-03-27 05:59:02','YYYY-MM-DD HH24:MI:SS'),100,'Y',TO_TIMESTAMP('2013-03-27 05:59:02','YYYY-MM-DD HH24:MI:SS'),100,11,'2843de75-ec9f-4c43-9b5a-7f0b37fe232c')
;

-- Mar 27, 2013 6:11:26 AM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Payroll (Processing,HR_Contract_ID,PaymentRule,Processed,HR_Payroll_ID,HR_Payroll_UU,Name,Value,Description,Created,CreatedBy,AD_Client_ID,Updated,UpdatedBy,AD_Org_ID,IsActive) VALUES ('N',1000000,'A','N',1000000,'b9f0e944-f6d1-4889-a3e3-0ec12d77c6d4','Monthly','Monthly','Monthly',TO_TIMESTAMP('2013-03-27 06:11:26','YYYY-MM-DD HH24:MI:SS'),100,11,TO_TIMESTAMP('2013-03-27 06:11:26','YYYY-MM-DD HH24:MI:SS'),100,11,'Y')
;

-- Mar 27, 2013 6:15:19 AM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Concept (HR_Concept_ID,ColumnType,IsReceipt,IsPaid,IsReadWrite,IsEmployee,IsPrinted,IsRegistered,Name,Type,IsDefault,Value,AD_Client_ID,IsActive,Updated,UpdatedBy,AD_Org_ID,Created,CreatedBy,HR_Concept_UU,SeqNo) VALUES (1000000,'A','N','Y','N','N','N','N','Monthly Salary','E','N','CC_SALARY',11,'Y',TO_TIMESTAMP('2013-03-27 06:15:19','YYYY-MM-DD HH24:MI:SS'),100,11,TO_TIMESTAMP('2013-03-27 06:15:19','YYYY-MM-DD HH24:MI:SS'),100,'bc8d9551-1562-4fcd-a463-e0cf6c1b667f',0)
;

-- Mar 27, 2013 6:16:23 AM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE HR_Concept SET IsEmployee='Y', Type='C',Updated=TO_TIMESTAMP('2013-03-27 06:16:23','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE HR_Concept_ID=1000000
;

-- Mar 27, 2013 6:16:53 AM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Attribute (MinValue,MaxValue,Amount,ColumnType,IsPrinted,Qty,ValidFrom,HR_Concept_ID,HR_Attribute_ID,C_BPartner_ID,AD_Client_ID,AD_Org_ID,Created,CreatedBy,Updated,UpdatedBy,IsActive,HR_Attribute_UU) VALUES (0,0,2500.000000000000,'A','N',0,TO_TIMESTAMP('2013-03-01','YYYY-MM-DD'),1000000,1000000,113,11,11,TO_TIMESTAMP('2013-03-27 06:16:53','YYYY-MM-DD HH24:MI:SS'),100,TO_TIMESTAMP('2013-03-27 06:16:53','YYYY-MM-DD HH24:MI:SS'),100,'Y','891f3f89-8fb3-4885-b0aa-dd9216d83629')
;

-- Mar 27, 2013 6:18:47 AM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO C_ValidCombination (C_ValidCombination_ID,IsFullyQualified,Combination,C_AcctSchema_ID,Account_ID,Description,C_ValidCombination_UU,IsActive,Created,CreatedBy,Updated,UpdatedBy,AD_Org_ID,AD_Client_ID) VALUES (1000001,'Y','HQ-60990-_-_-_-_',101,472,'HQ-Payroll Processing Expense-_-_-_-_','b2ab814c-dc60-43f1-aace-f8c6c2d444c7','Y',TO_TIMESTAMP('2013-03-27 06:18:47','YYYY-MM-DD HH24:MI:SS'),100,TO_TIMESTAMP('2013-03-27 06:18:47','YYYY-MM-DD HH24:MI:SS'),100,11,11)
;

-- Mar 27, 2013 6:19:32 AM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO C_ValidCombination (C_ValidCombination_ID,IsFullyQualified,Combination,C_AcctSchema_ID,Account_ID,Description,C_ValidCombination_UU,IsActive,Created,CreatedBy,Updated,UpdatedBy,AD_Org_ID,AD_Client_ID) VALUES (1000002,'Y','HQ-22200-_-_-_-_',101,603,'HQ-Payroll Withholdings-_-_-_-_','74fe79af-85e6-4e0c-88db-618ab47c89ae','Y',TO_TIMESTAMP('2013-03-27 06:19:32','YYYY-MM-DD HH24:MI:SS'),100,TO_TIMESTAMP('2013-03-27 06:19:32','YYYY-MM-DD HH24:MI:SS'),100,11,11)
;

-- Mar 27, 2013 6:19:43 AM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Concept_Acct (HR_Revenue_Acct,C_BP_Group_ID,HR_Concept_ID,HR_Expense_Acct,C_AcctSchema_ID,HR_Concept_Acct_ID,IsBalancing,CreatedBy,AD_Client_ID,Updated,UpdatedBy,AD_Org_ID,Created,IsActive,HR_Concept_Acct_UU) VALUES (1000001,105,1000000,1000000,101,1000000,'N',100,11,TO_TIMESTAMP('2013-03-27 06:19:43','YYYY-MM-DD HH24:MI:SS'),100,11,TO_TIMESTAMP('2013-03-27 06:19:43','YYYY-MM-DD HH24:MI:SS'),'Y','4f6dd1dc-9b9e-4dff-9beb-35bbd4ee4e2b')
;

-- Mar 27, 2013 2:36:40 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_PayrollConcept (HR_Payroll_ID,HR_Concept_ID,IsPrinted,IsInclude,SeqNo,HR_PayrollConcept_ID,Name,IsDisplayed,HR_PayrollConcept_UU,AD_Org_ID,Created,CreatedBy,Updated,UpdatedBy,AD_Client_ID,IsActive) VALUES (1000000,1000000,'N','N',0,1000000,'CC_SALARY','Y','097453a1-96cc-40b4-bf03-8d857d23e098',11,TO_TIMESTAMP('2013-03-27 14:36:40','YYYY-MM-DD HH24:MI:SS'),100,TO_TIMESTAMP('2013-03-27 14:36:40','YYYY-MM-DD HH24:MI:SS'),100,11,'Y')
;

-- Mar 27, 2013 2:38:13 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Year (HR_Payroll_ID,Processing,HR_Year_UU,HR_Year_ID,C_Year_ID,NetDays,Processed,Qty,StartDate,AD_Client_ID,UpdatedBy,Updated,AD_Org_ID,Created,CreatedBy,IsActive) VALUES (1000000,'N','c73150f8-cba0-42db-a36b-ebe05ddfd80f',1000000,200001,30,'N',0,TO_TIMESTAMP('2013-01-01','YYYY-MM-DD'),11,100,TO_TIMESTAMP('2013-03-27 14:38:13','YYYY-MM-DD HH24:MI:SS'),11,TO_TIMESTAMP('2013-03-27 14:38:13','YYYY-MM-DD HH24:MI:SS'),100,'Y')
;

-- Mar 27, 2013 2:38:43 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE HR_Year SET Qty=12,Updated=TO_TIMESTAMP('2013-03-27 14:38:43','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE HR_Year_ID=1000000
;

-- Mar 27, 2013 2:38:46 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Period (HR_Year_ID,HR_Period_ID,C_Year_ID,C_Period_ID,DateAcct,EndDate,HR_Payroll_ID,PeriodNo,Processed,StartDate,HR_Period_UU,Description,Name,AD_Org_ID,Created,CreatedBy,Processing,AD_Client_ID,UpdatedBy,IsActive,Updated) VALUES (1000000,1000000,200001,200012,TO_TIMESTAMP('2013-01-31','YYYY-MM-DD'),TO_TIMESTAMP('2013-01-31','YYYY-MM-DD'),1000000,1,'N',TO_TIMESTAMP('2013-01-01','YYYY-MM-DD'),'09519675-a51b-44bd-9ae4-d47e152d398e','Payroll Monthly From 1 To 2013-01-01 al 2013-01-31','2013-01-01 To 2013-01-31',11,TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'),100,'N',11,100,'Y',TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'))
;

-- Mar 27, 2013 2:38:46 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Period (HR_Year_ID,HR_Period_ID,C_Year_ID,C_Period_ID,DateAcct,EndDate,HR_Payroll_ID,PeriodNo,Processed,StartDate,HR_Period_UU,Description,Name,AD_Org_ID,Created,CreatedBy,Processing,AD_Client_ID,UpdatedBy,IsActive,Updated) VALUES (1000000,1000001,200001,200013,TO_TIMESTAMP('2013-02-28','YYYY-MM-DD'),TO_TIMESTAMP('2013-02-28','YYYY-MM-DD'),1000000,2,'N',TO_TIMESTAMP('2013-02-01','YYYY-MM-DD'),'fda247f2-baf1-4539-b485-61997e91920a','Payroll Monthly From 2 To 2013-02-01 al 2013-02-28','2013-02-01 To 2013-02-28',11,TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'),100,'N',11,100,'Y',TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'))
;

-- Mar 27, 2013 2:38:46 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Period (HR_Year_ID,HR_Period_ID,C_Year_ID,C_Period_ID,DateAcct,EndDate,HR_Payroll_ID,PeriodNo,Processed,StartDate,HR_Period_UU,Description,Name,AD_Org_ID,Created,CreatedBy,Processing,AD_Client_ID,UpdatedBy,IsActive,Updated) VALUES (1000000,1000002,200001,200014,TO_TIMESTAMP('2013-03-31','YYYY-MM-DD'),TO_TIMESTAMP('2013-03-31','YYYY-MM-DD'),1000000,3,'N',TO_TIMESTAMP('2013-03-01','YYYY-MM-DD'),'b7f3e797-519b-4e97-af43-f347fed926c2','Payroll Monthly From 3 To 2013-03-01 al 2013-03-31','2013-03-01 To 2013-03-31',11,TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'),100,'N',11,100,'Y',TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'))
;

-- Mar 27, 2013 2:38:46 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Period (HR_Year_ID,HR_Period_ID,C_Year_ID,C_Period_ID,DateAcct,EndDate,HR_Payroll_ID,PeriodNo,Processed,StartDate,HR_Period_UU,Description,Name,AD_Org_ID,Created,CreatedBy,Processing,AD_Client_ID,UpdatedBy,IsActive,Updated) VALUES (1000000,1000003,200001,200015,TO_TIMESTAMP('2013-04-30','YYYY-MM-DD'),TO_TIMESTAMP('2013-04-30','YYYY-MM-DD'),1000000,4,'N',TO_TIMESTAMP('2013-04-01','YYYY-MM-DD'),'a6c2236c-a1da-4e86-8489-4cd0e3f6f5dd','Payroll Monthly From 4 To 2013-04-01 al 2013-04-30','2013-04-01 To 2013-04-30',11,TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'),100,'N',11,100,'Y',TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'))
;

-- Mar 27, 2013 2:38:46 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Period (HR_Year_ID,HR_Period_ID,C_Year_ID,C_Period_ID,DateAcct,EndDate,HR_Payroll_ID,PeriodNo,Processed,StartDate,HR_Period_UU,Description,Name,AD_Org_ID,Created,CreatedBy,Processing,AD_Client_ID,UpdatedBy,IsActive,Updated) VALUES (1000000,1000004,200001,200016,TO_TIMESTAMP('2013-05-31','YYYY-MM-DD'),TO_TIMESTAMP('2013-05-31','YYYY-MM-DD'),1000000,5,'N',TO_TIMESTAMP('2013-05-01','YYYY-MM-DD'),'832114e1-d60a-4a91-8714-59d7a184c8df','Payroll Monthly From 5 To 2013-05-01 al 2013-05-31','2013-05-01 To 2013-05-31',11,TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'),100,'N',11,100,'Y',TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'))
;

-- Mar 27, 2013 2:38:46 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Period (HR_Year_ID,HR_Period_ID,C_Year_ID,C_Period_ID,DateAcct,EndDate,HR_Payroll_ID,PeriodNo,Processed,StartDate,HR_Period_UU,Description,Name,AD_Org_ID,Created,CreatedBy,Processing,AD_Client_ID,UpdatedBy,IsActive,Updated) VALUES (1000000,1000005,200001,200017,TO_TIMESTAMP('2013-06-30','YYYY-MM-DD'),TO_TIMESTAMP('2013-06-30','YYYY-MM-DD'),1000000,6,'N',TO_TIMESTAMP('2013-06-01','YYYY-MM-DD'),'bea8db20-800d-405c-ac8d-0e82857c2662','Payroll Monthly From 6 To 2013-06-01 al 2013-06-30','2013-06-01 To 2013-06-30',11,TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'),100,'N',11,100,'Y',TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'))
;

-- Mar 27, 2013 2:38:46 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Period (HR_Year_ID,HR_Period_ID,C_Year_ID,C_Period_ID,DateAcct,EndDate,HR_Payroll_ID,PeriodNo,Processed,StartDate,HR_Period_UU,Description,Name,AD_Org_ID,Created,CreatedBy,Processing,AD_Client_ID,UpdatedBy,IsActive,Updated) VALUES (1000000,1000006,200001,200018,TO_TIMESTAMP('2013-07-31','YYYY-MM-DD'),TO_TIMESTAMP('2013-07-31','YYYY-MM-DD'),1000000,7,'N',TO_TIMESTAMP('2013-07-01','YYYY-MM-DD'),'0084be2a-5eac-4f0d-a6d8-e747f72bb596','Payroll Monthly From 7 To 2013-07-01 al 2013-07-31','2013-07-01 To 2013-07-31',11,TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'),100,'N',11,100,'Y',TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'))
;

-- Mar 27, 2013 2:38:46 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Period (HR_Year_ID,HR_Period_ID,C_Year_ID,C_Period_ID,DateAcct,EndDate,HR_Payroll_ID,PeriodNo,Processed,StartDate,HR_Period_UU,Description,Name,AD_Org_ID,Created,CreatedBy,Processing,AD_Client_ID,UpdatedBy,IsActive,Updated) VALUES (1000000,1000007,200001,200019,TO_TIMESTAMP('2013-08-31','YYYY-MM-DD'),TO_TIMESTAMP('2013-08-31','YYYY-MM-DD'),1000000,8,'N',TO_TIMESTAMP('2013-08-01','YYYY-MM-DD'),'b4b879eb-f0b4-4817-83ef-49a2a1cb04d1','Payroll Monthly From 8 To 2013-08-01 al 2013-08-31','2013-08-01 To 2013-08-31',11,TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'),100,'N',11,100,'Y',TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'))
;

-- Mar 27, 2013 2:38:46 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Period (HR_Year_ID,HR_Period_ID,C_Year_ID,C_Period_ID,DateAcct,EndDate,HR_Payroll_ID,PeriodNo,Processed,StartDate,HR_Period_UU,Description,Name,AD_Org_ID,Created,CreatedBy,Processing,AD_Client_ID,UpdatedBy,IsActive,Updated) VALUES (1000000,1000008,200001,200020,TO_TIMESTAMP('2013-09-30','YYYY-MM-DD'),TO_TIMESTAMP('2013-09-30','YYYY-MM-DD'),1000000,9,'N',TO_TIMESTAMP('2013-09-01','YYYY-MM-DD'),'3daa5f23-72d0-40ab-abfe-3398056e29b6','Payroll Monthly From 9 To 2013-09-01 al 2013-09-30','2013-09-01 To 2013-09-30',11,TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'),100,'N',11,100,'Y',TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'))
;

-- Mar 27, 2013 2:38:46 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Period (HR_Year_ID,HR_Period_ID,C_Year_ID,C_Period_ID,DateAcct,EndDate,HR_Payroll_ID,PeriodNo,Processed,StartDate,HR_Period_UU,Description,Name,AD_Org_ID,Created,CreatedBy,Processing,AD_Client_ID,UpdatedBy,IsActive,Updated) VALUES (1000000,1000009,200001,200021,TO_TIMESTAMP('2013-10-31','YYYY-MM-DD'),TO_TIMESTAMP('2013-10-31','YYYY-MM-DD'),1000000,10,'N',TO_TIMESTAMP('2013-10-01','YYYY-MM-DD'),'a0320fb5-b896-44bd-b4d3-1a104e980563','Payroll Monthly From 10 To 2013-10-01 al 2013-10-31','2013-10-01 To 2013-10-31',11,TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'),100,'N',11,100,'Y',TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'))
;

-- Mar 27, 2013 2:38:46 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Period (HR_Year_ID,HR_Period_ID,C_Year_ID,C_Period_ID,DateAcct,EndDate,HR_Payroll_ID,PeriodNo,Processed,StartDate,HR_Period_UU,Description,Name,AD_Org_ID,Created,CreatedBy,Processing,AD_Client_ID,UpdatedBy,IsActive,Updated) VALUES (1000000,1000010,200001,200022,TO_TIMESTAMP('2013-11-30','YYYY-MM-DD'),TO_TIMESTAMP('2013-11-30','YYYY-MM-DD'),1000000,11,'N',TO_TIMESTAMP('2013-11-01','YYYY-MM-DD'),'3a0222a3-2227-4316-9446-adb53594750b','Payroll Monthly From 11 To 2013-11-01 al 2013-11-30','2013-11-01 To 2013-11-30',11,TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'),100,'N',11,100,'Y',TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'))
;

-- Mar 27, 2013 2:38:46 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Period (HR_Year_ID,HR_Period_ID,C_Year_ID,C_Period_ID,DateAcct,EndDate,HR_Payroll_ID,PeriodNo,Processed,StartDate,HR_Period_UU,Description,Name,AD_Org_ID,Created,CreatedBy,Processing,AD_Client_ID,UpdatedBy,IsActive,Updated) VALUES (1000000,1000011,200001,200023,TO_TIMESTAMP('2013-12-31','YYYY-MM-DD'),TO_TIMESTAMP('2013-12-31','YYYY-MM-DD'),1000000,12,'N',TO_TIMESTAMP('2013-12-01','YYYY-MM-DD'),'8c259718-7c26-4c34-a2ea-1d5a824f6275','Payroll Monthly From 12 To 2013-12-01 al 2013-12-31','2013-12-01 To 2013-12-31',11,TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'),100,'N',11,100,'Y',TO_TIMESTAMP('2013-03-27 14:38:46','YYYY-MM-DD HH24:MI:SS'))
;

-- Mar 27, 2013 2:39:12 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Process (Posted,DocAction,Processing,HR_Payroll_ID,HR_Process_ID,Processed,DateAcct,DocStatus,C_DocType_ID,Name,HR_Process_UU,DocumentNo,C_DocTypeTarget_ID,Updated,AD_Client_ID,Created,CreatedBy,UpdatedBy,AD_Org_ID,IsActive) VALUES ('N','CO','N',1000000,1000000,'N',TO_TIMESTAMP('2013-03-01','YYYY-MM-DD'),'DR',126,'PaySalary','62ed4d7c-b97d-424f-b6f9-105694056129','100000',50000,TO_TIMESTAMP('2013-03-27 14:39:12','YYYY-MM-DD HH24:MI:SS'),11,TO_TIMESTAMP('2013-03-27 14:39:12','YYYY-MM-DD HH24:MI:SS'),100,100,11,'Y')
;

-- Mar 28, 2013 8:10:07 AM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Department (HR_Department_ID,Name,Value,UpdatedBy,AD_Org_ID,Created,CreatedBy,IsActive,Updated,AD_Client_ID,HR_Department_UU) VALUES (1000000,'Compiere Support','Compiere Support',100,0,TO_TIMESTAMP('2013-03-28 08:10:07','YYYY-MM-DD HH24:MI:SS'),100,'Y',TO_TIMESTAMP('2013-03-28 08:10:07','YYYY-MM-DD HH24:MI:SS'),11,'391baf04-a2bc-4ded-b003-a197068e9264')
;

-- Mar 28, 2013 8:10:36 AM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Job (HR_Department_ID,IsParent,JobCant,HR_Job_ID,Name,Value,CreatedBy,Updated,UpdatedBy,AD_Client_ID,HR_Job_UU,Created,AD_Org_ID,IsActive) VALUES (1000000,'N',0,1000000,'Job','Job',100,TO_TIMESTAMP('2013-03-28 08:10:36','YYYY-MM-DD HH24:MI:SS'),100,11,'d6f15890-587d-4df3-bed6-2a564e349516',TO_TIMESTAMP('2013-03-28 08:10:36','YYYY-MM-DD HH24:MI:SS'),0,'Y')
;

-- Mar 28, 2013 8:10:53 AM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Employee (HR_Employee_UU,HR_Payroll_ID,StartDate,HR_Job_ID,HR_Department_ID,C_BPartner_ID,HR_Employee_ID,CreatedBy,UpdatedBy,Created,IsActive,Updated,AD_Client_ID,AD_Org_ID) VALUES ('b84a25b3-0aaa-4469-914b-90579b691970',1000000,TO_TIMESTAMP('2013-03-01','YYYY-MM-DD'),1000000,1000000,119,1000000,100,100,TO_TIMESTAMP('2013-03-28 08:10:53','YYYY-MM-DD HH24:MI:SS'),'Y',TO_TIMESTAMP('2013-03-28 08:10:53','YYYY-MM-DD HH24:MI:SS'),11,11)
;

-- Mar 28, 2013 8:11:17 AM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE HR_Employee SET ImageURL='http://www.red1.org',Updated=TO_TIMESTAMP('2013-03-28 08:11:17','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE HR_Employee_ID=1000000
;

-- Apr 1, 2013 4:20:15 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Employee (HR_Employee_UU,HR_Payroll_ID,StartDate,HR_Job_ID,HR_Department_ID,C_BPartner_ID,HR_Employee_ID,CreatedBy,UpdatedBy,Created,IsActive,Updated,AD_Client_ID,AD_Org_ID) VALUES ('1c1d148d-4377-4b9b-9cd8-8f1f91d26c23',1000000,TO_TIMESTAMP('2013-01-01','YYYY-MM-DD'),1000000,1000000,113,1000001,100,100,TO_TIMESTAMP('2013-04-01 16:20:15','YYYY-MM-DD HH24:MI:SS'),'Y',TO_TIMESTAMP('2013-04-01 16:20:15','YYYY-MM-DD HH24:MI:SS'),11,11)
;

-- Apr 1, 2013 4:21:28 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Attribute (MinValue,MaxValue,Amount,ColumnType,IsPrinted,Qty,ValidFrom,HR_Employee_ID,HR_Attribute_ID,C_BPartner_ID,AD_Client_ID,AD_Org_ID,Created,CreatedBy,Updated,UpdatedBy,IsActive,HR_Attribute_UU,HR_Concept_ID) VALUES (0,0,1500.000000000000,'A','N',0,TO_TIMESTAMP('2013-01-01','YYYY-MM-DD'),1000000,1000001,119,11,11,TO_TIMESTAMP('2013-04-01 16:21:28','YYYY-MM-DD HH24:MI:SS'),100,TO_TIMESTAMP('2013-04-01 16:21:28','YYYY-MM-DD HH24:MI:SS'),100,'Y','af50e536-5875-4be6-bd6d-e8e11385a7db',1000000)
;

-- Apr 1, 2013 4:21:39 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE HR_Employee SET StartDate=TO_TIMESTAMP('2013-01-01','YYYY-MM-DD'),Updated=TO_TIMESTAMP('2013-04-01 16:21:39','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE HR_Employee_ID=1000000
;

-- Apr 1, 2013 4:23:38 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE HR_Attribute SET ValidFrom=TO_TIMESTAMP('2013-01-01','YYYY-MM-DD'),Updated=TO_TIMESTAMP('2013-04-01 16:23:38','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE HR_Attribute_ID=1000000
;

-- Apr 1, 2013 4:24:39 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
INSERT INTO HR_Process (Posted,DocAction,Processing,HR_Payroll_ID,HR_Process_ID,Processed,DateAcct,DocStatus,HR_Job_ID,HR_Period_ID,C_DocType_ID,HR_Department_ID,Name,HR_Process_UU,DocumentNo,C_DocTypeTarget_ID,Updated,AD_Client_ID,Created,CreatedBy,UpdatedBy,AD_Org_ID,IsActive) VALUES ('N','CO','N',1000000,1000001,'N',TO_TIMESTAMP('2013-01-01','YYYY-MM-DD'),'DR',1000000,1000000,126,1000000,'PayNow','449be3ce-3379-49ff-b70d-0836d4a880c1','100000',50000,TO_TIMESTAMP('2013-04-01 16:24:38','YYYY-MM-DD HH24:MI:SS'),11,TO_TIMESTAMP('2013-04-01 16:24:38','YYYY-MM-DD HH24:MI:SS'),100,100,11,'Y')
;

-- Apr 2, 2013 4:16:20 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE AD_Val_Rule SET Code='docstatus = ''CL'' 
AND hr_process_id IN 
(SELECT hr_process_id FROM hr_movement WHERE c_invoiceline_id IS NULL)',Updated=TO_TIMESTAMP('2013-04-02 16:16:20','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Val_Rule_ID=1000000
;

-- Apr 2, 2013 4:59:45 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE HR_Concept SET AccountSign='D',Updated=TO_TIMESTAMP('2013-04-02 16:59:45','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE HR_Concept_ID=1000000
;

-- Apr 2, 2013 5:04:09 PM MYT
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE HR_Concept_Acct SET IsBalancing='N',Updated=TO_TIMESTAMP('2013-04-02 17:04:09','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE HR_Concept_Acct_ID=1000000
;

UPDATE HR_Payroll SET C_Charge_ID=100,Updated=TO_TIMESTAMP('2013-04-02 17:09:52','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE HR_Payroll_ID=1000000
;

UPDATE AD_Val_Rule SET Code='docbasetype = ''HRP''',Updated=TO_TIMESTAMP('2013-04-02 17:27:39','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Val_Rule_ID=1000001
;

-- Apr 5, 2013 10:50:48 AM CEST
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
DELETE FROM HR_Process WHERE HR_Process_ID=1000000
;

-- Apr 5, 2013 10:51:25 AM CEST
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE HR_Process SET C_Charge_ID=101,Updated=TO_TIMESTAMP('2013-04-05 10:51:25','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE HR_Process_ID=1000001
;

-- Apr 5, 2013 10:55:28 AM CEST
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE AD_Column SET AD_Val_Rule_ID=NULL,Updated=TO_TIMESTAMP('2013-04-05 10:55:28','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Column_ID=1000020
;

-- Apr 7, 2013 10:05:44 PM CEST
-- I forgot to set the DICTIONARY_ID_COMMENTS System Configurator
UPDATE AD_Val_Rule SET Code='docbasetype = ''API''',Updated=TO_TIMESTAMP('2013-04-07 22:05:44','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Val_Rule_ID=1000001
;

UPDATE HR_Concept SET C_Charge_ID=101,Updated=TO_TIMESTAMP('2013-04-08 12:06:04','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE HR_Concept_ID=1000000
;



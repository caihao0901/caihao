## 行转列操作
## 第一步的数据
## 船名        英文船名  航次    装卸船类型                 关联标识
## 中谷辽宁	null	1614N	DISCHARGE	98FD8DFA-BF41-4B10-937C-0EAA3E06A679
## 中谷辽宁	null	1614S	LOAD	        98FD8DFA-BF41-4B10-937C-0EAA3E06A679
## 第二步数据
##   船名                关联标识                       进口航次 出口航次
## 中谷辽宁	98FD8DFA-BF41-4B10-937C-0EAA3E06A679	1614N	 null
## 中谷辽宁	98FD8DFA-BF41-4B10-937C-0EAA3E06A679	null	1614S
## 第三步数据
##   船名                关联标识                       进口航次 出口航次
## 中谷辽宁	98FD8DFA-BF41-4B10-937C-0EAA3E06A679	1614N	1614S
##备注：第三步 最外层的sql行转列不能用sum，因为是varchar类型
## 使用max函数成功解决这个问题


select VESSEL_CN,LOAD_DISC_RELATION,
max(进口航次) as '进口航次',max(出口航次) as '出口航次'
from
(
SELECT
VESSEL_CN,LOAD_DISC_RELATION,
case when MAIN_TYPE = 'DISCHARGE' then  VOYAGE  else '' END as '进口航次',
case when MAIN_TYPE = 'LOAD' then  VOYAGE  else '' END as '出口航次'
from
(
SELECT
 CT.VESSEL_CN,
 CT.VESSEL_EN,
 CT.VOYAGE,
 CT.MAIN_TYPE,
 CT.LOAD_DISC_RELATION
FROM CNTR_THD_HANDLING_INFO_TOTAIL CT WITH(NOLOCK)
LEFT JOIN CNTR_THD_SYS_ORG_INFO CO WITH(NOLOCK) ON CO.COMPANY_ID = CT.USER_ID
WHERE 1=1
AND CT.LOAD_DISC_RELATION IS NOT NULL
AND CO.COMPANY_CODE = 'TJYGSMT'
)A
)B
GROUP BY VESSEL_CN,LOAD_DISC_RELATION
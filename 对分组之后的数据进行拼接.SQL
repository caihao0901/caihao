--#  对分组之后的数据进行合并
--#  第一步
--#  union之后数据为
--#  标识        地点   类型    数量       重量
--#  ZHG	上海	20GPF	212	 5659.28
--#  ZHG	上海	20TKF	2	   43.6
--#  ZHG	上海	40HCF	69	 1861.76
--#  ZHG	天津	20GPF	197	 5679
--#  ZHG	天津	20TKF	1	   28
--#  ZHG	天津	40HCF	126	 3716.16
--#  XHG	宁波	20GPF	114	 3299
--#  XHG	宁波	40HCF	21	 588.4
--#  XHG	上海	20GPF	131	 3751.1
--#  XHG	上海	20TKF	1        28
--#  XHG	上海	40HCF	126	 3716.16
--#  XHG	天津	20GPF	164	 4288.18
--#  XHG	天津	20TKF	2	   43.6
--#  XHG	天津	40HCF	48	 1273.36
--#  第二步
--#  根据标识地点进行分组拼接类型数量重量用“-”
--#  标识        地点          数据
--#  ZHG	上海	20GPF-212-5659.2800
--#  ZHG	上海	20TKF-2-43.6000
--#  ZHG	上海	40HCF-69-1861.7600
--#  ZHG	天津	20GPF-197-5679.0000
--#  ZHG	天津	20TKF-1-28.0000
--#  ZHG	天津	40HCF-126-3716.1600
--#  XHG	宁波	20GPF-114-3299.0000
--#  XHG	宁波	40HCF-21-588.4000
--#  XHG	上海	20GPF-131-3751.1000
--#  XHG	上海	20TKF-1-28.0000
--#  XHG	上海	40HCF-126-3716.1600
--#  XHG	天津	20GPF-164-4288.1800
--#  XHG	天津	20TKF-2-43.6000
--#  XHG	天津	40HCF-48-1273.3600
--#  第三步
--#  再次分组以上的数据进行数据拼接用“=”
--#  标识        地点                   最终数据
--#  ZHG	上海	20GPF-212-5659.2800=20TKF-2-43.6000=40HCF-69-1861.7600
--#  ZHG	天津	20GPF-197-5679.0000=20TKF-1-28.0000=40HCF-126-3716.1600
--#  XHG	宁波	20GPF-114-3299.0000=40HCF-21-588.4000
--#  XHG	上海	20GPF-131-3751.1000=20TKF-1-28.0000=40HCF-126-3716.1600
--#  XHG	天津	20GPF-164-4288.1800=20TKF-2-43.6000=40HCF-48-1273.3600
--#  最开始思路是对第二组的数据进行case when操作之后列转行
--#  但是考虑到类型有很多种，挨个写case when可能有空的情况，最后用了拼接的方式
--#  代码取到数据之后，用“=”分割一次，再用“-”分割一次就可以取到需要的数据


SELECT 
type,
port,

STUFF((
          SELECT '=' + total
          FROM (select 
type,
port,
boxType + '-' +cast(boxTotalNumber as varchar) + '-' +cast(boxTotalWeight as varchar) as total
from 
(
(
SELECT
 'ZHG' as type,
	ZHG as port,
	XX + XL + XT as boxType,
	COUNT( XX + XL + XT ) as boxTotalNumber, 
	sum( W_CONT.zl )  as boxTotalWeight
FROM
	W_voyBOOK 
	INNER JOIN W_BOOK  ON W_BOOK.ID = bookid
	INNER JOIN W_VOYCONT  ON W_voyCONT.voyBOOKID = W_voyBOOK.voybookID
	INNER JOIN W_CONT ON W_CONT.ID = contid 
	inner join w_cqb on w_cqb.id = W_voyBOOK.voyid
WHERE
		w_cqb.zwcm = '新瓯15'
	and w_cqb.hc = '1709S'
GROUP BY
	ZHG,
	XX + XL + XT 
)
UNION
(SELECT
  'XHG' as type,
	XHG as port,
	XX + XL + XT as boxType,
	COUNT ( XX + XL + XT )  as boxTotalNumber,
	SUM ( W_CONT.zl )  as boxTotalWeight
FROM
	W_voyBOOK
	INNER JOIN W_BOOK  ON W_BOOK.ID= bookid
	INNER JOIN W_VOYCONT  ON W_voyCONT.voyBOOKID= W_voyBOOK.voybookID
	INNER JOIN W_CONT ON W_CONT.ID= contid 
	inner join w_cqb on w_cqb.id = W_voyBOOK.voyid
WHERE
	w_cqb.zwcm = '新瓯15'
	and w_cqb.hc = '1709S'
GROUP BY
	XHG,
	XX + XL + XT ) 
	)A
	) T
          WHERE B.type = T.type
					and B.port = T.port
          FOR XML PATH('')), 1, 1, '') as totalData 

FROM


(select 
type,
port,
boxType + '-' +cast(boxTotalNumber as varchar) + '-' +cast(boxTotalWeight as varchar) as total
from 
(
(
SELECT
 'ZHG' as type,
	ZHG as port,
	XX + XL + XT as boxType,
	COUNT( XX + XL + XT ) as boxTotalNumber, 
	sum( W_CONT.zl )  as boxTotalWeight
FROM
	W_voyBOOK 
	INNER JOIN W_BOOK  ON W_BOOK.ID = bookid
	INNER JOIN W_VOYCONT  ON W_voyCONT.voyBOOKID = W_voyBOOK.voybookID
	INNER JOIN W_CONT ON W_CONT.ID = contid 
	inner join w_cqb on w_cqb.id = W_voyBOOK.voyid
WHERE
		w_cqb.zwcm = '新瓯15'
	and w_cqb.hc = '1709S'
GROUP BY
	ZHG,
	XX + XL + XT 
)
UNION
(SELECT
  'XHG' as type,
	XHG as port,
	XX + XL + XT as boxType,
	COUNT ( XX + XL + XT )  as boxTotalNumber,
	SUM ( W_CONT.zl )  as boxTotalWeight
FROM
	W_voyBOOK
	INNER JOIN W_BOOK  ON W_BOOK.ID= bookid
	INNER JOIN W_VOYCONT  ON W_voyCONT.voyBOOKID= W_voyBOOK.voybookID
	INNER JOIN W_CONT ON W_CONT.ID= contid 
	inner join w_cqb on w_cqb.id = W_voyBOOK.voyid
WHERE
	w_cqb.zwcm = '新瓯15'
	and w_cqb.hc = '1709S'
GROUP BY
	XHG,
	XX + XL + XT ) 
	)A
	) B
	GROUP BY type,port
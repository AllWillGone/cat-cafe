-- ============================================
-- 猫咖演示数据 - 合并版（用于本地Docker演示）
-- ============================================
USE cat_cafe;

-- ============================================
-- 用户（8 个，2 管理员 + 6 顾客）
-- ============================================
INSERT INTO user (userName, userPassword, userType, gender, birthday, userPhone, userAvatar) VALUES
('admin', '$2b$12$N8D1XsjNO1/XCojbSnzOqORAdl7gVstGa27DekJaqxuFdjnncC8Yi', 1, 1, '1995-06-15', '13900000000', NULL),
('test', '$2b$12$d5Expa1z9BKDms9meNuGpeGnoQWcCZC8zayN/NTxQypRTzqCtXnFK', 0, 1, '2000-05-20', '13900000001', '/avatars/166316ec77224e6095103e7c82b2e7df.png'),
('落日大桥', '$2b$12$ElSsDJrgjXVyPe4QRhSOze5m/P2U5BMpBpow5flTQyF1r43XYTEeC', 0, 2, '1999-08-15', '13900000003', '/avatars/749c01fe25874d75954d3617dfac5ca3.jpg'),
('陈际宇', '$2b$12$FnnofnLLU7NnLQZHPM8AkOx7JGl5.F/O7Cv.17PgMT0ZU3xEPOY/K', 0, 1, '2001-03-10', '13900000004', NULL),
('大肠杆菌', '$2b$12$1pPcAII8i8kUyJ0Gnb/xI.sqPsBky8mQChryXCJ8azm/j0WMCNzLS', 0, 2, '1998-12-25', '13900000005', '/avatars/36fe2e7933984a17ab0fcf005442f603.jpg'),
('大肠杆君', '$2b$12$/EuzoodB9ypm6672oaa2oepLijycCKrF/0vaw5taXB4knZE008H.a', 0, 1, '2000-07-07', '13900000006', '/avatars/28d7956b7b2341f8a93f96a4146f8c52.png'),
('aaa', '$2b$12$X3x8jDVNbwXj/qACzanAmeYnyivL2fz5fJ2tdOjt1XEw2FiZrZG86', 0, 2, '2002-01-01', '13900000007', NULL),
('🐴', '$2b$12$X3x8jDVNbwXj/qACzanAmeYnyivL2fz5fJ2tdOjt1XEw2FiZrZG86', 0, NULL, NULL, NULL, '/avatars/41048dd37085466f92bce76b10b11d98.jpg');

-- ============================================
-- 猫咪（15 只）
-- ============================================
INSERT INTO catinformation (catName, breed, birthday, status, personality, photoUrl, notes) VALUES
('海猫','暹罗猫','2022-04-12',1,'安静稳重，喜欢在安静的角落观察客人','/cats/haimao.jpg','安静区的常驻猫咪'),
('皮蛋','中华田园猫','2021-11-08',0,'聪明好动，对镜头特别敏感','/cats/pidan.jpg','curious about cameras'),
('小灰','灰猫','2023-02-20',1,'害羞但温柔，适合有耐心的客人','/cats/xiaohui.jpg','best with regulars'),
('雪球','波斯猫','2021-12-05',1,'慵懒柔软，睡眠冠军','/cats/xueqiu.jpg','sleep champion'),
('胖溪','英短','2020-08-30',1,'为食物而生，会做各种小把戏讨零食','/cats/pangxi.jpg','works for treats'),
('团子','布偶猫','2022-03-15',1,'温柔亲人，喜欢被抱，最爱蹭蹭','/cats/tuanzi.jpg','店里人气王，小朋友最爱'),
('拿铁','美短','2023-01-20',1,'精力旺盛，逗猫棒重度爱好者','/cats/latte.jpg','运动健将，每天至少两轮游戏'),
('奶盖','橘猫','2021-08-08',1,'吃货本猫，听见零食袋就飞奔','/cats/naigai.jpg','体重管理中的小胖子'),
('芝麻','黑猫','2022-06-30',0,'高冷神秘，只在安静时主动靠近','/cats/zhima.jpg','夜猫子，白天基本在睡觉'),
('年糕','加菲猫','2023-03-22',1,'呆萌憨厚，反应慢半拍但超级治愈','/cats/niangao.jpg','表情包担当，扁脸上镜'),
('摩卡','暹罗猫','2022-10-10',1,'话痨本痨，喵喵叫个不停','/cats/mocha.jpg','嗓子超大声，进门就听见'),
('包子','英短蓝猫','2021-05-18',1,'圆滚滚的小胖子，走路一扭一扭','/cats/baozi.jpg','摸起来像毛绒玩具'),
('花花','三花猫','2023-07-04',1,'独立又粘人，心情好了才给摸','/cats/mimi_calico.jpg','颜值担当，花纹对称超好看'),
('虎子','橘猫','2020-12-01',0,'猫中大佬，走路带风','/cats/tiger.jpg','店里的老大哥，其他猫都让着'),
('豆腐','布偶猫','2023-09-15',1,'胆小的软妹子，需要温柔对待','/cats/doufu.jpg','蓝眼睛特别好看');

-- ============================================
-- 商品（27 个，3 品类全覆盖）
-- ============================================
INSERT INTO product (productName, category, price, stockQuantity, imageUrl, description, status) VALUES
-- 服务类 (category=0)
('撸猫一小时套餐',0,59.00,996,'/products/7c33b323da3245fa9437fc50ce7b27e6.png','一小时内在所有在岗猫咪互动',1),
('半小时撸猫券',0,25.00,999,'','半小时内在所有在岗猫咪互动',1),
('撸猫+饮品套餐',0,68.00,999,'','一小时撸猫券 + 任选一杯饮品',1),
('工作日撸猫券',0,45.00,500,'','周一至周五专享，一小时撸猫体验',1),
('双人撸猫套餐',0,108.00,300,'','两人同行，各一小时 + 两杯饮品',1),
-- 餐饮类 (category=1)
('可乐',1,3.00,97,'/products/cola.jpg','冰镇可乐',1),
('零糖可乐',1,3.00,82,'/products/cola.jpg','无糖版可乐',1),
('猫屎咖啡',1,1.00,94,'/products/8f7b79187cb54305b849ef60061e3a35.png','招牌咖啡',1),
('猫爪拿铁',1,28.00,50,'/products/catpaw_latte.jpg','可爱猫爪拉花拿铁，打卡必备',1),
('抹茶蛋糕',1,22.00,30,'/products/matcha_cake.jpg','日式抹茶千层，猫咪也馋的甜点',1),
('猫咪马卡龙',1,18.00,25,'/products/macaron.jpg','猫咪造型马卡龙，一盒四枚',1),
('猫薄荷茶',1,15.00,40,'/products/catnip_tea.jpg','人猫共饮薄荷茶，清爽解暑',1),
('冰美式咖啡',1,18.00,60,'/products/iced_americano.jpg','经典冰美式，提神醒脑',1),
('热巧克力',1,20.00,40,'/products/hot_chocolate.jpg','浓郁可可+棉花糖，冬日暖饮',1),
('芝士蛋糕',1,25.00,20,'/products/cheesecake.jpg','纽约风格重芝士，每日限量',1),
('提拉米苏',1,28.00,15,'/products/tiramisu.jpg','意大利经典，咖啡与奶油完美融合',1),
('三明治套餐',1,32.00,25,'/products/sandwich.jpg','火腿芝士三明治+饮品任选',1),
-- 猫咪用品类 (category=2)
('逗猫棒',2,3.00,0,'/products/feather_wand.jpg','经典款逗猫棒（已售罄）',1),
('猫条零食',2,8.00,200,'/products/cat_snack.jpg','进口猫条，互动喂食专用',1),
('猫薄荷玩具鱼',2,15.00,3,'/products/catnip_fish.jpg','内含猫薄荷布艺鱼，抱着不撒手',1),
('猫抓板',2,25.00,0,'/products/scratch_pad.jpg','瓦楞纸猫抓板，保护沙发',0),
('猫窝',2,68.00,12,'/products/cat_bed.jpg','可拆洗毛绒猫窝，柔软舒适',1),
('猫爬架',2,199.00,5,'/products/cat_tree.jpg','三层实木猫爬架，含剑麻柱',1),
('电动老鼠玩具',2,35.00,18,'/products/cat_toy_mouse.jpg','遥控电动老鼠，猫咪疯狂追逐',1),
('猫项圈',2,28.00,30,'/products/cat_collar.jpg','可调节安全项圈，带铃铛',1),
('猫薄荷喷雾',2,22.00,25,'/products/catnip_spray.jpg','猫薄荷提取液，喷玩具上效果翻倍',1);


-- ============================================
-- 订单（用子查询动态查 ID，兼容任何 AUTO_INCREMENT 顺序）
-- ============================================

-- 已完成（状态 3）
INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName, orderNote)
SELECT 'batch_001', u.userId, p.productId, 1, 59.00, 3, 0, '2026-05-18 13:00:00', '2026-05-18 13:02:00', '2026-05-18 14:00:00', u.userPhone, u.userName, '第一次来体验猫咖'
FROM user u, product p WHERE u.userName='test' AND p.productName='撸猫一小时套餐';

INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName, orderNote)
SELECT 'batch_002', u.userId, p.productId, 1, 28.00, 3, 1, '2026-05-19 15:00:00', '2026-05-19 15:05:00', '2026-05-19 15:30:00', u.userPhone, u.userName, '猫爪拿铁超好喝'
FROM user u, product p WHERE u.userName='落日大桥' AND p.productName='猫爪拿铁';

INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName, orderNote)
SELECT 'batch_003', u.userId, p.productId, 1, 59.00, 3, 2, '2026-05-19 16:00:00', '2026-05-19 16:01:00', '2026-05-19 17:00:00', u.userPhone, u.userName, '朋友推荐的'
FROM user u, product p WHERE u.userName='陈际宇' AND p.productName='撸猫一小时套餐';

INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName, orderNote)
SELECT 'batch_004', u.userId, p.productId, 1, 59.00, 3, 0, '2026-05-20 10:00:00', '2026-05-20 10:01:00', '2026-05-20 11:00:00', u.userPhone, u.userName, '周末放松'
FROM user u, product p WHERE u.userName='大肠杆菌' AND p.productName='撸猫一小时套餐';

-- 已支付/待拿取（状态 2）—— 多商品批次 batch_005
INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName, orderNote)
SELECT 'batch_005', u.userId, p.productId, 1, 22.00, 2, 0, '2026-05-22 13:00:00', '2026-05-22 13:01:00', NULL, u.userPhone, u.userName, '要热的'
FROM user u, product p WHERE u.userName='大肠杆君' AND p.productName='抹茶蛋糕';

INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName)
SELECT 'batch_005', u.userId, p.productId, 2, 16.00, 2, 0, '2026-05-22 13:00:00', '2026-05-22 13:01:00', NULL, u.userPhone, u.userName
FROM user u, product p WHERE u.userName='大肠杆君' AND p.productName='猫条零食';

INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName)
SELECT 'batch_006', u.userId, p.productId, 1, 18.00, 2, 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00', NULL, u.userPhone, u.userName
FROM user u, product p WHERE u.userName='落日大桥' AND p.productName='猫咪马卡龙';

-- 已支付（状态 1）—— 多商品批次 batch_007
INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName, orderNote)
SELECT 'batch_007', u.userId, p.productId, 1, 59.00, 1, 0, '2026-05-20 11:00:00', '2026-05-20 11:01:00', NULL, u.userPhone, u.userName, '犒劳自己'
FROM user u, product p WHERE u.userName='test' AND p.productName='撸猫一小时套餐';

INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName)
SELECT 'batch_007', u.userId, p.productId, 1, 3.00, 1, 0, '2026-05-20 11:00:00', '2026-05-20 11:01:00', NULL, u.userPhone, u.userName
FROM user u, product p WHERE u.userName='test' AND p.productName='可乐';

INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName)
SELECT 'batch_007', u.userId, p.productId, 2, 16.00, 1, 0, '2026-05-20 11:00:00', '2026-05-20 11:01:00', NULL, u.userPhone, u.userName
FROM user u, product p WHERE u.userName='test' AND p.productName='猫条零食';

-- 已取消（状态 4）
INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName, orderNote)
SELECT 'batch_008', u.userId, p.productId, 1, 25.00, 4, NULL, '2026-05-23 18:00:00', NULL, NULL, u.userPhone, u.userName, '临时有事去不了'
FROM user u, product p WHERE u.userName='陈际宇' AND p.productName='三明治套餐';

INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName, orderNote)
SELECT 'batch_009', u.userId, p.productId, 1, 59.00, 4, NULL, '2026-05-20 17:00:00', NULL, NULL, u.userPhone, u.userName, '时间选错了'
FROM user u, product p WHERE u.userName='aaa' AND p.productName='撸猫一小时套餐';

INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName, orderNote)
SELECT 'batch_010', u.userId, p.productId, 1, 199.00, 4, NULL, '2026-05-24 09:00:00', NULL, NULL, u.userPhone, u.userName, '再看看别的'
FROM user u, product p WHERE u.userName='大肠杆菌' AND p.productName='猫爬架';

-- 未支付（状态 0）
INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName)
SELECT 'batch_011', u.userId, p.productId, 2, 36.00, 0, NULL, '2026-05-24 08:00:00', NULL, NULL, u.userPhone, u.userName
FROM user u, product p WHERE u.userName='落日大桥' AND p.productName='猫咪马卡龙';

INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName)
SELECT 'batch_012', u.userId, p.productId, 1, 32.00, 0, NULL, '2026-05-24 09:30:00', NULL, NULL, u.userPhone, u.userName
FROM user u, product p WHERE u.userName='大肠杆菌' AND p.productName='三明治套餐';

INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName)
SELECT 'batch_013', u.userId, p.productId, 1, 22.00, 0, NULL, '2026-05-24 10:00:00', NULL, NULL, u.userPhone, u.userName
FROM user u, product p WHERE u.userName='大肠杆君' AND p.productName='猫薄荷喷雾';

INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName, orderNote)
SELECT 'batch_014', u.userId, p.productId, 1, 68.00, 0, NULL, '2026-05-24 13:00:00', NULL, NULL, u.userPhone, u.userName, '还在考虑要不要买'
FROM user u, product p WHERE u.userName='test' AND p.productName='猫窝';

INSERT INTO orders (batchNo, userId, productId, productQuantity, totalAmount, orderStatus, paymentMethod, orderTime, paymentTime, completionTime, userPhone, userName)
SELECT 'batch_014', u.userId, p.productId, 1, 28.00, 0, NULL, '2026-05-24 13:00:00', NULL, NULL, u.userPhone, u.userName
FROM user u, product p WHERE u.userName='test' AND p.productName='猫项圈';

-- ============================================
-- 评论（含待审核，演示审核功能）
-- ============================================
INSERT INTO comment (targetType, targetId, userId, content, publishTime, auditStatus) VALUES
-- 已通过的猫咪评论
(1,1,1,'海猫真的太乖了，安静又温柔，看书特别惬意','2026-05-18 10:00:00',1),
(1,1,4,'暹罗猫就是聪明，海猫会主动蹭过来求摸摸','2026-05-19 14:00:00',1),
(1,3,5,'小灰毛色真的好看，熟悉后会主动靠近你','2026-05-20 09:00:00',1),
(1,5,3,'胖溪太逗了，为了一口吃的什么都能做','2026-05-20 11:00:00',1),
(1,6,1,'团子软软的像一颗棉花糖，抱在怀里暖暖的','2026-05-20 13:00:00',1),
(1,6,4,'团子人气最高不是没道理的，真的太治愈了','2026-05-22 11:00:00',1),
(1,7,5,'拿铁精力太旺盛了，陪玩一小时我自己先累了','2026-05-21 15:00:00',1),
(1,7,3,'美短的颜值真的高，拿铁的花纹特别漂亮','2026-05-22 09:00:00',1),
(1,8,6,'奶盖就是个吃货，听到零食袋子声立马飞奔','2026-05-20 16:00:00',1),
(1,9,4,'芝麻虽然高冷但黑猫的颜值绝了，神秘又优雅','2026-05-22 13:00:00',1),
(1,10,1,'年糕的扁脸真的太搞笑了，永远一副震惊表情','2026-05-23 14:00:00',1),
(1,11,7,'摩卡是个小话痨，进门就开始喵喵叫','2026-05-23 10:00:00',1),
(1,12,3,'包子摸起来就是一颗毛茸茸的球','2026-05-23 16:00:00',1),
-- 已通过的商品评论
(0,1,1,'撸猫一小时套餐性价比很高，59块能玩这么多猫','2026-05-18 11:00:00',1),
(0,1,6,'第一次来猫咖，体验超棒，猫咪们都好可爱！','2026-05-19 10:00:00',1),
(0,6,3,'可乐才3块钱，比外面超市还便宜','2026-05-20 09:00:00',1),
(0,9,4,'猫爪拿铁颜值太高了，先拍照再喝是标准流程','2026-05-22 16:00:00',1),
(0,9,1,'拉花师傅手艺不错，每次猫爪形状都不一样','2026-05-23 11:00:00',1),
(0,10,6,'抹茶蛋糕不甜不腻，配上猫薄荷茶正好','2026-05-22 10:00:00',1),
(0,15,5,'芝士蛋糕份量很足，每日限量要早点来','2026-05-23 15:00:00',1),
(0,18,3,'逗猫棒质量还行，猫咪们都玩疯了','2026-05-21 16:00:00',1),
-- 待审核（auditStatus=0，用于演示审核）
(0,8,7,'猫屎咖啡名字怪但味道意外不错，推荐试试','2026-05-24 10:00:00',0),
(1,9,4,'芝麻真的好高冷，等了两个小时才让摸一下','2026-05-24 09:30:00',0),
(1,6,5,'团子今天好像不太舒服的样子，希望没事','2026-05-24 10:30:00',0),
(0,13,1,'冰美式可以做少冰吗？今天这杯冰有点多','2026-05-24 11:30:00',0),
(0,14,7,'热巧克力有点太甜了，希望能出半糖版','2026-05-24 12:00:00',0),
(1,15,3,'豆腐太胆小了，一直躲在角落不出来','2026-05-24 12:30:00',0),
-- 已拒绝
(1,5,6,'ABC','2026-05-23 08:00:00',2);

-- ============================================
-- 点赞（覆盖猫咪、商品、评论三类型）
-- ============================================
INSERT INTO likes (likeType, objectId, userId) VALUES
-- 猫咪点赞 (likeType=2)
(2,6,1),(2,6,4),(2,6,5),
(2,7,1),(2,7,3),(2,7,6),
(2,8,3),(2,8,4),(2,8,7),
(2,9,1),(2,9,5),
(2,10,1),(2,10,4),(2,10,6),
(2,11,3),(2,11,7),
(2,12,1),(2,12,5),
(2,13,6),(2,13,4),
(2,14,3),(2,14,7),
(2,15,1),(2,15,5),
-- 商品点赞 (likeType=0)
(0,1,1),(0,1,3),(0,1,5),
(0,6,4),(0,6,7),
(0,9,3),(0,9,6),
(0,10,5),(0,10,7),
(0,15,1),(0,15,4),
(0,21,6),(0,22,3),(0,22,5),
(0,26,1),(0,26,7),
-- 评论点赞 (likeType=1)
(1,3,4),(1,5,6),(1,7,3),(1,10,1),
(1,14,5),(1,16,7),(1,18,3),(1,20,4);

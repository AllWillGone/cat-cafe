-- ============================================
-- 猫咖点单系统 - 测试数据
-- ============================================

USE cat_cafe;

-- ============================================
-- 1. 用户（密码统一为用户名+123，如 admin123）
-- ============================================
INSERT INTO `user` (`userName`, `userPassword`, `userType`, `gender`, `birthday`, `userPhone`, `userAvatar`) VALUES
('admin',     '$2b$12$7rBxybCAUXIEvPXVv6bD9OdZHCrBdvRh.luK/bTOLKqSk/3OuTMuG', 1, 1, '1998-03-15', '13800000001', '/avatars/admin.png'),
('testuser',  '$2b$12$mF6FY6wPsa7mYxMEVuEt1O2ciO0xSpiFLOWGBU15jfKClo9qYQYEi', 0, 2, '2000-07-22', '13800000002', '/avatars/default.png'),
('xiaoming',  '$2b$12$0ssvI.W1Qrjpq9rYJxFwAuvzYZLs/PcWGs5zbOvlpGz3ovsTvM1li', 0, 1, '2002-11-08', '13800000003', '/avatars/default.png'),
('xiaohong',  '$2b$12$NjK/8xbaV1/r22R04.ZDqOWCtvpez.i/Qk6qaiBFgEHK8cdKCVhum', 0, 2, '2001-05-18', '13800000004', '/avatars/default.png'),
('catlover',  '$2b$12$Pflh1V29C4Fkq3Vcxj5AcuzFb8qmH3XKSQJf.EYMfJ5DAhdTBDEcC', 0, NULL, NULL,       '13800000005', '/avatars/default.png');

-- ============================================
-- 2. 猫咪
-- ============================================
INSERT INTO `catinformation` (`catName`, `breed`, `birthday`, `status`, `personality`, `photoUrl`, `notes`) VALUES
('咪咪', '英短蓝猫',   '2022-06-01', 1, '温顺粘人，喜欢被撸下巴，尤其爱晒太阳。对小朋友特别友好，是店里的人气王。',       '/cats/mimi.jpg',     '定期打疫苗，已绝育'),
('豆豆', '布偶猫',     '2023-02-14', 1, '颜值担当，性格温柔顺从，抱起来像一团棉花糖。偶尔会撒娇要零食。',               '/cats/doudou.jpg',   '需要每日梳毛'),
('花花', '波斯猫',     '2021-09-20', 1, '高贵冷艳，但熟悉后非常亲人。喜欢安静的角落，讨厌噪音。',                       '/cats/huahua.jpg',   '最近换毛期，需多梳毛'),
('球球', '苏格兰折耳', '2023-08-08', 0, '呆萌可爱，喜欢玩球和逗猫棒。因天生折耳基因，需要特别注意关节健康。',             '/cats/qiuqiu.jpg',   '今日休息：轻微感冒，观察中'),
('小橘', '中华田园猫', '2024-01-15', 1, '超级吃货，看见食物就精神。精力旺盛，喜欢满店跑酷。',                           '/cats/xiaoju.jpg',   '注意控制饮食，偏胖'),
('奶茶', '暹罗猫',     '2022-12-03', 1, '话痨一只，喜欢跟客人聊天喵喵叫。智商高，能听懂简单的指令。',                   '/cats/naicha.jpg',   '好奇心重，注意关门');

-- ============================================
-- 3. 商品
-- ============================================
INSERT INTO `product` (`productName`, `category`, `price`, `stockQuantity`, `imageUrl`, `description`, `status`) VALUES
-- 服务类 category=0
('撸猫30分钟',  0, 38.00,  999, '/products/service_30min.png',  '与店里猫咪亲密互动30分钟，含免费饮品一杯',    1),
('撸猫1小时',   0, 58.00,  999, '/products/service_1h.png',     '与店里猫咪亲密互动60分钟，含饮品及小零食一份', 1),
-- 餐饮类 category=1
('猫爪拿铁',    1, 28.00,  100, '/products/latte.png',          '店内招牌，拿铁表面有可爱猫爪拉花',              1),
('美式咖啡',    1, 22.00,  100, '/products/americano.png',      '精选阿拉比卡豆，香醇浓郁',                      1),
('抹茶星冰乐',  1, 32.00,  100, '/products/matcha_frap.png',    '日式抹茶搭配鲜奶油，夏日人气饮品',              1),
('芝士蛋糕',    1, 35.00,   50, '/products/cheesecake.png',     '猫爪造型纽约芝士蛋糕，每日限量',                1),
-- 猫咪用品类 category=2
('逗猫棒',      2, 15.00,  200, '/products/cat_wand.png',       '羽毛逗猫棒，猫咪的最爱',                       1),
('猫罐头-金枪鱼', 2, 25.00, 80, '/products/canned_food.png',    '深海金枪鱼猫罐头，170g装',                     1),
('猫咪小零食',  2, 12.00,  150, '/products/cat_snack.png',      '鸡肉味猫咪小零食，100g装',                     1);

-- ============================================
-- 4. 订单（testuser 下两个批次）
-- ============================================
-- 批次1: 猫爪拿铁 x2 + 芝士蛋糕 x1
SET @batch1 = CONCAT('BATCH', UNIX_TIMESTAMP(), '001');
INSERT INTO `orders` (`batchNo`, `userId`, `productId`, `productQuantity`, `totalAmount`, `orderStatus`, `paymentMethod`, `paymentTime`, `userPhone`, `userName`, `orderNote`) VALUES
(@batch1, 2, 3, 2, 56.00, 3, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), '13800000002', 'testuser', '少糖'),
(@batch1, 2, 6, 1, 35.00, 3, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), '13800000002', 'testuser', '少糖');

UPDATE `orders` SET `completionTime` = DATE_SUB(NOW(), INTERVAL 1 DAY), `orderTime` = DATE_SUB(NOW(), INTERVAL 2 DAY)
WHERE `batchNo` = @batch1;

-- 批次2: 撸猫1小时 + 猫咪小零食 x2（未支付）
SET @batch2 = CONCAT('BATCH', UNIX_TIMESTAMP(), '002');
INSERT INTO `orders` (`batchNo`, `userId`, `productId`, `productQuantity`, `totalAmount`, `orderStatus`, `userPhone`, `userName`, `orderNote`) VALUES
(@batch2, 2, 2, 1, 58.00, 0, '13800000002', 'testuser', '预约周末下午'),
(@batch2, 2, 9, 2, 24.00, 0, '13800000002', 'testuser', '预约周末下午');

-- 单商品订单（xiaoming，已支付）
INSERT INTO `orders` (`batchNo`, `userId`, `productId`, `productQuantity`, `totalAmount`, `orderStatus`, `paymentMethod`, `paymentTime`, `userPhone`, `userName`, `orderNote`) VALUES
(NULL, 3, 4, 1, 22.00, 1, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), '13800000003', 'xiaoming', '加冰');

-- ============================================
-- 5. 评论（审核通过）
-- ============================================
INSERT INTO `comment` (`targetType`, `targetId`, `userId`, `content`, `auditStatus`, `publishTime`) VALUES
-- 商品评论 targetType=0
(0, 3, 2, '猫爪拿铁颜值太高了！拉花超可爱，味道也很棒，每次来必点。',        1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(0, 3, 5, '拿铁的猫爪拉花确实一绝，咖啡豆品质也不错，推荐！',                 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(0, 6, 2, '芝士蛋糕很好吃，入口即化，猫爪造型太适合拍照了。',                 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- 猫咪评论 targetType=1
(1, 1, 2, '咪咪太乖啦！一见面就蹭我的手要摸摸，心都化了。',                   1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 1, 3, '咪咪真的是店里的明星猫，性格超好，下次还来看它。',                 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 2, 4, '布偶猫真的像棉花糖一样软，抱起来手感太好了，颜值也超高！',          1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
-- 待审核评论
(1, 5, 5, '小橘也太能吃了，逗猫棒都不管用，只有零食能召唤它。',               0, NOW());

-- ============================================
-- 6. 点赞
-- ============================================
INSERT INTO `likes` (`likeType`, `objectId`, `userId`, `linkUrl`, `createTime`) VALUES
-- 商品点赞 likeType=0
(0, 3, 2, '/product/3',  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(0, 3, 4, '/product/3',  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(0, 3, 5, '/product/3',  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(0, 6, 3, '/product/6',  DATE_SUB(NOW(), INTERVAL 2 DAY)),
-- 评论点赞 likeType=1
(1, 1, 3, '/comment/1',  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 1, 4, '/comment/1',  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 3, 5, '/comment/3',  NOW()),
-- 猫咪点赞 likeType=2
(2, 1, 2, '/cat/1',      DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 1, 4, '/cat/1',      DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 2, 5, '/cat/2',      DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 5, 3, '/cat/5',      DATE_SUB(NOW(), INTERVAL 1 DAY));

mysqldump: [Warning] Using a password on the command line interface can be insecure.
-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: cat_cafe
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `catinformation`
--

DROP TABLE IF EXISTS `catinformation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `catinformation` (
  `catId` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '猫咪ID',
  `catName` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '猫咪名字',
  `breed` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品种',
  `birthday` date NOT NULL COMMENT '生日',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态: 0=休息中, 1=在岗中',
  `personality` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '性格描述',
  `photoUrl` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '照片URL',
  `notes` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '备注',
  PRIMARY KEY (`catId`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='猫咪信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `catinformation`
--

LOCK TABLES `catinformation` WRITE;
/*!40000 ALTER TABLE `catinformation` DISABLE KEYS */;
INSERT INTO `catinformation` VALUES (1,'海猫','暹罗猫','2022-04-12',1,'安静稳重','/cats/haimao.jpg','它的脸会慢慢变黑'),(2,'皮蛋','中华田园猫','2021-11-08',0,'聪明好动','/cats/pidan.jpg','喜欢逗猫棒，调皮的小猫'),(3,'小灰','灰猫','2023-02-20',1,'害羞','/cats/xiaohui.jpg','best with regulars'),(4,'雪球','白猫','2021-12-05',1,'lazy and soft','/cats/xueqiu.jpg','sleep champion'),(5,'胖溪','英短','2020-08-30',1,'性格温顺，时而调皮','/cats/pangxi.jpg','works for treats'),(7,'团子','布偶猫','2022-03-15',1,'温柔亲人，喜欢被抱，最爱蹭蹭','/cats/tuanzi.jpg','店里的人气王，小朋友最爱'),(8,'拿铁','美短','2023-01-20',1,'精力旺盛，喜欢逗猫棒，一玩就停不下来','/cats/latte.jpg','运动健将，每天需要至少两轮游戏'),(9,'奶盖','橘猫','2021-08-08',1,'吃货本猫，看见零食就飞奔而来','/cats/naigai.jpg','体重管理中的小胖子'),(10,'芝麻','黑猫','2022-06-30',0,'高冷神秘，只在安静的时候主动靠近人','/cats/zhima.jpg','夜猫子，白天基本在睡觉'),(11,'年糕','加菲猫','2023-03-22',1,'呆萌憨厚，反应慢半拍，但超级治愈','/cats/niangao.jpg','表情包担当，扁脸特别上镜'),(12,'摩卡','暹罗猫','2022-10-10',1,'话痨本痨，喵喵叫个不停，爱和人聊天','/cats/mocha.jpg','嗓子超大声，进门就听见'),(13,'包子','黑猫','2021-05-18',0,'圆滚滚的小胖子，走路一扭一扭','/cats/baozi.jpg','摸起来像毛绒玩具'),(14,'花花','三花猫','2023-07-04',1,'独立又粘人，心情好了才给摸','/cats/mimi_calico.jpg','颜值担当，花纹对称超好看'),(15,'虎子','橘猫','2020-12-01',1,'猫中大佬，走路带风，其他猫都让着他','/cats/tiger.jpg','店里的老大哥'),(16,'豆腐','布偶猫','2023-09-15',1,'胆小的软妹子，需要温柔对待','/cats/doufu.jpg','蓝眼睛特别好看'),(18,'123','123','2026-05-05',1,'123','/cats/6c8bd528d612409497b45eb509d4e72f.png','123');
/*!40000 ALTER TABLE `catinformation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `commentId` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `targetType` tinyint unsigned NOT NULL COMMENT '评论对象: 0=商品, 1=猫咪',
  `targetId` bigint unsigned NOT NULL COMMENT '目标ID(商品或猫咪的ID)',
  `userId` bigint unsigned NOT NULL COMMENT '用户ID',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `publishTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `auditStatus` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '审核状态: 0=待审核, 1=已通过, 2=已拒绝',
  PRIMARY KEY (`commentId`),
  KEY `idx_target` (`targetType`,`targetId`),
  KEY `idx_userId` (`userId`),
  KEY `idx_auditStatus` (`auditStatus`),
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES (1,0,3,1,'太贵了','2026-05-21 11:19:17',1),(2,1,5,1,'太胖了，嘿嘿','2026-05-21 13:03:09',1),(3,1,5,1,'cjy','2026-05-21 13:05:23',1),(4,1,5,5,'猫','2026-05-21 15:01:26',1),(5,0,4,5,'审核太慢了','2026-05-21 15:16:32',1),(6,1,5,5,'ABC','2026-05-22 15:39:52',2),(7,0,3,3,'记得加冰','2026-05-22 16:30:49',1),(8,1,5,6,'我认识你','2026-05-23 02:45:44',1),(9,1,5,6,'你麻麻知道你干这种活吗','2026-05-23 02:45:57',1),(10,1,5,6,'这就是你说的城里体面工作吗','2026-05-23 02:46:11',1),(11,1,5,3,'保佑我得高分好吗','2026-05-23 09:06:10',1),(12,1,5,3,'你好','2026-05-23 15:06:19',2),(13,1,5,7,'好猫','2026-05-24 08:25:13',1),(14,0,1,3,'体验非常好！海猫特别乖，一个小时过得超快，下次还会来','2026-05-20 14:30:00',1),(15,0,6,4,'猫爪拿铁颜值太高了！拉花很精致，味道也不错','2026-05-21 16:20:00',1),(16,0,1,5,'撸猫券太值了，猫咪们都好可爱，治愈了我的社畜心灵','2026-05-22 10:15:00',1),(17,0,8,7,'抹茶蛋糕配猫薄荷茶简直是绝配！环境也很舒适','2026-05-22 11:00:00',1),(18,1,6,4,'团子真的太乖了！一直趴在我腿上，完全不想走','2026-05-20 17:00:00',1),(19,1,7,3,'拿铁精力太旺盛了哈哈哈，陪他玩了半小时逗猫棒','2026-05-21 19:00:00',1),(20,1,8,5,'奶盖太搞笑了，为了吃的不择手段，直接翻我包','2026-05-22 14:00:00',1),(21,1,10,6,'年糕的扁脸真的太可爱了，拍了好多表情包','2026-05-22 15:30:00',1),(22,1,3,6,'小灰虽然害羞但熟悉了以后超级粘人，反差萌','2026-05-23 10:00:00',1),(23,0,5,3,'逗猫棒质量一般，用了一次羽毛就掉了，希望能改进','2026-05-24 09:00:00',1),(24,1,9,4,'芝麻好高冷哈哈哈，我等了一个小时它才过来让我摸了一下','2026-05-24 09:30:00',1),(25,0,4,7,'猫屎咖啡名字怪怪的但味道意外的还不错，推荐试试','2026-05-24 10:00:00',1),(26,1,6,5,'团子今天好像不太舒服的样子，店员说只是困了，希望照顾好猫咪们','2026-05-24 10:30:00',1),(27,1,1,3,'海猫真的太乖了，安静又温柔，坐在旁边看书特别惬意','2026-05-18 10:00:00',1),(28,1,1,5,'暹罗猫就是聪明，海猫会主动蹭过来求摸摸！','2026-05-19 14:00:00',1),(29,1,3,4,'小灰害羞的样子太可爱了，熟悉后就会来蹭你','2026-05-19 16:00:00',1),(30,1,3,6,'小灰毛色真的好看，灰色的毛发光泽特别好','2026-05-20 09:00:00',1),(31,1,4,3,'雪球名副其实，白色长毛像一团雪球','2026-05-18 15:00:00',1),(32,1,5,4,'胖溪太逗了，为了一口吃的什么都能做','2026-05-20 11:00:00',1),(33,1,5,3,'胖溪虽然胖但身手还挺灵活的，逗猫棒玩得飞起','2026-05-21 10:00:00',1),(34,1,7,1,'团子软软的像一只棉花糖，抱在怀里暖暖的','2026-05-20 13:00:00',1),(35,1,7,5,'团子人气最高不是没道理的，真的太治愈了！','2026-05-22 11:00:00',1),(36,1,8,6,'拿铁精力太旺盛了哈哈哈，陪玩一小时我自己先累了','2026-05-21 15:00:00',1),(37,1,8,3,'美短的颜值真的高，拿铁的花纹特别漂亮','2026-05-22 09:00:00',1),(38,1,9,4,'奶盖就是个吃货，看到零食袋子的声音立马飞奔','2026-05-20 16:00:00',1),(39,1,10,5,'芝麻虽然高冷但是黑猫的颜值真的绝了，神秘又优雅','2026-05-22 13:00:00',1),(40,1,11,1,'年糕的扁脸真的太搞笑了，永远一副震惊表情','2026-05-23 14:00:00',1),(41,1,12,7,'摩卡是个小话痨，我一进门就开始喵喵叫，太可爱了','2026-05-23 10:00:00',1),(42,1,13,3,'包子摸起来就是一颗毛茸茸的球，英短的毛质真的绝','2026-05-23 16:00:00',1),(43,0,1,1,'一小时撸猫套餐性价比很高，59块钱能和这么多猫咪玩','2026-05-18 11:00:00',1),(44,0,1,6,'第一次来猫咖，体验超棒，猫咪们都好可爱！','2026-05-19 10:00:00',1),(45,0,1,7,'推荐撸猫套餐！一小时刚刚好，时间太短不过瘾太长猫咪也累','2026-05-22 14:00:00',1),(46,0,2,3,'可乐才3块钱，比外面超市还便宜，良心价','2026-05-20 09:00:00',1),(47,0,4,5,'猫屎咖啡名字有点搞笑但味道不错，杯子也很可爱','2026-05-21 14:00:00',1),(48,0,8,4,'猫爪拿铁颜值太高了，先拍照再喝是标准流程','2026-05-22 16:00:00',1),(49,0,8,1,'拉花师傅手艺不错，每次来猫爪形状都不一样','2026-05-23 11:00:00',1),(50,0,9,6,'抹茶蛋糕不甜不腻，配上猫薄荷茶正好','2026-05-22 10:00:00',1),(51,0,13,5,'芝士蛋糕份量很足，口感细腻，要早点来不然卖完了','2026-05-23 15:00:00',1),(52,0,17,3,'逗猫棒质量不错，猫咪们都玩疯了','2026-05-21 16:00:00',1),(53,0,20,6,'猫爬架做工不错，放家里猫咪很喜欢','2026-05-23 09:00:00',1),(54,0,23,4,'猫薄荷喷雾效果明显，喷在玩具上猫咪玩了好久','2026-05-24 08:00:00',1),(55,0,5,3,'逗猫棒质量一般，用了一次羽毛就松了','2026-05-24 09:00:00',1),(56,1,10,4,'芝麻真的好高冷，等了俩小时都不理我哈哈','2026-05-24 09:30:00',1),(57,0,4,7,'猫屎咖啡味道意外的还不错，推荐试试看','2026-05-24 10:00:00',1),(58,1,7,5,'团子今天好像不太舒服，店员说只是困了','2026-05-24 10:30:00',1),(59,1,14,6,'花花今天心情不好，摸一下就走了','2026-05-24 11:00:00',1),(60,0,16,1,'冰美式可以做少冰吗？今天这杯冰有点多','2026-05-24 11:30:00',1),(62,1,15,3,'豆腐太胆小了，一直躲在角落不出来','2026-05-24 12:30:00',1),(63,1,5,3,'这猫这么可爱，她的主人一定很善良吧','2026-05-25 05:41:14',1);
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `likes`
--

DROP TABLE IF EXISTS `likes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `likes` (
  `likeId` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `likeType` tinyint unsigned NOT NULL COMMENT '点赞类型: 0=商品, 1=评论, 2=猫咪',
  `objectId` bigint unsigned NOT NULL COMMENT '被点赞对象ID',
  `userId` bigint unsigned NOT NULL COMMENT '用户ID',
  `linkUrl` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '跳转链接(查看点赞时跳转到对应物品)',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`likeId`),
  UNIQUE KEY `uk_user_like` (`userId`,`likeType`,`objectId`),
  KEY `idx_likeType_object` (`likeType`,`objectId`),
  CONSTRAINT `fk_likes_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `likes`
--

LOCK TABLES `likes` WRITE;
/*!40000 ALTER TABLE `likes` DISABLE KEYS */;
INSERT INTO `likes` VALUES (1,0,1,1,NULL,'2026-05-21 11:18:51'),(2,0,2,1,NULL,'2026-05-21 11:19:03'),(3,0,3,1,NULL,'2026-05-21 11:19:03'),(4,2,5,3,NULL,'2026-05-21 11:37:26'),(9,0,4,4,NULL,'2026-05-21 15:21:48'),(10,0,1,4,NULL,'2026-05-21 15:21:54'),(11,2,4,5,NULL,'2026-05-22 15:38:23'),(12,2,3,5,NULL,'2026-05-22 15:38:25'),(13,2,1,5,NULL,'2026-05-22 15:38:28'),(14,0,4,5,NULL,'2026-05-22 15:40:43'),(15,0,3,5,NULL,'2026-05-22 15:40:44'),(16,0,4,3,NULL,'2026-05-22 16:29:03'),(17,0,3,3,NULL,'2026-05-22 16:29:04'),(18,1,1,3,NULL,'2026-05-22 16:30:41'),(21,2,5,6,NULL,'2026-05-23 02:45:31'),(22,1,4,6,NULL,'2026-05-23 02:46:13'),(23,1,3,6,NULL,'2026-05-23 02:46:14'),(24,0,5,6,NULL,'2026-05-23 02:47:13'),(25,1,9,6,NULL,'2026-05-23 02:51:45'),(26,1,10,6,NULL,'2026-05-23 02:51:46'),(27,2,1,3,NULL,'2026-05-23 09:05:21'),(28,2,3,3,NULL,'2026-05-23 09:05:24'),(31,2,2,7,NULL,'2026-05-24 08:24:16'),(32,2,1,7,NULL,'2026-05-24 08:24:22'),(33,1,10,5,NULL,'2026-05-24 10:05:53'),(34,2,6,1,NULL,'2026-05-20 10:00:00'),(35,2,7,1,NULL,'2026-05-21 10:00:00'),(36,2,8,1,NULL,'2026-05-22 10:00:00'),(37,2,9,3,NULL,'2026-05-23 10:00:00'),(38,2,10,3,NULL,'2026-05-24 10:00:00'),(39,2,6,5,NULL,'2026-05-20 11:00:00'),(40,2,8,4,NULL,'2026-05-21 11:00:00'),(41,0,6,5,NULL,'2026-05-22 12:00:00'),(42,0,7,6,NULL,'2026-05-22 13:00:00'),(43,0,8,4,NULL,'2026-05-23 14:00:00'),(44,0,9,3,NULL,'2026-05-23 15:00:00'),(45,1,3,3,NULL,'2026-05-22 16:00:00'),(46,1,5,6,NULL,'2026-05-23 17:00:00'),(47,1,8,5,NULL,'2026-05-24 12:00:00'),(48,2,7,4,NULL,'2026-05-24 10:40:11'),(49,2,7,6,NULL,'2026-05-24 10:40:11'),(50,2,8,5,NULL,'2026-05-24 10:40:11'),(52,2,9,4,NULL,'2026-05-24 10:40:11'),(53,2,9,6,NULL,'2026-05-24 10:40:11'),(54,2,10,1,NULL,'2026-05-24 10:40:11'),(55,2,11,1,NULL,'2026-05-24 10:40:11'),(56,2,11,5,NULL,'2026-05-24 10:40:11'),(57,2,11,6,NULL,'2026-05-24 10:40:11'),(58,2,12,3,NULL,'2026-05-24 10:40:11'),(59,2,12,4,NULL,'2026-05-24 10:40:11'),(60,2,13,1,NULL,'2026-05-24 10:40:11'),(61,2,13,5,NULL,'2026-05-24 10:40:11'),(62,2,14,6,NULL,'2026-05-24 10:40:11'),(63,2,14,7,NULL,'2026-05-24 10:40:11'),(64,2,15,3,NULL,'2026-05-24 10:40:11'),(65,2,15,4,NULL,'2026-05-24 10:40:11'),(66,0,6,1,NULL,'2026-05-24 10:40:11'),(67,0,7,3,NULL,'2026-05-24 10:40:11'),(68,0,8,5,NULL,'2026-05-24 10:40:11'),(69,0,9,6,NULL,'2026-05-24 10:40:11'),(70,0,10,4,NULL,'2026-05-24 10:40:11'),(71,0,12,1,NULL,'2026-05-24 10:40:12'),(72,0,14,5,NULL,'2026-05-24 10:40:12'),(73,0,15,7,NULL,'2026-05-24 10:40:12'),(74,0,17,3,NULL,'2026-05-24 10:40:12'),(75,0,18,6,NULL,'2026-05-24 10:40:12'),(76,0,20,4,NULL,'2026-05-24 10:40:12'),(77,0,22,1,NULL,'2026-05-24 10:40:12'),(78,0,23,5,NULL,'2026-05-24 10:40:12'),(79,1,6,4,NULL,'2026-05-24 10:40:12'),(80,1,10,3,NULL,'2026-05-24 10:40:12'),(81,1,13,1,NULL,'2026-05-24 10:40:12'),(82,1,14,5,NULL,'2026-05-24 10:40:12'),(83,1,15,6,NULL,'2026-05-24 10:40:12'),(84,1,17,7,NULL,'2026-05-24 10:40:12'),(85,1,20,4,NULL,'2026-05-24 10:40:12'),(86,1,21,3,NULL,'2026-05-24 10:40:12'),(87,1,22,1,NULL,'2026-05-24 10:40:12'),(88,0,20,7,NULL,'2026-05-24 10:46:42'),(90,1,10,7,NULL,'2026-05-24 10:53:12'),(91,2,5,5,NULL,'2026-05-24 11:03:01'),(92,1,9,5,NULL,'2026-05-24 11:03:04'),(93,2,5,7,NULL,'2026-05-24 11:35:10'),(96,0,20,3,NULL,'2026-05-25 05:16:42'),(98,2,5,14,NULL,'2026-05-25 05:45:41'),(99,2,18,14,NULL,'2026-05-25 05:46:00');
/*!40000 ALTER TABLE `likes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `orderId` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `batchNo` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '批次号，同一批下单的多条记录共用',
  `userId` bigint unsigned NOT NULL COMMENT '用户ID',
  `productId` bigint unsigned NOT NULL COMMENT '商品ID',
  `productQuantity` int unsigned NOT NULL DEFAULT '1' COMMENT '商品数量',
  `totalAmount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `orderStatus` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '订单状态: 0=未支付, 1=已支付, 2=待拿取, 3=已完成, 4=已取消',
  `paymentMethod` tinyint unsigned DEFAULT NULL COMMENT '支付方式: 0=微信, 1=支付宝, 2=现金',
  `orderTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `paymentTime` datetime DEFAULT NULL COMMENT '支付时间',
  `completionTime` datetime DEFAULT NULL COMMENT '完成时间',
  `userPhone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '下单时手机号(快照)',
  `userName` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '下单时用户名(快照)',
  `orderNote` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '订单备注',
  PRIMARY KEY (`orderId`),
  KEY `idx_batchNo` (`batchNo`),
  KEY `idx_userId` (`userId`),
  KEY `idx_productId` (`productId`),
  KEY `idx_orderStatus` (`orderStatus`),
  KEY `idx_orderTime` (`orderTime`),
  CONSTRAINT `fk_orders_product` FOREIGN KEY (`productId`) REFERENCES `product` (`productId`) ON DELETE RESTRICT,
  CONSTRAINT `fk_orders_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,'20ef11a09e024328',1,3,1,13.00,1,0,'2026-05-21 11:23:43','2026-05-21 11:25:20',NULL,'111','test',NULL),(2,'88d4dcb5447d4dd6',1,3,1,3.00,0,0,'2026-05-21 12:29:17',NULL,NULL,'没有手机号怎么登录的','test',''),(3,'88d4dcb5447d4dd6',1,2,1,3.00,0,0,'2026-05-21 12:29:17',NULL,NULL,'没有手机号怎么登录的','test',''),(4,'df8cc4675d9a484e',3,3,1,3.00,0,0,'2026-05-21 13:09:05',NULL,NULL,'18616313186','落日大桥',''),(5,'4d7f134ab3ca4949',2,4,1,1.00,1,0,'2026-05-21 14:54:41','2026-05-21 14:55:01',NULL,'000000','admin',NULL),(6,'c87062759b00470a',5,3,1,3.00,0,0,'2026-05-21 15:01:35',NULL,NULL,'18994747539','大肠杆菌',''),(7,'44fd0e864cd54c15',5,3,1,3.00,0,0,'2026-05-21 15:05:55',NULL,NULL,'18994747539','大肠杆菌',''),(8,'74a75493c7ef4c6b',1,4,1,1.00,0,NULL,'2026-05-22 14:39:54',NULL,NULL,'没有手机号怎么登录的','test',NULL),(9,'74a75493c7ef4c6b',1,3,1,3.00,0,NULL,'2026-05-22 14:39:54',NULL,NULL,'没有手机号怎么登录的','test',NULL),(10,'74a75493c7ef4c6b',1,2,1,3.00,0,NULL,'2026-05-22 14:39:54',NULL,NULL,'没有手机号怎么登录的','test',NULL),(11,'74a75493c7ef4c6b',1,1,1,59.00,0,NULL,'2026-05-22 14:39:54',NULL,NULL,'没有手机号怎么登录的','test',NULL),(12,'c6d666f8bc9642da',1,1,1,59.00,0,NULL,'2026-05-22 14:40:18',NULL,NULL,'没有手机号怎么登录的','test',NULL),(13,'0950eb2d93e3401f',5,4,1,1.00,3,0,'2026-05-23 02:42:36',NULL,'2026-05-23 02:43:29','18994747539','大肠杆菌',''),(14,'6331c536e8d541de',6,4,1,1.00,0,0,'2026-05-23 02:46:54',NULL,NULL,'12111111111','大肠杆君','我要撸耄耋'),(15,'6331c536e8d541de',6,1,1,59.00,0,0,'2026-05-23 02:46:54',NULL,NULL,'12111111111','大肠杆君','我要撸耄耋'),(16,'99237ae2722d48b4',6,5,1,3.00,0,0,'2026-05-23 02:51:21',NULL,NULL,'12111111111','大肠杆君',''),(17,'91f8917045764b96',6,4,1,1.00,0,0,'2026-05-23 15:38:24',NULL,NULL,'1','大肠杆君',NULL),(18,'19fcbf7254e84f02',3,4,1,1.00,1,0,'2026-05-24 04:22:16','2026-05-24 08:27:20',NULL,'18616313186','落日大桥',NULL),(19,'829e8e150b34497d',5,4,1,1.00,4,NULL,'2026-05-24 04:25:36',NULL,NULL,'18994747539','大肠杆菌',NULL),(20,'396d5538bc0f40cd',7,4,1,1.00,4,NULL,'2026-05-24 08:24:58',NULL,NULL,'18943434343','aaa',NULL),(21,'396d5538bc0f40cd',7,3,1,3.00,2,NULL,'2026-05-24 08:24:58',NULL,NULL,'18943434343','aaa',NULL),(22,'batch_demo_01',3,1,1,59.00,3,0,'2026-05-18 13:00:00','2026-05-18 13:02:00','2026-05-18 14:00:00','13800001111','落日大桥','第一次来体验'),(23,'batch_demo_02',4,6,1,28.00,3,1,'2026-05-19 15:00:00','2026-05-19 15:05:00','2026-05-19 15:30:00','13800002222','陈际宇',NULL),(24,'batch_demo_03',5,1,1,59.00,3,2,'2026-05-19 16:00:00','2026-05-19 16:01:00','2026-05-19 17:00:00','13800003333','大肠杆菌','朋友推荐来的'),(25,'batch_demo_multi_01',1,1,1,59.00,1,NULL,'2026-05-20 11:00:00',NULL,NULL,'13900000000','test','周末犒劳自己'),(26,'batch_demo_multi_01',1,6,1,28.00,1,NULL,'2026-05-20 11:00:00',NULL,NULL,'13900000000','test',NULL),(27,'batch_demo_multi_01',1,11,2,16.00,1,NULL,'2026-05-20 11:00:00',NULL,NULL,'13900000000','test',NULL),(28,'batch_demo_pending',3,7,1,68.00,0,NULL,'2026-05-23 20:00:00',NULL,NULL,'13800001111','落日大桥',NULL),(29,'batch_demo_pending',3,8,1,22.00,0,NULL,'2026-05-23 20:00:00',NULL,NULL,'13800001111','落日大桥',NULL),(30,'batch_demo_ready',6,2,1,3.00,3,0,'2026-05-24 13:00:00','2026-05-24 13:01:00','2026-05-24 11:40:17','13900006666','大肠杆君',NULL),(31,'batch_demo_ready',6,9,1,18.00,2,0,'2026-05-24 13:00:00','2026-05-24 13:01:00',NULL,'13900006666','大肠杆君',NULL),(32,'batch_demo_cancelled',4,12,1,25.00,4,NULL,'2026-05-23 18:00:00','2026-05-23 18:01:00',NULL,'13800002222','陈际宇','临时有事去不了'),(33,'batch_today_01',5,3,2,6.00,1,0,'2026-05-24 08:30:00','2026-05-24 08:31:00',NULL,'13800003333','大肠杆菌',NULL),(34,'batch_today_02',7,4,1,1.00,0,NULL,'2026-05-24 14:00:00',NULL,NULL,'13800007777','aaa',''),(35,'batch_today_02',7,11,1,8.00,0,NULL,'2026-05-24 14:00:00',NULL,NULL,'13800007777','aaa',NULL),(36,'batch_01',3,1,1,59.00,3,0,'2026-05-15 14:00:00','2026-05-15 14:02:00','2026-05-15 15:00:00','','落日大桥','第一次体验猫咖！'),(37,'batch_02',4,2,1,3.00,3,1,'2026-05-15 15:00:00','2026-05-15 15:01:00','2026-05-15 15:20:00','','陈际宇',NULL),(38,'batch_03',1,1,1,59.00,3,0,'2026-05-16 10:00:00','2026-05-16 10:01:00','2026-05-16 11:00:00','','test','周末放松'),(39,'batch_03',1,8,1,28.00,3,0,'2026-05-16 10:00:00','2026-05-16 10:01:00','2026-05-16 11:00:00','','test','周末放松'),(40,'batch_04',5,6,1,25.00,3,2,'2026-05-16 11:00:00','2026-05-16 11:00:00','2026-05-16 11:30:00','','大肠杆菌',NULL),(41,'batch_05',3,4,1,1.00,3,1,'2026-05-17 14:00:00','2026-05-17 14:01:00','2026-05-17 14:30:00','','落日大桥','下午茶时间'),(42,'batch_05',3,2,1,3.00,3,1,'2026-05-17 14:00:00','2026-05-17 14:01:00','2026-05-17 14:30:00','','落日大桥','下午茶时间'),(43,'batch_06',6,1,1,59.00,3,0,'2026-05-17 15:00:00','2026-05-17 15:02:00','2026-05-17 16:00:00','','大肠杆君','犒劳自己'),(44,'batch_06',6,8,1,28.00,3,0,'2026-05-17 15:00:00','2026-05-17 15:02:00','2026-05-17 16:00:00','','大肠杆君','犒劳自己'),(45,'batch_06',6,9,1,22.00,3,0,'2026-05-17 15:00:00','2026-05-17 15:02:00','2026-05-17 16:00:00','','大肠杆君','犒劳自己'),(46,'batch_07',4,11,1,15.00,3,1,'2026-05-18 09:00:00','2026-05-18 09:01:00','2026-05-18 09:30:00','','陈际宇','早餐'),(47,'batch_07',4,9,1,22.00,3,1,'2026-05-18 09:00:00','2026-05-18 09:01:00','2026-05-18 09:30:00','','陈际宇','早餐'),(48,'batch_08',5,1,1,59.00,3,0,'2026-05-18 13:00:00','2026-05-18 13:00:00','2026-05-18 14:00:00','','大肠杆菌',NULL),(49,'batch_09',3,7,1,68.00,3,1,'2026-05-19 10:00:00','2026-05-19 10:01:00','2026-05-19 11:00:00','','落日大桥','套餐真划算'),(50,'batch_10',1,12,2,16.00,3,0,'2026-05-19 14:00:00','2026-05-19 14:01:00','2026-05-19 14:30:00','','test',NULL),(51,'batch_wait_01',3,17,1,12.00,2,0,'2026-05-20 15:00:00','2026-05-20 15:01:00',NULL,'','落日大桥','给自家猫买点玩具'),(52,'batch_wait_01',3,19,1,8.00,2,0,'2026-05-20 15:00:00','2026-05-20 15:01:00',NULL,'','落日大桥','给自家猫买点玩具'),(53,'batch_wait_02',4,16,1,15.00,2,1,'2026-05-21 16:00:00','2026-05-21 16:00:00',NULL,'','陈际宇',NULL),(54,'batch_wait_03',6,8,2,56.00,2,0,'2026-05-22 11:00:00','2026-05-22 11:02:00',NULL,'','大肠杆君','和闺蜜一起来'),(55,'batch_wait_03',6,14,1,22.00,2,0,'2026-05-22 11:00:00','2026-05-22 11:02:00',NULL,'','大肠杆君','和闺蜜一起来'),(56,'batch_wait_04',5,21,1,35.00,2,2,'2026-05-23 10:00:00','2026-05-23 10:00:00',NULL,'','大肠杆菌',NULL),(57,'batch_cancel_01',3,6,1,25.00,4,NULL,'2026-05-18 08:00:00',NULL,NULL,'','落日大桥','临时有事'),(58,'batch_cancel_02',7,1,1,59.00,4,NULL,'2026-05-20 18:00:00',NULL,NULL,'','aaa','时间选错了'),(59,'batch_cancel_03',4,22,1,199.00,4,NULL,'2026-05-22 14:00:00',NULL,NULL,'','陈际宇','再看看别的'),(60,'batch_pend_01',3,10,2,36.00,0,NULL,'2026-05-24 08:00:00',NULL,NULL,'','落日大桥',NULL),(61,'batch_pend_01',3,11,2,30.00,0,NULL,'2026-05-24 08:00:00',NULL,NULL,'','落日大桥',NULL),(62,'batch_pend_02',5,13,1,32.00,0,NULL,'2026-05-24 09:00:00',NULL,NULL,'','大肠杆菌',NULL),(63,'batch_pend_03',6,23,1,22.00,0,NULL,'2026-05-24 10:00:00',NULL,NULL,'','大肠杆君',NULL),(64,'batch_pend_04',7,15,1,18.00,0,NULL,'2026-05-24 12:00:00',NULL,NULL,'','aaa',NULL),(65,'batch_pend_05',1,20,1,68.00,0,NULL,'2026-05-24 13:00:00',NULL,NULL,'','test','考虑要不要买'),(66,'batch_pend_05',1,24,1,28.00,0,NULL,'2026-05-24 13:00:00',NULL,NULL,'','test','考虑要不要买'),(67,'f1260c40575c11f18f7ff69637bacae7',3,4,1,1.00,0,NULL,'2026-05-24 14:00:00',NULL,NULL,'','落日大桥',NULL),(68,'f1265cc2575c11f18f7ff69637bacae7',7,2,1,3.00,3,NULL,'2026-05-24 14:15:00',NULL,'2026-05-24 10:58:09','','aaa',NULL),(69,'f126b08d575c11f18f7ff69637bacae7',5,8,1,28.00,1,NULL,'2026-05-24 14:30:00','2026-05-24 14:16:31',NULL,'','大肠杆菌',NULL),(70,'d364889dbdd54022',7,24,1,199.00,4,NULL,'2026-05-24 11:38:55',NULL,NULL,'18943434343','aaa',NULL),(71,'d364889dbdd54022',7,14,1,15.00,0,NULL,'2026-05-24 11:38:55',NULL,NULL,'18943434343','aaa',NULL),(72,'e632a5d9f7804edc',7,27,1,22.00,0,0,'2026-05-24 13:33:53',NULL,NULL,'18943434343','aaa',''),(73,'8d5ed74038904b64',1,16,1,45.00,0,0,'2026-05-24 13:50:01',NULL,NULL,'18947532547','test','21313'),(74,'a60d14a96e5b4bca',1,27,1,22.00,0,NULL,'2026-05-24 13:52:59',NULL,NULL,'18947532547','test','123'),(75,'58c4a7a5d7924012',9,16,1,45.00,0,0,'2026-05-24 14:29:43',NULL,NULL,'19881232123','🐴',''),(76,'226f2f715c8c4b01',3,16,1,45.00,0,0,'2026-05-25 05:39:01',NULL,NULL,'18616313186','落日大桥',''),(77,'78e0586dc63045c9',14,27,2,44.00,3,0,'2026-05-25 05:46:07',NULL,'2026-05-25 05:46:30','18616313187','1234567','');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `productId` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `productName` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `category` tinyint unsigned NOT NULL COMMENT '分类: 0=服务, 1=餐饮, 2=猫咪用品',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `stockQuantity` int unsigned NOT NULL DEFAULT '0' COMMENT '库存数量',
  `imageUrl` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片URL',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '描述',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态: 0=已下架, 1=在售',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`productId`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,'撸猫一小时套餐',0,59.00,996,'/products/7c33b323da3245fa9437fc50ce7b27e6.png','这里不是普通的撸猫馆，而是治愈心灵的 “ 喵星乐园\"，等你来撒欢！\n当生活压力如潮水般涌来，“ 萌爪小筑 ” 就是你的解压神器站。走进这里，让毛茸宝贝们为你赶走疲惫与烦恼～',1,'2026-05-21 09:47:15'),(2,'可乐',1,3.00,97,'/products/cola.jpg','冰镇可乐',1,'2026-05-21 09:47:15'),(3,'零糖可乐',1,3.00,82,'/products/cola.jpg','second debug product',1,'2026-05-21 09:47:15'),(4,'猫屎咖啡',1,1.00,94,'/products/8f7b79187cb54305b849ef60061e3a35.png','麝香猫咖啡（Kopi Luwak），是印尼人的俗称，发音为“鲁瓦克” [4]，又称猫屎咖啡、努瓦克咖啡 [2]，是由印尼语“鲁瓦克咖啡”(Luwak Kopi)翻译而来 [4]，是印度尼西亚特产，被誉为咖啡世界第一，是印尼岛屿上特有的有袋大灵猫（Luwak，又称椰子猫或麝香猫）排泄出来的不能消化的咖啡豆精制而成的 [2]，被荷兰皇帝“钦点”为贡品之一 [4]，其味道更浓郁、酸味更淡。',1,'2026-05-21 11:17:17'),(5,'逗猫棒',2,3.00,0,'a','好玩',1,'2026-05-22 16:35:24'),(6,'半小时撸猫券',0,25.00,999,'','购买后可在半小时内与所有在岗猫咪互动',1,'2026-05-24 10:20:34'),(7,'撸猫+饮品套餐',0,68.00,999,'','包含一小时撸猫券 + 任选一杯饮品',1,'2026-05-24 10:20:34'),(8,'猫爪拿铁',1,28.00,50,'/products/catpaw_latte.jpg','可爱猫爪拉花拿铁，拍照打卡必备',1,'2026-05-24 10:20:34'),(9,'抹茶蛋糕',1,22.00,30,'/products/matcha_cake.jpg','日式抹茶千层，猫咪也馋的甜点',1,'2026-05-24 10:20:34'),(10,'猫咪马卡龙',1,18.00,25,'/products/macaron.jpg','猫咪造型马卡龙，一盒四枚',1,'2026-05-24 10:20:34'),(11,'猫薄荷茶',1,15.00,40,'/products/catnip_tea.jpg','人猫共饮的薄荷茶，清爽解暑',1,'2026-05-24 10:20:34'),(12,'猫条零食',2,8.00,200,'/products/cat_snack.jpg','进口猫条，互动喂食专用',1,'2026-05-24 10:20:34'),(13,'羽毛逗猫棒',2,12.00,60,'/products/feather_wand.jpg','长杆羽毛逗猫棒，猫咪最爱',1,'2026-05-24 10:20:34'),(14,'猫薄荷玩具鱼',2,15.00,3,'/products/catnip_fish.jpg','内含猫薄荷的布艺鱼，猫咪抱着不撒手',1,'2026-05-24 10:20:34'),(15,'猫抓板',2,25.00,0,'/products/scratch_pad.jpg','瓦楞纸猫抓板，保护你的沙发',0,'2026-05-24 10:20:34'),(16,'工作日撸猫券',0,45.00,497,'','周一至周五专享，一小时撸猫体验',1,'2026-05-24 10:38:09'),(17,'双人撸猫套餐',0,108.00,300,'','两人同行，各一小时 + 两杯饮品',1,'2026-05-24 10:38:09'),(18,'冰美式咖啡',1,18.00,60,'/products/iced_americano.jpg','经典冰美式，提神醒脑',1,'2026-05-24 10:38:09'),(19,'热巧克力',1,20.00,40,'/products/hot_chocolate.jpg','浓郁可可+棉花糖，冬日暖饮',0,'2026-05-24 10:38:09'),(20,'芝士蛋糕',1,25.00,20,'/products/cheesecake.jpg','纽约风格重芝士，每日限量',1,'2026-05-24 10:38:09'),(21,'提拉米苏',1,28.00,15,'/products/tiramisu.jpg','意大利经典，咖啡与奶油的完美融合',0,'2026-05-24 10:38:09'),(22,'三明治套餐',1,32.00,25,'/products/sandwich.jpg','火腿芝士三明治+饮品任选',1,'2026-05-24 10:38:09'),(23,'猫窝',2,68.00,12,'/products/cat_bed.jpg','可拆洗毛绒猫窝，柔软舒适',1,'2026-05-24 10:38:09'),(24,'猫爬架',2,199.00,5,'/products/cat_tree.jpg','三层实木猫爬架，含剑麻柱',1,'2026-05-24 10:38:09'),(25,'电动老鼠玩具',2,35.00,18,'/products/cat_toy_mouse.jpg','遥控电动老鼠，猫咪疯狂追逐',1,'2026-05-24 10:38:09'),(26,'猫项圈',2,28.00,30,'/products/cat_collar.jpg','可调节安全项圈，带铃铛',1,'2026-05-24 10:38:09'),(27,'猫薄荷喷雾',2,22.00,21,'/products/catnip_spray.jpg','猫薄荷提取液，喷在玩具上效果翻倍',1,'2026-05-24 10:38:09');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `userId` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `userPassword` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `userName` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `userType` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '用户类型: 0=顾客, 1=管理员',
  `gender` tinyint unsigned DEFAULT NULL COMMENT '性别: 1=男, 2=女, NULL=未设置',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `userPhone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `userAvatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `registerTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`userId`),
  UNIQUE KEY `uk_userName` (`userName`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'$2b$12$7fpEcRwGVOZ7mHsu8tlliuxS0gIZwTu8ypD2a.WucNntKETTA98na','test',0,1,'2077-05-05','18947532547','/avatars/166316ec77224e6095103e7c82b2e7df.png','2026-05-21 09:48:02'),(2,'$2b$12$yD9Y1OS7J2G4l7a0YFj.b.QE899ltv0fEWSY7i6Ea6udaR9BfYiea','admin',1,1,NULL,'',NULL,'2026-05-21 09:48:26'),(3,'$2b$12$r1V9re1kHz7iiEKPZqI4IOv61OCrDyUPQlxJ7zakFima6LReHdvzG','落日大桥',0,2,NULL,'18616313186','/avatars/749c01fe25874d75954d3617dfac5ca3.jpg','2026-05-21 11:37:18'),(4,'$2b$12$KB0o2vmiwUgzidS.p.A4uuTHKX34rUcvdJcthfIDy2.K2.m0Z6UDm','陈际宇',0,NULL,NULL,'19850559363',NULL,'2026-05-21 14:51:51'),(5,'$2b$12$R4XB4ehxA1n7tYqtaLkdnukSsxT8Uv2avk2xSw6muxpd0GNT9U./i','大肠杆菌',0,1,'2026-05-04','18994747539','/avatars/36fe2e7933984a17ab0fcf005442f603.jpg','2026-05-21 15:00:36'),(6,'$2b$12$DmaV9XzgIhgahug1/9sFKu.Qvx0JmfWDeeUFNXTCsrz9QuGOV26Pe','大肠杆君',0,NULL,NULL,'12111111111','/avatars/28d7956b7b2341f8a93f96a4146f8c52.png','2026-05-23 02:44:34'),(7,'$2b$12$39sKV.z.IGiy0cW8fbh3IucrxiDqgzBpybaiFiavUFA/MteKW0H.G','aaa',0,1,'2026-05-04','18943434342','/avatars/4ec517121c9a45a7ad5cc8e8b48042fd.jpg','2026-05-24 08:23:34'),(9,'$2b$12$8.Y6bl5dJb7wF/tTjl7XTO2atdmBH7HC00cDNMh2./9.jR8ue2dFW','🐴',0,NULL,NULL,'19881232123',NULL,'2026-05-24 14:28:22'),(10,'$2b$12$bBYQUURCwYlnb8z7pa7/dujEzcwjNV4RtVXdSER3MPdAivrGtjD4a','yang',0,1,'2026-05-24','18651819760',NULL,'2026-05-24 15:44:51'),(11,'$2b$12$y0I9NfQWqxDKUfnXzhXBsebkmbsuTnpFlMWWg/FbpETZEKmw3LRXG','123',0,NULL,NULL,'18111111111',NULL,'2026-05-25 05:16:49'),(12,'$2b$12$Dge/m1DkP6yxENlcYg2AnOwvjHcLQu0cO9fwKW/JyDdwwpsTGgZQy','清风拂山',0,NULL,NULL,'15348435453',NULL,'2026-05-25 05:20:29'),(13,'$2b$12$ongSdToiytOYmhGPZgT2He7BUdPmY8abcnk3suQdZPDuhqcODF/kW','石头星',0,NULL,NULL,'15346453452',NULL,'2026-05-25 05:35:32'),(14,'$2b$12$GBnWHjiNXihe1HRRsiToPu54kWdXytnel0SVnrRZJ4ehsCMtENdnq','1234567',0,NULL,NULL,'18616313187',NULL,'2026-05-25 05:44:51');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'cat_cafe'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-25 10:37:43

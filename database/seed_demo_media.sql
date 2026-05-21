USE cat_cafe;

UPDATE catinformation
SET catName = 'Mimi',
    breed = 'Siamese',
    birthday = '2023-06-01',
    status = 1,
    personality = 'playful and curious',
    photoUrl = '/cats/mimi.jpg',
    notes = 'guest favorite'
WHERE catId = 2;

UPDATE catinformation
SET catName = 'DuoDuo',
    breed = 'Ragdoll',
    birthday = '2022-09-15',
    status = 1,
    personality = 'gentle and friendly',
    photoUrl = '/cats/duoduo.jpg',
    notes = 'likes sunny seats'
WHERE catId = 3;

INSERT INTO catinformation (catName, breed, birthday, status, personality, photoUrl, notes)
SELECT 'HaiMao', 'British Shorthair', '2022-04-12', 1, 'calm and steady', '/cats/haimao.jpg', 'quiet lounge cat'
WHERE NOT EXISTS (SELECT 1 FROM catinformation WHERE photoUrl = '/cats/haimao.jpg');

INSERT INTO catinformation (catName, breed, birthday, status, personality, photoUrl, notes)
SELECT 'Pidan', 'Tuxedo', '2021-11-08', 1, 'smart and active', '/cats/pidan.jpg', 'curious about cameras'
WHERE NOT EXISTS (SELECT 1 FROM catinformation WHERE photoUrl = '/cats/pidan.jpg');

INSERT INTO catinformation (catName, breed, birthday, status, personality, photoUrl, notes)
SELECT 'XiaoHui', 'Tabby', '2023-02-20', 1, 'shy but sweet', '/cats/xiaohui.jpg', 'best with regulars'
WHERE NOT EXISTS (SELECT 1 FROM catinformation WHERE photoUrl = '/cats/xiaohui.jpg');

INSERT INTO catinformation (catName, breed, birthday, status, personality, photoUrl, notes)
SELECT 'XueQiu', 'Persian', '2021-12-05', 1, 'lazy and soft', '/cats/xueqiu.jpg', 'sleep champion'
WHERE NOT EXISTS (SELECT 1 FROM catinformation WHERE photoUrl = '/cats/xueqiu.jpg');

INSERT INTO catinformation (catName, breed, birthday, status, personality, photoUrl, notes)
SELECT 'PangXi', 'British Shorthair', '2020-08-30', 1, 'food motivated', '/cats/pangxi.jpg', 'works for treats'
WHERE NOT EXISTS (SELECT 1 FROM catinformation WHERE photoUrl = '/cats/pangxi.jpg');

UPDATE `user`
SET userAvatar = '/avatars/luoridaqiao.jpg'
WHERE userName = 'user';

UPDATE product
SET imageUrl = CASE
    WHEN productName IN ('Cola', 'Cola Zero', '可乐', '无糖可乐') THEN '/products/cola.jpg'
    ELSE ''
END
WHERE status = 1;

INSERT INTO product (productName, category, price, stockQuantity, imageUrl, description, status)
SELECT '一小时撸猫券', 0, 39.00, 999, '', '购买后可在一小时内与所有在岗猫咪互动，不对应购买单只猫咪。', 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE productName = '一小时撸猫券');

INSERT INTO product (productName, category, price, stockQuantity, imageUrl, description, status)
SELECT 'Cola', 1, 12.00, 99, '/products/cola.jpg', 'cold cola for debug', 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE productName = 'Cola' AND imageUrl = '/products/cola.jpg');

INSERT INTO product (productName, category, price, stockQuantity, imageUrl, description, status)
SELECT 'Cola Zero', 1, 13.00, 88, '/products/cola.jpg', 'second debug product', 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE productName = 'Cola Zero' AND imageUrl = '/products/cola.jpg');



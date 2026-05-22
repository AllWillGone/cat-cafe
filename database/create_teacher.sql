-- 老师查看数据库用的只读账号（Adminer 登录用）
-- 在服务器上手动执行: docker exec -i cat-cafe-mysql-1 mysql -uroot -pyang < database/create_teacher.sql

CREATE USER IF NOT EXISTS 'teacher'@'%' IDENTIFIED BY 'catcafe2026';
GRANT SELECT ON cat_cafe.* TO 'teacher'@'%';
FLUSH PRIVILEGES;

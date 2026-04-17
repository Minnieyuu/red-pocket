-- 如果資料庫不存在，才建立
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'red-pocket')
BEGIN
    CREATE DATABASE [red-pocket];
END
GO
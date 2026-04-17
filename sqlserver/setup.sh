# 等待 SQL Server 啟動（給它 30 秒）
echo "等待 SQL Server 啟動..."
sleep 30s

# 執行初始化 SQL
/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P $SA_PASSWORD -No -d master -i /usr/config/init.sql

echo "資料庫初始化完成！"
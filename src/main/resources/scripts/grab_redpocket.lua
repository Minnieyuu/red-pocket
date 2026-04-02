

--KEYS[1]庫存key
--KEYS[2]搶到的userSet Key
--ARGV[1]userId

-- 第一步：判斷用戶是否已搶過
local already = redis.call("SISMEMBER", KEYS[2], ARGV[1])
if already == 1 then
    return -1
end

-- 第二步：讀取剩餘庫存
local stock = tonumber(redis.call("GET", KEYS[1]))

-- 第三步：庫存不足（包含 key 不存在的 nil 情況）
if stock == nil or stock <= 0 then
    return 0
end

-- 第四步：原子扣庫存
redis.call("DECRBY", KEYS[1], 1)

-- 第五步：記錄得獎用戶
redis.call("SADD", KEYS[2], ARGV[1])

-- 第六步：回報成功
return 1

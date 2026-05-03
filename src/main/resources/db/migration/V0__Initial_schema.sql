-- 初始数据库结构创建
-- 用于全新环境初始化表结构

-- 创建交易信息表
CREATE TABLE IF NOT EXISTS trade_info (
    id INT AUTO_INCREMENT PRIMARY KEY,
    BASE_ID INT,
    BASE_SYMBOL VARCHAR(255),
    QUOTE_ID INT,
    QUOTE_SYMBOL VARCHAR(255),
    sale_or_buy VARCHAR(10),
    price DECIMAL(25, 12),
    BASE_NUM DECIMAL(20, 8),
    QUOTE_NUM DECIMAL(20, 8),
    TRADE_DATE VARCHAR(20)
);

-- 创建币种信息表
CREATE TABLE IF NOT EXISTS cmc_map (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tid INT,
    name VARCHAR(255),
    symbol VARCHAR(50),
    slug VARCHAR(255),
    IS_ACTIVE INT,
    rank INT,
    FIRST_HISTORICAL_DATA VARCHAR(50),
    LAST_HISTORICAL_DATA VARCHAR(50),
    PLATFORM_ID INT,
    TOKEN_ADDRESS VARCHAR(255),
    IS_SELECTED INT DEFAULT 0
);

-- 创建最新价格表
CREATE TABLE IF NOT EXISTS cmc_quotes_latest (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tid INT,
    name VARCHAR(255),
    symbol VARCHAR(50),
    slug VARCHAR(255),
    LAST_UPDATED VARCHAR(50),
    NUM_MARKET_PAIRS INT,
    DATE_ADDED VARCHAR(50),
    MAX_SUPPLY DECIMAL(30, 8),
    CIRCULATING_SUPPLY DECIMAL(30, 8),
    TOTAL_SUPPLY DECIMAL(30, 8),
    IS_ACTIVE INT,
    PLATFORM_ID INT,
    TOKEN_ADDRESS VARCHAR(255),
    CMC_RANK INT,
    price DECIMAL(25, 12),
    volume_24h DECIMAL(30, 2),
    volume_change_24h DECIMAL(19, 8),
    percent_change_1h DECIMAL(19, 8),
    percent_change_24h DECIMAL(19, 8),
    percent_change_7d DECIMAL(19, 8),
    percent_change_30d DECIMAL(19, 8),
    percent_change_60d DECIMAL(19, 8),
    percent_change_90d DECIMAL(19, 8),
    market_cap DECIMAL(30, 2),
    market_cap_dominance DECIMAL(19, 8)
);

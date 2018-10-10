INSERT INTO core.exchange (id, name, website, location, ip, future, fiat, instance_name, created_date, last_modified_date, created_by, last_modified_by) VALUES (1, 'bitstamp', 'https://www.bitstamp.net', '英国', null, false, null, 'org.knowm.xchange.bitstamp.BitstampExchange', '2018-09-14 16:38:43', '2018-09-14 16:38:43', null, null);
INSERT INTO core.exchange (id, name, website, location, ip, future, fiat, instance_name, created_date, last_modified_date, created_by, last_modified_by) VALUES (6, 'HitBtc', 'https://hitbtc.com', 'unknown', null, false, null, 'org.knowm.xchange.hitbtc.v2.HitbtcExchange', '2018-09-14 16:38:43', '2018-09-14 16:38:43', null, null);

delete from top_one_order_book where exchange_id =9;
delete from exchange where id=9;
INSERT INTO core.exchange (id, name, website, location, ip, future, fiat, instance_name, created_date, last_modified_date, created_by, last_modified_by) 
VALUES (9, 'bcex', 'https://www.bcex.ca/', 'unknown', null, false, null, 'org.knowm.xchange.paribu.ParibuExchange', '2018-09-14 16:38:43', '2018-09-14 16:38:43', null, null);
select * from arbitrage_log;

-- 订单数据 无法获取
INSERT INTO core.exchange (id, name, website, location, ip, future, fiat, instance_name, created_date, last_modified_date, created_by, last_modified_by) VALUES (2, 'binance', ' https://www.binance.com', 'unknown', null, false, null, 'org.knowm.xchange.binance.BinanceExchange', '2018-09-14 16:38:43', '2018-09-14 16:38:43', null, null);
INSERT INTO core.exchange (id, name, website, location, ip, future, fiat, instance_name, created_date, last_modified_date, created_by, last_modified_by) VALUES (3, 'OKEx', ' https://www.okex.com', 'unknown', null, false, null, 'org.knowm.xchange.okcoin.OkCoinExchange', '2018-09-14 16:38:43', '2018-09-14 16:38:43', null, null);
INSERT INTO core.exchange (id, name, website, location, ip, future, fiat, instance_name, created_date, last_modified_date, created_by, last_modified_by) VALUES (4, 'bitfinex', 'https://www.bitfinex.com', 'unknown', null, false, null, 'org.knowm.xchange.bitfinex.v2.BitfinexExchange', '2018-09-14 16:38:43', '2018-09-14 16:38:43', null, null);
INSERT INTO core.exchange (id, name, website, location, ip, future, fiat, instance_name, created_date, last_modified_date, created_by, last_modified_by) VALUES (5, 'zb', 'https://www.zb.com', 'unknown', null, false, null, 'org.knowm.xchange.zaif.ZaifExchange', '2018-09-14 16:38:43', '2018-09-14 16:38:43', null, null);
INSERT INTO core.exchange (id, name, website, location, ip, future, fiat, instance_name, created_date, last_modified_date, created_by, last_modified_by) VALUES (7, 'Bit-Z ', 'https://www.bit-z.com', 'unknown', null, false, null, 'org.xchange.bitz.BitZExchange', '2018-09-14 16:38:43', '2018-09-14 16:38:43', null, null);
INSERT INTO core.exchange (id, name, website, location, ip, future, fiat, instance_name, created_date, last_modified_date, created_by, last_modified_by) VALUES (8, 'Bibox ', 'https://www.bibox.com/', 'unknown', null, false, null, 'org.knowm.xchange.bibox.BiboxExchange', '2018-09-14 16:38:43', '2018-09-14 16:38:43', null, null);



-- 可以试试最大的现货交易所，binance.com。费率是0.1%，账户里有BNB进行手续费结算，还可以打折，api限制是10万次/天，且<10次/秒，且<1200次/分钟。这里面做的人多，估计机会也比较渺茫，不过主流市场，将来搬砖都用的到
-- 其次可以看看0.15%的okex.com，是个北京的科技公司
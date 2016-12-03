COPY MENU
FROM '/extra/jtan021/CS166-Project/project/data/menu.csv'
WITH DELIMITER ';';

COPY USERS
FROM '/extra/jtan021/CS166-Project/project/data/users.csv'
WITH DELIMITER ';';

COPY ORDERS
FROM '/extra/jtan021/CS166-Project/project/data/orders.csv'
WITH DELIMITER ';';
ALTER SEQUENCE orders_orderid_seq RESTART 87257;

COPY ITEMSTATUS
FROM '/extra/jtan021/CS166-Project/project/data/itemStatus.csv'
WITH DELIMITER ';';


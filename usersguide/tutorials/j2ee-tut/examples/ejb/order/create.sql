drop table lineitems;
drop table orders;

create table orders 
(orderid varchar(3) constraint pk_order primary key,  
customerid varchar(3),
totalprice decimal(10,2),
status varchar(10));

insert into orders
values ('456', 'c55', 100.00, 'shipped');

create table lineitems
(itemno integer,
orderid varchar(3),
productid varchar(3),
unitprice decimal(10,2),
quantity integer,
constraint fk_orderid
foreign key (orderid)
references orders(orderid));

insert into lineitems
values (1, '456', 'p67', 89.00, 1);
insert into lineitems
values (2, '456', 'p12', 11.00, 1);

exit;

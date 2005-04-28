create table salesrep
(salesrepid varchar(3) constraint pk_salesrep primary key,
name varchar(24));

insert into salesrep
values ('876', 'Clyde Webster');
insert into salesrep
values ('543', 'Janice Martin');
insert into salesrep
values ('777', 'John Johnston');

create table customer 
(customerid varchar(3) constraint pk_customer primary key,  
salesrepid varchar(3),
name varchar(24),
constraint fk_salesrepid
foreign key (salesrepid)
references salesrep(salesrepid));

insert into customer
values ('123', '876', 'Sal Jones');
insert into customer
values ('987', '777', 'Mary Jackson');
insert into customer
values ('221', '543', 'Alice Smith');
insert into customer
values ('388', '543', 'Bill Williamson');
insert into customer
values ('456', '543', 'Joe Smith');

exit;

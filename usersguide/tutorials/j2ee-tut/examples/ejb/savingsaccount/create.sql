drop table savingsaccount;

create table savingsaccount  
(id varchar(3) constraint pk_savings_account primary key,  
firstname varchar(24),  
lastname varchar(24),  
balance numeric(10,2));

exit;

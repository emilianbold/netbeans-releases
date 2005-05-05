drop table storagebin;
drop table widget;

create table widget
(widgetid varchar(3) constraint pk_widget primary key,
description varchar(50),
price decimal (10,2));

insert into widget
values ('876', 'Intergalactic Transporter', 1300.00);
insert into widget
values ('543', 'Superwarp Retrodrive', 55.00);
insert into widget
values ('777', 'Duct Tape', 1.00);


create table storagebin 
  (storagebinid varchar(3) 
     constraint pk_storagebin primary key,  
  widgetid varchar(3),
  quantity integer,
     constraint fk_widgetid
     foreign key (widgetid)
     references widget(widgetid));

insert into storagebin
values ('123', '876', 100);
insert into storagebin
values ('221', '543', 50);
insert into storagebin
values ('388', '777', 500);

exit;

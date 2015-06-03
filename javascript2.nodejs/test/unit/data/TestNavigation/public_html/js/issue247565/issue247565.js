var o1 = require("folder/literal");
var o2 = require("folder/literalRef");
var kocka = require("fi");

o1.obj.conf.a;
o1.pokus.getDay();
o1.obj.dob.getMilliseconds();
o1.obj.hello();
o1.obj.nick;


o2.obj.conf.a;
o2.obj.dob.getFullYear();
o2.pokus.getSeconds();
o2.obj.hello();
o2.obj.nick;
#!/usr/sbin/dtrace -qZs
 
self uint32_t stkIdx;


self int indent;

php*:::function-entry
/arg0/
{
        self->follow = 1;
}

php*:::function-entry
/arg0 && self->follow/
{
        /*printf("%*s", self->indent, "");*/
        printf("0 %d %s %s %d \"%s\" \n", timestamp, copyinstr(arg0), copyinstr(arg1), arg2, copyinstr(arg3));
        self->indent += 2;
        self->script = copyinstr(arg1);
}

php*:::function-return
/arg0 && self->follow/
{
        self->indent -= 2;
/*        printf("%*s", self->indent, "");*/
        printf("2 %d %s %s %d \"%s\"\n", timestamp, copyinstr(arg0),  self->script, arg2, copyinstr(arg3));
        printf("1 %d %s %s %d \"%s\"\n", timestamp, copyinstr(arg0), copyinstr(arg1), arg2, copyinstr(arg3));

}

php*:::function-return
/arg0/
{
        self->follow = 0;
}




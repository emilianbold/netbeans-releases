#!/usr/sbin/dtrace -qs
 




pid$1::*mysql_parse*:entry
{
        self->query = copyinstr(arg1);
}

pid$1::*mysql_execute_command*:entry
{
        self->start = timestamp;
}

pid$1::*mysql_execute_command*:return
/self->start/
{
        this->elapsed = timestamp;
        this->nano = this->elapsed - self->start;
        this->time1 = (this->elapsed - self->start)/1000000000;
        this->time2 = ((this->nano % 1000000000) * 100) / 1000000000;
        printf("%d \"%s\" %d.%d\n",  timestamp,  self->query, this->time1, this->time2);
        this->elapsed = 0;
        self->start = 0;
        self->query = 0;
}




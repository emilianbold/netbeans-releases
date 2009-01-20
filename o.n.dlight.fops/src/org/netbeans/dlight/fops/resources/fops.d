#!/usr/sbin/dtrace -s
#pragma D option quiet 
#pragma D option destructive

self uint32_t stkIdx;

syscall::read:entry 
/pid == $1/
{
    self->fd = arg0;
}

syscall::read:return 
/pid == $1/
{
    fname = (self->fd == 0 ) ? "<stdin>" : fds[self->fd].fi_pathname;
    printf("1 %d %d %d %d \"%s\" %d %d\n",
           cpu,
           tid,
	   timestamp,
	   0,
	   fname,
	   arg1,
	   self->stkIdx);
/*    raise(29); */
}

syscall::write:entry 
/pid == $1/
{
    self->fd = arg0;
}

syscall::write:return 
/pid == $1/
{
    fname = (self->fd == 1 ) ? "<stdout>" : (self->fd == 2 ) ? "<stderr>" : fds[self->fd].fi_pathname;
    printf("1 %d %d %d %d \"%s\" %d %d\n",
           cpu,
           tid,
	   timestamp,
	   1,
	   fname,
	   arg1,
	   self->stkIdx);
    /*raise(29);*/
}

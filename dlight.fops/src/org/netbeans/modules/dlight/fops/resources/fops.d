#!/usr/sbin/dtrace -s
#pragma D option quiet

/* Unique I/O session id */
uint64_t sid;

/* Maps file descriptor to session id. */
int64_t fd2sid[int];

syscall::open*:entry,
syscall::creat*:entry
/pid == $1/
{
    self->pathaddr = arg0;
}

syscall::open*:return,
syscall::creat*:return
/pid == $1 && self->pathaddr/
{
    this->sid = arg0? ++sid : 0;
    fd2sid[arg0] = this->sid;
    printf("%d %s %d \"%s\" %d", timestamp, "open", this->sid, copyinstr(self->pathaddr), 0);
    ustack();
    printf("\n");

    self->pathaddr = 0;
}

syscall::*read*:entry
/pid == $1/
{
    this->sid = fd2sid[arg0]? fd2sid[arg0] : -arg0;
    @transfer["read", (signed int)this->sid, arg0 == 0? "<stdin>" : fds[arg0].fi_pathname, ustack()] = sum(arg2);
}

syscall::*write*:entry
/pid == $1/
{
    this->sid = fd2sid[arg0]? fd2sid[arg0] : -arg0;
    @transfer["write", (signed int)this->sid, arg0 == 1? "<stdout>" : arg0 == 2? "<stderr>" : fds[arg0].fi_pathname, ustack()] = sum(arg2);
}

syscall::close*:entry
/pid == $1/
{
    self->fd = arg0;
    self->sid = fd2sid[arg0]? fd2sid[arg0] : -arg0;
    self->path = arg0 == 0? "<stdin>" : arg0 == 1? "<stdout>" : arg0 == 2? "<stderr>" : fds[arg0].fi_pathname;
}

syscall::close*:return
/pid == $1 && self->fd/
{
    printf("%d %s %d \"%s\" %d", timestamp, "close", (signed int)self->sid, self->path, 0);
    ustack();
    printf("\n");

    fd2sid[self->fd] = 0;
    self->fd = 0;
    self->sid = 0;
    self->path = "";
}

tick-1s, END
{
    printa("-1 %s %d \"%s\" %@d %k\n", @transfer);
    trunc(@transfer);
}

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
    printf("%d %s %d \"%s\" %d", timestamp, "open", this->sid, arg0? fds[arg0].fi_pathname : copyinstr(self->pathaddr), 0);
    ustack();
    printf("\n");

    self->pathaddr = 0;
}

syscall::*read*:entry
/pid == $1/
{
    self->fd = arg0;
    self->sid = fd2sid[arg0]? fd2sid[arg0] : -arg0-1;
}

syscall::*read*:return
/pid == $1 && self->sid/
{
    @transfer["read", (signed int)self->sid, self->fd == 0? "<stdin>" : fds[self->fd].fi_pathname, ustack()] = sum(arg1);

    self->fd = 0;
    self->sid = 0;
}

syscall::*write*:entry
/pid == $1/
{
    self->fd = arg0;
    self->sid = fd2sid[arg0]? fd2sid[arg0] : -arg0-1;
}

syscall::*write*:return
/pid == $1 && self->sid/
{
    @transfer["write", (signed int)self->sid, self->fd == 1? "<stdout>" : self->fd == 2? "<stderr>" : fds[self->fd].fi_pathname, ustack()] = sum(arg1);

    self->fd = 0;
    self->sid = 0;
}

syscall::close*:entry
/pid == $1/
{
    self->fd = arg0;
    self->sid = fd2sid[arg0]? fd2sid[arg0] : -arg0-1;
    self->path = arg0 == 0? "<stdin>" : arg0 == 1? "<stdout>" : arg0 == 2? "<stderr>" : fds[arg0].fi_pathname;
}

syscall::close*:return
/pid == $1 && self->sid/
{
    printf("%d %s %d \"%s\" %d", timestamp, "close", (signed int)self->sid, self->path, 0);
    ustack();
    printf("\n");

    fd2sid[self->fd] = 0;
    self->fd = 0;
    self->sid = 0;
    self->path = "";
}

tick-200ms, END
{
    printa("-1 %s %d \"%s\" %@d %k\n", @transfer);
    trunc(@transfer);
}

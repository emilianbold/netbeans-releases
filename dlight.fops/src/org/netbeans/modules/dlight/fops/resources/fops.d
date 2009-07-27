#!/usr/sbin/dtrace -s
#pragma D option quiet 

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
    this->fd = arg0;
    printf("%d %d %d %s %d \"%s\" %d",
            timestamp, cpu, tid, "open", this->fd, copyinstr(self->pathaddr), 0);
    ustack();
    printf("\n");
    self->pathaddr = 0;
}

syscall::close*:entry
/pid == $1 && fds[arg0].fi_pathname != "<none>" && fds[arg0].fi_pathname != "<unknown>"/
{
    self->fd = arg0;
    self->path = fds[self->fd].fi_pathname;
}

syscall::close*:return
/pid == $1 && self->fd/
{
    printf("%d %d %d %s %d \"%s\" %d",
            timestamp, cpu, tid, "close", self->fd, self->path, 0);
    ustack();
    printf("\n");
    self->fd = 0;
}

syscall::*read*:entry
/pid == $1 && fds[arg0].fi_pathname != "<none>" && fds[arg0].fi_pathname != "<unknown>"/
{
    this->fd = arg0;
    this->path = fds[this->fd].fi_pathname;
    printf("%d %d %d %s %d \"%s\" %d",
            timestamp, cpu, tid, "read", this->fd, this->path, arg2);
    ustack();
    printf("\n");
}

syscall::*write*:entry
/pid == $1 && fds[arg0].fi_pathname != "<none>" && fds[arg0].fi_pathname != "<unknown>"/
{
    this->fd = arg0;
    this->path = fds[this->fd].fi_pathname;
    printf("%d %d %d %s %d \"%s\" %d",
            timestamp, cpu, tid, "write", this->fd, this->path, arg2);
    ustack();
    printf("\n");
}

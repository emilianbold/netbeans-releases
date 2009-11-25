#!/usr/sbin/dtrace -Cs
#pragma D option quiet
#pragma D option aggrate=200ms

/* Interval between read/write aggregation dumps, in milliseconds */
inline int64_t fops_report_interval = 200;

/* Script start timestamp */
uint64_t fops_start_timestamp;

/* Time since script start, in milliseconds */
inline int64_t fops_timestamp = (timestamp - fops_start_timestamp);

/* Timestamp to use as aggregation key */
int64_t fops_key_timestamp;

/* Unique I/O session id sequence */
uint64_t sid;

/* Maps file descriptor to session id */
int64_t fd2sid[int];

/* Maps file descriptor to path */
string fd2path[int];

/* Open file count */
int64_t file_count;

/* Current I/O session id */
self int sid;

BEGIN
{
    fops_start_timestamp = timestamp;
    fops_key_timestamp = 0;
    fd2path[0] = "<stdin>";
    fd2path[1] = "<stdout>";
    fd2path[2] = "<stderr>";
}

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
    this->sid = (arg0 != -1)? ++sid : 0;
    fd2sid[arg0] = (arg0 != -1)? this->sid : 0;
    fd2path[arg0] = (arg0 != -1 && fds[arg0].fi_pathname != "<none>" && fds[arg0].fi_pathname != "<unknown>")? fds[arg0].fi_pathname : copyinstr(self->pathaddr);
    file_count = (arg0 != -1)? file_count + 1 : file_count;
    printf("%d %s %d \"%s\" %d %d", fops_timestamp, "open", this->sid, fd2path[arg0], 0, file_count);
    ustack();
    printf("\n");

    self->pathaddr = 0;
}

syscall::*read*:entry,
syscall::*write*:entry,
syscall::close*:entry
/pid == $1 && fd2path[arg0] == ""/
{
    fd2path[arg0] = fds[arg0].fi_pathname;
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
    @transfer[fops_key_timestamp, "read", self->sid, fd2path[self->fd], ustack()] = sum(arg1);

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
    @transfer[fops_key_timestamp, "write", self->sid, fd2path[self->fd], ustack()] = sum(arg1);

    self->fd = 0;
    self->sid = 0;
}

syscall::close*:entry
/pid == $1/
{
    self->fd = arg0;
    self->sid = fd2sid[arg0]? fd2sid[arg0] : -arg0-1;
}

syscall::close*:return
/pid == $1 && self->sid/
{
    file_count = (self->sid == -self->fd-1)? file_count : file_count - 1;
    printf("%d %s %d \"%s\" %d %d", fops_timestamp, "close", self->sid, fd2path[self->fd], 0, file_count);
    ustack();
    printf("\n");

    fd2sid[self->fd] = 0;
    fd2path[self->fd] = "";
    self->fd = 0;
    self->sid = 0;
}

tick-200ms, END
{
    printa("%d %s %d \"%s\" %@d -1 %k\n", @transfer);
    trunc(@transfer);
    fops_key_timestamp = fops_timestamp + fops_report_interval;
}

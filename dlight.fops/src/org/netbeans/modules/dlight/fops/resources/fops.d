#!/usr/sbin/dtrace -Cs
#pragma D option quiet
#pragma D option aggrate=200ms

/* Interval between read/write aggregation dumps, in milliseconds */
inline int64_t fops_report_interval = 200;

/* Script start timestamp */
uint64_t fops_start_timestamp;

/* Time since script start, in nanoseconds */
inline int64_t fops_timestamp = (timestamp - fops_start_timestamp);

/* Timestamp to use as aggregation key */
int64_t fops_key_timestamp;

/* Unique I/O session id sequence */
uint64_t sid_seq;

/* I/O session info */
typedef struct ioinfo {
    int64_t sid; /* session id */
    string dest; /* session destination (file path or socket address) */
} ioinfo_t;

/* Maps file descriptor to session info */
ioinfo_t fd2ioinfo[int];

/* Open file count */
int64_t file_count;

BEGIN
{
    fops_start_timestamp = timestamp;
    fops_key_timestamp = 0;
    fd2ioinfo[0].sid = -1;
    fd2ioinfo[0].dest = "<stdin>";
    fd2ioinfo[1].sid = -2;
    fd2ioinfo[1].dest = "<stdout>";
    fd2ioinfo[2].sid = -3;
    fd2ioinfo[2].dest = "<stderr>";
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
    this->sid = (arg0 != -1)? ++sid_seq : 0;
    this->dest = (arg0 != -1)? fds[arg0].fi_pathname : copyinstr(self->pathaddr);
    (arg0 != -1)? ++file_count : 0;
    (arg0 != -1)? fd2ioinfo[arg0].sid = this->sid : 0;
    (arg0 != -1)? fd2ioinfo[arg0].dest = this->dest : 0;

    printf("%d %s %d \"%s\" %d %d", fops_timestamp, "open", this->sid, this->dest, 0, file_count);
    ustack();
    printf("\n");

    self->pathaddr = 0;
}

syscall::*read*:entry,
syscall::*write*:entry,
syscall::close*:entry
/pid == $1 && !fd2ioinfo[arg0].sid && fds[arg0].fi_pathname != "<none>"/
{
    fd2ioinfo[arg0].sid = -arg0 - 1;
    fd2ioinfo[arg0].dest = fds[arg0].fi_fs == "sockfs"? "<socket>" : fds[arg0].fi_pathname;
}

syscall::*read*:entry,
syscall::*write*:entry,
syscall::close*:entry
/pid == $1/
{
    self->fd = arg0;
    self->sid = fd2ioinfo[arg0].sid;
}

syscall::*read*:return
/pid == $1 && self->sid/
{
    @transfer[fops_key_timestamp, "read", self->sid, fd2ioinfo[self->fd].dest, ustack()] = sum(0 < arg0? arg0 : 0);

    self->fd = 0;
    self->sid = 0;
}

syscall::*write*:return
/pid == $1 && self->sid/
{
    @transfer[fops_key_timestamp, "write", self->sid, fd2ioinfo[self->fd].dest, ustack()] = sum(0 < arg0? arg0 : 0);

    self->fd = 0;
    self->sid = 0;
}

syscall::close*:return
/pid == $1 && self->sid/
{
    (0 < self->sid)? --file_count : 0;
    printf("%d %s %d \"%s\" %d %d", fops_timestamp, "close", self->sid, fd2ioinfo[self->fd].dest, 0, file_count);
    ustack();
    printf("\n");

    fd2ioinfo[self->fd].sid = 0;
    fd2ioinfo[self->fd].dest = "";

    self->fd = 0;
    self->sid = 0;
}

tick-200ms, END
{
    printa("%d %s %d \"%s\" %@d -1 %k\n", @transfer);
    trunc(@transfer);
    fops_key_timestamp = fops_timestamp + fops_report_interval;
}

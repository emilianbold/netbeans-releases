#!/usr/sbin/dtrace -Zs

#pragma D option quiet

profile-100hz /pid == $1 && !self->vprev/
{
    self->vprev = vtimestamp;
}

profile-100hz /pid == $1 && self->vprev/
{
    this->vtime = vtimestamp;
    printf("%d\n", this->vtime - self->vprev);
    printf("%d %d %d", cpu, tid, timestamp);
    ustack();
    printf("\n"); /* empty line indicates end of ustack */
    self->vprev = this->vtime;
}

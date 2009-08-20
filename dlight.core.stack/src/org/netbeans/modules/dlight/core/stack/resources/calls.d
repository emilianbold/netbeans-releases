#!/usr/sbin/dtrace -Zs

#pragma D option quiet

profile-100hz, sched:::on-cpu
/pid == $1 && !self->vprev/
{
    self->vprev = vtimestamp;
}

profile-100hz, sched:::sleep, sched:::off-cpu
/pid == $1 && self->vprev/
{
    this->vtime = vtimestamp;
    printf("%d %d %d %d %d", timestamp, cpu, tid, curthread->t_mstate, this->vtime - self->vprev);
    ustack();
    printf("\n"); /* empty line indicates end of ustack */
    self->vprev = this->vtime;
}

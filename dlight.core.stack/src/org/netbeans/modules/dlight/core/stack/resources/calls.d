#!/usr/sbin/dtrace -Zs

#pragma D option quiet

/*
 * Previous vtimestamp the thread was seen on CPU
 */
self uint64_t vprev;

/* Current vtimestamp */
this uint64_t vcurr;

profile-100hz
/pid == $1 && !vprev[curlwpsinfo->pr_addr]/
{
    vprev[curlwpsinfo->pr_addr] = vtimestamp;
}

profile-100hz
/pid == $1/
{
    this->vcurr = vtimestamp;
    printf("%d %d %d %d %d %d", timestamp, cpu, tid, curthread->t_state, curthread->t_mstate, this->vcurr - self->vprev);
    ustack();
    printf("\n"); /* empty line indicates end of ustack */
    self->vprev = this->vcurr;
}

/* sched probes should catch thread in states other than running */
sched:::sleep, sched:::on-cpu, sched:::off-cpu
/pid == $1/
{
    printf("%d %d %d %d %d %d", timestamp, cpu, tid, curthread->t_state, curthread->t_mstate, 0);
    ustack();
    printf("\n"); /* empty line indicates end of ustack */
}

/* forge thread state to 0x10 (TS_STOPPED) to indicate thread start */
proc:::lwp-start
/pid == $1/
{
    printf("%d %d %d %d %d %d", timestamp, cpu, tid, 0x10 /*curthread->t_state*/, curthread->t_mstate, 0);
    ustack();
    printf("\n"); /* empty line indicates end of ustack */
}

/* forge thread state to 0x08 (TS_ZOMB) to indicate thread exit */
proc:::lwp-exit
/pid == $1/
{
    printf("%d %d %d %d %d %d", timestamp, cpu, tid, 0x08 /*curthread->t_state*/, curthread->t_mstate, 0);
    ustack();
    printf("\n"); /* empty line indicates end of ustack */
    vprev[curlwpsinfo->pr_addr] = 0;
}

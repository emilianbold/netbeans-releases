#!/usr/sbin/dtrace -ZCs

#pragma D option quiet
#define ts() (timestamp-starttime)
#define get_t_state() curthread->t_state == 0x00 ? "TS_FREE" : curthread->t_state == 0x01 ? "TS_SLEEP" : curthread->t_state == 0x02 ? "TS_RUN" : curthread->t_state == 0x04 ? "TS_ONPROC" : curthread->t_state == 0x08 ? "TS_ZOMB" : curthread->t_state == 0x10 ? "TS_STOPPED" : curthread->t_state == 0x20 ? "TS_WAIT" : ""
#define get_t_mstate() curthread->t_mstate == 0 ? "LMS_USER" : curthread->t_mstate == 1 ? "LMS_SYSTEM" : curthread->t_mstate == 2 ? "LMS_TRAP" : curthread->t_mstate == 3 ? "LMS_TFAULT" : curthread->t_mstate == 4 ? "LMS_DFAULT" : curthread->t_mstate == 5 ? "LMS_KFAULT" : curthread->t_mstate == 6 ? "LMS_USER_LOCK" : curthread->t_mstate == 7 ? "LMS_SLEEP" : curthread->t_mstate == 8 ? "LMS_WAIT_CPU" : curthread->t_mstate == 9 ? "LMS_STOPPED" : ""
#define get_pr_state() curlwpsinfo->pr_state == 1 ? "SSLEEP" : curlwpsinfo->pr_state == 2 ? "SRUN" : curlwpsinfo->pr_state == 3 ? "SZOMB" : curlwpsinfo->pr_state == 4 ? "SSTOP" : curlwpsinfo->pr_state == 5 ? "SIDL" : curlwpsinfo->pr_state == 6 ? "SONPROC" : curlwpsinfo->pr_state == 7 ? "SWAIT" : ""
#define get_pr_stype() curlwpsinfo->pr_stype == 0 ? "SOBJ_NONE" : curlwpsinfo->pr_stype == 1 ? "SOBJ_MUTEX" : curlwpsinfo->pr_stype == 2 ? "SOBJ_RWLOCK" : curlwpsinfo->pr_stype == 3 ? "SOBJ_CV" : curlwpsinfo->pr_stype == 4 ? "SOBJ_SEMA" : curlwpsinfo->pr_stype == 5 ? "SOBJ_USER" : curlwpsinfo->pr_stype == 6 ? "SOBJ_USER_PI" : curlwpsinfo->pr_stype == 7 ? "SOBJ_SHUTTLE" : ""
#define trace_calls 0

this uint64 starttime;
BEGIN {
  starttime=timestamp;
}

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
    printf("%d %d %d %d %d %d", ts(), cpu, tid, curthread->t_state, curthread->t_mstate, this->vcurr - self->vprev);
    ustack();
    printf("\n"); /* empty line indicates end of ustack */
    self->vprev = this->vcurr;
}

/* sched probes should catch thread in states other than running */
sched:::sleep
/pid == $1/
{
#if trace_calls
    printf("%d %d %d %d %d %d (%d-sleep MSA=%s State=%s Stat=%s Synch=%s)", ts(), cpu, tid, curthread->t_state, curthread->t_mstate, 0,
           tid, get_t_mstate(), get_t_state(), get_pr_state(), get_pr_stype());
#else
    printf("%d %d %d %d %d %d", ts(), cpu, tid, curthread->t_state, curthread->t_mstate, 0);
#endif
    /*
    */
    ustack();
    printf("\n"); /* empty line indicates end of ustack */
}

/* sched probes should catch thread in states other than running */
sched:::on-cpu
/pid == $1/
{
#if trace_calls
    printf("%d %d %d %d %d %d (%d-on-cpu MSA=%s State=%s Stat=%s Synch=%s)", ts(), cpu, tid, curthread->t_state, curthread->t_mstate, 0,
           tid, get_t_mstate(), get_t_state(), get_pr_state(), get_pr_stype());
#else
    printf("%d %d %d %d %d %d", ts(), cpu, tid, curthread->t_state, curthread->t_mstate, 0);
#endif
    ustack();
    printf("\n");
}

/* sched probes should catch thread in states other than running */
sched:::preempt
/pid == $1/
{
#if trace_calls
    printf("%d %d %d %d %d %d (%d-preempt MSA=%s State=%s Stat=%s Synch=%s)", ts(), cpu, tid, curthread->t_state, curthread->t_mstate, 0,
           tid, get_t_mstate(), get_t_state(), get_pr_state(), get_pr_stype());
#else
    printf("%d %d %d %d %d %d", ts(), cpu, tid, curthread->t_state, curthread->t_mstate, 0);
#endif
    ustack();
    printf("\n"); /* empty line indicates end of ustack */
}

sched:::off-cpu
/pid == $1/
{
#if trace_calls
    printf("%d %d %d %d %d %d (%d-off-cpu MSA=%s State=%s Stat=%s Synch=%s)", ts(), cpu, tid, curthread->t_state, curthread->t_mstate, 0,
           tid, get_t_mstate(), get_t_state(), get_pr_state(), get_pr_stype());
#else
    printf("%d %d %d %d %d %d", ts(), cpu, tid, curthread->t_state, curthread->t_mstate, 0);
#endif
    ustack();
    printf("\n");
}

/* forge thread state to 0x10 (TS_STOPPED) to indicate thread start */
proc:::lwp-start
/pid == $1/
{
#if trace_calls
    printf("%d %d %d %d %d %d (%d-lwp-start MSA=%s State=%s Stat=%s Synch=%s)", ts(), cpu, tid, 0x10, curthread->t_mstate, 0,
           tid, get_t_mstate(), get_t_state(), get_pr_state(), get_pr_stype());
#else
    printf("%d %d %d %d %d %d", ts(), cpu, tid, 0x10, curthread->t_mstate, 0);
#endif
    ustack();
    printf("\n"); /* empty line indicates end of ustack */
}

/* forge thread state to 0x08 (TS_ZOMB) to indicate thread exit */
proc:::lwp-exit
/pid == $1/
{
    printf("%d %d %d %d %d %d", ts(), cpu, tid, 0x08 /*curthread->t_state*/, curthread->t_mstate, 0);
    ustack();
    printf("\n"); /* empty line indicates end of ustack */
    vprev[curlwpsinfo->pr_addr] = 0;
}

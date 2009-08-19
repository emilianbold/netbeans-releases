#!/usr/sbin/dtrace -Cqs

#define COPY_ARRAY(dst, src) (dst[0]=src[0], dst[1]=src[1], dst[2]=src[2], dst[3]=src[3], dst[4]=src[4], dst[5]=src[5], dst[6]=src[6], dst[7]=src[7], dst[8]=src[8], dst[9]=src[9])
#define S(i) ((s[i]-info[ttid].states[i])*1000/delta)

struct threadinfo {
    int64_t lastts;
    int64_t past;
    int64_t states[10];
};

struct threadinfo info[int];
int64_t s[10];
int64_t w[8];
int64_t ttid;
int64_t delta;

int *p;
uint8_t endian_shift;
BEGIN {
   p=alloca(4);
   p[0]=0x01000000;
   endian_shift=*((char*)p)?0:8;
}

tick-10ms
/!t || t->t_procp->p_pidp->pid_id != $1/
{
       pidp = `pidhash[$1 & (`pid_hashsz - 1)];
       pidp = !pidp ? NULL : (pidp->pid_id == $1)                               ? pidp :
                             (pidp->pid_link->pid_id == $1)                     ? pidp->pid_link :
                             (pidp->pid_link->pid_link->pid_id == $1)           ? pidp->pid_link->pid_link :
                             (pidp->pid_link->pid_link->pid_link->pid_id == $1) ? pidp->pid_link->pid_link->pid_link : NULL;
       t = pidp ? `procdir[((*(uint32_t *)pidp) >> (endian_shift))&0xffffff].pe_proc->p_tlist : NULL;
       nextreport=timestamp;
}

tick-10ms
/t && t->t_procp->p_pidp->pid_id == $1 && timestamp > nextreport/
{
       ttid      = t->t_tid;
       cur_mstate= t->t_mstate;
       COPY_ARRAY(s, t->t_lwp->lwp_mstate.ms_acct);

       now = s[0]+s[1]+s[2]+s[3]+s[4]+s[5]+s[6]+s[7]+s[8]+s[9];
       delta = now - info[ttid].past;
       info[ttid].past = now;

       /* the following two are needed to accomodate long persisting states */
       info[ttid].states[cur_mstate] -= !delta;
       delta = (delta || (t->t_state & 0x6)) ? delta : 1;

       nextreport = (t->t_tid < t->t_forw->t_tid) ? nextreport : nextreport + 500*1000*1000;

       t = t->t_forw;
}

tick-10ms
/delta && info[ttid].lastts/
{
       /* the following two let us properly attribute OTHER WAIT */
       wi=(t->t_sobj_ops?t->t_sobj_ops->sobj_type:1);
       w[wi]=S(7);

       printf("%d %d %d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d\n", cpu, ttid, info[ttid].lastts,
              S(0), S(1), S(2), S(3), S(4), S(5), S(8), S(9), S(6), w[0], w[1], w[2], w[3], w[4], w[5], w[6], w[7]);

}

tick-10ms
/delta/
{
       w[wi] = 0;
       COPY_ARRAY(info[ttid].states, s);
       delta = 0;
       info[ttid].lastts = timestamp;
}
/*
proc:::lwp-exit
{
      printf("%d %d %d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d\n", cpu, ttid, timestamp,
                          0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0);
}
*/
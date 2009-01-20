#!/usr/sbin/dtrace -s

#pragma D option quiet

profile-100 /pid == $1/
{
    printf("cpu=%d, thread=%d, time=%d, pc=%x", cpu, tid, walltimestamp, uregs[R_PC]);
    ustack();
    printf("\n"); /* empty line indicates end of ustack */
}

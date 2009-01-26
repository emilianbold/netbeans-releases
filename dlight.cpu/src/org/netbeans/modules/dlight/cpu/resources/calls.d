#!/usr/sbin/dtrace -s

#pragma D option quiet

profile-100 /pid == $1/
{
    printf("%d %d %d %x", cpu, tid, timestamp, uregs[R_PC]);
    ustack();
    printf("\n"); /* empty line indicates end of ustack */
}

#!/usr/sbin/dtrace -wqZCs
#pragma D option defaultargs

#define ts() (timestamp - BSS_starttime)

BEGIN {
  BSS_starttime = ($2==0) ? timestamp : $2;
  system("/usr/bin/prun %d", $1);
}  

syscall::fork*:return
/ppid == $1 && arg1 == 0 && __FORK_FOLLOW_CONDITION__/
{
   stop();
   system("__DLIGHT_DSCRIPT__ %d %d&", pid, BSS_starttime);
}

proc:::exit
/pid==$1/
{
   exit(0);
}

END { 
   /* printf("\n%d __EOF_MARKER__\n", $1); */
}
#!/usr/sbin/dtrace -wqs
int BSS_waitpid;
int BSS_active;
uint64 BSS_starttime;

BEGIN {
  BSS_waitpid = $1;
  BSS_active=1;
  BSS_starttime=timestamp;
  system("prun %d", $1);
}

proc:::exec-success
__DLIGHT_PREDICATE__
{
   BSS_active=0;
   BSS_waitpid=pid;
   stop();
   system("__DLIGHT_DSCRIPT__ %d %d | tee -a /tmp/XX.log", pid, BSS_starttime);
}

proc:::exit
/pid==BSS_waitpid/
{
   BSS_waitpid=$1;
   BSS_active=1;
}

proc:::exit
/pid==$1/
{
   printf("__EOF_MARKER__\n");
   exit(0);
}

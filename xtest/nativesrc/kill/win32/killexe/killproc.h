// killproc.h
//
// Termination of processes.
//
// $Id$
//

#ifndef __killproc_h_included
#define __killproc_h_included

BOOL
WINAPI
KillProcess(
	IN DWORD dwProcessId
	);

BOOL
WINAPI
KillProcessEx(
	IN DWORD dwProcessId,
	IN BOOL bTree
	);

#endif // __killproc_h_included

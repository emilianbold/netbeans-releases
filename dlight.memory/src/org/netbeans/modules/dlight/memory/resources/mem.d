#!/usr/sbin/dtrace -ZCs
#pragma D option quiet
#pragma D option bufsize=16m
#pragma D option switchrate=100hz
#pragma D option dynvarsize=16m
#pragma D option cleanrate=100hz

/* the current total amount of memory allocated and not freed */
int total;

/* maps address to the allocated size */
int heap[int64_t];

/* currently requested size  */
self int size;

/* prevents from cpunting twice on recursion (e.g calloc calls malloc, etc) */
self int inside;

BEGIN {
	total = 0;
}

#ifdef NOSTACK
#define PRINT_STACK() 
#else
#define PRINT_STACK() \
	ustack(); \
	printf("\n");
#endif

#define ALLOC_ENTRY(_sz) \
	self->size = _sz; \
	/*printf(">> size=%d", _sz); \*/ \
	/*ustack();*/

#define ALLOC_EXIT(_addr) \
	heap[_addr] = self->size; \
	total += self->size; \
	/*printf("## alloc timestamp %d size %d address %x total %d", timestamp, self->size, _addr, total); */\
	printf("%d 1 %d %d %d", timestamp, self->size, _addr, total); \
	/*printf("\nAlloc size=%d addr=%x", self->size, _addr);*/ \
	PRINT_STACK(); \
	self->size = 0;

#define FREE_ENTRY(_addr) \
	this->freed = heap[_addr]; \
	heap[_addr] = 0; \
	total -= this->freed; \
	/*printf("## free timestamp %d size %d  address %x total %d", timestamp, this->freed, _addr, total);*/ \
	printf("%d -1 %d %d %d", timestamp, this->freed, _addr, total); \
	/*printf("\nFree size=%d addr=%x", this->freed, _addr);*/ \
	PRINT_STACK();


/*
By default $1 is the pid of process to trace.
But you can change this via -DPID=pid\$target -
and use -c or -p dtrace command line option
*/
#ifndef PID
#define PID pid$1
#endif

/*---------------------------------------------------
NB: the pattern
    pid$1::*malloc:entry  / probefunc=="malloc" / { ...
is a workaround for DTrace bug #6806913
---------------------------------------------------*/

/*---------- malloc ----------*/

PID::*malloc:entry
/ !self->inside && probefunc=="malloc" /
{
	self->inside++;
	ALLOC_ENTRY(arg0);
}

PID::*malloc:return
/ self->inside && probefunc=="malloc" /
{
	ALLOC_EXIT(arg1);
	self->inside--;
}

/*---------- calloc ----------*/

PID::*calloc:entry
/ !self->inside && probefunc=="calloc" /
{
	self->inside++;
	ALLOC_ENTRY(arg0 * arg1);
}

PID::*calloc:return
/ self->inside && probefunc=="calloc" /
{
	ALLOC_EXIT(arg1);
	self->inside--;
}

/*---------- memalign ----------*/

PID::*memalign:entry
/ !self->inside && probefunc=="memalign" /
{
	self->inside++;
	ALLOC_ENTRY(arg1);
}

PID::*memalign:return
/ self->inside && probefunc=="memalign" /
{
	ALLOC_EXIT(arg1);
	self->inside--;
}

/*---------- valloc ----------*/

PID::*valloc:entry
/ ! self->inside && probefunc=="valloc" /
{
	self->inside++;
	ALLOC_ENTRY(arg0);
}

PID::*valloc:return
/ self->inside && probefunc=="valloc" /
{
	ALLOC_EXIT(arg1);
	self->inside--;
}


/*---------- realloc ----------*/

PID::*realloc:entry
/ !self->inside && probefunc=="realloc" /
{
	self->inside++;
	FREE_ENTRY(arg0); /* arg0 - address */
	ALLOC_ENTRY(arg1); /* arg1 - size */
}

PID::*realloc:return
/ self->inside && probefunc=="realloc" /
{
	ALLOC_EXIT(arg1); /* arg1 - address */
	self->inside--;
}

/*---------- free ----------*/


PID::*free:entry
/ probefunc=="free" /
{
	FREE_ENTRY(arg0);
}

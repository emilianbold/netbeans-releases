#!/usr/sbin/dtrace -ZCs

#pragma D option quiet

/*
 * Macro usage
 * DEBUG 
 * 	0 - for production mode - no debug info is printed
 *	1 - debug info is printed
 *	2 - extra debug info is printed (e.g. each plockstat probe is traced)
 * STATISTICS
 *	0 - no statistics
 *	1 - statistics is gathered and printed upon dtrace script exit
 * QUIET 
 *	0 - for production mode
 *	1 - no output for each particular blocker (make sense in conjunction with STATISTICS)
 */

typedef uint64_t address;

typedef int bool;
enum { false = 0, true = 1 };

#ifdef DEBUG
inline string prefix = "### SYNC ";
#endif

/* mutex address -> timestamp of the moment the blocked state started */
self uint64_t mutex_block_start[address];

/* mutex address -> 1 if some thread is blocked on it, otherwise 0 */
bool has_waiters[address];

/* mutex address -> thread that holds the mutex */
id_t blocker_threads[address];

/* timestamp of BEGIN probe - used as default thread start timestamp */
uint64_t dtrace_start_timestamp;

self uint64_t thread_wait_time;
self uint64_t thread_start;

#if STATISTICS
long wait_count;
long wait_time;
inline int stats_denominator = 1000;
#endif

BEGIN {
	dtrace_start_timestamp = timestamp;
}

plockstat$1:::mutex-block,
plockstat$1:::rw-block
{
	self->mutex_block_start[arg0] = timestamp;
#if DEBUG
	printf("%s thread %d starts waiting on %x blocked by %d\n", prefix, tid, arg0, blocker_threads[arg0]);
	ustack();
#endif
	has_waiters[arg0] = tid;
}

plockstat$1:::mutex-acquire,
plockstat$1:::rw-acquire
/ arg1 == 0 &&  self->mutex_block_start[arg0] /
{
	this->time = timestamp - self->mutex_block_start[arg0];
	self->mutex_block_start[arg0] = 0;
	has_waiters[arg0] = 0;
	self->thread_wait_time += this->time;
	this->thread_duration = timestamp - (self->thread_start ? self->thread_start : dtrace_start_timestamp);
#if DEBUG
	printf("%s thread %d waited on %x blocked by thread %d for %d mks  total thread wait %d, duration %d\n",
		prefix, tid, arg0, blocker_threads[arg0], this->time/1000, self->thread_wait_time, this->thread_duration);
#endif
#if ! QUIET
	printf("%d %d %d %d %d %d %d", timestamp, tid, arg0, blocker_threads[arg0], this->time, self->thread_wait_time, this->thread_duration);
	ustack();
	printf("\n");
#endif
#if STATISTICS
	@wait_time_dist[0] = quantize(this->time/stats_denominator);
	wait_time += this->time/stats_denominator;
	wait_count++;
#endif
	blocker_threads[arg0] = tid;
}

plockstat$1:::mutex-acquire,
plockstat$1:::rw-acquire
/ arg1 == 0 &&  ! self->mutex_block_start[arg0] /
{
	blocker_threads[arg0] = tid;
}

plockstat$1:::mutex-release,
plockstat$1:::rw-release
/ arg1 == 0 && has_waiters[arg0] /
{
	blocker_threads[arg0] = 0;
#if DEBUG
	printf("%s thread %d released %x\n", prefix, tid, arg0);
	ustack();
#endif	
}

plockstat$1:::mutex-release,
plockstat$1:::rw-release
/ arg1 == 0 && ! has_waiters[arg0] /
{
	blocker_threads[arg0] = 0;
}

pid$1:libc:pthread_barrier_wait:entry
{
	self->barrier_wait_start = timestamp;
}

pid$1:libc:pthread_barrier_wait:return
/ self->barrier_wait_start /
{
	this->time = timestamp - self->barrier_wait_start;
	self->thread_wait_time += this->time;
	this->thread_duration = timestamp - (self->thread_start ? self->thread_start : dtrace_start_timestamp);
#if ! QUIET
	printf("%d %d %d %d %d %d %d", timestamp, tid, arg0, 0, this->time, self->thread_wait_time, this->thread_duration);
	ustack();
	printf("\n");
#endif
}

proc:::lwp-start
/pid == $1/
{
	self->thread_start = timestamp;
#if DEBUG
	printf("started thread %d\n", tid);
#endif
}

proc:::lwp-exit
/pid == $1/
{
	this->thread_duration = timestamp - self->thread_start;
#if DEBUG
	printf("finished thread %d duration %d ns wait time %d ns\n", tid, this->thread_duration, self->thread_wait_time);
#endif
}

/*plockstat$1:::mutex-blocked {}*/
/*plockstat$1:::mutex-spin{}*/
/*plockstat$1:::mutex-spun {}*/
/*plockstat$1:::mutex-error {} */


/* pid$1:libc:printf:entry{ self->in_printf = 1; } */
/* pid$1:libc:printf:return { self->in_printf = 0; } */

#if STATISTICS
END {
	printa(@wait_time_dist);
	printf("Total wait count %d\n", wait_count);
	printf("Total wait time %d\n", wait_time);
}
#endif

#if DEBUG > 1
plockstat$1:::*
{
	printf("probe %s thread %x arg0=%x arg1=%x arg2=%x arg3=%x arg4=%x\n", probefunc, tid, arg0, arg1, arg2, arg3, arg4);
	ustack();
}
#endif

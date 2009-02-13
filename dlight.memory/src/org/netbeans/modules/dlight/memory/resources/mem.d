#!/usr/sbin/dtrace -Cs
#pragma D option quiet

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

#define PRINT_STACK() \
	printf("%d %d %d", cpu, tid, timestamp); \
	ustack(); \
	printf("\n"); \
	

#define ALLOC_ENTRY(_sz) \
	self->size = _sz; \
	/*printf(">> size=%d", _sz); \*/ \
	/*ustack();*/

#define ALLOC_EXIT(_addr) \
	heap[_addr] = self->size; \
	total += self->size; \
	/*printf("## alloc timestamp %d size %d address %x total %d", timestamp, self->size, _addr, total); */\
	printf("%d 1 %d %d %d\n", timestamp, self->size, _addr, total); \
	/*printf("\nAlloc size=%d addr=%x", self->size, _addr);*/ \
	PRINT_STACK(); \
	self->size = 0;

#define FREE_ENTRY(_addr) \
	this->freed = heap[_addr]; \
	heap[_addr] = 0; \
	total -= this->freed; \
	/*printf("## free timestamp %d size %d  address %x total %d", timestamp, this->freed, _addr, total);*/ \
	printf("%d -1 %d %d %d\n", timestamp, this->freed, _addr, total); \
	/*printf("\nFree size=%d addr=%x", this->freed, _addr);*/ \
	PRINT_STACK();


/*---------- malloc ----------*/

pid$1::malloc:entry
/ !self->inside /
{
	self->inside++;
	ALLOC_ENTRY(arg0);
}

pid$1::malloc:return
/ self->inside /
{
	ALLOC_EXIT(arg1);
	self->inside--;
}

/*---------- calloc ----------*/

pid$1::calloc:entry
/ !self->inside /
{
	self->inside++;
	ALLOC_ENTRY(arg0 * arg1);
}

pid$1::calloc:return
/ self->inside /
{
	ALLOC_EXIT(arg1);
	self->inside--;
}

/*---------- memalign ----------*/

pid$1::memalign:entry
/ !self->inside /
{
	self->inside++;
	ALLOC_ENTRY(arg1);
}

pid$1::memalign:return
/ self->inside /
{
	ALLOC_EXIT(arg1);
	self->inside--;
}

/*---------- valloc ----------*/

pid$1::valloc:entry
/ ! self->inside /
{
	self->inside++;
	ALLOC_ENTRY(arg0);
}

pid$1::valloc:return
/ self->inside /
{
	ALLOC_EXIT(arg1);
	self->inside--;
}


/*---------- realloc ----------*/

pid$1::realloc:entry
/ !self->inside /
{
	self->inside++;
	FREE_ENTRY(arg0); /* arg0 - address */
	ALLOC_ENTRY(arg1); /* arg1 - size */
}

pid$1::realloc:return
/ self->inside /
{
	ALLOC_EXIT(arg1); /* arg1 - address */
	self->inside--;
}

/*---------- free ----------*/


pid$1::free:entry
{
	FREE_ENTRY(arg0);
}

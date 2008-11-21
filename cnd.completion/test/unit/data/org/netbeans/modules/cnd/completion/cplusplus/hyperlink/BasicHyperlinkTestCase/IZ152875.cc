#define	DTRACE_PROBE2(provider, name, arg1, arg2) {			\
	void __dtrace_##provider##___##name(unsigned long,	\
	    unsigned long);						\
	__dtrace_##provider##___##name((unsigned long)arg1,		\
	    (unsigned long)arg2);					\
}

static int
mutex_trylock_adaptive(int plockstat, int mutex__spun) {
    int count = 0;
    if (count) {
        DTRACE_PROBE2(plockstat, mutex__spun, 0, count);
    }
}

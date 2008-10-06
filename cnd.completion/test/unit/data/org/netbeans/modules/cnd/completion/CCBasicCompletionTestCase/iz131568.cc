static	void	*Realloc(void *, size_t);

static void foo_11(void *p) {
     // CC here
    Realloc(p, 32);
}

static	void	*Realloc(void * ptr, size_t size) {

}

static union {
    int * restrict p1 ;
    int * __restrict p3 ;
};

static void restrictTestFoo1(int *restrict a)
{
    
}

static void restrictTestFoo2()
{
    int * restrict p2;
    
     //
}

#define A 79

#ifdef NOWAY
#if A >100
        int na100;
#elif A >50
        int na50;
#elif A> 20
        int na20;
#else
        int nb;
#endif        
#elif !NOWAY
#if A > 100
        int a100;
#elif A > 50
        int a50;
#elif A> 20
        int a20;
#else
        int b;
#endif        
#else 
        int nowayatall;
#endif

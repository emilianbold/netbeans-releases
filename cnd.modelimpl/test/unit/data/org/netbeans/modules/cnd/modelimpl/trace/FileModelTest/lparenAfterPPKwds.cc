#if(defined(A))
int a;
#elseif(defined(B))
int b;
#elif(defined(C))
int c;
#else\
/*else*/
int d;
#endif/*end*/

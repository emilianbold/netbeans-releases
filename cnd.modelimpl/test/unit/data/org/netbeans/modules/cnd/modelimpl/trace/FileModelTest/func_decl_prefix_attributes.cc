// this is simplified exaple from GL library (gl.h) for gcc version >= 3.3 
#  define GLAPI __attribute__((visibility("default")))
#  define GLAPIENTRY

GLAPI void GLAPIENTRY glFunction( int red, int green, int blue );

// example from IZ#149506
__attribute__((format(printf,0,1))) static void foo_149506() {}

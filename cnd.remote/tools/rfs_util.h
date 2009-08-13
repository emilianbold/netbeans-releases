#include <stdio.h>

enum {
    true = 1,
    false = 0
};

#if TRACE
FILE *trace_file = stderr;
void trace_startup(const char* env_var) {
    char *file_name = getenv(env_var);
    //fprintf(stderr, "trace_startup:\n%s=%s\n", env_var, (file_name ? file_name : "NULL"));
    if (file_name) {        
        trace_file = fopen(file_name, "a");
        if (trace_file) {
            fprintf(stderr, "Redirecting trace to %s\n", file_name);
            fprintf(trace_file, "\n\n--------------------\n");
            fflush(trace_file);
        } else {
            fprintf(stderr, "Redirecting trace to %s failed.\n", file_name);
            trace_file = stderr;
        }
    }
}
void trace_shutdown() {
    if (trace_file && trace_file != stderr) {
        fclose(trace_file);
    }
}
#define trace(args...) { fprintf(trace_file, "!RFS> "); fprintf(trace_file, ## args); fflush(trace_file); }
#else
#define trace_startup(...)
#define trace(...) 
#define trace_shutdown()
#endif

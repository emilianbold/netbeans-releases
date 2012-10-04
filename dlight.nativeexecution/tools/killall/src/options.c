#include "error.h"
#include "options.h"
#include <string.h>
#include <stdlib.h>
#include <signal.h>

extern int str2sig(const char *name, int *sig_ret);

static void setsig(options_t* params, char* opt, char* sigstr) {
    if (sigstr == NULL || sigstr[0] == '\0') {
        err_quit("missing signal after %s\n", opt);
    }
    if (str2sig(sigstr, &params->sig) == -1) {
        err_quit("Unknown signal %s\n", sigstr);
    }
}

int readopts(int argc, char** argv, options_t* opts) {
    int idx;
    int nopt = 1;

    memset(opts, 0, sizeof (options_t));

    for (idx = 1; idx < argc; idx++) {
        if (argv[idx][0] == '-') {
            if (strcmp(argv[idx], "-p") == 0) {
                opts->scope = S_PID;
                setsig(opts, argv[idx], argv[idx + 1]);
                idx++;
                nopt += 2;
            } else if (strcmp(argv[idx], "-g") == 0) {
                opts->scope = S_PGID;
                setsig(opts, argv[idx], argv[idx + 1]);
                idx++;
                nopt += 2;
            } else if (strcmp(argv[idx], "-s") == 0) {
                opts->scope = S_SID;
                setsig(opts, argv[idx], argv[idx + 1]);
                idx++;
                nopt += 2;
            } else if (strcmp(argv[idx], "-q") == 0) {
                opts->scope = P_QUEUE;
                setsig(opts, argv[idx], argv[idx + 1]);
                idx++;
                nopt += 2;
            } else if (strcmp(argv[idx], "-m") == 0) {
                idx++;
                if (argv[idx] == NULL || argv[idx][0] == '\0') {
                    err_quit("missing value after %s\n", argv[idx - 1]);
                }
                opts->magicenv = argv[idx];
                nopt += 2;
            } else {
                printf("ERROR unrecognized option '%s'\n", argv[idx]);
                exit(-1);
            }
        } else {
            break;
        }
    }

    return nopt;
}

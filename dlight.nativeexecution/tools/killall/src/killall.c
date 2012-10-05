#include <signal.h>
#include <sys/types.h>
#include "killall.h"

const char* progname;

#ifndef SOLARIS 

typedef struct signame {
    const char *sigstr;
    const int signum;
} signame_t;

static signame_t signames[] = {
    { "EXIT", 0},
    { "HUP", SIGHUP},
    { "INT", SIGINT},
    { "QUIT", SIGQUIT},
    { "ILL", SIGILL},
    { "TRAP", SIGTRAP},
    { "ABRT", SIGABRT},
    { "BUS", SIGBUS},
    { "FPE", SIGFPE},
    { "KILL", SIGKILL},
    { "USR1", SIGUSR1},
    { "SEGV", SIGSEGV},
    { "USR2", SIGUSR2},
    { "PIPE", SIGPIPE},
    { "ALRM", SIGALRM},
    { "TERM", SIGTERM},
    { "CHLD", SIGCHLD},
    { "CONT", SIGCONT},
    { "STOP", SIGSTOP},
    { "TSTP", SIGTSTP},
    { "TTIN", SIGTTIN},
    { "TTOU", SIGTTOU},
    { "URG", SIGURG},
    { "XCPU", SIGXCPU},
    { "XFSZ", SIGXFSZ},
    { "VTALRM", SIGVTALRM},
    { "PROF", SIGPROF},
    { "WINCH", SIGWINCH},
    { "IO", SIGIO},
    { "SYS", SIGSYS}
};

#define SIGCNT  (sizeof (signames) / sizeof (struct signame))

int str2sig(const char *name, int *sig_ret) {
    signame_t* sp;
    for (sp = signames; sp < &signames[SIGCNT]; sp++) {
        if (strcmp(sp->sigstr, name) == 0) {
            *sig_ret = sp->signum;
            return 0;
        }
    }
    return -1;
}
#endif    

static int sendsignal(sigscope_t scope, int id, int sig) {
    switch (scope) {
        case S_PID:
            kill((pid_t) id, sig);
            break;
        case S_PGID:
            killpg((pid_t) id, sig);
            break;
#ifdef SOLARIS
        case S_SID:
            sigsend(P_SID, (id_t) id, sig);
#endif
            // not supported on other systems?
            break;
    }
}

int main(int argc, char** argv) {
    options_t params;
    int nopt;

    // Get program name - this is used in error.c, for example
    progname = basename(argv[0]);

    // Parse options
    nopt = readopts(argc, argv, &params);
    argv += nopt;
    argc -= nopt;
    if (argc == 0) {
        err_quit("\n\nusage: %s -p|-g|-s signal_name [-m env] id\n"
                "\t-p\t\tsend signal signal_name to a process with the specified id\n"
                "\t-g\t\tsend signal signal_name to all processes with the specified process group ID\n"
                "\t-s\t\tsend signal signal_name to all processes with the specified session ID\n"
                "\t-m\t\tin addition find all processes that have env entry in their environment and send the signal to them\n"
                "\nusage: %s -q signal_name pid value\n"
                "\t-q\t\tsignal process with the given signal and integer value attached.\n",
                progname, progname);
    }

    params.id = atoi(argv[0]);

    if (params.id <= 0) {
        err_quit("Wrong ID: %s", argv[0]);
    }

    if (params.scope == P_QUEUE) {
        if (argc < 2) {
            err_quit("value is expected for sigqueue");
        }
#ifndef MACOSX
        union sigval value;
        value.sival_int = atoi(argv[1]);
        return sigqueue(params.id, params.sig, value);
#else
        // unsupported on Mac?
        return sendsignal(S_PID, params.id, params.sig);
#endif        
    }

    sendsignal(params.scope, params.id, params.sig);

    return 0;
}

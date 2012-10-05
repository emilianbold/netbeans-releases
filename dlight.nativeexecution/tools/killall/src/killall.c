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
#ifdef SIGABRT
    {"SIGABRT", SIGABRT},
#endif
#ifdef SIGALRM
    {"SIGALRM", SIGALRM},
#endif
#ifdef SIGBUS
    {"SIGBUS", SIGBUS},
#endif
#ifdef SIGCANCEL
    {"SIGCANCEL", SIGCANCEL},
#endif
#ifdef SIGCHLD
    {"SIGCHLD", SIGCHLD},
#endif
#ifdef SIGCLD
    {"SIGCLD", SIGCLD},
#endif
#ifdef SIGCONT
    {"SIGCONT", SIGCONT},
#endif
#ifdef SIGEMT
    {"SIGEMT", SIGEMT},
#endif
#ifdef SIGFPE
    {"SIGFPE", SIGFPE},
#endif
#ifdef SIGFREEZE
    {"SIGFREEZE", SIGFREEZE},
#endif
#ifdef SIGHUP
    {"SIGHUP", SIGHUP},
#endif
#ifdef SIGILL
    {"SIGILL", SIGILL},
#endif
#ifdef SIGINFO
    {"SIGINFO", SIGINFO},
#endif
#ifdef SIGINT
    {"SIGINT", SIGINT},
#endif
#ifdef SIGIO
    {"SIGIO", SIGIO},
#endif
#ifdef SIGIOT
    {"SIGIOT", SIGIOT},
#endif
#ifdef SIGJVM1
    {"SIGJVM1", SIGJVM1},
#endif
#ifdef SIGJVM2
    {"SIGJVM2", SIGJVM2},
#endif
#ifdef SIGKILL
    {"SIGKILL", SIGKILL},
#endif
#ifdef SIGLOST
    {"SIGLOST", SIGLOST},
#endif
#ifdef SIGLWP
    {"SIGLWP", SIGLWP},
#endif
#ifdef SIGPIPE
    {"SIGPIPE", SIGPIPE},
#endif
#ifdef SIGPOLL
    {"SIGPOLL", SIGPOLL},
#endif
#ifdef SIGPROF
    {"SIGPROF", SIGPROF},
#endif
#ifdef SIGPWR
    {"SIGPWR", SIGPWR},
#endif
#ifdef SIGQUIT
    {"SIGQUIT", SIGQUIT},
#endif
#ifdef SIGSEGV
    {"SIGSEGV", SIGSEGV},
#endif
#ifdef SIGSTKFLT
    {"SIGSTKFLT", SIGSTKFLT},
#endif
#ifdef SIGSTOP
    {"SIGSTOP", SIGSTOP},
#endif
#ifdef SIGSYS
    {"SIGSYS", SIGSYS},
#endif
#ifdef SIGTERM
    {"SIGTERM", SIGTERM},
#endif
#ifdef SIGTHAW
    {"SIGTHAW", SIGTHAW},
#endif
#ifdef SIGTRAP
    {"SIGTRAP", SIGTRAP},
#endif
#ifdef SIGTSTP
    {"SIGTSTP", SIGTSTP},
#endif
#ifdef SIGTTIN
    {"SIGTTIN", SIGTTIN},
#endif
#ifdef SIGTTOU
    {"SIGTTOU", SIGTTOU},
#endif
#ifdef SIGURG
    {"SIGURG", SIGURG},
#endif
#ifdef SIGUSR1
    {"SIGUSR1", SIGUSR1},
#endif
#ifdef SIGUSR2
    {"SIGUSR2", SIGUSR2},
#endif
#ifdef SIGVTALRM
    {"SIGVTALRM", SIGVTALRM},
#endif
#ifdef SIGWAITING
    {"SIGWAITING", SIGWAITING},
#endif
#ifdef SIGWINCH
    {"SIGWINCH", SIGWINCH},
#endif
#ifdef SIGXCPU
    {"SIGXCPU", SIGXCPU},
#endif
#ifdef SIGXFSZ
    {"SIGXFSZ", SIGXFSZ},
#endif
#ifdef SIGXRES
    {"SIGXRES", SIGXRES},
#endif
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
            return kill((pid_t) id, sig);
        case S_PGID:
            return killpg((pid_t) id, sig);
#ifdef SOLARIS
        case S_SID:
            return sigsend(P_SID, (id_t) id, sig);
#endif
            // not supported on other systems?
            return -1;
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

    return sendsignal(params.scope, params.id, params.sig);
}

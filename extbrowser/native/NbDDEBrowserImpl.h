#ifndef __NBDDEBROWSERIMPL_INCLUDED__
#define __NBDDEBROWSERIMPL_INCLUDED__

extern "C" {

struct ddesrv {
    char *name;
    char *topic;
};
typedef struct ddesrv ddesrv_t;


DWORD WINAPI BrowserThread (LPVOID param);

} // extern "C"

#endif /* __NBDDEBROWSERIMPL_INCLUDED__ */
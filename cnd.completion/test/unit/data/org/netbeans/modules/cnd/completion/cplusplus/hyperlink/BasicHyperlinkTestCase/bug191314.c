typedef enum { IR_FALSE, IR_TRUE } BOOLEAN;
typedef BOOLEAN (hashtravfunc)(void *fromPtr, void **toPtr, void *udata);
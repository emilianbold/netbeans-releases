/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

#include <jvmpi.h>
#include <string.h>

static JavaVM *jvm;
static JNIEnv *env;
static JVMPI_Interface *jvmpi;

extern "C" JNIEXPORT jint JNICALL JVM_OnLoad(JavaVM *jvm, char *options, void *reserved);
static void notifyEvent(JVMPI_Event *event);

typedef struct {
    jmethodID method_id;
    const char *class_name;
    const char *method_name;
    const char *method_sig;
} mentry_t;

static mentry_t* allMethods[10000 * 100];
static int allMethodsLength = 0;

const mentry_t* lookupMethodID(jmethodID mid) {
    for (int i = 0; i < allMethodsLength; i++) {
        if (allMethods[i]->method_id == mid)
            return allMethods[i];
    }
    return NULL;
}

void storeNewMethodID(jmethodID mid, const char* classname, const char* mname, const char* msig) {
    mentry_t* e = new mentry_t;
    e->method_id = mid;
    e->class_name = strdup(classname);
    e->method_name = strdup(mname);
    e->method_sig = strdup(msig);
    allMethods[allMethodsLength++] = e;
}

JNIEXPORT jint JNICALL JVM_OnLoad(JavaVM *aJvm, char *options, void *reserved) {
    fprintf(stderr, "jtrace> %s\n", "initializing .....");

    jvm = aJvm;

    if (jvm->GetEnv((void**)&env, JNI_VERSION_1_2)) {
        fprintf(stderr, "jtrace> %s\n", "error in obtaining JNI interface pointer");
        return JNI_ERR;
    }
    
    if ((jvm->GetEnv((void **)&jvmpi, JVMPI_VERSION_1)) < 0) {
        fprintf(stderr, "jtrace> %s\n", "error in obtaining JVMPI interface pointer");
        return JNI_ERR;
    }

    jvmpi->NotifyEvent = notifyEvent;
    
    jvmpi->EnableEvent(JVMPI_EVENT_CLASS_LOAD, NULL);
    jvmpi->EnableEvent(JVMPI_EVENT_METHOD_ENTRY2, NULL);

    fprintf(stderr, "jtrace> %s\n", ".... ok\n");
    return JNI_OK;
}

void notifyEvent(JVMPI_Event *event) {
    switch (event->event_type) {
        case JVMPI_EVENT_CLASS_LOAD: 
            fprintf(stderr, "jtrace> loaded %s\n", event->u.class_load.class_name);
            for (int i = 0; i < event->u.class_load.num_methods; i++) {
                JVMPI_Method *m = & event->u.class_load.methods[i];
//                fprintf(stderr, "jtrace>   %s%s\n", m->method_name, m->method_signature);
                if (0 == strcmp(m->method_name, "propertyChange")
                    && 0 == strcmp(m->method_signature, "(Ljava/beans/PropertyChangeEvent;)V"))
                    storeNewMethodID(m->method_id, event->u.class_load.class_name,
                                     m->method_name, m->method_signature);
            }
            break;

        case JVMPI_EVENT_METHOD_ENTRY2:
            jmethodID mid = event->u.method_entry2.method_id;
            jobjectID obj = event->u.method_entry2.obj_id;

            const mentry_t* e = lookupMethodID(mid);
//             if (e == NULL) {
//                 jobjectID classid = jvmpi->GetMethodClass(mid);
//                 jint res = jvmpi->RequestEvent(JVMPI_EVENT_CLASS_LOAD, classid);
//                 if (res != JVMPI_SUCCESS) {
//                     // warning
//                 }
//                 e = lookupMethodID(mid);
//             }
            if (e != NULL) {
                fprintf(stderr, "jtrace> entered %s.%s%s\n", e->class_name, e->method_name, e->method_sig);
            }
            break;
    }
}

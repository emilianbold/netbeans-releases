/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;

/**
 *
 * @author Martin Entlicher
 */
public class RemoteServices {
    public static enum ServiceType {
        AWT, FX
    }
    
    private static final Logger logger = Logger.getLogger(RemoteServices.class.getName());
    
    private static final String REMOTE_CLASSES_ZIPFILE = "/org/netbeans/modules/debugger/jpda/visual/resources/debugger-remote.zip";
    
    private static final Map<JPDADebugger, ClassObjectReference> remoteServiceClasses = new WeakHashMap<JPDADebugger, ClassObjectReference>();
    
    private static final RequestProcessor AUTORESUME_AFTER_SUSPEND_RP = new RequestProcessor("Autoresume after suspend", 1);
    
    private static final Set<PropertyChangeListener> serviceListeners = new WeakSet<PropertyChangeListener>();

    private RemoteServices() {}
    
    public static void addServiceListener(PropertyChangeListener listener) {
        synchronized (serviceListeners) {
            serviceListeners.add(listener);
        }
    }

    private static void fireServiceClass(JPDADebuggerImpl debugger) {
        PropertyChangeEvent pche = new PropertyChangeEvent(RemoteServices.class, "serviceClass", null, debugger);
        PropertyChangeListener[] listeners;
        synchronized (serviceListeners) {
            listeners = serviceListeners.toArray(new PropertyChangeListener[]{});
        }
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(pche);
        }
    }
    
    public static ClassObjectReference uploadBasicClasses(JPDAThreadImpl t, ServiceType sType) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, IOException, PropertyVetoException {
        ThreadReference tawt = t.getThreadReference();
        VirtualMachine vm = tawt.virtualMachine();
        
        ReferenceType threadType = tawt.referenceType();
        Method getContextCl = ((ClassType)threadType).concreteMethodByName("getContextClassLoader", "()Ljava/lang/ClassLoader;");
        
        ObjectReference cl = (ObjectReference)tawt.invokeMethod(tawt, getContextCl, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
        t.notifyMethodInvoking();
        try {
            ClassType classLoaderClass = null;
            if (cl != null) {
                classLoaderClass = (ClassType)cl.referenceType();
            } else {
                classLoaderClass = getClass(vm, ClassLoader.class.getName());
                Method getSystemClassLoader = classLoaderClass.concreteMethodByName("getSystemClassLoader", "()Ljava/lang/ClassLoader;");
                cl = (ObjectReference) classLoaderClass.invokeMethod(tawt, getSystemClassLoader, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
            }

            List<RemoteClass> remoteClasses = getRemoteClasses();
            ClassObjectReference basicClass = null;
            for (RemoteClass rc : remoteClasses) {
                if ((sType == ServiceType.AWT && rc.name.contains("AWT")) ||
                    (sType == ServiceType.FX && rc.name.contains("FX"))) {
                    ClassObjectReference theUploadedClass = null;
                    ArrayReference byteArray = createTargetBytes(vm, rc.bytes);
                    StringReference nameMirror = null;
                    try {
                        Method defineClass = classLoaderClass.concreteMethodByName("defineClass", "(Ljava/lang/String;[BII)Ljava/lang/Class;");
                        boolean uploaded = false;
                        while (!uploaded) {
                            nameMirror = vm.mirrorOf(rc.name);
                            try {
                                nameMirror.disableCollection();
                                uploaded = true;
                            } catch (ObjectCollectedException ocex) {
                                // Just collected, try again...
                            }
                        }
                        uploaded = false;
                        while (!uploaded) {
                            theUploadedClass = (ClassObjectReference) cl.invokeMethod(tawt, defineClass, Arrays.asList(nameMirror, byteArray, vm.mirrorOf(0), vm.mirrorOf(rc.bytes.length)), ObjectReference.INVOKE_SINGLE_THREADED);
                            if (basicClass == null && rc.name.indexOf('$') < 0) {
                                try {
                                    // Disable collection only of the basic class
                                    theUploadedClass.disableCollection();
                                    basicClass = theUploadedClass;
                                    uploaded = true;
                                } catch (ObjectCollectedException ocex) {
                                    // Just collected, try again...
                                }
                            } else {
                                uploaded = true;
                            }
                        }
                    } finally {
                        byteArray.enableCollection(); // We can dispose it now
                        if (nameMirror != null) {
                            nameMirror.enableCollection();
                        }
                    }
                }
                //Method resolveClass = classLoaderClass.concreteMethodByName("resolveClass", "(Ljava/lang/Class;)V");
                //systemClassLoader.invokeMethod(tawt, resolveClass, Arrays.asList(theUploadedClass), ObjectReference.INVOKE_SINGLE_THREADED);
            }
            if (basicClass != null) {
                // Initialize the class:
                ClassType theClass = getClass(vm, Class.class.getName());
                // Perhaps it's not 100% correct, we should be calling the new class' newInstance() method, not Class.newInstance() method.
                Method newInstance = theClass.concreteMethodByName("newInstance", "()Ljava/lang/Object;");
                ObjectReference newInstanceOfBasicClass = (ObjectReference) basicClass.invokeMethod(tawt, newInstance, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                synchronized (remoteServiceClasses) {
                    remoteServiceClasses.put(t.getDebugger(), basicClass);
                }
                fireServiceClass(t.getDebugger());
            }
            return basicClass;
        } finally {
            t.notifyMethodInvokeDone();
        }
    }
    
    public static void uploadAllClasses(JPDAThreadImpl t) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, IOException {
        ThreadReference tawt = t.getThreadReference();
        VirtualMachine vm = tawt.virtualMachine();
        ClassType classLoaderClass = getClass(vm, ClassLoader.class.getName());
        Method getSystemClassLoader = classLoaderClass.concreteMethodByName("getSystemClassLoader", "()Ljava/lang/ClassLoader;");
        ObjectReference systemClassLoader = (ObjectReference) classLoaderClass.invokeMethod(tawt, getSystemClassLoader, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
        List<RemoteClass> remoteClasses = getRemoteClasses();
        for (RemoteClass rc : remoteClasses) {
            ArrayReference byteArray = createTargetBytes(vm, rc.bytes);
            try {
                Method defineClass = classLoaderClass.concreteMethodByName("defineClass", "(Ljava/lang/String;[BII)Ljava/lang/Class;");
                systemClassLoader.invokeMethod(tawt, defineClass, Arrays.asList(vm.mirrorOf(rc.name), byteArray, vm.mirrorOf(0), vm.mirrorOf(rc.bytes.length)), ObjectReference.INVOKE_SINGLE_THREADED);
            } finally {
                byteArray.enableCollection(); // We can dispose it now
            }
        }
    }
    
    private static void runOnBreakpoint(final JPDAThread awtThread, String bpClass, String bpMethod, final Runnable runnable, final CountDownLatch latch) {
        final MethodBreakpoint mb = MethodBreakpoint.create(bpClass, bpMethod);
        final JPDADebugger dbg = ((JPDAThreadImpl)awtThread).getDebugger();
        
        mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
        mb.setSuspend(MethodBreakpoint.SUSPEND_EVENT_THREAD);
        mb.setHidden(true);
        mb.addJPDABreakpointListener(new JPDABreakpointListener() {
            @Override
            public void breakpointReached(JPDABreakpointEvent event) {
                try {
                    if (dbg.equals(event.getDebugger())) {
                        DebuggerManager.getDebuggerManager().removeBreakpoint(mb);
                        try {
                            ((JPDAThreadImpl)awtThread).notifyMethodInvoking();
                            runnable.run();
                        } catch (PropertyVetoException e) {
                        } finally {
                            ((JPDAThreadImpl)awtThread).notifyMethodInvokeDone();
                        }
                    }
                } finally {
                    event.resume();
                    latch.countDown();
                }
            }
        });
        DebuggerManager.getDebuggerManager().addBreakpoint(mb);
    }
    
    private static final Map<JPDAThread, RequestProcessor.Task> tasksByThreads = new WeakHashMap<JPDAThread, RequestProcessor.Task> ();
    
    /**
     * Run the provided runnable after the thread is assured to be stopped on an event.
     * If the thread was initially running, it's resumed with some delay
     * (to allow another execution of runOnStoppedThread() without the expensive thread preparation).
     * It's assumed that the runnable will invoke methods on the thread.
     * Therefore method invoke notification methods are executed automatically.
     * @param thread The remote thread.
     * @param run The Runnable that is executed when the thread is assured to be stopped on an event.
     * @throws PropertyVetoException when can not invoke methods.
     */
    public static void runOnStoppedThread(JPDAThread thread, final Runnable run, ServiceType sType) throws PropertyVetoException {
        final JPDAThreadImpl t = (JPDAThreadImpl) thread;
        boolean wasSuspended = true;
        
        t.accessLock.writeLock().lock();
        try {
            ThreadReference threadReference = t.getThreadReference();
            wasSuspended = t.isSuspended();
            if (t.isSuspended() && !threadReference.isAtBreakpoint()) {
                // TODO: Suspended, but will not be able to invoke methods
                
            }
            if (!t.isSuspended()) {
                final CountDownLatch latch = new CountDownLatch(1);
                t.accessLock.writeLock().unlock();
                switch(sType) {
                    case AWT: {
                        runOnBreakpoint(
                            thread, 
                            "org.netbeans.modules.debugger.jpda.visual.remote.RemoteAWTService", // NOI18N
                            "calledInAWT", // NOI18N
                            run,
                            latch
                        );
                        VirtualMachine vm = ((JPDAThreadImpl) thread).getThreadReference().virtualMachine();
                        ClassObjectReference serviceClassObject;
                        synchronized (remoteServiceClasses) {
                            serviceClassObject = remoteServiceClasses.get(((JPDAThreadImpl) thread).getDebugger());
                        }
                        ClassType serviceClass = (ClassType) serviceClassObject.reflectedType();//getClass(vm, "org.netbeans.modules.debugger.jpda.visual.remote.RemoteService");
                        Field awtAccess = serviceClass.fieldByName("awtAccess"); // NOI18N
                        try {
                            serviceClass.setValue(awtAccess, vm.mirrorOf(true));
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        break;
                    }
                    case FX: {
                        runOnBreakpoint(
                            thread,
                            "org.netbeans.modules.debugger.jpda.visual.remote.RemoteFXService", // NOI18N
                            "access", // NOI18N
                            run,
                            latch
                        );
                        break;
                    }
                }
                try {
                    // wait for the async operation to finish
                    latch.await();
                    t.accessLock.writeLock().lock();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                RequestProcessor.Task autoresumeTask;
                if (!wasSuspended) {
                    AutoresumeTask resumeTask = new AutoresumeTask(t);
                    autoresumeTask = AUTORESUME_AFTER_SUSPEND_RP.create(resumeTask);
                    synchronized (tasksByThreads) {
                        tasksByThreads.put(thread, autoresumeTask);
                    }
                } else {
                    synchronized (tasksByThreads) {
                        autoresumeTask = tasksByThreads.get(thread);
                    }
                }
                t.notifyMethodInvoking();
                if (autoresumeTask != null) {
                    autoresumeTask.schedule(Integer.MAX_VALUE); // wait for run.run() to finish...
                }
                try {
                    run.run();
                } finally {
                    t.notifyMethodInvokeDone();
                    if (autoresumeTask != null) {
                        autoresumeTask.schedule(AutoresumeTask.WAIT_TIME);
                    }
                }
            }
        } finally {
            t.accessLock.writeLock().unlock();
        }
    }
    
    public static List<RemoteListener> getAttachedListeners(final JavaComponentInfo ci) throws PropertyVetoException {
        final List<RemoteListener> rlisteners = new ArrayList<RemoteListener>();
        final JPDAThreadImpl thread = ci.getThread();
        final ObjectReference component = ci.getComponent();
        runOnStoppedThread(thread, new Runnable() {
            @Override
            public void run() {
                if (ci instanceof RemoteAWTScreenshot.AWTComponentInfo) {
                    retrieveAttachedListeners(thread, component, rlisteners);
                } else {
                    retrieveAttachedFXListeners(thread, component, rlisteners);
                }
            }
        }, (ci instanceof RemoteAWTScreenshot.AWTComponentInfo) ? ServiceType.AWT : ServiceType.FX);
        return rlisteners;
    }
        
    private static void retrieveAttachedListeners(JPDAThreadImpl thread, ObjectReference component, List<RemoteListener> rlisteners) {
        ThreadReference t = thread.getThreadReference();
        ReferenceType clazz = component.referenceType();
        List<Method> visibleMethods = clazz.visibleMethods();
        for (Method m : visibleMethods) {
            String name = m.name();
            if (!name.startsWith("get") || !name.endsWith("Listeners")) {
                continue;
            }
            if (m.argumentTypeNames().size() > 0) {
                continue;
            }
            Value result;
            try {
                result = component.invokeMethod(t, m, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                continue;
            }
            String listenerType = null;
            try {
                Type returnType = m.returnType();
                if (returnType instanceof ArrayType) {
                    ArrayType art = (ArrayType) returnType;
                    listenerType = art.componentTypeName();
                }
            } catch (ClassNotLoadedException ex) {
                continue;
            }
            if (listenerType == null) {
                continue;
            }
            ArrayReference array = (ArrayReference) result;
            List<Value> listeners = array.getValues();
            for (Value v : listeners) {
                RemoteListener rl = new RemoteListener(listenerType, (ObjectReference) v);
                rlisteners.add(rl);
            }
        }
    }
    
    private static void retrieveAttachedFXListeners(JPDAThreadImpl thread, ObjectReference component, List<RemoteListener> rlisteners) {
        ThreadReference t = thread.getThreadReference();
        ReferenceType clazz = component.referenceType();
        List<Method> visibleMethods = clazz.visibleMethods();
        for (Method m : visibleMethods) {
            String name = m.name();
            if (!name.startsWith("getOn")) {
                continue;
            }
            if (m.argumentTypeNames().size() > 0) {
                continue;
            }
            Value result;
            try {
                result = component.invokeMethod(t, m, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                if (result == null) {
                    continue;
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                continue;
            }
            String listenerType = null;
            try {
                Type returnType = m.returnType();
                if (returnType.name().equals("javafx.event.EventHandler")) {
                    listenerType = name.substring(5);
                }
//                if (returnType instanceof ArrayType) {
//                    ArrayType art = (ArrayType) returnType;
//                    listenerType = art.componentTypeName();
//                }
            } catch (ClassNotLoadedException ex) {
                continue;
            }
            if (listenerType == null) {
                continue;
            }
            RemoteListener rl = new RemoteListener(listenerType, (ObjectReference)result);
            rlisteners.add(rl);
//            ArrayReference array = (ArrayReference) result;
//            List<Value> listeners = array.getValues();
//            for (Value v : listeners) {
//                RemoteListener rl = new RemoteListener(listenerType, (ObjectReference) v);
//                rlisteners.add(rl);
//            }
        }
    }
    
    public static List<ReferenceType> getAttachableListeners(JavaComponentInfo ci) {
        ObjectReference component = ci.getComponent();
        ReferenceType clazz = component.referenceType();
        List<Method> visibleMethods = clazz.visibleMethods();
        List<ReferenceType> listenerClasses = new ArrayList<ReferenceType>();
        for (Method m : visibleMethods) {
            String name = m.name();
            if (!name.startsWith("add") || !name.endsWith("Listener")) {
                continue;
            }
            List<Type> argTypes;
            try {
                argTypes = m.argumentTypes();
            } catch (ClassNotLoadedException ex) {
                continue;
            }
            if (argTypes.size() != 1) {
                continue;
            }
            Type t = argTypes.get(0);
            if (!(t instanceof ReferenceType)) {
                continue;
            }
            ReferenceType rt = (ReferenceType) t;
            String lname = rt.name();
            int i = lname.lastIndexOf('.');
            if (i < 0) i = 0;
            else i++;
            int ii = lname.lastIndexOf('$', i);
            if (ii > i) i = ii + 1;
            System.err.println("  getAttachableListeners() '"+name.substring(3)+"' should equal to '"+lname.substring(i)+"', lname = "+lname+", i = "+i);
            if (!name.substring(3).equals(lname.substring(i))) {
                // addXXXListener() method name does not match XXXListener simple class name.
                // TODO: Perhaps check removeXXXListener method instead of this.
                continue;
            }
            listenerClasses.add(rt);
        }
        return listenerClasses;
    }
    
    public static ObjectReference attachLoggingListener(final JavaComponentInfo ci,
                                                        final ClassObjectReference listenerClass,
                                                        final LoggingListenerCallBack listener) throws PropertyVetoException {
        final JPDAThreadImpl thread = ci.getThread();
        final ObjectReference[] listenerPtr = new ObjectReference[] { null };
        runOnStoppedThread(thread, new Runnable() {
            @Override
            public void run() {
                ThreadReference t = thread.getThreadReference();
                ObjectReference component = ci.getComponent();
                final MethodBreakpoint mb = MethodBreakpoint.create("org.netbeans.modules.debugger.jpda.visual.remote.RemoteAWTService", "calledWithEventsData");
                mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
                mb.setSuspend(MethodBreakpoint.SUSPEND_EVENT_THREAD);
                mb.setHidden(true);
                //final Object bpLock = new Object();
                mb.addJPDABreakpointListener(new JPDABreakpointListener() {
                    @Override
                    public void breakpointReached(JPDABreakpointEvent event) {
                        //synchronized (bpLock) {
                            //DebuggerManager.getDebuggerManager().removeBreakpoint(mb);
                        try {
                            ThreadReference tr = ((JPDAThreadImpl) event.getThread()).getThreadReference();
                            StackFrame topFrame;
                            try {
                                topFrame = tr.frame(0);
                            } catch (IncompatibleThreadStateException ex) {
                                Exceptions.printStackTrace(ex);
                                return;
                            }
                            List<Value> argumentValues = topFrame.getArgumentValues();
                            //System.err.println("LoggingListener breakpoint reached: argumentValues = "+argumentValues);
                            if (argumentValues.size() < 2) {  // ERROR: BP is hit somehere else
                                return;
                            }
                            if (!ci.getComponent().equals(argumentValues.get(0))) {
                                // Reported for some other component
                                return;
                            }
                            ArrayReference allDataArray = (ArrayReference) argumentValues.get(1);
                            int totalLength = allDataArray.length();
                            List<Value> dataValues = allDataArray.getValues();
                            String[] eventProps = null;
                            for (int i = 0; i < totalLength; ) {
                                StringReference sr = (StringReference) dataValues.get(i);
                                String dataLengthStr = sr.value();
                                //System.err.println("  data["+i+"] = "+dataLengthStr);
                                int dataLength = Integer.parseInt(dataLengthStr);
                                String[] data = new String[dataLength];
                                i++;
                                for (int j = 0; j < dataLength; j++, i++) {
                                    sr = (StringReference) dataValues.get(i);
                                    data[j] = sr.value();
                                    //System.err.println("  data["+i+"] = "+data[j]);
                                }
                                if (eventProps == null) {
                                    eventProps = data;
                                } else {
                                    listener.eventsData(ci, eventProps, data/*stack*/);
                                    eventProps = null;
                                }
                            }
                        } finally {
                            event.resume();
                        }
                    }
                });
                DebuggerManager.getDebuggerManager().addBreakpoint(mb);
                VirtualMachine vm = t.virtualMachine();
                ClassObjectReference serviceClassObject;
                synchronized (remoteServiceClasses) {
                    serviceClassObject = remoteServiceClasses.get(thread.getDebugger());
                }
                ClassType serviceClass = (ClassType) serviceClassObject.reflectedType();//getClass(vm, "org.netbeans.modules.debugger.jpda.visual.remote.RemoteService");
                Method addLoggingListener = serviceClass.concreteMethodByName("addLoggingListener", "(Ljava/awt/Component;Ljava/lang/Class;)Ljava/lang/Object;");
                try {
                    ObjectReference theListener = (ObjectReference)
                            serviceClass.invokeMethod(t, addLoggingListener, Arrays.asList(component, listenerClass), ObjectReference.INVOKE_SINGLE_THREADED);
                    listenerPtr[0] = theListener;
                } catch (InvalidTypeException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ClassNotLoadedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IncompatibleThreadStateException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, ServiceType.AWT);
        return listenerPtr[0];
    }
    
    public static boolean detachLoggingListener(final JavaComponentInfo ci,
                                                final ClassObjectReference listenerClass,
                                                final ObjectReference listener) throws PropertyVetoException {
        final JPDAThreadImpl thread = ci.getThread();
        final boolean[] retPtr = new boolean[] { false };
        runOnStoppedThread(thread, new Runnable() {
            @Override
            public void run() {
                ObjectReference component = ci.getComponent();
                ThreadReference t = thread.getThreadReference();
                ClassObjectReference serviceClassObject;
                synchronized (remoteServiceClasses) {
                    serviceClassObject = remoteServiceClasses.get(thread.getDebugger());
                }
                ClassType serviceClass = (ClassType) serviceClassObject.reflectedType();
                Method removeLoggingListener = serviceClass.concreteMethodByName("removeLoggingListener", "(Ljava/awt/Component;Ljava/lang/Class;Ljava/lang/Object;)Z");
                try {
                    BooleanValue success = (BooleanValue)
                            serviceClass.invokeMethod(t, removeLoggingListener, Arrays.asList(component, listenerClass, listener), ObjectReference.INVOKE_SINGLE_THREADED);
                    retPtr[0] = success.value();
                } catch (InvalidTypeException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ClassNotLoadedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IncompatibleThreadStateException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, ServiceType.AWT);
        return retPtr[0];
    }
    
    public static ClassObjectReference getServiceClass(JPDADebugger debugger) {
        synchronized (remoteServiceClasses) {
            return remoteServiceClasses.get(debugger);
        }
        
    }
    
    static ClassType getClass(VirtualMachine vm, String name) {
        List<ReferenceType> classList = vm.classesByName(name);
        ReferenceType clazz = null;
        for (ReferenceType c : classList) {
            if (c.classLoader() == null) {
                clazz = c;
                break;
            }
        }
        return (ClassType) clazz;
    }
    
    static ArrayType getArrayClass(VirtualMachine vm, String name) {
        List<ReferenceType> classList = vm.classesByName(name);
        ReferenceType clazz = null;
        for (ReferenceType c : classList) {
            if (c.classLoader() == null) {
                clazz = c;
                break;
            }
        }
        return (ArrayType) clazz;
    }
    
    private static List<RemoteClass> getRemoteClasses() throws IOException {
        InputStream in = RemoteServices.class.getResourceAsStream(REMOTE_CLASSES_ZIPFILE);
        try {
            ZipInputStream zin = new ZipInputStream(in);
            ZipEntry ze;
            List<RemoteClass> rcl = new ArrayList<RemoteClass>();
            while((ze = zin.getNextEntry()) != null) {
                String fileName = ze.getName();
                if (!fileName.endsWith(".class")) continue;
                String name = fileName.substring(0, fileName.length() - ".class".length());
                int baseStart = name.lastIndexOf('/');
                if (baseStart < 0) continue;
                baseStart++;
                int baseEnd = name.indexOf('$', baseStart);
                if (baseEnd < 0) {
                    baseEnd = name.length();
                }
                RemoteClass rc = new RemoteClass();
                rc.name = name.replace('/', '.');
                int l = (int) ze.getSize();
                byte[] bytes = new byte[l];
                int num = 0;
                while (num < l) {
                    int r = zin.read(bytes, num, l - num);
                    if (r < 0) {
                        Exceptions.printStackTrace(new IllegalStateException("Can not read full content of "+name+" entry. Length = "+l+", read num = "+num));
                        break;
                    }
                    num += r;
                }
                rc.bytes = bytes;
                rcl.add(rc);
            }
            return rcl;
        } finally {
            in.close();
        }
    }
    
    private static ArrayReference createTargetBytes(VirtualMachine vm, byte[] bytes) throws InvalidTypeException, ClassNotLoadedException {
        ArrayType bytesArrayClass = getArrayClass(vm, "byte[]");
        ArrayReference array = null;
        boolean disabledCollection = false;
        while (!disabledCollection) {
            array = bytesArrayClass.newInstance(bytes.length);
            try {
                array.disableCollection();
                disabledCollection = true;
            } catch (ObjectCollectedException ocex) {
                // Collected too soon, try again...
            }
        }
        for (int i = 0; i < bytes.length; i++) {
            array.setValue(i, vm.mirrorOf(bytes[i]));
        }
        return array;
    }
    
    private static class RemoteClass {
        private String name;
        private byte[] bytes;
    }
    
    public static class RemoteListener {
        
        private String type;
        //private String classType;
        private ObjectReference l;
        
        public RemoteListener(String type, ObjectReference l) {
            this.type = type;
            this.l = l;
        }
        
        public String getType() {
            return type;
        }
        
        //public String getClassType() {
        //    return classType;
        //}
        
        public ObjectReference getListener() {
            return l;
        }
    }
    
    public static interface LoggingListenerCallBack {
        
        public void eventsData(JavaComponentInfo ci, String[] data, String[] stack);
        
    }
    
    private static class AutoresumeTask implements Runnable, PropertyChangeListener {
        
        private static final int WAIT_TIME = 500;
        
        private volatile JPDAThreadImpl t;

        public AutoresumeTask(JPDAThreadImpl t) {
            this.t = t;
            t.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JPDAThreadImpl thread = this.t;
            if (thread == null) return ;
            if (JPDAThread.PROP_SUSPENDED.equals(evt.getPropertyName()) &&
                !"methodInvoke".equals(evt.getPropagationId())) {               // NOI18N
                
                thread.removePropertyChangeListener(this);
                logger.fine("AutoresumeTask: autoresume canceled, thread changed suspended state: suspended = "+thread.isSuspended());
                synchronized (tasksByThreads) {
                    tasksByThreads.remove(thread);
                }
                t = null;
            }
        }
        
        @Override
        public void run() {
            JPDAThreadImpl thread = this.t;
            this.t = null;
            if (thread != null) {
                thread.removePropertyChangeListener(this);
                thread.resume();
            }
        }
    }

}

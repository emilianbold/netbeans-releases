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
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Entlicher
 */
public class RemoteServices {
    
    private static final String REMOTE_CLASSES_ZIPFILE = "/org/netbeans/modules/debugger/jpda/visual/resources/debugger-remote.zip";
    private static final String[] BASIC_REMOTE_CLASSES = new String[] { "RemoteService", "RemoteService$AWTAccessLoop" };
    private static final String BASIC_REMOTE_CLASS = "RemoteService"; // NOI18N
    private static final String REMOTE_PACKAGE = "org.netbeans.modules.debugger.jpda.visual.remote"; // NOI18N
    
    private static final Map<JPDAThreadImpl, ClassObjectReference> remoteServiceClasses = new WeakHashMap<JPDAThreadImpl, ClassObjectReference>();

    private RemoteServices() {}
    
    /*
    public static void uploadClass(JPDAThreadImpl t, String className) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, IOException {
        ThreadReference tawt = t.getThreadReference();
        VirtualMachine vm = tawt.virtualMachine();
        ClassType classLoaderClass = getClass(vm, ClassLoader.class.getName());
        Method getSystemClassLoader = classLoaderClass.concreteMethodByName("getSystemClassLoader", "()Ljava/lang/ClassLoader;");
        ObjectReference systemClassLoader = (ObjectReference) classLoaderClass.invokeMethod(tawt, getSystemClassLoader, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
        byte[] bytes = getRemoteClassCode(className);
        ArrayReference byteArray = createTargetBytes(vm, bytes);
        Method defineClass = classLoaderClass.concreteMethodByName("defineClass", "(Ljava/lang/String;[BII)Ljava/lang/Class;");
        ClassObjectReference theUploadedClass = (ClassObjectReference) systemClassLoader.invokeMethod(tawt, defineClass, Arrays.asList(vm.mirrorOf("javauidbg.TestContainerListener"), byteArray, vm.mirrorOf(0), vm.mirrorOf(bytes.length)), ObjectReference.INVOKE_SINGLE_THREADED);
        
        // l = c.newInstance();
        ClassType theClass = getClass(vm, Class.class.getName());
        // Perhaps it's not 100% correct, we should be calling the new class' newInstance() method, not Class.newInstance() method.
        Method newInstance = theClass.concreteMethodByName("newInstance", "()Ljava/lang/Object;");
        ObjectReference theListener = (ObjectReference) theUploadedClass.invokeMethod(tawt, newInstance, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);

    }
     */
    
    public static ClassObjectReference uploadBasicClasses(JPDAThreadImpl t) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, IOException {
        ThreadReference tawt = t.getThreadReference();
        VirtualMachine vm = tawt.virtualMachine();
        ClassType classLoaderClass = getClass(vm, ClassLoader.class.getName());
        Method getSystemClassLoader = classLoaderClass.concreteMethodByName("getSystemClassLoader", "()Ljava/lang/ClassLoader;");
        ObjectReference systemClassLoader = (ObjectReference) classLoaderClass.invokeMethod(tawt, getSystemClassLoader, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
        List<RemoteClass> remoteClasses = getRemoteClasses(BASIC_REMOTE_CLASS, true);
        ClassObjectReference basicClass = null;
        for (RemoteClass rc : remoteClasses) {
            ArrayReference byteArray = createTargetBytes(vm, rc.bytes);
            Method defineClass = classLoaderClass.concreteMethodByName("defineClass", "(Ljava/lang/String;[BII)Ljava/lang/Class;");
            ClassObjectReference theUploadedClass = (ClassObjectReference) systemClassLoader.invokeMethod(tawt, defineClass, Arrays.asList(vm.mirrorOf(rc.name), byteArray, vm.mirrorOf(0), vm.mirrorOf(rc.bytes.length)), ObjectReference.INVOKE_SINGLE_THREADED);
            //Method resolveClass = classLoaderClass.concreteMethodByName("resolveClass", "(Ljava/lang/Class;)V");
            //systemClassLoader.invokeMethod(tawt, resolveClass, Arrays.asList(theUploadedClass), ObjectReference.INVOKE_SINGLE_THREADED);
            if (basicClass == null && rc.name.indexOf('$') < 0 && rc.name.endsWith(BASIC_REMOTE_CLASS)) {
                basicClass = theUploadedClass;
            }
        }
        if (basicClass != null) {
            // Initialize the class:
            ClassType theClass = getClass(vm, Class.class.getName());
            // Perhaps it's not 100% correct, we should be calling the new class' newInstance() method, not Class.newInstance() method.
            Method newInstance = theClass.concreteMethodByName("newInstance", "()Ljava/lang/Object;");
            ObjectReference newInstanceOfBasicClass = (ObjectReference) basicClass.invokeMethod(tawt, newInstance, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
            synchronized (remoteServiceClasses) {
                remoteServiceClasses.put(t, basicClass);
            }
        }
        return basicClass;
        /*
        // l = c.newInstance();
        ClassType theClass = getClass(vm, Class.class.getName());
        // Perhaps it's not 100% correct, we should be calling the new class' newInstance() method, not Class.newInstance() method.
        Method newInstance = theClass.concreteMethodByName("newInstance", "()Ljava/lang/Object;");
        ObjectReference theListener = (ObjectReference) theUploadedClass.invokeMethod(tawt, newInstance, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
        */
    }
    
    public static void uploadAllClasses(JPDAThreadImpl t) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException, IOException {
        ThreadReference tawt = t.getThreadReference();
        VirtualMachine vm = tawt.virtualMachine();
        ClassType classLoaderClass = getClass(vm, ClassLoader.class.getName());
        Method getSystemClassLoader = classLoaderClass.concreteMethodByName("getSystemClassLoader", "()Ljava/lang/ClassLoader;");
        ObjectReference systemClassLoader = (ObjectReference) classLoaderClass.invokeMethod(tawt, getSystemClassLoader, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
        boolean isBasicLoaded = getClass(vm, REMOTE_PACKAGE+'.'+BASIC_REMOTE_CLASS) != null;
        String basicRemoteClass = isBasicLoaded ? null : BASIC_REMOTE_CLASS;
        List<RemoteClass> remoteClasses = getRemoteClasses(basicRemoteClass, false);
        for (RemoteClass rc : remoteClasses) {
            ArrayReference byteArray = createTargetBytes(vm, rc.bytes);
            Method defineClass = classLoaderClass.concreteMethodByName("defineClass", "(Ljava/lang/String;[BII)Ljava/lang/Class;");
            systemClassLoader.invokeMethod(tawt, defineClass, Arrays.asList(vm.mirrorOf(rc.name), byteArray, vm.mirrorOf(0), vm.mirrorOf(rc.bytes.length)), ObjectReference.INVOKE_SINGLE_THREADED);
        }
    }
    
    public static JPDAThread makeAWTThreadStopOnEvent(JPDAThread awtThread) {
        final MethodBreakpoint mb = MethodBreakpoint.create("org.netbeans.modules.debugger.jpda.visual.remote.RemoteService", "calledInAWT");
        mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
        mb.setSuspend(MethodBreakpoint.SUSPEND_EVENT_THREAD);
        mb.setHidden(true);
        final Object bpLock = new Object();
        mb.addJPDABreakpointListener(new JPDABreakpointListener() {
            @Override
            public void breakpointReached(JPDABreakpointEvent event) {
                synchronized (bpLock) {
                    DebuggerManager.getDebuggerManager().removeBreakpoint(mb);
                    bpLock.notifyAll();
                }
            }
        });
        DebuggerManager.getDebuggerManager().addBreakpoint(mb);
        VirtualMachine vm = ((JPDAThreadImpl) awtThread).getThreadReference().virtualMachine();
        ClassObjectReference serviceClassObject;
        synchronized (remoteServiceClasses) {
            serviceClassObject = remoteServiceClasses.get((JPDAThreadImpl) awtThread);
        }
        ClassType serviceClass = (ClassType) serviceClassObject.reflectedType();//getClass(vm, "org.netbeans.modules.debugger.jpda.visual.remote.RemoteService");
        Field awtAccess = serviceClass.fieldByName("awtAccess");
        try {
            serviceClass.setValue(awtAccess, vm.mirrorOf(true));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            DebuggerManager.getDebuggerManager().removeBreakpoint(mb);
            return awtThread;
        }
        synchronized (bpLock) {
            try {
                bpLock.wait();
            } catch (InterruptedException ex) {
            }
        }
        return awtThread;
    }
    
    public static List<RemoteListener> getAttachedListeners(RemoteScreenshot.ComponentInfo ci) {
        JPDAThreadImpl thread = ci.getAWTThread();
        ThreadReference t = thread.getThreadReference();
        ObjectReference component = ci.getComponent();
        ReferenceType clazz = component.referenceType();
        List<Method> visibleMethods = clazz.visibleMethods();
        List<RemoteListener> rlisteners = new ArrayList<RemoteListener>();
        Lock l = thread.accessLock.writeLock();
        l.lock();
        try {
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
            return rlisteners;
        } finally {
            l.unlock();
        }
    }
    
    public static List<ReferenceType> getAttachableListeners(RemoteScreenshot.ComponentInfo ci) {
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
    
    public static ObjectReference attachLoggingListener(final RemoteScreenshot.ComponentInfo ci,
                                                        ClassObjectReference listenerClass,
                                                        final LoggingListenerCallBack listener) {
        JPDAThreadImpl thread = ci.getAWTThread();
        ThreadReference t = thread.getThreadReference();
        ObjectReference component = ci.getComponent();
        Lock l = thread.accessLock.writeLock();
        l.lock();
        try {
            final MethodBreakpoint mb = MethodBreakpoint.create("org.netbeans.modules.debugger.jpda.visual.remote.RemoteService", "calledWithEventsData");
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
                            listener.eventsData(ci, data);
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
                serviceClassObject = remoteServiceClasses.get((JPDAThreadImpl) thread);
            }
            ClassType serviceClass = (ClassType) serviceClassObject.reflectedType();//getClass(vm, "org.netbeans.modules.debugger.jpda.visual.remote.RemoteService");
            Method addLoggingListener = serviceClass.concreteMethodByName("addLoggingListener", "(Ljava/awt/Component;Ljava/lang/Class;)Ljava/lang/Object;");
            try {
                ObjectReference theListener = (ObjectReference)
                        serviceClass.invokeMethod(t, addLoggingListener, Arrays.asList(component, listenerClass), ObjectReference.INVOKE_SINGLE_THREADED);
                return theListener;
            } catch (InvalidTypeException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ClassNotLoadedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IncompatibleThreadStateException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        } finally {
            l.unlock();
        }
        return null;
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
    
    private static List<RemoteClass> getRemoteClasses(String classBaseName, boolean include) throws IOException {
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
                if (classBaseName != null && include != classBaseName.equals(name.substring(baseStart, baseEnd))) {
    //                continue;
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
        ArrayReference array = bytesArrayClass.newInstance(bytes.length);
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
        
        public void eventsData(RemoteScreenshot.ComponentInfo ci, String[] data);
        
    }
    
}

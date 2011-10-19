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
package org.netbeans.modules.debugger.jpda.util;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Checks the presence of AWT grab status from
 * sun.awt.X11.XAwtState.grabWindowRef and release the grab.
 * This prevents from not responding X server due to paused application which holds the grab.
 * Warning: this class contains really hacked code accessing X11 functionality.
 * 
 * @author Martin Entlicher
 */
// See https://netbeans.org/bugzilla/show_bug.cgi?id=93076
class AWTGrabHandler {
    
    private static final Logger logger = Logger.getLogger(AWTGrabHandler.class.getName());
    
    private JPDADebuggerImpl debugger;
    private Boolean doGrabCheck = null; // Not decided at the beginning
    
    AWTGrabHandler(JPDADebuggerImpl debugger) {
        this.debugger = debugger;
    }
    
    public boolean solveGrabbing(VirtualMachine vm) {
        if (vm == null) return true;
        if (Boolean.FALSE.equals(doGrabCheck)) {
            return true;
        }
        // Check if AWT-EventQueue thread is suspended and a window holds a grab
        List<ThreadReference> allThreads = VirtualMachineWrapper.allThreads0(vm);
        for (ThreadReference t : allThreads) {
            if (!t.isSuspended()) continue;
            boolean success = solveGrabbing(t);
            if (!success) {
                return false;
            }
        }
        return true;
    }
    
    public boolean solveGrabbing(ThreadReference t) {
        
        if (Boolean.FALSE.equals(doGrabCheck)) {
            return true;
        }
        
        String name;
        try {
            name = ThreadReferenceWrapper.name(t);
        } catch (InternalExceptionWrapper ex) {
            return true; // Suppose the X grab is not there
        } catch (VMDisconnectedExceptionWrapper ex) {
            return true; // Disconnected - grab is solved
        } catch (ObjectCollectedExceptionWrapper ex) {
            return true;
        } catch (IllegalThreadStateExceptionWrapper ex) {
            return true;
        }
        if (name.startsWith("AWT-EventQueue")) {
            if (doGrabCheck == null) {
                doGrabCheck = checkXServer(t);
                logger.fine("Doing the AWT grab check = "+doGrabCheck);
            }
            if (Boolean.TRUE.equals(doGrabCheck)) {
                //System.err.println("");
                ObjectReference grabbedWindow = getGrabbedWindow(t);
                //System.err.println("Thread "+t+": some window is grabbed: "+isGrabbed+"\n");
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Thread "+t+": some window is grabbed: "+grabbedWindow);
                }
                if (grabbedWindow != null) {
                    boolean successUngrab = ungrabWindow(t, grabbedWindow, 5000);
                    logger.fine("Grabbed window was ungrabbed: "+successUngrab);
                    if (!successUngrab) {
                        InputOutput io = debugger.getIO();
                        if (io != null) {
                            OutputWriter ow = io.getErr();
                            ow.println(NbBundle.getMessage(AWTGrabHandler.class, "MSG_GrabNotReleasedDbgContinue"));
                            ow.flush();
                            io.select();
                        }
                    }
                    return successUngrab;
                }
            }
        }
        return true;
    }
    
    /*public static boolean isGrabWindowCheck(VirtualMachine vm) {
        if (GraphicsEnvironment.isHeadless()) return false;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) return false;
        GraphicsDevice screen = ge.getDefaultScreenDevice();
        String screenID = screen.getIDstring();
        int type = screen.getType();
        System.err.println("Screen ID = \""+screenID+"\", type = "+type);
        return true;
    }*/
    
    public static ObjectReference getGrabbedWindow(ThreadReference t) {
        try {
            VirtualMachine vm = MirrorWrapper.virtualMachine(t);
            List<ReferenceType> classesByName = VirtualMachineWrapper.classesByName0(vm, "sun.awt.X11.XAwtState");  // NOI18N
            if (classesByName.isEmpty()) {
                logger.fine("No XAwtState class found.");
                return null;
            }
            ReferenceType rt = classesByName.get(0);
            Field grabWindowRefField = ReferenceTypeWrapper.fieldByName(rt, "grabWindowRef");
            if (grabWindowRefField == null) {
                logger.info("No grabWindowRef field");
                return null;
            }
            Value grabWindowRef = ReferenceTypeWrapper.getValue(rt, grabWindowRefField);
            if (grabWindowRef == null) {
                logger.fine("grabWindowRef field is null.");
                return null;
            }
            // We can read the grabbed window from the Reference.referent field.
            classesByName = VirtualMachineWrapper.classesByName0(vm, "java.lang.ref.Reference");
            if (classesByName.isEmpty()) {
                logger.info("No Reference class found.");
                return null;
            }
            Field referenceField = ReferenceTypeWrapper.fieldByName(classesByName.get(0), "referent");
            if (referenceField == null) {
                logger.info("No referent field in Reference class");
                return null;
            }
            Value grabWindow = ObjectReferenceWrapper.getValue((ObjectReference) grabWindowRef, referenceField);
            if (grabWindow == null) {
                logger.fine("Grabbed window is null");
                return null;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Grabbed window is "+grabWindow);
            }
            // Grabbed window is instance of sun.awt.X11.XFramePeer
            // TODO check XBaseWindow.disposed
            return (ObjectReference) grabWindow;
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        } catch (ObjectCollectedExceptionWrapper ex) {
            return null;
        } catch (ClassNotPreparedExceptionWrapper ex) {
            return null;
        }
    }
    
    private boolean ungrabWindow(final ThreadReference tr, final ObjectReference grabbedWindow, int timeout) {
        final boolean[] success = new boolean[] { false };
        Task task = debugger.getRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                success[0] = ungrabWindow(tr, grabbedWindow);
            }
        });
        JPDAThreadImpl thread = debugger.getThread(tr);
        try {
            thread.notifyMethodInvoking();
            task.schedule(0);
            task.waitFinished(timeout);
        } catch (PropertyVetoException pvex) {
            logger.log(Level.INFO, "Method invoke vetoed", pvex);
            thread = null;
        } catch (InterruptedException ex) {
        } finally {
            if (thread != null) {
                thread.notifyMethodInvokeDone();
            }
        }
        if (!task.isFinished()) {
            // Something went wrong during ungrab. Maybe a deadlock?
            // We can not do anything but kill the debugger session to resolve the problem.
            debugger.finish();
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(AWTGrabHandler.class, "MSG_GrabNotReleasedDbgKilled"), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
            return true; // "success" by terminating the debugger session
        }
        return success[0];
    }
    
    public boolean ungrabWindow(ThreadReference tr, ObjectReference grabbedWindow) {
        // Call XBaseWindow.ungrabInput()
        try {
            VirtualMachine vm = MirrorWrapper.virtualMachine(grabbedWindow);
            List<ReferenceType> xbaseWindowClassesByName = VirtualMachineWrapper.classesByName(vm, "sun.awt.X11.XBaseWindow");
            if (xbaseWindowClassesByName.isEmpty()) {
                logger.info("Unable to release X grab, no XBaseWindow class in target VM "+VirtualMachineWrapper.description(vm));
                return false;
            }
            ClassType XBaseWindowClass = (ClassType) xbaseWindowClassesByName.get(0);
            Method ungrabInput = XBaseWindowClass.concreteMethodByName("ungrabInput", "()V");
            if (ungrabInput == null) {
                logger.info("Unable to release X grab, method ungrabInput not found in target VM "+VirtualMachineWrapper.description(vm));
                return false;
            }
            XBaseWindowClass.invokeMethod(tr, ungrabInput, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
        } catch (VMDisconnectedExceptionWrapper vmdex) {
            return true; // Disconnected, all is good.
        } catch (Exception ex) {
            logger.log(Level.INFO, "Unable to release X grab.", ex);
            return false;
        }
        return true;
    }

    private boolean checkXServer(ThreadReference t) {
        try {
            return checkXServerExc(t);
        } catch (Exception ex) {
            logger.log(Level.FINE, "Exception thrown from checkXServer: ", ex);
            return false;
        }
    }
    
    private Boolean checkXServerExc(ThreadReference tr) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
        // Check if we're running under X11:
        //if (!(Toolkit.getDefaultToolkit() instanceof sun.awt.X11.XToolkit)) {
        //    return false; // Not an X server
        //}
        Toolkit t = Toolkit.getDefaultToolkit();
        Class XToolkit = Class.forName("sun.awt.X11.XToolkit");                 // NOI18N
        if (!XToolkit.isAssignableFrom(t.getClass())) {
            logger.fine("XToolkit not found.");                                 // NOI18N
            return false;
        }
        
        // To verify if the apps are running on the same X server, set a window property
        //long defaultRootWindow = XToolkit.getDefaultRootWindow();
        long defaultRootWindow = (Long) XToolkit.getMethod("getDefaultRootWindow").invoke(t);
        String grabCheckStr = "NB_debugger_AWT_grab_check";
        Class XAtom = Class.forName("sun.awt.X11.XAtom");
        //XAtom xa = new XAtom(grabCheckStr, true);
        Constructor XAtomC = XAtom.getConstructor(String.class, Boolean.TYPE);
        Object xa = XAtomC.newInstance(grabCheckStr, true);
        //xa.setProperty(defaultRootWindow, grabCheckStr);
        // Set it later on when we verify are able to read it from the target VM.
        // Set the property in this VM: xa.setProperty(defaultRootWindow, grabCheckStr);
        XAtom.getMethod("setProperty", Long.TYPE, String.class).invoke(xa, defaultRootWindow, grabCheckStr);
            
        
        JPDAThreadImpl thread = null;
        try {
            // VERIFY WITH DEBUGGER HERE
            // Run in target VM:
            /*
                // TODO: Test the headless mode:
                if (GraphicsEnvironment.isHeadless()) {
                    return false;
                }
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                if (ge.isHeadlessInstance()) {
                    return false;
                }

                if (!(Toolkit.getDefaultToolkit() instanceof XToolkit)) {
                    return ; // Not an X server
                }
                // To verify if the apps are running on the same X server, read a window property
                boolean sunAwtDisableGrab = XToolkit.getSunAwtDisableGrab();
                if (sunAwtDisableGrab) return false;
                long defaultRootWindow = XToolkit.getDefaultRootWindow();
                String grabCheckStr = "NB_debugger_AWT_grab_check";
                XAtom xa = new XAtom(grabCheckStr, true);
                String prop = xa.getProperty(defaultRootWindow);
             */
            VirtualMachine virtualMachine = MirrorWrapper.virtualMachine(tr);
            List<ReferenceType> toolkitClassesByName = VirtualMachineWrapper.classesByName(virtualMachine, "java.awt.Toolkit");
            if (toolkitClassesByName.isEmpty()) {
                return null; // There is AWT-EventQueue thread and no Toolkit ? Try again, later...
            }
            List<ReferenceType> xtoolkitClassesByName = VirtualMachineWrapper.classesByName(virtualMachine, "sun.awt.X11.XToolkit");
            if (xtoolkitClassesByName.isEmpty()) {
                // not an X Server
                return false;
            }
            thread = debugger.getThread(tr);
            thread.notifyMethodInvoking();
            //writeLock = thread.accessLock.writeLock();
            //writeLock.lock();
            ClassType ToolkitClass = (ClassType) toolkitClassesByName.get(0);
            Method getDefaultToolkit = ClassTypeWrapper.concreteMethodByName(ToolkitClass, "getDefaultToolkit", "()Ljava/awt/Toolkit;");
            ObjectReference toolkit = (ObjectReference) ToolkitClass.invokeMethod(tr, getDefaultToolkit, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
            
            ClassType XToolkitClass = (ClassType) xtoolkitClassesByName.get(0);
            // if (!(Toolkit.getDefaultToolkit() instanceof XToolkit)) {
            if (!isAssignable(XToolkitClass, (ClassType) toolkit.referenceType())) {
                return false; // XToolkit not found.
            }
            
            //boolean sunAwtDisableGrab = XToolkit.getSunAwtDisableGrab();
            Method getSunAwtDisableGrab = ClassTypeWrapper.concreteMethodByName(XToolkitClass, "getSunAwtDisableGrab", "()Z");
            if (getSunAwtDisableGrab == null) {
                logger.fine("XToolkit.getSunAwtDisableGrab() method not found in target VM "+VirtualMachineWrapper.description(virtualMachine));
            } else {
                BooleanValue sunAwtDisableGrab = (BooleanValue) XToolkitClass.invokeMethod(tr, getSunAwtDisableGrab, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("sunAwtDisableGrab = "+sunAwtDisableGrab.value());
                }
                if (sunAwtDisableGrab.value()) {
                    // AWT grab is disabled, no need to check for grabbed windows.
                    return false;
                }
            }
            
            //long defaultRootWindow = XToolkit.getDefaultRootWindow();
            Method getDefaultRootWindow = ClassTypeWrapper.concreteMethodByName(XToolkitClass, "getDefaultRootWindow", "()J");
            if (getDefaultRootWindow == null) {
                // No way to find the root window
                logger.fine("XToolkit.getDefaultRootWindow() method does not exist in the target VM "+VirtualMachineWrapper.description(virtualMachine));
                return false;
            }
            LongValue defaultRootWindowValue = (LongValue) XToolkitClass.invokeMethod(tr, getDefaultRootWindow, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
            
            // XAtom xa = new XAtom(grabCheckStr, true);
            List<ReferenceType> xatomClassesByName = VirtualMachineWrapper.classesByName(virtualMachine, "sun.awt.X11.XAtom");
            if (xatomClassesByName.isEmpty()) {
                logger.fine("No XAtom class in target VM "+VirtualMachineWrapper.description(virtualMachine));
                return false;
            }
            ClassType XAtomClass = (ClassType) xatomClassesByName.get(0);
            Method xatomConstructor = XAtomClass.concreteMethodByName("<init>", "(Ljava/lang/String;Z)V");
            if (xatomConstructor == null) {
                logger.fine("No XAtom(String, boolean) constructor in target VM "+VirtualMachineWrapper.description(virtualMachine));
                return false;
            }
            ObjectReference xaInstance = XAtomClass.newInstance(tr, xatomConstructor,
                    Arrays.asList(VirtualMachineWrapper.mirrorOf(virtualMachine, grabCheckStr),
                                  VirtualMachineWrapper.mirrorOf(virtualMachine, true)),
                    ObjectReference.INVOKE_SINGLE_THREADED);
            
            // String prop = xa.getProperty(defaultRootWindow);
            Method getProperty = XAtomClass.concreteMethodByName("getProperty", "(J)Ljava/lang/String;");
            if (getProperty == null) {
                logger.fine("No XAtom.getProperty(long) method in target VM "+VirtualMachineWrapper.description(virtualMachine));
                return false;
            }
            StringReference srProperty = (StringReference) xaInstance.invokeMethod(tr, getProperty, Collections.singletonList(defaultRootWindowValue), ObjectReference.INVOKE_SINGLE_THREADED);
            if (srProperty == null) {
                logger.fine(grabCheckStr+" property not defined.");
                return false; // The property is not defined => we're running on different X servers.
            }
            String prop = srProperty.value();
            logger.fine(grabCheckStr+" property = "+prop);
            return grabCheckStr.equals(prop);
            
        } catch (InternalExceptionWrapper iex) {
            // Something unexpected, give up
            logger.log(Level.FINE, "InternalExceptionWrapper in target VM.", iex);
            return false;
        } catch (VMDisconnectedExceptionWrapper dex) {
            return false;
        } catch (ClassNotPreparedExceptionWrapper cnpex) {
            logger.log(Level.FINE, "ClassNotPreparedExceptionWrapper in target VM.", cnpex);
            return null; // Something is not initialized yet...
        } catch (ClassNotLoadedException cnlex) {
            logger.log(Level.FINE, "ClassNotLoadedException in target VM.", cnlex);
            return null; // Something is not initialized yet...
        } catch (IncompatibleThreadStateException itsex) {
            logger.log(Level.FINE, "IncompatibleThreadStateException in target VM.", itsex);
            return null; // Try next time...
        } catch (InvalidTypeException itex) {
            Exceptions.printStackTrace(itex);
            return false;
        } catch (InvocationException iex) {
            Exceptions.printStackTrace(iex);
            return false;
        } catch (UnsupportedOperationExceptionWrapper uoex) {
            if (logger.isLoggable(Level.FINE)) {
                try {
                    logger.log(Level.FINE, "Unsupported operation in target VM "+VirtualMachineWrapper.description(tr.virtualMachine()), uoex);
                } catch (Exception ex) {
                    logger.log(Level.FINE, "Unsupported operation in target VM.", uoex);
                }
            }
            return false;
        } catch (PropertyVetoException pvex) {
            logger.fine("Method invocation vetoed. "+pvex);
            thread = null;
            return null;
        } finally {
            if (thread != null) {
                thread.notifyMethodInvokeDone();
            }
            //xa.DeleteProperty(defaultRootWindow);
            XAtom.getMethod("DeleteProperty", Long.TYPE).invoke(xa, defaultRootWindow);
        }
    }

    private static boolean isAssignable(ClassType ct1, ClassType ct2) {
        // return ct1.isAssignableFrom(ct2)
        if (ct1.equals(ct2)) {
            return true;
        }
        ClassType cts = ct2.superclass();
        if (cts != null) {
            return isAssignable(ct1, cts);
        } else {
            return false;
        }
    }
    
}

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

import com.sun.jdi.AbsentInformationException;
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
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.WatchpointEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo.Stack;
import org.netbeans.modules.debugger.jpda.visual.ui.ScreenshotComponent;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="", types=LazyDebuggerManagerListener.class)
public class VisualDebuggerListener extends DebuggerManagerAdapter {
    
    private static final Logger logger = Logger.getLogger(VisualDebuggerListener.class.getName());
    
    private static final Map<JPDADebugger, Map<ObjectReference, Stack>> componentsAndStackTraces
            = new WeakHashMap<JPDADebugger, Map<ObjectReference, Stack>>();
    
    private Collection<Breakpoint> trackComponentBreakpoints = new ArrayList<Breakpoint>();

    @Override
    public void engineAdded(DebuggerEngine engine) {
        // Create a BP in AWT and when hit, inject the remote service.
        final JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
        Properties p = Properties.getDefault().getProperties("debugger.options.JPDA.visual");
        boolean uploadAgent = p.getBoolean("UploadAgent", true);
        logger.log(Level.FINE, "engineAdded({0}), debugger = {1}, uploadAgent = {2}", new Object[]{engine, debugger, uploadAgent});
        if (debugger != null && uploadAgent) {
            final AtomicBoolean inited = new AtomicBoolean(false);
            final MethodBreakpoint[] mb = new MethodBreakpoint[2];
            mb[0] = MethodBreakpoint.create("java.awt.EventQueue", "getNextEvent");
            mb[1] = MethodBreakpoint.create("com.sun.javafx.tk.quantum.QuantumToolkit", "pulse");
            mb[0].setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
            mb[0].setSuspend(MethodBreakpoint.SUSPEND_EVENT_THREAD);
            mb[0].setHidden(true);
            mb[0].addJPDABreakpointListener(new JPDABreakpointListener() {
                @Override
                public void breakpointReached(JPDABreakpointEvent event) {
                    if (debugger.equals(event.getDebugger())) {
                        DebuggerManager.getDebuggerManager().removeBreakpoint(mb[0]);
                        DebuggerManager.getDebuggerManager().removeBreakpoint(mb[1]);
                        if (inited.compareAndSet(false, true)) {
                            initDebuggerRemoteService(event.getThread(), RemoteServices.ServiceType.AWT);
                        }
                    }
                    event.resume();
                }
            });
            mb[1].setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
            mb[1].setSuspend(MethodBreakpoint.SUSPEND_EVENT_THREAD);
            mb[1].setHidden(true);
            mb[1].addJPDABreakpointListener(new JPDABreakpointListener() {
                @Override
                public void breakpointReached(JPDABreakpointEvent event) {
                    if (debugger.equals(event.getDebugger())) {
                        DebuggerManager.getDebuggerManager().removeBreakpoint(mb[0]);
                        DebuggerManager.getDebuggerManager().removeBreakpoint(mb[1]);
                        if (inited.compareAndSet(false, true)) {
                            initDebuggerRemoteService(event.getThread(), RemoteServices.ServiceType.FX);
                        }
                    }
                    event.resume();
                }
            });
            DebuggerManager.getDebuggerManager().addBreakpoint(mb[0]);
            DebuggerManager.getDebuggerManager().addBreakpoint(mb[1]);
        }
        boolean trackComponentChanges = p.getBoolean("TrackComponentChanges", false);
        if (debugger != null) {
            if (trackComponentChanges) {
                FieldBreakpoint fb = FieldBreakpoint.create("java.awt.Component", "parent", FieldBreakpoint.TYPE_MODIFICATION);
                fb.setHidden(true);
                fb.addJPDABreakpointListener(new JPDABreakpointListener() {
                    @Override
                    public void breakpointReached(JPDABreakpointEvent event) {
                        componentParentChanged(debugger, event);
                        event.resume();
                    }
                });
                DebuggerManager.getDebuggerManager().addBreakpoint(fb);
                trackComponentBreakpoints.add(fb);
                
                MethodBreakpoint mb = MethodBreakpoint.create("javafx.scene.Node", "setParent");
                mb.setHidden(true);
                mb.addJPDABreakpointListener(new JPDABreakpointListener() {
                    @Override
                    public void breakpointReached(JPDABreakpointEvent event) {
                        componentParentChanged(debugger, event);
                        event.resume();
                    }
                });
                DebuggerManager.getDebuggerManager().addBreakpoint(mb);
                trackComponentBreakpoints.add(mb);
            }
            
        }
    }

    private void initDebuggerRemoteService(JPDAThread thread, RemoteServices.ServiceType sType) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "initDebuggerRemoteService({0})", thread);
        }
        ClassObjectReference cor = null;
        try {
            cor = RemoteServices.uploadBasicClasses((JPDAThreadImpl) thread, sType);
        } catch (PropertyVetoException pvex) {
            Exceptions.printStackTrace(pvex);
        } catch (InvalidTypeException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotLoadedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IncompatibleThreadStateException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationException ex) {
            final InvocationExceptionTranslated iextr = new InvocationExceptionTranslated(ex, ((JPDAThreadImpl) thread).getDebugger());
            iextr.setPreferredThread((JPDAThreadImpl) thread);
            iextr.getMessage();
            iextr.getLocalizedMessage();
            iextr.getCause();
            iextr.getStackTrace();
            Exceptions.printStackTrace(iextr);
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Uploaded class = {0}", cor);
        }
        if (cor == null) {
            return ;
        }
        JPDAThreadImpl t = (JPDAThreadImpl) thread;
        ThreadReference tr = t.getThreadReference();
        VirtualMachine vm = tr.virtualMachine();
        ClassType serviceClass = (ClassType) cor.reflectedType();//RemoteServices.getClass(vm, "org.netbeans.modules.debugger.jpda.visual.remote.RemoteService");
        
        Method startMethod = serviceClass.concreteMethodByName("startAccessLoop", "()V");
        try {
            t.notifyMethodInvoking();
            serviceClass.invokeMethod(tr, startMethod, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            t.notifyMethodInvokeDone();
            cor.enableCollection(); // While AWTAccessLoop is running, it should not be collected.
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("The RemoteServiceClass is there: "+RemoteServices.getClass(vm, "org.netbeans.modules.debugger.jpda.visual.remote.RemoteService"));
        }
    }
    
    @Override
    public void engineRemoved(DebuggerEngine engine) {
        ScreenshotComponent.closeScreenshots(engine);
        JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
        logger.fine("engineRemoved("+engine+"), debugger = "+debugger);
        if (debugger != null) {
            stopDebuggerRemoteService(debugger);
        }
        if (!trackComponentBreakpoints.isEmpty()) {
            Iterator<Breakpoint> it = trackComponentBreakpoints.iterator();
            while (it.hasNext()) {
                DebuggerManager.getDebuggerManager().removeBreakpoint(it.next());
                it.remove();
            }
        }
    }
    
    private void stopDebuggerRemoteService(JPDADebugger d) {
        ClassObjectReference serviceClass = RemoteServices.getServiceClass(d);
        if (serviceClass == null) {
            return ;
        }
        try {
//            ReferenceType serviceType = serviceClass.reflectedType();
//            Field awtAccessLoop = serviceType.fieldByName("awtAccessLoop"); // NOI18N
//            ((ClassType) serviceType).setValue(awtAccessLoop, serviceClass.virtualMachine().mirrorOf(false));
            serviceClass.enableCollection();
        } catch (VMDisconnectedException vdex) {
            // Ignore
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static Stack getStackOf(JPDADebugger debugger, ObjectReference component) {
        synchronized(componentsAndStackTraces) {
            Map<ObjectReference, Stack> cs = componentsAndStackTraces.get(debugger);
            if (cs != null) {
                return cs.get(component);
            } else {
                return null;
            }
        }
    }
    
    private static void componentParentChanged(JPDADebugger debugger, JPDABreakpointEvent event) {
        ObjectReference component = null;

        try {
            java.lang.reflect.Field f = event.getClass().getDeclaredField("event"); // NOI18N
            f.setAccessible(true);
            Event jdievent = (Event) f.get(event);
            if (jdievent instanceof WatchpointEvent) {
                component = ((WatchpointEvent) jdievent).object();
            } else {
                JPDAThread t = event.getThread();
                JDIVariable v = (JDIVariable)t.getCallStack(0, 1)[0].getThisVariable();
                component = (ObjectReference)v.getJDIValue();
            }
        } catch (Exception ex) {}
        if (component != null) {
            Stack stack;
            try {
                stack = new Stack(event.getThread().getCallStack());
            } catch (AbsentInformationException ex) {
                return;
            }
            synchronized (componentsAndStackTraces) {
                Map<ObjectReference, Stack> componentAndStackTrace = componentsAndStackTraces.get(debugger);
                if (componentAndStackTrace == null) {
                    componentAndStackTrace = new HashMap<ObjectReference, Stack>();
                    componentsAndStackTraces.put(debugger, componentAndStackTrace);
                }
                //System.err.println("Component "+component+" has changed parent from "+Arrays.asList(stack.getFrames()));
                //System.err.println("   Parent = "+((JDIVariable) event.getVariable()).getJDIValue());
                componentAndStackTrace.put(component, stack);
            }
        }
    }
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.debug.Breakpoint;
import com.oracle.truffle.api.debug.DebugStackFrame;
import com.oracle.truffle.api.debug.DebugValue;
import com.oracle.truffle.api.debug.Debugger;
import com.oracle.truffle.api.debug.DebuggerSession;
import com.oracle.truffle.api.debug.SuspendedEvent;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.vm.PolyglotEngine;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Truffle accessor for JPDA debugger.
 * 
 * This class serves as a intermediary between the {@link JPDATruffleDebugManager}
 * and JPDA Java debugger (<code>org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccessor</code>),
 * which submits breakpoints and calls methods of this class.
 * To be able to invoke methods via debugger at any time, use {@link AccessLoop}.
 * 
 * Creation of a PolyglotEngine instance is out of control for the debugger.
 * Thus to intercept execution and suspension, we add Java method breakpoints into
 * <code>com.oracle.truffle.api.vm.PolyglotEngine.dispatchExecutionEvent()</code> and
 * <code>com.oracle.truffle.api.vm.PolyglotEngine.dispatchSuspendedEvent()</code> methods.
 * 
 * @author Martin
 */
public class JPDATruffleAccessor extends Object {
    
    static final boolean TRACE = Boolean.getBoolean("truffle.nbdebug.trace");   // NOI18N
    
    private static final String ACCESS_THREAD_NAME = "JPDA Truffle Access Loop";   // NOI18N
    private static volatile boolean accessLoopRunning = false;
    private static volatile Thread accessLoopThread;
    private static final Map<Debugger, JPDATruffleDebugManager> debugManagers = new WeakHashMap<>();
    /** Explicitly set this field to true to step into script calls. */
    static boolean isSteppingInto = false; // Step into was issued in JPDA debugger
    static int steppingIntoTruffle = 0; // = 0 no stepping change, > 0 set step into, < 0 unset stepping into
    /** A field to test for whether the access loop is sleeping and can be interrupted. */
    static boolean accessLoopSleeping = false;
    private static boolean stepIntoPrepared;

    /** A step command:
     * 0 no step (continue)
     * 1 step into
     * 2 step over
     * 3 step out
     */
    //private static int stepCmd = 0;

    public JPDATruffleAccessor() {}
    
    static Thread startAccessLoop() {
        if (!accessLoopRunning) {
            Thread loop;
            AccessLoop accessLoop;
            try {
                accessLoop = new AccessLoop();
                loop = new Thread(accessLoop, ACCESS_THREAD_NAME);
                loop.setDaemon(true);
                loop.setPriority(Thread.MIN_PRIORITY);
            } catch (SecurityException se) {
                return null;
            }
            accessLoopThread = loop;
            accessLoopRunning = true;
            loop.start();
        }
        return accessLoopThread;
    }
    
    static void stopAccessLoop() {
        synchronized (debugManagers) {
            for (JPDATruffleDebugManager debugManager : debugManagers.values()) {
                debugManager.dispose();
            }
        }
        accessLoopRunning = false;
        if (accessLoopThread != null) {
            accessLoopThread.interrupt();
            accessLoopThread = null;
        }
    }
    
    static JPDATruffleDebugManager setUpDebugManagerFor(/*PolyglotEngine*/Object engineObj, boolean doStepInto) {
        trace("setUpDebugManagerFor("+engineObj+", "+doStepInto+")");
        PolyglotEngine engine = (PolyglotEngine) engineObj;
        Debugger debugger = Debugger.find(engine);
        JPDATruffleDebugManager tdm = new JPDATruffleDebugManager(debugger, doStepInto);
        synchronized (debugManagers) {
            debugManagers.put(debugger, tdm);
        }
        return tdm;
    }
    
    static int executionHalted(JPDATruffleDebugManager tdm,
                               SourcePosition position,
                               boolean haltedBefore,
                               DebugValue returnValue,
                               FrameInfo frameInfo,
                               Breakpoint[] breakpointsHit,
                               Throwable[] breakpointConditionExceptions,
                               int stepCmd) {
        // Called when the execution is halted. Have a breakpoint here.
        return stepCmd;
    }
    
    static void setStep(JPDATruffleDebugManager debugManager, int stepCmd) {
        SuspendedEvent evt = debugManager.getCurrentSuspendedEvent();
        switch (stepCmd) {
            case 0: evt.prepareContinue();
                    break;
            case 1: evt.prepareStepInto(1);
                    break;
            case 2: evt.prepareStepOver(1);
                    break;
            case 3: evt.prepareStepOut();
                    break;
            default:
                    throw new IllegalStateException("Unknown step command: "+stepCmd);
        }
    }
    
    /*
    static Object getSlotValue(Object event, Object frameObj, Object slotObj) {
        SuspendedEvent suspEvent = (SuspendedEvent) event;
        FrameInstance frameInstance = (FrameInstance) frameObj;
        Frame frame = frameInstance.getFrame(FrameInstance.FrameAccess.MATERIALIZE, true);
        FrameSlot slot = (FrameSlot) slotObj;
        /* Does not work in some cases, throws FrameSlotTypeException
        switch(slot.getKind()) {
            case Boolean:   return FrameUtil.getBooleanSafe(frame, slot);
            case Byte:      return FrameUtil.getByteSafe(frame, slot);
            case Double:    return FrameUtil.getDoubleSafe(frame, slot);
            case Float:     return FrameUtil.getFloatSafe(frame, slot);
            case Int:       return FrameUtil.getIntSafe(frame, slot);
            case Long:      return FrameUtil.getLongSafe(frame, slot);
            case Object:    Object obj = FrameUtil.getObjectSafe(frame, slot);
                            //Node node = frame.materialize().getFrameDescriptor().
                            FrameInstance fi = Truffle.getRuntime().getCurrentFrame();
                            Node node = (fi != null) ? fi.getCallNode() : null;   // TODO find frame's node
                            DebugSupportProvider debugSupport = (node != null) ? getDebugSupport(node) : null;
                            Visualizer visualizer = (debugSupport != null) ? debugSupport.getVisualizer() : null;
                            String name;
                            if (visualizer != null) {
                                name = visualizer.displayIdentifier(slot);
                            } else {
                                name = slot.getIdentifier().toString();
                            }
                            TruffleObject to = new TruffleObject(visualizer, name, obj);
                            //return context.getVisualizer().displayValue(context, obj);
                            //System.err.println("TruffleObject: "+to);
                            //System.err.println("  children Generic = "+Arrays.toString(to.getChildrenGeneric()));
                            //System.err.println("  children JS = "+Arrays.toString(to.getChildrenJS()));
                            return to;
            case Illegal:   
            default:        return null;
        }
        *//*
        if (frame.isBoolean(slot)) {
            return FrameUtil.getBooleanSafe(frame, slot);
        }
        if (frame.isByte(slot)) {
            return FrameUtil.getByteSafe(frame, slot);
        }
        if (frame.isDouble(slot)) {
            return FrameUtil.getDoubleSafe(frame, slot);
        }
        if (frame.isFloat(slot)) {
            return FrameUtil.getFloatSafe(frame, slot);
        }
        if (frame.isInt(slot)) {
            return FrameUtil.getIntSafe(frame, slot);
        }
        if (frame.isLong(slot)) {
            return FrameUtil.getLongSafe(frame, slot);
        }
        if (frame.isObject(slot)) {
            Object obj = FrameUtil.getObjectSafe(frame, slot);
            Node node = frameInstance.getCallNode();
            //System.err.println("Frame instance "+frameInstance+" node = "+node);
            //Node node = frame.materialize().getFrameDescriptor().
            String name;
            name = slot.getIdentifier().toString();
            TruffleObject to = new TruffleObject(name, obj, suspEvent, frameInstance);
            //return context.getVisualizer().displayValue(context, obj);
            //System.err.println("TruffleObject: "+to);
            //System.err.println("  children Generic = "+Arrays.toString(to.getChildrenGeneric()));
            //System.err.println("  children JS = "+Arrays.toString(to.getChildrenJS()));
            return to;
        }
        return null;
    }
    */
    
    /*
    static TruffleFrame[] getFramesInfo(FrameInstance[] frames) {
        Visualizer visualizer = debugManager.getContext().getVisualizer();
        int n = frames.length;
        TruffleFrame[] frameInfos = new TruffleFrame[n];
        for (int i = 0; i < n; i++) {
            FrameInstance fi = frames[i];
            TruffleFrame tf = new TruffleFrame();
            tf.callTargetName = visualizer.displayCallTargetName(fi.getCallTarget());
            tf.methodName = visualizer.displayMethodName(fi.getCallNode());
            tf.sourceLocation = visualizer.displaySourceLocation(fi.getCallNode());
            frameInfos[i] = tf;
        }
        return frameInfos;
    }
    */
    /**
     * @param frames The array of stack frame infos
     * @return An array of two elements: a String of frame information and
     * an array of code contents.
     *//*
    static Object[] getFramesInfo(FrameInstance[] frames) {
        //Visualizer visualizer = debugManager.getVisualizer();
        int n = frames.length;
        //TruffleFrame[] frameInfos = new TruffleFrame[n];
        StringBuilder frameInfos = new StringBuilder();
        String[] codes = new String[n];
        Object[] thiss = new Object[n];
        for (int i = 0; i < n; i++) {
            FrameInstance fi = frames[i];
            frameInfos.append(DebuggerVisualizer.getDisplayName(fi.getCallTarget()));
            frameInfos.append('\n');
            frameInfos.append(DebuggerVisualizer.getMethodName(fi.getCallNode().getRootNode()));
            frameInfos.append('\n');
            frameInfos.append(DebuggerVisualizer.getSourceLocation(fi.getCallNode()));
            frameInfos.append('\n');
            if (fi.getCallNode() == null) {
                /* frames with null call nodes are filtered out by JPDATruffleDebugManager.FrameInfo
                System.err.println("Frame with null call node: "+fi);
                System.err.println("  is virtual frame = "+fi.isVirtualFrame());
                System.err.println("  call target = "+fi.getCallTarget());
                System.err.println("frameInfos = "+frameInfos);
                *//*
            }
            SourcePosition position = JPDATruffleDebugManager.getPosition(fi.getCallNode());
            frameInfos.append(position.id);
            frameInfos.append('\n');
            frameInfos.append(position.name);
            frameInfos.append('\n');
            frameInfos.append(position.path);
            frameInfos.append('\n');
            frameInfos.append(position.uri.toString());
            frameInfos.append('\n');
            frameInfos.append(position.line);
            
            frameInfos.append("\n\n");
            
            codes[i] = position.code;
            /*
             TODO Find "this"
            Frame f = fi.getFrame(FrameInstance.FrameAccess.READ_ONLY, false);
            if (f instanceof VirtualFrame) {
                thiss[i] = JSFrameUtil.getThisObj((VirtualFrame) f);
            }*//*
        }
        return new Object[] { frameInfos.toString(), codes, thiss };
    }*/
    
    /**
     * @param frames The array of stack frame infos
     * @return An array of two elements: a String of frame information and
     * an array of code contents.
     */
    static Object[] getFramesInfo(DebugStackFrame[] frames, boolean includeInternal) {
        trace("getFramesInfo({0})",includeInternal);
        int n = frames.length;
        StringBuilder frameInfos = new StringBuilder();
        String[] codes = new String[n];
        Object[] thiss = new Object[n];
        int j = 0;
        for (int i = 0; i < n; i++) {
            DebugStackFrame sf = frames[i];
            boolean isInternal = FrameInfo.isInternal(sf);
            //System.err.println("SF("+sf.getName()+", "+sf.getSourceSection()+") is internal = "+isInternal);
            if (!includeInternal && isInternal) {
                continue;
            }
            String sfName = sf.getName();
            if (sfName == null) {
                sfName = "";
            }
            frameInfos.append(sfName);
            frameInfos.append('\n');
            frameInfos.append(DebuggerVisualizer.getSourceLocation(sf.getSourceSection()));
            frameInfos.append('\n');
            /*if (fi.getCallNode() == null) {
                /* frames with null call nodes are filtered out by JPDATruffleDebugManager.FrameInfo
                System.err.println("Frame with null call node: "+fi);
                System.err.println("  is virtual frame = "+fi.isVirtualFrame());
                System.err.println("  call target = "+fi.getCallTarget());
                System.err.println("frameInfos = "+frameInfos);
                *//*
            }*/
            SourcePosition position = JPDATruffleDebugManager.getPosition(sf.getSourceSection());
            frameInfos.append(createPositionIdentificationString(position));
            if (includeInternal) {
                frameInfos.append('\n');
                frameInfos.append(isInternal);
            }
            
            frameInfos.append("\n\n");
            
            codes[j] = position.code;
            /*
             TODO Find "this"
            Frame f = fi.getFrame(FrameInstance.FrameAccess.READ_ONLY, false);
            if (f instanceof VirtualFrame) {
                thiss[i] = JSFrameUtil.getThisObj((VirtualFrame) f);
            }*/
            j++;
        }
        if (j < n) {
            codes = Arrays.copyOf(codes, j);
            thiss = Arrays.copyOf(thiss, j);
        }
        boolean areSkippedInternalFrames = j < n;
        return new Object[] { frameInfos.toString(), codes, thiss, areSkippedInternalFrames };
    }
    
    private static String createPositionIdentificationString(SourcePosition position) {
        StringBuilder str = new StringBuilder();
        str.append(position.id);
        str.append('\n');
        str.append(position.name);
        str.append('\n');
        str.append(position.path);
        str.append('\n');
        str.append(position.uri.toString());
        str.append('\n');
        str.append(position.line);
        return str.toString();
    }

    // 9*vars: <name>, <type>, <writable>, <String value>,
    //         <var source>, <VS code>, <type source>, <TS code>, <DebugValue>
    static Object[] getVariables(DebugStackFrame sf) {
        List<DebugValue> values = new ArrayList<>();
        for (Iterator<DebugValue> iterator = sf.iterator(); iterator.hasNext(); ) {
            values.add(iterator.next());
        }
        int numValues = values.size();
        Object[] vars = new Object[numValues*9];
        for (int i = 0; i < numValues; i++) {
            DebugValue value = values.get(i);
            int vi = 9*i;
            TruffleObject tobj = new TruffleObject(value);
            vars[vi] = tobj.name;
            vars[vi + 1] = tobj.type;
            vars[vi + 2] = tobj.writable;
            vars[vi + 3] = tobj.displayValue;
            if (tobj.valueSourcePosition != null) {
                vars[vi + 4] = createPositionIdentificationString(tobj.valueSourcePosition);
                vars[vi + 5] = tobj.valueSourcePosition.code;
            }
            if (tobj.typeSourcePosition != null) {
                vars[vi + 6] = createPositionIdentificationString(tobj.typeSourcePosition);
                vars[vi + 7] = tobj.typeSourcePosition.code;
            }
            vars[vi + 8] = tobj;
        }
        return vars;
    }

    static void debuggerAccess() {
        // A breakpoint is submitted on this method.
        // When accessLoopThread is interrupted, this breakpoint is hit
        // and methods can be executed via JPDA debugger.
    }
    
    static Breakpoint[] setLineBreakpoint(String uriStr, int line,
                                          int ignoreCount, String condition) throws URISyntaxException {
        return doSetLineBreakpoint(new URI(uriStr), line, ignoreCount, condition, false);
    }
    
    static Breakpoint setLineBreakpoint(JPDATruffleDebugManager debugManager, String uriStr, int line,
                                        int ignoreCount, String condition) throws URISyntaxException {
        try {
            return doSetLineBreakpoint(debugManager.getDebuggerSession(), new URI(uriStr), line, ignoreCount, condition, false);
        } catch (IOException ex) {
            System.err.println("setLineBreakpoint("+uriStr+", "+line+"): "+ex);
            return null;
        }
    }
    
    static Breakpoint[] setOneShotLineBreakpoint(String uriStr, int line) throws URISyntaxException {
        return doSetLineBreakpoint(new URI(uriStr), line, 0, null, true);
    }
    
    private static Breakpoint[] doSetLineBreakpoint(URI uri, int line,
                                                    int ignoreCount, String condition,
                                                    boolean oneShot) {
        Breakpoint[] lbs;
        JPDATruffleDebugManager[] managers;
        synchronized (debugManagers) {
            managers = debugManagers.values().toArray(new JPDATruffleDebugManager[] {});
        }
        lbs = new Breakpoint[managers.length];
        int i = 0;
        for (JPDATruffleDebugManager debugManager : managers) {
            DebuggerSession debuggerSession = debugManager.getDebuggerSession();
            if (debuggerSession == null) {
                lbs = Arrays.copyOf(lbs, lbs.length - 1);
                //synchronized (debugManagers) {
                //    debugManagers.remove(debugger);
                //}
                continue;
            }
            Breakpoint lb;
            try {
                lb = doSetLineBreakpoint(debuggerSession, uri, line,
                                         ignoreCount, condition, oneShot);
            } catch (IOException dex) {
                System.err.println("setLineBreakpoint("+uri+", "+line+"): "+dex);
                lbs = Arrays.copyOf(lbs, lbs.length - 1);
                continue;
            }
            lbs[i++] = lb;
        }
        return lbs;
    }
    
    private static Breakpoint doSetLineBreakpoint(DebuggerSession debuggerSession,
                                                  URI uri, int line,
                                                  int ignoreCount, String condition,
                                                  boolean oneShot) throws IOException {
        Breakpoint.Builder bb = Breakpoint.newBuilder(uri).lineIs(line);
        if (ignoreCount != 0) {
            bb.ignoreCount(ignoreCount);
        }
        if (oneShot) {
            bb.oneShot();
        }
        Breakpoint lb = bb.build();
        if (condition != null) {
            lb.setCondition(condition);
        }
        trace("JPDATruffleAccessor.setLineBreakpoint({0}, {1}, {2}): lb = {3}", debuggerSession, uri, line, lb);
        return debuggerSession.install(lb);
    }
    
    static void removeBreakpoint(Object br) {
        ((Breakpoint) br).dispose();
    }
    
    static Object evaluate(DebugStackFrame sf, String expression) {
        return sf.eval(expression).as(String.class);
    }
    
    static void trace(String message, Object... parameters) {
        if (TRACE) {
            System.out.println("NB Debugger: " + MessageFormat.format(message, parameters));
        }
    }

    private static class AccessLoop implements Runnable {
        
        @Override
        public void run() {
            while (accessLoopRunning) {
                accessLoopSleeping = true;
                // Wait until we're interrupted
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException iex) {}
                accessLoopSleeping = false;
                trace("AccessLoop: steppingIntoTruffle = "+steppingIntoTruffle+", isSteppingInto = "+isSteppingInto+", stepIntoPrepared = "+stepIntoPrepared);
                
                if (steppingIntoTruffle != 0) {
                    if (steppingIntoTruffle > 0) {
                        if (!stepIntoPrepared) {
                            synchronized (debugManagers) {
                                for (JPDATruffleDebugManager debugManager : debugManagers.values()) {
                                    debugManager.prepareExecStepInto();
                                }
                            }
                            stepIntoPrepared = true;
                            trace("Prepared step into and continue.");
                        }
                        isSteppingInto = true;
                    } else {
                        // un-prepare step into, if possible.
                        synchronized (debugManagers) {
                            for (JPDATruffleDebugManager debugManager : debugManagers.values()) {
                                debugManager.prepareExecContinue();
                            }
                        }
                        isSteppingInto = false;
                        stepIntoPrepared = false;
                    }
                    steppingIntoTruffle = 0;
                    continue;
                }
                trace("accessLoopRunning = "+accessLoopRunning+", possible debugger access...");
                if (accessLoopRunning) {
                    debuggerAccess();
                }
            }
        }

    }

}

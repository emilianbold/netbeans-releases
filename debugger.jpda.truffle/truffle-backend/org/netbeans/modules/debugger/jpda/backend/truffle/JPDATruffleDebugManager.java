/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2015 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.debug.Debugger;
import com.oracle.truffle.api.debug.ExecutionEvent;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.vm.PolyglotEngine;

/**
 *
 * @author martin
 */
class JPDATruffleDebugManager {
    
    //private static final JSNodeProberDelegate nodeProberDelegate = new JSNodeProberDelegate();
    
    //private final ScriptEngine engine;
    //private final ExecutionContext context;
    //private final TopFrameHolder topFrameHolder;
    //private final PolyglotEngine.Language language;
    private final Debugger debugger;
    private final PolyglotEngine tvm;
    private final ExecutionEvent execEvent;

    public JPDATruffleDebugManager(Debugger debugger, PolyglotEngine tvm, ExecutionEvent event) {
        //super(dbgClient);
        //this.engine = engine;
        //this.context = context;
        //this.topFrameHolder = new TopFrameHolder();
        //((JPDADebugClient) dbgClient).setTopFrameHolder(topFrameHolder);
        //this.sourceExecution = new JPDAJSSourceExecution((TruffleJSEngine) engine);
        //language = getLanguage(engine);
        this.debugger = debugger; // DebugEngine.create(dbgClient, language);
        this.tvm = tvm;
        this.execEvent = event;
        /*
        startExecution(null);
        prepareContinue();
        */
        //nodeProberDelegate.addNodeProber(
        //        new JPDAJSDebugProber((JSContext) context, this, new JPDAInstrumentProxy(instrumentCallback, context)));
        //System.err.println("new JPDATruffleDebugManager("+engine+")");
    }
    
    static JPDATruffleDebugManager setUp() {
        //System.err.println("JPDATruffleDebugManager.setUp()");
        //TruffleJSEngineFactory.addNodeProber(nodeProberDelegate);
        //Probe.registerASTProber(new JPDAJSDebugProber());
        return null; // Initialize TruffleJSEngine class only.
    }

    static JPDATruffleDebugManager setUp(Debugger debugger, PolyglotEngine tvm, ExecutionEvent event) {
        //System.err.println("JPDATruffleDebugManager.setUp()");
        //JSContext jsContext = ((TruffleJSEngine) engine).getJSContext();
        //ScriptContext context = engine.getContext();
        JPDATruffleDebugManager debugManager = new JPDATruffleDebugManager(debugger, tvm, event);
                //engine, jsContext, new JPDADebugClient(getLanguage(engine)));
        //jsContext.setDebugContext(new JPDADebugContext(jsContext, debugManager));
        //jsContext.addNodeProber(new JPDAJSNodeProber(jsContext, debugManager, ));
        //System.err.println("SET UP of JPDATruffleDebugManager = "+debugManager+" for "+engine+" and prober to "+jsContext);
        return debugManager;
    }
    
    /*
    private static PolyglotEngine.Language getLanguage(ScriptEngine engine) {
        Map<String, PolyglotEngine.Language> languages = PolyglotEngine.newVM().build().getLanguages();
        List<String> languageMIMETypes = engine.getFactory().getMimeTypes();
        PolyglotEngine.Language language = languages.get(languageMIMETypes.get(0));
        return language;
    }
    */

    /*
    ExecutionContext getContext() {
        return context;
    }*/
    
    Debugger getDebugger() {
        return debugger;
    }
    
    PolyglotEngine getPolyglotEngine() {
        return tvm;
    }
    
    /*
    Visualizer getVisualizer() {
        return language.getToolSupport().getVisualizer();
    }*/
    
    /*
    @Override
    public void run(Source source) throws DebugException {
        //System.err.println("JPDATruffleDebugManager.run("+source+")");
        startExecution(source);
        prepareContinue();
        try {
            runSource(source);
        } finally {
            endExecution();
        }
    }

    @Override
    public void runStepInto(Source source) throws DebugException {
        startExecution(source);
        prepareStepInto(1);
        try {
            runSource(source);
        } finally {
            endExecution();
        }
    }
    
    private void runSource(Source source) throws DebugException {
        try {
            ((TruffleJSEngine) engine).eval(source);
        } catch (ScriptException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof QuitException) {
                throw (QuitException) cause;
            }
            if (cause instanceof KillException) {
                throw (KillException) cause;
            }
            if (cause instanceof UserScriptException) {
                final UserScriptException uncaught = (UserScriptException) cause;
                final StackTraceElement[] stackTrace = uncaught.getStackTrace();
                String location = "";
                if (stackTrace.length > 0) {
                    final StackTraceElement elem = stackTrace[0];
                    location = " at " + elem.getFileName() + ", line " + elem.getLineNumber();
                }
                throw new DebugException("Uncaught exception: \"" + uncaught.getLocalizedMessage() + "\" " + location);
            }
            throw new DebugException("Can't run source " + source.getName() + ": " + e.getMessage());
        }
    }
    */
    
    /*
    Object eval(Source source) throws DebugException {
        return debugger.eval(source, topFrameHolder.currentNode, topFrameHolder.currentTopFrame);
    }*/
    
    /*
    @Override
    public Object eval(Source source, Node node, MaterializedFrame frame) {
        //System.err.println("JPDATruffleDebugManager.eval("+source+", "+node+", "+frame+")");
        startExecution(source);
        try {
            if (frame == null) {
                return engine.eval(source.getCode());
            }
            final JavaScriptNode jsNode = (JavaScriptNode) node;
            final Environment environment = (Environment) jsNode.getEnvironment();
            return ((JSContext) context).getEvaluator().evaluate((JSContext) context, source, environment, frame);
        } catch (ScriptException e) {
            //throw JSException.create(JSErrorType.EvalError, e.getMessage());
            return null;
        } finally {
            endExecution();
        }
    }

    @Override
    public ToolEvalNodeFactory createExprEvalNodeFactory(String expr, ToolEvalResultListener resultListener) {
        return ((TruffleJSEngine) engine).createExprEvalNodeFactory((JSContext) context, expr, resultListener);
    }
    */
    static SourcePosition getPosition(Node node) {
        SourceSection sourceSection = node.getSourceSection();
        if (sourceSection == null) {
            sourceSection = node.getEncapsulatingSourceSection();
        }
        if (sourceSection == null) {
            System.err.println("Node without sourceSection! node = "+node+", of class: "+node.getClass());
            throw new IllegalStateException("Node without sourceSection! node = "+node+", of class: "+node.getClass());
        }
        int line = sourceSection.getStartLine();
        Source source = sourceSection.getSource();
        //System.err.println("source of "+node+" = "+source);
        //System.err.println("  name = "+source.getName());
        //System.err.println("  short name = "+source.getShortName());
        //System.err.println("  path = "+source.getPath());
        //System.err.println("  code at line = "+source.getCode(line));
        String name = source.getShortName();
        String path = source.getPath();
        String code = source.getCode();
        return new SourcePosition(source, name, path, line, code);
    }

    void dispose() {
        /*
        endExecution();
        */
    }

    /*
    // Logging only
    @Override
    public LineBreakpoint setLineBreakpoint(LineLocation lineLocation) throws DebugException {
        LineBreakpoint lb = super.setLineBreakpoint(lineLocation);
        System.err.println("setLineBreakpoint("+lineLocation+"):");
        System.err.println("  state = "+lb.getStateDescription());
        System.err.println("  Line Breakpoint Support = "+lineBreaks);
        try {
            Object l2pm = linesToProbesMap;
            Field l2pmf = l2pm.getClass().getDeclaredField("lineToProbesMap");
            l2pmf.setAccessible(true);
            System.err.println("  linesToProbesMap = "+linesToProbesMap);
            System.err.println("  linesToProbesMap.lineToProbesMap = "+l2pmf.get(linesToProbesMap));
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            Object lbs = lineBreaks;
            Field lineToProbesMapField = lbs.getClass().getDeclaredField("lineToProbesMap");
            lineToProbesMapField.setAccessible(true);
            System.err.println("  lineBreaks.lineToProbesMap = "+lineToProbesMapField.get(lineBreaks));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return lb;
    }*/
    
    /*
    private static class JPDADebugClient implements DebugClient {
        
        private final PolyglotEngine.Language language;
        private TopFrameHolder topFrameHolder;
        
        public JPDADebugClient(PolyglotEngine.Language language) {
            this.language = language;
        }

        @Override
        public void haltedAt(Node astNode, MaterializedFrame frame, List<String> warnings) {
            //System.err.println("JPDADebugClient.haltedAt("+astNode+", "+frame+")");
            topFrameHolder.currentTopFrame = frame;
            topFrameHolder.currentNode = astNode;
            SourcePosition position = getPosition(astNode);
            Visualizer visualizer = language.getToolSupport().getVisualizer();
            
            FrameInfo fi = new FrameInfo(frame, visualizer, astNode);
            
            if (JPDATruffleAccessor.isSteppingInto) {
                JPDATruffleAccessor.executionStepInto(astNode, frame, visualizer.displayMethodName(astNode), //name,
                        position.id, position.name, position.path,
                        position.line, position.code,
                        fi.slots, fi.slotNames, fi.slotTypes,
                        fi.stackTrace, fi.topFrame,
                        new TruffleObject(visualizer, "this", fi.thisObject));
            } else {
                JPDATruffleAccessor.executionHalted(astNode, frame,
                        position.id, position.name, position.path,
                        position.line, position.code,
                        fi.slots, fi.slotNames, fi.slotTypes,
                        fi.stackTrace, fi.topFrame,
                        new TruffleObject(visualizer, "this", fi.thisObject));
            }
            
            topFrameHolder.currentTopFrame = null;
            topFrameHolder.currentNode = null;
        }

        private void setTopFrameHolder(TopFrameHolder topFrameHolder) {
            this.topFrameHolder = topFrameHolder;
        }

        @Override
        public PolyglotEngine.Language getLanguage() {
            return language;
        }

    }
    */
    
    /*
    private static class JPDAInstrumentProxy implements DebugInstrumentCallback {
    
        private final DebugInstrumentCallback delegateCallback;
        private final ExecutionContext context;
        private boolean isStepping = false;
    
        public JPDAInstrumentProxy(DebugInstrumentCallback delegateCallback,
                                   ExecutionContext context) {
            this.delegateCallback = delegateCallback;
            this.context = context;
        }

        @Override
        public boolean isStepping() {
            if (!isStepping) {
                return delegateCallback.isStepping();
            } else {
                return true;
            }
        }

        @Override
        public void haltedAt(Node astNode, MaterializedFrame frame) {
            //System.err.println("JPDAInstrumentProxy.haltedAt("+astNode+", "+frame+")");
            delegateCallback.haltedAt(astNode, frame);
        }

        @Override
        public void callEntering(Node astNode, String name) {
            //System.err.println("JPDAInstrumentProxy.callEntering("+astNode+", "+name+")");
            astNode.getSourceSection();
            if (JPDATruffleAccessor.isSteppingInto) {
                SourcePosition position = getPosition(astNode);
                FrameInstance currentFrame = Truffle.getRuntime().getCurrentFrame();
                Frame frame = currentFrame.getFrame(FrameInstance.FrameAccess.MATERIALIZE, true);
                Visualizer visualizer = context.getVisualizer();
                FrameInfo fi = new FrameInfo(frame.materialize(), visualizer, astNode);
                JPDATruffleAccessor.executionStepInto(astNode, name,
                        position.id, position.name, position.path,
                        position.line, position.code,
                        fi.slots, fi.slotNames, fi.slotTypes,
                        fi.stackTrace, fi.topFrame,
                        new TruffleObject(context, "this", fi.thisObject));
            }
            delegateCallback.callEntering(astNode, name);
        }

        @Override
        public void callReturned(Node astNode, String name) {
            //System.err.println("JPDAInstrumentProxy.callReturned("+astNode+", "+name+")");
            delegateCallback.callReturned(astNode, name);
        }
        
    }
    */
    
    /*
    private static class TopFrameHolder {
        MaterializedFrame currentTopFrame;
        Node currentNode;
    }
    */
    
    /*
    private static class JPDADebugContext implements DebugContext {
        
        private final ExecutionContext execContext;
        private final DebugManager debugManager;
        
        public JPDADebugContext(ExecutionContext execContext, DebugManager debugManager) {
            this.execContext = execContext;
            this.debugManager = debugManager;
        }

        @Override
        public ExecutionContext getContext() {
            return execContext;
        }

        @Override
        public NodeInstrumenter getNodeInstrumenter() {
            return null;
        }

        @Override
        public DebugManager getDebugManager() {
            return debugManager;
        }

        @Override
        public ASTPrinter getASTPrinter() {
            return null;
        }

        @Override
        public String displayValue(Object o) {
            return String.valueOf(o);
        }

        @Override
        public String displayIdentifier(FrameSlot fs) {
            return fs.getIdentifier().toString();
        }

        @Override
        public void executionHalted(Node node, MaterializedFrame mf) {
            JPDATruffleAccessor.executionHalted(node, mf);
        }
        
    }
    */

    void prepareExecStepInto() {
        System.err.println("prepareExecStepInto()...");
        execEvent.prepareStepInto();
        System.err.println("prepareExecStepInto() DONE.");
    }

    void prepareExecContinue() {
        System.err.println("prepareExecContinue()...");
        execEvent.prepareContinue();
        System.err.println("prepareExecContinue() DONE.");
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.debugger.jpda.backend.truffle.js;

import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.instrument.AdvancedInstrumentResultListener;
import com.oracle.truffle.api.instrument.AdvancedInstrumentRootFactory;
import com.oracle.truffle.api.instrument.KillException;
import com.oracle.truffle.api.instrument.QuitException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.debug.DebugException;
import com.oracle.truffle.debug.SourceExecution;
import com.oracle.truffle.js.engine.TruffleJSEngine;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.parser.env.Environment;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.UserScriptException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.script.ScriptException;

/**
 *
 * @author Martin Entlicher
 */
public class JPDAJSSourceExecution extends SourceExecution {
    
    private final TruffleJSEngine engine;
    private final AtomicBoolean dummyExecLock = new AtomicBoolean(false);
    
    public JPDAJSSourceExecution(TruffleJSEngine engine) {
        this.engine = engine;
    }

    @Override
    protected void languageRun(Source source) throws DebugException {
        if (source == null) {
            synchronized (dummyExecLock) {
                dummyExecLock.notifyAll();
                dummyExecLock.set(true);
                try {
                    dummyExecLock.wait();
                } catch (InterruptedException ex) {
                }
            }
        } else {
            runSource(source);
        }
    }

    @Override
    protected Object languageEval(Source source, Node node, MaterializedFrame mFrame) {
        try {
            if (mFrame == null) {
                return engine.eval(source.getCode());
            }
            final JavaScriptNode jsNode = (JavaScriptNode) node;
            final Environment environment = (Environment) jsNode.getEnvironment();
            JSContext context = engine.getJSContext();
            return context.getEvaluator().evaluate(context, source, environment, mFrame);
        } catch (ScriptException e) {
            //throw JSException.create(JSErrorType.EvalError, e.getMessage());
            return null;
        }
    }

    private void runSource(Source source) throws DebugException {
        try {
            engine.eval(source);
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
    
    @Override
    protected AdvancedInstrumentRootFactory languageAdvancedInstrumentRootFactory(String expr, AdvancedInstrumentResultListener resultListener) throws DebugException {
        return engine.createAdvancedInstrumentRootFactory(engine.getJSContext(), expr);
    }

    public void startDummyExecution() {
        synchronized (dummyExecLock) {
            if (!dummyExecLock.get()) {
                try {
                    dummyExecLock.wait();
                } catch (InterruptedException ex) {}
            }
        }
    }

    public void endDummyExecution() {
        synchronized (dummyExecLock) {
            if (!dummyExecLock.get()) {
                try {
                    dummyExecLock.wait();
                } catch (InterruptedException ex) {}
            }
            try {
                dummyExecLock.wait();
            } catch (InterruptedException ex) {}
        }
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.debugger.jpda.backend.truffle.js;

import com.oracle.truffle.api.instrument.PhylumTag;
import static com.oracle.truffle.api.instrument.StandardTag.CALL;
import static com.oracle.truffle.api.instrument.StandardTag.STATEMENT;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.debug.impl.AbstractDebugManager;
import com.oracle.truffle.debug.instrument.DebugCallInstrument;
import com.oracle.truffle.debug.instrument.DebugInstrumentCallback;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.control.EmptyNode;
import com.oracle.truffle.js.nodes.instrument.JSNodeProber;
import com.oracle.truffle.js.nodes.instrument.JSWrapper;
import com.oracle.truffle.js.runtime.JSContext;

/**
 * JavaScript - specific node prober
 * @author martin
 */
public class JPDAJSNodeProber implements JSNodeProber {
    
    private final JSContext jsContext;
    private final AbstractDebugManager debugManager;
    private final DebugInstrumentCallback instrumentCallback;
    
    public JPDAJSNodeProber(JSContext jsContext, AbstractDebugManager debugManager,
                            DebugInstrumentCallback instrumentCallback) {
        this.jsContext = jsContext;
        this.debugManager = debugManager;
        this.instrumentCallback = instrumentCallback;
    }

    @Override
    public JavaScriptNode probeAsStatement(JavaScriptNode jsn) {
        //System.err.println("probeAsStatement("+jsn+")");
        assert jsn != null;
        if (jsn instanceof EmptyNode) {
            return jsn;
        }
        final JSWrapper wrapper = getWrapper(jsn);
        wrapper.tagAs(STATEMENT);
        return wrapper;
    }

    @Override
    public JavaScriptNode probeAsCall(JavaScriptNode jsn, String callName) {
        //System.err.println("probeAsCall("+jsn+", "+callName+")");
        assert jsn != null;

        JSWrapper wrapper = null;
        if (jsn instanceof JSWrapper) {
            wrapper = (JSWrapper) jsn;
            if (!wrapper.isTaggedAs(CALL)) {
                wrapper.tagAs(CALL);
                wrapper.getProbe().addInstrument(new DebugCallInstrument(jsContext, instrumentCallback, callName));
            }
        } else {
            wrapper = new JSWrapper(jsContext, jsn);
            wrapper.tagAs(CALL);
            wrapper.getProbe().addInstrument(new DebugCallInstrument(jsContext, instrumentCallback, callName));
        }
        return wrapper;
    }

    @Override
    public JavaScriptNode probeAsLocalAssignment(JavaScriptNode jsn, String string) {
        //System.err.println("JPDAJSNodeProber.probeAsLocalAssignment("+jsn+", "+string+")");
        // TODO
        return null;
    }

    @Override
    public JavaScriptNode probeAsThrow(JavaScriptNode jsn) {
        // TODO
        return null;
    }

    @Override
    public Node probeAs(Node node, PhylumTag pt, Object... os) {
        //System.err.println("JPDAJSNodeProber.probeAs("+node+", "+pt+")");
        // TODO
        return null;
    }
    
    private JSWrapper getWrapper(JavaScriptNode jsNode) {
        return (jsNode instanceof JSWrapper) ? (JSWrapper) jsNode : new JSWrapper(jsContext, jsNode);
    }
    
}

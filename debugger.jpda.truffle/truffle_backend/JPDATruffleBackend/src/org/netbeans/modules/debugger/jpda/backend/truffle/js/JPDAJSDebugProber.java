/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.debugger.jpda.backend.truffle.js;

import com.oracle.truffle.api.instrument.ASTProber;
import com.oracle.truffle.api.instrument.InstrumentationNode;
import static com.oracle.truffle.api.instrument.StandardSyntaxTag.CALL;
import static com.oracle.truffle.api.instrument.StandardSyntaxTag.STATEMENT;
import static com.oracle.truffle.api.instrument.StandardSyntaxTag.THROW;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeVisitor;
import com.oracle.truffle.debug.impl.AbstractDebugManager;
import com.oracle.truffle.debug.instrument.DebugInstrumentCallback;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.control.BlockNode;
import com.oracle.truffle.js.nodes.control.ThrowNode;
import com.oracle.truffle.js.nodes.function.JSFunctionCallNode;
import com.oracle.truffle.js.runtime.JSContext;

/**
 * JavaScript - specific node prober
 * @author martin
 */
public class JPDAJSDebugProber implements NodeVisitor, ASTProber {
    
    public JPDAJSDebugProber() {
    }

    @Override
    public boolean visit(Node node) {
        if (node instanceof JavaScriptNode && !(node instanceof InstrumentationNode)) {
            // Presume that all JS nodes can be probed
            final JavaScriptNode jsNode = (JavaScriptNode) node;

            if (isInBlock(jsNode) && jsNode.getSourceSection() != null) {
                jsNode.probe().tagAs(STATEMENT, null);
            }

            if (jsNode instanceof JSFunctionCallNode) {
                // TODO
                jsNode.probe().tagAs(CALL, null);

            } else if (jsNode instanceof ThrowNode) {
                jsNode.probe().tagAs(THROW, null);
            }
        }
        return true;
    }
    
    private boolean isInBlock(JavaScriptNode node) {
        Node parent = node.getParent();
        if (parent instanceof InstrumentationNode) {
            parent = parent.getParent();
        }
        return parent instanceof BlockNode;
    }

    public void probeAST(Node node) {
        node.accept(this);
    }

}

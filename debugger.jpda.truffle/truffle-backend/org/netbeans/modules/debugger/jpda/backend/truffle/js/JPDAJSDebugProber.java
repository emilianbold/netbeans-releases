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

package org.netbeans.modules.debugger.jpda.backend.truffle.js;

import com.oracle.truffle.api.instrument.ASTProber;
import com.oracle.truffle.api.instrument.InstrumentationNode;
import com.oracle.truffle.api.instrument.Instrumenter;
import static com.oracle.truffle.api.instrument.StandardSyntaxTag.CALL;
import static com.oracle.truffle.api.instrument.StandardSyntaxTag.STATEMENT;
import static com.oracle.truffle.api.instrument.StandardSyntaxTag.THROW;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeVisitor;
import com.oracle.truffle.api.nodes.RootNode;
/*
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.control.BlockNode;
import com.oracle.truffle.js.nodes.control.SequenceNode;
import com.oracle.truffle.js.nodes.control.ThrowNode;
import com.oracle.truffle.js.nodes.function.JSFunctionCallNode;
*/

/**
 * JavaScript - specific node prober
 * @author martin
 */
public class JPDAJSDebugProber implements NodeVisitor, ASTProber {
    
    public JPDAJSDebugProber() {
    }

    @Override
    public boolean visit(Node node) {
        /*
        if (node instanceof JavaScriptNode && node.isInstrumentable()) {
            // Presume that all JS nodes can be probed
            final JavaScriptNode jsNode = (JavaScriptNode) node;

            if (inSequence(jsNode) && jsNode.getSourceSection() != null) {
                jsNode.probe().tagAs(STATEMENT, null);
            }

            if (jsNode instanceof JSFunctionCallNode) {
                // ? ((JSFunctionCallNode) jsNode).getFunction().getDescription();
                // TODO
                //jsNode.probe().tagAs(CALL, null);

            } else if (jsNode instanceof ThrowNode) {
                jsNode.probe().tagAs(THROW, null);
            }
        }
        */
        return true;
    }
    /*
    private boolean isInBlock(JavaScriptNode node) {
        Node parent = node.getParent();
        if (parent instanceof InstrumentationNode) {
            parent = parent.getParent();
        }
        return parent instanceof BlockNode;
    }

    private boolean inSequence(JavaScriptNode node) {
        Node parent = node.getParent();
        if (parent instanceof InstrumentationNode) {
            parent = parent.getParent();
        }
        return parent instanceof SequenceNode;
    }
    */
    @Override
    public void probeAST(Instrumenter instrumenter, RootNode node) {
        node.accept(this);
    }

}

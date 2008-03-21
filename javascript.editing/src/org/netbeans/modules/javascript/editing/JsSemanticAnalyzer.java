/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.editing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;

/**
 * Semantically analyze a given JavaScript buffer
 * 
 * @todo E4X XML nodes
 * 
 * @todo Produce a function call hashmap of bad browser calls, and look up calls in semantic highlighting
 *   against the name map. Only if it matches, do a full FQN check and if so, do a browser delta.
 *
 * @author Tor Norbye
 */
public class JsSemanticAnalyzer implements SemanticAnalyzer {

    private boolean cancelled;
    private Map<OffsetRange, ColoringAttributes> semanticHighlights;

    public Map<OffsetRange, ColoringAttributes> getHighlights() {
        return semanticHighlights;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    public void cancel() {
        cancelled = true;
    }

    public void run(CompilationInfo info) throws Exception {
        resume();

        if (isCancelled()) {
            return;
        }

        JsParseResult rpr = AstUtilities.getParseResult(info);
        if (rpr == null) {
            return;
        }

        Node root = rpr.getRootNode();
        if (root == null) {
            return;
        }

        VariableVisitor visitor = rpr.getVariableVisitor();
        Map<OffsetRange, ColoringAttributes> highlights =
                new HashMap<OffsetRange, ColoringAttributes>(100);
        Collection<Node> unusedVars = visitor.getUnusedVars();
        for (Node node : unusedVars) {
            OffsetRange range = AstUtilities.getNameRange(node);
            highlights.put(range, ColoringAttributes.UNUSED);
        }

        Collection<Node> globalVars = visitor.getGlobalVars(false);
        for (Node node : globalVars) {
            String s = node.getString();
            OffsetRange range = AstUtilities.getNameRange(node);
            if (Character.isUpperCase(s.charAt(0))) {
                // A property which mimics a class
                highlights.put(range, ColoringAttributes.CLASS);
            } else {
                highlights.put(range, ColoringAttributes.GLOBAL);
            }
        }
        
        List<Node> regexps = new ArrayList<Node>();
        AstUtilities.addNodesByType(root, new int[] { Token.REGEXP, Token.FUNCNAME, Token.OBJLITNAME }, regexps);
        for (Node node : regexps) {
            OffsetRange range = AstUtilities.getNameRange(node);
            final int type = node.getType();
            if (type == Token.REGEXP) {
                highlights.put(range, ColoringAttributes.REGEXP);
            } else if (type == Token.OBJLITNAME) {
                 if (AstUtilities.isLabelledFunction(node)) {
                    highlights.put(range, ColoringAttributes.METHOD);
                 }
            } else {
                assert type == Token.FUNCNAME;
                highlights.put(range, ColoringAttributes.METHOD);
            }
        }
        
        if (isCancelled()) {
            return;
        }

        if (highlights.size() > 0) {
            if (rpr.getTranslatedSource() != null) {
                Map<OffsetRange, ColoringAttributes> translated = new HashMap<OffsetRange, ColoringAttributes>(2 * highlights.size());
                for (Map.Entry<OffsetRange, ColoringAttributes> entry : highlights.entrySet()) {
                    OffsetRange range = LexUtilities.getLexerOffsets(info, entry.getKey());
                    if (range != OffsetRange.NONE) {
                        translated.put(range, entry.getValue());
                    }
                }

                highlights = translated;
            }

            this.semanticHighlights = highlights;
        } else {
            this.semanticHighlights = null;
        }
    }
}

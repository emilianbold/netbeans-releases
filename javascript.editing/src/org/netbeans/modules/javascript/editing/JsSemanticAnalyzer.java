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
import java.util.Set;
import java.util.Map;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.javascript.editing.embedding.JsModel;
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
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;

    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
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

//        if (rpr.getEditedNode() != null && rpr.semanticHighlights != null) {
//            // Just perform incremental analysis
//            semanticHighlights = analyzeIncremental(info, rpr, root);
//        } else {
            semanticHighlights = analyzeFullTree(info, rpr, root);
//        }
//        rpr.semanticHighlights = semanticHighlights;
    }

    Map<OffsetRange, Set<ColoringAttributes>> analyzeFullTree(CompilationInfo info, JsParseResult rpr, Node root) {
        VariableVisitor visitor = rpr.getVariableVisitor();
        Map<OffsetRange, Set<ColoringAttributes>> highlights =
                new HashMap<OffsetRange, Set<ColoringAttributes>>(100);
        Collection<Node> unusedVars = visitor.getUnusedVars();
        for (Node node : unusedVars) {
            OffsetRange range = AstUtilities.getNameRange(node);
            highlights.put(range, ColoringAttributes.UNUSED_SET);
        }

        Collection<Node> globalVars = visitor.getGlobalVars(false);
        // I'm sanitizing keywords like "undefined" etc. by stripping the final character
        // to avoid a parser error. This gives me what is actually a global variable
        // symbol in the input, which gets highlighted here. We don't want that, so if
        // I see that the global variable sits immediately next to the sanitized range,
        // I don't highlight it.
        OffsetRange sanitizedRange = rpr.getSanitizedRange();
        boolean checkRange = sanitizedRange != OffsetRange.NONE && sanitizedRange.getLength() == 1;
        if (checkRange) {
            String sanitized = rpr.getSanitizedContents();
            if (sanitized != null && sanitized.length() > 0 && !Character.isLetter(sanitized.charAt(0))) {
                // If I'm clipping away things like "," or "." it wasn't a clipped keyword and
                // there's no need to avoid highlighting these symbols
                checkRange = false;
            }
        }
        for (Node node : globalVars) {
            String s = node.getString();
            //filter out generated code
            // and zero-length nodes (they may have names but these are generated)
            if (JsModel.isGeneratedIdentifier(s) || node.getSourceStart() == node.getSourceEnd()) {
                continue;
            }
            OffsetRange range = AstUtilities.getNameRange(node);
            if (checkRange && range.getEnd() == sanitizedRange.getStart()) {
                continue;
            }
            if (Character.isUpperCase(s.charAt(0))) {
                // A property which mimics a class
                highlights.put(range, ColoringAttributes.CLASS_SET);
            } else {
                highlights.put(range, ColoringAttributes.GLOBAL_SET);
            }
        }
        
        List<Node> regexps = new ArrayList<Node>();
        if (JsUtils.isEjsFile(info.getFileObject())) {
            // No E4X highlights in EJS files
            AstUtilities.addNodesByType(root, new int[] { Token.REGEXP, Token.FUNCNAME, Token.OBJLITNAME }, regexps);
        } else {
            AstUtilities.addNodesByType(root, new int[] { Token.REGEXP, Token.FUNCNAME, Token.OBJLITNAME, Token.E4X }, regexps);
        }
        for (Node node : regexps) {
            OffsetRange range = AstUtilities.getNameRange(node);
            if (node.isStringNode() && JsModel.isGeneratedIdentifier(node.getString())) {
                continue;
            }
            final int type = node.getType();
            if (type == Token.REGEXP) {
                highlights.put(range, ColoringAttributes.REGEXP_SET);
            } else if (type == Token.OBJLITNAME) {
                 if (AstUtilities.isLabelledFunction(node)) {
                    highlights.put(range, ColoringAttributes.METHOD_SET);
                 }
            } else if (type == Token.FUNCNAME) {
                highlights.put(range, ColoringAttributes.METHOD_SET);
            } else {
                assert type == Token.E4X;
                Node child = node.getFirstChild();
                if (child != null && child.getType() == Token.STRING) {
                    // It's an E4X string without embedded code (those would have
                    // a Token.ADD child instead of Token.STRING) which means we
                    // should be able to parse these guys and show some XML colors
                    // and other info.
                    // For now, just highlight.
                    highlights.put(range, ColoringAttributes.CUSTOM1_SET);

                    String xml = child.getString();
                    // Simple "parsing" of the String to identify element regions
                    parseXml(highlights, xml, child.getSourceStart());
                }
            }
        }
        
        if (isCancelled()) {
            return null;
        }

        if (highlights.size() > 0) {
            if (rpr.getTranslatedSource() != null) {
                Map<OffsetRange, Set<ColoringAttributes>> translated = new HashMap<OffsetRange, Set<ColoringAttributes>>(2 * highlights.size());
                for (Map.Entry<OffsetRange, Set<ColoringAttributes>> entry : highlights.entrySet()) {
                    OffsetRange range = LexUtilities.getLexerOffsets(info, entry.getKey());
                    if (range != OffsetRange.NONE) {
                        translated.put(range, entry.getValue());
                    }
                }

                highlights = translated;
            }

            return highlights;
        } else {
            return null;
        }
    }

    /**
     * Process XML in E4X. Not a full parse - just scan for elements and attributes; ignore
     * entities etc. Provides offsets for elements and attributes so we can highlight them.
     */
    private void parseXml(Map<OffsetRange, Set<ColoringAttributes>> highlights, String xml, int sourceStart) {
        int start = 0;

        final int IN_TEXT = 1;
        final int LOOKING_FOR_ELEM_START = 2;
        final int LOOKING_FOR_ELEM_END = 3;
        final int LOOKING_FOR_TEXT = 4;

        int state = IN_TEXT;

        for (int i = 0, n = xml.length(); i < n; i++) {
            char c = xml.charAt(i);
            if (state == IN_TEXT) {
                if (c == '<') {
                    // Finish text region
                    if (i > start) {
                        OffsetRange range = new OffsetRange(sourceStart + start, sourceStart + i);
                        highlights.put(range, ColoringAttributes.CUSTOM3_SET);
                    }

                    // Beginning of start or ending element
                    state = LOOKING_FOR_ELEM_START;
                }
            } else if (state == LOOKING_FOR_ELEM_START) {
                if (c != '/') {
                    start = i;
                    state = LOOKING_FOR_ELEM_END;
                }
            } else if (state == LOOKING_FOR_ELEM_END) {
                if (!Character.isLetterOrDigit(c) && c != '_' && c != ':') {
                    OffsetRange range = new OffsetRange(sourceStart + start, sourceStart + i);
                    highlights.put(range, ColoringAttributes.CUSTOM2_SET);

                    if (c == '>') {
                        state = IN_TEXT;
                        start = i+1;
                    } else {
                        state = LOOKING_FOR_TEXT;
                    }
                }
            } else if (state == LOOKING_FOR_TEXT) {
                if (c == '>') {
                    state = IN_TEXT;
                    start = i+1;
                }
            } else {
                assert false : state;
            }
        }
    }

    // Perform a partial analysis of the tree, only for the incrementally parsed function
    //Map<OffsetRange, Set<ColoringAttributes>> analyzeIncremental(CompilationInfo info, JsParseResult rpr, Node root) {
    //}
}

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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import org.jruby.nb.ast.AliasNode;
import org.jruby.nb.ast.ArgsNode;
import org.jruby.nb.ast.ArgumentNode;
import org.jruby.nb.ast.BlockArgNode;
import org.jruby.nb.ast.DAsgnNode;
import org.jruby.nb.ast.DVarNode;
import org.jruby.nb.ast.ForNode;
import org.jruby.nb.ast.ListNode;
import org.jruby.nb.ast.LocalAsgnNode;
import org.jruby.nb.ast.LocalVarNode;
import org.jruby.nb.ast.MethodDefNode;
import org.jruby.nb.ast.Node;
import org.jruby.nb.ast.NodeType;
import org.jruby.nb.ast.types.INameNode;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.ruby.lexer.LexUtilities;


/**
 * Walk through the JRuby AST and note interesting things
 * @todo Use the org.jruby.nb.ast.visitor.NodeVisitor interface
 * @todo Do mixins and includes trip up my unused private method detection code?
 * @todo Treat toplevel methods as private?
 * @todo Show unused highlighting for unused class variables:
 *    private_class_method
 *   See section 7.8 in http://www.rubycentral.com/faq/rubyfaq-7.html
 * @todo Handle java fully packaged names by not bolding "java" and "javax" method
 *   calls in Java projects
 * @todo I can do faster tree walking with a quick integer set of node types I'm
 *   interested in, or more specifically a set of node types I know I can prune:
 *   ArgNodes etc. 
 * @todo Stash unused variables in a list I can reference from a quickfix!
 * @author Tor Norbye
 */
public class RubySemanticAnalyzer implements SemanticAnalyzer {
    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;
    private static final Set<String> JAVA_PREFIXES = new HashSet<String>();
    static {
        JAVA_PREFIXES.add("java"); // NOI18N
        JAVA_PREFIXES.add("javax"); // NOI18N
        JAVA_PREFIXES.add("org"); // NOI18N
        JAVA_PREFIXES.add("com"); // NOI18N
    }

    public RubySemanticAnalyzer() {
    }

    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    public final synchronized void cancel() {
        cancelled = true;
    }

    public void run(CompilationInfo info) {
        resume();

        if (isCancelled()) {
            return;
        }

        RubyParseResult rpr = AstUtilities.getParseResult(info);
        if (rpr == null) {
            return;
        }

        Node root = rpr.getRootNode();
        if (root == null) {
            return;
        }

        Map<OffsetRange, Set<ColoringAttributes>> highlights =
            new HashMap<OffsetRange, Set<ColoringAttributes>>(100);

        AstPath path = new AstPath();
        path.descend(root);
        annotate(root, highlights, path, null, false);
        path.ascend();

        if (isCancelled()) {
            return;
        }

        if (highlights.size() > 0) {
            if (rpr.getTranslatedSource() != null) {
                Map<OffsetRange, Set<ColoringAttributes>> translated = new HashMap<OffsetRange,Set<ColoringAttributes>>(2*highlights.size());
                for (Map.Entry<OffsetRange,Set<ColoringAttributes>> entry : highlights.entrySet()) {
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

    /** Find unused local and dynamic variables */
    @SuppressWarnings("fallthrough")
    private void annotate(Node node, Map<OffsetRange,Set<ColoringAttributes>> highlights, AstPath path,
        List<String> parameters, boolean isParameter) {
        switch (node.nodeId) {
        case ARGSNODE: {
            isParameter = true;
            break;
        }
        case LOCALASGNNODE: {
            LocalAsgnNode lasgn = (LocalAsgnNode)node;
            Node method = AstUtilities.findLocalScope(node, path);

            boolean isUsed = isUsedInMethod(method, lasgn.getName(), isParameter);

            if (!isUsed) {
                OffsetRange range = AstUtilities.getLValueRange(lasgn);
                highlights.put(range, ColoringAttributes.UNUSED_SET);
            } else if (parameters != null) {
                String name = ((LocalAsgnNode)node).getName();

                if (parameters.contains(name)) {
                    OffsetRange range = AstUtilities.getNameRange(node);
                    highlights.put(range, ColoringAttributes.PARAMETER_SET);
                }
            }
            break;
        }
        case DASGNNODE: {
            DAsgnNode dasgn = (DAsgnNode)node;

            Node method = AstUtilities.findLocalScope(node, path);

            boolean isUsed = isUsedInMethod(method, dasgn.getName(), false);

            if (!isUsed) {
                OffsetRange range = AstUtilities.getLValueRange(dasgn);
                highlights.put(range, ColoringAttributes.UNUSED_SET);
            }
            
            break;
        }
        
        case DEFNNODE:
        case DEFSNODE: {
            MethodDefNode def = (MethodDefNode)node;
            parameters = AstUtilities.getDefArgs(def, true);

            if ((parameters != null) && (parameters.size() > 0)) {
                List<String> unused = new ArrayList<String>();

                for (String parameter : parameters) {
                    boolean isUsed = isUsedInMethod(node, parameter, true);

                    if (!isUsed) {
                        unused.add(parameter);
                    }
                }

                if (unused.size() > 0) {
                    annotateUnusedParameters(def, highlights, unused);
                    parameters.removeAll(unused);
                }

                if (parameters != null) {
                    if (parameters.size() == 0) {
                        parameters = null;
                    } else {
                        annotateParameters(def, highlights, parameters);
                    }
                }
            }

            highlightMethodName(node, highlights);
            break;
        }
        
        case LOCALVARNODE: {
            if (parameters != null) {
                if (parameters.contains(((LocalVarNode)node).getName())) {
                    OffsetRange range = AstUtilities.getRange(node);
                    highlights.put(range, ColoringAttributes.PARAMETER_SET);
                }
            }
            break;
        }
        
        case VCALLNODE:
            // FALLTHROUGH!
            if (JAVA_PREFIXES.contains(((INameNode)node).getName())) {
                // Skip highlighting "org" in "org.foo.Bar" etc.
                break;
            }
        //case CALLNODE:
        case FCALLNODE: {
            // CallNode seems overly aggressive - it will show all operators for example
            OffsetRange range = AstUtilities.getCallRange(node);
            highlights.put(range, ColoringAttributes.METHOD_SET);
            break;
        }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            path.descend(child);
            annotate(child, highlights, path, parameters, isParameter);
            path.ascend();
        }
    }

    private void annotateParameters(MethodDefNode node,
        Map<OffsetRange, Set<ColoringAttributes>> highlights, List<String> usedParameterNames) {
        List<Node> nodes = node.childNodes();

        for (Node c : nodes) {
            if (c.nodeId == NodeType.ARGSNODE) {
                ArgsNode an = (ArgsNode)c;

                if (an.getRequiredArgsCount() > 0) {
                    List<Node> args = an.childNodes();

                    for (Node arg : args) {
                        if (arg instanceof ListNode) { // Many specific types
                            List<Node> args2 = arg.childNodes();

                            for (Node arg2 : args2) {
                                if (arg2.nodeId == NodeType.ARGUMENTNODE) {
                                    if (usedParameterNames.contains(((ArgumentNode)arg2).getName())) {
                                        OffsetRange range = AstUtilities.getRange(arg2);
                                        highlights.put(range, ColoringAttributes.PARAMETER_SET);
                                    }
                                } else if (arg2.nodeId == NodeType.LOCALASGNNODE) {
                                    if (usedParameterNames.contains(((LocalAsgnNode)arg2).getName())) {
                                        OffsetRange range = AstUtilities.getNameRange(arg2);
                                        highlights.put(range, ColoringAttributes.PARAMETER_SET);
                                    }
                                }
                            }
                        }
                    }
                }

                // Rest args
                if (an.getRestArgNode() != null) {
                    ArgumentNode bn = an.getRestArgNode();

                    if (usedParameterNames.contains(bn.getName())) {
                        OffsetRange range = AstUtilities.getRange(bn);
                        highlights.put(range, ColoringAttributes.PARAMETER_SET);
                    }
                }

                // Block args
                if (an.getRestArgNode() != null) {
                    ArgumentNode bn = an.getRestArgNode();

                    if (usedParameterNames.contains(bn.getName())) {
                        OffsetRange range = AstUtilities.getRange(bn);
                        highlights.put(range, ColoringAttributes.PARAMETER_SET);
                    }
                }

                // Block args
                if (an.getBlockArgNode() != null) {
                    BlockArgNode bn = an.getBlockArgNode();

                    if (usedParameterNames.contains(bn.getName())) {
                        OffsetRange range = AstUtilities.getRange(bn);
                        highlights.put(range, ColoringAttributes.PARAMETER_SET);
                    }
                }
            }
        }
    }

    private void annotateUnusedParameters(MethodDefNode node,
        Map<OffsetRange, Set<ColoringAttributes>> highlights, List<String> names) {
        List<Node> nodes = node.childNodes();

        for (Node c : nodes) {
            if (c.nodeId == NodeType.ARGSNODE) {
                ArgsNode an = (ArgsNode)c;

                if (an.getRequiredArgsCount() > 0) {
                    List<Node> args = an.childNodes();

                    for (Node arg : args) {
                        if (arg instanceof ListNode) { // Check subclasses
                            List<Node> args2 = arg.childNodes();

                            for (Node arg2 : args2) {
                                if (arg2.nodeId == NodeType.ARGUMENTNODE) {
                                    if (names.contains(((ArgumentNode)arg2).getName())) {
                                        OffsetRange range = AstUtilities.getRange(arg2);
                                        highlights.put(range, ColoringAttributes.UNUSED_SET);
                                    }
                                } else if (arg2.nodeId == NodeType.LOCALASGNNODE) {
                                    if (names.contains(((LocalAsgnNode)arg2).getName())) {
                                        OffsetRange range = AstUtilities.getNameRange(arg2);
                                        highlights.put(range, ColoringAttributes.UNUSED_SET);
                                    }
                                }
                            }
                        }
                    }
                }

                // Rest args
                if (an.getRestArgNode() != null) {
                    ArgumentNode bn = an.getRestArgNode();

                    if (names.contains(bn.getName())) {
                        OffsetRange range = AstUtilities.getRange(bn);
                        highlights.put(range, ColoringAttributes.UNUSED_SET);
                    }
                }

                if (an.getBlockArgNode() != null) {
                    BlockArgNode bn = an.getBlockArgNode();

                    if (names.contains(bn.getName())) {
                        OffsetRange range = AstUtilities.getRange(bn);
                        highlights.put(range, ColoringAttributes.UNUSED_SET);
                    }
                }
            }
        }
    }

    private boolean isUsedInMethod(Node node, String targetName, boolean isParameter) {
        switch (node.nodeId) {
        case LOCALVARNODE: {
            if (node.nodeId == NodeType.LOCALVARNODE) {
                String name = ((LocalVarNode)node).getName();

                if (targetName.equals(name)) {
                    return true;
                }
            }
            break;
        }
        case FORNODE: {
            // XXX This is no longer necessary, right?
            // Workaround for the fact that ForNode's childNodes implementation
            // is wrong - Tom is committing a fix; this is until we pick that
            // fix (SVN #3561)  up
            Node iterNode = ((ForNode)node).getIterNode();
            if (iterNode instanceof INameNode) {
                if (targetName.equals(((INameNode)iterNode).getName())) {
                    return true;
                }
            }
            break;
        }
        case DVARNODE:
            if (targetName.equals(((DVarNode)node).getName())) {
                return true;
            }
            break;
        case ALIASNODE: {
            AliasNode an = (AliasNode)node;

            if (an.getOldName().equals(targetName)) {
                return true;
            }
            break;
        }
        case ZSUPERNODE:
            // Super with no arguments passes arguments to parent so consider
            // the parameters used
            if (isParameter) {
                return true;
            }
            break;
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            // The "outer" foo here is unused - we shouldn't
            // recurse into method bodies when doing unused detection
            // foo = 1; def bar; foo = 2; print foo; end;
            if (child.nodeId == NodeType.DEFSNODE || child.nodeId == NodeType.DEFNNODE) {
                continue;
            }

            boolean used = isUsedInMethod(child, targetName, isParameter);

            if (used) {
                return true;
            }
        }

        return false;
    }

    private void highlightMethodName(Node node, Map<OffsetRange, Set<ColoringAttributes>> highlights) {
        OffsetRange range = AstUtilities.getFunctionNameRange(node);

        if (range != OffsetRange.NONE) {
            if (!highlights.containsKey(range)) { // Don't block out already annotated private methods
                highlights.put(range, ColoringAttributes.METHOD_SET);
            }
        }
    }
}

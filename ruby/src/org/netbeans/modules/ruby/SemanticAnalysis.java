/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jruby.ast.AliasNode;
import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.BlockArgNode;
import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.ForNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.types.INameNode;
import org.netbeans.api.gsf.ColoringAttributes;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.SemanticAnalyzer;


/**
 * Walk through the JRuby AST and note interesting things
 * @todo Use the org.jruby.ast.visitor.NodeVisitor interface
 * @todo Do mixins and includes trip up my unused private method detection code?
 * @todo Treat toplevel methods as private?
 * @todo Show unused highlighting for unused class variables:
 *    private_class_method
 *   See section 7.8 in http://www.rubycentral.com/faq/rubyfaq-7.html
 * @author Tor Norbye
 */
public class SemanticAnalysis implements SemanticAnalyzer {
    private boolean cancelled;
    private Map<OffsetRange, ColoringAttributes> semanticHighlights;

    public SemanticAnalysis() {
    }

    public Map<OffsetRange, ColoringAttributes> getHighlights() {
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

        Node root = AstUtilities.getRoot(info);

        if (root == null) {
            return;
        }

        Map<OffsetRange, ColoringAttributes> highlights =
            new HashMap<OffsetRange, ColoringAttributes>(100);

        AstPath path = new AstPath();
        path.descend(root);
        annotate(root, highlights, path, null);
        path.ascend();

        if (isCancelled()) {
            return;
        }

        if (highlights.size() > 0) {
            this.semanticHighlights = highlights;
        } else {
            this.semanticHighlights = null;
        }
    }

    /** Find unused local and dynamic variables */
    @SuppressWarnings("unchecked")
    private void annotate(Node node, Map<OffsetRange, ColoringAttributes> highlights, AstPath path,
        List<String> parameters) {
        switch (node.nodeId) {
        case NodeTypes.LOCALASGNNODE: {
            LocalAsgnNode lasgn = (LocalAsgnNode)node;
            Node method = AstUtilities.findLocalScope(node, path);

            boolean isUsed = isUsedInMethod(method, lasgn.getName());

            if (!isUsed) {
                OffsetRange range = AstUtilities.getLValueRange(lasgn);
                highlights.put(range, ColoringAttributes.UNUSED);
            } else if (parameters != null) {
                String name = ((LocalAsgnNode)node).getName();

                if (parameters.contains(name)) {
                    OffsetRange range = AstUtilities.getRange(node);
                    // Adjust end offset to only include the left hand size
                    range = new OffsetRange(range.getStart(), range.getStart() + name.length());
                    highlights.put(range, ColoringAttributes.PARAMETER);
                }
            }
            break;
        }
        case NodeTypes.DASGNNODE: {
            DAsgnNode dasgn = (DAsgnNode)node;

            Node method = AstUtilities.findLocalScope(node, path);

            boolean isUsed = isUsedInMethod(method, dasgn.getName());

            if (!isUsed) {
                OffsetRange range = AstUtilities.getLValueRange(dasgn);
                highlights.put(range, ColoringAttributes.UNUSED);
            }
            
            break;
        }
        
        case NodeTypes.DEFNNODE:
        case NodeTypes.DEFSNODE: {
            MethodDefNode def = (MethodDefNode)node;
            parameters = AstUtilities.getDefArgs(def, true);

            if ((parameters != null) && (parameters.size() > 0)) {
                List<String> unused = new ArrayList();

                for (String parameter : parameters) {
                    boolean isUsed = isUsedInMethod(node, parameter);

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
        
        case NodeTypes.LOCALVARNODE: {
            if (parameters != null) {
                if (parameters.contains(((LocalVarNode)node).getName())) {
                    OffsetRange range = AstUtilities.getRange(node);
                    highlights.put(range, ColoringAttributes.PARAMETER);
                }
            }
            break;
        }
        
        case NodeTypes.FCALLNODE:
        //case NodeTypes.CALLNODE:
        case NodeTypes.VCALLNODE: {
            // CallNode seems overly aggressive - it will show all operators for example
            OffsetRange range = AstUtilities.getCallRange(node);
            highlights.put(range, ColoringAttributes.METHOD);
            break;
        }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            path.descend(child);
            annotate(child, highlights, path, parameters);
            path.ascend();
        }
    }

    @SuppressWarnings("unchecked")
    private void annotateParameters(MethodDefNode node,
        Map<OffsetRange, ColoringAttributes> highlights, List<String> usedParameterNames) {
        List<Node> nodes = (List<Node>)node.childNodes();

        for (Node c : nodes) {
            if (c.nodeId == NodeTypes.ARGSNODE) {
                ArgsNode an = (ArgsNode)c;

                if (an.getArgsCount() > 0) {
                    List<Node> args = (List<Node>)an.childNodes();

                    for (Node arg : args) {
                        if (arg instanceof ListNode) { // Many specific types
                            List<Node> args2 = (List<Node>)arg.childNodes();

                            for (Node arg2 : args2) {
                                if (arg2.nodeId == NodeTypes.ARGUMENTNODE) {
                                    if (usedParameterNames.contains(((ArgumentNode)arg2).getName())) {
                                        OffsetRange range = AstUtilities.getRange(arg2);
                                        highlights.put(range, ColoringAttributes.PARAMETER);
                                    }
                                } else if (arg2.nodeId == NodeTypes.LOCALASGNNODE) {
                                    if (usedParameterNames.contains(((LocalAsgnNode)arg2).getName())) {
                                        OffsetRange range = AstUtilities.getRange(arg2);
                                        highlights.put(range, ColoringAttributes.PARAMETER);
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
                        highlights.put(range, ColoringAttributes.PARAMETER);
                    }
                }

                // Block args
                if (an.getRestArgNode() != null) {
                    ArgumentNode bn = an.getRestArgNode();

                    if (usedParameterNames.contains(bn.getName())) {
                        OffsetRange range = AstUtilities.getRange(bn);
                        highlights.put(range, ColoringAttributes.PARAMETER);
                    }
                }

                // Block args
                if (an.getBlockArgNode() != null) {
                    BlockArgNode bn = an.getBlockArgNode();

                    if (usedParameterNames.contains(bn.getName())) {
                        OffsetRange range = AstUtilities.getRange(bn);
                        highlights.put(range, ColoringAttributes.PARAMETER);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void annotateUnusedParameters(MethodDefNode node,
        Map<OffsetRange, ColoringAttributes> highlights, List<String> names) {
        List<Node> nodes = (List<Node>)node.childNodes();

        for (Node c : nodes) {
            if (c.nodeId == NodeTypes.ARGSNODE) {
                ArgsNode an = (ArgsNode)c;

                if (an.getArgsCount() > 0) {
                    List<Node> args = (List<Node>)an.childNodes();

                    for (Node arg : args) {
                        if (arg instanceof ListNode) { // Check subclasses
                            List<Node> args2 = (List<Node>)arg.childNodes();

                            for (Node arg2 : args2) {
                                if (arg2.nodeId == NodeTypes.ARGUMENTNODE) {
                                    if (names.contains(((ArgumentNode)arg2).getName())) {
                                        OffsetRange range = AstUtilities.getRange(arg2);
                                        highlights.put(range, ColoringAttributes.UNUSED);
                                    }
                                } else if (arg2.nodeId == NodeTypes.LOCALASGNNODE) {
                                    if (names.contains(((LocalAsgnNode)arg2).getName())) {
                                        OffsetRange range = AstUtilities.getRange(arg2);
                                        highlights.put(range, ColoringAttributes.UNUSED);
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
                        highlights.put(range, ColoringAttributes.UNUSED);
                    }
                }

                if (an.getBlockArgNode() != null) {
                    BlockArgNode bn = an.getBlockArgNode();

                    if (names.contains(bn.getName())) {
                        OffsetRange range = AstUtilities.getRange(bn);
                        highlights.put(range, ColoringAttributes.UNUSED);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isUsedInMethod(Node node, String targetName) {
        if (node.nodeId == NodeTypes.LOCALVARNODE) {
            String name = ((LocalVarNode)node).getName();

            if (targetName.equals(name)) {
                return true;
            }
        } else if (node.nodeId == NodeTypes.FORNODE) {
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
        } else if (node.nodeId == NodeTypes.DVARNODE) {
            if (targetName.equals(((DVarNode)node).getName())) {
                return true;
            }
        } else if (node.nodeId == NodeTypes.ALIASNODE) {
            AliasNode an = (AliasNode)node;

            if (an.getOldName().equals(targetName)) {
                return true;
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            // The "outer" foo here is unused - we shouldn't
            // recurse into method bodies when doing unused detection
            // foo = 1; def bar; foo = 2; print foo; end;
            if (child.nodeId == NodeTypes.DEFSNODE || child.nodeId == NodeTypes.DEFNNODE) {
                continue;
            }

            boolean used = isUsedInMethod(child, targetName);

            if (used) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private void highlightMethodName(Node node, Map<OffsetRange, ColoringAttributes> highlights) {
        OffsetRange range = AstUtilities.getFunctionNameRange(node);

        if (range != OffsetRange.NONE) {
            if (!highlights.containsKey(range)) { // Don't block out already annotated private methods
                highlights.put(range, ColoringAttributes.METHOD);
            }
        }
    }
}

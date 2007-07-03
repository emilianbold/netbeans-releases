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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jruby.ast.AliasNode;
import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.BlockArgNode;
import org.jruby.ast.ClassVarAsgnNode;
import org.jruby.ast.ClassVarDeclNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.ConstDeclNode;
import org.jruby.ast.ConstNode;
import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.GlobalAsgnNode;
import org.jruby.ast.GlobalVarNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.types.INameNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.InstantRenamer;
import org.netbeans.api.gsf.OffsetRange;
import org.openide.util.NbBundle;


/**
 * Handle renaming of local elements
 * @todo I should be able to rename top-level methods as well since they
 *   are private
 *
 * @author Tor Norbye
 */
public class RenameHandler implements InstantRenamer {
    public RenameHandler() {
    }

    public boolean isRenameAllowed(CompilationInfo info, int caretOffset,
        String[] explanationRetValue) {
        Node root = AstUtilities.getRoot(info);

        if (root == null) {
            explanationRetValue[0] = NbBundle.getMessage(RenameHandler.class, "NoRenameWithErrors");

            return false;
        }

        AstPath path = new AstPath(root, caretOffset);
        Node closest = path.leaf();

        if (closest instanceof LocalVarNode || closest instanceof LocalAsgnNode ||
                closest instanceof DVarNode || closest instanceof DAsgnNode ||
                closest instanceof BlockArgNode) {
            return true;
        }

        if (closest instanceof ArgumentNode) {
            Node parent = path.leafParent();

            if (parent != null) {
                // Make sure it's not a method name
                if (!(parent instanceof MethodDefNode)) {
                    return true;
                }
            }
        }

        //explanationRetValue[0] = NbBundle.getMessage(RenameHandler.class, "NoRename");
        //return false;
        if (closest instanceof InstAsgnNode || closest instanceof InstVarNode ||
                closest instanceof ClassVarDeclNode || closest instanceof ClassVarNode ||
                closest instanceof ClassVarAsgnNode || closest instanceof GlobalAsgnNode ||
                closest instanceof GlobalVarNode || closest instanceof ConstDeclNode ||
                closest instanceof ConstNode || closest instanceof MethodDefNode ||
                AstUtilities.isCall(closest) || closest instanceof ArgumentNode ||
                closest instanceof Colon2Node || closest instanceof SymbolNode ||
                closest instanceof AliasNode) {
            // TODO - what about the string arguments in an alias node? Gotta check those
            return true;
        }

        return false;
    }

    public Set<OffsetRange> getRenameRegions(CompilationInfo info, int caretOffset) {
        Node root = AstUtilities.getRoot(info);

        if (root == null) {
            return Collections.emptySet();
        }

        Set<OffsetRange> regions = new HashSet<OffsetRange>();

        AstPath path = new AstPath(root, caretOffset);
        Node closest = path.leaf();

        if (closest instanceof LocalVarNode || closest instanceof LocalAsgnNode) {
            // A local variable read or a parameter read, or an assignment to one of these
            String name = ((INameNode)closest).getName();
            Node method = AstUtilities.findMethod(path);

            if (method == null) {
                method = AstUtilities.findBlock(path);
            }

            if (method == null) {
                // Use parent, possibly Grand Parent if we have a newline node in the way
                method = path.leafParent();

                if (method instanceof NewlineNode) {
                    method = path.leafGrandParent();
                }

                if (method == null) {
                    method = closest;
                }
            }

            addLocals(method, name, regions);
        } else if (closest instanceof DVarNode || closest instanceof DAsgnNode) {
            // A dynamic variable read or assignment
            String name = ((INameNode)closest).getName();
            Node block = AstUtilities.findBlock(path);

            if (block == null) {
                // Use parent
                block = path.leafParent();

                if (block == null) {
                    block = closest;
                }
            }

            addDynamicVars(block, name, regions);
        } else if (closest instanceof ArgumentNode || closest instanceof BlockArgNode) {
            // A method name (if under a DefnNode or DefsNode) or a parameter (if indirectly under an ArgsNode)
            String name = ((INameNode)closest).getName();

            Node parent = path.leafParent();

            if (parent != null) {
                // Make sure it's a parameter, not a method
                if (!(parent instanceof MethodDefNode)) {
                    // Parameter (check to see if its under ArgumentNode)
                    Node method = AstUtilities.findMethod(path);

                    if (method == null) {
                        method = AstUtilities.findBlock(path);
                    }

                    if (method == null) {
                        // Use parent, possibly Grand Parent if we have a newline node in the way
                        method = path.leafParent();

                        if (method instanceof NewlineNode) {
                            method = path.leafGrandParent();
                        }

                        if (method == null) {
                            method = closest;
                        }
                    }

                    addLocals(method, name, regions);
                }
            }
        }

        return regions;
    }

    @SuppressWarnings("unchecked")
    private void addLocals(Node node, String name, Set<OffsetRange> ranges) {
        if (node instanceof LocalVarNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                ranges.add(range);
            }
        } else if (node instanceof LocalAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                // Adjust end offset to only include the left hand size
                range = new OffsetRange(range.getStart(), range.getStart() + name.length());
                ranges.add(range);
            }
        } else if (node instanceof ArgsNode) {
            ArgsNode an = (ArgsNode)node;

            if (an.getArgsCount() > 0) {
                List<Node> args = (List<Node>)an.childNodes();

                for (Node arg : args) {
                    if (arg instanceof ListNode) {
                        List<Node> args2 = (List<Node>)arg.childNodes();

                        for (Node arg2 : args2) {
                            if (arg2 instanceof ArgumentNode) {
                                if (((ArgumentNode)arg2).getName().equals(name)) {
                                    OffsetRange range = AstUtilities.getRange(arg2);
                                    ranges.add(range);
                                }
                            } else if (arg2 instanceof LocalAsgnNode) {
                                if (((LocalAsgnNode)arg2).getName().equals(name)) {
                                    OffsetRange range = AstUtilities.getRange(arg2);
                                    ranges.add(range);
                                }
                            }
                        }
                    }
                }
            }

            // Rest args
            if (an.getRestArgNode() != null) {
                ArgumentNode bn = an.getRestArgNode();

                if (bn.getName().equals(name)) {
                    ISourcePosition pos = bn.getPosition();

                    // +1: Skip "*" and "&" prefix
                    OffsetRange range =
                        new OffsetRange(pos.getStartOffset() + 1, pos.getEndOffset());
                    ranges.add(range);
                }
            }

            if (an.getBlockArgNode() != null) {
                BlockArgNode bn = an.getBlockArgNode();

                if (bn.getName().equals(name)) {
                    ISourcePosition pos = bn.getPosition();

                    // +1: Skip "*" and "&" prefix
                    OffsetRange range =
                        new OffsetRange(pos.getStartOffset() + 1, pos.getEndOffset());
                    ranges.add(range);
                }
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            addLocals(child, name, ranges);
        }
    }

    @SuppressWarnings("unchecked")
    private void addDynamicVars(Node node, String name, Set<OffsetRange> ranges) {
        if (node instanceof DVarNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                ranges.add(range);
            }
        } else if (node instanceof DAsgnNode) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                // TODO - AstUtility for this
                // Adjust end offset to only include the left hand size
                range = new OffsetRange(range.getStart(), range.getStart() + name.length());
                ranges.add(range);
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            addDynamicVars(child, name, ranges);
        }
    }
}

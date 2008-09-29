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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jruby.nb.ast.ArgsNode;
import org.jruby.nb.ast.ArgumentNode;
import org.jruby.nb.ast.BlockArgNode;
import org.jruby.nb.ast.ListNode;
import org.jruby.nb.ast.LocalAsgnNode;
import org.jruby.nb.ast.LocalVarNode;
import org.jruby.nb.ast.MethodDefNode;
import org.jruby.nb.ast.Node;
import org.jruby.nb.ast.NodeType;
import org.jruby.nb.ast.types.INameNode;
import org.jruby.nb.lexer.yacc.ISourcePosition;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.InstantRenamer;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.util.NbBundle;


/**
 * Handle renaming of local elements
 * @todo I should be able to rename top-level methods as well since they
 *   are private
 * @todo Rename |j| in the following will only rename "j" inside the block!
 * <pre>
i = 50
j = 200
k = 100
x = [1,2,3]
x.each do |j|
  puts j
end
puts j
 * </pre>
 * @todo When you fix, make sure BlockarReuse is also fixed!
 * @todo Try renaming "hello" in the exception here; my code is confused
 *   about what I'm renaming (aliases method name) and the refactoring dialog
 *   name is wrong! This is happening because it's also changing GlobalAsgnNode for $!
 *   but its parent is LocalAsgnNode, and -its- -grand- parent is a RescueBodyNode! 
 *   I should special case this!
 * <pre>
def hello
  begin
    ex = 50
    puts "test"
  
  rescue Exception => hello
    puts hello
  end
end
 *
 * </pre>
 *
 * @author Tor Norbye
 */
public class RubyRenameHandler implements InstantRenamer {
    public RubyRenameHandler() {
    }

    public boolean isRenameAllowed(CompilationInfo info, int caretOffset,
        String[] explanationRetValue) {
        Node root = AstUtilities.getRoot(info);

        if (root == null) {
            explanationRetValue[0] = NbBundle.getMessage(RubyRenameHandler.class, "NoRenameWithErrors");

            return false;
        }
        
        int astOffset = AstUtilities.getAstOffset(info, caretOffset);
        if (astOffset == -1) {
            return false;
        }

        AstPath path = new AstPath(root, astOffset);
        Node closest = path.leaf();
        if (closest == null) {
            return false;
        }

        if (closest.nodeId == NodeType.LOCALVARNODE || closest.nodeId == NodeType.LOCALASGNNODE ||
                closest.nodeId == NodeType.DVARNODE || closest.nodeId == NodeType.DASGNNODE ||
                closest.nodeId == NodeType.BLOCKARGNODE) {
            return true;
        }

        if (closest.nodeId == NodeType.ARGUMENTNODE) {
            Node parent = path.leafParent();

            if (parent != null) {
                // Make sure it's not a method name
                if (!(parent instanceof MethodDefNode)) {
                    return true;
                }
            }
        }

        //explanationRetValue[0] = NbBundle.getMessage(RubyRenameHandler.class, "NoRename");
        //return false;
        switch (closest.nodeId) {
        case INSTASGNNODE:
        case INSTVARNODE:
        case CLASSVARDECLNODE:
        case CLASSVARNODE:
        case CLASSVARASGNNODE:
        case GLOBALASGNNODE:
        case GLOBALVARNODE:
        case CONSTDECLNODE:
        case CONSTNODE:
        case DEFNNODE:
        case DEFSNODE:
        case FCALLNODE:
        case CALLNODE:
        case VCALLNODE:
        case ARGUMENTNODE:
        case COLON2NODE:
        case COLON3NODE:
        case ALIASNODE:
        case SYMBOLNODE:
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

        int astOffset = AstUtilities.getAstOffset(info, caretOffset);
        if (astOffset == -1) {
            return Collections.emptySet();
        }

        AstPath path = new AstPath(root, astOffset);
        Node closest = path.leaf();
        if (closest == null) {
            return Collections.emptySet();
        }

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

                if (method.nodeId == NodeType.NEWLINENODE) {
                    method = path.leafGrandParent();
                }

                if (method == null) {
                    method = closest;
                }
            }

            addLocals(info, method, name, regions);
        } else if (closest.nodeId == NodeType.DVARNODE || closest.nodeId == NodeType.DASGNNODE) {
            // A dynamic variable read or assignment
            String name = ((INameNode)closest).getName();
            List<Node> applicableBlocks = AstUtilities.getApplicableBlocks(path, true);
            for (Node block : applicableBlocks) {
                addDynamicVars(info, block, name, regions);
            }
        } else if (closest.nodeId == NodeType.ARGUMENTNODE || closest.nodeId == NodeType.BLOCKARGNODE) {
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

                        if (method.nodeId == NodeType.NEWLINENODE) {
                            method = path.leafGrandParent();
                        }

                        if (method == null) {
                            method = closest;
                        }
                    }

                    addLocals(info, method, name, regions);
                }
            }
        }

        return regions;
    }

    private void addLocals(CompilationInfo info, Node node, String name, Set<OffsetRange> ranges) {
        if (node.nodeId == NodeType.LOCALVARNODE) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                range = LexUtilities.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    ranges.add(range);
                }
            }
        } else if (node.nodeId == NodeType.LOCALASGNNODE) {
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                // Adjust end offset to only include the left hand size
                range = new OffsetRange(range.getStart(), range.getStart() + name.length());
                range = LexUtilities.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    ranges.add(range);
                }
            }
        } else if (node.nodeId == NodeType.ARGSNODE) {
            ArgsNode an = (ArgsNode)node;

            if (an.getRequiredArgsCount() > 0) {
                List<Node> args = an.childNodes();

                for (Node arg : args) {
                    if (arg instanceof ListNode) {
                        List<Node> args2 = arg.childNodes();

                        for (Node arg2 : args2) {
                            if (arg2.nodeId == NodeType.ARGUMENTNODE) {
                                if (((ArgumentNode)arg2).getName().equals(name)) {
                                    OffsetRange range = AstUtilities.getRange(arg2);
                                    range = LexUtilities.getLexerOffsets(info, range);
                                    if (range != OffsetRange.NONE) {
                                        ranges.add(range);
                                    }
                                }
                            } else if (arg2.nodeId == NodeType.LOCALASGNNODE) {
                                if (((LocalAsgnNode)arg2).getName().equals(name)) {
                                    OffsetRange range = AstUtilities.getRange(arg2);
                                    range = LexUtilities.getLexerOffsets(info, range);
                                    if (range != OffsetRange.NONE) {
                                        ranges.add(range);
                                    }
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
                    range = LexUtilities.getLexerOffsets(info, range);
                    if (range != OffsetRange.NONE) {
                        ranges.add(range);
                    }
                }
            }

            if (an.getBlockArgNode() != null) {
                BlockArgNode bn = an.getBlockArgNode();

                if (bn.getName().equals(name)) {
                    ISourcePosition pos = bn.getPosition();

                    // +1: Skip "*" and "&" prefix
                    OffsetRange range =
                        new OffsetRange(pos.getStartOffset() + 1, pos.getEndOffset());
                    range = LexUtilities.getLexerOffsets(info, range);
                    if (range != OffsetRange.NONE) {
                        ranges.add(range);
                    }
                }
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            addLocals(info, child, name, ranges);
        }
    }

    // TODO: Check
    //  quick tip renaming
    //  unused detection
    //  occurrences marking
    //  code completion
    //  live code templates
    // ...anyone else who calls findBlock
    //
    // Test both parent blocks, sibling blocks and descendant blocks
    // Make sure the "isUsed" detection is smarter too.
    
    private void addDynamicVars(CompilationInfo info, Node node, String name, Set<OffsetRange> ranges) {
        switch (node.nodeId) {
        case DVARNODE:
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                range = LexUtilities.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    ranges.add(range);
                }
            }
            break;
        case DASGNNODE:
            if (((INameNode)node).getName().equals(name)) {
                OffsetRange range = AstUtilities.getRange(node);
                // TODO - AstUtility for this
                // Adjust end offset to only include the left hand size
                range = new OffsetRange(range.getStart(), range.getStart() + name.length());
                range = LexUtilities.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    ranges.add(range);
                }
            }
            break;
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            switch (child.nodeId) {
            case ITERNODE:
            //case BLOCKNODE:
            case DEFNNODE:
            case DEFSNODE:
            case CLASSNODE:
            case SCLASSNODE:
            case MODULENODE:
                continue;
            }

            addDynamicVars(info, child, name, ranges);
        }
    }
}

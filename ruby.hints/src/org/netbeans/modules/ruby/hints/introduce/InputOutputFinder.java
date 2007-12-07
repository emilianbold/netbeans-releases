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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.hints.introduce;

import org.netbeans.modules.ruby.ParseTreeVisitor;
import org.netbeans.modules.ruby.ParseTreeWalker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jruby.ast.ArgsNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.MultipleAsgnNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.types.INameNode;

/** 
 * This visitor computes the set of input and output variables required by
 * a code block for extract method.
 * In particular, it tracks the local variable assignments inside the method,
 * and checks which are used outside of the method (which would make it an
 * output variable) and similarly, which variables are used inside the method
 * before getting assigned (which would make it an input variable).
 * @author Tor Norbye
 */
class InputOutputVarFinder implements ParseTreeVisitor {
    //private enum When { BEFORE, DURING, AFTER };
    private static final int WHEN_BEFORE = 0;
    private static final int WHEN_DURING = 1;
    private static final int WHEN_AFTER = 2;

    private final Node startNode;
    private final Node endNode;
    private final List<Node> applicableBlocks;
    private int when = WHEN_BEFORE;
    private int ifs;
    private Node currentBlock;
    private final List<Node> blockStack = new ArrayList<Node>(); // JDK16: Use Deque

    private Map<Node,UsageScope> blockScopes = new HashMap<Node,UsageScope>();
    private UsageScope methodScope = new UsageScope(null);
    private UsageScope blockScope;
    
    /** The node ranges are inclusive */
    InputOutputVarFinder(Node startNode, Node endNode, List<Node> applicableBlocks) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.applicableBlocks = applicableBlocks;
    }

    public Set<String> getInputVars() {
        UsageScope scope = methodScope;
        for (UsageScope s : blockScopes.values()) {
            if (s.block != null && !applicableBlocks.contains(s.block)) {
                continue;
            }
            scope.merge(s);
        }
        
        Set<String> inputs = new HashSet<String>(scope.readDuring);
        // But not read before
        inputs.removeAll(scope.writtenBeforeReadDuring);

        // Also need to pass in any variables I'm modifying that are read after
        Set<String> outputs = new HashSet<String>(scope.writtenDuring);
        outputs.retainAll(scope.readAfter);
        Set<String> extraOutputs = new HashSet<String>(scope.writtenBefore);
        extraOutputs.retainAll(outputs);
        // unless they are written before read
        extraOutputs.removeAll(scope.writtenBeforeReadDuring);
        inputs.addAll(extraOutputs);

        return inputs;
    }

    public Set<String> getOutputVars() {
        UsageScope scope = methodScope;
        for (UsageScope s : blockScopes.values()) {
            if (s.block != null && !applicableBlocks.contains(s.block)) {
                continue;
            }
            scope.merge(s);
        }
        
        Set<String> outputs = new HashSet<String>(scope.writtenDuring);
        outputs.retainAll(scope.readAfter);

        return outputs;
    }

    public boolean visit(Node node) {
        if (node == startNode) {
            when = WHEN_DURING;
        } 
        switch (node.nodeId) {
        case NodeTypes.ARGSNODE: {
            assert when == WHEN_BEFORE; // Is this true when I extract a whole method? I can't do that, right?

            // TODO - use AstUtilities.getDefArgs here - but avoid hitting them twice!
            //List<String> parameters = AstUtilities.getDefArgs(def, true);
            // However, I've gotta find the parameter nodes themselves too!
            ArgsNode an = (ArgsNode)node;

            if (an.getArgsCount() > 0) {
                @SuppressWarnings("unchecked")
                List<Node> args = (List<Node>)an.childNodes();

                for (Node arg : args) {
                    if (arg instanceof ListNode) {
                        @SuppressWarnings("unchecked")
                        List<Node> args2 = (List<Node>)arg.childNodes();

                        for (Node arg2 : args2) {
                            if (arg2.nodeId == NodeTypes.ARGUMENTNODE) {
                                methodScope.write(((INameNode)arg2).getName());
                            } else if (arg2.nodeId == NodeTypes.LOCALASGNNODE) {
                                methodScope.write(((INameNode)arg2).getName());
                            }
                        }
                    }
                }
            }

            // Rest args
            if (an.getRestArgNode() != null) {
                String name = an.getRestArgNode().getName();
                methodScope.write(name);
            }

            // Block args
            if (an.getBlockArgNode() != null) {
                String name = an.getBlockArgNode().getName();
                methodScope.write(name);
            }

            // Skip argsnode since we've already processed it
            return true;
        }

        case NodeTypes.ITERNODE: {
            blockStack.add(node);
            currentBlock = node;
            blockScope = new UsageScope(currentBlock);
            blockScopes.put(currentBlock, blockScope);
            
            if (when == WHEN_DURING) {
                applicableBlocks.add(node);
            }
            break;
        }
        case NodeTypes.DEFNNODE:
        case NodeTypes.DEFSNODE:
        case NodeTypes.CLASSNODE:
        case NodeTypes.SCLASSNODE:
        case NodeTypes.MODULENODE:
            // We're probably extracting from within the top level of a file or class;
            // don't look into methods
            return when != WHEN_BEFORE;
                
        case NodeTypes.DVARNODE: {
            String name = ((INameNode)node).getName();
            blockScope.read(name);
            break;
        }
        case NodeTypes.LOCALVARNODE: {
            String name = ((INameNode)node).getName();
            methodScope.read(name);
            break;
        }
        case NodeTypes.MULTIPLEASGNNODE: {
            // I need to visit the right-hand-side children nodes of this assignment first to ensure that
            // in this:
            //    x,y=x+1,y+1
            // properly sees that "x" is read before it is written. 
            MultipleAsgnNode multiple = (MultipleAsgnNode)node;
            if (multiple.getValueNode() != null) {
                new ParseTreeWalker(this).walk(multiple.getValueNode());
            }
            break;
        }
        case NodeTypes.WHENNODE:
        case NodeTypes.IFNODE: {
            ifs++;
        }
        }

        return false;
    }

    public boolean unvisit(Node node) {
        switch (node.nodeId) {
        case NodeTypes.ITERNODE: {
            blockStack.remove(blockStack.size()-1);
            currentBlock = blockStack.size() > 0 ? blockStack.get(blockStack.size()-1) : null;
            if (currentBlock != null) {
                blockScope = blockScopes.get(currentBlock);
                assert blockScope != null;
            }
            break;
        }
        // I must process assignments AFTER I've processed the children since
        //  x = x + 1 
        // should be processed as a read before a write even though I encounter the
        // LocalAsgnNode before its child LocalVarNode
        case NodeTypes.LOCALASGNNODE: {
            String name = ((INameNode)node).getName();
            methodScope.write(name);
            break;
        }
        case NodeTypes.DASGNNODE: {
            String name = ((INameNode)node).getName();
            blockScope.write(name);
            break;
        }
        case NodeTypes.WHENNODE:
        case NodeTypes.IFNODE: {
            ifs--;
        }
        }

        if (node == endNode) {
            when = WHEN_AFTER;
        }

        return false;
    }

    private class UsageScope {
        UsageScope(Node block) {
            this.block = block;
        }
        
        private void read(String name) {
            if (when == WHEN_DURING) {
                if (!writtenBeforeReadDuring.contains(name)) {
                    readDuring.add(name);
                }
            } else if (when == WHEN_AFTER) {
                // I don't want a reassignment of the variable before it's been
                // read to count as a usage of the result from the fragment
                if (!writtenAfter.contains(name)) {
                    readAfter.add(name);
                }
            }
        }
        
        private void write(String name) {
            if (when == WHEN_BEFORE) {
                writtenBefore.add(name);
            } else if (when == WHEN_DURING) {
                    writtenDuring.add(name);
                if (ifs == 0 && !readDuring.contains(name)) {
                    writtenBeforeReadDuring.add(name);
                }
            } else if (when == WHEN_AFTER) {
                if (ifs == 0 && !readAfter.contains(name)) {
                    writtenAfter.add(name);
                }
            }
        }
        
        private void merge(UsageScope other) {
            writtenBefore.addAll(other.writtenBefore);
            readDuring.addAll(other.readDuring);
            writtenDuring.addAll(other.writtenDuring);
            writtenBeforeReadDuring.addAll(other.writtenBeforeReadDuring);
            writtenAfter.addAll(other.writtenAfter);
            readAfter.addAll(other.readAfter);
        }

        /** Block, or null if it's the local method */
        private Node block; 
        /** Variables that exist in scope before the code fragment */
        private final Set<String> writtenBefore = new HashSet<String>();
        /** Variables that are read during the code fragment */
        private final Set<String> readDuring = new HashSet<String>(); // rename readBeforeWrittenDuring
        /** Variables that are written to during the code fragment */
        private final Set<String> writtenDuring = new HashSet<String>();
        /** Variables that are written to during the code fragment */
        private final Set<String> writtenBeforeReadDuring = new HashSet<String>();
        /** Variables that are written PRIOR TO A READ OF THE SAME VAR after the code fragment */
        private final Set<String> writtenAfter = new HashSet<String>(); // rename writtenBeforeReadAfter
        /** Variables that are read (prior to a write) after the code fragment */
        private final Set<String> readAfter = new HashSet<String>(); // rename readBeforeWrittenAfter
    }
}

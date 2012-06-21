/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.model.impl;

import com.oracle.nashorn.ir.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Petr Pisl
 */
public class PathNodeVisitor extends NodeVisitor {
    private final List<Node> treePath = new ArrayList<Node>();
    
    public List<? extends Node> getPath() {
        return treePath;
    }
    
    public void addToPath(Node node) {
        treePath.add(node);
    }
    
    public void removeFromPathTheLast() {
        treePath.remove(treePath.size() - 1);
    }
    
    @Override
    public Node visit(AccessNode accessNode, boolean onset) {
        if (onset) {
            treePath.add(accessNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(accessNode, onset);
    }

    @Override
    public Node visit(BinaryNode binaryNode, boolean onset) {
        if (onset) {
            treePath.add(binaryNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(binaryNode, onset);
    }

    @Override
    public Node visit(Block block, boolean onset) {
        if (onset) {
            treePath.add(block);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(block, onset);
    }

    @Override
    public Node visit(BreakNode breakNode, boolean onset) {
        if (onset) {
            treePath.add(breakNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(breakNode, onset);
    }

    @Override
    public Node visit(CallNode callNode, boolean onset) {
        if (onset) {
            treePath.add(callNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(callNode, onset);
    }

    @Override
    public Node visit(CaseNode caseNode, boolean onset) {
        if (onset) {
            treePath.add(caseNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(caseNode, onset);
    }

    @Override
    public Node visit(CatchNode catchNode, boolean onset) {
        if (onset) {
            treePath.add(catchNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(catchNode, onset);
    }

    @Override
    public Node visit(ContinueNode continueNode, boolean onset) {
        if (onset) {
            treePath.add(continueNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(continueNode, onset);
    }

    @Override
    public Node visit(ExecuteNode executeNode, boolean onset) {
        if (onset) {
            treePath.add(executeNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(executeNode, onset);
    }

    @Override
    public Node visit(ForNode forNode, boolean onset) {
        if (onset) {
            treePath.add(forNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(forNode, onset);
    }

    @Override
    public Node visit(FunctionNode functionNode, boolean onset) {
        if (onset) {
            treePath.add(functionNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(functionNode, onset);
    }

    @Override
    public Node visit(IdentNode identNode, boolean onset) {
        if (onset) {
            treePath.add(identNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(identNode, onset);
    }

    @Override
    public Node visit(IfNode ifNode, boolean onset) {
        if (onset) {
            treePath.add(ifNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(ifNode, onset);
    }

    @Override
    public Node visit(IndexNode indexNode, boolean onset) {
        if (onset) {
            treePath.add(indexNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(indexNode, onset);
    }

    @Override
    public Node visit(LabelNode labeledNode, boolean onset) {
        if (onset) {
            treePath.add(labeledNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(labeledNode, onset);
    }

    @Override
    public Node visit(LineNumberNode lineNumberNode, boolean onset) {
        if (onset) {
            treePath.add(lineNumberNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(lineNumberNode, onset);
    }

    @Override
    public Node visit(LiteralNode literalNode, boolean onset) {
        if (onset) {
            treePath.add(literalNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(literalNode, onset);
    }

    @Override
    public Node visit(ObjectNode objectNode, boolean onset) {
        if (onset) {
            treePath.add(objectNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(objectNode, onset);
    }

    @Override
    public Node visit(PropertyNode propertyNode, boolean onset) {
        if (onset) {
            treePath.add(propertyNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(propertyNode, onset);
    }

    @Override
    public Node visit(ReferenceNode referenceNode, boolean onset) {
        if (onset) {
            treePath.add(referenceNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(referenceNode, onset);
    }

    @Override
    public Node visit(ReturnNode returnNode, boolean onset) {
        if (onset) {
            treePath.add(returnNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(returnNode, onset);
    }

    @Override
    public Node visit(RuntimeNode runtimeNode, boolean onset) {
        if (onset) {
            treePath.add(runtimeNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(runtimeNode, onset);
    }

    @Override
    public Node visit(SwitchNode switchNode, boolean onset) {
        if (onset) {
            treePath.add(switchNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(switchNode, onset);
    }

    @Override
    public Node visit(TernaryNode ternaryNode, boolean onset) {
        if (onset) {
            treePath.add(ternaryNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(ternaryNode, onset);
    }

    @Override
    public Node visit(ThrowNode throwNode, boolean onset) {
        if (onset) {
            treePath.add(throwNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(throwNode, onset);
    }

    @Override
    public Node visit(TryNode tryNode, boolean onset) {
        if (onset) {
            treePath.add(tryNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(tryNode, onset);
    }

    @Override
    public Node visit(UnaryNode unaryNode, boolean onset) {
        if (onset) {
            treePath.add(unaryNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(unaryNode, onset);
    }

    @Override
    public Node visit(VarNode varNode, boolean onset) {
        if (onset) {
            treePath.add(varNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(varNode, onset);
    }

    @Override
    public Node visit(WhileNode whileNode, boolean onset) {
        if (onset) {
            treePath.add(whileNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(whileNode, onset);
    }

    @Override
    public Node visit(WithNode withNode, boolean onset) {
        if (onset) {
            treePath.add(withNode);
        } else {
            treePath.remove(treePath.size() - 1);
        }
        return super.visit(withNode, onset);
    }
    
    
    
}

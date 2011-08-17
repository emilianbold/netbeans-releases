/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.css.lib.api.model;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Immutable content of a CSS rule.
 *
 * a rule example:
 * h1 {
 *      color: red;
 * }
 *
 * @author mfukala@netbeans.org
 */
public class Rule extends Item {

    public Rule(Snapshot snapshot, Node node) {
        super(snapshot, node);
        assert node.type() == NodeType.ruleSet;
    }
    
    public List<Declaration> items() {
        List<Declaration> items = new ArrayList<Declaration>();
        NodeVisitor<List<Declaration>> declarationsVisitor = new NodeVisitor<List<Declaration>>(items) {

            @Override
            public boolean visit(Node node) {
                if(node.type() == NodeType.declaration) {
                    getResult().add(new Declaration(snapshot, node));
                }
                return false;
            }
            
        };
        
        declarationsVisitor.visitChildren(node);
        return items;
    }
    
    public Node getSelectorsGroup() {
        return NodeUtil.getChildByType(node, NodeType.selectorsGroup);
    }
    
    /** @return the css rule name */
    @Override
    public String name() {
        return getSelectorsGroup().image().toString().trim();
    }

    /** @return offset of the rule name in the model's document. */
    public int getRuleNameOffset() {
        return getSelectorsGroup().from();
    }
    
    /** @return offset of the rule's closing bracket in the model's document. */
    public int getRuleCloseBracketOffset() {
        Node rbraceNode = NodeUtil.getChildTokenNode(node, CssTokenId.RBRACE);
        return rbraceNode != null ? rbraceNode.from() : -1;
    }

    /** @return offset of the rule's opening bracket in the model's document. */
    public int getRuleOpenBracketOffset() {
        Node lbraceNode = NodeUtil.getChildTokenNode(node, CssTokenId.LBRACE);
        return lbraceNode != null ? lbraceNode.from() : -1;
        
    }

    @Override
    public String toString() {
        return "Rule[" + name() + "; " + getRuleOpenBracketOffset() + " - " + getRuleCloseBracketOffset() + "]"; //NOI18N
    }
    
}

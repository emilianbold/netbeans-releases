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

import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.parsing.api.Snapshot;
 
/**
 * An immutable representation of a css rule item eg.:
 *
 * color: red;
 *
 * @author mfukala@netbeans.org
 */
public class Declaration extends Item {

    //XXX: hack for the terrible StyleBuilder logic. Must be fixed!!!
    public static Declaration createArtificial(final String propertyName, final String expression) {
        return new Declaration(null,null) {

            @Override
            public Item getProperty() {
                return new Item(null,null) {

                    @Override
                    public String name() {
                        return propertyName;
                    }

                    @Override
                    public int offset() {
                        return -1;
                    }
                    
                };
            }
            
            @Override
            public Item getValue() {
                return new Item(null,null) {

                    @Override
                    public String name() {
                        return expression;
                    }

                    @Override
                    public int offset() {
                        return -1;
                    }
                    
                };
            }
        };
    }
    
    Declaration(Snapshot snapshot, Node declarationNode) {
        super(snapshot, declarationNode);
    }
    
    public boolean isImporant() {
        return false;
    }

    public Item getProperty() {
        Node propertyNode = NodeUtil.getChildByType(node, NodeType.property);
        return propertyNode != null ? new Item(snapshot, propertyNode) : null;
    }

    public Item getValue() {
        Node propertyNode = NodeUtil.getChildByType(node, NodeType.expr);
        return propertyNode != null ? new Item(snapshot, propertyNode) : null;
    }

    @Override
    public String toString() {
        return "Declaration[" + getProperty() + "; " + getValue() + "]"; //NOI18N
    }

    /** Gets offset of the key - value separator in the css rule item.
     */
    public int colonOffset() {
        Node colonNode = NodeUtil.getChildTokenNode(node, CssTokenId.COLON);        
        return colonNode != null ? colonNode.from() : -1;
    }

    /** Gets offset of the ending semicolon in rule item or -1 if there is no ending semicolon.
     */
    public int semicolonOffset() {
        //the semicolon following the declaration is a member of parent 'declarations' node
        Node n = NodeUtil.getSibling(node, false);
        return n != null && n.type() == NodeType.token && NodeUtil.getTokenNodeTokenId(n) == CssTokenId.SEMI
                ? n.from()
                : -1;
    }

}

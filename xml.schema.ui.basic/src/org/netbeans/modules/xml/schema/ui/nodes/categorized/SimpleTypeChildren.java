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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.DefaultExpandedCookie;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.List;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SimpleTypeDefinition;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;

/**
 *
 * @author  Ajit Bhate
 */
public class SimpleTypeChildren<C extends SimpleType>
        extends CategorizedChildren<C>
{
    /**
     *
     *
     */
    public SimpleTypeChildren(SchemaUIContext context,
            SchemaComponentReference<C> reference) {
        super(context,reference);
    }
    
    
    /**
     *
     *
     */
    protected java.util.List<Node> createKeys() {
        java.util.List<Node> keys=super.createKeys();
        
        int index=0;
        
        // Insert the inherited node after the details node
        if (keys.size() > 0 && keys.get(0) instanceof DetailsNode)
            index=1;
        
        LocalSimpleType inlineType = null;
        SimpleTypeDefinition definition = getReference().get().getDefinition();
        if(definition instanceof SimpleTypeRestriction) {
            SimpleTypeRestriction str = (SimpleTypeRestriction)definition;
            inlineType = str.getInlineType();
        }
        if(definition instanceof List) {
            List list = (List)definition;
            inlineType = list.getInlineType();
        }
        if(definition instanceof SimpleTypeRestriction ||
                definition instanceof List) {
            for (int i=0;i<keys.size();i++) {
                Node n = keys.get(i);
                SchemaComponentNode scn = (SchemaComponentNode)n.getCookie
                        (SchemaComponentNode.class);
                if(scn!=null && scn.getReference().get()==definition) {
                    index = i;
                    keys.remove(index);
                    break;
                }
            }
        }
        if(inlineType != null) {
            Node inlineNode=
                    getContext().getFactory().createNode(inlineType);
            
            DefaultExpandedCookie expanded=(DefaultExpandedCookie)
            inlineNode.getCookie(DefaultExpandedCookie.class);
            if (expanded!=null)
                expanded.setDefaultExpanded(true);
            
            keys.add(index++,inlineNode);
        }
        if(definition instanceof SimpleTypeRestriction) {
            // add enum children
            java.util.List<Enumeration> enumChildren =
                    definition.getChildren(Enumeration.class);
            for(Enumeration e:enumChildren) {
                keys.add(index++,getContext().getFactory().createNode(e));
            }
        }
        return keys;
    }
}

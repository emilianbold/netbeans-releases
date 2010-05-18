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

package org.netbeans.modules.xml.schema.abe.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.CustomDatatype;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Chris Webster
 */
public class CategorizedChildren extends Children.Keys
        implements ComponentListener {
    private ABEUIContext context;
    private AXIDocument document;    
    private List<Class> childFilters;
    
    /** Creates a new instance of ABENodeChildren */
    public CategorizedChildren(ABEUIContext context, AXIDocument document,
            List<Class> childFilters) {
        this.context = context;
        this.document = document;
        this.childFilters = childFilters;
    }
    
    /**
     *
     *
     */
    private boolean isChildAllowed(Class componentClass) {
        // If no filters are specified, allow the child
        if (getChildFilters()==null)
            return true;
        
        for (Class clazz: getChildFilters()) {
            if (clazz.isAssignableFrom(componentClass))
                return true;
        }
        
        return false;
    }
    
    /**
     *
     *
     */
    protected java.util.List<Node> createKeys() {
        List<Node> keys=new ArrayList<Node>();
        
        // categorize only for schema node
        if(document instanceof AXIDocument) {
            if(isChildAllowed(Datatype.class))
                keys.add(new PrimitiveSimpleTypesNode(getContext()));
            if(isChildAllowed(ContentModel.class))
                keys.add(new GlobalContentModelsNode(getContext(), document));
            if(isChildAllowed(CustomDatatype.class))
                keys.add(new SimpleTypesNode(getContext(), document));
            if(isChildAllowed(Attribute.class))
                keys.add(new GlobalAttributesNode(getContext(), document));
            if(isChildAllowed(Element.class))
                keys.add(new GlobalElementsNode(getContext(), document));
            List<AXIModel> refModels = getContext().getModel().
                    getReferencedModels();            
            if(refModels != null && refModels.size() != 0) {
                keys.add(new ReferencedSchemasNode(getContext(),
                        document, childFilters));
            }
        } else {
            // add nodes in lexical order
            for (AXIComponent child: document.getChildren()) {
                Node node=getContext().getFactory().createNode(getNode(), child);
                keys.add(node);
            }
        }
        
        return keys;
    }
    
    /**
     *
     *
     */
    @Override
    protected Node[] createNodes(Object key) {
        Node[] result=null;
        
        if (key instanceof Node)
            result=new Node[] { (Node)key };
        
        return result;
    }
    
    private ABEUIContext getContext() {
        return context;
    }
    
    private void refreshChildren() {
        setKeys(createKeys());
    }
    
    protected void addNotify() {
        super.addNotify();
        refreshChildren();
//		ComponentListener cl = (ComponentListener)
//			WeakListeners.create(ComponentListener.class, this,
//			parentComponent.getModel());
//		parentComponent.getModel().addComponentListener(cl);
    }
    
    protected void removeNotify() {
        super.removeNotify();
        setKeys(Collections.emptyList());
    }
    
    public void valueChanged(ComponentEvent evt) {
    }
    
    public void childrenDeleted(ComponentEvent evt) {
        if (evt.getSource() == document) {
            refreshChildren();
        }
    }
    
    public void childrenAdded(ComponentEvent evt) {
        if (evt.getSource() == document) {
            refreshChildren();
        }
    }
    
    private List<Class> getChildFilters() {
        return childFilters;
    }
}

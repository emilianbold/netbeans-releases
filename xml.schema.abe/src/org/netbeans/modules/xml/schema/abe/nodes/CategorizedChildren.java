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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

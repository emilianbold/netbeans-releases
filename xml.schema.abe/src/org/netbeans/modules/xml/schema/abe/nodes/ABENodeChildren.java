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
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;

/**
 *
 * @author Chris Webster
 */
public class ABENodeChildren extends Children.Keys
        implements ComponentListener {
    private AXIComponent component;
    
    /** Creates a new instance of ABENodeChildren */
    public ABENodeChildren(AXIComponent component) {
        this.component = component;
    }
    
    protected Node[] createNodes(Object key) {
        if (key instanceof Compositor) {
            return new Node[] {new CompositorNode((Compositor) key)};
        }
        
        if(key instanceof AnyElement)
            return new Node[] {new AnyElementNode((AnyElement)key)};
        else if (key instanceof AbstractElement) {
            return new Node[] {new ElementNode((AbstractElement)key)};
        }
        
        if (key instanceof ContentModel) {
            return new Node[] {new ContentModelNode((ContentModel)key)};
        }
        
        return new Node[0];
    }
    
    private void refreshChildren() {
        setKeys(sortComponents(component.getChildren()));
    }
    
    protected void addNotify() {
        super.addNotify();
        refreshChildren();
        ComponentListener cl = (ComponentListener)
        WeakListeners.create(ComponentListener.class, this,
                component.getModel());
        component.getModel().addComponentListener(cl);
    }
    
    protected void removeNotify() {
        super.removeNotify();
        setKeys(Collections.emptyList());
    }
    
    public void valueChanged(ComponentEvent evt) {
    }
    
    public void childrenDeleted(ComponentEvent evt) {
        if (evt.getSource() == component) {
            refreshChildren();
        }
    }
    
    public void childrenAdded(ComponentEvent evt) {
        if (evt.getSource() == component) {
            refreshChildren();
        }
    }
    
    private List sortComponents(List<AXIComponent> list){
        //sort only for AXIDocument.
        if(! (this.component instanceof AXIDocument) )
            return list;
        //separate out elements and CMs
        List<AXIContainer> el = new ArrayList<AXIContainer>();
        List<AXIContainer> cml = new ArrayList<AXIContainer>();
        for(AXIComponent comp: list){
            if(comp instanceof AbstractElement){
                el.add((AXIContainer) comp);
            }else if(comp instanceof ContentModel){
                if( ((ContentModel)comp).getType() == ContentModel.ContentModelType.COMPLEX_TYPE)
                    cml.add((AXIContainer)comp);
            }
        }
        //sort GEs
        Collections.sort(el,
                new Comparator<AXIContainer>() {
            public int compare(AXIContainer e1, AXIContainer e2) {
                return e1.getName().compareTo(e2.getName());
            }
            
        });
        //sort GCTs
        Collections.sort(cml,
                new Comparator<AXIContainer>() {
            public int compare(AXIContainer e1, AXIContainer e2) {
                return e1.getName().compareTo(e2.getName());
            }
            
        });
        List<AXIContainer> result = new ArrayList<AXIContainer>();
        //club both arrays
        result.addAll(el);
        result.addAll(cml);
        return result;
    }
}

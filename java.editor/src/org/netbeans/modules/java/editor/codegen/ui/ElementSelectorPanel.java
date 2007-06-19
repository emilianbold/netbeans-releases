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

package org.netbeans.modules.java.editor.codegen.ui;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.swing.JPanel;
import org.netbeans.api.java.source.ElementHandle;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;

/**
 *
 * @author  Petr Hrebejk, Dusan Balek
 */
public class ElementSelectorPanel extends JPanel implements ExplorerManager.Provider {
    
    private ExplorerManager manager = new ExplorerManager();
    private CheckTreeView elementView;
    
    /** Creates new form ElementSelectorPanel */
    public ElementSelectorPanel(ElementNode.Description elementDescription, boolean singleSelection ) {        
        setLayout(new BorderLayout());
        elementView = new CheckTreeView();
        elementView.setRootVisible(false);
        add(elementView, BorderLayout.CENTER);
        setRootElement(elementDescription, singleSelection);
        //make sure that the first element is pre-selected
        Node root = manager.getRootContext();
        Node[] children = root.getChildren().getNodes();
        if( null != children && children.length > 0 ) {
            try         {
                manager.setSelectedNodes(new org.openide.nodes.Node[]{children[0]});
            } catch (PropertyVetoException ex) {
                //ignore
            }
        }
    }
    
    public List<ElementHandle<? extends Element>> getTreeSelectedElements() {
        ArrayList<ElementHandle<? extends Element>> handles = new ArrayList<ElementHandle<? extends Element>>();
                
        for (Node node : manager.getSelectedNodes()) {
            if (node instanceof ElementNode) {
                ElementNode.Description description = node.getLookup().lookup(ElementNode.Description.class);
                handles.add(description.getElementHandle());
            }
        }
     
        return handles;
    }    
        
    public List<ElementHandle<? extends Element>> getSelectedElements() {
        ArrayList<ElementHandle<? extends Element>> handles = new ArrayList<ElementHandle<? extends Element>>();
            
        Node n = manager.getRootContext();        
        ElementNode.Description description = n.getLookup().lookup(ElementNode.Description.class);
        getSelectedHandles( description, handles );
       
        return handles;
    }
    
    public void setRootElement(ElementNode.Description elementDescription, boolean singleSelection) {  
        
        Node n;
        if ( elementDescription != null ) {
            ElementNode en = new ElementNode(elementDescription);
            en.setSingleSelection(singleSelection);
            n = en;        
        }
        else {
            n = Node.EMPTY;
        }
        manager.setRootContext(n);
        
    }
    
    public void doInitialExpansion( int howMuch ) {
        
        Node root = getExplorerManager().getRootContext();
        Node[] subNodes = root.getChildren().getNodes(true);
        
        if ( subNodes == null ) {
            return;
        }
        Node toSelect = null;
        
        int row = 0;

        boolean oldScroll = elementView.getScrollsOnExpand();
        elementView.setScrollsOnExpand( false );
        
        for( int i = 0; subNodes != null && i < (howMuch == - 1 || howMuch > subNodes.length ? subNodes.length : howMuch ) ; i++ ) {                    
            // elementView.expandNode2(subNodes[i]);
            row ++;
            elementView.expandRow(row);
            Node[] ssn = subNodes[i].getChildren().getNodes( true );
            row += ssn.length;
            if ( toSelect == null ) {                
                if ( ssn.length > 0 ) {
                    toSelect = ssn[0];
                }                    
            }
        }
        
        elementView.setScrollsOnExpand( oldScroll );
        
        try  {
            if (toSelect != null ) {
                getExplorerManager().setSelectedNodes(new org.openide.nodes.Node[]{toSelect});
            }
        }
        catch (PropertyVetoException ex) {
            // Ignore
        }
    }
    
    // ExplorerManager.Provider imlementation ----------------------------------
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    private void getSelectedHandles( ElementNode.Description description,
                                     ArrayList<ElementHandle<? extends Element>> target) {
        
        List<ElementNode.Description> subs = description.getSubs();
        
        if ( subs == null ) {
            return;
        }
        
        for( ElementNode.Description d : subs ) {
            if ( d.isSelectable() && d.isSelected() ) {
                target.add(d.getElementHandle() );
            }
            else {
                getSelectedHandles( d, target );
            }
        }
    }
       
}

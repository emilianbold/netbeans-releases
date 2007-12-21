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
package org.netbeans.modules.bpel.design;

import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.selection.DiagramSelectionListener;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;


/**
 *
 * @author root
 */
public class SelectionBridge implements
        PropertyChangeListener,
        DiagramSelectionListener
       
{
    private DesignView designView;
    private boolean insideChangeNode = false;
    
    public SelectionBridge(DesignView designView) {
        this.designView = designView;
        
        TopComponent.getRegistry().addPropertyChangeListener(this);
        
        designView.getSelectionModel().addSelectionListener(this);
    }
    
    public void release(){
        TopComponent.getRegistry().removePropertyChangeListener(this);
        designView.getSelectionModel().removeSelectionListener(this);
        designView = null;
    }
    
    public void selectionChanged(BpelEntity oldSelection, final BpelEntity newSelection) {
        try {
            
            Node node = null;
            
            if (newSelection != null ) {
                node = designView.getNodeForPattern(designView.getModel().getPattern(newSelection));
            } else {
                // Workaround for bug xxx:
                // Set process as active node when nothing is been
                // selected on diagram
                // Otherwise the printing is broken
                DataObject dobj = (DataObject) designView.getLookup()
                        .lookup(DataObject.class);
                
                if (dobj != null) {
                    node = dobj.getNodeDelegate();
                } 
                
                if (node == null) {
                    node = designView.getNodeForPattern(designView.getModel()
                            .getRootPattern());
                }
                // Hack. Reset the active node before deleting element
                // related to active node.
                // see bug 6377934
            }
            
            insideChangeNode = true;
            
            if (node != null){
                setActivatedNodes(new Node[]{node});
            } else {
                setActivatedNodes(new Node[]{});
            }
            
            insideChangeNode = false;
        } catch( Exception ex){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        
        if (designView != null ) {
            if (! designView.isVisible()) {
                return;
            }
        }
        
        if (insideChangeNode) {
            return;
        }
        
        String propertyName = evt.getPropertyName();
        
        //ignore this event if model is in broken state
        if( designView.getBPELModel().getState() != BpelModel.State.VALID){
            return;
        }
        
        if (propertyName.equals(TopComponent.Registry.PROP_ACTIVATED_NODES)) {
            Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
            if (nodes.length != 1) {
                return;
            }
            
            TopComponent tc = TopComponent.getRegistry().getActivated();
            /* Ignore event coming from my TC */
            if (nodes[0] instanceof BpelNode){
                
                Object ref = ((BpelNode) nodes[0]).getReference();
                if (ref == null) {
                    return;
                }
                
                if ( ref instanceof BpelEntity ){
                    BpelEntity entity = (BpelEntity) ref;
                    
                    /**
                     * Iterate up to the root of entity hierarchy
                     * To find first entity wrapped with pattern
                     **/
                    designView.getModel().expandToBeVisible(entity);
                    
                    Pattern pattern = null;
                    
                    while (entity != null){
                        pattern = designView.getModel().getPattern(entity);
                        if (pattern != null){
                            break;
                        }
                        entity = entity.getParent();
                    }
                    
                    if (pattern != null){
                        designView.getSelectionModel().setSelected(entity);
                    }
                    
                    designView.scrollSelectedToView();
                }
            }
        }
    }
     
    private void setActivatedNodes(final Node[] nodes) {
        try {
            TopComponent targetTopComponent = null;
            Container container = (Container)designView;
            while (container != null) { // Find TopComponent
                if (container instanceof TopComponent) {
                    targetTopComponent = (TopComponent) container;
                    break;
                    
                }
                container = container.getParent();
            }
            if(targetTopComponent != null) {
                targetTopComponent.setActivatedNodes(nodes);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    
}

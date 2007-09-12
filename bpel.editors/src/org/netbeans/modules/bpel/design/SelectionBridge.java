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

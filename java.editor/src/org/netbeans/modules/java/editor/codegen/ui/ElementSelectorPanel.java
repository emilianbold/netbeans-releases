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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        elementView.setUseSubstringInQuickSearch(true);
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

        //#143049
        if (description == null)
            return;
        
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

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

package org.netbeans.modules.xml.schema.abe.nodes.properties;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.datatype.CustomDatatype;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.schema.abe.nodes.ABENodeFactory;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.schema.abe.nodes.ABEAbstractNode;
import org.netbeans.modules.xml.schema.abe.nodes.ContentModelNode;
import org.netbeans.modules.xml.schema.abe.nodes.CustomDatatypeNode;
import org.netbeans.modules.xml.schema.abe.nodes.DatatypeNode;
import org.netbeans.modules.xml.schema.abe.nodes.GlobalContentModelsNode;
import org.netbeans.modules.xml.schema.abe.nodes.PrimitiveSimpleTypesNode;
import org.netbeans.modules.xml.schema.abe.nodes.SimpleTypesNode;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author  Jeri Lockhart
 */
public class AXIComponentSelectionPanel extends JPanel
        implements ExplorerManager.Provider, PropertyChangeListener {
    
    static final long serialVersionUID = 1L;
    public static final String PROPERTY_SELECTION = "selectionChanged";
    private transient Object currentSelection;
    private transient String typeDisplayName;
    private Collection<? extends AXIComponent> exclude;
    private BeanTreeView typeView;
    private ExplorerManager explorerManager;
    
    private List<Class> filterTypes;
    
    /** Creates new form AXIComponentSelectionPanel */
    public AXIComponentSelectionPanel(AXIModel model, String typeDisplayName,
            List<Class> filterTypes, Object initialSelection,
            Collection<? extends AXIComponent> exclude) {
        this.currentSelection = initialSelection;
        this.typeDisplayName = typeDisplayName;
        this.filterTypes = filterTypes;
        if(exclude==null) {
            this.exclude = Collections.emptyList();
        } else {
            this.exclude = exclude;
        }
        initComponents();
        initialize(model);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        selectedLbl = new javax.swing.JLabel();
        nameTxt = new javax.swing.JTextField();
        cvPanel = new javax.swing.JPanel();

        selectedLbl.setText(org.openide.util.NbBundle.getMessage(AXIComponentSelectionPanel.class, "LBL_Currently_Selected", new Object[] {typeDisplayName}));

        nameTxt.setEditable(false);
        nameTxt.setText(getCurrentSelectionName());

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(selectedLbl)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(nameTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(selectedLbl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE))
                .addContainerGap())
        );

        cvPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, cvPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cvPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void initialize(final AXIModel model) {
//		ABENodeFactory factory = new ABENodeFactory(
//				model, childTypes, Lookup.EMPTY);
        ABENodeFactory factory = new ABENodeFactory(
                model, Lookup.EMPTY);
        final Node rootNode = factory.createRootNode(filterTypes);
        // View for selecting a global type.
        typeView = new BeanTreeView();
        typeView.setPopupAllowed(false);
        typeView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        typeView.setRootVisible(false);
        cvPanel.add(typeView, BorderLayout.CENTER);
        explorerManager = new ExplorerManager();
        explorerManager.setRootContext(rootNode);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                selectCurrentSelection(model);
            }
        });
    }
    
    private void selectCurrentSelection(final AXIModel model) {
        if(currentSelection!=null) {
            Node node = findNode(explorerManager.getRootContext(),
                    currentSelection, model);
            if(node!=null) {
                try {
                    getExplorerManager().setSelectedNodes(new Node[]{node});
                } catch (PropertyVetoException ex) {
                }
            }
        }
        getExplorerManager().addPropertyChangeListener(this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
            Node[] nodes = getExplorerManager().getSelectedNodes();
            if (nodes.length > 0) {
                if(nodes[0] instanceof ABEAbstractNode) {
                    setCurrentSelection(((ABEAbstractNode)nodes[0]).getAXIComponent());                    
                    return;
                }
                
                if(nodes[0] instanceof DatatypeNode) {
                    setCurrentSelection((Datatype)
                    ((DatatypeNode)nodes[0]).getType());
                    return;
                }
                
                if(nodes[0] instanceof PrimitiveSimpleTypesNode.TypeNode) {
                    setCurrentSelection((Datatype)
                    ((PrimitiveSimpleTypesNode.TypeNode)nodes[0]).getType());
                    return;
                }
            }
            setCurrentSelection(null);
        }
    }

    private void setCurrentSelection(Object sc){
        if(sc instanceof AXIType) {
            Object oldValue = currentSelection;
            currentSelection = sc==null||exclude.contains(sc)?null:sc;
            nameTxt.setText(getCurrentSelectionName());  //NOI18N
            firePropertyChange(PROPERTY_SELECTION,oldValue,currentSelection);
        }
    }
    
    public Object getCurrentSelection() {
        return currentSelection;
    }
    
    public String getCurrentSelectionName() {
        if(currentSelection instanceof AXIType)
            return ((AXIType)currentSelection).getName();
        return "";
    }
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
    private Node findNode(Node root, Object currentSelection, AXIModel model) {
        Node[] categories = root.getChildren().getNodes();
        for(Node category:categories) {
            if(category instanceof PrimitiveSimpleTypesNode &&
                    currentSelection instanceof Datatype) {
                String name = ((Datatype)currentSelection).getName();
                Node[] childs = category.getChildren().getNodes();
                for(Node child:childs) {
                    if(((PrimitiveSimpleTypesNode.TypeNode)child).getName().equals(name))
                        return child;
                }
            } else if(category instanceof SimpleTypesNode &&
                    currentSelection instanceof CustomDatatype) {
                String name = ((CustomDatatype)currentSelection).getName();
                Node[] childs = category.getChildren().getNodes();
                for(Node child:childs) {
                    if(((CustomDatatypeNode)child).getName().equals(name))
                        return child;
                }
            } else if(category instanceof GlobalContentModelsNode &&
                    currentSelection instanceof ContentModel) {
                String name = ((ContentModel)currentSelection).getName();
                Node[] childs = category.getChildren().getNodes();
                for(Node child:childs) {
                    if(((ContentModelNode)child).getName().equals(name))
                        return child;
                }
            }
        }
        return null;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cvPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField nameTxt;
    private javax.swing.JLabel selectedLbl;
    // End of variables declaration//GEN-END:variables
    
}

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

/*
 * AttributeGroupRefPanel.java
 *
 * Created on January 4, 2006, 1:42 PM
 */

package org.netbeans.modules.xml.schema.ui.basic.editors;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.tree.TreeSelectionModel;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.ui.basic.UIUtilities;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.PrimitiveSimpleType;

/**
 *
 * @author  Jeri Lockhart
 */
public class SchemaComponentSelectionPanel<T extends ReferenceableSchemaComponent> extends JPanel
        implements ExplorerManager.Provider, PropertyChangeListener {
    
    static final long serialVersionUID = 1L;
    public static final String PROPERTY_SELECTION = "selectionChanged";
    private Class<T> type;
    private transient SchemaModel model;
    private transient T currentSelection;
    private Collection<? extends SchemaComponent> exclude;
    private BeanTreeView typeView;
    private boolean includePrimitives;
    
    /**
     * Creates new form SchemaComponentSelectionPanel.
     *
     * @param  model             the schema model.
     * @param  type              the type of component to select.
     * @param  initialSelection  the initial selection, if any.
     * @param  exclude           which components to exclude.
     * @param  primitives        if true, include primitive simple types.
     */
    public SchemaComponentSelectionPanel(SchemaModel model,	Class<T> type,
            T initialSelection, Collection<? extends SchemaComponent> exclude,
            boolean primitives) {
        this.type = type;
        if(exclude==null) {
            this.exclude = Collections.emptyList();
        } else {
            this.exclude = exclude;
        }
        this.includePrimitives = primitives;
        initComponents();
        this.model=model;
        initialize(initialSelection);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        typeSelectionPanel = new EmbededPanel(this);
        cvPanel = new javax.swing.JPanel();
        selectedLbl = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionPane = new javax.swing.JTextPane();

        cvPanel.setLayout(new java.awt.BorderLayout());

        selectedLbl.setLabelFor(descriptionPane);
        org.openide.awt.Mnemonics.setLocalizedText(selectedLbl, org.openide.util.NbBundle.getMessage(SchemaComponentSelectionPanel.class, "LBL_Currently_Selected", new Object[] {"", ""}));
        selectedLbl.setToolTipText(org.openide.util.NbBundle.getMessage(SchemaComponentSelectionPanel.class, "HINT_Currently_Selected"));

        jScrollPane1.setBorder(null);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(20, 28));
        descriptionPane.setBorder(null);
        descriptionPane.setEditable(false);
        descriptionPane.setBackground(getBackground());
        jScrollPane1.setViewportView(descriptionPane);

        org.jdesktop.layout.GroupLayout typeSelectionPanelLayout = new org.jdesktop.layout.GroupLayout(typeSelectionPanel);
        typeSelectionPanel.setLayout(typeSelectionPanelLayout);
        typeSelectionPanelLayout.setHorizontalGroup(
            typeSelectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(typeSelectionPanelLayout.createSequentialGroup()
                .add(selectedLbl)
                .addContainerGap())
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
            .add(cvPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
        );
        typeSelectionPanelLayout.setVerticalGroup(
            typeSelectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(typeSelectionPanelLayout.createSequentialGroup()
                .add(cvPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                .add(6, 6, 6)
                .add(selectedLbl)
                .add(0, 0, 0)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(typeSelectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(typeSelectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void initialize(final T initialSelection) {
        ArrayList<Class<? extends SchemaComponent>> childTypes =
                new ArrayList<Class<? extends SchemaComponent>>();
        childTypes.add(SchemaModelReference.class);
        childTypes.add(type);
        if(includePrimitives&&!type.isAssignableFrom(PrimitiveSimpleType.class))
            childTypes.add(PrimitiveSimpleType.class);
        CategorizedSchemaNodeFactory factory = new CategorizedSchemaNodeFactory(
                model, childTypes, Lookup.EMPTY);
        final Node rootNode = factory.createRootNode();
        // View for selecting a global type.
        typeView = new BeanTreeView();
        typeView.setBorder(BasicBorders.getTextFieldBorder());
        typeView.setPopupAllowed(false);
        typeView.setDefaultActionAllowed(false);
        typeView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        typeView.setRootVisible(false);
        typeView.getAccessibleContext().setAccessibleName(
                org.openide.util.NbBundle.getMessage(
                SchemaComponentSelectionPanel.class, "LBL_GlobalReferenceTree"));
        typeView.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getMessage(
                SchemaComponentSelectionPanel.class, "LBL_GlobalReferenceTree"));
        cvPanel.add(typeView, BorderLayout.CENTER);
        getExplorerManager().setRootContext(rootNode);
        if(initialSelection!=null) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    setInitialSelection(initialSelection);
                }
            });
        }
    }
    
    public void setInitialSelection(T initialSelection) {
        currentSelection = initialSelection;
        if(currentSelection!=null) {
            Node node = UIUtilities.findNode(getExplorerManager().
                    getRootContext(),currentSelection, model);
            if(node!=null) {
                setDescription(node);
                try {
                    getExplorerManager().setSelectedNodes(new Node[]{node});
                } catch (PropertyVetoException ex) {
                }
            }
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
            Node[] nodes = getExplorerManager().getSelectedNodes();
            if (nodes.length > 0) {
                SchemaComponentNode<T> scn = findReferenceableNode(nodes[0]);
                setCurrentSelection(scn==null?null:scn.getReference().get());
                setDescription(scn);
                return;
            }
            setCurrentSelection(null);
        }
    }
    
    private void setCurrentSelection(T sc) {
        T oldValue = currentSelection;
        currentSelection = sc==null||exclude.contains(sc)?null:sc;
        firePropertyChange(PROPERTY_SELECTION,oldValue,currentSelection);
    }
    
    public T getCurrentSelection() {
        return currentSelection;
    }
    
    public ExplorerManager getExplorerManager() {
        return ((EmbededPanel)getTypeSelectionPanel()).getExplorerManager();
    }
    
    public void setEnabled(boolean flag) {
        super.setEnabled(flag);
        typeView.setEnabled(flag);
        selectedLbl.setEnabled(flag);
        descriptionPane.setEnabled(flag);
    }
    
    public JPanel getTypeSelectionPanel() {
        return typeSelectionPanel;
    }
    
    @SuppressWarnings("unchecked")
    private SchemaComponentNode<T> findReferenceableNode(Node node) {
        Node parent = node;
        SchemaComponentNode scn = null;
        while(parent!=null) {
            scn = (SchemaComponentNode)parent
                    .getCookie(SchemaComponentNode.class);
            if(scn!=null && type.isInstance(scn.getReference().get()))
                return (SchemaComponentNode<T>)scn;
            parent=parent.getParentNode();
        }
        return null;
    }
    
    private void setDescription(Node selected) {
        String arg0 = "";
        String arg1 = "";
        if(currentSelection!=null) {
            arg0 = currentSelection.getName();
            SchemaComponentNode scn = (SchemaComponentNode)selected.
                    getCookie(SchemaComponentNode.class);
            arg1 = scn==null?"":"("+scn.getTypeDisplayName()+")";
        }
        selectedLbl.setText(NbBundle.getMessage(
                SchemaComponentSelectionPanel.class,
                "LBL_Currently_Selected", new Object[] {arg0,arg1}));
        if(selected!=null&&selected.getShortDescription()!=null)
            descriptionPane.setText(selected.getShortDescription());
        else
            descriptionPane.setText("");
        descriptionPane.setCaretPosition(0);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JPanel cvPanel;
    private javax.swing.JTextPane descriptionPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel selectedLbl;
    public javax.swing.JPanel typeSelectionPanel;
    // End of variables declaration//GEN-END:variables
    
    private static class EmbededPanel extends JPanel
            implements ExplorerManager.Provider {
        private static final long serialVersionUID = 1L;
        private ExplorerManager explorerManager;
        private PropertyChangeListener listener;
        public EmbededPanel(PropertyChangeListener listener) {
            super();
            this.listener = listener;
            explorerManager = new ExplorerManager();
        }
        public ExplorerManager getExplorerManager() {
            return explorerManager;
        }

        public void removeNotify() {
            super.removeNotify();
            getExplorerManager().removePropertyChangeListener(listener);
        }
        
        public void addNotify() {
            super.addNotify();
            getExplorerManager().addPropertyChangeListener(listener);
        }
    }
}

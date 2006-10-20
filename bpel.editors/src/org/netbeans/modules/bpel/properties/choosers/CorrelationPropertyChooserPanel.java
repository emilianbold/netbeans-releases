/*
 * NewJPanel.java
 *
 * Created on 30 Èþëü 2006 ã., 1:18
 */

package org.netbeans.modules.bpel.properties.choosers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.CorrelationPropertyNode;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.netbeans.modules.bpel.properties.ExtendedLookup;
import org.netbeans.modules.bpel.properties.NodeUtils;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.bpel.properties.editors.controls.AbstractTreeChooserPanel;
import org.netbeans.modules.bpel.properties.editors.controls.NodesTreeParams;
import org.netbeans.modules.bpel.properties.editors.controls.Reusable;
import org.netbeans.modules.bpel.properties.editors.controls.filter.ChildTypeFilter;
import org.netbeans.modules.bpel.properties.editors.nodes.factory.PropertyChooserNodeFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author  ekaterina
 */
public class CorrelationPropertyChooserPanel extends AbstractTreeChooserPanel
        implements Reusable {
    
    static final long serialVersionUID = 1L;
    
    public CorrelationPropertyChooserPanel() {
        initComponents();
    }

    public CorrelationPropertyChooserPanel(Lookup lookup) {
        super(lookup);
    }

    public void createContent() {
        initComponents();
        //
        ((BeanTreeView)myTreeView).setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION );
        ((BeanTreeView)myTreeView).setRootVisible(true);
        ((BeanTreeView)myTreeView).setPopupAllowed(false);
        //
        //
        chbShowImportedOnly.setSelected(true);
        //
        chbShowImportedOnly.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BpelModel model = (BpelModel)getLookup().lookup(BpelModel.class);
                Process process = model.getProcess();
                BpelNode soughtNode = NodeUtils.findFirstNode(
                        process, getExplorerManager().getRootContext());
                //
                Children childrent = soughtNode.getChildren();
                if (childrent instanceof ReloadableChildren) {
                    ((ReloadableChildren)childrent).reload();
                }
            }
        });
        //
        super.createContent();
        //
        Util.activateInlineMnemonics(this);
    }
    
    protected Node constructRootNode() {
        Node result = null;
        //
        BpelModel model = (BpelModel)getLookup().lookup(BpelModel.class);
        Process process = model.getProcess();
        PropertyChooserNodeFactory factory =
                new PropertyChooserNodeFactory(
                PropertyNodeFactory.getInstance());
        result = (BpelNode)factory.createNode(
                NodeType.PROCESS, process, null, getLookup());
        //
        return result;
    }
    
    public void setLookup(Lookup lookup) {
        //
        List lookupObjects = new ArrayList();
        //
        // Create the default tree parameters if not any is specified
        NodesTreeParams treeParams =
                (NodesTreeParams)lookup.lookup(NodesTreeParams.class);
        if (treeParams == null) {
            // Set default Chooser Params
            treeParams = new NodesTreeParams();
            treeParams.setTargetNodeClasses(CorrelationPropertyNode.class);
            treeParams.setLeafNodeClasses(CorrelationPropertyNode.class);
            //
            lookupObjects.add(treeParams);
        }
        //
        // Create a filter to prevent showing not imported WSDL or Schema files
        ChildTypeFilter showImportedOnlyFilter = new ChildTypeFilter() {
            public boolean isPairAllowed(
                    NodeType parentType, NodeType childType) {
                if (chbShowImportedOnly.isSelected()) {
                    if (childType.equals(NodeType.WSDL_FILE) ||
                            childType.equals(NodeType.SCHEMA_FILE)) {
                        return false;
                    } else {
                        return true;
                    }
                }
                return true;
            }
        };
        lookupObjects.add(showImportedOnlyFilter);
        //
        if (lookupObjects.isEmpty()) {
            super.setLookup(lookup);
        } else {
            Object[] loArr = lookupObjects.toArray();
            Lookup correctedLookup = new ExtendedLookup(lookup, loArr);
            super.setLookup(correctedLookup);
        }
    }
    
    /**
     * Set selection to the node is corresponding to the specified variable.
     * Nothing is doing if the variable is null.
     */
    public void setSelectedCorrelationProperty(CorrelationProperty newValue) {
        if (newValue != null) {
            Node rootNode = getExplorerManager().getRootContext();
            Node node = NodeUtils.findFirstNode(
                    newValue, CorrelationPropertyNode.class, rootNode);
            if (node != null) {
                super.setSelectedValue(node);
            }
        }
    }
    
    public CorrelationProperty getSelectedCorrelationProperty() {
        Node node = super.getSelectedNode();
        assert node instanceof CorrelationPropertyNode;
        return ((CorrelationPropertyNode)node).getReference();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        pnlLookupProvider = new TreeWrapperPanel();
        myTreeView = new BeanTreeView();
        chbShowImportedOnly = new javax.swing.JCheckBox();

        myTreeView.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        org.jdesktop.layout.GroupLayout pnlLookupProviderLayout = new org.jdesktop.layout.GroupLayout(pnlLookupProvider);
        pnlLookupProvider.setLayout(pnlLookupProviderLayout);
        pnlLookupProviderLayout.setHorizontalGroup(
            pnlLookupProviderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, myTreeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
        );
        pnlLookupProviderLayout.setVerticalGroup(
            pnlLookupProviderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myTreeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
        );

        chbShowImportedOnly.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "CHB_Show_Imported_Files_Only"));
        chbShowImportedOnly.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chbShowImportedOnly.setMargin(new java.awt.Insets(0, 0, 0, 0));
        chbShowImportedOnly.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_CHB_Show_Imported_Files_Only"));
        chbShowImportedOnly.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_CHB_Show_Imported_Files_Only"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(chbShowImportedOnly)
                .addContainerGap(271, Short.MAX_VALUE))
            .add(pnlLookupProvider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(pnlLookupProvider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbShowImportedOnly))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chbShowImportedOnly;
    private javax.swing.JScrollPane myTreeView;
    private javax.swing.JPanel pnlLookupProvider;
    // End of variables declaration//GEN-END:variables
    
}

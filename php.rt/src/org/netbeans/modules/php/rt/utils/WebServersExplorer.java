/*
 * WebServersExplorer.java
 *
 * Created on 8 Ноябрь 2007 г., 12:46
 */

package org.netbeans.modules.php.rt.utils;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.php.rt.WebServerRegistry;
import org.netbeans.modules.php.rt.WebServersRootNode;
import org.netbeans.modules.php.rt.actions.AddHostAction;
import org.netbeans.modules.php.rt.actions.CustomizeHostAction;
import org.netbeans.modules.php.rt.actions.DeleteAction;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.CustomizeAction;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.windows.TopComponent;

/**
 *
 * @author  avk
 */
public class WebServersExplorer extends TopComponent 
        implements ExplorerManager.Provider
{
    private static final String LBL_MANAGE_SERVERS_TITLE = "LBL_ManageServers_Title"; // NOI18N

    private static Logger LOGGER = Logger.getLogger(WebServersExplorer.class.getName());
    /** Creates new form WebServersExplorer */
    public WebServersExplorer() {
        initComponents();
        init();
    }

    /*
    public static WebServersExplorer findInstance() {
        return SharedClassObject.findObject(WebServersExplorer.class, true);
    }
     */

    public boolean showDialog() {
        boolean confirm = false;
        
        String title = NbBundle.getMessage(WebServersExplorer.class, 
                LBL_MANAGE_SERVERS_TITLE);

        DialogDescriptor dialog = new DialogDescriptor(
                this, 
                title, 
                true, 
                DialogDescriptor.OK_CANCEL_OPTION, 
                NotifyDescriptor.OK_OPTION, 
                null);

        confirm = (DialogDisplayer.getDefault().notify(dialog) == NotifyDescriptor.OK_OPTION);
        // just check that array has cell to store result
        return confirm;
    }
    
    public ExplorerManager getExplorerManager() {
        return myExplManager;
    }

    public boolean isRootSelected(){
        Node node = getFirstSelectedNode();
        if (node != null){
            return node.equals(myExplManager.getRootContext());
        }
        return false;
    }
    
    public Host getSelection(){
        Node node = getFirstSelectedNode();
        if (node != null){
            return node.getLookup().lookup(Host.class);
        }
        return null;
    }
    
    public void setSelection(Host host){
        if (host == null){
            return;
        }
        try {
            Node root = myExplManager.getRootContext();
            String[] path = new String[]{host.getId()};
            Node node = NodeOp.findPath(root, path);
            if (node != null) {
                myExplManager.setSelectedNodes(new Node[]{node});
            }
        } catch (PropertyVetoException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (NodeNotFoundException ex) {
            // just do not select any node
            //LOGGER.log(Level.WARNING, null, ex);
        }
    }
    
    /** Overriden to pass focus directly to main content, which in 
     * turn assures that some element is always selected
     */ 
    @Override
    public boolean requestFocusInWindow () {
        boolean result = super.requestFocusInWindow();
        jScrollPane1.requestFocusInWindow();
        return result;
    }

    protected Node getFirstSelectedNode(){
        Node[] nodes = myExplManager.getSelectedNodes();
        if (nodes.length > 0 && nodes[0] != null){
            return nodes[0];
        }
        return null;
    }
    
    protected void refreshButtonsEnablement(){
        Node[] nodes = myExplManager.getSelectedNodes();
        myCustomizeBtn.setEnabled(isCustomizeAvailable(nodes));
        myDeleteBtn.setEnabled(isDeleteAvailable(nodes));
    }
    
    private boolean hasDeleteAction(Node node) {
        Action[] acts = node.getActions(false);
        for (Action act : acts) {
            if (act instanceof DeleteAction) {
                return true;
            }
        }
        return false;
    }

    private boolean isCustomizeAvailable(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return false;
        }
        Node node = nodes[0];
        if (node == null) {
            return false;
        }
        return node.hasCustomizer();
    }

    private boolean isDeleteAvailable(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return false;
        }
        for (Node node : nodes) {
            if (node == null || !hasDeleteAction(node)) {
                return false;
            }
        }
        return true;
    }

    private void init(){
        associateLookup(ExplorerUtils.createLookup(myExplManager, getActionMap()));
        
        Node root = new WebServersRootNode();
        FilterNode fRoot = new ExplorerNode(root);
        myExplManager.setRootContext(fRoot);

        TreeSelectionListener listener = new TreeSelectionListener();
        myExplManager.addPropertyChangeListener(listener);
       
        //mgr.setRootContext(new AbstractNode(new MyChildren()));
    
        refreshButtonsEnablement();
        startDefaultCreationWizard();
    }
    
    private void startDefaultCreationWizard(){
        Collection<Host> collection = WebServerRegistry.getInstance().getHosts();
        if (collection.size() == 0){
            AddHostAction.findInstance().showCustomizer();
        }
    }
    
    private class ExplorerNode extends FilterNode{

        public ExplorerNode(Node node) {
            this(node, new ExplorerNodeChildren(node));
        }

        public ExplorerNode(Node node, org.openide.nodes.Children children) {
            super(node, children, node.getLookup());
        }

        @Override
        public Action[] getActions(boolean context) {
            return super.getActions(context);
        }

        @Override
        public boolean hasCustomizer() {
            LOGGER.info("<<<<<<  "+getName()+" has customizer: "+super.hasCustomizer());
            return super.hasCustomizer();
        }

        @Override
        public Component getCustomizer() {
            Component c = super.getCustomizer();
            LOGGER.info("<<<<<<  "+getName()+" customizer= "+c);
            return c;
        }
        
    }
    
    private class ExplorerNodeChildren extends FilterNode.Children{

        private static final int MAX_NODES_DEPTH = 3;
        
        ExplorerNodeChildren(final Node originalNode) {
            super(originalNode);
        }

        @Override
        protected Node[] createNodes(Node originalNode) {
            return super.createNodes(originalNode);
        }

        @Override
        protected Node copyNode(Node originalNode) {
            String[] path = NodeOp.createPath(
                    originalNode, myExplManager.getRootContext());
            if (path.length >= MAX_NODES_DEPTH){
                return new ExplorerNode(originalNode, Children.LEAF);
            } else {
                return new ExplorerNode(originalNode);
            }
        }

        
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new WebServersTreeView();
        myAddNewBtn = new javax.swing.JButton();
        myCustomizeBtn = new javax.swing.JButton();
        myDeleteBtn = new javax.swing.JButton();

        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(myAddNewBtn, org.openide.util.NbBundle.getMessage(WebServersExplorer.class, "LBL_WebServersExplorer_Add_Btn")); // NOI18N
        myAddNewBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myAddNewBtnActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(myCustomizeBtn, org.openide.util.NbBundle.getMessage(WebServersExplorer.class, "LBL_WebServersExplorer_Customize_Btn")); // NOI18N
        myCustomizeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myCustomizeBtnActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(myDeleteBtn, org.openide.util.NbBundle.getMessage(WebServersExplorer.class, "LBL_WebServersExplorer_Delete_Btn")); // NOI18N
        myDeleteBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myDeleteBtnActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(myDeleteBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(myAddNewBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(myCustomizeBtn))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(myAddNewBtn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myDeleteBtn)
                        .add(24, 24, 24)
                        .add(myCustomizeBtn)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void myAddNewBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myAddNewBtnActionPerformed
        AddHostAction.findInstance().showCustomizer();
}//GEN-LAST:event_myAddNewBtnActionPerformed

    private void myCustomizeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myCustomizeBtnActionPerformed
        Node[] nodes = myExplManager.getSelectedNodes();
        CustomizeHostAction.findInstance().customize(nodes);
}//GEN-LAST:event_myCustomizeBtnActionPerformed

    private void myDeleteBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myDeleteBtnActionPerformed
        Node[] nodes = myExplManager.getSelectedNodes();
        DeleteAction.findInstance().delete(nodes);
    }//GEN-LAST:event_myDeleteBtnActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton myAddNewBtn;
    private javax.swing.JButton myCustomizeBtn;
    private javax.swing.JButton myDeleteBtn;
    // End of variables declaration//GEN-END:variables

    private class WebServersTreeView extends BeanTreeView{

        WebServersTreeView(){
            setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        }
        
        @Override
        protected boolean selectionAccept(Node[] nodes) {
            if (nodes != null && nodes.length > 1){
                return false;
            }
            return true;
        }

        @Override
        public boolean requestFocusInWindow() {
            return super.requestFocusInWindow();
        }
        

    }
    
    private class TreeSelectionListener implements PropertyChangeListener{
        
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getSource() != myExplManager) {
                    return;
                }
                String evtName = evt.getPropertyName();
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evtName)) {
                    selectionChanged();
                    return;
                } else if (ExplorerManager.PROP_NODE_CHANGE.equals(evtName)){
                    
                }
                
            }
            
            private void selectionChanged(){
                refreshButtonsEnablement();
            }
            
    }    
    
    private ExplorerManager myExplManager = new ExplorerManager();

}

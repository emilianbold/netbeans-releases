/*
 * BrowserPanel.java
 *
 * Created on January 24, 2006, 7:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.subversion.ui.browser;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.UserCancelException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class BrowserPanel extends JPanel implements ExplorerManager.Provider, VetoableChangeListener {

    private final ExplorerManager manager;
    private BrowserBeanTreeView treeView;
    private JLabel label;

    private static final SVNUrl[] EMPTY_URL = new SVNUrl[0];
    
    /** Creates new form BrowserPanel */
    public BrowserPanel() {
        manager = new ExplorerManager();
        manager.addVetoableChangeListener(this);
        setLayout(new BorderLayout(6, 6));
        
        treeView = new BrowserBeanTreeView();
        treeView.setDragSource(true);
        treeView.setDropTarget(true);        
      
        treeView.setPopupAllowed (false);
        treeView.setDefaultActionAllowed (false);
        treeView.setBorder(BorderFactory.createEtchedBorder());
        add(java.awt.BorderLayout.CENTER, treeView);
        
        label = new JLabel();        
        label.setLabelFor(treeView.getTree());        
        add(label, BorderLayout.NORTH);
        
        setBorder(BorderFactory.createEmptyBorder(12,12,0,12));
    }
   
    public void setup(String title, Node root, String browserAcsn, String browserAcsd) {
        manager.setRootContext(root);
      
        treeView.getAccessibleContext().setAccessibleDescription(browserAcsd);
        treeView.getAccessibleContext().setAccessibleName(browserAcsn);            
        label.setToolTipText(browserAcsd);
        Mnemonics.setLocalizedText(label, title);
    }
    
    public Node[] getSelectedNodes() {
        return manager.getSelectedNodes();
    }

    public SVNUrl[] getSelectedURLs() {
        Node[] nodes = (Node[]) manager.getSelectedNodes();
        
        if(nodes.length == 0) {
            return EMPTY_URL;
        }
        
        SVNUrl[] ret = new SVNUrl[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            ret[i] = ((RepositoryPathNode) nodes[i]).getSVNUrl();
        }
        return ret;
    }
    
    // XXX ist there some another way to get the tree?
    private class BrowserBeanTreeView extends BeanTreeView {
        public JTree getTree() {
            return tree;
        } 
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }    

    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            
            Node[] oldSelection =  (Node[]) evt.getOldValue();
            if(oldSelection.length == 0) {
                return;
            }
            
            Node[] newSelection = (Node[]) evt.getNewValue();
            boolean selectionChange = true;
            for (int i = 0; i < oldSelection.length; i++) {
                if(isInArray(oldSelection[i], newSelection)) {
                    selectionChange = false;
                    break;
                }
            }
            if(selectionChange) {
                return;
            }
            
            // we anticipate that nothing went wrong and 
            // all nodes in the old selection are at the same level
            Node selectedNode = oldSelection[0]; 
            
            for (int i = 0; i < newSelection.length; i++) {
                 if (getNodeLevel(selectedNode) != getNodeLevel(newSelection[i])) {
                    throw new PropertyVetoException("", evt); // NOI18N
                }
            }
        }
    }
    
    private int getNodeLevel(Node node) {
        int level = 0;
        while(node!=null) {
            node = node.getParentNode();
            level++;
        }
        return level;
    }
    
    private boolean isInArray(Node node, Node[] nodeArray) {
        for (int i = 0; i < nodeArray.length; i++) {
            if(node==nodeArray[i]) {
                return true;
            }
        }
        return false;
    }
}

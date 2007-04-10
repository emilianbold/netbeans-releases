/*
 * WSDesignViewNavigatorContent.java
 *
 * Created on April 9, 2007, 5:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.navigator;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author rico
 */
public class WSDesignViewNavigatorContent extends JPanel
        implements ExplorerManager.Provider, PropertyChangeListener{
    
    /** Explorer manager for the tree view. */
    private ExplorerManager explorerManager;
    /** Our schema component node tree view. */
    private TreeView treeView;
   
    
    /** Creates a new instance of WSDesignViewNavigatorContent */
    public WSDesignViewNavigatorContent() {
        setLayout(new BorderLayout());
        explorerManager = new ExplorerManager();
        treeView = new BeanTreeView();
        explorerManager.addPropertyChangeListener(this);
    }
    
    public void propertyChange(PropertyChangeEvent arg0) {
        
    }
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
    public void navigate(){
        add(treeView, BorderLayout.CENTER);
        AbstractNode root = new AbstractNode(new WSChildren());
        root.setName("Operations");
        getExplorerManager().setRootContext(root);
        revalidate();
        repaint();
    }
    
   public class WSChildren extends Children.Keys{
        protected Node[] createNodes(Object key) {
            if(key instanceof MethodModel){
                MethodModel m = (MethodModel)key;
                AbstractNode n = new AbstractNode(Children.LEAF);
                n.setName(m.getOperationName());
                return new Node[] {n};
            }
            return new Node[0];
        }
        
        protected void addNotify() {
            updateKeys();
        }
        
        private void updateKeys(){
            List keys = new ArrayList();
            this.setKeys(keys);
        }
    }
    
}

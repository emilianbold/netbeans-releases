/*
 * ResultsPanel.java
 *
 * Created on July 14, 2006, 4:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.query.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.xml.refactoring.ui.util.AnalysisUtilities;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.netbeans.modules.xml.schema.refactoring.SchemaUIHelper.FilteredSchemaNode;

/**
 *
 * @author Jeri Lockhart
 */
public class ResultsPanel extends JPanel implements ExplorerManager.Provider,
             PropertyChangeListener{
        
        private ExplorerManager explorerManager;
        private static final long serialVersionUID = 1L;
        private TView treeView;
        
        public ResultsPanel(Node root) {
            super();
            initialize(root);
        }
        
        
        private void initialize(Node root) {
//            nodePreferredAction = new WhereUsedAction(customizer.getColView());
            explorerManager = new ExplorerManager();
            removeAll();
            setPreferredSize(new Dimension(200,200));
            setLayout(new BorderLayout());
            treeView=new TView();
            treeView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            treeView.setRootVisible(true);
            treeView.setDefaultActionAllowed(true);
            Object key = "org.openide.actions.PopupAction";
            KeyStroke ks = KeyStroke.getKeyStroke("shift F10");
            treeView.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, key);
            
            add(treeView,BorderLayout.CENTER);
            explorerManager.setRootContext(root);
            explorerManager.addPropertyChangeListener(this);
            
            addComponentListener(new ComponentAdapter(){
                public void componentResized(ComponentEvent e) {
                    // Don't expand nodes if scrolling will be needed
                    // Nodes for Primitive types are not expanded
                    removeComponentListener(this);	// only check once
    //				System.out.println("WhereUsedExplorer component listener removed.");
                    
                    int rh = treeView.getRowHeight();
                    Node root = explorerManager.getRootContext();
                    int count = getRowCount(root);
//                    int totalH = rh * count;
//                    if (d.height > totalH){
                        // expand nodes
                        Node[] catNodes = root.getChildren().getNodes();
                        for (int i = 0; i < catNodes.length;i++) {
                                treeView.expandNode(catNodes[i]);
                        }
//                    }
                }
            });
            
        }
        
        private int getRowCount(Node root){
            int count = 0;
            if (root != null){
                count++;
            }
            else return count;
            Children children = root.getChildren();
            if (children != null){
                Node[] catNodes = children.getNodes();
                for (Node n:catNodes){
                    count++;
                    Children unusedCh = n.getChildren();
                    if (unusedCh != null){
                        count += unusedCh.getNodesCount();
                    }
                }
            }
            return count;
        }
        
        
        
        ///////////////////////////////////////////////////////////////////////
        //  Implement ExplorerManager.Provider
        ///////////////////////////////////////////////////////////////////////
        public ExplorerManager getExplorerManager() {
            return explorerManager;
        }
        
        
        
        ////////////////////////////////////////////////////////////////////
        // Implement PropertyChangeListener
        ////////////////////////////////////////////////////////////////////
        public void propertyChange(PropertyChangeEvent evt){
            if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())){
                Node[] selNodes = (Node[]) evt.getNewValue();
                TopComponent parent = (TopComponent)SwingUtilities.
                        getAncestorOfClass(TopComponent.class,treeView);
                if (parent != null){
                    parent.setActivatedNodes(selNodes);
                }
            }
        }// end propertyChange()
        
        
    public  class TView extends BeanTreeView{
        public static final long serialVersionUID = 1L;
        
        public TView(){
            super();            
        }
        
        public int getRowHeight() {
            return tree.getRowHeight();
        }
    }
    }



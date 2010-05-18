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
import org.netbeans.modules.xml.refactoring.spi.AnalysisUtilities;
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



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
package org.netbeans.modules.csl.navigation;

import java.awt.BorderLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.CompilationInfo;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.api.StructureScanner.Configuration;
import org.netbeans.modules.csl.navigation.ClassMemberFilters;
import org.netbeans.modules.csl.navigation.ElementNode;
import org.netbeans.modules.csl.navigation.actions.FilterSubmenuAction;
import org.netbeans.modules.csl.navigation.actions.SortActionSupport.SortByNameAction;
import org.netbeans.modules.csl.navigation.actions.SortActionSupport.SortBySourceAction;
import org.netbeans.modules.csl.navigation.base.FiltersManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.netbeans.modules.csl.navigation.base.TapPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 *
 * @author  phrebejk
 */
public class ClassMemberPanelUI extends javax.swing.JPanel
        implements ExplorerManager.Provider, FiltersManager.FilterChangeListener {
    
    private ExplorerManager manager = new ExplorerManager();
    private MyBeanTreeView elementView;
    private TapPanel filtersPanel;
    private JLabel filtersLbl;
    private Lookup lookup = null; // XXX may need better lookup
    private ClassMemberFilters filters;
    
    private Action[] actions; // General actions for the panel
    
    /** Creates new form ClassMemberPanelUi */
    public ClassMemberPanelUI(Language language) {
                      
        initComponents();
        
        // Tree view of the elements
        elementView = createBeanTreeView();        
        add(elementView, BorderLayout.CENTER);
               
        // filters
        filtersPanel = new TapPanel();
        filtersLbl = new JLabel(NbBundle.getMessage(ClassMemberPanelUI.class, "LBL_Filter")); //NOI18N
        filtersLbl.setBorder(new EmptyBorder(0, 5, 5, 0));
        filtersPanel.add(filtersLbl);
        filtersPanel.setOrientation(TapPanel.DOWN);
        // tooltip
        KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        String keyText = Utilities.keyToString(toggleKey);
        filtersPanel.setToolTipText(NbBundle.getMessage(ClassMemberPanelUI.class, "TIP_TapPanel", keyText));
        
        filters = new ClassMemberFilters( this );
        filters.getInstance().hookChangeListener(this);
        filtersPanel.add(filters.getComponent());
        
        actions = new Action[] {            
            new SortByNameAction( filters ),
            new SortBySourceAction( filters ),
            null,
            new FilterSubmenuAction(filters.getInstance())            
        };
        
        // See http://www.netbeans.org/issues/show_bug.cgi?id=128985
        // We don't want filters for all languages. Hardcoded for now.
        boolean includeFilters = true;
        if (language != null && language.getStructure() != null) {
            StructureScanner scanner = language.getStructure();
            Configuration configuration = scanner.getConfiguration();
            if (configuration != null) {
                includeFilters = configuration.isFilterable();
                if (!includeFilters) {
                    //issue #132883 workaround
                    filters.disableFiltering = true;

                    // Perhaps we don't have to create the filterspanel etc. above
                    // in this case, but it's right before 6.1 high resistance and
                    // I'm afraid code relies on the above stuff being non-null
                }
            }
        }
        if (includeFilters) {
            add(filtersPanel, BorderLayout.SOUTH);
        }

        manager.setRootContext(ElementNode.getWaitNode());
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean result = super.requestFocusInWindow();
        elementView.requestFocusInWindow();
        return result;
    }
    
    public org.openide.util.Lookup getLookup() {
        // XXX Check for chenge of FileObject
        return lookup;
    }
    
    public org.netbeans.modules.csl.navigation.ElementScanningTask getTask() {
        
        return new ElementScanningTask(this);
        
    }
    
    
    public void showWaitNode() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               elementView.setRootVisible(true);
               manager.setRootContext(ElementNode.getWaitNode());
            } 
        });
    }

    public void selectElementNode(final CompilationInfo info, final int offset) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ElementNode root = getRootNode();
                if ( root == null ) {
                    return;
                }
                final ElementNode node = root.getMimeRootNodeForOffset(info, offset);
                Node[] selectedNodes = manager.getSelectedNodes();
                if (!(selectedNodes != null && selectedNodes.length == 1 && selectedNodes[0] == node)) {
                    try {
                        manager.setSelectedNodes(new Node[]{ node == null ? getRootNode() : node });
                    } catch (PropertyVetoException propertyVetoException) {
                        Exceptions.printStackTrace(propertyVetoException);
                    }
                }
            }
        });
    }

    public void refresh( final StructureItem description, final FileObject fileObject) {
        final ElementNode rootNode = getRootNode();
        
        if ( rootNode != null && rootNode.getFileObject().equals( fileObject) ) {
            // update
            //System.out.println("UPDATE ======" + description.fileObject.getName() );
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    long startTime = System.currentTimeMillis();
                    rootNode.updateRecursively( description );
                    long endTime = System.currentTimeMillis();
                    Logger.getLogger("TIMER").log(Level.FINE, "Navigator Merge",
                            new Object[] {fileObject, endTime - startTime});
                    
                }
            } );            
        } 
        else {
            //System.out.println("REFRES =====" + description.fileObject.getName() );
            // New fileobject => refresh completely
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    long startTime = System.currentTimeMillis();
                    elementView.setRootVisible(false);
                    manager.setRootContext(new ElementNode( description, ClassMemberPanelUI.this, fileObject ) );

                    boolean expand = true;
                    Language language = LanguageRegistry.getInstance().getLanguageByMimeType(fileObject.getMIMEType());
                    if (language != null && language.getStructure() != null) {
                        StructureScanner scanner = language.getStructure();
                        Configuration configuration = scanner.getConfiguration();
                        if (configuration != null) {
                            expand = configuration.getExpandDepth() != 0;
                        }
                    }
                    if (expand) {
                        boolean scrollOnExpand = elementView.getScrollOnExpand();
                        elementView.setScrollOnExpand( false );
                        elementView.expandAll();
                        elementView.setScrollOnExpand( scrollOnExpand );
                    } else {
                        // Expand just the top levels, especially if we're dealing with a mimeroot
                        Node[] nodes = manager.getRootContext().getChildren().getNodes();
                        for (Node node : nodes) {
                            elementView.expandNode(node);
                        }
                    }
                    long endTime = System.currentTimeMillis();
                    Logger.getLogger("TIMER").log(Level.FINE, "Navigator Initialization",
                            new Object[] {fileObject, endTime - startTime});
                }
            } );
            
        }
    }
    
    public void sort() {
        getRootNode().refreshRecursively();
    }
    
    public ClassMemberFilters getFilters() {
        return filters;
    }
    
    public void expandNode( Node n ) {
        elementView.expandNode(n);
    }
    
    public Action[] getActions() {
        return actions;
    }
    
    public FileObject getFileObject() {
        return getRootNode().getFileObject();
    }
    
    // FilterChangeListener ----------------------------------------------------
    
    public void filterStateChanged(ChangeEvent e) {
        ElementNode root = getRootNode();
        
        if ( root != null ) {
            root.refreshRecursively();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    // Private methods ---------------------------------------------------------
    
    private ElementNode getRootNode() {
        
        Node n = manager.getRootContext();
         if ( n instanceof ElementNode ) {
            return (ElementNode)n;
        }
        else {
            return null;
        }
    }
    
    private MyBeanTreeView createBeanTreeView() {
//        ActionMap map = getActionMap();
//        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
//        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
//        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
//        map.put("delete", new DelegatingAction(ActionProvider.COMMAND_DELETE, ExplorerUtils.actionDelete(manager, true)));
//        
        
        MyBeanTreeView btv = new MyBeanTreeView();    // Add the BeanTreeView        
//      btv.setDragSource (true);        
//      btv.setRootVisible(false);        
//      associateLookup( ExplorerUtils.createLookup(manager, map) );        
        return btv;
        
    }
    
    
    // ExplorerManager.Provider imlementation ----------------------------------
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    
    private static class MyBeanTreeView extends BeanTreeView {
        public boolean getScrollOnExpand() {
            return tree.getScrollsOnExpand();
}
        
        public void setScrollOnExpand( boolean scroll ) {
            this.tree.setScrollsOnExpand( scroll );
        }
    }
}

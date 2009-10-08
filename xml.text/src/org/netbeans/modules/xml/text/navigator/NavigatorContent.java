/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xml.text.navigator;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.text.Document;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.xml.text.navigator.base.AbstractXMLNavigatorContent;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.DataEditorSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;
import org.openide.windows.TopComponent;


/** XML Navigator UI component containing a tree of XML elements.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public class NavigatorContent extends AbstractXMLNavigatorContent   {
    
    private static final boolean DEBUG = false;
    
    //suppose we always have only one instance of the navigator panel at one time
    //so using the static fields is OK. TheeNodeAdapter is reading these two
    //fields and change it's look accordingly
    static boolean showAttributes = true;
    static boolean showContent = true;
    
    private DataObject peerDO = null;
    private WeakHashMap uiCache = new WeakHashMap();
    private boolean editorOpened = false;
    
    public NavigatorContent() {
        setLayout(new BorderLayout());
    }
    
    public void navigate(DataObject d) {
        if(peerDO != null && peerDO != d) {
            //release the original document (see closeDocument() javadoc)
            closeDocument(peerDO);
        }
        
        EditorCookie ec = (EditorCookie)d.getCookie(EditorCookie.class);
        if(ec == null) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "The DataObject " + d.getName() + "(class=" + d.getClass().getName() + ") has no EditorCookie!?");
        } else {
            try {
                if(DEBUG) System.out.println("[xml navigator] navigating to DATAOBJECT " + d.hashCode());
                //test if the document is opened in editor
                BaseDocument bdoc = (BaseDocument)ec.openDocument();
                //create & show UI
                if(bdoc != null) {
                    //there is something we can navigate in
                    navigate(d, bdoc);
                    //remember the peer dataobject to be able the call EditorCookie.close() when closing navigator
                    this.peerDO = d;
                    //check if the editor for the DO has an opened pane
                    editorOpened = ec.getOpenedPanes() != null && ec.getOpenedPanes().length > 0;
                }
                
            }catch(UserQuestionException uqe) {
                //do not open a question dialog when the document is just loaded into the navigator
                showError(AbstractXMLNavigatorContent.ERROR_TOO_LARGE_DOCUMENT);
            }catch(IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    public void navigate(final DataObject documentDO, final BaseDocument bdoc) {
        if(DEBUG) System.out.println("[xml navigator] navigating to DOCUMENT " + bdoc.hashCode());
        //called from AWT thread
        showWaitPanel();
        
        //try to find the UI in the UIcache
        final JPanel cachedPanel;
        WeakReference panelWR = (WeakReference)uiCache.get(documentDO);
        if(panelWR != null) {
            NavigatorContentPanel cp = (NavigatorContentPanel)panelWR.get();
            if(cp != null) {
                if(DEBUG) System.out.println("panel is cached");
                //test if the document associated with the panel is the same we got now
                cachedPanel = bdoc == cp.getDocument() ? cp : null;
                if(cachedPanel == null) {
                    if(DEBUG) System.out.println("but the document is different - creating a new UI...");
                    if(DEBUG) System.out.println("the cached document : " + cp.getDocument());
                    
                    //remove the old mapping from the cache
                    uiCache.remove(documentDO);
                }
            } else
                cachedPanel = null;
        } else
            cachedPanel = null;
        
        //get the model and create the new UI on background
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                //get document model for the file
                try {
                    final DocumentModel model;
                    if(cachedPanel == null && bdoc.getLength() != 0)
                        model = DocumentModel.getDocumentModel(bdoc);
                    else
                        model = null; //if the panel is cached it holds a refs to the model - not need to init it again
                    
                    
                    if(cachedPanel != null || model != null) {
                        
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    showWaitPanel();
                                    JPanel panel = null;
                                    if(cachedPanel == null) {
                                        try {
                                            //lock the model for modifications during the
                                            //navigator tree creation
                                            model.readLock();
                                            
                                            //cache the newly created panel
                                            panel = new NavigatorContentPanel(model);
                                            //use the document dataobject as a key since the document itself is very easily discarded and hence
                                            //harly usable as a key of the WeakHashMap
                                            uiCache.put(documentDO, new WeakReference(panel));
                                            if(DEBUG) System.out.println("[xml navigator] panel created");
                                            
                                            //start to listen to the document property changes - we need to get know when the document is being closed
                                            EditorCookie.Observable eco = (EditorCookie.Observable)documentDO.getCookie(EditorCookie.Observable.class);
                                            if(eco != null) {
                                                eco.addPropertyChangeListener(NavigatorContent.this);
                                            } else {
                                                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "The DataObject " + documentDO.getName() + "(class=" + documentDO.getClass().getName() + ") has no EditorCookie.Observable!");
                                            }
                                        }finally{
                                            //unlock the model
                                            model.readUnlock();
                                        }
                                    } else {
                                        panel = cachedPanel;
                                        if(DEBUG) System.out.println("[xml navigator] panel gotten from cache");
                                    }
                                    
                                    //paint the navigator UI
                                    removeAll();
                                    add(panel, BorderLayout.CENTER);
                                    revalidate();
                                    //panel.revalidate();
                                    repaint();
                                }
                            });
                    } else {
                        //model is null => show message
                        showError(AbstractXMLNavigatorContent.ERROR_CANNOT_NAVIGATE);
                    }
                }catch(DocumentModelException dme) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dme);
                }
            }
        });
    }
    
    public void release() {
        removeAll();
        repaint();
        
        closeDocument(peerDO);
    }
    
    /** A hacky fix for XMLSyncSupport - I need to call EditorCookie.close when the navigator
     * is deactivated and there is not view pane for the navigated document. Then a the synchronization
     * support releases a strong reference to NbEditorDocument. */
    private void closeDocument(DataObject dobj) {
        if(dobj != null) {
            EditorCookie ec = (EditorCookie)peerDO.getCookie(EditorCookie.class);
            if(ec != null) {
                JEditorPane panes[] = ec.getOpenedPanes();
                //call EC.close() if there isn't any pane and the editor was opened
                if((panes == null || panes.length == 0)) {
                    ((EditorCookie.Observable)ec).removePropertyChangeListener(this);
                    
                    if(editorOpened) {
                        ec.close();
                        if(DEBUG) System.out.println("document instance for dataobject " + dobj.hashCode() + " closed.");
                    }
                }
                editorOpened = false;
            }
        }
    }
        
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName() == EditorCookie.Observable.PROP_DOCUMENT) {
            if(evt.getNewValue() == null) {
                final DataObject dobj = ((DataEditorSupport)evt.getSource()).getDataObject();
                if(dobj != null) {
                    editorOpened = false;
                    //document is being closed
                    if(DEBUG) System.out.println("document has been closed for DO: " + dobj.hashCode());
                    
                    //remove the property change listener from the DataObject's EditorSupport
                    EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
                    if(ec != null)
                        ((EditorCookie.Observable)ec).removePropertyChangeListener(this);
                    
                    //and navigate the document again (must be called asynchronously
                    //otherwise the ClonableEditorSupport locks itself (new call to CES from CES.propertyChange))
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if(dobj.isValid()) navigate(dobj);
                        }
                    });
                }
            } else {
                //a new pane created
                editorOpened = true;
            }
        }
    }
    
    private class NavigatorContentPanel extends JPanel implements FiltersManager.FilterChangeListener {
        
        private JTree tree;
        private FiltersManager filters;
        private Document doc;
        
        public NavigatorContentPanel(DocumentModel dm) {
            this.doc = dm.getDocument();
            
            setLayout(new BorderLayout());
            //create the JTree pane
            tree = new PatchedJTree();
            TreeModel model = createTreeModel(dm);
            tree.setModel(model);
            //tree.setLargeModel(true);
            tree.setShowsRootHandles(true);
            tree.setRootVisible(false);
            tree.setCellRenderer(new NavigatorTreeCellRenderer());
            tree.putClientProperty("JTree.lineStyle", "Angled");
            ToolTipManager.sharedInstance().registerComponent(tree);
            
            MouseListener ml = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int selRow = tree.getRowForLocation(e.getX(), e.getY());
                    if(selRow != -1) {
                        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                        TreeNodeAdapter tna = (TreeNodeAdapter)selPath.getLastPathComponent();
                        if(e.getClickCount() == 2)
                            openAndFocusElement(tna, false);
                        
                        if(e.getClickCount() == 1)
                            openAndFocusElement(tna, true); //select active line only
                        
                    }
                }
            };
            tree.addMouseListener(ml);
            
            final TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
            selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            tree.setSelectionModel(selectionModel);
            tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "open"); // NOI18N
            tree.getActionMap().put("open", new AbstractAction() { // NOI18N
                public void actionPerformed(ActionEvent e) {
                    TreePath selPath = selectionModel.getLeadSelectionPath();
                    TreeNodeAdapter tna = (TreeNodeAdapter)selPath.getLastPathComponent();
                    openAndFocusElement(tna, false);
                }
            });
            
            JScrollPane treeView = new JScrollPane(tree);
            treeView.setBorder(BorderFactory.createEmptyBorder());
            treeView.setViewportBorder(BorderFactory.createEmptyBorder());
            
            add(treeView, BorderLayout.CENTER);
            
            //create the TapPanel
            TapPanel filtersPanel = new TapPanel();
            JLabel filtersLbl = new JLabel(NbBundle.getMessage(NavigatorContent.class, "LBL_Filter")); //NOI18N
            filtersLbl.setBorder(new EmptyBorder(0, 5, 5, 0));
            filtersPanel.add(filtersLbl);
            filtersPanel.setOrientation(TapPanel.DOWN);
            // tooltip
            KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
            String keyText = org.openide.util.Utilities.keyToString(toggleKey);
            filtersPanel.setToolTipText(NbBundle.getMessage(NavigatorContent.class, "TIP_TapPanel", keyText));
            
            //create FiltersManager
            filters = createFilters();
            //listen to filters changes
            filters.hookChangeListener(this);
            
            filtersPanel.add(filters.getComponent());
            
            add(filtersPanel, BorderLayout.SOUTH);
            
            //add popup menu mouse listener
            MouseListener pmml = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if(e.getClickCount() == 1 && e.getModifiers() == MouseEvent.BUTTON3_MASK) {
                        //show popup
                        JPopupMenu pm = new JPopupMenu();
                        JMenuItem[] items = new FilterActions(filters).createMenuItems();
                        //add filter actions
                        for(int i = 0; i < items.length; i++) pm.add(items[i]);
                        pm.pack();
                        pm.show(tree, e.getX(), e.getY());
                    }
                }
            };
            tree.addMouseListener(pmml);
            
            //expand all root elements which are tags
            TreeNode rootNode = (TreeNode)model.getRoot();
            for(int i = 0; i < rootNode.getChildCount(); i++) {
                TreeNode node = rootNode.getChildAt(i);
                if(node.getChildCount() > 0)
                    tree.expandPath(new TreePath(new TreeNode[]{rootNode, node}));
            }
        }
        
        public Document getDocument() {
            return this.doc;
        }
        
        private void openAndFocusElement(final TreeNodeAdapter selected, final boolean selectLineOnly) {
            BaseDocument bdoc = (BaseDocument)selected.getDocumentElement().getDocument();
            DataObject dobj = NbEditorUtilities.getDataObject(bdoc);
            if(dobj == null) return ;
            
            final EditorCookie.Observable ec = (EditorCookie.Observable)dobj.getCookie(EditorCookie.Observable.class);
            if(ec == null) return ;
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JEditorPane[] panes = ec.getOpenedPanes();
                    if (panes != null && panes.length > 0) {
                        // editor already opened, so just select
                        selectElementInPane(panes[0], selected, !selectLineOnly);
                    } else if(!selectLineOnly) {
                        // editor not opened yet
                        ec.open();
                        try {
                            ec.openDocument(); //wait to editor to open
                            panes = ec.getOpenedPanes();
                            if (panes != null && panes.length > 0) {
                                selectElementInPane(panes[0], selected, true);
                            }
                        }catch(IOException ioe) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                        }
                    }
                }
            });
        }
        
        private void selectElementInPane(final JEditorPane pane, final TreeNodeAdapter tna, final boolean focus) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    pane.setCaretPosition(tna.getDocumentElement().getStartOffset());
                }
            });
            if(focus) {
                // try to activate outer TopComponent
                Container temp = pane;
                while (!(temp instanceof TopComponent)) {
                    temp = temp.getParent();
                }
                ((TopComponent) temp).requestActive();
            }
        }
        
        private TreeModel createTreeModel(DocumentModel dm) {
            DocumentElement rootElement = dm.getRootElement();
            DefaultTreeModel dtm = new DefaultTreeModel(null);
            TreeNodeAdapter rootTna = new TreeNodeAdapter(rootElement, dtm, tree, null);
            dtm.setRoot(rootTna);
            
            return dtm;
        }
        
        /** Creates filter descriptions and filters itself */
        private FiltersManager createFilters() {
            FiltersDescription desc = new FiltersDescription();
            
            desc.addFilter(ATTRIBUTES_FILTER,
                    NbBundle.getMessage(NavigatorContent.class, "LBL_ShowAttributes"),     //NOI18N
                    NbBundle.getMessage(NavigatorContent.class, "LBL_ShowAttributesTip"),     //NOI18N
                    showAttributes, ImageUtilities.loadImageIcon("org/netbeans/modules/xml/text/navigator/resources/a.png", false), //NOI18N
                    null
                    );
            desc.addFilter(CONTENT_FILTER,
                    NbBundle.getMessage(NavigatorContent.class, "LBL_ShowContent"),     //NOI18N
                    NbBundle.getMessage(NavigatorContent.class, "LBL_ShowContentTip"),     //NOI18N
                    showContent, ImageUtilities.loadImageIcon("org/netbeans/modules/xml/text/navigator/resources/content.png", false), //NOI18N
                    null
                    );
            
            return FiltersDescription.createManager(desc);
        }
        
        
        public void filterStateChanged(ChangeEvent e) {
            showAttributes = filters.isSelected(ATTRIBUTES_FILTER);
            showContent = filters.isSelected(CONTENT_FILTER);
            
            tree.repaint();
        }
        
        private class PatchedJTree extends JTree {
            
            private boolean firstPaint;
            
            public PatchedJTree() {
                super();
                firstPaint = true;
            }
            
            /** Overriden to calculate correct row height before first paint */
            public void paint(Graphics g) {
                if (firstPaint) {
                    int height = g.getFontMetrics(getFont()).getHeight();
                    setRowHeight(height + 2);
                    firstPaint = false;
                }
                super.paint(g);
            }
            
        }
        
        public static final String ATTRIBUTES_FILTER = "attrs";
        public static final String CONTENT_FILTER = "content";
        
    }
        
}


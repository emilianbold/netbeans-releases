/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */   
package org.netbeans.modules.mobility.svgcore.navigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.netbeans.modules.mobility.svgcore.view.source.SVGSourceMultiViewElement;
import org.netbeans.modules.mobility.svgcore.view.svg.SelectionCookie;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author Pavel Benes (based on the class NavigatorContent by Marek Fukala)
 */
public class SVGNavigatorContent extends JPanel {
    public static final String ATTRIBUTES_FILTER = "attrs";
    public static final String ID_FILTER         = "id";
    public static final String ANIMATION_FILTER  = "anim";
    
    private static final boolean DEBUG = false;
    private static SVGNavigatorContent navigatorContentInstance = null;
    
    private WeakHashMap<SVGDataObject, WeakReference> uiCache = new WeakHashMap<SVGDataObject, WeakReference>();
    
    public static synchronized SVGNavigatorContent getDefault() {
        if(navigatorContentInstance == null) {
            navigatorContentInstance = new SVGNavigatorContent();
        }
        return navigatorContentInstance;
    }
    
    private JPanel active = null;
    private final JPanel emptyPanel;    
    private final JLabel msgLabel;    
    private SVGDataObject peerDO = null;
        
    private SVGNavigatorContent() {
        setLayout(new BorderLayout());
        //init empty panel
        setBackground(Color.WHITE);
        emptyPanel = new JPanel();
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setLayout(new BorderLayout());
        msgLabel = new JLabel();
        emptyPanel.add(msgLabel, BorderLayout.CENTER);
    }
    
    public void navigate(final SVGDataObject d) { 
        //TODO refactor
        peerDO = d;
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                showWaitPanel();
/*                
                Document bdoc;
                
                try {
                    bdoc = d.getModel().getDocument();
                } catch( Exception e) {
                    showCannotNavigate();
                    return;
                }                
*/                
                final JPanel cachedPanel;
                WeakReference panelWR = (WeakReference)uiCache.get(d);
                if(panelWR != null) {
                    NavigatorContentPanel cp = (NavigatorContentPanel)panelWR.get();
                    if(cp != null) {
                        if(DEBUG) System.out.println("panel is cached");
                        //test if the document associated with the panel is the same we got now
                        cachedPanel = cp;
//                        cachedPanel = bdoc == cp.getDocument() ? cp : null;
//                        if(cachedPanel == null) {
//                            if(DEBUG) System.out.println("but the document is different - creating a new UI...");
//                            if(DEBUG) System.out.println("the cached document : " + cp.getDocument());
//
//                            //remove the old mapping from the cache
//                            uiCache.remove(d);
//                        }
                    } else
                        cachedPanel = null;
                } else
                    cachedPanel = null;

                JPanel panel = null;
                if(cachedPanel == null) {
                    try {
                        //cache the newly created panel
                        panel = new NavigatorContentPanel(d);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showCannotNavigate();
                        return;
                    }
                    //use the document dataobject as a key since the document itself is very easily discarded and hence
                    //harly usable as a key of the WeakHashMap
                    uiCache.put(d, new WeakReference<JPanel>(panel));
                    if(DEBUG) System.out.println("[xml navigator] panel created");                    
                } else {
                    panel = cachedPanel;
                    if(DEBUG) System.out.println("[xml navigator] panel gotten from cache");
                }
                final JPanel p = panel;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        SVGNavigatorContent.this.removeAll();
                        SVGNavigatorContent.this.add(p, BorderLayout.CENTER);                                
                        SVGNavigatorContent.this.validate();                                
                        SVGNavigatorContent.this.repaint();                                
                    }
                });
                
            }
        });        
    }
    
    void select(int [] path) {
//        try {
            WeakReference panelWR = (WeakReference)uiCache.get(peerDO);
            if(panelWR != null) {
                NavigatorContentPanel cp = (NavigatorContentPanel)panelWR.get();
                if(cp != null) {
                    cp.tree.selectNode(path);
                }
            }
//        } catch (PropertyVetoException ex){
//            ex.printStackTrace();
//        }
    }

    public void release() {
        removeAll();
        repaint();
    }
    
    public void showCannotNavigate() {
        removeAll();
        msgLabel.setIcon(null);
        msgLabel.setForeground(Color.GRAY);
        msgLabel.setText(NbBundle.getMessage(SVGNavigatorContent.class, "LBL_CannotNavigate")); //NOI18N
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(emptyPanel, BorderLayout.CENTER);
        repaint();
    }
    
    private void showWaitPanel() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                removeAll();
                msgLabel.setIcon(null);
                msgLabel.setForeground(Color.GRAY);
                msgLabel.setHorizontalAlignment(SwingConstants.LEFT);
                msgLabel.setText(NbBundle.getMessage(SVGNavigatorContent.class, "LBL_Wait")); //NOI18N
                add(emptyPanel, BorderLayout.NORTH);
                repaint();
            }
        });
    }

     private class NavigatorContentPanel extends JPanel implements 
                 FiltersManager.FilterChangeListener {
        private final SVGDataObject    doj;
        private final SVGNavigatorTree tree;
        private final FiltersManager   filters;

        private final SVGFileModel.ModelListener modelListener = new SVGFileModel.ModelListener() {
            public void modelChanged(int[] path) {
                assert SwingUtilities.isEventDispatchThread() : "Not in AWT event dispach thread";  //NOI18N
                try {
                    //TODO update only part of the tree according to the event
                    //chlds.update();
                } catch (Exception ex) {
                      ex.printStackTrace();
                }                                 
            }
        };
       
        
        public NavigatorContentPanel(SVGDataObject doj) throws Exception {
            this.doj = doj;
            setLayout(new BorderLayout());
            
            //create the JTree pane
            tree = new SVGNavigatorTree(doj);
            ToolTipManager.sharedInstance().registerComponent(tree);
            
            MouseListener ml = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int selRow = tree.getRowForLocation(e.getX(), e.getY());
                    if(selRow != -1) {
                        TreePath         selPath = tree.getPathForLocation(e.getX(), e.getY());
                        SVGNavigatorNode tna     = (SVGNavigatorNode)selPath.getLastPathComponent();                       
                        DocumentElement  de      = tna.getDocumentElement();
                        
                        switch( e.getClickCount()) {
                            case 1:
                                TopComponent tc = NavigatorContentPanel.this.doj.getMTVC();

                                if ( tc != null) {
                                    Lookup           lkp     = tc.getLookup();                                
                                    SelectionCookie  cookie  = (SelectionCookie)lkp.lookup(SelectionCookie.class);

                                    if ( cookie != null) {
                                        cookie.updateSelection(NavigatorContentPanel.this.doj, de, false);
                                    }
                                }
                                break;
                            case 2:
                                SVGSourceMultiViewElement.selectElement(NavigatorContentPanel.this.doj, de, true);
                                break;
                        }
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
                    SVGNavigatorNode tna = (SVGNavigatorNode)selPath.getLastPathComponent();
                    //openAndFocusElement(tna, false);
                }
            });
            
            JScrollPane treeView = new JScrollPane(tree);
            treeView.setBorder(BorderFactory.createEmptyBorder());
            treeView.setViewportBorder(BorderFactory.createEmptyBorder());
            
            add(treeView, BorderLayout.CENTER);
                      
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
            TreeNode rootNode = (TreeNode)tree.getTreeModel().getRoot();
            for(int i = 0; i < rootNode.getChildCount(); i++) {
                TreeNode node = rootNode.getChildAt(i);
                if(node.getChildCount() > 0)
                    tree.expandPath(new TreePath(new TreeNode[]{rootNode, node}));
            }
                                
            //create the TapPanel
            TapPanel filtersPanel = new TapPanel();
            JLabel filtersLbl = new JLabel(NbBundle.getMessage(SVGNavigatorContent.class, "LBL_Filter")); //NOI18N
            filtersLbl.setBorder(new EmptyBorder(0, 5, 5, 0));
            filtersPanel.add(filtersLbl);
            filtersPanel.setOrientation(TapPanel.DOWN);
            // tooltip
            KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
            String keyText = org.openide.util.Utilities.keyToString(toggleKey);
            filtersPanel.setToolTipText(NbBundle.getMessage(SVGNavigatorContent.class, "TIP_TapPanel", keyText));
            
            //create FiltersManager
            filters = createFilters();
            //listen to filters changes
            filters.hookChangeListener(this);
            
            filtersPanel.add(filters.getComponent());
            
            add(filtersPanel, BorderLayout.SOUTH);
            
            doj.getModel().addModelListener(modelListener);
        }        
        
        /** Creates filter descriptions and filters itself */
        private FiltersManager createFilters() {
            FiltersDescription desc = new FiltersDescription();
            
            desc.addFilter(ATTRIBUTES_FILTER,
                    NbBundle.getMessage(SVGNavigatorContent.class, "LBL_ShowAttributes"),     //NOI18N
                    NbBundle.getMessage(SVGNavigatorContent.class, "LBL_ShowAttributesTip"),     //NOI18N
                    SVGNavigatorTree.showAttributes,
                    new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/mobility/svgcore/resources/a.png")), //NOI18N
                    null
            );
            desc.addFilter(ID_FILTER,
                    NbBundle.getMessage(SVGNavigatorContent.class, "LBL_ShowId"),     //NOI18N
                    NbBundle.getMessage(SVGNavigatorContent.class, "LBL_ShowIdTip"),     //NOI18N
                    SVGNavigatorTree.showIdOnly,
                    new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/mobility/svgcore/resources/filterIdentified.png")), //NOI18N
                    null
            );
            desc.addFilter(ANIMATION_FILTER,
                    NbBundle.getMessage(SVGNavigatorContent.class, "LBL_ShowAnimation"),     //NOI18N
                    NbBundle.getMessage(SVGNavigatorContent.class, "LBL_ShowAnimationTip"),     //NOI18N
                    SVGNavigatorTree.showAnimationsOnly,
                    new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/mobility/svgcore/resources/filterAnimations.png")), //NOI18N
                    null
            );
            
            return FiltersDescription.createManager(desc);
        }

        public void filterStateChanged(ChangeEvent e) {
            boolean filterChanged         = false;   
            boolean attrVisibilityChanged = false;
            boolean selected;
            
            selected = filters.isSelected(ATTRIBUTES_FILTER);
            if ( selected != SVGNavigatorTree.showAttributes) {
                SVGNavigatorTree.showAttributes = selected;
                attrVisibilityChanged = true;
            }

            if ( (selected=filters.isSelected(ID_FILTER)) != SVGNavigatorTree.showIdOnly) {
                filterChanged = true;
                SVGNavigatorTree.showIdOnly = selected;
            };
            if ( (selected=filters.isSelected(ANIMATION_FILTER)) != SVGNavigatorTree.showAnimationsOnly) {
                filterChanged = true;
                SVGNavigatorTree.showAnimationsOnly = selected;
            };
            
            if (filterChanged) {
                tree.filterChanged();
            } else {
                if (attrVisibilityChanged) {
                    tree.validate();
                    tree.repaint();
                }
            }
        }
     }    
}


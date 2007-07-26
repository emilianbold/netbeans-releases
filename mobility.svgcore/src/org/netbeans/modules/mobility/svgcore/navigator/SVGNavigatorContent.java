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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.netbeans.modules.mobility.svgcore.view.source.SVGSourceMultiViewElement;
import org.netbeans.modules.mobility.svgcore.view.svg.AnimationCookie;
import org.netbeans.modules.mobility.svgcore.view.svg.SelectionCookie;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author Pavel Benes (based on the class NavigatorContent by Marek Fukala)
 */
public class SVGNavigatorContent extends JPanel implements SceneManager.SelectionListener {
    public static final String ATTRIBUTES_FILTER = "attrs"; //NOI18N
    public static final String ID_FILTER         = "id";    //NOI18N
    public static final String ANIMATION_FILTER  = "anim";  //NOI18N
    
    //private static final boolean DEBUG = false;
    private static SVGNavigatorContent navigatorContentInstance = null;
    
    public static synchronized SVGNavigatorContent getDefault() {
        if(navigatorContentInstance == null) {
            navigatorContentInstance = new SVGNavigatorContent();
        }
        return navigatorContentInstance;
    }
    
    private final JPanel        emptyPanel;    
    private final JLabel        msgLabel;    
    private       SVGDataObject peerDO = null;
    private       NavigatorContentPanel  navigatorPanel = null; 
        
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

    public void selectionChanged( SVGObject [] newSelection, SVGObject [] oldSelection, boolean isReadOnly) {
        if (newSelection != null) {
            select( newSelection[0].getElementId());
        } 
    }
    
    public synchronized void navigate(final SVGDataObject d) {   
        if (d != peerDO) {
            if (peerDO != null) {
                peerDO.getSceneManager().removeSelectionListener(this);
            }
            peerDO = d;
            
            if (d != null) {
                d.getSceneManager().addSelectionListener( this);
                
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        showWaitPanel();
                        final NavigatorContentPanel panel;
                        try {
                            //cache the newly created panel
                            panel = new NavigatorContentPanel(d);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showCannotNavigate();
                            return;
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                setContent(d, panel);
                            }
                        });

                    }
                });        
            } else {
                setContent(null, null);
            }
        }
    }

    public synchronized void setContent( final SVGDataObject obj, final NavigatorContentPanel panel) {
        //peerDO         = obj;
        navigatorPanel = panel;
                
        SVGNavigatorContent.this.removeAll();
        if (panel != null) {
            SVGNavigatorContent.this.add(panel, BorderLayout.CENTER);                                
        }
        SVGNavigatorContent.this.validate();                                
        SVGNavigatorContent.this.repaint();                                
    }
    
    synchronized void select(String selectedId) {
        if (navigatorPanel != null) {
            navigatorPanel.tree.selectNode(selectedId);
        }
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
        private final SVGDataObject    m_doj;
        private final SVGNavigatorTree tree;
        private final FiltersManager   filters;

        private final SVGFileModel.ModelListener modelListener = new SVGFileModel.ModelListener() {
            public void modelChanged() {
                tree.repaint();
            }

            public void modelSwitched() {
                SwingUtilities.invokeLater(new Runnable() {
                   public void run() {
                       try {
                        tree.initialize();
                        tree.repaint();
                       } catch( Exception e) {
                           e.printStackTrace();
                       }
                   } 
                });
            }            
        };       
        
        public NavigatorContentPanel(SVGDataObject doj) throws Exception {
            m_doj = doj;
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
                                TopComponent tc = m_doj.getMTVC();

                                if ( tc != null) {
                                    Lookup           lkp     = tc.getLookup();                                
                                    SelectionCookie  cookie  = (SelectionCookie)lkp.lookup(SelectionCookie.class);

                                    if ( cookie != null) {
                                        cookie.updateSelection(m_doj, de, false);
                                    }
                                }
                                break;
                            case 2:
                                SVGSourceMultiViewElement.selectElement(m_doj, de, true);
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
                public void mousePressed(final MouseEvent e) {
                    if(e.getClickCount() == 1 && e.getModifiers() == MouseEvent.BUTTON3_MASK) {
                        //show popup
                        JPopupMenu pm = new JPopupMenu();
                        
                        final AnimationCookie animCookie = (AnimationCookie) getCookie(AnimationCookie.class);
                        final DocumentElement de         = getElementAt(e.getX(), e.getY());
                        boolean               isReadOnly = m_doj.getSceneManager().isReadOnly();
                        
                        if (animCookie != null && de != null && 
                            SVGFileModel.isAnimation(de)) {

                            JMenuItem animStart = new JMenuItem(NbBundle.getMessage(SVGNavigatorContent.class, "LBL_AnimStart")); //NOI18N
                            animStart.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent evt) {
                                    animCookie.startAnimation(m_doj, de);
                                }
                            });
                            animStart.setEnabled(isReadOnly);
                            pm.add(animStart);

                            JMenuItem animStop = new JMenuItem(NbBundle.getMessage(SVGNavigatorContent.class, "LBL_AnimStop")); //NOI18N
                            animStop.addActionListener(new java.awt.event.ActionListener() {
                                public void actionPerformed(ActionEvent evt) {
                                    animCookie.stopAnimation(m_doj, de);
                                }
                            });

                            animStop.setEnabled(isReadOnly);
                            pm.add(animStop);
                        }
                        
                        JMenuItem[] items = new FilterActions(filters).createMenuItems();
                        //add filter actions
                        for(int i = 0; i < items.length; i++) {
                            pm.add(items[i]);
                        }
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

        protected Cookie getCookie(Class clazz) {
            Cookie       cookie = null;;
            TopComponent tc     = m_doj.getMTVC();

            if ( tc != null) {
                cookie = (Cookie) tc.getLookup().lookup(clazz);
            }
            return cookie;
        }

        protected DocumentElement getElementAt( int x, int y) {
            DocumentElement de     = null;
            int             selRow = tree.getRowForLocation(x, y);
            
            if(selRow != -1) {
                TreePath         selPath = tree.getPathForLocation(x, y);
                SVGNavigatorNode tna     = (SVGNavigatorNode)selPath.getLastPathComponent();                       
                de = tna.getDocumentElement();
            }
            return de;
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


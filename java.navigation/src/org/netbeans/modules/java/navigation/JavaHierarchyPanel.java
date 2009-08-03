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

package org.netbeans.modules.java.navigation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.lang.model.element.Element;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.netbeans.api.java.source.CompilationInfo;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
public class JavaHierarchyPanel extends javax.swing.JPanel {
    private static TreeModel pleaseWaitTreeModel;
    static
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        root.add(new DefaultMutableTreeNode(NbBundle.getMessage(JavaHierarchyPanel.class, "LBL_WaitNode"))); // NOI18N
        pleaseWaitTreeModel = new DefaultTreeModel(root);
    }
    
    private FileObject fileObject;
    private JavaHierarchyModel javaHierarchyModel;

    /**
     *
     * @param fileObject
     * @param elements
     * @param compilationInfo
     */
    public JavaHierarchyPanel(FileObject fileObject, Element[] elements, CompilationInfo compilationInfo) {
        this.fileObject = fileObject;
        initComponents();
        
        docPane = new DocumentationScrollPane( true );
        splitPane.setRightComponent( docPane );
        splitPane.setDividerLocation(JavaMembersAndHierarchyOptions.getHierarchyDividerLocation());        
        
        ToolTipManager.sharedInstance().registerComponent(javaHierarchyTree);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        caseSensitiveFilterCheckBox.setSelected(JavaMembersAndHierarchyOptions.isCaseSensitive());
        showSuperTypeHierarchyToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowSuperTypeHierarchy());
        showSubTypeHierarchyToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowSubTypeHierarchy());
        showFQNToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowFQN());
        showInnerToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowInner());

        javaHierarchyTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        javaHierarchyTree.setRootVisible(false);
        javaHierarchyTree.setShowsRootHandles(true);
        javaHierarchyTree.setCellRenderer(new JavaTreeCellRenderer());

        javaHierarchyModel = new JavaHierarchyModel(fileObject, elements, compilationInfo);
        javaHierarchyTree.setModel(javaHierarchyModel);

        registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                close();
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        filterTextField.getDocument().addDocumentListener(
                new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                selectMatchingRow();
            }
            public void insertUpdate(DocumentEvent e) {
                selectMatchingRow();
            }
            public void removeUpdate(DocumentEvent e) {
                selectMatchingRow();
            }
        }
        );

        filterTextField.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.firstRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, false),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.previousRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.nextRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.lastRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, false),
                JComponent.WHEN_FOCUSED);

        signatureEditorPane.putClientProperty(
            "HighlightsLayerExcludes", // NOI18N
            "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" // NOI18N
        );

        signatureEditorPane.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.firstRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, false),
                JComponent.WHEN_FOCUSED);

        signatureEditorPane.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.previousRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
                JComponent.WHEN_FOCUSED);

        signatureEditorPane.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.nextRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false),
                JComponent.WHEN_FOCUSED);

        signatureEditorPane.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.lastRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, false),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath treePath = javaHierarchyTree.getSelectionPath();
                if (treePath != null) {
                    Object node = treePath.getLastPathComponent();
                    if (node instanceof JavaElement) {
                        // TODO
                        applyFilter();
                    }
                }
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath treePath = javaHierarchyTree.getSelectionPath();
                if (treePath != null) {
                    Object node = treePath.getLastPathComponent();
                    if (node instanceof JavaElement) {
                        gotoElement((JavaElement) node);
                    }
                }
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Component view = docPane.getViewport().getView();
                        if (view instanceof JEditorPane) {
                            JEditorPane editorPane = (JEditorPane) view;
                            ActionListener actionForKeyStroke =
                                editorPane.getActionForKeyStroke(
                                        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0, false));                            
                            actionForKeyStroke.actionPerformed(
                                    new ActionEvent(editorPane, ActionEvent.ACTION_PERFORMED, ""));
                        }
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.SHIFT_MASK, false),
                JComponent.WHEN_FOCUSED);
        filterTextField.registerKeyboardAction(
                new ActionListener() {
                    private boolean firstTime = true;
                    public void actionPerformed(ActionEvent actionEvent) {
                        Component view = docPane.getViewport().getView();
                        if (view instanceof JEditorPane) {
                            JEditorPane editorPane = (JEditorPane) view;
                            ActionListener actionForKeyStroke =
                                editorPane.getActionForKeyStroke(
                                        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0, false));
                            actionEvent = new ActionEvent(editorPane, ActionEvent.ACTION_PERFORMED, "");
                            actionForKeyStroke.actionPerformed(actionEvent);
                            if (firstTime) {
                                actionForKeyStroke.actionPerformed(actionEvent);
                                firstTime = false;
                            }
                        }
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.SHIFT_MASK, false),
                JComponent.WHEN_FOCUSED);

        caseSensitiveFilterCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                JavaMembersAndHierarchyOptions.setCaseSensitive(caseSensitiveFilterCheckBox.isSelected());
                if (filterTextField.getText().trim().length() > 0) {
                    // apply filters again only if there is some filter text
                    selectMatchingRow();
                }
            }
        });

        javaHierarchyTree.addMouseListener(
                new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                Point point = me.getPoint();
                TreePath treePath = javaHierarchyTree.getPathForLocation(point.x, point.y);
                if (treePath != null) {
                    Object node = treePath.getLastPathComponent();
                    if (node instanceof JavaElement) {
                        if (me.getClickCount() == 1) {
                            if (me.isControlDown()) {
                                JavaElement javaToolsJavaElement = (JavaElement) node;
                                applyFilter();
                            }
                        }  else if (me.getClickCount() == 2){
                            gotoElement((JavaElement) node);
                        }
                    }
                }
            }
        }
        );

        javaHierarchyTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                showSignature();
                showJavaDoc();
            }
        });

        javaHierarchyTree.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath treePath = javaHierarchyTree.getLeadSelectionPath();
                if (treePath != null) {
                    Object node = treePath.getLastPathComponent();
                    if (node instanceof JavaElement) {
                        gotoElement((JavaElement) node);
                    }
                }
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                JComponent.WHEN_FOCUSED);

        javaHierarchyTree.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath treePath = javaHierarchyTree.getLeadSelectionPath();
                if (treePath != null) {
                    Object node = treePath.getLastPathComponent();
                    if (node instanceof JavaElement) {
                        // TODO
                        applyFilter();
                    }
                }
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true),
                JComponent.WHEN_FOCUSED);

        showSuperTypeHierarchyToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                // Prevent reloading of super type hierarchy
                if (!JavaMembersAndHierarchyOptions.isShowSuperTypeHierarchy()) {
                    applyFilter(true);
                }
            }
        });

        showSubTypeHierarchyToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                // Prevent reloading of sub type hierarchy
                if (!JavaMembersAndHierarchyOptions.isShowSubTypeHierarchy()) {
                    applyFilter(true);
                }
            }
        });

        showFQNToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                JavaMembersAndHierarchyOptions.setShowFQN(showFQNToggleButton.isSelected());
                javaHierarchyModel.fireTreeNodesChanged();
            }
        });

        showInnerToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyFilter(true);
            }
        });

        expandAllButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        expandAll();
                    }
                });

        closeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        close();
                    }
                });
    }

    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                applyFilter(true);
                filterTextField.requestFocusInWindow();
            }           
        });
    }

    public void removeNotify() {
        JavaMembersAndHierarchyOptions.setHierarchyDividerLocation(splitPane.getDividerLocation());
        docPane.setData( null );
        super.removeNotify();
    }
    
    // Hack to allow showing of Help window when F1 or HELP key is pressed.
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (e.getKeyCode() == KeyEvent.VK_F1 || e.getKeyCode() == KeyEvent.VK_HELP)  {
            JComponent rootPane = SwingUtilities.getRootPane(this);
            if (rootPane != null) {
                rootPane.putClientProperty(ResizablePopup.HELP_COOKIE, Boolean.TRUE); // NOI18N
            }
        }
        return super.processKeyBinding(ks, e, condition, pressed);
    }

    private Component lastFocusedComponent;
    
    private void enterBusy() {
        javaHierarchyTree.setModel(pleaseWaitTreeModel);
        JRootPane rootPane = SwingUtilities.getRootPane(JavaHierarchyPanel.this);
        if (rootPane != null) {
            rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            lastFocusedComponent = window.getFocusOwner();
        }
        filterTextField.setEnabled(false);  
        caseSensitiveFilterCheckBox.setEnabled(false);
        showSubTypeHierarchyToggleButton.setEnabled(false);
        showSuperTypeHierarchyToggleButton.setEnabled(false);
        showFQNToggleButton.setEnabled(false);
        showInnerToggleButton.setEnabled(false);
        expandAllButton.setEnabled(false);
    }
    
    private void leaveBusy() {
        javaHierarchyTree.setModel(javaHierarchyModel);
        JRootPane rootPane = SwingUtilities.getRootPane(JavaHierarchyPanel.this);
        if (rootPane != null) {
            rootPane.setCursor(Cursor.getDefaultCursor());
        }
        filterTextField.setEnabled(true);  
        caseSensitiveFilterCheckBox.setEnabled(true);
        showSubTypeHierarchyToggleButton.setEnabled(true);
        showSuperTypeHierarchyToggleButton.setEnabled(true);
        showFQNToggleButton.setEnabled(true);
        showInnerToggleButton.setEnabled(true);
        expandAllButton.setEnabled(true);
        if (lastFocusedComponent != null) {
            if (lastFocusedComponent.isDisplayable()) {
                lastFocusedComponent.requestFocusInWindow();
            }
            lastFocusedComponent = null;
        }
    }
    
    private void applyFilter() {
        applyFilter(true);
    }
    
    private void applyFilter(final boolean structural) {
        if (structural) {
            enterBusy();
        }

        JavaMembersAndHierarchyOptions.setCaseSensitive(caseSensitiveFilterCheckBox.isSelected());
        JavaMembersAndHierarchyOptions.setShowSuperTypeHierarchy(showSuperTypeHierarchyToggleButton.isSelected());
        JavaMembersAndHierarchyOptions.setShowSubTypeHierarchy(showSubTypeHierarchyToggleButton.isSelected());
        JavaMembersAndHierarchyOptions.setShowFQN(showFQNToggleButton.isSelected());
        JavaMembersAndHierarchyOptions.setShowInner(showInnerToggleButton.isSelected());

        RequestProcessor.getDefault().post(
            new Runnable() {
                public void run() {
                    try {
    
                        if (structural) {
                                javaHierarchyModel.update();
                        }
                    } finally {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                if (structural) {
                                    leaveBusy();
                                }
                                // expand the tree
                                for (int row = 0; row < javaHierarchyTree.getRowCount(); row++) {
                                    TreePath treePath = javaHierarchyTree.getPathForRow(row);
                                    if (JavaMembersAndHierarchyOptions.isShowSubTypeHierarchy()) {
                                        if (treePath.getPathCount() < JavaMembersAndHierarchyOptions.getSubTypeHierarchyDepth()) {
                                            javaHierarchyTree.expandRow(row);
                                        }
                                    } else {
                                        javaHierarchyTree.expandRow(row);
                                    }
                                }
                            }});
                    }
                }
            });
    }

    private void expandAll() {
        SwingUtilities.invokeLater(
                new Runnable() {
            public void run() {
                JRootPane rootPane = SwingUtilities.getRootPane(JavaHierarchyPanel.this);
                if (rootPane != null) {
                    rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                }
            }
        }
        );

        SwingUtilities.invokeLater(
                new Runnable() {
            public void run() {
                try {
                    // expand the tree
                    for (int row = 0; row < javaHierarchyTree.getRowCount(); row++) {
                        javaHierarchyTree.expandRow(row);
                    }
                    selectMatchingRow();
                } finally {
                    JRootPane rootPane = SwingUtilities.getRootPane(JavaHierarchyPanel.this);
                    if (rootPane != null) {
                        rootPane.setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        }
        );
    }

    private void selectMatchingRow() {
        filterTextField.setForeground(UIManager.getColor("TextField.foreground"));
        javaHierarchyTree.setSelectionRow(-1);
        // select first matching
        for (int row = 0; row < javaHierarchyTree.getRowCount(); row++) {
            Object o = javaHierarchyTree.getPathForRow(row).getLastPathComponent();
            if (o instanceof JavaElement) {
                String filterText = filterTextField.getText();
                if (Utils.patternMatch((JavaElement)o, filterText, filterText.toLowerCase())) {
                    javaHierarchyTree.setSelectionRow(row);
                    javaHierarchyTree.scrollRowToVisible(row);
                    return;
                }
            }
        }
        filterTextField.setForeground(Color.RED);
    }

    private void gotoElement(JavaElement javaToolsJavaElement) {
        try {
            javaToolsJavaElement.gotoElement();
        } finally {
            close();
        }
    }

    private void showSignature() {
        signatureEditorPane.setText("");
        signatureEditorPane.setToolTipText(null);
        TreePath treePath = javaHierarchyTree.getSelectionPath();
        if (treePath != null) {
            Object node = treePath.getLastPathComponent();
            if (node instanceof JavaElement) {
                signatureEditorPane.setText(((JavaElement)node).getTooltip());
                signatureEditorPane.setCaretPosition(0);
                signatureEditorPane.setToolTipText(((JavaElement)node).getTooltip());
            }
        }
    }

    private void showJavaDoc() {
        TreePath treePath = javaHierarchyTree.getSelectionPath();
        if (treePath != null) {
            Object node = treePath.getLastPathComponent();
            if (node instanceof JavaElement) {
                docPane.setData( ((JavaElement)node).getJavaDoc() );
            }
        }
    }

    private DocumentationScrollPane docPane;
    
    private void close() {
        Window window = SwingUtilities.getWindowAncestor(JavaHierarchyPanel.this);
        if (window != null) {
            window.setVisible(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javaHierarchyModeButtonGroup = new javax.swing.ButtonGroup();
        filterLabel = new javax.swing.JLabel();
        filterTextField = new javax.swing.JTextField();
        caseSensitiveFilterCheckBox = new javax.swing.JCheckBox();
        splitPane = new javax.swing.JSplitPane();
        javaHierarchyTreeScrollPane = new javax.swing.JScrollPane();
        javaHierarchyTree = new javax.swing.JTree();
        signatureEditorPane = new javax.swing.JEditorPane();
        filtersLabel = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        filtersToolbar = new NoBorderToolBar();
        showSuperTypeHierarchyToggleButton = new javax.swing.JToggleButton();
        showSubTypeHierarchyToggleButton = new javax.swing.JToggleButton();
        showFQNToggleButton = new javax.swing.JToggleButton();
        showInnerToggleButton = new javax.swing.JToggleButton();
        expandAllButton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        filterLabel.setLabelFor(filterTextField);
        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getBundle(JavaHierarchyPanel.class).getString("LABEL_filterLabel")); // NOI18N

        filterTextField.setToolTipText(org.openide.util.NbBundle.getBundle(JavaHierarchyPanel.class).getString("TOOLTIP_filterTextField")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(caseSensitiveFilterCheckBox, org.openide.util.NbBundle.getBundle(JavaHierarchyPanel.class).getString("LABEL_caseSensitiveFilterCheckBox")); // NOI18N
        caseSensitiveFilterCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        splitPane.setDividerLocation(350);
        splitPane.setOneTouchExpandable(true);

        javaHierarchyTreeScrollPane.setBorder(null);
        javaHierarchyTreeScrollPane.setViewportView(javaHierarchyTree);
        javaHierarchyTree.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JavaHierarchyPanel.class, "ACSD_JavaHierarchyTree")); // NOI18N

        splitPane.setLeftComponent(javaHierarchyTreeScrollPane);

        signatureEditorPane.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Nb.ScrollPane.Border.color")));
        signatureEditorPane.setContentType("text/x-java");
        signatureEditorPane.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(filtersLabel, org.openide.util.NbBundle.getMessage(JavaHierarchyPanel.class, "LABEL_filtersLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(closeButton, org.openide.util.NbBundle.getMessage(JavaHierarchyPanel.class, "LABEL_Close")); // NOI18N

        filtersToolbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        filtersToolbar.setFloatable(false);
        filtersToolbar.setBorderPainted(false);
        filtersToolbar.setOpaque(false);

        javaHierarchyModeButtonGroup.add(showSuperTypeHierarchyToggleButton);
        showSuperTypeHierarchyToggleButton.setIcon(JavaMembersAndHierarchyIcons.SUPER_TYPE_HIERARCHY_ICON);
        showSuperTypeHierarchyToggleButton.setMnemonic('S');
        showSuperTypeHierarchyToggleButton.setSelected(true);
        showSuperTypeHierarchyToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaHierarchyPanel.class).getString("TOOLTIP_showSuperTypeHierarchyToggleButton")); // NOI18N
        showSuperTypeHierarchyToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showSuperTypeHierarchyToggleButton);

        javaHierarchyModeButtonGroup.add(showSubTypeHierarchyToggleButton);
        showSubTypeHierarchyToggleButton.setIcon(JavaMembersAndHierarchyIcons.SUB_TYPE_HIERARCHY_ICON);
        showSubTypeHierarchyToggleButton.setMnemonic('B');
        showSubTypeHierarchyToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaHierarchyPanel.class).getString("TOOLTIP_showSubTypeHierarchyToggleButton")); // NOI18N
        showSubTypeHierarchyToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showSubTypeHierarchyToggleButton);

        showFQNToggleButton.setIcon(JavaMembersAndHierarchyIcons.FQN_ICON);
        showFQNToggleButton.setMnemonic('Q');
        showFQNToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaHierarchyPanel.class).getString("TOOLTIP_showFQNToggleButton")); // NOI18N
        showFQNToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showFQNToggleButton);

        showInnerToggleButton.setIcon(JavaMembersAndHierarchyIcons.INNER_CLASS_ICON);
        showInnerToggleButton.setMnemonic('I');
        showInnerToggleButton.setSelected(true);
        showInnerToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaHierarchyPanel.class).getString("TOOLTIP_showInnerToggleButton")); // NOI18N
        showInnerToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showInnerToggleButton);

        expandAllButton.setIcon(JavaMembersAndHierarchyIcons.EXPAND_ALL_ICON);
        expandAllButton.setMnemonic('E');
        expandAllButton.setToolTipText(org.openide.util.NbBundle.getMessage(JavaHierarchyPanel.class, "TOOLTIP_expandAll")); // NOI18N
        expandAllButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(expandAllButton);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 739, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(filterLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(caseSensitiveFilterCheckBox))
                    .add(signatureEditorPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 739, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(filtersLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filtersToolbar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 637, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(filterLabel)
                    .add(caseSensitiveFilterCheckBox)
                    .add(filterTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(signatureEditorPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(filtersLabel)
                        .add(closeButton))
                    .add(filtersToolbar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        caseSensitiveFilterCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JavaHierarchyPanel.class, "caseSensitiveFilterCheckBox_ACSD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JCheckBox caseSensitiveFilterCheckBox;
    public javax.swing.JButton closeButton;
    public javax.swing.JButton expandAllButton;
    public javax.swing.JLabel filterLabel;
    public javax.swing.JTextField filterTextField;
    public javax.swing.JLabel filtersLabel;
    public javax.swing.JToolBar filtersToolbar;
    public javax.swing.ButtonGroup javaHierarchyModeButtonGroup;
    public javax.swing.JTree javaHierarchyTree;
    public javax.swing.JScrollPane javaHierarchyTreeScrollPane;
    public javax.swing.JToggleButton showFQNToggleButton;
    public javax.swing.JToggleButton showInnerToggleButton;
    public javax.swing.JToggleButton showSubTypeHierarchyToggleButton;
    public javax.swing.JToggleButton showSuperTypeHierarchyToggleButton;
    public javax.swing.JEditorPane signatureEditorPane;
    public javax.swing.JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables
}

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.navigation;

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
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.netbeans.api.java.source.CompilationInfo;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
public class JavaHierarchyPanel extends javax.swing.JPanel {
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
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, true),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.previousRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.nextRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.lastRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, true),
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
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, true),
                JComponent.WHEN_FOCUSED);

        signatureEditorPane.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.previousRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true),
                JComponent.WHEN_FOCUSED);

        signatureEditorPane.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.nextRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true),
                JComponent.WHEN_FOCUSED);

        signatureEditorPane.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.lastRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, true),
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

        caseSensitiveFilterCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
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
                    applyFilter();
                }
            }
        });

        showSubTypeHierarchyToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                // Prevent reloading of sub type hierarchy
                if (!JavaMembersAndHierarchyOptions.isShowSubTypeHierarchy()) {
                    applyFilter();
                }
            }
        });

        showFQNToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyFilter();
            }
        });

        showInnerToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyFilter();
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

    private boolean showingSubDialog = false;

    public void addNotify() {
        super.addNotify();
        applyFilter();
    }

    public void removeNotify() {
        // Reset the hierarchy mode
        JavaMembersAndHierarchyOptions.setShowSuperTypeHierarchy(true);
        docPane.setData( null );
        super.removeNotify();
    }

    private void applyFilter() {
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
                    JavaMembersAndHierarchyOptions.setCaseSensitive(caseSensitiveFilterCheckBox.isSelected());
                    JavaMembersAndHierarchyOptions.setShowSuperTypeHierarchy(showSuperTypeHierarchyToggleButton.isSelected());
                    JavaMembersAndHierarchyOptions.setShowSubTypeHierarchy(showSubTypeHierarchyToggleButton.isSelected());
                    JavaMembersAndHierarchyOptions.setShowFQN(showFQNToggleButton.isSelected());
                    JavaMembersAndHierarchyOptions.setShowInner(showInnerToggleButton.isSelected());

                    javaHierarchyModel.update();

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
                        TreePath treePath = javaHierarchyTree.getPathForRow(row);
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
        javaHierarchyModel.setPattern(filterTextField.getText());
        // select first matching
        for (int row = 0; row < javaHierarchyTree.getRowCount(); row++) {
            Object o = javaHierarchyTree.getPathForRow(row).getLastPathComponent();
            if (o instanceof JavaElement) {
                if (javaHierarchyModel.patternMatch((JavaElement)o)) {
                    javaHierarchyTree.setSelectionRow(row);
                    break;
                }
            }
        }
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
        caseSensitiveFilterCheckBox.setFocusable(false);
        caseSensitiveFilterCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        splitPane.setDividerLocation(350);
        splitPane.setOneTouchExpandable(true);

        javaHierarchyTreeScrollPane.setBorder(null);
        javaHierarchyTreeScrollPane.setViewportView(javaHierarchyTree);

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
        showSuperTypeHierarchyToggleButton.setSelected(true);
        showSuperTypeHierarchyToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaHierarchyPanel.class).getString("TOOLTIP_showSuperTypeHierarchyToggleButton")); // NOI18N
        showSuperTypeHierarchyToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showSuperTypeHierarchyToggleButton);

        javaHierarchyModeButtonGroup.add(showSubTypeHierarchyToggleButton);
        showSubTypeHierarchyToggleButton.setIcon(JavaMembersAndHierarchyIcons.SUB_TYPE_HIERARCHY_ICON);
        showSubTypeHierarchyToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaHierarchyPanel.class).getString("TOOLTIP_showSubTypeHierarchyToggleButton")); // NOI18N
        showSubTypeHierarchyToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showSubTypeHierarchyToggleButton);

        showFQNToggleButton.setIcon(JavaMembersAndHierarchyIcons.FQN_ICON);
        showFQNToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaHierarchyPanel.class).getString("TOOLTIP_showFQNToggleButton")); // NOI18N
        showFQNToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showFQNToggleButton);

        showInnerToggleButton.setIcon(JavaMembersAndHierarchyIcons.INNER_CLASS_ICON);
        showInnerToggleButton.setSelected(true);
        showInnerToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaHierarchyPanel.class).getString("TOOLTIP_showInnerToggleButton")); // NOI18N
        showInnerToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showInnerToggleButton);

        expandAllButton.setIcon(JavaMembersAndHierarchyIcons.EXPAND_ALL_ICON);
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
                    .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 710, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(filterLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(caseSensitiveFilterCheckBox))
                    .add(signatureEditorPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 710, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(filtersLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filtersToolbar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
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

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

package org.netbeans.modules.web.beans.navigation;

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
import java.util.Collection;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.netbeans.api.java.source.CompilationController;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Based on org.netbeans.modules.java.navigation.JavaHierarchyPanel
 *
 * @author ads
 *
 */
public class AmbiguousInjectablesPanel extends javax.swing.JPanel {

    public static final Icon FQN_ICON = ImageUtilities.loadImageIcon(
            "org/netbeans/modules/java/navigation/resources/fqn.gif", false); // NOI18N

    public static final Icon EXPAND_ALL_ICON = ImageUtilities.loadImageIcon(
            "org/netbeans/modules/java/navigation/resources/expandall.gif", false); // NOI18N

    private static TreeModel pleaseWaitTreeModel;
    static
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        root.add(new DefaultMutableTreeNode(NbBundle.getMessage(
                AmbiguousInjectablesPanel.class, "LBL_WaitNode"))); // NOI18N
        pleaseWaitTreeModel = new DefaultTreeModel(root);
    }
    
    private AmbiguousInjectablesModel javaHierarchyModel;

    public AmbiguousInjectablesPanel(Collection<Element> elements, 
            VariableElement var,  List<AnnotationMirror> bindings , 
            CompilationController controller ) 
    {
        initComponents();
        
        initInjectionPoint( var, bindings , controller );
        
        docPane = new DocumentationScrollPane( true );
        mySplitPane.setRightComponent( docPane );
        // TODO splitPane.setDividerLocation(JavaMembersAndHierarchyOptions.getHierarchyDividerLocation());
        
        ToolTipManager.sharedInstance().registerComponent(javaHierarchyTree);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        caseSensitiveFilterCheckBox.setSelected(WebBeansNavigationOptions.isCaseSensitive());
        showFQNToggleButton.setSelected(WebBeansNavigationOptions.isShowFQN());

        javaHierarchyTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        javaHierarchyTree.setRootVisible(false);
        javaHierarchyTree.setShowsRootHandles(true);
        javaHierarchyTree.setCellRenderer(new JavaTreeCellRenderer());

        javaHierarchyModel = new AmbiguousInjectablesModel(elements, 
                controller );
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
                //TODO : Utils.firstRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, false),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //TODO : Utils.previousRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //TODO : Utils.lastRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, false),
                JComponent.WHEN_FOCUSED);

        myBindings.putClientProperty(
            "HighlightsLayerExcludes", // NOI18N
            "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" // NOI18N
        );

        myBindings.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //TODO : Utils.firstRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, false),
                JComponent.WHEN_FOCUSED);

        myBindings.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //TODO : Utils.previousRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
                JComponent.WHEN_FOCUSED);

        myBindings.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //TODO : Utils.nextRow(javaHierarchyTree);
            }
        },
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false),
                JComponent.WHEN_FOCUSED);

        myBindings.registerKeyboardAction(
                new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //TODO : Utils.lastRow(javaHierarchyTree);
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
                WebBeansNavigationOptions.setCaseSensitive(caseSensitiveFilterCheckBox.isSelected());
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
                showBindings();
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

        showFQNToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                WebBeansNavigationOptions.setShowFQN(showFQNToggleButton.isSelected());
                javaHierarchyModel.fireTreeNodesChanged();
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
        //TODO : JavaMembersAndHierarchyOptions.setHierarchyDividerLocation(splitPane.getDividerLocation());
        docPane.setData( null );
        super.removeNotify();
    }
    
    // Hack to allow showing of Help window when F1 or HELP key is pressed.
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        if (e.getKeyCode() == KeyEvent.VK_F1 || e.getKeyCode() == KeyEvent.VK_HELP)  {
            JComponent rootPane = SwingUtilities.getRootPane(this);
            if (rootPane != null) {
                //TODO : rootPane.putClientProperty(ResizablePopup.HELP_COOKIE, Boolean.TRUE); // NOI18N
            }
        }
        return super.processKeyBinding(ks, e, condition, pressed);
    }

    private Component lastFocusedComponent;
    
    private void enterBusy() {
        javaHierarchyTree.setModel(pleaseWaitTreeModel);
        JRootPane rootPane = SwingUtilities.getRootPane(AmbiguousInjectablesPanel.this);
        if (rootPane != null) {
            rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            lastFocusedComponent = window.getFocusOwner();
        }
        filterTextField.setEnabled(false);  
        caseSensitiveFilterCheckBox.setEnabled(false);
        showFQNToggleButton.setEnabled(false);
        expandAllButton.setEnabled(false);
    }
    
    private void leaveBusy() {
        javaHierarchyTree.setModel(javaHierarchyModel);
        JRootPane rootPane = SwingUtilities.getRootPane(AmbiguousInjectablesPanel.this);
        if (rootPane != null) {
            rootPane.setCursor(Cursor.getDefaultCursor());
        }
        filterTextField.setEnabled(true);  
        caseSensitiveFilterCheckBox.setEnabled(true);
        showFQNToggleButton.setEnabled(true);
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

        WebBeansNavigationOptions.setCaseSensitive(caseSensitiveFilterCheckBox.isSelected());
        WebBeansNavigationOptions.setShowFQN(showFQNToggleButton.isSelected());

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
                                    javaHierarchyTree.expandRow(row);
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
                JRootPane rootPane = SwingUtilities.getRootPane(AmbiguousInjectablesPanel.this);
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
                    JRootPane rootPane = SwingUtilities.getRootPane(AmbiguousInjectablesPanel.this);
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
                /*TODO :if (Utils.patternMatch((JavaElement)o, filterText, filterText.toLowerCase())) {
                    javaHierarchyTree.setSelectionRow(row);
                    javaHierarchyTree.scrollRowToVisible(row);
                    return;
                }*/
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

    private void showBindings() {
        myInjectableBindings.setText("");
        myInjectableBindings.setToolTipText(null);
        TreePath treePath = javaHierarchyTree.getSelectionPath();
        if (treePath != null) {
            Object node = treePath.getLastPathComponent();
            if (node instanceof JavaElement) {
                myInjectableBindings.setText(((JavaElement)node).getTooltip());
                myInjectableBindings.setCaretPosition(0);
                myInjectableBindings.setToolTipText(((JavaElement)node).getTooltip());
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
        Window window = SwingUtilities.getWindowAncestor(AmbiguousInjectablesPanel.this);
        if (window != null) {
            window.setVisible(false);
        }
    }
    

    private void initInjectionPoint( VariableElement var,
            List<AnnotationMirror> bindings , CompilationController controller )
    {
        TypeMirror typeMirror  = var.asType();
        Element element = controller.getTypes().asElement( typeMirror );
        String name ;
        if ( element == null ){
            name ="";
        }
        else {
            name = ( element instanceof TypeElement )? 
                ((TypeElement)element).getQualifiedName().toString() : 
                    element.getSimpleName().toString();
        }
        myType.setText(name);
        
        StringBuilder builder = new StringBuilder();
        for (AnnotationMirror annotationMirror : bindings) {
            DeclaredType type = annotationMirror.getAnnotationType();
            Element annotation = type.asElement();
            String annotationName  = ( annotation instanceof TypeElement )? 
                    ((TypeElement)annotation).getQualifiedName().toString() : 
                        annotation.getSimpleName().toString();
            builder.append("@");                // NOI18N
            builder.append(annotationName);     // NOI18N
            builder.append(", ");               // NOI18N
        }
        String bindingsText ;
        if ( builder.length() >0 ){
            bindingsText  = builder.substring(0 , builder.length() -2 );
        }
        else {
            // this should never happens actually.
            bindingsText = "";
        }
        myBindings.setText( bindingsText );
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javaHierarchyModeButtonGroup = new javax.swing.ButtonGroup();
        mySplitPane = new javax.swing.JSplitPane();
        javaHierarchyTreeScrollPane = new javax.swing.JScrollPane();
        javaHierarchyTree = new javax.swing.JTree();
        filterLabel = new javax.swing.JLabel();
        filterTextField = new javax.swing.JTextField();
        caseSensitiveFilterCheckBox = new javax.swing.JCheckBox();
        filtersLabel = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        filtersToolbar = new NoBorderToolBar();
        showFQNToggleButton = new javax.swing.JToggleButton();
        expandAllButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        myBindings = new javax.swing.JEditorPane();
        myBindingLbl = new javax.swing.JLabel();
        myType = new javax.swing.JEditorPane();
        myTypeLbl = new javax.swing.JLabel();
        myInjectableBindings = new javax.swing.JEditorPane();
        jLabel1 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        mySplitPane.setDividerLocation(300);

        javaHierarchyTreeScrollPane.setBorder(null);
        javaHierarchyTreeScrollPane.setViewportView(javaHierarchyTree);
        javaHierarchyTree.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSD_InjectableHierarchy")); // NOI18N

        mySplitPane.setLeftComponent(javaHierarchyTreeScrollPane);

        filterLabel.setLabelFor(filterTextField);
        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getBundle(AmbiguousInjectablesPanel.class).getString("LABEL_filterLabel")); // NOI18N

        filterTextField.setToolTipText(org.openide.util.NbBundle.getBundle(AmbiguousInjectablesPanel.class).getString("TOOLTIP_filterTextField")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(caseSensitiveFilterCheckBox, org.openide.util.NbBundle.getBundle(AmbiguousInjectablesPanel.class).getString("LABEL_caseSensitiveFilterCheckBox")); // NOI18N
        caseSensitiveFilterCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(filtersLabel, org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "LABEL_filtersLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(closeButton, org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "LABEL_Close")); // NOI18N

        filtersToolbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        filtersToolbar.setFloatable(false);
        filtersToolbar.setBorderPainted(false);
        filtersToolbar.setOpaque(false);

        showFQNToggleButton.setIcon(FQN_ICON);
        showFQNToggleButton.setMnemonic('Q');
        showFQNToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(AmbiguousInjectablesPanel.class).getString("TOOLTIP_showFQNToggleButton")); // NOI18N
        showFQNToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showFQNToggleButton);

        expandAllButton.setIcon(EXPAND_ALL_ICON);
        expandAllButton.setMnemonic('E');
        expandAllButton.setToolTipText(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "TOOLTIP_expandAll")); // NOI18N
        expandAllButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(expandAllButton);

        myBindings.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Nb.ScrollPane.Border.color")));
        myBindings.setContentType("text/x-java");
        myBindings.setEditable(false);

        myBindingLbl.setLabelFor(myBindings);
        org.openide.awt.Mnemonics.setLocalizedText(myBindingLbl, org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "LBL_Bindings")); // NOI18N

        myType.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Nb.ScrollPane.Border.color")));
        myType.setContentType("text/x-java");
        myType.setEditable(false);

        myTypeLbl.setLabelFor(myType);
        org.openide.awt.Mnemonics.setLocalizedText(myTypeLbl, org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "LBL_Type")); // NOI18N

        myInjectableBindings.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Nb.ScrollPane.Border.color")));
        myInjectableBindings.setContentType("text/x-java");
        myInjectableBindings.setEditable(false);

        jLabel1.setLabelFor(myInjectableBindings);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "LBL_CurrentElementBindings")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(mySplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(filterLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(caseSensitiveFilterCheckBox))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(myBindingLbl)
                            .add(myTypeLbl))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(myType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
                            .add(myBindings, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myInjectableBindings, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(filtersLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filtersToolbar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myTypeLbl)
                    .add(myType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myBindings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myBindingLbl))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(filterLabel)
                    .add(filterTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(caseSensitiveFilterCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mySplitPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 175, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(myInjectableBindings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(filtersLabel)
                        .add(closeButton))
                    .add(filtersToolbar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        filterLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSN_TextFilter")); // NOI18N
        filterLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSD_TextFilter")); // NOI18N
        filterTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSD_TextFieldFilter")); // NOI18N
        caseSensitiveFilterCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSN_CaseSensitive")); // NOI18N
        caseSensitiveFilterCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "caseSensitiveFilterCheckBox_ACSD")); // NOI18N
        filtersLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSN_Filters")); // NOI18N
        filtersLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSD_Filters")); // NOI18N
        closeButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSN_Close")); // NOI18N
        closeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSD_Close")); // NOI18N
        myBindingLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSN_Bindings")); // NOI18N
        myBindingLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSD_Bindnigs")); // NOI18N
        myTypeLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSN_Type")); // NOI18N
        myTypeLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSD_Type")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSN_InjectableBindings")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AmbiguousInjectablesPanel.class, "ACSD_InjectableBindnigs")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JCheckBox caseSensitiveFilterCheckBox;
    public javax.swing.JButton closeButton;
    public javax.swing.JButton expandAllButton;
    public javax.swing.JLabel filterLabel;
    public javax.swing.JTextField filterTextField;
    public javax.swing.JLabel filtersLabel;
    public javax.swing.JToolBar filtersToolbar;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JSeparator jSeparator1;
    public javax.swing.ButtonGroup javaHierarchyModeButtonGroup;
    public javax.swing.JTree javaHierarchyTree;
    public javax.swing.JScrollPane javaHierarchyTreeScrollPane;
    public javax.swing.JLabel myBindingLbl;
    public javax.swing.JEditorPane myBindings;
    public javax.swing.JEditorPane myInjectableBindings;
    public javax.swing.JSplitPane mySplitPane;
    public javax.swing.JEditorPane myType;
    public javax.swing.JLabel myTypeLbl;
    public javax.swing.JToggleButton showFQNToggleButton;
    // End of variables declaration//GEN-END:variables
}

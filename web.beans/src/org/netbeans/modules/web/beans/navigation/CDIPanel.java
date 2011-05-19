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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JTree;
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
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.beans.api.model.CdiException;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult.ResolutionResult;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.navigation.actions.WebBeansActionHelper;
import org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy.InspectActionId;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Based on org.netbeans.modules.java.navigation.JavaHierarchyPanel
 *
 * @author ads
 *
 */
abstract class CDIPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 9033410521614864413L;

    public static final Icon FQN_ICON = ImageUtilities.loadImageIcon(
            "org/netbeans/modules/java/navigation/resources/fqn.gif", false);       // NOI18N

    public static final Icon EXPAND_ALL_ICON = ImageUtilities.loadImageIcon(
            "org/netbeans/modules/java/navigation/resources/expandall.gif", false); // NOI18N
    
    private static TreeModel pleaseWaitTreeModel;
    static
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        root.add(new DefaultMutableTreeNode(NbBundle.getMessage(
                CDIPanel.class, "LBL_WaitNode"))); // NOI18N
        pleaseWaitTreeModel = new DefaultTreeModel(root);
    }
    
    CDIPanel(JavaHierarchyModel treeModel ) {
        initComponents();
        myJavaHierarchyModel = treeModel;
        
        // disable filtering for now: list of injectables will be always short
        mySeparator.setVisible(false);
        myFilterLabel.setVisible(false);
        myFilterTextField.setVisible(false);
        myCaseSensitiveFilterCheckBox.setVisible(false);
        
        myDocPane = new DocumentationScrollPane( true );
        mySplitPane.setRightComponent( myDocPane );
        mySplitPane.setDividerLocation(
                WebBeansNavigationOptions.getHierarchyDividerLocation());
        
        ToolTipManager.sharedInstance().registerComponent(myJavaHierarchyTree);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        myCaseSensitiveFilterCheckBox.setSelected(
                WebBeansNavigationOptions.isCaseSensitive());
        myShowFQNToggleButton.setSelected(
                WebBeansNavigationOptions.isShowFQN());

        myJavaHierarchyTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        myJavaHierarchyTree.setRootVisible(false);
        myJavaHierarchyTree.setShowsRootHandles(true);
        myJavaHierarchyTree.setCellRenderer(new JavaTreeCellRenderer());

        myJavaHierarchyTree.setModel(treeModel);

        registerKeyboardAction(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        close();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        initListeners();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                reload();
                myFilterTextField.requestFocusInWindow();
            }           
        });
    }

    @Override
    public void removeNotify() {
        WebBeansNavigationOptions.setHierarchyDividerLocation(
                mySplitPane.getDividerLocation());
        myDocPane.setData( null );
        super.removeNotify();
    }
    
    protected abstract void showSelectedCDI();

    protected abstract void reloadSubjectElement();
    
    // Hack to allow showing of Help window when F1 or HELP key is pressed.
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, 
            boolean pressed) 
    {
        if (e.getKeyCode() == KeyEvent.VK_F1 || e.getKeyCode() == KeyEvent.VK_HELP)  {
            JComponent rootPane = SwingUtilities.getRootPane(this);
            if (rootPane != null) {
                rootPane.putClientProperty(ResizablePopup.HELP_COOKIE, Boolean.TRUE); 
            }
        }
        return super.processKeyBinding(ks, e, condition, pressed);
    }
    
    protected JLabel getSubjectElementLabel(){
        return mySubjectElementbl;
    }
    
    protected JEditorPane getInitialElement(){
        return mySubjectElement;
    }
    
    protected JLabel getSubjectBindingsLabel(){
        return myBindingLbl;
    }
    
    protected JLabel getSelectedBindingsLbl(){
        return mySelectedBindingLbl;
    }
    
    protected JEditorPane getSelectedBindingsComponent(){
        return mySelectedBindings;
    }
    
    protected JTree getJavaTree(){
        return myJavaHierarchyTree;
    }
    
    protected boolean showFqns(){
        return myShowFQNToggleButton.isSelected();
    }
    
    protected JEditorPane getScopeComponent(){
        return myScope;
    }
    
    protected JLabel getScopeLabel() {
        return myScopeLabel;
    }
    
    protected JEditorPane getStereotypesComponent(){
        return myStereotypes;
    }
    
    protected JLabel getStereotypeLabel() {
        return myStereotypesLbl;
    }
    
    protected JEditorPane getInitialBindingsComponent(){
        return myBindings;
    }
    
    protected JLabel getStereotypesLabel(){
        return myStereotypesLbl;
    }
    
    protected JLabel getSelectedBindingsLabel(){
        return mySelectedBindingLbl;
    }
    
    private void enterBusy() {
        myJavaHierarchyTree.setModel(pleaseWaitTreeModel);
        JRootPane rootPane = SwingUtilities.getRootPane(CDIPanel.this);
        if (rootPane != null) {
            rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            myLastFocusedComponent = window.getFocusOwner();
        }
        myFilterTextField.setEnabled(false);
        myCaseSensitiveFilterCheckBox.setEnabled(false);
        myShowFQNToggleButton.setEnabled(false);
        myExpandAllButton.setEnabled(false);
    }
    
    private void leaveBusy() {
        myJavaHierarchyTree.setModel(myJavaHierarchyModel);
        JRootPane rootPane = SwingUtilities.getRootPane(CDIPanel.this);
        if (rootPane != null) {
            rootPane.setCursor(Cursor.getDefaultCursor());
        }
        myFilterTextField.setEnabled(true);
        myCaseSensitiveFilterCheckBox.setEnabled(true);
        myShowFQNToggleButton.setEnabled(true);
        myExpandAllButton.setEnabled(true);
        if (myLastFocusedComponent != null) {
            if (myLastFocusedComponent.isDisplayable()) {
                myLastFocusedComponent.requestFocusInWindow();
            }
            myLastFocusedComponent = null;
        }
    }
    
    private void reload() {
        enterBusy();

        WebBeansNavigationOptions.setCaseSensitive(myCaseSensitiveFilterCheckBox.isSelected());
        WebBeansNavigationOptions.setShowFQN(myShowFQNToggleButton.isSelected());

        RequestProcessor.getDefault().post(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        myJavaHierarchyModel.update();
                    } finally {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                leaveBusy();
                                // expand the tree
                                for (int row = 0; 
                                    row < myJavaHierarchyTree.getRowCount(); row++) 
                                {
                                    myJavaHierarchyTree.expandRow(row);
                                }
                            }});
                    }
                }
            });
    }

    private void expandAll() {
        SwingUtilities.invokeLater(
                new Runnable() {
            @Override
            public void run() {
                JRootPane rootPane = SwingUtilities.getRootPane(CDIPanel.this);
                if (rootPane != null) {
                    rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                }
            }
        }
        );

        SwingUtilities.invokeLater(
                new Runnable() {
            @Override
            public void run() {
                try {
                    // expand the tree
                    for (int row = 0; row < myJavaHierarchyTree.getRowCount(); row++) {
                        myJavaHierarchyTree.expandRow(row);
                    }
                    selectMatchingRow();
                } finally {
                    JRootPane rootPane = SwingUtilities.getRootPane(CDIPanel.this);
                    if (rootPane != null) {
                        rootPane.setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        }
        );
    }

    private void selectMatchingRow() {
        myFilterTextField.setForeground(UIManager.getColor("TextField.foreground"));
        myJavaHierarchyTree.setSelectionRow(-1);
        // select first matching
        for (int row = 0; row < myJavaHierarchyTree.getRowCount(); row++) {
            Object o = myJavaHierarchyTree.getPathForRow(row).getLastPathComponent();
            if (o instanceof JavaElement) {
                String filterText = myFilterTextField.getText();
                if (Utils.patternMatch((JavaElement)o, filterText, 
                        filterText.toLowerCase())) 
                {
                    myJavaHierarchyTree.setSelectionRow(row);
                    myJavaHierarchyTree.scrollRowToVisible(row);
                    return;
                }
            }
        }
        myFilterTextField.setForeground(Color.RED);
    }

    private void gotoElement(JavaElement javaToolsJavaElement) {
        try {
            javaToolsJavaElement.gotoElement();
        } finally {
            close();
        }
    }

    private void showJavaDoc() {
        TreePath treePath = myJavaHierarchyTree.getSelectionPath();
        if (treePath != null) {
            Object node = treePath.getLastPathComponent();
            if (node instanceof JavaElement) {
                myDocPane.setData( ((JavaElement)node).getJavaDoc() );
            }
        }
    }

    private void close() {
        Window window = SwingUtilities.getWindowAncestor(CDIPanel.this);
        if (window != null) {
            window.setVisible(false);
        }
    }
    
    private void initListeners() {
        myFilterTextField.getDocument().addDocumentListener(
                new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                selectMatchingRow();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                selectMatchingRow();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                selectMatchingRow();
            }
        }
        );
        
        registerKeyboardActions();

        myCaseSensitiveFilterCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                WebBeansNavigationOptions.setCaseSensitive(
                        myCaseSensitiveFilterCheckBox.isSelected());
                if (myFilterTextField.getText().trim().length() > 0) {
                    // apply filters again only if there is some filter text
                    selectMatchingRow();
                }
            }
        });

        myJavaHierarchyTree.addMouseListener(
                new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                Point point = me.getPoint();
                TreePath treePath = myJavaHierarchyTree.
                    getPathForLocation(point.x, point.y);
                if (treePath != null) {
                    Object node = treePath.getLastPathComponent();
                    if (node instanceof JavaElement) {
                        if (me.getClickCount() == 2){
                            gotoElement((JavaElement) node);
                        }
                    }
                }
            }
        }
        );

        myJavaHierarchyTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                showSelectedCDI();
                showJavaDoc();
            }
        });

        myShowFQNToggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                WebBeansNavigationOptions.setShowFQN(myShowFQNToggleButton.isSelected());
                myJavaHierarchyModel.fireTreeNodesChanged();
                reloadSubjectElement();
                showSelectedCDI();
            }
        });

        myExpandAllButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        expandAll();
                    }
                });

        myCloseButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        close();
                    }
                });
    }
    private void registerKeyboardActions() {
        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.firstRow(myJavaHierarchyTree);
            }
        };

        myFilterTextField.registerKeyboardAction( listener,
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, false),
                JComponent.WHEN_FOCUSED);
        
        myBindings.registerKeyboardAction(listener,
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, false),
                JComponent.WHEN_FOCUSED);

        listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.previousRow(myJavaHierarchyTree);
            }
        };
        myFilterTextField.registerKeyboardAction(listener,
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
                JComponent.WHEN_FOCUSED);
        
        myBindings.registerKeyboardAction( listener ,
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
                JComponent.WHEN_FOCUSED);

        listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.nextRow(myJavaHierarchyTree);
            }
        };
        myFilterTextField.registerKeyboardAction(listener,
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false),
                JComponent.WHEN_FOCUSED);
        
        myBindings.registerKeyboardAction(listener,
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false),
                JComponent.WHEN_FOCUSED);

        listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Utils.lastRow(myJavaHierarchyTree);
            }
        };
        myFilterTextField.registerKeyboardAction(listener,
                KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, false),
                JComponent.WHEN_FOCUSED);
        
        myBindings.registerKeyboardAction(listener,
                KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, false),
                JComponent.WHEN_FOCUSED);

        myBindings.putClientProperty(
            "HighlightsLayerExcludes", // NOI18N
            "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" // NOI18N
        );

        myFilterTextField.registerKeyboardAction(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath treePath = myJavaHierarchyTree.getSelectionPath();
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

        myFilterTextField.registerKeyboardAction(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        Component view = myDocPane.getViewport().getView();
                        if (view instanceof JEditorPane) {
                            JEditorPane editorPane = (JEditorPane) view;
                            ActionListener actionForKeyStroke =
                                editorPane.getActionForKeyStroke(
                                        KeyStroke.getKeyStroke(
                                                KeyEvent.VK_PAGE_UP, 0, false));                            
                            actionForKeyStroke.actionPerformed(
                                    new ActionEvent(editorPane, 
                                            ActionEvent.ACTION_PERFORMED, ""));
                        }
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 
                        KeyEvent.SHIFT_MASK, false),
                JComponent.WHEN_FOCUSED);
        myFilterTextField.registerKeyboardAction(
                new ActionListener() {
                    private boolean firstTime = true;
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        Component view = myDocPane.getViewport().getView();
                        if (view instanceof JEditorPane) {
                            JEditorPane editorPane = (JEditorPane) view;
                            ActionListener actionForKeyStroke =
                                editorPane.getActionForKeyStroke(
                                        KeyStroke.getKeyStroke(
                                                KeyEvent.VK_PAGE_DOWN, 0, false));
                            actionEvent = new ActionEvent(editorPane, 
                                    ActionEvent.ACTION_PERFORMED, "");
                            actionForKeyStroke.actionPerformed(actionEvent);
                            if (firstTime) {
                                actionForKeyStroke.actionPerformed(actionEvent);
                                firstTime = false;
                            }
                        }
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 
                        KeyEvent.SHIFT_MASK, false),
                JComponent.WHEN_FOCUSED);
        
        myJavaHierarchyTree.registerKeyboardAction(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath treePath = myJavaHierarchyTree.getLeadSelectionPath();
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

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mySplitPane = new javax.swing.JSplitPane();
        myJavaHierarchyTreeScrollPane = new javax.swing.JScrollPane();
        myJavaHierarchyTree = new javax.swing.JTree();
        myFilterLabel = new javax.swing.JLabel();
        myFilterTextField = new javax.swing.JTextField();
        myCaseSensitiveFilterCheckBox = new javax.swing.JCheckBox();
        myFiltersLabel = new javax.swing.JLabel();
        myCloseButton = new javax.swing.JButton();
        myFiltersToolbar = new NoBorderToolBar();
        myShowFQNToggleButton = new javax.swing.JToggleButton();
        myExpandAllButton = new javax.swing.JButton();
        mySeparator = new javax.swing.JSeparator();
        myBindings = new javax.swing.JEditorPane();
        myBindingLbl = new javax.swing.JLabel();
        mySubjectElement = new javax.swing.JEditorPane();
        mySubjectElementbl = new javax.swing.JLabel();
        mySelectedBindings = new javax.swing.JEditorPane();
        mySelectedBindingLbl = new javax.swing.JLabel();
        myScopeLabel = new javax.swing.JLabel();
        myScope = new javax.swing.JEditorPane();
        myStereotypesLbl = new javax.swing.JLabel();
        myStereotypes = new javax.swing.JEditorPane();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        mySplitPane.setDividerLocation(300);

        myJavaHierarchyTreeScrollPane.setBorder(null);
        myJavaHierarchyTreeScrollPane.setViewportView(myJavaHierarchyTree);
        myJavaHierarchyTree.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSD_InjectableHierarchy")); // NOI18N

        mySplitPane.setLeftComponent(myJavaHierarchyTreeScrollPane);

        myFilterLabel.setLabelFor(myFilterTextField);
        org.openide.awt.Mnemonics.setLocalizedText(myFilterLabel, org.openide.util.NbBundle.getBundle(CDIPanel.class).getString("LABEL_filterLabel")); // NOI18N

        myFilterTextField.setToolTipText(org.openide.util.NbBundle.getBundle(CDIPanel.class).getString("TOOLTIP_filterTextField")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myCaseSensitiveFilterCheckBox, org.openide.util.NbBundle.getBundle(CDIPanel.class).getString("LABEL_caseSensitiveFilterCheckBox")); // NOI18N
        myCaseSensitiveFilterCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(myFiltersLabel, org.openide.util.NbBundle.getMessage(CDIPanel.class, "LABEL_filtersLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myCloseButton, org.openide.util.NbBundle.getMessage(CDIPanel.class, "LABEL_Close")); // NOI18N

        myFiltersToolbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        myFiltersToolbar.setFloatable(false);
        myFiltersToolbar.setBorderPainted(false);
        myFiltersToolbar.setOpaque(false);

        myShowFQNToggleButton.setIcon(FQN_ICON);
        myShowFQNToggleButton.setMnemonic('Q');
        myShowFQNToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(CDIPanel.class).getString("TOOLTIP_showFQNToggleButton")); // NOI18N
        myShowFQNToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        myFiltersToolbar.add(myShowFQNToggleButton);

        myExpandAllButton.setIcon(EXPAND_ALL_ICON);
        myExpandAllButton.setMnemonic('E');
        myExpandAllButton.setToolTipText(org.openide.util.NbBundle.getMessage(CDIPanel.class, "TOOLTIP_expandAll")); // NOI18N
        myExpandAllButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        myFiltersToolbar.add(myExpandAllButton);

        myBindings.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Nb.ScrollPane.Border.color")));
        myBindings.setContentType("text/x-java");
        myBindings.setEditable(false);

        myBindingLbl.setLabelFor(myBindings);
        org.openide.awt.Mnemonics.setLocalizedText(myBindingLbl, org.openide.util.NbBundle.getMessage(CDIPanel.class, "LBL_Bindings")); // NOI18N

        mySubjectElement.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Nb.ScrollPane.Border.color")));
        mySubjectElement.setContentType("text/x-java");
        mySubjectElement.setEditable(false);

        mySubjectElementbl.setLabelFor(mySubjectElement);
        org.openide.awt.Mnemonics.setLocalizedText(mySubjectElementbl, org.openide.util.NbBundle.getMessage(CDIPanel.class, "LBL_Type")); // NOI18N

        mySelectedBindings.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Nb.ScrollPane.Border.color")));
        mySelectedBindings.setContentType("text/x-java");
        mySelectedBindings.setEditable(false);

        mySelectedBindingLbl.setLabelFor(mySelectedBindings);
        org.openide.awt.Mnemonics.setLocalizedText(mySelectedBindingLbl, org.openide.util.NbBundle.getMessage(CDIPanel.class, "LBL_CurrentElementBindings")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myScopeLabel, org.openide.util.NbBundle.getMessage(CDIPanel.class, "LBL_Scope")); // NOI18N

        myScope.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Nb.ScrollPane.Border.color")));
        myScope.setContentType("text/x-java");
        myScope.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(myStereotypesLbl, org.openide.util.NbBundle.getMessage(CDIPanel.class, "LBL_Stereotypes")); // NOI18N

        myStereotypes.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Nb.ScrollPane.Border.color")));
        myStereotypes.setContentType("text/x-java");
        myStereotypes.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mySplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 643, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(myFilterLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myFilterTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myCaseSensitiveFilterCheckBox))
                    .add(mySeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 643, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(myBindingLbl)
                            .add(mySubjectElementbl))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mySubjectElement, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
                            .add(myBindings, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mySelectedBindingLbl)
                            .add(myScopeLabel)
                            .add(myStereotypesLbl)
                            .add(myFiltersLabel))
                        .add(28, 28, 28)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(myFiltersToolbar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(myCloseButton))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, mySelectedBindings, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
                            .add(myScope, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
                            .add(myStereotypes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mySubjectElementbl)
                    .add(mySubjectElement, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myBindings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myBindingLbl))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mySeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myFilterLabel)
                    .add(myFilterTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myCaseSensitiveFilterCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mySplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mySelectedBindingLbl)
                    .add(mySelectedBindings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myScopeLabel)
                    .add(myScope, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(myStereotypes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(myCloseButton))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(layout.createSequentialGroup()
                            .add(myStereotypesLbl)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                            .add(myFiltersLabel)
                            .add(6, 6, 6))
                        .add(myFiltersToolbar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        myFilterLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSN_TextFilter")); // NOI18N
        myFilterLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSD_TextFilter")); // NOI18N
        myFilterTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSD_TextFieldFilter")); // NOI18N
        myCaseSensitiveFilterCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSN_CaseSensitive")); // NOI18N
        myCaseSensitiveFilterCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CDIPanel.class, "caseSensitiveFilterCheckBox_ACSD")); // NOI18N
        myFiltersLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSN_Filters")); // NOI18N
        myFiltersLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSD_Filters")); // NOI18N
        myCloseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSN_Close")); // NOI18N
        myCloseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSD_Close")); // NOI18N
        myBindingLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSN_Bindings")); // NOI18N
        myBindingLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSD_Bindnigs")); // NOI18N
        mySubjectElementbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSN_Type")); // NOI18N
        mySubjectElementbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSD_Type")); // NOI18N
        mySelectedBindingLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSN_InjectableBindings")); // NOI18N
        mySelectedBindingLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSD_InjectableBindnigs")); // NOI18N
        myScopeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSN_Scope")); // NOI18N
        myScopeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSD_Scope")); // NOI18N
        myScope.getAccessibleContext().setAccessibleName(myScopeLabel.getAccessibleContext().getAccessibleName());
        myScope.getAccessibleContext().setAccessibleDescription(myScopeLabel.getAccessibleContext().getAccessibleDescription());
        myStereotypesLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSN_Stereotypes")); // NOI18N
        myStereotypesLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CDIPanel.class, "ACSD_Stereotypes")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel myBindingLbl;
    private javax.swing.JEditorPane myBindings;
    private javax.swing.JCheckBox myCaseSensitiveFilterCheckBox;
    private javax.swing.JButton myCloseButton;
    private javax.swing.JButton myExpandAllButton;
    private javax.swing.JLabel myFilterLabel;
    private javax.swing.JTextField myFilterTextField;
    private javax.swing.JLabel myFiltersLabel;
    private javax.swing.JToolBar myFiltersToolbar;
    private javax.swing.JTree myJavaHierarchyTree;
    private javax.swing.JScrollPane myJavaHierarchyTreeScrollPane;
    private javax.swing.JEditorPane myScope;
    private javax.swing.JLabel myScopeLabel;
    private javax.swing.JLabel mySelectedBindingLbl;
    private javax.swing.JEditorPane mySelectedBindings;
    private javax.swing.JSeparator mySeparator;
    private javax.swing.JToggleButton myShowFQNToggleButton;
    private javax.swing.JSplitPane mySplitPane;
    private javax.swing.JEditorPane myStereotypes;
    private javax.swing.JLabel myStereotypesLbl;
    private javax.swing.JEditorPane mySubjectElement;
    private javax.swing.JLabel mySubjectElementbl;
    // End of variables declaration//GEN-END:variables
    
    private Component myLastFocusedComponent;
    private DocumentationScrollPane myDocPane;
    
    private JavaHierarchyModel myJavaHierarchyModel;
}

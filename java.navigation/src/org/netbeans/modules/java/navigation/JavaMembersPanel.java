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
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
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
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
public class JavaMembersPanel extends javax.swing.JPanel {

    private static final RequestProcessor RP = new RequestProcessor(JavaMembersPanel.class.getName(),1);

    private static TreeModel pleaseWaitTreeModel;
    static
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        root.add(new DefaultMutableTreeNode(NbBundle.getMessage(JavaMembersPanel.class, "LBL_WaitNode"))); // NOI18N
        pleaseWaitTreeModel = new DefaultTreeModel(root);
    }

    private volatile JavaMembersModel javaMembersModel;
    private volatile JavaMembersModel.FilterModel javaMembersFilterModel;

    /**
     *
     * @param fileObject
     * @param elements
     * @param compilationInfo
     */
    public JavaMembersPanel(final FileObject fileObject, final ElementHandle<?>[] elements) {
        this();
        javaMembersModel = new JavaMembersModel(fileObject, elements);
        javaMembersFilterModel = javaMembersModel.getFilterModel();
        enterBusy();
        RP.post(new Runnable() {
            public void run() {
                try {
                    javaMembersModel.update();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run () {
                            javaMembersTree.setModel(javaMembersFilterModel);
                        }
                    });
                } finally {
                    SwingUtilities.invokeLater(new Runnable(){
                        public void run() {
                            leaveBusy();
                        }
                    });
                }
            }
        });
        registerActions();
    }

    JavaMembersPanel(final FileObject fileObject) {
        this();
        enterBusy();
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    JavaSource javaSource = JavaSource.forFileObject(fileObject);
                    if (javaSource != null) {
                        javaSource.runUserActionTask(new Task<CompilationController>() {
                            @Override
                            public void run(CompilationController compilationController) throws Exception {
                                compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                                final List<? extends TypeElement> topLevels = compilationController.getTopLevelElements();
                                final Set<Element> elementsSet = new LinkedHashSet<Element>(topLevels.size() + 1);
                                for (TypeElement element : topLevels) {                                    
                                    if (elementsSet.isEmpty()) {
                                        final Element enclosingElement = element.getEnclosingElement();
                                        if (enclosingElement != null &&
                                            enclosingElement.getKind() == ElementKind.PACKAGE) {
                                            // add package
                                            elementsSet.add(enclosingElement);
                                        }
                                    }
                                    elementsSet.add(element);                                    
                                }
                                ElementHandle[] handles = new ElementHandle[elementsSet.size()];
                                Iterator<Element> elements = elementsSet.iterator();
                                for (int i=0; i<handles.length; i++) {
                                    handles[i] = ElementHandle.create(elements.next());
                                }
                                javaMembersModel = new JavaMembersModel(fileObject, handles);
                                javaMembersFilterModel = javaMembersModel.getFilterModel();
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run () {
                                        javaMembersTree.setModel(javaMembersFilterModel);
                                    }
                                });
                                
                            }
                        }, true);
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
                finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            leaveBusy();
                        }
                    });
                }
            }
        });
        
        registerActions();
    }

    private JavaMembersPanel() {
        initComponents();
        docPane = new DocumentationScrollPane( true );
        splitPane.setRightComponent( docPane );
        splitPane.setDividerLocation(JavaMembersAndHierarchyOptions.getMembersDividerLocation());

        ToolTipManager.sharedInstance().registerComponent(javaMembersTree);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        caseSensitiveFilterCheckBox.setSelected(JavaMembersAndHierarchyOptions.isCaseSensitive());
        showInheritedToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowInherited());
        showFQNToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowFQN());
        showInnerToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowInner());
        showConstructorsToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowConstructors());
        showMethodsToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowMethods());
        showFieldsToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowFields());
        showEnumConstantsToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowEnumConstants());
        showProtectedToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowProtected());
        showPackageToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowPackage());
        showPrivateToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowPrivate());
        showStaticToggleButton.setSelected(JavaMembersAndHierarchyOptions.isShowStatic());

        javaMembersTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        javaMembersTree.setRootVisible(false);
        javaMembersTree.setShowsRootHandles(true);
        javaMembersTree.setCellRenderer(new JavaTreeCellRenderer());
    }



    //<editor-fold defaultstate="collapsed" desc="Action Registration">
    private void registerActions() {
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
                        applyFilter();
                    }
                    public void insertUpdate(DocumentEvent e) {
                        applyFilter();
                    }
                    public void removeUpdate(DocumentEvent e) {
                        applyFilter();
                    }
                });

        filterTextField.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Utils.firstRow(javaMembersTree);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, false),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Utils.previousRow(javaMembersTree);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Utils.nextRow(javaMembersTree);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Utils.lastRow(javaMembersTree);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, false),
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

        signatureEditorPane.putClientProperty(
            "HighlightsLayerExcludes", // NOI18N
            "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" // NOI18N
        );

        signatureEditorPane.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Utils.firstRow(javaMembersTree);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, true),
                JComponent.WHEN_FOCUSED);

        signatureEditorPane.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Utils.previousRow(javaMembersTree);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true),
                JComponent.WHEN_FOCUSED);

        signatureEditorPane.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Utils.nextRow(javaMembersTree);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true),
                JComponent.WHEN_FOCUSED);

        signatureEditorPane.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Utils.lastRow(javaMembersTree);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, true),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        TreePath treePath = javaMembersTree.getSelectionPath();
                        if (treePath != null) {
                            Object node = treePath.getLastPathComponent();
                            if (node instanceof JavaElement) {
                                JavaElement javaToolsJavaElement = (JavaElement) node;
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
                        TreePath treePath = javaMembersTree.getSelectionPath();
                        if (treePath != null) {
                            Object node = treePath.getLastPathComponent();
                            if (node instanceof JavaElement) {
                                gotoElement((JavaElement) node);
                            }
                        }
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), JComponent.WHEN_FOCUSED);

        caseSensitiveFilterCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (filterTextField.getText().trim().length() > 0) {
                    // apply filters again only if there is some filter text
                    applyFilter();
                }
            }
        });

        javaMembersTree.addMouseListener(
                new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                Point point = me.getPoint();
                TreePath treePath = javaMembersTree.getPathForLocation(point.x, point.y);
                if (treePath != null) {
                    Object node = treePath.getLastPathComponent();
                    if (node instanceof JavaElement) {
                        if (me.getClickCount() == 1) {
                            if (me.isControlDown()) {
                                filterTextField.setText("");
                                JavaElement javaToolsJavaElement = (JavaElement) node;
                                applyFilter();
                            }
                        } else  if (me.getClickCount() == 2) {
                            gotoElement((JavaElement) node);
                        }
                    }
                }

            }
        }
        );

        javaMembersTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                showSignature();
                showJavaDoc();
            }
        });

        javaMembersTree.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        TreePath treePath = javaMembersTree.getLeadSelectionPath();
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

        javaMembersTree.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        TreePath treePath = javaMembersTree.getLeadSelectionPath();
                        if (treePath != null) {
                            Object node = treePath.getLastPathComponent();
                            if (node instanceof JavaElement) {
                                filterTextField.setText("");
                                applyFilter();
                            }
                        }
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true),
                JComponent.WHEN_FOCUSED);

        showInheritedToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyFilter(true);
            }
        });

        showFQNToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                JavaMembersAndHierarchyOptions.setShowFQN(showFQNToggleButton.isSelected());
                javaMembersFilterModel.fireTreeNodesChanged();
            }
        });

        showInnerToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyFilter(true);
            }
        });

        showConstructorsToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyFilter();
            }
        });

        showMethodsToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyFilter();
            }
        });

        showFieldsToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyFilter();
            }
        });

        showEnumConstantsToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyFilter();
            }
        });

        showProtectedToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyFilter();
            }
        });

        showPackageToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyFilter();
            }
        });

        showPrivateToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyFilter();
            }
        });

        showStaticToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyFilter();
            }
        });

        closeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        close();
                    }
                });
    }
    //</editor-fold>

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
        JavaMembersAndHierarchyOptions.setMembersDividerLocation(splitPane.getDividerLocation());
        docPane.setData(null);
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
        javaMembersTree.setModel(pleaseWaitTreeModel);
        JRootPane rootPane = SwingUtilities.getRootPane(JavaMembersPanel.this);
        if (rootPane != null) {
            rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            lastFocusedComponent = window.getFocusOwner();
        }
        filterTextField.setEnabled(false);  
        caseSensitiveFilterCheckBox.setEnabled(false);
        showInheritedToggleButton.setEnabled(false);
        showFQNToggleButton.setEnabled(false);
        showInnerToggleButton.setEnabled(false);
        showConstructorsToggleButton.setEnabled(false);
        showMethodsToggleButton.setEnabled(false);
        showFieldsToggleButton.setEnabled(false);
        showEnumConstantsToggleButton.setEnabled(false);
        showProtectedToggleButton.setEnabled(false);
        showPackageToggleButton.setEnabled(false);
        showPrivateToggleButton.setEnabled(false);
        showStaticToggleButton.setEnabled(false);     
    }
    
    private void leaveBusy() {
        javaMembersTree.setModel(javaMembersFilterModel);
        JRootPane rootPane = SwingUtilities.getRootPane(JavaMembersPanel.this);
        if (rootPane != null) {
            rootPane.setCursor(Cursor.getDefaultCursor());
        }
        filterTextField.setEnabled(true);  
        caseSensitiveFilterCheckBox.setEnabled(true);
        showInheritedToggleButton.setEnabled(true);
        showFQNToggleButton.setEnabled(true);
        showInnerToggleButton.setEnabled(true);
        showConstructorsToggleButton.setEnabled(true);
        showMethodsToggleButton.setEnabled(true);
        showFieldsToggleButton.setEnabled(true);
        showEnumConstantsToggleButton.setEnabled(true);
        showProtectedToggleButton.setEnabled(true);
        showPackageToggleButton.setEnabled(true);
        showPrivateToggleButton.setEnabled(true);
        showStaticToggleButton.setEnabled(true);
        if (lastFocusedComponent != null) {
            if (lastFocusedComponent.isDisplayable()) {
                lastFocusedComponent.requestFocusInWindow();
            }
            lastFocusedComponent = null;
        }
    }
       
    private void applyFilter() {
        applyFilter(false);
    }
    
    private void applyFilter(final boolean structural) {
        if (structural) {
            enterBusy();
        }

        final String pattern  = filterTextField.getText();
        
        JavaMembersAndHierarchyOptions.setCaseSensitive(caseSensitiveFilterCheckBox.isSelected());
        JavaMembersAndHierarchyOptions.setShowInherited(showInheritedToggleButton.isSelected());
        JavaMembersAndHierarchyOptions.setShowFQN(showFQNToggleButton.isSelected());
        JavaMembersAndHierarchyOptions.setShowInner(showInnerToggleButton.isSelected());
        JavaMembersAndHierarchyOptions.setShowConstructors(showConstructorsToggleButton.isSelected());
        JavaMembersAndHierarchyOptions.setShowMethods(showMethodsToggleButton.isSelected());
        JavaMembersAndHierarchyOptions.setShowFields(showFieldsToggleButton.isSelected());
        JavaMembersAndHierarchyOptions.setShowEnumConstants(showEnumConstantsToggleButton.isSelected());
        JavaMembersAndHierarchyOptions.setShowProtected(showProtectedToggleButton.isSelected());
        JavaMembersAndHierarchyOptions.setShowPackage(showPackageToggleButton.isSelected());
        JavaMembersAndHierarchyOptions.setShowPrivate(showPrivateToggleButton.isSelected());
        JavaMembersAndHierarchyOptions.setShowStatic(showStaticToggleButton.isSelected());
        
        // apply filters and update the tree
        RP.post(
            new Runnable() {
            public void run() {
                    javaMembersFilterModel.setPattern(pattern);
                    try {    
                        if (structural) {
                            javaMembersModel.update();
                        }
                        
                        javaMembersFilterModel.update();
                    } finally {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                if (structural) {
                                    leaveBusy();
                                }
                                // expand the tree
                                for (int row = 0; row < javaMembersTree.getRowCount(); row++) {                        
                                    javaMembersTree.expandRow(row);
                                }
                                
                                filterTextField.setForeground(UIManager.getColor("TextField.foreground"));
                                String filterText = filterTextField.getText();
                                if (filterText.trim().length() > 0) {
                                    // select first matching
                                    for (int row = 0; row < javaMembersTree.getRowCount(); row++) {
                                        Object o = javaMembersTree.getPathForRow(row).getLastPathComponent();
                                        if (o instanceof JavaElement) {
                                            JavaElement javaElement = (JavaElement) o;
                                            ElementKind elementKind = javaElement.getElementKind();
                                            if (
//                                                elementKind == ElementKind.CLASS ||
//                                                elementKind == ElementKind.INTERFACE||
//                                                elementKind == ElementKind.ENUM ||
//                                                elementKind == ElementKind.ANNOTATION_TYPE ||
                                                elementKind == ElementKind.PACKAGE) {
                                                    continue;
                                            }    
                                            if (JavaMembersModel.patternMatch((JavaElement)o, filterText)) {
                                                javaMembersTree.setSelectionRow(row);
                                                break;
                                            }
                                        }
                                    }
                                    if (javaMembersTree.getSelectionCount() == 0) {
                                        filterTextField.setForeground(Color.red);
                                    }
                                } else {
                                    // Try to select non-package node
                                    if (javaMembersTree.getRowCount() > 1){
                                        TreePath treePath = javaMembersTree.getPathForRow(0);
                                        if (treePath != null) {
                                            Object node = treePath.getLastPathComponent();
                                            if (node instanceof JavaElement) {
                                                JavaElement javaELement = (JavaElement) node;
                                                if (javaELement.getElementKind() == ElementKind.PACKAGE) {
                                                    // Select the next row
                                                    javaMembersTree.setSelectionRow(1);
                                                } else {
                                                    // Select the first row
                                                    javaMembersTree.setSelectionRow(0);
                                                }
                                            }
                                        }
                                    }
                                }
                            }                       
                         });
                    }                    
            }
        });
    }
    
    private void gotoElement(JavaElement javaElement) {
        try {
            javaElement.gotoElement();
        } finally {
            close();
        }
    }

    private void showSignature() {
        signatureEditorPane.setText("");
        signatureEditorPane.setToolTipText(null);
        TreePath treePath = javaMembersTree.getSelectionPath();
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
        TreePath treePath = javaMembersTree.getSelectionPath();
        if (treePath != null) {
            final Object node = treePath.getLastPathComponent();
            if (node instanceof JavaElement) {
                RP.post(new Runnable() {
                    public void run() {
                        final ElementJavadoc jdoc = ((JavaElement)node).getJavaDoc();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                docPane.setData(jdoc);
                            }
                        });
                    }
                });
            }
        }
    }

    private DocumentationScrollPane docPane;

    private void close() {
        Window window = SwingUtilities.getWindowAncestor(JavaMembersPanel.this);
        if (window != null) {
            ResizablePopup.cleanup(window);
        }
    }

    private void gotoClass(TypeElement javaClass) {
//        PositionBounds bounds = null;
//        if (javaClass.getResource() != null) {
//            bounds = JavaMetamodel.getManager().getElementPosition(javaClass);
//            if (bounds == null) {
//                ClassDefinition classDefinition = ((JMManager)JavaMetamodel.getManager()).getSourceElementIfExists(javaClass);
//                if (classDefinition != null) {
//                    javaClass = (JavaClass) classDefinition;
//                }
//            }
//        }
//        Resource resource = javaClass.getResource();
//        if (resource != null) {
//            JavaMembersModel = new JavaMembersModel(resource);
//        } else {
//            JavaMembersModel = new JavaMembersModel(javaClass);
//        }
//        javaMembersTree.setModel(JavaMembersModel);
//        applyFilter();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filterLabel = new javax.swing.JLabel();
        filterTextField = new javax.swing.JTextField();
        caseSensitiveFilterCheckBox = new javax.swing.JCheckBox();
        splitPane = new javax.swing.JSplitPane();
        javaMembersTreeScrollPane = new javax.swing.JScrollPane();
        javaMembersTree = new javax.swing.JTree();
        signatureEditorPane = new javax.swing.JEditorPane();
        filtersLabel = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        filtersToolbar = new NoBorderToolBar();
        showInheritedToggleButton = new javax.swing.JToggleButton();
        showFQNToggleButton = new javax.swing.JToggleButton();
        showInnerToggleButton = new javax.swing.JToggleButton();
        showConstructorsToggleButton = new javax.swing.JToggleButton();
        showMethodsToggleButton = new javax.swing.JToggleButton();
        showFieldsToggleButton = new javax.swing.JToggleButton();
        showEnumConstantsToggleButton = new javax.swing.JToggleButton();
        showProtectedToggleButton = new javax.swing.JToggleButton();
        showPackageToggleButton = new javax.swing.JToggleButton();
        showPrivateToggleButton = new javax.swing.JToggleButton();
        showStaticToggleButton = new javax.swing.JToggleButton();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        filterLabel.setLabelFor(filterTextField);
        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("LABEL_filterLabel")); // NOI18N

        filterTextField.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_filterTextField")); // NOI18N
        filterTextField.setNextFocusableComponent(javaMembersTree);

        org.openide.awt.Mnemonics.setLocalizedText(caseSensitiveFilterCheckBox, org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("LABEL_caseSensitiveFilterCheckBox")); // NOI18N
        caseSensitiveFilterCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        splitPane.setDividerLocation(350);
        splitPane.setOneTouchExpandable(true);

        javaMembersTreeScrollPane.setBorder(null);
        javaMembersTreeScrollPane.setViewportView(javaMembersTree);
        javaMembersTree.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JavaMembersPanel.class, "ACSN_JavaMembersTree")); // NOI18N
        javaMembersTree.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JavaMembersPanel.class, "ACSD_JavaMembersTree")); // NOI18N

        splitPane.setLeftComponent(javaMembersTreeScrollPane);

        signatureEditorPane.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Nb.ScrollPane.Border.color")));
        signatureEditorPane.setContentType("text/x-java");
        signatureEditorPane.setEditable(false);
        signatureEditorPane.setNextFocusableComponent(filtersToolbar);

        org.openide.awt.Mnemonics.setLocalizedText(filtersLabel, org.openide.util.NbBundle.getMessage(JavaMembersPanel.class, "LABEL_filtersLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(closeButton, org.openide.util.NbBundle.getMessage(JavaMembersPanel.class, "LABEL_Close")); // NOI18N

        filtersToolbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        filtersToolbar.setFloatable(false);
        filtersToolbar.setBorderPainted(false);
        filtersToolbar.setOpaque(false);

        showInheritedToggleButton.setIcon(JavaMembersAndHierarchyIcons.INHERITED_ICON);
        showInheritedToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showInheritedToggleButton")); // NOI18N
        filtersToolbar.add(showInheritedToggleButton);

        showFQNToggleButton.setIcon(JavaMembersAndHierarchyIcons.FQN_ICON);
        showFQNToggleButton.setSelected(true);
        showFQNToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showFQNToggleButton")); // NOI18N
        filtersToolbar.add(showFQNToggleButton);

        showInnerToggleButton.setIcon(JavaMembersAndHierarchyIcons.INNER_CLASS_ICON);
        showInnerToggleButton.setSelected(true);
        showInnerToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showInnerToggleButton")); // NOI18N
        filtersToolbar.add(showInnerToggleButton);

        showConstructorsToggleButton.setIcon(JavaMembersAndHierarchyIcons.CONSTRUCTOR_ICON);
        showConstructorsToggleButton.setSelected(true);
        showConstructorsToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showConstructorsToggleButton")); // NOI18N
        filtersToolbar.add(showConstructorsToggleButton);

        showMethodsToggleButton.setIcon(JavaMembersAndHierarchyIcons.METHOD_ICON);
        showMethodsToggleButton.setSelected(true);
        showMethodsToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showMethodsToggleButton")); // NOI18N
        filtersToolbar.add(showMethodsToggleButton);

        showFieldsToggleButton.setIcon(JavaMembersAndHierarchyIcons.FIELD_ICON);
        showFieldsToggleButton.setSelected(true);
        showFieldsToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showFieldsToggleButton")); // NOI18N
        filtersToolbar.add(showFieldsToggleButton);

        showEnumConstantsToggleButton.setIcon(JavaMembersAndHierarchyIcons.ENUM_CONSTANTS_ICON);
        showEnumConstantsToggleButton.setSelected(true);
        showEnumConstantsToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showEnumConstantsToggleButton")); // NOI18N
        filtersToolbar.add(showEnumConstantsToggleButton);

        showProtectedToggleButton.setIcon(JavaMembersAndHierarchyIcons.PROTECTED_ICON);
        showProtectedToggleButton.setSelected(true);
        showProtectedToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showProtectedToggleButton")); // NOI18N
        filtersToolbar.add(showProtectedToggleButton);

        showPackageToggleButton.setIcon(JavaMembersAndHierarchyIcons.PACKAGE_ICON);
        showPackageToggleButton.setSelected(true);
        showPackageToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showPackageToggleButton")); // NOI18N
        filtersToolbar.add(showPackageToggleButton);

        showPrivateToggleButton.setIcon(JavaMembersAndHierarchyIcons.PRIVATE_ICON);
        showPrivateToggleButton.setSelected(true);
        showPrivateToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showPrivateToggleButton")); // NOI18N
        filtersToolbar.add(showPrivateToggleButton);

        showStaticToggleButton.setIcon(JavaMembersAndHierarchyIcons.STATIC_ICON);
        showStaticToggleButton.setSelected(true);
        showStaticToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showStaticToggleButton")); // NOI18N
        filtersToolbar.add(showStaticToggleButton);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 763, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(filterLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filterTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(caseSensitiveFilterCheckBox))
                    .addComponent(signatureEditorPane, javax.swing.GroupLayout.DEFAULT_SIZE, 763, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(filtersLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filtersToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterLabel)
                    .addComponent(caseSensitiveFilterCheckBox)
                    .addComponent(filterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(signatureEditorPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(filtersLabel)
                        .addComponent(closeButton))
                    .addComponent(filtersToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        caseSensitiveFilterCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JavaMembersPanel.class, "caseSensitiveFilterCheckBox_ACSD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JCheckBox caseSensitiveFilterCheckBox;
    public javax.swing.JButton closeButton;
    public javax.swing.JLabel filterLabel;
    public javax.swing.JTextField filterTextField;
    public javax.swing.JLabel filtersLabel;
    public javax.swing.JToolBar filtersToolbar;
    public javax.swing.JTree javaMembersTree;
    public javax.swing.JScrollPane javaMembersTreeScrollPane;
    public javax.swing.JToggleButton showConstructorsToggleButton;
    public javax.swing.JToggleButton showEnumConstantsToggleButton;
    public javax.swing.JToggleButton showFQNToggleButton;
    public javax.swing.JToggleButton showFieldsToggleButton;
    public javax.swing.JToggleButton showInheritedToggleButton;
    public javax.swing.JToggleButton showInnerToggleButton;
    public javax.swing.JToggleButton showMethodsToggleButton;
    public javax.swing.JToggleButton showPackageToggleButton;
    public javax.swing.JToggleButton showPrivateToggleButton;
    public javax.swing.JToggleButton showProtectedToggleButton;
    public javax.swing.JToggleButton showStaticToggleButton;
    public javax.swing.JEditorPane signatureEditorPane;
    public javax.swing.JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables
}

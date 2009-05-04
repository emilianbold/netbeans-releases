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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
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
import javax.swing.tree.TreeNode;
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
public class JavaMembersPanel extends javax.swing.JPanel {
    private static TreeModel pleaseWaitTreeModel;
    static
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        root.add(new DefaultMutableTreeNode(NbBundle.getMessage(JavaMembersPanel.class, "LBL_WaitNode"))); // NOI18N
        pleaseWaitTreeModel = new DefaultTreeModel(root);
    }

    private FileObject fileObject;
    private JavaMembersModel javaMembersModel;
    private JavaMembersModel.FilterModel javaMembersFilterModel;

    /**
     *
     * @param fileObject
     * @param elements
     * @param compilationInfo
     */
    public JavaMembersPanel(FileObject fileObject, Element[] elements, CompilationInfo compilationInfo) {
        this.fileObject = fileObject;
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

        javaMembersModel = new JavaMembersModel(fileObject, elements, compilationInfo);
        javaMembersFilterModel = javaMembersModel.getFilterModel();
        javaMembersTree.setModel(javaMembersFilterModel);

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
        
        javaMembersFilterModel.setPattern(filterTextField.getText());
        
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
        RequestProcessor.getDefault().post(
            new Runnable() {
            public void run() {
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
            Object node = treePath.getLastPathComponent();
            if (node instanceof JavaElement) {
                docPane.setData( ((JavaElement)node).getJavaDoc() );
            }
        }
    }

    private DocumentationScrollPane docPane;

    private void close() {
        Window window = SwingUtilities.getWindowAncestor(JavaMembersPanel.this);
        if (window != null) {
            window.setVisible(false);
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
        showInheritedToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showInheritedToggleButton);

        showFQNToggleButton.setIcon(JavaMembersAndHierarchyIcons.FQN_ICON);
        showFQNToggleButton.setSelected(true);
        showFQNToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showFQNToggleButton")); // NOI18N
        showFQNToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showFQNToggleButton);

        showInnerToggleButton.setIcon(JavaMembersAndHierarchyIcons.INNER_CLASS_ICON);
        showInnerToggleButton.setSelected(true);
        showInnerToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showInnerToggleButton")); // NOI18N
        showInnerToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showInnerToggleButton);

        showConstructorsToggleButton.setIcon(JavaMembersAndHierarchyIcons.CONSTRUCTOR_ICON);
        showConstructorsToggleButton.setSelected(true);
        showConstructorsToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showConstructorsToggleButton")); // NOI18N
        showConstructorsToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showConstructorsToggleButton);

        showMethodsToggleButton.setIcon(JavaMembersAndHierarchyIcons.METHOD_ICON);
        showMethodsToggleButton.setSelected(true);
        showMethodsToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showMethodsToggleButton")); // NOI18N
        showMethodsToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showMethodsToggleButton);

        showFieldsToggleButton.setIcon(JavaMembersAndHierarchyIcons.FIELD_ICON);
        showFieldsToggleButton.setSelected(true);
        showFieldsToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showFieldsToggleButton")); // NOI18N
        showFieldsToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showFieldsToggleButton);

        showEnumConstantsToggleButton.setIcon(JavaMembersAndHierarchyIcons.ENUM_CONSTANTS_ICON);
        showEnumConstantsToggleButton.setSelected(true);
        showEnumConstantsToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showEnumConstantsToggleButton")); // NOI18N
        showEnumConstantsToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showEnumConstantsToggleButton);

        showProtectedToggleButton.setIcon(JavaMembersAndHierarchyIcons.PROTECTED_ICON);
        showProtectedToggleButton.setSelected(true);
        showProtectedToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showProtectedToggleButton")); // NOI18N
        showProtectedToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showProtectedToggleButton);

        showPackageToggleButton.setIcon(JavaMembersAndHierarchyIcons.PACKAGE_ICON);
        showPackageToggleButton.setSelected(true);
        showPackageToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showPackageToggleButton")); // NOI18N
        showPackageToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showPackageToggleButton);

        showPrivateToggleButton.setIcon(JavaMembersAndHierarchyIcons.PRIVATE_ICON);
        showPrivateToggleButton.setSelected(true);
        showPrivateToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showPrivateToggleButton")); // NOI18N
        showPrivateToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showPrivateToggleButton);

        showStaticToggleButton.setIcon(JavaMembersAndHierarchyIcons.STATIC_ICON);
        showStaticToggleButton.setSelected(true);
        showStaticToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showStaticToggleButton")); // NOI18N
        showStaticToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        filtersToolbar.add(showStaticToggleButton);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 740, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(filterLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(caseSensitiveFilterCheckBox))
                    .add(signatureEditorPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 740, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(filtersLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filtersToolbar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
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

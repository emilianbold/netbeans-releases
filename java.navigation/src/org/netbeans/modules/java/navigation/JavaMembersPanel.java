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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
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
public class JavaMembersPanel extends javax.swing.JPanel {
    private FileObject fileObject;
    private JavaMembersModel javaMembersModel;

    /**
     *
     * @param fileObject
     * @param elements
     * @param compilationInfo
     */
    public JavaMembersPanel(FileObject fileObject, Element[] elements, CompilationInfo compilationInfo) {
        this.fileObject = fileObject;
        initComponents();

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
        javaMembersTree.setModel(javaMembersModel);
        javaDocPane.setEditorKitForContentType("text/html", new HTMLEditorKit()); // NOI18N
        javaDocPane.setContentType("text/html"); // NOI18N

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
                KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, true),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Utils.previousRow(javaMembersTree);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Utils.nextRow(javaMembersTree);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true),
                JComponent.WHEN_FOCUSED);

        filterTextField.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Utils.lastRow(javaMembersTree);
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
                applyFilter();
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
        //                        JavaMembersPanel.this.JavaMembersModel = new JavaMembersModel(((JavaElement)node).getElementHandle());
        //                        javaMembersTree.setModel(JavaMembersPanel.this.JavaMembersModel);
                                applyFilter();
                            }
                        }
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true),
                JComponent.WHEN_FOCUSED);

        javaDocPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    URL url = e.getURL();
                    if (url != null //&& url.getProtocol().equals("http")
                            ) {
                        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                    }
                }
            }
        });

        showInheritedToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyFilter();
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

    private boolean showingSubDialog = false;

    public void addNotify() {
        super.addNotify();
        SwingUtilities.getRootPane(this).registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        close();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        SwingUtilities.getWindowAncestor(JavaMembersPanel.this).addWindowListener(
                new WindowAdapter() {
            public void windowDeactivated(WindowEvent windowEvent) {
                if (!showingSubDialog) {
                    close();
                }
            }});
        applyFilter();
    }

    private void applyFilter() {
        // show wait cursor
        SwingUtilities.invokeLater(
                new Runnable() {
            public void run() {
                JRootPane rootPane = SwingUtilities.getRootPane(JavaMembersPanel.this);
                if (rootPane != null) {
                    rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                }
            }
        });

        // apply filters and update the tree
        SwingUtilities.invokeLater(
            new Runnable() {
            public void run() {
                javaMembersModel.setPattern(filterTextField.getText());

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

                javaMembersModel.update();

                // expand the tree
                for (int row = 0; row < javaMembersTree.getRowCount(); row++) {
                    TreePath treePath = javaMembersTree.getPathForRow(row);
                    javaMembersTree.expandRow(row);
                }

                // select first matching
                for (int row = 0; row < javaMembersTree.getRowCount(); row++) {
                    Object o = javaMembersTree.getPathForRow(row).getLastPathComponent();
                    if (o instanceof JavaElement) {
                        if (javaMembersModel.patternMatch((JavaElement)o)) {
                            javaMembersTree.setSelectionRow(row);
                            break;
                        }
                    }
                }

                JRootPane rootPane = SwingUtilities.getRootPane(JavaMembersPanel.this);
                if (rootPane != null) {
                    rootPane.setCursor(Cursor.getDefaultCursor());
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
                Utils.showJavaDoc((JavaElement)node, javaDocPane);
            }
        }
    }

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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        filterLabel = new javax.swing.JLabel();
        filterTextField = new javax.swing.JTextField();
        caseSensitiveFilterCheckBox = new javax.swing.JCheckBox();
        splitPane = new javax.swing.JSplitPane();
        javaMembersTreeScrollPane = new javax.swing.JScrollPane();
        javaMembersTree = new javax.swing.JTree();
        javaDocScrollPane = new javax.swing.JScrollPane();
        javaDocPane = new javax.swing.JEditorPane();
        signatureEditorPane = new javax.swing.JEditorPane();
        filtersLabel = new javax.swing.JLabel();
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
        closeButton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        filterLabel.setDisplayedMnemonic('F');
        filterLabel.setLabelFor(filterTextField);
        filterLabel.setText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("LABEL_filterLabel")); // NOI18N

        filterTextField.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_filterTextField")); // NOI18N

        caseSensitiveFilterCheckBox.setMnemonic('C');
        caseSensitiveFilterCheckBox.setText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("LABEL_caseSensitiveFilterCheckBox")); // NOI18N
        caseSensitiveFilterCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        caseSensitiveFilterCheckBox.setFocusable(false);
        caseSensitiveFilterCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        splitPane.setDividerLocation(400);
        splitPane.setOneTouchExpandable(true);

        javaMembersTreeScrollPane.setBorder(null);
        javaMembersTreeScrollPane.setViewportView(javaMembersTree);

        splitPane.setLeftComponent(javaMembersTreeScrollPane);

        javaDocPane.setEditable(false);
        javaDocScrollPane.setViewportView(javaDocPane);

        splitPane.setRightComponent(javaDocScrollPane);

        signatureEditorPane.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Nb.ScrollPane.Border.color")));
        signatureEditorPane.setContentType("text/x-java");
        signatureEditorPane.setEditable(false);

        filtersLabel.setText(org.openide.util.NbBundle.getMessage(JavaMembersPanel.class, "LABEL_filtersLabel")); // NOI18N

        showInheritedToggleButton.setIcon(JavaMembersAndHierarchyIcons.INHERITED_ICON);
        showInheritedToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showInheritedToggleButton")); // NOI18N
        showInheritedToggleButton.setFocusPainted(false);
        showInheritedToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        showFQNToggleButton.setIcon(JavaMembersAndHierarchyIcons.FQN_ICON);
        showFQNToggleButton.setSelected(true);
        showFQNToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showFQNToggleButton")); // NOI18N
        showFQNToggleButton.setFocusPainted(false);
        showFQNToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        showInnerToggleButton.setIcon(JavaMembersAndHierarchyIcons.INNER_CLASS_ICON);
        showInnerToggleButton.setSelected(true);
        showInnerToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showInnerToggleButton")); // NOI18N
        showInnerToggleButton.setFocusPainted(false);
        showInnerToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        showConstructorsToggleButton.setIcon(JavaMembersAndHierarchyIcons.CONSTRUCTOR_ICON);
        showConstructorsToggleButton.setSelected(true);
        showConstructorsToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showConstructorsToggleButton")); // NOI18N
        showConstructorsToggleButton.setFocusPainted(false);
        showConstructorsToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        showMethodsToggleButton.setIcon(JavaMembersAndHierarchyIcons.METHOD_ICON);
        showMethodsToggleButton.setSelected(true);
        showMethodsToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showMethodsToggleButton")); // NOI18N
        showMethodsToggleButton.setFocusPainted(false);
        showMethodsToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        showFieldsToggleButton.setIcon(JavaMembersAndHierarchyIcons.FIELD_ICON);
        showFieldsToggleButton.setSelected(true);
        showFieldsToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showFieldsToggleButton")); // NOI18N
        showFieldsToggleButton.setFocusPainted(false);
        showFieldsToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        showEnumConstantsToggleButton.setIcon(JavaMembersAndHierarchyIcons.ENUM_CONSTANTS_ICON);
        showEnumConstantsToggleButton.setSelected(true);
        showEnumConstantsToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showFieldsToggleButton")); // NOI18N
        showEnumConstantsToggleButton.setFocusPainted(false);
        showEnumConstantsToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        showProtectedToggleButton.setIcon(JavaMembersAndHierarchyIcons.PROTECTED_ICON);
        showProtectedToggleButton.setSelected(true);
        showProtectedToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showProtectedToggleButton")); // NOI18N
        showProtectedToggleButton.setFocusPainted(false);
        showProtectedToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        showPackageToggleButton.setIcon(JavaMembersAndHierarchyIcons.PACKAGE_ICON);
        showPackageToggleButton.setSelected(true);
        showPackageToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showPackageToggleButton")); // NOI18N
        showPackageToggleButton.setFocusPainted(false);
        showPackageToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        showPrivateToggleButton.setIcon(JavaMembersAndHierarchyIcons.PRIVATE_ICON);
        showPrivateToggleButton.setSelected(true);
        showPrivateToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showPrivateToggleButton")); // NOI18N
        showPrivateToggleButton.setFocusPainted(false);
        showPrivateToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        showStaticToggleButton.setIcon(JavaMembersAndHierarchyIcons.STATIC_ICON);
        showStaticToggleButton.setSelected(true);
        showStaticToggleButton.setToolTipText(org.openide.util.NbBundle.getBundle(JavaMembersPanel.class).getString("TOOLTIP_showStaticToggleButton")); // NOI18N
        showStaticToggleButton.setFocusPainted(false);
        showStaticToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        closeButton.setMnemonic('l');
        closeButton.setText(org.openide.util.NbBundle.getMessage(JavaMembersPanel.class, "LABEL_Close")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(filterLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                        .add(15, 15, 15)
                        .add(caseSensitiveFilterCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(signatureEditorPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(filtersLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showInheritedToggleButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showFQNToggleButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showInnerToggleButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showConstructorsToggleButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showMethodsToggleButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showFieldsToggleButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showEnumConstantsToggleButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showProtectedToggleButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showPackageToggleButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showPrivateToggleButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showStaticToggleButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 456, Short.MAX_VALUE)
                        .add(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(caseSensitiveFilterCheckBox)
                    .add(filterLabel)
                    .add(filterTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(signatureEditorPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(filtersLabel)
                    .add(showInheritedToggleButton)
                    .add(showFQNToggleButton)
                    .add(showInnerToggleButton)
                    .add(showConstructorsToggleButton)
                    .add(showMethodsToggleButton)
                    .add(showFieldsToggleButton)
                    .add(showEnumConstantsToggleButton)
                    .add(showProtectedToggleButton)
                    .add(showPackageToggleButton)
                    .add(showPrivateToggleButton)
                    .add(showStaticToggleButton)
                    .add(closeButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JCheckBox caseSensitiveFilterCheckBox;
    public javax.swing.JButton closeButton;
    public javax.swing.JLabel filterLabel;
    public javax.swing.JTextField filterTextField;
    public javax.swing.JLabel filtersLabel;
    public javax.swing.JEditorPane javaDocPane;
    public javax.swing.JScrollPane javaDocScrollPane;
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

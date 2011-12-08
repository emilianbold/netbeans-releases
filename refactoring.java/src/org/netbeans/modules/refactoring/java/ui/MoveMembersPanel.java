/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.refactoring.java.RefactoringModule;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaMoveMembersProperties.Visibility;
import org.netbeans.modules.refactoring.java.ui.elements.ElementNode.Description;
import org.netbeans.modules.refactoring.java.ui.elements.SortActionSupport.SortByNameAction;
import org.netbeans.modules.refactoring.java.ui.elements.SortActionSupport.SortBySourceAction;
import org.netbeans.modules.refactoring.java.ui.elements.*;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.CheckableNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.*;

/**
 *
 * @author Ralph Ruijs
 */
@Messages({"#filters",
    "LBL_ShowNonPublic=Show Non-Public Members",
    "LBL_ShowStatic=Show Static Members",
    "LBL_ShowFields=Show Fields",
    "LBL_ShowInherited=Show Inherited Members",
    "LBL_ShowNonPublicTip=Show non-public members",
    "LBL_ShowStaticTip=Show static members",
    "LBL_ShowFieldsTip=Show fields",
    "LBL_ShowInheritedTip=Show inherited members"})
public class MoveMembersPanel extends javax.swing.JPanel implements CustomRefactoringPanel, ExplorerManager.Provider, DescriptionFilter, FiltersManager.FilterChangeListener {

    private static final String MIME_JAVA = "text/x-java"; // NOI18N
    private static final String JAVADOC = "updateJavadoc.moveMembers"; // NOI18N
    private static final String DELEGATE = "delegate.moveMembers"; // NOI18N
    private static final String DEPRECATE = "deprecate.moveMembers"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(MoveMembersPanel.class.getName(), 1);
    private ChangeListener parent;
    private FiltersManager filtersManager;
    private final ExplorerManager manager;
    private final TreePathHandle[] selectedElements;
    private final JComponent[] singleLineEditor;
    private final TargetTypeAction returnTypeAction;
    private final FileObject fileObject;
    private TreePathHandle target;
    private TapPanel filtersPanel;

    /**
     * constants for defined filters
     */
    private static final String SHOW_NON_PUBLIC = "show_non_public";
    private static final String SHOW_STATIC = "show_static";
    private static final String SHOW_FIELDS = "show_fields";
    private static final String SHOW_INHERITED = "show_inherited";
    private JToggleButton sortByNameButton;
    private JToggleButton sortByPositionButton;
    private boolean naturalSort;
    private final Action[] actions;

    /**
     * Creates new form MoveMembersPanel
     */
    public MoveMembersPanel(TreePathHandle[] selectedElements, ChangeListener parent) {
        manager = new ExplorerManager();
        this.parent = parent;
        this.naturalSort = NbPreferences.forModule(MoveMembersPanel.class).getBoolean("naturalSort", false); //NOI18N
        this.selectedElements = selectedElements;
        this.fileObject = selectedElements[0].getFileObject();
        this.returnTypeAction = new TargetTypeAction();
        singleLineEditor = Utilities.createSingleLineEditor(MIME_JAVA);
        singleLineEditor[0].setPreferredSize(null);
        initComponents();
        try {
            DataObject dob = DataObject.find(fileObject);
            ((JEditorPane) singleLineEditor[1]).getDocument().putProperty(
                    Document.StreamDescriptionProperty,
                    dob);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        manager.setRootContext(ElementNode.getWaitNode());
        outlineView1.getOutline().setRootVisible(true);
        outlineView1.getOutline().setTableHeader(null);
        initFiltersPanel();
        actions = new Action[] {
            new SortByNameAction( this ),
            new SortBySourceAction( this )
        };
    }
    
    public Action[] getActions() {
        return actions;
    }

    private void initFiltersPanel() throws MissingResourceException {
        filtersPanel = new TapPanel();
        filtersPanel.setOrientation(TapPanel.DOWN);
        FiltersDescription desc = new FiltersDescription();

//        desc.addFilter(SHOW_INHERITED,
//                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowInherited"), //NOI18N
//                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowInheritedTip"), //NOI18N
//                false, ImageUtilities.loadImageIcon("org/netbeans/modules/java/navigation/resources/filterHideInherited.png", false), //NOI18N
//                null);
        desc.addFilter(SHOW_FIELDS,
                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowFields"), //NOI18N
                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowFieldsTip"), //NOI18N
                true, ImageUtilities.loadImageIcon("org/netbeans/modules/java/navigation/resources/filterHideFields.png", false), //NOI18N
                null);
        desc.addFilter(SHOW_STATIC,
                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowStatic"), //NOI18N
                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowStaticTip"), //NOI18N
                true, ImageUtilities.loadImageIcon("org/netbeans/modules/java/navigation/resources/filterHideStatic.png", false), //NOI18N
                null);
        desc.addFilter(SHOW_NON_PUBLIC,
                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowNonPublic"), //NOI18N
                NbBundle.getMessage(MoveMembersPanel.class, "LBL_ShowNonPublicTip"), //NOI18N
                true, ImageUtilities.loadImageIcon("org/netbeans/modules/java/navigation/resources/filterHideNonPublic.png", false), //NOI18N
                null);
        AbstractButton[] res = new AbstractButton[4];
        sortByNameButton = new JToggleButton(new SortActionSupport.SortByNameAction(this));
        sortByNameButton.setToolTipText(sortByNameButton.getText());
        sortByNameButton.setText(null);
        sortByNameButton.setSelected(!isNaturalSort());
        res[0] = sortByNameButton;

        sortByPositionButton = new JToggleButton(new SortActionSupport.SortBySourceAction(this));
        sortByPositionButton.setToolTipText(sortByPositionButton.getText());
        sortByPositionButton.setText(null);
        sortByPositionButton.setSelected(isNaturalSort());
        res[1] = sortByPositionButton;
        
        res[2] = new JButton(null, new JCheckBoxIcon(true, new Dimension(16, 16)));
        res[2].addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll(true);
            }
        });
        res[3] = new JButton(null, new JCheckBoxIcon(false, new Dimension(16, 16)));
        res[3].addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll(false);
            }
        });
        filtersManager = FiltersDescription.createManager(desc);
        filtersManager.hookChangeListener(this);

        JComponent buttons = filtersManager.getComponent(res);
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        filtersPanel.add(buttons);
        if ("Aqua".equals(UIManager.getLookAndFeel().getID())) //NOI18N
        {
            filtersPanel.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
        membersListPanel.add(filtersPanel, BorderLayout.SOUTH);
    }

    public Collection<Description> filter(Collection<Description> original) {

        boolean non_public = filtersManager.isSelected(SHOW_NON_PUBLIC);
        boolean statik = filtersManager.isSelected(SHOW_STATIC);
        boolean fields = filtersManager.isSelected(SHOW_FIELDS);
        boolean inherited = /* filtersManager.isSelected(SHOW_INHERITED) */ false;

        ArrayList<Description> result = new ArrayList<Description>(original.size());
        for (Description description : original) {

            if(description.isConstructor()) {
                continue;
            }
            if (!inherited && description.isInherited()) {
                continue;
            }
            if (!non_public
                    && !description.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }

            if (!statik && description.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }

            if (!fields && description.getKind() == ElementKind.FIELD) {
                continue;
            }
            result.add(description);
        }
        Collections.sort(result, isNaturalSort() ? Description.POSITION_COMPARATOR : Description.ALPHA_COMPARATOR);
        return result;
    }

    public void setNaturalSort(boolean naturalSort) {
        this.naturalSort = naturalSort;
        NbPreferences.forModule(MoveMembersPanel.class).putBoolean("naturalSort", naturalSort); //NOI18N
        if (null != sortByNameButton) {
            sortByNameButton.setSelected(!naturalSort);
        }
        if (null != sortByPositionButton) {
            sortByPositionButton.setSelected(naturalSort);
        }
        sort();
    }

    public void sort() {
        ElementNode root = getRootNode();
        if (null != root) {
            root.refreshRecursively();
        }
    }

    private ElementNode getRootNode() {
        Node n = manager.getRootContext();
        if (n instanceof ElementNode) {
            return (ElementNode) n;
        } else {
            return null;
        }
    }
    
    private void selectAll(boolean select) {
        for (Node node : manager.getRootContext().getChildren().getNodes()) {
            if(node instanceof ElementNode) {
                ElementNode elementNode = (ElementNode) node;
                CheckableNode check = elementNode.getLookup().lookup(CheckableNode.class);
                if(check != null) {
                    check.setSelected(select);
                    elementNode.selectionChanged();
                }
            }
        }
    }

    private Action getReturnTypeAction() {
        return returnTypeAction;
    }
    private boolean initialized = false;

    @Override
    public void initialize() {
        if (!initialized) {
            RP.post(new Runnable() {

                @Override
                public void run() {
                    JavaSource javaSource = JavaSource.forFileObject(fileObject);
                    if (javaSource != null) {
                        try {
                            javaSource.runWhenScanFinished(new ElementScanningTask(), true);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
            initialized = true;
        }
    }

    @Override
    public Component getComponent() {
        return this;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        membersListPanel = new javax.swing.JPanel();
        outlineView1 = new org.openide.explorer.view.OutlineView();
        visibilityPanel = new javax.swing.JPanel();
        btnEscalate = new javax.swing.JRadioButton();
        btnAsIs = new javax.swing.JRadioButton();
        btnPrivate = new javax.swing.JRadioButton();
        btnDefault = new javax.swing.JRadioButton();
        btnProtected = new javax.swing.JRadioButton();
        btnPublic = new javax.swing.JRadioButton();
        lblMoveMembersFrom = new javax.swing.JLabel();
        lblSource = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = (JScrollPane)singleLineEditor[0];
        btnFindType = new javax.swing.JButton();
        lblTarget = new javax.swing.JLabel();
        chkDelegate = new javax.swing.JCheckBox();
        chkDeprecate = new javax.swing.JCheckBox();
        chkJavaDoc = new javax.swing.JCheckBox();

        membersListPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.membersListPanel.border.title"))); // NOI18N
        membersListPanel.setLayout(new java.awt.BorderLayout());

        outlineView1.setDoubleBuffered(true);
        outlineView1.setDragSource(false);
        outlineView1.setDropTarget(false);
        outlineView1.setTreeSortable(true);
        membersListPanel.add(outlineView1, java.awt.BorderLayout.CENTER);

        visibilityPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.visibilityPanel.border.title"))); // NOI18N

        buttonGroup1.add(btnEscalate);
        btnEscalate.setSelected(true);
        btnEscalate.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.btnEscalate.text")); // NOI18N
        btnEscalate.setActionCommand(Visibility.ESCALATE.name());

        buttonGroup1.add(btnAsIs);
        btnAsIs.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.btnAsIs.text")); // NOI18N
        btnAsIs.setActionCommand(Visibility.ASIS.name());

        buttonGroup1.add(btnPrivate);
        btnPrivate.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.btnPrivate.text")); // NOI18N
        btnPrivate.setActionCommand(Visibility.PRIVATE.name());

        buttonGroup1.add(btnDefault);
        btnDefault.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.btnDefault.text")); // NOI18N
        btnDefault.setActionCommand(Visibility.DEFAULT.name());

        buttonGroup1.add(btnProtected);
        btnProtected.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.btnProtected.text")); // NOI18N
        btnProtected.setActionCommand(Visibility.PROTECTED.name());

        buttonGroup1.add(btnPublic);
        btnPublic.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.btnPublic.text")); // NOI18N
        btnPublic.setActionCommand(Visibility.PUBLIC.name());

        javax.swing.GroupLayout visibilityPanelLayout = new javax.swing.GroupLayout(visibilityPanel);
        visibilityPanel.setLayout(visibilityPanelLayout);
        visibilityPanelLayout.setHorizontalGroup(
            visibilityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(visibilityPanelLayout.createSequentialGroup()
                .addGroup(visibilityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEscalate)
                    .addComponent(btnAsIs)
                    .addComponent(btnPrivate)
                    .addComponent(btnDefault)
                    .addComponent(btnProtected)
                    .addComponent(btnPublic))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        visibilityPanelLayout.setVerticalGroup(
            visibilityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(visibilityPanelLayout.createSequentialGroup()
                .addComponent(btnEscalate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAsIs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPrivate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDefault)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnProtected)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPublic))
        );

        lblMoveMembersFrom.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.lblMoveMembersFrom.text")); // NOI18N

        lblSource.setText("<ClassName>"); // NOI18N

        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        btnFindType.setAction(getReturnTypeAction());
        btnFindType.setText("…"); // NOI18N
        jPanel2.add(btnFindType, java.awt.BorderLayout.EAST);

        lblTarget.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.lblTarget.text")); // NOI18N
        jPanel2.add(lblTarget, java.awt.BorderLayout.WEST);

        chkDelegate.setSelected(((Boolean) RefactoringModule.getOption(DELEGATE, Boolean.FALSE)).booleanValue());
        chkDelegate.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.chkDelegate.text")); // NOI18N
        chkDelegate.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkDelegateItemStateChanged(evt);
            }
        });

        chkDeprecate.setSelected(((Boolean) RefactoringModule.getOption(DEPRECATE, Boolean.TRUE)).booleanValue());
        chkDeprecate.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.chkDeprecate.text")); // NOI18N
        chkDeprecate.setEnabled(((Boolean) RefactoringModule.getOption(DELEGATE, Boolean.FALSE)).booleanValue());
        chkDeprecate.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkDeprecateItemStateChanged(evt);
            }
        });

        chkJavaDoc.setSelected(((Boolean) RefactoringModule.getOption(JAVADOC, Boolean.TRUE)).booleanValue());
        chkJavaDoc.setText(org.openide.util.NbBundle.getMessage(MoveMembersPanel.class, "MoveMembersPanel.chkJavaDoc.text")); // NOI18N
        chkJavaDoc.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkJavaDocItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(membersListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(visibilityPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(chkDeprecate))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblMoveMembersFrom)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblSource))
                            .addComponent(chkDelegate)
                            .addComponent(chkJavaDoc))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMoveMembersFrom)
                    .addComponent(lblSource))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(visibilityPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(membersListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 339, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkJavaDoc)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkDelegate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkDeprecate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void chkDeprecateItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkDeprecateItemStateChanged
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption(DEPRECATE, b);
    }//GEN-LAST:event_chkDeprecateItemStateChanged

    private void chkDelegateItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkDelegateItemStateChanged
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption(DELEGATE, b);
        chkDeprecate.setVisible(b);
    }//GEN-LAST:event_chkDelegateItemStateChanged

    private void chkJavaDocItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkJavaDocItemStateChanged
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption(JAVADOC, b);
    }//GEN-LAST:event_chkJavaDocItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton btnAsIs;
    private javax.swing.JRadioButton btnDefault;
    private javax.swing.JRadioButton btnEscalate;
    private javax.swing.JButton btnFindType;
    private javax.swing.JRadioButton btnPrivate;
    private javax.swing.JRadioButton btnProtected;
    private javax.swing.JRadioButton btnPublic;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox chkDelegate;
    private javax.swing.JCheckBox chkDeprecate;
    private javax.swing.JCheckBox chkJavaDoc;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblMoveMembersFrom;
    private javax.swing.JLabel lblSource;
    private javax.swing.JLabel lblTarget;
    private javax.swing.JPanel membersListPanel;
    private org.openide.explorer.view.OutlineView outlineView1;
    private javax.swing.JPanel visibilityPanel;
    // End of variables declaration//GEN-END:variables

    public List<? extends TreePathHandle> getHandles() {
        List<TreePathHandle> result = new LinkedList<TreePathHandle>();
        ElementNode rootNode = getRootNode();
        for (Description description : rootNode.getDescritption().getSubs()) {
            if(description.getSelected() == Boolean.TRUE) {
                result.add(TreePathHandle.from(description.getElementHandle(), description.getCpInfo()));
            }
        }
        return result;
    }
    
    public boolean getDeprecated() {
        return chkDeprecate.isSelected();
    }
    
    public boolean getUpdateJavaDoc() {
        return chkJavaDoc.isSelected();
    }
    
    public boolean getDelegate() {
        return chkDelegate.isSelected();
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public TreePathHandle getTarget() {
        try {
            JavaSource.forFileObject(fileObject).runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController parameter) throws Exception {
                    TypeElement typeElement = parameter.getElements().getTypeElement(((JEditorPane) singleLineEditor[1]).getText());
                    target = TreePathHandle.create(typeElement, parameter);
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return target;
    }

    private void refresh(final Description description) {
        final DescriptionFilter descriptionFilter = this;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                manager.setRootContext(new ElementNode(description, descriptionFilter));
                outlineView1.getOutline().setRootVisible(false);
                ((JEditorPane) singleLineEditor[1]).setText(description.getElementHandle().getQualifiedName());
                lblSource.setText("<html>" + description.getHtmlHeader()); //NOI18N
                lblSource.setIcon(ElementIcons.getElementIcon(description.getKind(), description.getModifiers()));
            }
        });
    }

    public boolean isNaturalSort() {
        return naturalSort;
    }

    public void filterStateChanged(ChangeEvent e) {
        ElementNode root = getRootNode();
        
        if ( root != null ) {
            root.refreshRecursively();
        }
    }

    public Visibility getVisibility() {
        return Visibility.valueOf(buttonGroup1.getSelection().getActionCommand());
    }

    private class TargetTypeAction extends AbstractAction {

        public TargetTypeAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ElementHandle<TypeElement> type = TypeElementFinder.find(ClasspathInfo.create(fileObject), ((JEditorPane) singleLineEditor[1]).getText(), null);
            if (type != null) {
                String fqn = type.getQualifiedName().toString();
                ((JEditorPane) singleLineEditor[1]).setText(fqn);
                ((JEditorPane) singleLineEditor[1]).selectAll();
                parent.stateChanged(null);
            }
        }
    }

    class ElementScanningTask implements CancellableTask<CompilationController> {

        private final AtomicBoolean canceled = new AtomicBoolean();
        private static final String TYPE_COLOR = "#707070";
        private static final String INHERITED_COLOR = "#7D694A";

        public ElementScanningTask() {
        }

        public void cancel() {
            //System.out.println("Element task canceled");
            canceled.set(true);
        }

        public void run(CompilationController info) throws Exception {
            canceled.set(false); // Task shared for one file needs reset first

            Description rootDescription = null;

            final Map<Element, Long> pos = new HashMap<Element, Long>();
            TreePath typeElementPath = RefactoringUtils.findEnclosingClass(info, selectedElements[0].resolve(info), true, true, true, true, false);

            if (!canceled.get()) {
                Trees trees = info.getTrees();
                PositionVisitor posVis = new PositionVisitor (trees, canceled);
                posVis.scan(info.getCompilationUnit(), pos);
            }
            
            if (!canceled.get() && typeElementPath != null) {
                TypeElement topLevelElement = (TypeElement) info.getTrees().getElement(typeElementPath);
                rootDescription = element2description(topLevelElement, null, false, info, pos);
                if (null != rootDescription) {
                    addMembers(topLevelElement, rootDescription, info, pos);
                }
            }

            if (!canceled.get()) {
                refresh(null != rootDescription ? rootDescription : new Description());
            }
        }

        private void addMembers(final TypeElement e, final Description parentDescription, final CompilationInfo info, final Map<Element, Long> pos) {
            List<? extends Element> members = e.getEnclosedElements();
            for (Element m : members) {
                if (canceled.get()) {
                    return;
                }

                Description d = element2description(m, e, parentDescription.isInherited(), info, pos);
                if (null != d) {
                    parentDescription.getSubs().add(d);
                    if (m instanceof TypeElement && !d.isInherited()) {
                        addMembers((TypeElement) m, d, info, pos);
                    }
                }
            }
        }

        private Description element2description(final Element e, final Element parent,
                final boolean isParentInherited, final CompilationInfo info,
                final Map<Element, Long> pos) {
            if (info.getElementUtilities().isSynthetic(e)) {
                return null;
            }

            boolean inherited = isParentInherited || (null != parent && !parent.equals(e.getEnclosingElement()));
            Description d = new Description(e.getSimpleName().toString(), ElementHandle.create(e), e.getKind(), inherited);

            if (e instanceof TypeElement) {
                d.setSubs(new HashSet<Description>());
                d.setHtmlHeader(createHtmlHeader((TypeElement) e, info.getElements().isDeprecated(e), d.isInherited()));
            } else if (e instanceof ExecutableElement) {
                d.setHtmlHeader(createHtmlHeader((ExecutableElement) e, info.getElements().isDeprecated(e), d.isInherited()));
            } else if (e instanceof VariableElement) {
                if (!(e.getKind() == ElementKind.FIELD || e.getKind() == ElementKind.ENUM_CONSTANT)) {
                    return null;
                }
                d.setHtmlHeader(createHtmlHeader((VariableElement) e, info.getElements().isDeprecated(e), d.isInherited()));
            }

            d.setModifiers(e.getModifiers());
            d.setPos(getPosition(e, info, pos));
            d.setCpInfo(info.getClasspathInfo());
            d.setSelected(isSelected(e, info));
            d.setIsConstructor(e.getKind() == ElementKind.CONSTRUCTOR);

            return d;
        }

        private long getPosition(final Element e, final CompilationInfo info, final Map<Element, Long> pos) {
            Long res = pos.get(e);
            if (res == null) {
                return -1;
            }
            return res.longValue();
        }

        /**
         * Creates HTML display name of the Executable element
         */
        private String createHtmlHeader(ExecutableElement e, boolean isDeprecated, boolean isInherited) {

            StringBuilder sb = new StringBuilder();
            if (isDeprecated) {
                sb.append("<s>"); // NOI18N
            }
            if (isInherited) {
                sb.append("<font color=" + INHERITED_COLOR + ">"); // NOI18N
            }
            Name name = e.getKind() == ElementKind.CONSTRUCTOR ? e.getEnclosingElement().getSimpleName() : e.getSimpleName();
            sb.append(UIUtilities.escape(name.toString()));
            if (isDeprecated) {
                sb.append("</s>"); // NOI18N
            }

            sb.append("("); // NOI18N

            List<? extends VariableElement> params = e.getParameters();
            for (Iterator<? extends VariableElement> it = params.iterator(); it.hasNext();) {
                VariableElement param = it.next();
                sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N
                final boolean vararg = !it.hasNext() && e.isVarArgs();
                sb.append(printArg(param.asType(), vararg));
                sb.append("</font>"); // NOI18N
                sb.append(" "); // NOI18N
                sb.append(UIUtilities.escape(param.getSimpleName().toString()));
                if (it.hasNext()) {
                    sb.append(", "); // NOI18N
                }
            }


            sb.append(")"); // NOI18N

            if (e.getKind() != ElementKind.CONSTRUCTOR) {
                TypeMirror rt = e.getReturnType();
                if (rt.getKind() != TypeKind.VOID) {
                    sb.append(" : "); // NOI18N     
                    sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N
                    sb.append(print(e.getReturnType()));
                    sb.append("</font>"); // NOI18N                    
                }
            }

            return sb.toString();
        }

        private String createHtmlHeader(VariableElement e, boolean isDeprecated, boolean isInherited) {

            StringBuilder sb = new StringBuilder();

            if (isDeprecated) {
                sb.append("<s>"); // NOI18N
            }
            if (isInherited) {
                sb.append("<font color=" + INHERITED_COLOR + ">"); // NOI18N
            }
            sb.append(UIUtilities.escape(e.getSimpleName().toString()));
            if (isDeprecated) {
                sb.append("</s>"); // NOI18N
            }

            if (e.getKind() != ElementKind.ENUM_CONSTANT) {
                sb.append(" : "); // NOI18N
                sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N
                sb.append(print(e.asType()));
                sb.append("</font>"); // NOI18N
            }

            return sb.toString();
        }

        private String createHtmlHeader(TypeElement e, boolean isDeprecated, boolean isInherited) {

            StringBuilder sb = new StringBuilder();
            if (isDeprecated) {
                sb.append("<s>"); // NOI18N
            }
            if (isInherited) {
                sb.append("<font color=" + INHERITED_COLOR + ">"); // NOI18N
            }
            sb.append(UIUtilities.escape(e.getSimpleName().toString()));
            if (isDeprecated) {
                sb.append("</s>"); // NOI18N
            }
            List<? extends TypeParameterElement> typeParams = e.getTypeParameters();
            if (typeParams != null && !typeParams.isEmpty()) {
                sb.append("&lt;"); // NOI18N

                for (Iterator<? extends TypeParameterElement> it = typeParams.iterator(); it.hasNext();) {
                    TypeParameterElement tp = it.next();
                    sb.append(UIUtilities.escape(tp.getSimpleName().toString()));
                    List<? extends TypeMirror> bounds = null;
                    try {
                         bounds = tp.getBounds();
                    } catch (NullPointerException npe) {
                        // Ignore
                    }
                    if (bounds != null && !bounds.isEmpty()) {
                        sb.append(printBounds(bounds));
                    }
                    
                    if (it.hasNext()) {
                        sb.append(", "); // NOI18N
                    }
                }

                sb.append("&gt;"); // NOI18N
            }

            // Add superclass and implemented interfaces

            TypeMirror sc = e.getSuperclass();
            String scName = print(sc);

            if (sc == null
                    || e.getKind() == ElementKind.ENUM
                    || e.getKind() == ElementKind.ANNOTATION_TYPE
                    || "Object".equals(scName) || // NOI18N
                    "<none>".equals(scName)) { // NOI18N
                scName = null;
            }

            List<? extends TypeMirror> ifaces = e.getInterfaces();

            if ((scName != null || !ifaces.isEmpty())
                    && e.getKind() != ElementKind.ANNOTATION_TYPE) {
                sb.append(" :: "); // NOI18N
                if (scName != null) {
                    sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N                
                    sb.append(scName);
                    sb.append("</font>"); // NOI18N
                }
                if (!ifaces.isEmpty()) {
                    if (scName != null) {
                        sb.append(" : "); // NOI18N
                    }
                    for (Iterator<? extends TypeMirror> it = ifaces.iterator(); it.hasNext();) {
                        TypeMirror typeMirror = it.next();
                        sb.append("<font color=" + TYPE_COLOR + ">"); // NOI18N                
                        sb.append(print(typeMirror));
                        sb.append("</font>"); // NOI18N
                        if (it.hasNext()) {
                            sb.append(", "); // NOI18N
                        }
                    }

                }
            }

            return sb.toString();
        }

        private String printBounds(List<? extends TypeMirror> bounds) {
            if (bounds.size() == 1 && "java.lang.Object".equals(bounds.get(0).toString())) {
                return "";
            }

            StringBuilder sb = new StringBuilder();

            sb.append(" extends "); // NOI18N

            for (Iterator<? extends TypeMirror> it = bounds.iterator(); it.hasNext();) {
                TypeMirror bound = it.next();
                sb.append(print(bound));
                if (it.hasNext()) {
                    sb.append(" & "); // NOI18N
                }
            }

            return sb.toString();
        }

        private String printArg(final TypeMirror tm, final boolean varArg) {
            if (varArg) {
                if (tm.getKind() == TypeKind.ARRAY) {
                    final ArrayType at = (ArrayType) tm;
                    final StringBuilder sb = new StringBuilder(print(at.getComponentType()));
                    sb.append("...");   //NOI18N
                    return sb.toString();
                } else {
                    assert false : "Expected array: " + tm.toString() + " ( " + tm.getKind() + " )"; //NOI18N
                }
            }
            return print(tm);
        }

        private String print(TypeMirror tm) {
            StringBuilder sb;

            switch (tm.getKind()) {
                case DECLARED:
                    DeclaredType dt = (DeclaredType) tm;
                    sb = new StringBuilder(dt.asElement().getSimpleName().toString());
                    List<? extends TypeMirror> typeArgs = dt.getTypeArguments();
                    if (!typeArgs.isEmpty()) {
                        sb.append("&lt;");

                        for (Iterator<? extends TypeMirror> it = typeArgs.iterator(); it.hasNext();) {
                            TypeMirror ta = it.next();
                            sb.append(print(ta));
                            if (it.hasNext()) {
                                sb.append(", ");
                            }
                        }
                        sb.append("&gt;");
                    }

                    return sb.toString();
                case TYPEVAR:
                    TypeVariable tv = (TypeVariable) tm;
                    sb = new StringBuilder(tv.asElement().getSimpleName().toString());
                    return sb.toString();
                case ARRAY:
                    ArrayType at = (ArrayType) tm;
                    sb = new StringBuilder(print(at.getComponentType()));
                    sb.append("[]");
                    return sb.toString();
                case WILDCARD:
                    WildcardType wt = (WildcardType) tm;
                    sb = new StringBuilder("?");
                    if (wt.getExtendsBound() != null) {
                        sb.append(" extends "); // NOI18N
                        sb.append(print(wt.getExtendsBound()));
                    }
                    if (wt.getSuperBound() != null) {
                        sb.append(" super "); // NOI18N
                        sb.append(print(wt.getSuperBound()));
                    }
                    return sb.toString();
                default:
                    return UIUtilities.escape(tm.toString());
            }
        }

        private Boolean isSelected(Element e, CompilationInfo info) {
            Boolean result = Boolean.FALSE;
            for (TreePathHandle tph : selectedElements) {
                if (e.equals(tph.resolveElement(info))) {
                    result = Boolean.TRUE;
                    break;
                }
            }
            return result;
        }
    }
    
    private static class PositionVisitor extends TreePathScanner<Void, Map<Element,Long>> {

        private final Trees trees;
        private final SourcePositions sourcePositions;
        private final AtomicBoolean canceled;
        private CompilationUnitTree cu;

        public PositionVisitor (final Trees trees, final AtomicBoolean canceled) {
            assert trees != null;
            assert canceled != null;
            this.trees = trees;
            this.sourcePositions = trees.getSourcePositions();
            this.canceled = canceled;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree node, Map<Element, Long> p) {
            this.cu = node;
            return super.visitCompilationUnit(node, p);
        }

        @Override
        public Void visitClass(ClassTree node, Map<Element, Long> p) {
            Element e = this.trees.getElement(this.getCurrentPath());
            if (e != null) {
                long pos = this.sourcePositions.getStartPosition(cu, node);
                p.put(e, pos);
            }
            return super.visitClass(node, p);
        }

        @Override
        public Void visitMethod(MethodTree node, Map<Element, Long> p) {
            Element e = this.trees.getElement(this.getCurrentPath());
            if (e != null) {
                long pos = this.sourcePositions.getStartPosition(cu, node);
                p.put(e, pos);
            }
            return null;
        }

        @Override
        public Void visitVariable(VariableTree node, Map<Element, Long> p) {
            Element e = this.trees.getElement(this.getCurrentPath());
            if (e != null) {
                long pos = this.sourcePositions.getStartPosition(cu, node);
                p.put(e, pos);
            }
            return null;
        }

        @Override
        public Void scan(Tree tree, Map<Element, Long> p) {
            if (!canceled.get()) {
                return super.scan(tree, p);
            }
            else {                
                return null;
            }
        }        
    }
    
    private static class JCheckBoxIcon implements Icon {
        private final JPanel delegate;

        public JCheckBoxIcon(boolean selected, Dimension dimension) {
            this.delegate = new JPanel(new BorderLayout(), false);
            this.delegate.setSize(dimension);
            this.delegate.setBorder(null);
            this.delegate.add(new JCheckBox(null, null, selected), BorderLayout.CENTER);
            this.delegate.addNotify();
            this.delegate.validate();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.translate(x, y);
            delegate.paintAll(g);
        }

        @Override
        public int getIconWidth() {
            return delegate.getWidth();
        }

        @Override
        public int getIconHeight() {
            return delegate.getHeight();
        }
    }
}

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

/*
 * InspectAndRefactorPanel.java
 *
 * Created on Jun 20, 2011, 4:46:45 PM
 */
package org.netbeans.modules.java.hints.jackpot.impl.refactoring;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.accessibility.AccessibleContext;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;
import org.netbeans.modules.java.hints.jackpot.impl.batch.BatchSearch.Folder;
import org.netbeans.modules.java.hints.jackpot.impl.batch.BatchSearch.Scope;
import org.netbeans.modules.java.hints.jackpot.impl.batch.Scopes;
import org.netbeans.modules.java.hints.jackpot.impl.refactoring.InspectAndRefactorUI.HintWrap;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata;
import org.netbeans.modules.java.hints.jackpot.spi.Trigger.PatternDescription;
import org.netbeans.modules.java.hints.options.HintsPanel;
import org.netbeans.modules.java.hints.options.HintsPanelLogic;
import org.netbeans.modules.refactoring.java.api.ui.JavaScopeBuilder;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Union2;

/**
 *
 * @author Jan Becicka
 */
public class InspectAndRefactorPanel extends javax.swing.JPanel implements PopupMenuListener {

    private static final String PACKAGE = "org/netbeans/spi/java/project/support/ui/package.gif"; // NOI18N    
    private FileObject fileObject;
    private final HintWrap hintWrap;
    org.netbeans.modules.refactoring.api.Scope customScope;
    
    /** Creates new form InspectAndRefactorPanel */
    public InspectAndRefactorPanel(Lookup context, ChangeListener parent, boolean query) {
        
        initComponents();
        hintWrap = context.lookup(HintWrap.class);
        configurationCombo.setModel(new ConfigurationsComboModel(false));
        singleRefactoringCombo.setModel(new InspectionComboModel(hintWrap != null ? Collections.singletonList(hintWrap.hm) : Utilities.getBatchSupportedHints()));
        singleRefactoringCombo.addActionListener( new ActionListener() {

            Object currentItem = singleRefactoringCombo.getSelectedItem();
            @Override
            public void actionPerformed(ActionEvent e) {
                Object tempItem = singleRefactoringCombo.getSelectedItem();
                if (!(tempItem instanceof HintMetadata)) {
                    singleRefactoringCombo.setSelectedItem(currentItem);
                } else {
                    currentItem = tempItem;
                }
            }
        });
   
        configurationCombo.setRenderer(new ConfigurationRenderer());
        singleRefactoringCombo.setRenderer(new InspectionRenderer());
        singleRefactoringCombo.addPopupMenuListener(this);

        DataObject dob = context.lookup(DataObject.class);
        Icon prj = null;
        ProjectInformation pi=null;
        if (dob != null) {
            FileObject file = context.lookup(FileObject.class);
            if (file != null) {
                Project owner = FileOwnerQuery.getOwner(file);
                if (owner != null) {
                    fileObject = file;
                    pi = ProjectUtils.getInformation(owner);
                    prj = pi.getIcon();
                }
            }
        }
        
        JLabel customScope = null;
        JLabel currentFile = null;
        JLabel currentPackage = null;
        JLabel currentProject = null;
        JLabel allProjects = null;
        
        customScope = new JLabel(NbBundle.getMessage(InspectAndRefactorPanel.class, "LBL_CustomScope"), prj , SwingConstants.LEFT); //NOI18N
        if (fileObject!=null) {
            currentFile = new JLabel(NbBundle.getMessage(InspectAndRefactorPanel.class, "LBL_CurrentFile", fileObject.getNameExt()), new ImageIcon(dob.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_32x32)), SwingConstants.LEFT);
            currentPackage = new JLabel(NbBundle.getMessage(InspectAndRefactorPanel.class, "LBL_CurrentPackage", getPackageName(fileObject)), ImageUtilities.loadImageIcon(PACKAGE, false), SwingConstants.LEFT);
            currentProject = new JLabel(NbBundle.getMessage(InspectAndRefactorPanel.class, "LBL_CurrentProject",pi.getDisplayName()), pi.getIcon(), SwingConstants.LEFT);
        }
        allProjects = new JLabel(NbBundle.getMessage(InspectAndRefactorPanel.class, "LBL_AllProjects"), prj, SwingConstants.LEFT); //NOI18N
        if (currentProject!=null)
            scopeCombo.setModel(new DefaultComboBoxModel(new Object[]{allProjects, currentProject, currentPackage, currentFile, customScope }));
        else
            scopeCombo.setModel(new DefaultComboBoxModel(new Object[]{allProjects, customScope }));
        scopeCombo.setRenderer(new JLabelRenderer());
        loadPrefs();
        if (hintWrap != null) {
            singleRefactoringCombo.setSelectedItem(hintWrap.hm);
            setConfig(false);
            singleRefactorRadio.setSelected(true);
            singleRefactorRadio.setEnabled(false);
            singleRefactoringCombo.setEnabled(false);
            manageSingleRefactoring.setEnabled(false);
            configurationRadio.setEnabled(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new ButtonGroup();
        inspectLabel = new JLabel();
        scopeCombo = new JComboBox();
        refactorUsingLabel = new JLabel();
        configurationRadio = new JRadioButton();
        singleRefactorRadio = new JRadioButton();
        configurationCombo = new JComboBox();
        singleRefactoringCombo = new JComboBox();
        manageConfigurations = new JButton();
        manageSingleRefactoring = new JButton();
        customScopeButton = new JButton();

        Mnemonics.setLocalizedText(inspectLabel, NbBundle.getMessage(InspectAndRefactorPanel.class, "InspectAndRefactorPanel.inspectLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(refactorUsingLabel, NbBundle.getMessage(InspectAndRefactorPanel.class, "InspectAndRefactorPanel.refactorUsingLabel.text"));

        buttonGroup.add(configurationRadio);
        configurationRadio.setSelected(true);
        Mnemonics.setLocalizedText(configurationRadio, NbBundle.getMessage(InspectAndRefactorPanel.class, "InspectAndRefactorPanel.configurationRadio.text"));
        configurationRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configurationRadioActionPerformed(evt);
            }
        });

        buttonGroup.add(singleRefactorRadio);

        Mnemonics.setLocalizedText(singleRefactorRadio, NbBundle.getMessage(InspectAndRefactorPanel.class, "InspectAndRefactorPanel.singleRefactorRadio.text"));
        singleRefactorRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                singleRefactorRadioActionPerformed(evt);
            }
        });

        configurationCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                configurationComboItemStateChanged(evt);
            }
        });

        singleRefactoringCombo.setEnabled(false);
        singleRefactoringCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                singleRefactoringComboItemStateChanged(evt);
            }
        });

        Mnemonics.setLocalizedText(manageConfigurations, NbBundle.getMessage(InspectAndRefactorPanel.class, "InspectAndRefactorPanel.manageConfigurations.text")); // NOI18N
        manageConfigurations.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                manageConfigurationsItemStateChanged(evt);
            }
        });
        manageConfigurations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manageConfigurationsActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(manageSingleRefactoring, NbBundle.getMessage(InspectAndRefactorPanel.class, "InspectAndRefactorPanel.manageSingleRefactoring.text"));
        manageSingleRefactoring.setEnabled(false);
        manageSingleRefactoring.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manageSingleRefactoringActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(customScopeButton, NbBundle.getMessage(InspectAndRefactorPanel.class, "InspectAndRefactorPanel.customScopeButton.text"));
        customScopeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                customScopeButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(refactorUsingLabel)
                    .addComponent(inspectLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(configurationRadio)
                            .addComponent(singleRefactorRadio))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(singleRefactoringCombo, 0, 33, Short.MAX_VALUE)
                            .addComponent(configurationCombo, 0, 33, Short.MAX_VALUE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                            .addComponent(manageSingleRefactoring, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(manageConfigurations, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scopeCombo, 0, 179, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(customScopeButton)
                        .addGap(43, 43, 43)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(inspectLabel)
                    .addComponent(scopeCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(customScopeButton))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(configurationRadio)
                    .addComponent(configurationCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(manageConfigurations)
                    .addComponent(refactorUsingLabel))
                .addGap(1, 1, 1)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(singleRefactoringCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(manageSingleRefactoring)
                    .addComponent(singleRefactorRadio)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void configurationRadioActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configurationRadioActionPerformed
        setConfig(true);
    }//GEN-LAST:event_configurationRadioActionPerformed

    private void setConfig(boolean yes) {
        singleRefactoringCombo.setEnabled(!yes);
        manageSingleRefactoring.setEnabled(!yes);
        configurationCombo.setEnabled(yes);
        manageConfigurations.setEnabled(yes);
        storePrefs();
    }
    
    private void singleRefactorRadioActionPerformed(ActionEvent evt) {//GEN-FIRST:event_singleRefactorRadioActionPerformed
        setConfig(false);
    }//GEN-LAST:event_singleRefactorRadioActionPerformed

    private void manageConfigurationsActionPerformed(ActionEvent evt) {//GEN-FIRST:event_manageConfigurationsActionPerformed
        manageRefactorings(false);
    }//GEN-LAST:event_manageConfigurationsActionPerformed

    private void manageSingleRefactoringActionPerformed(ActionEvent evt) {//GEN-FIRST:event_manageSingleRefactoringActionPerformed
        manageRefactorings(true);
    }//GEN-LAST:event_manageSingleRefactoringActionPerformed

    private void manageConfigurationsItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_manageConfigurationsItemStateChanged
        storePrefs();
    }//GEN-LAST:event_manageConfigurationsItemStateChanged

    private void singleRefactoringComboItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_singleRefactoringComboItemStateChanged
        storePrefs();
    }//GEN-LAST:event_singleRefactoringComboItemStateChanged

    private void configurationComboItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_configurationComboItemStateChanged
        storePrefs();
    }//GEN-LAST:event_configurationComboItemStateChanged

    private void customScopeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_customScopeButtonActionPerformed
        switch (scopeCombo.getSelectedIndex()) {
            case 0:
                //all projects
                Set<FileObject> todo = new HashSet<FileObject>();

                for (ClassPath source : GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
                    todo.addAll(Arrays.asList(source.getRoots()));
                }

                customScope = org.netbeans.modules.refactoring.api.Scope.create(todo, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
                break;
            case 1:
                //current project
                customScope = org.netbeans.modules.refactoring.api.Scope.create(Arrays.asList(ClassPath.getClassPath(fileObject, ClassPath.SOURCE).getRoots()), Collections.EMPTY_LIST, Collections.EMPTY_LIST);
                break;
            case 2:
                //current package
                if (fileObject != null) {
                    Collection col = Collections.singleton(new NonRecursiveFolder() {

                        @Override
                        public FileObject getFolder() {
                            return fileObject.getParent();
                        }
                    });
                    customScope = org.netbeans.modules.refactoring.api.Scope.create(Collections.EMPTY_LIST, col, Collections.EMPTY_LIST);
                    break;
                }
            case 3:
                if (fileObject != null) {
                    customScope = org.netbeans.modules.refactoring.api.Scope.create(Collections.singleton(fileObject), Collections.EMPTY_LIST, Collections.EMPTY_LIST);
                    break;
                }
            default:
                //custom
                customScope = org.netbeans.modules.refactoring.api.Scope.create(Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        }
        org.netbeans.modules.refactoring.api.Scope s = JavaScopeBuilder.open(NbBundle.getMessage(InspectAndRefactorPanel.class, "CTL_CustomScope"), customScope);
        if (s != null) {
            customScope = s;
            scopeCombo.setSelectedIndex(scopeCombo.getItemCount() - 1);
        }
    }//GEN-LAST:event_customScopeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ButtonGroup buttonGroup;
    private JComboBox configurationCombo;
    private JRadioButton configurationRadio;
    private JButton customScopeButton;
    private JLabel inspectLabel;
    private JButton manageConfigurations;
    private JButton manageSingleRefactoring;
    private JLabel refactorUsingLabel;
    private JComboBox scopeCombo;
    private JRadioButton singleRefactorRadio;
    private JComboBox singleRefactoringCombo;
    // End of variables declaration//GEN-END:variables

    public Union2<String, Iterable<? extends HintDescription>> getPattern() {
        if(singleRefactorRadio.isSelected()) {
            if (hintWrap != null) {
                return Union2.<String, Iterable<? extends HintDescription>>createSecond(hintWrap.hints);
            }
        HintMetadata hint = (HintMetadata) singleRefactoringCombo.getSelectedItem();
        Collection<? extends HintDescription> hintDesc = RulesManager.getInstance().allHints.get(hint);
        return Union2.<String, Iterable<? extends HintDescription>>createSecond(hintDesc);
            
        } else {
            Configuration config = (Configuration) configurationCombo.getSelectedItem();
            List<HintDescription> hintsToApply = new LinkedList();
            for (HintMetadata hint:config.getHints()) {
                hintsToApply.addAll(RulesManager.getInstance().allHints.get(hint));
            }
            return Union2.<String, Iterable<? extends HintDescription>>createSecond(hintsToApply);
        }
    }

    public Scope getScope() {
        switch (scopeCombo.getSelectedIndex()) {
            case 0:
                //all projects
                return Scopes.allOpenedProjectsScope();
            case 1:
                if (fileObject!=null) 
                    return getThisProjectScope();
                else 
                    return getCustomScope();
            case 2:
                return getThisPackageScope();
            case 3:
                return getThisFileScope();
            case 4:
                return getCustomScope();
            default:
                return Scopes.allOpenedProjectsScope();
        }
    }

    private Scope getCustomScope() {
        if (customScope==null) {
            return Scopes.specifiedFoldersScope(new Folder[0]);
        }
        LinkedList list = new LinkedList();
        list.addAll(customScope.getFiles());
        list.addAll(customScope.getFolders());
        list.addAll(customScope.getSourceRoots());
        
        return Scopes.specifiedFoldersScope(Folder.convert(list));
    }
    

    private Scope getThisProjectScope() {
        return Scopes.specifiedFoldersScope(Folder.convert(ClassPath.getClassPath(fileObject, ClassPath.SOURCE).getRoots()));
    }

    private Scope getThisPackageScope() {
        return Scopes.specifiedFoldersScope(Folder.convert(Collections.singleton(fileObject.getParent())));
    }

    private Scope getThisFileScope() {
        return Scopes.specifiedFoldersScope(Folder.convert(Collections.singleton(fileObject)));
    }

    private void manageRefactorings(boolean single) {
        HintsPanel panel;
        if (single) {
            panel = new HintsPanel((HintMetadata) singleRefactoringCombo.getSelectedItem());
        } else {
            panel = new HintsPanel((Configuration) configurationCombo.getSelectedItem());
        }
        DialogDescriptor descriptor = new DialogDescriptor(panel, NbBundle.getMessage(InspectAndRefactorPanel.class, "CTL_ManageRefactorings"), true, new Object[]{}, null, 0, null, null);
        
        JDialog dialog = (JDialog) DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.validate();
        dialog.pack();
        dialog.setVisible(true);
        Configuration selectedConfiguration = panel.getSelectedConfiguration();
        if (selectedConfiguration != null) {
            configurationCombo.setSelectedItem(selectedConfiguration);
        }
        HintMetadata selectedHint = panel.getSelectedHint();
        if (selectedHint!=null) {
            singleRefactoringCombo.setSelectedItem(selectedHint);
        }
    }

    private boolean prefsLoading = false;

    private void storePrefs() {
        if (prefsLoading)
            return;
        Preferences prefs = NbPreferences.forModule(InspectAndRefactorPanel.class);
        if (hintWrap == null) {
            prefs.putBoolean("InspectAndRefactorPanel.singleRefactorRadio", singleRefactorRadio.isSelected());
            prefs.putInt("InspectAndRefactorPanel.configurationCombo", configurationCombo.getSelectedIndex());
            prefs.putInt("InspectAndRefactorPanel.singleRefactoringCombo", singleRefactoringCombo.getSelectedIndex());
        }
        prefs.putInt("InspectAndRefactorPanel.scopeCombo", scopeCombo.getSelectedIndex());
                
    }
    
    private void loadPrefs() {
        prefsLoading = true;
        try {
            Preferences prefs = NbPreferences.forModule(InspectAndRefactorPanel.class);
            boolean sel = prefs.getBoolean("InspectAndRefactorPanel.singleRefactorRadio", true);
            setConfig(!sel);
            singleRefactorRadio.setSelected(sel);
            try {
                configurationCombo.setSelectedIndex(prefs.getInt("InspectAndRefactorPanel.configurationCombo", 0));
            } catch (IllegalArgumentException iae) {
                //ignore
            }
            try {
                singleRefactoringCombo.setSelectedIndex(prefs.getInt("InspectAndRefactorPanel.singleRefactoringCombo", 0));
            } catch (IllegalArgumentException iae) {
                //ignore
            }
            try {
                scopeCombo.setSelectedIndex(prefs.getInt("InspectAndRefactorPanel.scopeCombo", 0));
            } catch (IllegalArgumentException iae) {
                
            }
        } finally {
            prefsLoading = false;
        }
    }

    private String getPackageName(FileObject file) {
        return ClassPath.getClassPath(file, ClassPath.SOURCE).getResourceName(file.getParent(), '.', false);
    }

    private Popup popup = null;
    private PropertyChangeListener listener;

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        
        final Object comp = singleRefactoringCombo.getUI().getAccessibleChild(singleRefactoringCombo, 0);
        if (!(comp instanceof JPopupMenu)) {
            return;
        }
        
        
        
        SwingUtilities.invokeLater(new Runnable() {
            private static final String HTML_DESC_FOOTER = "</body></html>"; //NOI18N
            private final String HTML_DESC_HEADER = "<html><body><b>" + NbBundle.getMessage(HintsPanel.class, "CTL_Description_Border") + "</b><br>";//NOI18N

            @Override
            public void run() {
                final JPopupMenu menu = (JPopupMenu) comp;
                HintMetadata item = (HintMetadata) singleRefactoringCombo.getSelectedItem();
                
                final JEditorPane pane = new JEditorPane();
                pane.setContentType("text/html");  //NOI18N
                pane.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(pane);
                pane.setText(HTML_DESC_HEADER + item.description + HintsPanelLogic.getQueryWarning(item) + HTML_DESC_FOOTER);
                scrollPane.setPreferredSize(menu.getSize());
                Dimension size = menu.getSize();
                Point location = menu.getLocationOnScreen();
                singleRefactoringCombo.getAccessibleContext().addPropertyChangeListener(listener = new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getPropertyName().equals(AccessibleContext.ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY)) {
                            AccessibleContext context = (AccessibleContext) evt.getNewValue();
                            Object elementAt = singleRefactoringCombo.getModel().getElementAt(context.getAccessibleIndexInParent());
                            if (elementAt instanceof HintMetadata) {
                                HintMetadata item = (HintMetadata) elementAt;
                                pane.setText(HTML_DESC_HEADER + item.description + HintsPanelLogic.getQueryWarning(item) + HTML_DESC_FOOTER);
                                pane.setCaretPosition(0);
                            }
                        }
                    }
                });
                popup = PopupFactory.getSharedInstance().getPopup(menu, scrollPane, (int) (location.getX()), (int) (location.getY() - size.getHeight() - singleRefactoringCombo.getHeight()) + 5);
                popup.show();
            }
        });
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        if (popup!=null) {
            popup.hide();
            popup = null;
        }
        singleRefactoringCombo.getAccessibleContext().removePropertyChangeListener(listener);
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    private static class JLabelRenderer extends JLabel implements ListCellRenderer, UIResource {
        public JLabelRenderer () {
            setOpaque(true);
        }
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if ( value != null ) {
                setText(((JLabel)value).getText());
                setIcon(((JLabel)value).getIcon());
            }
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
        
        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }    
}

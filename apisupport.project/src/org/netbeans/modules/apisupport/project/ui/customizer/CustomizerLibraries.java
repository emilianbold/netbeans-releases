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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.FileChooser;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerComponentFactory.DependencyListModel;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizer;
import org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.java.api.common.project.ui.customizer.ClassPathListCellRenderer;
import org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator;
import org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator.ListComponent;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;

/**
 * Represents <em>Libraries</em> panel in NetBeans Module customizer.
 *
 * @author mkrauskopf
 */
public class CustomizerLibraries extends NbPropertyPanel.Single {
    private final ListComponent emListComp;
    
    /** Creates new form CustomizerLibraries */
    public CustomizerLibraries(final SingleModuleProperties props, ProjectCustomizer.Category category) {
        super(props, CustomizerLibraries.class, category);
        initComponents();
        initAccessibility();
        if (!getProperties().isSuiteComponent()) {
            addLibrary.setVisible(false);
            Mnemonics.setLocalizedText(addDepButton, getMessage("CTL_AddButton"));
        }
        refresh();
        dependencyList.setCellRenderer(CustomizerComponentFactory.getDependencyCellRenderer(false));
        javaPlatformCombo.setRenderer(JavaPlatformComponentFactory.javaPlatformListCellRenderer());
        removeTokenButton.setEnabled(false);
        wrappedJarsList.setCellRenderer(ClassPathListCellRenderer.createClassPathListRenderer(
                getProperties().getEvaluator(),
                FileUtil.toFileObject(getProperties().getProjectDirectoryFile())));
        DefaultButtonModel dummy = new DefaultButtonModel();
        emListComp = EditMediator.createListComponent(wrappedJarsList);
        EditMediator.register(
                getProperties().getProject(),
                getProperties().getHelper(),
                getProperties().getRefHelper(),
                emListComp,
                dummy,
                dummy,
                dummy,
                removeButton.getModel(),
                dummy,
                dummy,
                editButton.getModel(),
                null,
                null);
        attachListeners();
    }

    void refresh() {
        refreshJavaPlatforms();
        refreshPlatforms();
        platformValue.setEnabled(getProperties().isStandalone());
        managePlafsButton.setEnabled(getProperties().isStandalone());
        updateEnabled();
        reqTokenList.setModel(getProperties().getRequiredTokenListModel());
        dependencyList.setModel(getProperties().getDependenciesListModel());
        wrappedJarsList.setModel(getProperties().getWrappedJarsListModel());
        dependencyList.getModel().addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent e) { updateEnabled(); }
            public void intervalAdded(ListDataEvent e) { updateEnabled(); }
            public void intervalRemoved(ListDataEvent e) { updateEnabled(); }
        });
    }
    
    private void attachListeners() {
        platformValue.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // set new platform
                    getProperties().setActivePlatform((NbPlatform) platformValue.getSelectedItem());
                    runDependenciesListModelRefresh();
                }
            }
        });
        dependencyList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateEnabled();
                }
            }
        });
        javaPlatformCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // set new platform
                    getProperties().setActiveJavaPlatform((JavaPlatform) javaPlatformCombo.getSelectedItem());
                }
            }
        });
        reqTokenList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    removeTokenButton.setEnabled(reqTokenList.getSelectedIndex() != -1);
                }
            }
        });
    }

    private Logger LOG = Logger.getLogger(CustomizerLibraries.class.getName());
    
    private void runDependenciesListModelRefresh() {
        dependencyList.setModel(CustomizerComponentFactory.createListWaitModel());
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                // generate fresh dependencies list
                final DependencyListModel newModel = getProperties().getDependenciesListModel();
                LOG.log(Level.FINER, "DependenciesListModel generated");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        LOG.log(Level.FINER, "DependenciesListModel invokeLater entered");
                        dependencyList.setModel(newModel);
                        LOG.log(Level.FINER, "DependenciesListModel model set");
                        updateEnabled();
                    }
                });
            }
        });
        updateEnabled();
    }

    private void refreshJavaPlatforms() {
        javaPlatformCombo.setModel(JavaPlatformComponentFactory.javaPlatformListModel());
        javaPlatformCombo.setSelectedItem(getProperties().getActiveJavaPlatform());
    }
    
    private void refreshPlatforms() {
        platformValue.setModel(new PlatformComponentFactory.NbPlatformListModel(getProperties().getActivePlatform())); // refresh
        platformValue.requestFocusInWindow();
    }
    
    private void updateEnabled() {
        // add and OK is disabled in waitmodel
        // TODO C.P how to disable OK?
        boolean okEnabled = ! CustomizerComponentFactory.isWaitModel(dependencyList.getModel());
        // if there is no selection disable edit/remove buttons
        boolean enabled = dependencyList.getModel().getSize() > 0 
                && okEnabled
                && getProperties().isActivePlatformValid()
                && dependencyList.getSelectedIndex() != -1;
        editDepButton.setEnabled(enabled);
        removeDepButton.setEnabled(enabled);
        addDepButton.setEnabled(okEnabled && getProperties().isActivePlatformValid());
        boolean javaEnabled = getProperties().isNetBeansOrg() ||
                (getProperties().isStandalone() &&
                /* #71631 */ ((NbPlatform) platformValue.getSelectedItem()).getHarnessVersion() >= NbPlatform.HARNESS_VERSION_50u1);
        javaPlatformCombo.setEnabled(javaEnabled);
        javaPlatformButton.setEnabled(javaEnabled);
    }
    
    private CustomizerComponentFactory.DependencyListModel getDepListModel() {
        return (CustomizerComponentFactory.DependencyListModel) dependencyList.getModel();
    }
    
    private String getMessage(String key) {
        return NbBundle.getMessage(CustomizerLibraries.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        platformsPanel = new javax.swing.JPanel();
        platformValue = org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory.getNbPlatformsComboxBox();
        platform = new javax.swing.JLabel();
        managePlafsButton = new javax.swing.JButton();
        javaPlatformLabel = new javax.swing.JLabel();
        javaPlatformCombo = new javax.swing.JComboBox();
        javaPlatformButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelModules = new javax.swing.JPanel();
        dependencySP = new javax.swing.JScrollPane();
        dependencyList = new javax.swing.JList();
        addDepButton = new javax.swing.JButton();
        addLibrary = new javax.swing.JButton();
        removeDepButton = new javax.swing.JButton();
        moduleDepsLabel = new javax.swing.JLabel();
        editDepButton = new javax.swing.JButton();
        jPanelTokens = new javax.swing.JPanel();
        reqTokenSP = new javax.swing.JScrollPane();
        reqTokenList = new javax.swing.JList();
        requiredTokensLabel = new javax.swing.JLabel();
        addTokenButton = new javax.swing.JButton();
        removeTokenButton = new javax.swing.JButton();
        jPanelJars = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        wrappedJarsSP = new javax.swing.JScrollPane();
        wrappedJarsList = new javax.swing.JList();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        addJarButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        platformsPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 12);
        platformsPanel.add(platformValue, gridBagConstraints);

        platform.setLabelFor(platformValue);
        org.openide.awt.Mnemonics.setLocalizedText(platform, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_NetBeansPlatform")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 12);
        platformsPanel.add(platform, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(managePlafsButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_ManagePlatform")); // NOI18N
        managePlafsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                managePlatforms(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        platformsPanel.add(managePlafsButton, gridBagConstraints);

        javaPlatformLabel.setLabelFor(javaPlatformCombo);
        org.openide.awt.Mnemonics.setLocalizedText(javaPlatformLabel, NbBundle.getMessage(CustomizerLibraries.class, "LBL_Java_Platform")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformsPanel.add(javaPlatformLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformsPanel.add(javaPlatformCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(javaPlatformButton, NbBundle.getMessage(CustomizerLibraries.class, "LBL_Manage_Java_Platforms")); // NOI18N
        javaPlatformButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaPlatformButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        platformsPanel.add(javaPlatformButton, gridBagConstraints);

        add(platformsPanel, java.awt.BorderLayout.PAGE_START);

        jPanelModules.setLayout(new java.awt.GridBagLayout());

        dependencySP.setViewportView(dependencyList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 12);
        jPanelModules.add(dependencySP, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addDepButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_AddDependency")); // NOI18N
        addDepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addModuleDependency(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 5, 9);
        jPanelModules.add(addDepButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addLibrary, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_AddNewLibrary")); // NOI18N
        addLibrary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLibraryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 12, 9);
        jPanelModules.add(addLibrary, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeDepButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_RemoveButton")); // NOI18N
        removeDepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeModuleDependency(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 5, 9);
        jPanelModules.add(removeDepButton, gridBagConstraints);

        moduleDepsLabel.setLabelFor(dependencySP);
        org.openide.awt.Mnemonics.setLocalizedText(moduleDepsLabel, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_ModuleDependencies")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jPanelModules.add(moduleDepsLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(editDepButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_EditButton")); // NOI18N
        editDepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editModuleDependency(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 12, 9);
        jPanelModules.add(editDepButton, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_ModulesPanel"), jPanelModules); // NOI18N

        jPanelTokens.setLayout(new java.awt.GridBagLayout());

        reqTokenSP.setViewportView(reqTokenList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 12);
        jPanelTokens.add(reqTokenSP, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(requiredTokensLabel, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_RequiredTokens")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jPanelTokens.add(requiredTokensLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addTokenButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_AddButton")); // NOI18N
        addTokenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToken(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 9);
        jPanelTokens.add(addTokenButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeTokenButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_RemoveButton")); // NOI18N
        removeTokenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeToken(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 9);
        jPanelTokens.add(removeTokenButton, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_TokensPanel"), jPanelTokens); // NOI18N

        jPanelJars.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_WrappedJars")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanelJars.add(jLabel1, gridBagConstraints);

        wrappedJarsSP.setViewportView(wrappedJarsList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 12);
        jPanelJars.add(wrappedJarsSP, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(editButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_EditButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 9);
        jPanelJars.add(editButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_RemoveButton")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 9);
        jPanelJars.add(removeButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addJarButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_AddJarButton")); // NOI18N
        addJarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addJarButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 9);
        jPanelJars.add(addJarButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(exportButton, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "CTL_ExportButton")); // NOI18N
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 9);
        jPanelJars.add(exportButton, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_JarsPanel"), jPanelJars); // NOI18N

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    private void addLibraryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLibraryActionPerformed
        NbModuleProject project = UIUtil.runLibraryWrapperWizard(getProperties().getProject());
        if (project != null) {
            try {
                // presuambly we do not need to reset anything else
                getProperties().resetUniverseDependencies();
                ModuleDependency dep = new ModuleDependency(
                        getProperties().getModuleList().getEntry(project.getCodeNameBase()));
                getDepListModel().addDependency(dep);
            } catch (IOException e) {
                assert false : e;
            }
        }
    }//GEN-LAST:event_addLibraryActionPerformed
    
    private void javaPlatformButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaPlatformButtonActionPerformed
        PlatformsCustomizer.showCustomizer((JavaPlatform) javaPlatformCombo.getSelectedItem());
        refreshJavaPlatforms();
    }//GEN-LAST:event_javaPlatformButtonActionPerformed
    
    private void removeToken(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeToken
        CustomizerComponentFactory.RequiredTokenListModel model = (CustomizerComponentFactory.RequiredTokenListModel) reqTokenList.getModel();
        Object[] selected = reqTokenList.getSelectedValues();
        for (int i = 0; i < selected.length; i++) {
            model.removeToken((String) selected[i]);
        }
        if (model.getSize() > 0) {
            reqTokenList.setSelectedIndex(0);
        }
        reqTokenList.requestFocusInWindow();
    }//GEN-LAST:event_removeToken
    
    private void addToken(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToken
        // create add panel
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.setLayout(new BorderLayout(0, 2));
        JList tokenList = new JList(getProperties().getAllTokens());
        JScrollPane tokenListSP = new JScrollPane(tokenList);
        JLabel provTokensTxt = new JLabel();
        provTokensTxt.setLabelFor(tokenList);
        Mnemonics.setLocalizedText(provTokensTxt, getMessage("LBL_ProvidedTokens_T"));
        panel.getAccessibleContext().setAccessibleDescription(getMessage("ACS_ProvidedTokensTitle"));
        tokenList.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_ProvidedTokens"));
        tokenListSP.getVerticalScrollBar().getAccessibleContext().setAccessibleName(getMessage("ACS_CTL_ProvidedTokensVerticalScroll"));
        tokenListSP.getVerticalScrollBar().getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CTL_ProvidedTokensVerticalScroll"));
        tokenListSP.getHorizontalScrollBar().getAccessibleContext().setAccessibleName(getMessage("ACS_CTL_ProvidedTokensHorizontalScroll"));
        tokenListSP.getHorizontalScrollBar().getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CTL_ProvidedTokensHorizontalScroll"));
        
        panel.add(provTokensTxt, BorderLayout.NORTH);
        panel.add(tokenListSP, BorderLayout.CENTER);
        
        DialogDescriptor descriptor = new DialogDescriptor(panel,
                getMessage("LBL_ProvidedTokens_NoMnem"));
        Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        d.setVisible(true);
        d.dispose();
        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
            Object[] selected = tokenList.getSelectedValues();
            CustomizerComponentFactory.RequiredTokenListModel model = (CustomizerComponentFactory.RequiredTokenListModel) reqTokenList.getModel();
            for (int i = 0; i < selected.length; i++) {
                model.addToken((String) selected[i]);
            }
            if (selected.length > 0) {
                reqTokenList.clearSelection();
                reqTokenList.setSelectedValue(selected[0], true);
            }
        }
        reqTokenList.requestFocusInWindow();
    }//GEN-LAST:event_addToken
    
    private void managePlatforms(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_managePlatforms
        NbPlatformCustomizer.showCustomizer();
        refreshPlatforms();
    }//GEN-LAST:event_managePlatforms
    
    private void editModuleDependency(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editModuleDependency
        ModuleDependency origDep = getDepListModel().getDependency(
                dependencyList.getSelectedIndex());
        EditDependencyPanel editPanel = new EditDependencyPanel(
                origDep, getProperties().getActivePlatform());
        DialogDescriptor descriptor = new DialogDescriptor(editPanel,
                getMessage("CTL_EditModuleDependencyTitle"));
        descriptor.setHelpCtx(new HelpCtx(EditDependencyPanel.class));
        Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        d.setVisible(true);
        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
            getDepListModel().editDependency(origDep, editPanel.getEditedDependency());
        }
        d.dispose();
        dependencyList.requestFocusInWindow();
    }//GEN-LAST:event_editModuleDependency
    
    private void removeModuleDependency(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeModuleDependency
        List<ModuleDependency> deps = NbCollections.checkedListByCopy(Arrays.asList(dependencyList.getSelectedValues()), ModuleDependency.class, false);
        if (deps.size() > 0) {
            getDepListModel().removeDependencies(deps);
            if (dependencyList.getModel().getSize() > 0) {
                dependencyList.setSelectedIndex(0);
            }
        }
        dependencyList.requestFocusInWindow();
    }//GEN-LAST:event_removeModuleDependency
    
    private void addModuleDependency(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addModuleDependency
        ModuleDependency[] newDeps = AddModulePanel.selectDependencies(getProperties());
        for (int i = 0; i < newDeps.length; i++) {
            ModuleDependency dep = newDeps[i];
            if ("0".equals(dep.getReleaseVersion()) && !dep.hasImplementationDepedendency()) { // #72216 NOI18N
                getDepListModel().addDependency(new ModuleDependency(
                            dep.getModuleEntry(), "0-1", dep.getSpecificationVersion(), // NOI18N
                            dep.hasCompileDependency(), dep.hasImplementationDepedendency()));
            } else {
                getDepListModel().addDependency(dep);
            }
            dependencyList.setSelectedValue(dep, true);
        }
        dependencyList.requestFocusInWindow();
    }//GEN-LAST:event_addModuleDependency

    private static final Pattern checkWrappedJarPat = Pattern.compile("^(.*)release[\\\\/]modules[\\\\/]ext[\\\\/]([^\\\\/]+)$");

    private void addJarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addJarButtonActionPerformed
        // Let user search for the Jar file;
        // copied from EditMediator in order to setup
        FileChooser chooser;
        AntProjectHelper helper = getProperties().getHelper();
        Project project = getProperties().getProject();
        if (helper.isSharableProject()) {
            chooser = new FileChooser(helper, true);
        } else {
            chooser = new FileChooser(FileUtil.toFile(project.getProjectDirectory()), null);
        }
        chooser.enableVariableBasedSelection(true);
        chooser.setFileHidingEnabled(false);
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled( true );
        chooser.setDialogTitle( NbBundle.getMessage( EditMediator.class, "LBL_AddJar_DialogTitle" ) ); // NOI18N
        //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
        chooser.setAcceptAllFileFilterUsed( false );
        chooser.setFileFilter(EditMediator.JAR_ZIP_FILTER);
        File curDir = EditMediator.getLastUsedClassPathFolder();
        chooser.setCurrentDirectory (curDir);
        chooser.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage( EditMediator.class, "LBL_AddJar_DialogTitle" ));
        int option = chooser.showOpenDialog( SwingUtilities.getWindowAncestor( emListComp.getComponent() ) ); // Show the chooser

        if ( option == JFileChooser.APPROVE_OPTION ) {

            String filePaths[];
            try {
                filePaths = chooser.getSelectedPaths();
            } catch (IOException ex) {
                // TODO: add localized message
                Exceptions.printStackTrace(ex);
                return;
            }

            // check corrupted jar/zip files
            File base = FileUtil.toFile(helper.getProjectDirectory());
            List<String> newPaths = new ArrayList<String> ();
            for (String path : filePaths) {
                File fl = PropertyUtils.resolveFile(base, path);
                FileObject fo = FileUtil.toFileObject(fl);
                assert fo != null : fl;
                if (FileUtil.isArchiveFile (fo))
                    try {
                        new JarFile (fl);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog (
                            SwingUtilities.getWindowAncestor (emListComp.getComponent ()),
                            NbBundle.getMessage (EditMediator.class, "LBL_Corrupted_JAR", fl),
                                NbBundle.getMessage (EditMediator.class, "LBL_Corrupted_JAR_title"),
                                JOptionPane.WARNING_MESSAGE
                        );
                        continue;
                    }

                // if not in release/modules/ext, copy the JAR there
                Matcher m = checkWrappedJarPat.matcher(fl.getAbsolutePath());
                File prjDir = getProperties().getProjectDirectoryFile();
                if (! m.matches() || ! (new File(m.group(1))).equals(prjDir)) {
                    try {
                        String[] entry = Util.copyClassPathExtensionJar(prjDir, fl);
                        if (entry != null) {
                            // change referenced file to copied one
                            path = entry[1];
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                        continue;
                    }
                }
                // todo - notify available packages

                newPaths.add (path);
            }

            filePaths = newPaths.toArray (new String [newPaths.size ()]);
            int[] newSelection = ClassPathUiSupport.addJarFiles(getProperties().getWrappedJarsListModel(), emListComp.getSelectedIndices(),
                    filePaths, base,
                    chooser.getSelectedPathVariables(), null);
            emListComp.setSelectedIndices( newSelection );
            curDir = FileUtil.normalizeFile(chooser.getCurrentDirectory());
            EditMediator.setLastUsedClassPathFolder(curDir);
        }
    }//GEN-LAST:event_addJarButtonActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        // TODO add your handling code here:
//        getProperties().getPublicPackagesModel().reloadData(getProperties().loadPublicPackages());
//        getProperties().firePropertiesRefreshed();
    }//GEN-LAST:event_exportButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        getProperties().updateAvailablePackages();

    }//GEN-LAST:event_removeButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addDepButton;
    private javax.swing.JButton addJarButton;
    private javax.swing.JButton addLibrary;
    private javax.swing.JButton addTokenButton;
    private javax.swing.JList dependencyList;
    private javax.swing.JScrollPane dependencySP;
    private javax.swing.JButton editButton;
    private javax.swing.JButton editDepButton;
    private javax.swing.JButton exportButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanelJars;
    private javax.swing.JPanel jPanelModules;
    private javax.swing.JPanel jPanelTokens;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton javaPlatformButton;
    private javax.swing.JComboBox javaPlatformCombo;
    private javax.swing.JLabel javaPlatformLabel;
    private javax.swing.JButton managePlafsButton;
    private javax.swing.JLabel moduleDepsLabel;
    private javax.swing.JLabel platform;
    private javax.swing.JComboBox platformValue;
    private javax.swing.JPanel platformsPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton removeDepButton;
    private javax.swing.JButton removeTokenButton;
    private javax.swing.JList reqTokenList;
    private javax.swing.JScrollPane reqTokenSP;
    private javax.swing.JLabel requiredTokensLabel;
    private javax.swing.JList wrappedJarsList;
    private javax.swing.JScrollPane wrappedJarsSP;
    // End of variables declaration//GEN-END:variables
    
    private void initAccessibility() {
        addTokenButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_AddTokenButton"));
        dependencyList.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_DependencyList"));
        editDepButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_EditDepButton"));
        removeDepButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_RemoveDepButton"));
        removeTokenButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_RemoveTokenButton"));
        addDepButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_AddDepButton"));
        reqTokenList.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_ReqTokenList"));
        managePlafsButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_ManagePlafsButton"));
        platformValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_PlatformValue"));
        javaPlatformCombo.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_JavaPlatformCombo"));
        javaPlatformButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_JavaPlatformButton"));
        
        javaPlatformLabel.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_JavaPlatformLbl"));
        platform.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_PlatformLbl"));
    }
    
}

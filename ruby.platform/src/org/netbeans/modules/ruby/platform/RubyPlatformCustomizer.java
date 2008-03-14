/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.ruby.platform;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.io.File;
import java.util.Locale;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.ruby.platform.PlatformComponentFactory.RubyPlatformListModel;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.platform.gems.GemPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class RubyPlatformCustomizer extends JPanel {
    
    private static final String LAST_PLATFORM_DIRECTORY = "lastPlatformDirectory"; // NOI18N
    
    private static String lastSelectedPlatformID;
    
    public static void showCustomizer() {
        RubyPlatformCustomizer customizer = new RubyPlatformCustomizer();
        JButton closeButton = new JButton();
        closeButton.getAccessibleContext().setAccessibleDescription(getMessage("RubyPlatformCustomizer.closeButton.AccessibleContext.accessibleName"));
        Mnemonics.setLocalizedText(closeButton,
                NbBundle.getMessage(RubyPlatformCustomizer.class, "CTL_Close"));
        DialogDescriptor descriptor = new DialogDescriptor(
                customizer,
                getMessage("CTL_RubyPlatformManager_Title"), // NOI18N
                true,
                new Object[] {closeButton},
                closeButton,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(RubyPlatformCustomizer.class),
                null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setVisible(true);
        dlg.dispose();
    }

    public RubyPlatformCustomizer() {
        initComponents();
        getAccessibleContext().setAccessibleName(getMessage("RubyPlatformCustomizer.AccessibleContext.accessibleName"));
        getAccessibleContext().setAccessibleDescription(getMessage("RubyPlatformCustomizer.AccessibleContext.accessibleDescription"));
        refreshPlatformList();
        gemPathList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                removeGemPath.setEnabled(gemPathList.getSelectedValue() != null);
            }
        });
        platformsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                refreshPlatform();
            }
        });

        // run platform detection is this is the first time
        if (Util.isFirstPlatformTouch()) {
            performPlatformDetection();
        } else {
            setAutoDetecting(false);
        }
    }

    public static void manage(JComboBox platforms) {
        RubyPlatformCustomizer.showCustomizer();
        RubyPlatform origPlatform = (RubyPlatform) platforms.getSelectedItem();
        platforms.setModel(new PlatformComponentFactory.RubyPlatformListModel()); // refresh
        platforms.setSelectedItem(origPlatform);
        platforms.requestFocus();
    }

    private RubyPlatform getSelectedPlatform() {
        return (RubyPlatform) platformsList.getSelectedValue();
    }

    private void refreshPlatformList() {
        if (platformsList.getModel().getSize() > 0) {
            platformsList.setSelectedIndex(0);
            if (lastSelectedPlatformID != null) {
                RubyPlatform lastPlaf = RubyPlatformManager.getPlatformByID(lastSelectedPlatformID);
                if (lastPlaf != null) {
                    platformsList.setSelectedValue(lastPlaf, true);
                }
            }
        }
        refreshPlatform();
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(RubyPlatformCustomizer.class, key);
    }

    private void performPlatformDetection() {
        setAutoDetecting(true);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                RubyPlatformManager.performPlatformDetection();
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        platformsList.setModel(new RubyPlatformListModel());
                        refreshPlatformList();
                        setAutoDetecting(false);
                    }
                });
            }
        });
    }

    private void setAutoDetecting(boolean autoDetecting) {
        autoDetectButton.setEnabled(!autoDetecting);
        addButton.setEnabled(!autoDetecting);
        autoDetectProgress.setVisible(autoDetecting);
        autoDetectLabel.setVisible(autoDetecting);
    }

    private void refreshPlatform() {
        RubyPlatform plaf = getSelectedPlatform();

        if (plaf == null) {
            removeButton.setEnabled(false);
            return;
        }
        lastSelectedPlatformID = plaf.getID();
        plfNameValue.setText(plaf.getInfo().getLongDescription());
        plfInterpreterValue.setText(plaf.getInterpreter());
        Color color;
        boolean gemsInstalled = plaf.hasRubyGemsInstalled();
        if (gemsInstalled) {
            gemHomeValue.setText(plaf.getGemManager().getGemHome());
            gemPathList.setModel(createGemPathsModel(plaf));
            gemToolValue.setText(plaf.getGemManager().getGemTool() + " (" + plaf.getInfo().getGemVersion() + ')'); // NOI18N
            color = UIManager.getColor("Label.foreground");
        } else {
            color = PlatformComponentFactory.INVALID_PLAF_COLOR;
            String notInstalledMsg = GemManager.getNotInstalledMessage();
            gemHomeValue.setText(notInstalledMsg);
            gemToolValue.setText(notInstalledMsg);
        }
        browseGemHome.setEnabled(gemsInstalled);
        gemHomeValue.setForeground(color);
        gemToolValue.setForeground(color);
        removeButton.setEnabled(!plaf.isDefault());
        removeGemPath.setEnabled(gemPathList.getSelectedValue() != null);

        refreshDebugger();
    }

    private ListModel createGemPathsModel(final RubyPlatform plaf) {
        return new GemPathsListModel(plaf);
    }

    private static final class GemPathsListModel extends AbstractListModel {

        private RubyPlatform platform;

        GemPathsListModel(final RubyPlatform platform) {
            this.platform = platform;
        }

        public Object getElementAt(int index) {
            return index >= getSize() ? null : getPaths().toArray()[index];
        }

        public int getSize() {
            return getPaths().size();
        }

        void addPath(File repo) {
            platform.getGemManager().addGemPath(repo);
            super.fireIntervalAdded(this, 0, getSize());
        }

        void removePath(File path) {
            platform.getGemManager().removeGemPath(path);
            super.fireIntervalRemoved(this, 0, getSize());
        }

        private Set<File> getPaths() {
            return platform.getGemManager().getGemPath();
        }
    }

    private GemPathsListModel getGemPathListModel() {
        return (GemPathsListModel) gemPathList.getModel();
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        platformsListSP = new javax.swing.JScrollPane();
        platformsList = PlatformComponentFactory.getRubyPlatformsList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        autoDetectButton = new javax.swing.JButton();
        configPanel = new javax.swing.JPanel();
        gemTool = new javax.swing.JLabel();
        gemToolValue = new javax.swing.JTextField();
        gemHome = new javax.swing.JLabel();
        gemHomeValue = new javax.swing.JTextField();
        browseGemHome = new javax.swing.JButton();
        gemPath = new javax.swing.JLabel();
        plfInterpreter = new javax.swing.JLabel();
        plfInterpreterValue = new javax.swing.JTextField();
        plfName = new javax.swing.JLabel();
        plfNameValue = new javax.swing.JTextField();
        gemPathSP = new javax.swing.JScrollPane();
        gemPathList = new javax.swing.JList();
        addGemPath = new javax.swing.JButton();
        removeGemPath = new javax.swing.JButton();
        progressPanel = new javax.swing.JPanel();
        autoDetectLabel = new javax.swing.JLabel();
        autoDetectProgress = new javax.swing.JProgressBar();
        debuggerPanel = new javax.swing.JPanel();
        rubyDebuggerLabel = new javax.swing.JLabel();
        upperSep = new javax.swing.JSeparator();
        engineLabel = new javax.swing.JLabel();
        engineType = new javax.swing.JLabel();
        installFastDebugger = new javax.swing.JButton();

        platformsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        platformsListSP.setViewportView(platformsList);
        platformsList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.platformsList.AccessibleContext.accessibleName")); // NOI18N
        platformsList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.platformsList.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonaddPlatform(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonremovePlatform(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(autoDetectButton, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.autoDetectButton.text")); // NOI18N
        autoDetectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoDetectButtonremovePlatform(evt);
            }
        });

        gemTool.setLabelFor(gemToolValue);
        org.openide.awt.Mnemonics.setLocalizedText(gemTool, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.gemTool.text")); // NOI18N

        gemToolValue.setEditable(false);

        gemHome.setLabelFor(gemHomeValue);
        org.openide.awt.Mnemonics.setLocalizedText(gemHome, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.gemHome.text")); // NOI18N

        gemHomeValue.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(browseGemHome, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.browseGemHome.text")); // NOI18N
        browseGemHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseGemHomeActionPerformed(evt);
            }
        });

        gemPath.setLabelFor(gemPathList);
        org.openide.awt.Mnemonics.setLocalizedText(gemPath, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.gemPath.text")); // NOI18N

        plfInterpreter.setLabelFor(plfInterpreterValue);
        org.openide.awt.Mnemonics.setLocalizedText(plfInterpreter, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.plfInterpreter.text")); // NOI18N

        plfInterpreterValue.setEditable(false);

        plfName.setLabelFor(plfNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(plfName, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.plfName.text")); // NOI18N

        plfNameValue.setEditable(false);

        gemPathSP.setViewportView(gemPathList);
        gemPathList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.gemPathList.AccessibleContext.accessibleName")); // NOI18N
        gemPathList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.gemPathList.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addGemPath, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.addGemPath.text")); // NOI18N
        addGemPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addGemPathActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeGemPath, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.removeGemPath.text")); // NOI18N
        removeGemPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeGemPathActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout configPanelLayout = new org.jdesktop.layout.GroupLayout(configPanel);
        configPanel.setLayout(configPanelLayout);
        configPanelLayout.setHorizontalGroup(
            configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(configPanelLayout.createSequentialGroup()
                .add(configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(plfInterpreter)
                    .add(plfName)
                    .add(gemHome)
                    .add(gemPath)
                    .add(gemTool))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(configPanelLayout.createSequentialGroup()
                        .add(configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(gemHomeValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                            .add(gemPathSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(removeGemPath)
                            .add(addGemPath)
                            .add(browseGemHome)))
                    .add(plfInterpreterValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                    .add(plfNameValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                    .add(gemToolValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)))
        );

        configPanelLayout.linkSize(new java.awt.Component[] {gemHome, gemTool, plfInterpreter, plfName}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        configPanelLayout.linkSize(new java.awt.Component[] {addGemPath, browseGemHome, removeGemPath}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        configPanelLayout.setVerticalGroup(
            configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(configPanelLayout.createSequentialGroup()
                .add(configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(plfName)
                    .add(plfNameValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(7, 7, 7)
                .add(configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(plfInterpreter)
                    .add(plfInterpreterValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(gemHome)
                    .add(gemHomeValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseGemHome))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(gemPath)
                    .add(gemPathSP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(configPanelLayout.createSequentialGroup()
                        .add(addGemPath)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeGemPath)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(gemTool)
                    .add(gemToolValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        gemToolValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.gemToolValue.AccessibleContext.accessibleDescription")); // NOI18N
        gemHomeValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.gemHomeValue.AccessibleContext.accessibleDescription")); // NOI18N
        browseGemHome.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.browseGemHome.AccessibleContext.accessibleDescription")); // NOI18N
        plfInterpreterValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.plfInterpreterValue.AccessibleContext.accessibleDescription")); // NOI18N
        plfNameValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.plfNameValue.AccessibleContext.accessibleDescription")); // NOI18N
        addGemPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.addGemPath.AccessibleContext.accessibleDescription")); // NOI18N
        removeGemPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.removeGemPath.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(autoDetectLabel, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.autoDetectLabel.text")); // NOI18N
        progressPanel.add(autoDetectLabel);

        autoDetectProgress.setIndeterminate(true);
        progressPanel.add(autoDetectProgress);

        org.openide.awt.Mnemonics.setLocalizedText(rubyDebuggerLabel, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.rubyDebuggerLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(engineLabel, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.engineLabel.text")); // NOI18N

        engineType.setFont(new java.awt.Font("Dialog", 2, 12));
        org.openide.awt.Mnemonics.setLocalizedText(engineType, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.classicDebuggerEngine.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(installFastDebugger, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.installFastDebugger.text")); // NOI18N
        installFastDebugger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installFastDebuggerActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout debuggerPanelLayout = new org.jdesktop.layout.GroupLayout(debuggerPanel);
        debuggerPanel.setLayout(debuggerPanelLayout);
        debuggerPanelLayout.setHorizontalGroup(
            debuggerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(debuggerPanelLayout.createSequentialGroup()
                .add(rubyDebuggerLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(upperSep, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE))
            .add(debuggerPanelLayout.createSequentialGroup()
                .add(engineLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(engineType)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(installFastDebugger)
                .add(178, 178, 178))
        );
        debuggerPanelLayout.setVerticalGroup(
            debuggerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(debuggerPanelLayout.createSequentialGroup()
                .add(debuggerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(rubyDebuggerLabel)
                    .add(upperSep, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(debuggerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(engineLabel)
                    .add(engineType)
                    .add(installFastDebugger))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        installFastDebugger.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.installFastDebugger.AccessibleContext.accessibleDescription")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(platformsListSP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 235, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(debuggerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(configPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(autoDetectButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 151, Short.MAX_VALUE)
                        .add(progressPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(platformsListSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(layout.createSequentialGroup()
                        .add(configPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(debuggerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(addButton)
                        .add(removeButton)
                        .add(autoDetectButton))
                    .add(progressPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {addButton, autoDetectButton, progressPanel, removeButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.addButton.AccessibleContext.accessibleDescription")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.removeButton.AccessibleContext.accessibleDescription")); // NOI18N
        autoDetectButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.autoDetectButton.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private PlatformComponentFactory.RubyPlatformListModel getPlafListModel() {
        return (PlatformComponentFactory.RubyPlatformListModel) platformsList.getModel();
    }

    private void addButtonaddPlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonaddPlatform
        JFileChooser chooser = new JFileChooser(Util.getPreferences().get(LAST_PLATFORM_DIRECTORY, ""));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f)  {
                return f.isDirectory() || (f.isFile() && f.getName().toLowerCase(Locale.US).contains("ruby")); // NOI18N
            }
            public String getDescription() {
                return getMessage("RubyPlatformCustomizer.rubyPlatform");
            }
        });
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File intepreter = FileUtil.normalizeFile(chooser.getSelectedFile());
            Util.getPreferences().put(LAST_PLATFORM_DIRECTORY, intepreter.getParentFile().getAbsolutePath());
            RubyPlatform platform = getPlafListModel().addPlatform(intepreter);
            if (platform == null) {
                Util.notifyLocalized(RubyPlatformCustomizer.class,
                        "RubyPlatformCustomizer.invalid.platform.added", intepreter.getAbsolutePath()); // NOI18N
            } else {
                refreshPlatform();
            }
        }
    }//GEN-LAST:event_addButtonaddPlatform

    private void removeButtonremovePlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonremovePlatform
        RubyPlatform plaf = getSelectedPlatform();
        if (plaf != null) {
            getPlafListModel().removePlatform(plaf);
            platformsList.setSelectedValue(RubyPlatformManager.getDefaultPlatform(), true);
            refreshPlatform();
        }
    }//GEN-LAST:event_removeButtonremovePlatform

    private void autoDetectButtonremovePlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoDetectButtonremovePlatform
        performPlatformDetection();
}//GEN-LAST:event_autoDetectButtonremovePlatform

    private void installFastDebuggerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installFastDebuggerActionPerformed
        if (getSelectedPlatform().installFastDebugger()) {
            refreshDebugger();
        }
    }//GEN-LAST:event_installFastDebuggerActionPerformed

    private void browseGemHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseGemHomeActionPerformed
        boolean changed = GemPanel.chooseAndSetGemHome(this, getSelectedPlatform());
        if (changed) {
            refreshPlatform();
        }
    }//GEN-LAST:event_browseGemHomeActionPerformed

    private void addGemPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addGemPathActionPerformed
        File repo = GemPanel.chooseGemRepository(this);
        if (repo != null) {
            String absPath = repo.getAbsolutePath();
            if (!getGemPathListModel().getPaths().contains(absPath)) {
                getGemPathListModel().addPath(repo);
                refreshPlatform();
                gemPathList.requestFocus();
                gemPathList.setSelectedValue(absPath, true);
            }
        }
}//GEN-LAST:event_addGemPathActionPerformed

    private void removeGemPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeGemPathActionPerformed
        getGemPathListModel().removePath((File) gemPathList.getSelectedValue());
        refreshPlatform();
        if (getGemPathListModel().getSize() > 0) {
            gemPathList.setSelectedIndex(0);
        }
        gemPathList.requestFocus();
}//GEN-LAST:event_removeGemPathActionPerformed

    private void refreshDebugger() {
        RubyPlatform platform = getSelectedPlatform();
        boolean isJRuby = platform.isJRuby();
        boolean fdInstalled = platform.hasFastDebuggerInstalled();
        installFastDebugger.setEnabled(!isJRuby && platform.hasRubyGemsInstalled());
        installFastDebugger.setVisible(!isJRuby && !fdInstalled);
        String key = platform.hasFastDebuggerInstalled()
                ? "RubyPlatformCustomizer.rubyDebugEngine.text" // NOI18N
                : "RubyPlatformCustomizer.classicDebuggerEngine.text"; // NOI18N
        engineType.setText(getMessage(key));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addGemPath;
    private javax.swing.JButton autoDetectButton;
    private javax.swing.JLabel autoDetectLabel;
    private javax.swing.JProgressBar autoDetectProgress;
    private javax.swing.JButton browseGemHome;
    private javax.swing.JPanel configPanel;
    private javax.swing.JPanel debuggerPanel;
    private javax.swing.JLabel engineLabel;
    private javax.swing.JLabel engineType;
    private javax.swing.JLabel gemHome;
    private javax.swing.JTextField gemHomeValue;
    private javax.swing.JLabel gemPath;
    private javax.swing.JList gemPathList;
    private javax.swing.JScrollPane gemPathSP;
    private javax.swing.JLabel gemTool;
    private javax.swing.JTextField gemToolValue;
    private javax.swing.JButton installFastDebugger;
    private javax.swing.JList platformsList;
    private javax.swing.JScrollPane platformsListSP;
    private javax.swing.JLabel plfInterpreter;
    private javax.swing.JTextField plfInterpreterValue;
    private javax.swing.JLabel plfName;
    private javax.swing.JTextField plfNameValue;
    private javax.swing.JPanel progressPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton removeGemPath;
    private javax.swing.JLabel rubyDebuggerLabel;
    private javax.swing.JSeparator upperSep;
    // End of variables declaration//GEN-END:variables
}

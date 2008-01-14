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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.ruby.platform.PlatformComponentFactory.RubyPlatformListModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

public class RubyPlatformCustomizer extends javax.swing.JPanel {
    
    private static final String LAST_PLATFORM_DIRECTORY = "lastPlatformDirectory"; // NOI18N
    private static final String FIRST_TIME_KEY = "platform-manager-called-first-time"; // NOI18N
    
    private static final Color INVALID_PLAF_COLOR = UIManager.getColor("nb.errorForeground"); // NOI18N

    public static void showCustomizer() {
        RubyPlatformCustomizer customizer = new RubyPlatformCustomizer();
        JButton closeButton = new JButton();
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
        refreshPlatformList();
        platformsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                refreshPlatform();
            }
        });

        // run platform detection is this is the first time
        Preferences preferences = NbPreferences.forModule(RubyPlatformCustomizer.class);
        if (preferences.getBoolean(FIRST_TIME_KEY, true)) {
            performPlatformDetection();
            preferences.putBoolean(FIRST_TIME_KEY, false);
        }
    }

    private void refreshPlatformList() {
        if (platformsList.getModel().getSize() > 0) {
            platformsList.setSelectedIndex(0);
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
        RubyPlatform plaf = (RubyPlatform) platformsList.getSelectedValue();

        if (plaf == null) {
            removeButton.setEnabled(false);
            return;
        }
        plfNameValue.setText(plaf.getInfo().getLongDescription());
        plfInterpreterValue.setText(plaf.getInterpreter());
        Color color;
        if (plaf.hasRubyGemsInstalled()) {
            gemHomeValue.setText(plaf.getGemManager().getGemDir());
            gemToolValue.setText(plaf.getGemManager().getGemTool() + " (" + plaf.getInfo().getGemVersion() + ')'); // NOI18N
            color = UIManager.getColor("Label.foreground");
        } else {
            color = INVALID_PLAF_COLOR;
            String notInstalled = NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.rubyGemsNotInstalled");
            gemHomeValue.setText(notInstalled);
            gemToolValue.setText(notInstalled);
        }
        gemHomeValue.setForeground(color);
        gemToolValue.setForeground(color);
        removeButton.setEnabled(!plaf.isDefault());
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
        plfName = new javax.swing.JLabel();
        plfNameValue = new javax.swing.JTextField();
        plfInterpreter = new javax.swing.JLabel();
        plfInterpreterValue = new javax.swing.JTextField();
        gemHome = new javax.swing.JLabel();
        gemHomeValue = new javax.swing.JTextField();
        gemTool = new javax.swing.JLabel();
        gemToolValue = new javax.swing.JTextField();
        autoDetectButton = new javax.swing.JButton();
        autoDetectLabel = new javax.swing.JLabel();
        autoDetectProgress = new javax.swing.JProgressBar();

        platformsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        platformsListSP.setViewportView(platformsList);

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

        plfName.setLabelFor(plfNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(plfName, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.plfName.text")); // NOI18N

        plfNameValue.setEditable(false);

        plfInterpreter.setLabelFor(plfInterpreterValue);
        org.openide.awt.Mnemonics.setLocalizedText(plfInterpreter, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.plfInterpreter.text")); // NOI18N

        plfInterpreterValue.setEditable(false);

        gemHome.setLabelFor(gemHomeValue);
        org.openide.awt.Mnemonics.setLocalizedText(gemHome, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.gemHome.text")); // NOI18N

        gemHomeValue.setEditable(false);

        gemTool.setLabelFor(gemToolValue);
        org.openide.awt.Mnemonics.setLocalizedText(gemTool, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.gemTool.text")); // NOI18N

        gemToolValue.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(autoDetectButton, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.autoDetectButton.text")); // NOI18N
        autoDetectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoDetectButtonremovePlatform(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(autoDetectLabel, org.openide.util.NbBundle.getMessage(RubyPlatformCustomizer.class, "RubyPlatformCustomizer.autoDetectLabel.text")); // NOI18N

        autoDetectProgress.setIndeterminate(true);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(platformsListSP, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 235, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(plfName)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(plfNameValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(plfInterpreter, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(plfInterpreterValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(gemHome, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(gemHomeValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(gemTool, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(gemToolValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(autoDetectButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 216, Short.MAX_VALUE)
                        .add(autoDetectLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(autoDetectProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {gemHome, gemTool, plfInterpreter, plfName}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(12, 12, 12)
                .add(platformsListSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addButton)
                    .add(removeButton)
                    .add(autoDetectButton))
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(plfNameValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(plfName))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(plfInterpreterValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(plfInterpreter))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(gemHomeValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(gemHome))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(gemToolValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(gemTool))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 211, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(autoDetectProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(autoDetectLabel))
                .add(20, 20, 20))
        );
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
                return "Ruby Platform"; //getMessage("CTL_JavadocTab");
            }
        });
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File intepreter = FileUtil.normalizeFile(chooser.getSelectedFile());
            Util.getPreferences().put(LAST_PLATFORM_DIRECTORY, intepreter.getParentFile().getAbsolutePath());
            // XXX store last used directory into the settings
            getPlafListModel().addPlatform(intepreter);
            refreshPlatform();
        }
    }//GEN-LAST:event_addButtonaddPlatform

    private void removeButtonremovePlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonremovePlatform
        RubyPlatform plaf = (RubyPlatform) platformsList.getSelectedValue();
        if (plaf != null) {
            getPlafListModel().removePlatform(plaf);
            platformsList.setSelectedValue(RubyPlatformManager.getDefaultPlatform(), true);
            refreshPlatform();
        }
    }//GEN-LAST:event_removeButtonremovePlatform

    private void autoDetectButtonremovePlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoDetectButtonremovePlatform
        performPlatformDetection();
}//GEN-LAST:event_autoDetectButtonremovePlatform

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton autoDetectButton;
    private javax.swing.JLabel autoDetectLabel;
    private javax.swing.JProgressBar autoDetectProgress;
    private javax.swing.JLabel gemHome;
    private javax.swing.JTextField gemHomeValue;
    private javax.swing.JLabel gemTool;
    private javax.swing.JTextField gemToolValue;
    private javax.swing.JList platformsList;
    private javax.swing.JScrollPane platformsListSP;
    private javax.swing.JLabel plfInterpreter;
    private javax.swing.JTextField plfInterpreterValue;
    private javax.swing.JLabel plfName;
    private javax.swing.JTextField plfNameValue;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}

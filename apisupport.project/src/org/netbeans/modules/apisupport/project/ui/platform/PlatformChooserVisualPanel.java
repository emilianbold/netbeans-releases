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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.apisupport.project.ui.ModuleUISettings;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicVisualPanel;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * First panel from <em>Adding New Platform</em> wizard panels. Allows user to
 * choose platform directory.
 *
 * @author Martin Krauskopf
 */
public class PlatformChooserVisualPanel extends BasicVisualPanel
        implements PropertyChangeListener {
    
    /** Creates new form BasicInfoVisualPanel */
    public PlatformChooserVisualPanel(WizardDescriptor setting) {
        super(setting);
        initComponents();
        initAccessibility();
        String location = ModuleUISettings.getDefault().getLastUsedNbPlatformLocation();
        if (location != null) {
            platformChooser.setCurrentDirectory(new File(location));
        }
        platformChooser.setAcceptAllFileFilterUsed(false);
        platformChooser.setFileFilter(new FileFilter() {
            public boolean accept(File f)  {
                return f.isDirectory();
            }
            public String getDescription() {
                return getMessage("CTL_PlatformFolder");
            }
        });
        platformChooser.addPropertyChangeListener(this);
        setName(NbPlatformCustomizer.CHOOSER_STEP);
        platformChooser.putClientProperty(
                "JFileChooser.appBundleIsTraversable", "always"); // NOI18N #73124
    }

    public void addNotify() {
        super.addNotify();
        checkForm();
    }
    
    /** Stores collected data into model. */
    void storeData() {
        File file = platformChooser.getSelectedFile();
        if (file != null) {
            getSettings().putProperty(NbPlatformCustomizer.PLAF_DIR_PROPERTY,
                    file.getAbsolutePath());
            getSettings().putProperty(NbPlatformCustomizer.PLAF_LABEL_PROPERTY,
                    plafLabelValue.getText());
        } // when wizard is cancelled file is null
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (propName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            checkForm();
        }
    }
    
    private void checkForm() {
        File selFile = platformChooser.getSelectedFile();
        boolean invalid = true;
        if (selFile != null) { // #73123
            File plafDir = FileUtil.normalizeFile(selFile);
            if (/* #60133 */ plafDir != null && NbPlatform.isPlatformDirectory(plafDir)) {
                try {
                    setPlafLabel(NbPlatform.computeDisplayName(plafDir));
                } catch (IOException e) {
                    setPlafLabel(plafDir.getAbsolutePath());
                }
                if (!NbPlatform.isSupportedPlatform(plafDir)) {
                    setError(getMessage("MSG_UnsupportedPlatform"));
                } else if (NbPlatform.contains(plafDir)) {
                    setError(getMessage("MSG_AlreadyAddedPlatform"));
                } else if (!NbPlatform.isLabelValid(plafLabelValue.getText())) {
                    setWarning(getMessage("MSG_NameIsAlreadyUsedGoToNext"));
                } else {
                    markValid();
                    ModuleUISettings.getDefault().setLastUsedNbPlatformLocation(plafDir.getParentFile().getAbsolutePath());
                }
                invalid = false;
            }
        }
        if (invalid) {
            markInvalid();
            setPlafLabel(null);
            storeData();
        }
    }
    
    private void setPlafLabel(String label) {
        plafLabelValue.setText(label);
        plafLabelValue.setCaretPosition(0);
        storeData();
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(PlatformChooserVisualPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        infoPanel = new javax.swing.JPanel();
        inner = new javax.swing.JPanel();
        plafLabel = new javax.swing.JLabel();
        plafLabelValue = new javax.swing.JTextField();
        platformChooser = new javax.swing.JFileChooser();

        infoPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));

        inner.setLayout(new java.awt.GridLayout(2, 1, 0, 6));

        plafLabel.setLabelFor(plafLabelValue);
        org.openide.awt.Mnemonics.setLocalizedText(plafLabel, org.openide.util.NbBundle.getMessage(PlatformChooserVisualPanel.class, "LBL_PlatformName_P"));
        inner.add(plafLabel);

        plafLabelValue.setColumns(15);
        plafLabelValue.setEditable(false);
        inner.add(plafLabelValue);

        infoPanel.add(inner);

        setLayout(new java.awt.BorderLayout());

        platformChooser.setAccessory(infoPanel);
        platformChooser.setControlButtonsAreShown(false);
        platformChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        add(platformChooser, java.awt.BorderLayout.CENTER);

    }
    // </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel infoPanel;
    private javax.swing.JPanel inner;
    private javax.swing.JLabel plafLabel;
    private javax.swing.JTextField plafLabelValue;
    private javax.swing.JFileChooser platformChooser;
    // End of variables declaration//GEN-END:variables
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_PlatformChooserVisualPanel"));
        plafLabelValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_plafLabelValue"));
    }
    
}

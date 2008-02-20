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

package org.netbeans.modules.j2ee.ejbjarproject.ui.wizards;

import javax.swing.JPanel;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2ee.common.project.ui.UserProjectSettings;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.modules.SpecificationVersion;

/**
 * Displays a warning that the project's Java platform will be set to JDK 1.4 or
 * the source level will be set to 1.4. See issue #55797.
 *
 * @author Andrei Badea
 */
public final class J2eeVersionWarningPanel extends JPanel {
    private static final long serialVersionUID = 11195574181067260L;
    
    /**
     * Display a warning that the target platform will be downgraded to JDK 1.4
     */
    public final static String WARN_SET_JDK_14 = "warnSetJdk14"; // NOI18N
    
    /**
     * Display a warning that the target platform will be upgraded to JDK 1.5
     */
    public final static String WARN_SET_JDK_15 = "warnSetJdk15"; // NOI18N
    
    /**
     * Display a warning that the source level will be downgraded to 1.4
     */
    public final static String WARN_SET_SOURCE_LEVEL_14 = "warnSetSourceLevel14"; // NOI18N
    
    /**
     * Display a warning that the source level will be upgraded to 1.5
     */
    public final static String WARN_SET_SOURCE_LEVEL_15 = "warnSetSourceLevel15"; // NOI18N
    
    private String warningType;
    private String javaPlatformName;
    
    public J2eeVersionWarningPanel(String warningType) {
        initComponents();
        setWarningType(warningType);
    }
    
    public String getWarningType() {
        return warningType;
    }
    
    public void setWarningType(String warningType) {
        String warningText = null;
        String checkBoxText = null;
        
        this.warningType = warningType;
        
        setJdk14Panel.setVisible(false);
        setSourceLevel14Panel.setVisible(false);
        setJdk15Panel.setVisible(false);
        setSourceLevel15Panel.setVisible(false);
        
        if (WARN_SET_JDK_14.equals(warningType)) {
            setJdk14Panel.setVisible(true);
            downgradeJdk14CheckBox.setSelected(UserProjectSettings.getDefault().isAgreedSetJdk14());
        } else if (WARN_SET_SOURCE_LEVEL_14.equals(warningType)) {
            setSourceLevel14Panel.setVisible(true);
            downgradeSourceLevel14CheckBox.setSelected(UserProjectSettings.getDefault().isAgreedSetSourceLevel14());
        } else if (WARN_SET_JDK_15.equals(warningType)) {
            setJdk15Panel.setVisible(true);
            downgradeJdk15CheckBox.setSelected(UserProjectSettings.getDefault().isAgreedSetJdk15());
        } else if (WARN_SET_SOURCE_LEVEL_15.equals(warningType)) {
            setSourceLevel15Panel.setVisible(true);
            downgradeSourceLevel15CheckBox.setSelected(UserProjectSettings.getDefault().isAgreedSetSourceLevel15());
        }
    }
    
    public boolean getDowngradeAllowed() {
        if (WARN_SET_JDK_14.equals(warningType)) {
            return downgradeJdk14CheckBox.isSelected();
        } else if (WARN_SET_SOURCE_LEVEL_14.equals(warningType)) {
            return downgradeSourceLevel14CheckBox.isSelected();
        } else if (WARN_SET_JDK_15.equals(warningType)) {
            return downgradeJdk15CheckBox.isSelected();
        } else if (WARN_SET_SOURCE_LEVEL_15.equals(warningType)) {
            return downgradeSourceLevel15CheckBox.isSelected();
        }
        return false;
    }
    
    public String getSuggestedJavaPlatformName() {
        if (javaPlatformName == null && WARN_SET_JDK_14.equals(warningType) ) {
            JavaPlatform[] java14Platforms = getJavaPlatforms("1.4");
            javaPlatformName = java14Platforms[0].getDisplayName();
        }
        if (javaPlatformName == null && WARN_SET_JDK_15.equals(warningType) ) {
            JavaPlatform[] java14Platforms = getJavaPlatforms("1.5");
            javaPlatformName = java14Platforms[0].getDisplayName();
        }
        return javaPlatformName;
    }
    
    public static String findWarningType(String j2eeLevel) {
//        System.out.println("findWarningType: j2eeLevel="+j2eeLevel);
        JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        SpecificationVersion version = defaultPlatform.getSpecification().getVersion();
        String sourceLevel = version.toString();
        // #89131: these levels are not actually distinct from 1.5.
        if (sourceLevel.equals("1.6") || sourceLevel.equals("1.7")) {
            sourceLevel = "1.5";
        }
//        System.out.println("default platform is "+version);
        
        // no warning if 1.4 is the default for j2ee14
        if (new SpecificationVersion("1.4").equals(version) && j2eeLevel.equals(J2eeModule.J2EE_14)) { // NOI18N
            return null;
        }
        
        // no warning if 1.5, 1.6, 1.7 is the default for j2ee15
        if ("1.5".equals(sourceLevel) && j2eeLevel.equals(J2eeModule.JAVA_EE_5)) { // NOI18N
            return null;
        }
        
        if (j2eeLevel.equals(J2eeModule.J2EE_14)) {
            JavaPlatform[] java14Platforms = getJavaPlatforms("1.4"); //NOI18N
            if (java14Platforms.length > 0) {
                // the user has JDK 1.4, so we warn we'll downgrade to 1.4
                return WARN_SET_JDK_14;
            } else {
                // no JDK 1.4, the best we can do is downgrade the source level to 1.4
                return WARN_SET_SOURCE_LEVEL_14;
            }
        } else {
            assert j2eeLevel.equals(J2eeModule.JAVA_EE_5);
            JavaPlatform[] java15Platforms = getJavaPlatforms("1.5"); //NOI18N
            if (java15Platforms.length > 0) {
                // the user has JDK 1.4, so we warn we'll downgrade to 1.4
                return WARN_SET_JDK_15;
            } else {
                // no JDK 1.4, the best we can do is downgrade the source level to 1.4
                return WARN_SET_SOURCE_LEVEL_15;
            }
        }
    }
    
    private static JavaPlatform[] getJavaPlatforms(String level) {
        return JavaPlatformManager.getDefault().getPlatforms(null, new Specification("J2SE", new SpecificationVersion(level))); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setJdk14Panel = new javax.swing.JPanel();
        warningJdk14TextArea = new javax.swing.JTextArea();
        downgradeJdk14CheckBox = new javax.swing.JCheckBox();
        setSourceLevel15Panel = new javax.swing.JPanel();
        warningSourceLevel15TextArea = new javax.swing.JTextArea();
        downgradeSourceLevel15CheckBox = new javax.swing.JCheckBox();
        setJdk15Panel = new javax.swing.JPanel();
        warningJdk15TextArea = new javax.swing.JTextArea();
        downgradeJdk15CheckBox = new javax.swing.JCheckBox();
        setSourceLevel14Panel = new javax.swing.JPanel();
        warningSourceLevel14TextArea = new javax.swing.JTextArea();
        downgradeSourceLevel14CheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        setJdk14Panel.setLayout(new java.awt.GridBagLayout());

        warningJdk14TextArea.setEditable(false);
        warningJdk14TextArea.setLineWrap(true);
        warningJdk14TextArea.setText(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("MSG_RecommendationSetJdk14")); // NOI18N
        warningJdk14TextArea.setWrapStyleWord(true);
        warningJdk14TextArea.setFocusable(false);
        warningJdk14TextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        setJdk14Panel.add(warningJdk14TextArea, gridBagConstraints);
        warningJdk14TextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "ACSN_RecommendationSetJdk14")); // NOI18N
        warningJdk14TextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "ACSD_RecommendationSetJdk14")); // NOI18N

        downgradeJdk14CheckBox.setMnemonic(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "MNE_AgreeSetJdk14").charAt(0));
        downgradeJdk14CheckBox.setText(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetJdk14")); // NOI18N
        downgradeJdk14CheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downgradeJdk14CheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        setJdk14Panel.add(downgradeJdk14CheckBox, gridBagConstraints);
        downgradeJdk14CheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("ACS_AgreeSetJdk14")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(setJdk14Panel, gridBagConstraints);

        setSourceLevel15Panel.setLayout(new java.awt.GridBagLayout());

        warningSourceLevel15TextArea.setEditable(false);
        warningSourceLevel15TextArea.setLineWrap(true);
        warningSourceLevel15TextArea.setText(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("MSG_RecommendationSetSourceLevel15")); // NOI18N
        warningSourceLevel15TextArea.setWrapStyleWord(true);
        warningSourceLevel15TextArea.setFocusable(false);
        warningSourceLevel15TextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        setSourceLevel15Panel.add(warningSourceLevel15TextArea, gridBagConstraints);
        warningSourceLevel15TextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "ACSN_RecommendationSetSourceLevel15")); // NOI18N
        warningSourceLevel15TextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "ACSD_RecommendationSetSourceLevel15")); // NOI18N

        downgradeSourceLevel15CheckBox.setMnemonic(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "MNE_AgreeSetSourceLevel15").charAt(0));
        downgradeSourceLevel15CheckBox.setText(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetSourceLevel15")); // NOI18N
        downgradeSourceLevel15CheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downgradeSourceLevel15CheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        setSourceLevel15Panel.add(downgradeSourceLevel15CheckBox, gridBagConstraints);
        downgradeSourceLevel15CheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("ACS_AgreeSetSourceLevel15")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(setSourceLevel15Panel, gridBagConstraints);

        setJdk15Panel.setLayout(new java.awt.GridBagLayout());

        warningJdk15TextArea.setEditable(false);
        warningJdk15TextArea.setLineWrap(true);
        warningJdk15TextArea.setText(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("MSG_RecommendationSetJdk15")); // NOI18N
        warningJdk15TextArea.setWrapStyleWord(true);
        warningJdk15TextArea.setFocusable(false);
        warningJdk15TextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        setJdk15Panel.add(warningJdk15TextArea, gridBagConstraints);
        warningJdk15TextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "ACSN_RecommendationSetJdk15")); // NOI18N
        warningJdk15TextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "ACSD_RecommendationSetJdk15")); // NOI18N

        downgradeJdk15CheckBox.setMnemonic(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "MNE_AgreeSetJdk15").charAt(0));
        downgradeJdk15CheckBox.setText(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetJdk15")); // NOI18N
        downgradeJdk15CheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downgradeJdk15CheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        setJdk15Panel.add(downgradeJdk15CheckBox, gridBagConstraints);
        downgradeJdk15CheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("ACS_AgreeSetJdk15")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(setJdk15Panel, gridBagConstraints);

        setSourceLevel14Panel.setLayout(new java.awt.GridBagLayout());

        warningSourceLevel14TextArea.setEditable(false);
        warningSourceLevel14TextArea.setLineWrap(true);
        warningSourceLevel14TextArea.setText(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("MSG_RecommendationSetSourceLevel14")); // NOI18N
        warningSourceLevel14TextArea.setWrapStyleWord(true);
        warningSourceLevel14TextArea.setFocusable(false);
        warningSourceLevel14TextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        setSourceLevel14Panel.add(warningSourceLevel14TextArea, gridBagConstraints);
        warningSourceLevel14TextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "ACSN_LabelSetSourceLevel14")); // NOI18N
        warningSourceLevel14TextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "ACSD_LabelSetSourceLevel14")); // NOI18N

        downgradeSourceLevel14CheckBox.setMnemonic(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "MNE_AgreeSetSourceLevel14").charAt(0));
        downgradeSourceLevel14CheckBox.setText(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetSourceLevel14")); // NOI18N
        downgradeSourceLevel14CheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downgradeSourceLevel14CheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        setSourceLevel14Panel.add(downgradeSourceLevel14CheckBox, gridBagConstraints);
        downgradeSourceLevel14CheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("ACS_AgreeSetSourceLevel14")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(setSourceLevel14Panel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void downgradeSourceLevel15CheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downgradeSourceLevel15CheckBoxActionPerformed
        UserProjectSettings.getDefault().setAgreedSetSourceLevel15(downgradeSourceLevel15CheckBox.isSelected());
    }//GEN-LAST:event_downgradeSourceLevel15CheckBoxActionPerformed

    private void downgradeJdk15CheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downgradeJdk15CheckBoxActionPerformed
        UserProjectSettings.getDefault().setAgreedSetJdk15(downgradeJdk15CheckBox.isSelected());
    }//GEN-LAST:event_downgradeJdk15CheckBoxActionPerformed

    private void downgradeSourceLevel14CheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downgradeSourceLevel14CheckBoxActionPerformed
        UserProjectSettings.getDefault().setAgreedSetSourceLevel14(downgradeSourceLevel14CheckBox.isSelected());
    }//GEN-LAST:event_downgradeSourceLevel14CheckBoxActionPerformed

    private void downgradeJdk14CheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downgradeJdk14CheckBoxActionPerformed
        UserProjectSettings.getDefault().setAgreedSetJdk14(downgradeJdk14CheckBox.isSelected());
    }//GEN-LAST:event_downgradeJdk14CheckBoxActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox downgradeJdk14CheckBox;
    private javax.swing.JCheckBox downgradeJdk15CheckBox;
    private javax.swing.JCheckBox downgradeSourceLevel14CheckBox;
    private javax.swing.JCheckBox downgradeSourceLevel15CheckBox;
    private javax.swing.JPanel setJdk14Panel;
    private javax.swing.JPanel setJdk15Panel;
    private javax.swing.JPanel setSourceLevel14Panel;
    private javax.swing.JPanel setSourceLevel15Panel;
    private javax.swing.JTextArea warningJdk14TextArea;
    private javax.swing.JTextArea warningJdk15TextArea;
    private javax.swing.JTextArea warningSourceLevel14TextArea;
    private javax.swing.JTextArea warningSourceLevel15TextArea;
    // End of variables declaration//GEN-END:variables
    
}


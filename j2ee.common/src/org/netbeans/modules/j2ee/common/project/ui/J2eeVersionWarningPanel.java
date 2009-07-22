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

package org.netbeans.modules.j2ee.common.project.ui;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.common.project.ui.UserProjectSettings;
import org.netbeans.api.j2ee.core.Profile;
import org.openide.modules.SpecificationVersion;

/**
 * Displays a warning that the project's Java platform will be set to JDK 1.4 or
 * the source level will be set to 1.4. See issue #55797.
 *
 * @author Andrei Badea
 */
final class J2eeVersionWarningPanel extends javax.swing.JPanel {
    
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
        } else return false;
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

    public static String findWarningType(Profile j2eeProfile) {
//        System.out.println("findWarningType: j2eeLevel="+j2eeLevel);
        JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        SpecificationVersion version = defaultPlatform.getSpecification().getVersion();
        String sourceLevel = version.toString();
        // #89131: these levels are not actually distinct from 1.5.
        if (sourceLevel.equals("1.6") || sourceLevel.equals("1.7"))
            sourceLevel = "1.5";
//        System.out.println("default platform is "+version);

        // no warning if 1.4 is the default for j2ee14
        if (new SpecificationVersion("1.4").equals(version) && j2eeProfile == Profile.J2EE_14) // NOI18N
            return null;

        // no warning if 1.5, 1.6, 1.7 is the default for j2ee15
        if ("1.5".equals(sourceLevel) && j2eeProfile == Profile.JAVA_EE_5) // NOI18N
            return null;

        if (j2eeProfile == Profile.J2EE_14) {
            JavaPlatform[] java14Platforms = getJavaPlatforms("1.4"); //NOI18N
            if (java14Platforms.length > 0) {
                // the user has JDK 1.4, so we warn we'll downgrade to 1.4
                return WARN_SET_JDK_14;
            } else {
                // no JDK 1.4, the best we can do is downgrade the source level to 1.4
                return WARN_SET_SOURCE_LEVEL_14;
            }
        } else if (j2eeProfile == Profile.JAVA_EE_5) {
            JavaPlatform[] java15Platforms = getJavaPlatforms("1.5"); //NOI18N
            if (java15Platforms.length > 0) {
                // the user has JDK 1.4, so we warn we'll downgrade to 1.4
                return WARN_SET_JDK_15;
            } else {
                // no JDK 1.4, the best we can do is downgrade the source level to 1.4
                return WARN_SET_SOURCE_LEVEL_15;
            }
        } else {
            //e.g. 1.3, javaee6, etc. - better ignore then assert
            return null;
        }
    }

    @Deprecated
    public static String findWarningType(String j2eeLevel) {
        return findWarningType(Profile.fromPropertiesString(j2eeLevel));
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

        setJdk14Panel = new javax.swing.JPanel();
        downgradeJdk14CheckBox = new javax.swing.JCheckBox();
        warningJdk14Label = new javax.swing.JLabel();
        setSourceLevel15Panel = new javax.swing.JPanel();
        downgradeSourceLevel15CheckBox = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        setJdk15Panel = new javax.swing.JPanel();
        downgradeJdk15CheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        setSourceLevel14Panel = new javax.swing.JPanel();
        downgradeSourceLevel14CheckBox = new javax.swing.JCheckBox();
        warningSourceLevel14Label = new javax.swing.JLabel();

        downgradeJdk14CheckBox.setMnemonic(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "MNE_AgreeSetJdk14").charAt(0));
        downgradeJdk14CheckBox.setText(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetJdk14")); // NOI18N
        downgradeJdk14CheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downgradeJdk14CheckBoxActionPerformed(evt);
            }
        });

        warningJdk14Label.setText(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("MSG_RecommendationSetJdk14")); // NOI18N

        org.jdesktop.layout.GroupLayout setJdk14PanelLayout = new org.jdesktop.layout.GroupLayout(setJdk14Panel);
        setJdk14Panel.setLayout(setJdk14PanelLayout);
        setJdk14PanelLayout.setHorizontalGroup(
            setJdk14PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(warningJdk14Label, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
            .add(setJdk14PanelLayout.createSequentialGroup()
                .add(downgradeJdk14CheckBox)
                .addContainerGap())
        );
        setJdk14PanelLayout.setVerticalGroup(
            setJdk14PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(setJdk14PanelLayout.createSequentialGroup()
                .add(warningJdk14Label)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(downgradeJdk14CheckBox))
        );

        downgradeJdk14CheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("ACS_AgreeSetJdk14")); // NOI18N

        downgradeSourceLevel15CheckBox.setMnemonic(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "MNE_AgreeSetSourceLevel15").charAt(0));
        downgradeSourceLevel15CheckBox.setText(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetSourceLevel15")); // NOI18N
        downgradeSourceLevel15CheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downgradeSourceLevel15CheckBoxActionPerformed(evt);
            }
        });

        jLabel2.setText(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("MSG_RecommendationSetSourceLevel15")); // NOI18N

        org.jdesktop.layout.GroupLayout setSourceLevel15PanelLayout = new org.jdesktop.layout.GroupLayout(setSourceLevel15Panel);
        setSourceLevel15Panel.setLayout(setSourceLevel15PanelLayout);
        setSourceLevel15PanelLayout.setHorizontalGroup(
            setSourceLevel15PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
            .add(setSourceLevel15PanelLayout.createSequentialGroup()
                .add(downgradeSourceLevel15CheckBox)
                .addContainerGap())
        );
        setSourceLevel15PanelLayout.setVerticalGroup(
            setSourceLevel15PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(setSourceLevel15PanelLayout.createSequentialGroup()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(downgradeSourceLevel15CheckBox))
        );

        downgradeSourceLevel15CheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("ACS_AgreeSetSourceLevel15")); // NOI18N

        downgradeJdk15CheckBox.setMnemonic(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "MNE_AgreeSetJdk15").charAt(0));
        downgradeJdk15CheckBox.setText(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetJdk15")); // NOI18N
        downgradeJdk15CheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downgradeJdk15CheckBoxActionPerformed(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("MSG_RecommendationSetJdk15")); // NOI18N

        org.jdesktop.layout.GroupLayout setJdk15PanelLayout = new org.jdesktop.layout.GroupLayout(setJdk15Panel);
        setJdk15Panel.setLayout(setJdk15PanelLayout);
        setJdk15PanelLayout.setHorizontalGroup(
            setJdk15PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
            .add(setJdk15PanelLayout.createSequentialGroup()
                .add(downgradeJdk15CheckBox)
                .addContainerGap())
        );
        setJdk15PanelLayout.setVerticalGroup(
            setJdk15PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(setJdk15PanelLayout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(downgradeJdk15CheckBox))
        );

        downgradeJdk15CheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("ACS_AgreeSetJdk15")); // NOI18N

        downgradeSourceLevel14CheckBox.setMnemonic(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "MNE_AgreeSetSourceLevel14").charAt(0));
        downgradeSourceLevel14CheckBox.setText(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetSourceLevel14")); // NOI18N
        downgradeSourceLevel14CheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downgradeSourceLevel14CheckBoxActionPerformed(evt);
            }
        });

        warningSourceLevel14Label.setText(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("MSG_RecommendationSetSourceLevel14")); // NOI18N

        org.jdesktop.layout.GroupLayout setSourceLevel14PanelLayout = new org.jdesktop.layout.GroupLayout(setSourceLevel14Panel);
        setSourceLevel14Panel.setLayout(setSourceLevel14PanelLayout);
        setSourceLevel14PanelLayout.setHorizontalGroup(
            setSourceLevel14PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(warningSourceLevel14Label, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
            .add(setSourceLevel14PanelLayout.createSequentialGroup()
                .add(downgradeSourceLevel14CheckBox)
                .addContainerGap())
        );
        setSourceLevel14PanelLayout.setVerticalGroup(
            setSourceLevel14PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(setSourceLevel14PanelLayout.createSequentialGroup()
                .add(warningSourceLevel14Label)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(downgradeSourceLevel14CheckBox))
        );

        downgradeSourceLevel14CheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(J2eeVersionWarningPanel.class).getString("ACS_AgreeSetSourceLevel14")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(setJdk14Panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(setJdk15Panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(setSourceLevel14Panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(setSourceLevel15Panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(setJdk14Panel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(setSourceLevel14Panel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(setJdk15Panel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(setSourceLevel15Panel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel setJdk14Panel;
    private javax.swing.JPanel setJdk15Panel;
    private javax.swing.JPanel setSourceLevel14Panel;
    private javax.swing.JPanel setSourceLevel15Panel;
    private javax.swing.JLabel warningJdk14Label;
    private javax.swing.JLabel warningSourceLevel14Label;
    // End of variables declaration//GEN-END:variables
    
}


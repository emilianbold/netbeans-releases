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

import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.spi.java.project.support.PreferredProjectPlatform;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

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

    public final static String WARN_SET_JDK_6 = "warnSetJdk6"; // NOI18N
    
    public final static String WARN_SET_JDK_7 = "warnSetJdk7"; // NOI18N
    
    /**
     * Display a warning that the source level will be downgraded to 1.4
     */
    public final static String WARN_SET_SOURCE_LEVEL_14 = "warnSetSourceLevel14"; // NOI18N
    
    /**
     * Display a warning that the source level will be upgraded to 1.5
     */
    public final static String WARN_SET_SOURCE_LEVEL_15 = "warnSetSourceLevel15"; // NOI18N

    public final static String WARN_SET_SOURCE_LEVEL_6 = "warnSetSourceLevel6"; // NOI18N
    
    public final static String WARN_SET_SOURCE_LEVEL_7 = "warnSetSourceLevel7"; // NOI18N
    
    private String warningType;
    
    public J2eeVersionWarningPanel(String warningType) {
        initComponents();
        setWarningType(warningType);
    }
    
    public String getWarningType() {
        return warningType;
    }
    
    public void setWarningType(String warningType) {
        boolean select = false;
        String labelText = "";
        String checkboxText = "";
        this.warningType = warningType;
        if (WARN_SET_JDK_14.equals(warningType)) {
            labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetJdk14");
            checkboxText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetJdk14");
            select = UserProjectSettings.getDefault().isAgreedSetJdk14();
        } else if (WARN_SET_SOURCE_LEVEL_14.equals(warningType)) {
            labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetSourceLevel14");
            checkboxText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetSourceLevel14");
            select = UserProjectSettings.getDefault().isAgreedSetSourceLevel14();
        } else if (WARN_SET_JDK_15.equals(warningType)) {
            labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetJdk15");
            checkboxText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetJdk15");
            select = UserProjectSettings.getDefault().isAgreedSetJdk15();
        } else if (WARN_SET_SOURCE_LEVEL_15.equals(warningType)) {
            labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetSourceLevel15");
            checkboxText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetSourceLevel15");
            select = UserProjectSettings.getDefault().isAgreedSetSourceLevel15();
        } else if (WARN_SET_JDK_6.equals(warningType)) {
            labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetJdk6");
            checkboxText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetJdk6");
            select = true;
        } else if (WARN_SET_JDK_7.equals(warningType)) {
            labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetJdk7");
            checkboxText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetJdk7");
            select = true;
        } else if (WARN_SET_SOURCE_LEVEL_6.equals(warningType)) {
            labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetSourceLevel6");
            checkboxText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetSourceLevel6");
            select = true;
        } else if (WARN_SET_SOURCE_LEVEL_7.equals(warningType)) {
            labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetSourceLevel7");
            checkboxText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetSourceLevel7");
            select = true;
        }
        jLabel.setText(labelText);
        jCheckBox.setSelected(select);
        jCheckBox.setText(checkboxText);
    }
    
    public boolean getDowngradeAllowed() {
        return jCheckBox.isSelected();
    }
    
    public String getSuggestedJavaPlatformName() {
        if (WARN_SET_JDK_14.equals(warningType) ) {
            JavaPlatform[] javaPlatforms = getJavaPlatforms("1.4");
            return getPreferredPlatform(javaPlatforms).getDisplayName();
        }
        if (WARN_SET_JDK_15.equals(warningType) ) {
            JavaPlatform[] javaPlatforms = getJavaPlatforms("1.5");
            return getPreferredPlatform(javaPlatforms).getDisplayName();
        }
        if (WARN_SET_JDK_6.equals(warningType) ) {
            JavaPlatform[] javaPlatforms = getJavaPlatforms("1.6");
            return getPreferredPlatform(javaPlatforms).getDisplayName();
        }
        if (WARN_SET_JDK_7.equals(warningType) ) {
            JavaPlatform[] javaPlatforms = getJavaPlatforms("1.7");
            return getPreferredPlatform(javaPlatforms).getDisplayName();
        }
        return getPreferredPlatform(null).getDisplayName();
    }
    
    private static JavaPlatform getPreferredPlatform(@NullAllowed final JavaPlatform[] platforms) {
        final JavaPlatform pp = PreferredProjectPlatform.getPreferredPlatform(JavaPlatform.getDefault().getSpecification().getName());
        if (platforms == null) {
            return pp;
        }
        for (JavaPlatform jp : platforms) {
            if (jp.equals(pp)) {
                return jp;
            }
        }
        return platforms[0];
    }

    public static String findWarningType(Profile j2eeProfile) {
//        System.out.println("findWarningType: j2eeLevel="+j2eeLevel);
        JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        SpecificationVersion version = defaultPlatform.getSpecification().getVersion();
        String sourceLevel = version.toString();

        // no warning if 1.4 is the default for j2ee14
        if ("1.4".equals(sourceLevel) && j2eeProfile == Profile.J2EE_14) // NOI18N
            return null;

        // no warning if 1.5 is the default for j2ee15
        if ("1.5".equals(sourceLevel) && j2eeProfile == Profile.JAVA_EE_5) // NOI18N
            return null;

        // no warning if 1.6 is the default for j2ee16
        if ("1.6".equals(sourceLevel) && (j2eeProfile == Profile.JAVA_EE_6_FULL || j2eeProfile == Profile.JAVA_EE_6_WEB)) // NOI18N
            return null;
        
        // no warning if 1.7 is the default for j2ee7
        if ("1.7".equals(sourceLevel) && (j2eeProfile == Profile.JAVA_EE_7_FULL || j2eeProfile == Profile.JAVA_EE_7_WEB)) // NOI18N
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
                return WARN_SET_JDK_15;
            } else {
                return WARN_SET_SOURCE_LEVEL_15;
            }
        } else if (j2eeProfile == Profile.JAVA_EE_6_FULL || j2eeProfile == Profile.JAVA_EE_6_WEB) {
            JavaPlatform[] java16Platforms = getJavaPlatforms("1.6"); //NOI18N
            if (java16Platforms.length > 0) {
                return WARN_SET_JDK_6;
            } else {
                return WARN_SET_SOURCE_LEVEL_6;
            }
        } else if (j2eeProfile == Profile.JAVA_EE_7_FULL || j2eeProfile == Profile.JAVA_EE_7_WEB) {
            JavaPlatform[] java17Platforms = getJavaPlatforms("1.7"); //NOI18N
            if (java17Platforms.length > 0) {
                return WARN_SET_JDK_7;
            } else {
                return WARN_SET_SOURCE_LEVEL_7;
            }
        } else {
            //e.g. 1.3 - better ignore then assert
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

        jLabel = new javax.swing.JLabel();
        jCheckBox = new javax.swing.JCheckBox();

        jLabel.setText(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetJdk14")); // NOI18N

        jCheckBox.setText(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "CTL_AgreeSetJdk14")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox;
    private javax.swing.JLabel jLabel;
    // End of variables declaration//GEN-END:variables
    
}


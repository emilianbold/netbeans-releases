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

package org.netbeans.modules.mobility.project.ui.customizer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.JPanel;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.util.NbBundle;

/**
 *
 * @author  phrebejk, Adam Sotona
 */
public class CustomizerCompile extends JPanel implements CustomizerPanel, VisualPropertyGroup {
    
    public static final String[] DEBUG_LEVELS = {"debug", "info", "warn", "error", "fatal"}; //NOI18N
    public static final String[] ENCODINGS = Charset.availableCharsets().keySet().toArray(new String[0]);
    
    private static final String[] PROPERTY_GROUP = new String[] {DefaultPropertiesDescriptor.JAVAC_DEPRECATION,
    DefaultPropertiesDescriptor.JAVAC_DEBUG,
    DefaultPropertiesDescriptor.JAVAC_OPTIMIZATION,
    DefaultPropertiesDescriptor.JAVAC_ENCODING,
    DefaultPropertiesDescriptor.DEBUG_LEVEL};
    
    private VisualPropertySupport vps;
    
    /** Creates new form CustomizerCompile */
    public CustomizerCompile() {
        initComponents();
        initAccessibility();
        //As most common choice, sort UTF-8 to top
        Arrays.sort(ENCODINGS, new EncodingComparator());
    }
    
    public void initValues(ProjectProperties props, String configuration) {
        vps = VisualPropertySupport.getDefault(props);
        vps.register(defaultCheck, configuration, this);
        
    }
    
    public void initGroupValues(final boolean useDefault) {
        vps.register( deprecateCheck, DefaultPropertiesDescriptor.JAVAC_DEPRECATION, useDefault );
        vps.register( debugCheck, DefaultPropertiesDescriptor.JAVAC_DEBUG, useDefault );
        vps.register( optimizeCheck, DefaultPropertiesDescriptor.JAVAC_OPTIMIZATION, useDefault );
        vps.register( jComboBoxEncoding, ENCODINGS, DefaultPropertiesDescriptor.JAVAC_ENCODING, useDefault);
        vps.register( jComboDebugLevel, DEBUG_LEVELS, DefaultPropertiesDescriptor.DEBUG_LEVEL, useDefault );
        javacLabel.setEnabled(!useDefault);
        jLabelEncoding.setEnabled(!useDefault);
        jLabelDebugLevel.setEnabled(!useDefault);
    }
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_GROUP;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        defaultCheck = new javax.swing.JCheckBox();
        javacLabel = new javax.swing.JLabel();
        debugCheck = new javax.swing.JCheckBox();
        optimizeCheck = new javax.swing.JCheckBox();
        deprecateCheck = new javax.swing.JCheckBox();
        jLabelEncoding = new javax.swing.JLabel();
        jComboBoxEncoding = new javax.swing.JComboBox();
        jLabelDebugLevel = new javax.swing.JLabel();
        jComboDebugLevel = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(defaultCheck, NbBundle.getMessage(CustomizerCompile.class, "LBL_Use_Default")); // NOI18N
        defaultCheck.setMargin(new java.awt.Insets(0, 0, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(defaultCheck, gridBagConstraints);
        defaultCheck.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerCompile.class, "ACSD_CustCompile_UseDefault")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(javacLabel, NbBundle.getMessage(CustomizerCompile.class, "LBL_CustCompile_JavacOptions")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(javacLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(debugCheck, NbBundle.getMessage(CustomizerCompile.class, "LBL_CustCompile_DebuggingInfo")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(debugCheck, gridBagConstraints);
        debugCheck.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerCompile.class, "ACSD_CustCompile_GenDebug")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(optimizeCheck, NbBundle.getMessage(CustomizerCompile.class, "LBL_CustCompile_Optimization")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(optimizeCheck, gridBagConstraints);
        optimizeCheck.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerCompile.class, "ACSD_CustCompile_Optimalization")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deprecateCheck, NbBundle.getMessage(CustomizerCompile.class, "LBL_CustCompile_ReportDeprecated")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(deprecateCheck, gridBagConstraints);
        deprecateCheck.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerCompile.class, "ACSD_CustCompile_Deprecated")); // NOI18N

        jLabelEncoding.setLabelFor(jComboBoxEncoding);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelEncoding, NbBundle.getMessage(CustomizerCompile.class, "LBL_CustCompile_Encoding")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jLabelEncoding, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        add(jComboBoxEncoding, gridBagConstraints);

        jLabelDebugLevel.setLabelFor(jComboDebugLevel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelDebugLevel, NbBundle.getMessage(CustomizerCompile.class, "LBL_CustCompile_DebugLevel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jLabelDebugLevel, gridBagConstraints);

        jComboDebugLevel.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        add(jComboDebugLevel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerCompile.class, "ACSN_CustCompile"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerCompile.class, "ACSD_CustCompile"));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox debugCheck;
    private javax.swing.JCheckBox defaultCheck;
    private javax.swing.JCheckBox deprecateCheck;
    private javax.swing.JComboBox jComboBoxEncoding;
    private javax.swing.JComboBox jComboDebugLevel;
    private javax.swing.JLabel jLabelDebugLevel;
    private javax.swing.JLabel jLabelEncoding;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel javacLabel;
    private javax.swing.JCheckBox optimizeCheck;
    // End of variables declaration//GEN-END:variables

    private static final class EncodingComparator implements Comparator <String> {
        String defCharset = Charset.defaultCharset().displayName();
        public int compare(String o1, String o2) {
            if ("UTF-8".equals(o1)) { //NOI18N
                return Integer.MIN_VALUE + 1;
            } else if ("UTF-8".equals(o2)) {
                return Integer.MAX_VALUE - 1;
            } else if (defCharset.equals(o1)) {
                return Integer.MIN_VALUE;
            } else if (defCharset.equals(o2)) {
                return Integer.MAX_VALUE;
            }
            return o1.compareToIgnoreCase(o2);
        }
    }
}

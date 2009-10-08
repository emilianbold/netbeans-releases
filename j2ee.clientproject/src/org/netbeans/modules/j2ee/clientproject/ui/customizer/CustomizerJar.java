/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.j2ee.clientproject.ui.customizer;

import javax.swing.JPanel;
import org.openide.util.HelpCtx;

/** Customizer for general project attributes.
 *
 * @author  phrebejk
 */
public class CustomizerJar extends JPanel implements HelpCtx.Provider {
    private static final long serialVersionUID = 1L;
    
    public CustomizerJar( AppClientProjectProperties uiProperties ) {
        initComponents();
        
        jTextFieldDistDir.setDocument( uiProperties.DIST_JAR_MODEL );
        jTextFieldExcludes.setDocument( uiProperties.BUILD_CLASSES_EXCLUDES_MODEL );
        
        uiProperties.JAR_COMPRESS_MODEL.setMnemonic( jCheckBoxCommpress.getMnemonic() );
        jCheckBoxCommpress.setModel( uiProperties.JAR_COMPRESS_MODEL ); 
    } 
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( CustomizerJar.class );
    }
        
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelDistDir = new javax.swing.JLabel();
        jTextFieldDistDir = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldExcludes = new javax.swing.JTextField();
        jCheckBoxCommpress = new javax.swing.JCheckBox();
        excludeMessage = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabelDistDir.setLabelFor(jTextFieldDistDir);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelDistDir, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "LBL_CustomizeJar_DistDir_JTextField"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        add(jLabelDistDir, gridBagConstraints);

        jTextFieldDistDir.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jTextFieldDistDir, gridBagConstraints);
        jTextFieldDistDir.getAccessibleContext().setAccessibleDescription(null);

        jLabel2.setLabelFor(jTextFieldExcludes);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "LBL_CustomizeJar_Excludes_JTextField"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jTextFieldExcludes, gridBagConstraints);
        jTextFieldExcludes.getAccessibleContext().setAccessibleDescription(null);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxCommpress, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "LBL_CustomizeJar_Commpres_JCheckBox"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(jCheckBoxCommpress, gridBagConstraints);
        jCheckBoxCommpress.getAccessibleContext().setAccessibleDescription(null);

        excludeMessage.setLabelFor(jTextFieldExcludes);
        org.openide.awt.Mnemonics.setLocalizedText(excludeMessage, java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/clientproject/ui/customizer/Bundle").getString("LBL_CustomizerJar_ExcludeMessage"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(excludeMessage, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel excludeMessage;
    private javax.swing.JCheckBox jCheckBoxCommpress;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelDistDir;
    private javax.swing.JTextField jTextFieldDistDir;
    private javax.swing.JTextField jTextFieldExcludes;
    // End of variables declaration//GEN-END:variables
                
}

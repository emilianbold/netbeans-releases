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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.ddloaders.web.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Utils;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.api.project.SourceGroup;

/**
 * @author  mkuchtiak
 */
public class OverviewPanel extends SectionInnerPanel implements java.awt.event.ItemListener {
    DDDataObject dObj;
    WebApp webApp;
    /** Creates new form JspPGPanel */
    public OverviewPanel(SectionView sectionView, DDDataObject dObj) {
        super(sectionView);
        this.dObj=dObj;
        webApp=dObj.getWebApp();
        initComponents();
        
        // Display Name
        dispNameTF.setText(webApp.getDefaultDisplayName());
        addModifier(dispNameTF);
        
        // Description
        Utils.makeTextAreaLikeTextField(descriptionTA,dispNameTF);
        descriptionTA.setText(webApp.getDefaultDescription());
        addModifier(descriptionTA);
        
        // Distributable
        jCheckBox1.setSelected(webApp.isDistributable());
        jCheckBox1.addItemListener(this);
        
        // Session Timeout
        stTF.setText(getSessionTimeout());
        addValidatee(stTF);
        
    }
    
    private String getSessionTimeout() {
        SessionConfig config = webApp.getSingleSessionConfig();
        if (config==null) return "";
        java.math.BigInteger timeout = config.getSessionTimeout();
        return (timeout==null?"":timeout.toString());
    }
    
    private void setSessionTimeout(String text) {
        String val = text.trim();
        SessionConfig config = webApp.getSingleSessionConfig();
        if (config!=null) {
            if (text.length()==0) webApp.setSessionConfig(null);
            else config.setSessionTimeout(new java.math.BigInteger(val));
        } else if (text.length()>0) {
            try {
                SessionConfig newConfig = (SessionConfig)webApp.createBean("SessionConfig");
                newConfig.setSessionTimeout(new java.math.BigInteger(val));
                webApp.setSessionConfig(newConfig);
            } catch (ClassNotFoundException ex){}
        }

    }
    
    public javax.swing.JComponent getErrorComponent(String errorId) {
        return null;
    }

    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==stTF) {
            String text = value.trim();
            if (text.length()==0) {
                getSectionView().getErrorPanel().clearError();
            } else {
                java.math.BigInteger st=null;
                try {
                    st = new java.math.BigInteger(text);
                } catch (NumberFormatException ex) {}
                if (st==null) {
                    getSectionView().getErrorPanel().setError(new Error(Error.TYPE_FATAL, Error.ERROR_MESSAGE, "Invalid Value : "+text, stTF));
                    return;
                }
                getSectionView().getErrorPanel().clearError();
            }
        }
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        String text = ((String)value).trim();
        if (source==dispNameTF) {
            webApp.setDisplayName(text.length()==0?null:text);
        } else if (source==descriptionTA) {
            webApp.setDescription(text.length()==0?null:text);
        } else if (source==stTF) {
            setSessionTimeout(text);
        }
    }
    
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (source==stTF) {
            stTF.setText(getSessionTimeout());
        }
    }
    
    public void linkButtonPressed(Object obj, String id) {
    } 
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        filler = new javax.swing.JPanel();
        dispNameLabel = new javax.swing.JLabel();
        dispNameTF = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionTA = new javax.swing.JTextArea();
        jCheckBox1 = new javax.swing.JCheckBox();
        stLabel = new javax.swing.JLabel();
        stTF = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        filler.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(filler, gridBagConstraints);

        dispNameLabel.setLabelFor(dispNameTF);
        org.openide.awt.Mnemonics.setLocalizedText(dispNameLabel, org.openide.util.NbBundle.getMessage(OverviewPanel.class, "LBL_displayName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(dispNameLabel, gridBagConstraints);

        dispNameTF.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(dispNameTF, gridBagConstraints);

        descriptionLabel.setLabelFor(descriptionTA);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(OverviewPanel.class, "LBL_description")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(descriptionLabel, gridBagConstraints);

        descriptionTA.setRows(3);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        add(descriptionTA, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(OverviewPanel.class, "LBL_distributable")); // NOI18N
        jCheckBox1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jCheckBox1, gridBagConstraints);

        stLabel.setLabelFor(stTF);
        org.openide.awt.Mnemonics.setLocalizedText(stLabel, org.openide.util.NbBundle.getMessage(OverviewPanel.class, "LBL_sessionTimeout")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        add(stLabel, gridBagConstraints);

        stTF.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        add(stTF, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(OverviewPanel.class, "LBL_min")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        add(jLabel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextArea descriptionTA;
    private javax.swing.JLabel dispNameLabel;
    private javax.swing.JTextField dispNameTF;
    private javax.swing.JPanel filler;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel stLabel;
    private javax.swing.JTextField stTF;
    // End of variables declaration//GEN-END:variables
 
    public void itemStateChanged(java.awt.event.ItemEvent evt) {                                            
        if (evt.getSource() == jCheckBox1) {
            dObj.modelUpdatedFromUI();
            dObj.setChangedFromUI(true);
            webApp.setDistributable(jCheckBox1.isSelected());
            dObj.setChangedFromUI(false);
        }
    }
    
    /** This will be called before model is changed from this panel
     */
    protected void startUIChange() {
        dObj.setChangedFromUI(true);
    }
    
    /** This will be called after model is changed from this panel
     */
    protected void endUIChange() {
        dObj.modelUpdatedFromUI();
        dObj.setChangedFromUI(false);
    }
}

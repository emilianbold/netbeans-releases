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

package org.netbeans.modules.uml.propertysupport.options.panels;

import java.util.Hashtable;
import java.util.prefs.Preferences;
import javax.swing.JComboBox;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author  krichard
 */
public class ShowMeDialogsListPanel extends javax.swing.JPanel {
    
    
    /** Creates new form ShowMeDialogsListPanel */
    public ShowMeDialogsListPanel() {
        initComponents();
        
        for (int i = 0; i < mappedChoices.length; i ++)
            getMappedIndex.put (mappedChoices[i], displayChoices[i]) ;
    }
    
    private int getMappedIndex (String s) {
        int index = -1;
        
        for (int i = 0; i < mappedChoices.length && index < 0; i ++) {
            if (s.equals(mappedChoices[i])) {
                index = i;
            }
        }
        
        return index ;
    }
    
    /**
     * Setting all the ui elements to match their respective prefences.
     * This is called in the corresponding UMLOptionsPanel's update method.
     */
    public void load() {
        
        String s = "";
        Preferences prefs = NbPreferences.forModule(DummyCorePreference.class);
        int index = -1 ;
        
        s = prefs.get("UML_ShowMe_Allow_Lengthy_Searches", PSK_ASK);
        index = getMappedIndex (s) ;
        allowLengthySearchesCB.setSelectedIndex(index);
        
        s = prefs.get("UML_ShowMe_Automatically_Create_Classifiers", PSK_ASK);
        index = getMappedIndex (s) ;
        autoCreateCB.setSelectedIndex(index);
        
        s = prefs.get("UML_ShowMe_Delete_Combined_Fragment_Messages", PSK_ASK);
        index = getMappedIndex (s) ;
        deleteCombFragCB.setSelectedIndex(index);
        
        s = prefs.get("UML_ShowMe_Delete_Connector_Messages", PSK_ASK);
        index = getMappedIndex (s) ;
        deleteConnectorCB.setSelectedIndex(index);
        
        s = prefs.get("UML_ShowMe_Delete_File_when_Deleting_Artifacts", PSK_ASK);
        index = getMappedIndex (s) ;
        deleteFileCB.setSelectedIndex(index);
        
        s = prefs.get("UML_ShowMe_Dont_Show_Filter_Warning_Dialog", PSK_ASK);
        index = getMappedIndex (s) ;
        filterWarningCB.setSelectedIndex(index);
                        
        s = prefs.get("UML_ShowMe_Move_Invoked_Operation", PSK_ASK);
        index = getMappedIndex (s) ;
        moveInvokedCB.setSelectedIndex(index);
        
        s = prefs.get("UML_ShowMe_Overwrite_Existing_Participants", PSK_ASK);
        index = getMappedIndex (s) ;
        overwriteCB.setSelectedIndex(index);
        
        s = prefs.get("UML_ShowMe_Transform_When_Elements_May_Be_Lost", PSK_ASK);
        index = getMappedIndex (s) ;
        transformCB.setSelectedIndex(index);   
        
    }
    
    public void store() {
        
        Preferences prefs = NbPreferences.forModule(DummyCorePreference.class);
        int index = -1;
        
        index = allowLengthySearchesCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Allow_Lengthy_Searches", mappedChoices[index]);
        index = autoCreateCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Automatically_Create_Classifiers", mappedChoices[index]);
        index = deleteCombFragCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Delete_Combined_Fragment_Messages", mappedChoices[index]);
        index = deleteConnectorCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Delete_Connector_Messages", mappedChoices[index]);
        index = deleteFileCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Delete_File_when_Deleting_Artifacts", mappedChoices[index]);
        index = filterWarningCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Dont_Show_Filter_Warning_Dialog", mappedChoices[index]);
        index = moveInvokedCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Move_Invoked_Operation", mappedChoices[index]);
        index = overwriteCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Overwrite_Existing_Participants", mappedChoices[index]);
        index = transformCB.getSelectedIndex() ;
        prefs.put("UML_ShowMe_Transform_When_Elements_May_Be_Lost", mappedChoices[index]);
        
    }
    
    public void cancel() {
        //do nothing ;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        deleteFileCB = new JComboBox (displayChoices);
        filterWarningCB = new JComboBox (displayChoices);
        transformCB = new JComboBox (displayChoices);
        overwriteCB = new JComboBox (displayChoices);
        deleteConnectorCB = new JComboBox (displayChoices);
        autoCreateCB = new JComboBox (displayChoices);
        deleteCombFragCB = new JComboBox (displayChoices);
        moveInvokedCB = new JComboBox (displayChoices);
        allowLengthySearchesCB = new JComboBox (displayChoices);

        mainLabel.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "mainLabel.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel1.text_3")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel2.text_2")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel3.text_2")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel5.text_2")); // NOI18N

        jLabel6.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel6.text_2")); // NOI18N

        jLabel7.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel7.text_2")); // NOI18N

        jLabel8.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel8.text_2")); // NOI18N

        jLabel9.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel9.text_1")); // NOI18N

        jLabel10.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jLabel10.text_2")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel3)
                            .add(jLabel5)
                            .add(jLabel6)
                            .add(jLabel9)
                            .add(jLabel10)
                            .add(jLabel7)
                            .add(jLabel8)
                            .add(jLabel1))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, allowLengthySearchesCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, moveInvokedCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, deleteCombFragCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, autoCreateCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, deleteConnectorCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, overwriteCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, transformCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 169, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(15, 15, 15))
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(deleteFileCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 169, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(filterWarningCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 169, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(mainLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(deleteFileCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(filterWarningCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(transformCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(overwriteCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(deleteConnectorCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(autoCreateCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(deleteCombFragCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(moveInvokedCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(allowLengthySearchesCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10))
                .addContainerGap(15, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    
    private String PSK_ASK = "PSK_ASK" ;
    private String PSK_ALWAYS = "PSK_ALWAYS" ;
    private String PSK_NEVER = "PSK_NEVER" ;
    
    private String ASK = NbBundle.getMessage(ShowMeDialogsListPanel.class, "ASK") ;
    private String ALWAYS = NbBundle.getMessage(ShowMeDialogsListPanel.class, "ALWAYS") ;
    private String NEVER = NbBundle.getMessage(ShowMeDialogsListPanel.class, "NEVER") ;
    
    private String[] displayChoices = {ASK, ALWAYS, NEVER} ;
    private String[] mappedChoices = {PSK_ASK, PSK_ALWAYS, PSK_NEVER} ;
    
    private Hashtable getMappedIndex = new Hashtable();
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox allowLengthySearchesCB;
    private javax.swing.JComboBox autoCreateCB;
    private javax.swing.JComboBox deleteCombFragCB;
    private javax.swing.JComboBox deleteConnectorCB;
    private javax.swing.JComboBox deleteFileCB;
    private javax.swing.JComboBox filterWarningCB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel mainLabel;
    private javax.swing.JComboBox moveInvokedCB;
    private javax.swing.JComboBox overwriteCB;
    private javax.swing.JComboBox transformCB;
    // End of variables declaration//GEN-END:variables
    
}

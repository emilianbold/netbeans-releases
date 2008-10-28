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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.earproject.classpath.ClassPathSupportCallbackImpl;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Customizer for Enterprise Application packaging.
 */
public class CustomizerJarContent extends JPanel implements HelpCtx.Provider {
    private static final long serialVersionUID = 1L;
    
    private final EarProjectProperties uiProperties;
    
    public CustomizerJarContent(EarProjectProperties earProperties) {
        this.uiProperties = earProperties;
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_A11YDesc"));
        
        jTextFieldFileName.setDocument(uiProperties.ARCHIVE_NAME_MODEL);
        jTextFieldExContent.setDocument( uiProperties.BUILD_CLASSES_EXCLUDES_MODEL );
        uiProperties.ARCHIVE_COMPRESS_MODEL.setMnemonic( jCheckBoxCompress.getMnemonic() );
        jCheckBoxCompress.setModel( uiProperties.ARCHIVE_COMPRESS_MODEL );
        ClassPathUiSupport.Callback callback = new ClassPathUiSupport.Callback() {
            public void initItem(Item item) {
                if (item.getType() != ClassPathSupport.Item.TYPE_LIBRARY || !item.getLibrary().getType().equals(J2eePlatform.LIBRARY_TYPE)) {
                    item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, "/"); //NOI18N
                }
            }
        };
        EditMediator.register( uiProperties.getProject(),
                uiProperties.getProject().getAntProjectHelper(),
                uiProperties.getProject().getReferenceHelper(),
                EditMediator.createListComponent(jTableAddContent, uiProperties.EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel()) , 
                jButtonAddJar.getModel(),
                jButtonAddLib.getModel(),
                jButtonAddProject.getModel(),
                jButtonRemove.getModel(),
                (new JButton()).getModel(), // no button in UI
                (new JButton()).getModel(), // no button in UI
                (new JButton()).getModel(), // no button in UI
                uiProperties.SHARED_LIBRARIES_MODEL,
                callback,
                new String[]{EjbProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE, JavaProjectConstants.ARTIFACT_TYPE_JAR},
                null, JFileChooser.FILES_ONLY);
        jTableAddContent.setModel( uiProperties.EAR_CONTENT_ADDITIONAL_MODEL);
        jTableAddContent.setDefaultRenderer(ClassPathSupport.Item.class, uiProperties.CLASS_PATH_TABLE_RENDERER);
        initTableVisualProperties(jTableAddContent);
    }
    
    private void initTableVisualProperties(JTable table) {
        //table.setGridColor(jTableCpC.getBackground());
        table.setRowHeight(jTableAddContent.getRowHeight() + 4);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        // set the color of the table's JViewport
        table.getParent().setBackground(table.getBackground());
   
        //#88174 - Need horizontal scrollbar for library names
        //ugly but I didn't find a better way how to do it
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMinWidth(230);
        column.setWidth(230);
        column.setMinWidth(75);
        column = table.getColumnModel().getColumn(1);
        column.setMinWidth(126);
        column.setWidth(126);
        column.setMinWidth(28);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCheckBoxCompress = new javax.swing.JCheckBox();
        jLabelExContent = new javax.swing.JLabel();
        jLabelAddContent = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableAddContent = new javax.swing.JTable();
        jButtonAddJar = new javax.swing.JButton();
        jButtonAddLib = new javax.swing.JButton();
        jButtonAddProject = new javax.swing.JButton();
        jButtonRemove = new javax.swing.JButton();
        jTextFieldFileName = new javax.swing.JTextField();
        jLabelFileName = new javax.swing.JLabel();
        jLabelExContent1 = new javax.swing.JLabel();
        jTextFieldExContent = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxCompress, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_Commpres_JCheckBox")); // NOI18N
        jCheckBoxCompress.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(jLabelExContent, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_Content_JLabel")); // NOI18N

        jLabelAddContent.setLabelFor(jTableAddContent);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelAddContent, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_AddContent_JLabel")); // NOI18N

        jTableAddContent.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(jTableAddContent);
        jTableAddContent.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "LBL_AACH_ProjectJarFiles_JLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJar, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_AddJar_JButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLib, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_AddLib_JButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddProject, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_AddProject_JButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemove, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_Remove_JButton")); // NOI18N

        jTextFieldFileName.setEditable(false);

        jLabelFileName.setLabelFor(jTextFieldFileName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelFileName, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_FileName_JLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelExContent1, NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeEAR_Content_Comment_JLabel")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBoxCompress)
                    .add(jLabelAddContent)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(jButtonAddProject, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(jButtonAddLib, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(jButtonAddJar, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(jButtonRemove, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabelExContent)
                                    .add(jLabelFileName))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldFileName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabelExContent1)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldExContent, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelFileName)
                    .add(jTextFieldFileName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelExContent)
                    .add(jTextFieldExContent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelExContent1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxCompress)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelAddContent)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jButtonAddProject)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonAddLib)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonAddJar)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButtonRemove)
                        .addContainerGap())
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)))
        );

        jCheckBoxCompress.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_Commpres_A11YDesc")); // NOI18N
        jButtonAddJar.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_AddJar_A11YDesc")); // NOI18N
        jButtonAddLib.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_AddLib_A11YDesc")); // NOI18N
        jButtonAddProject.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_AddProject_A11YDesc")); // NOI18N
        jButtonRemove.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_AdditionalRemove_A11YDesc")); // NOI18N
        jTextFieldFileName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeEAR_FileName_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddJar;
    private javax.swing.JButton jButtonAddLib;
    private javax.swing.JButton jButtonAddProject;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JCheckBox jCheckBoxCompress;
    private javax.swing.JLabel jLabelAddContent;
    private javax.swing.JLabel jLabelExContent;
    private javax.swing.JLabel jLabelExContent1;
    private javax.swing.JLabel jLabelFileName;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableAddContent;
    private javax.swing.JTextField jTextFieldExContent;
    private javax.swing.JTextField jTextFieldFileName;
    // End of variables declaration//GEN-END:variables
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerJarContent.class);
    }
    
}

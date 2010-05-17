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

package org.netbeans.modules.uml.requirements.doorsprovider;

import java.awt.Frame;
import java.util.StringTokenizer;
import javax.swing.DefaultListModel;
import org.netbeans.modules.uml.core.requirementsframework.RequirementsException;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.UIFactory;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author  Thuy
 */
public class DoorsProjectDialog extends javax.swing.JPanel
{
    //private boolean wasAccepted = false;
    private String selectedItem = "";
    private boolean errorFound = false;
    
    /** Creates new form DoorsProjectDialog */
    public DoorsProjectDialog()
    {
        initComponents();
        addDOORSProjects();
    }
    
    protected void addDOORSProjects()
   {
      try
      {
         String result = DoorUtility.sendRequestToDoors("#include \"GetProjectInfo.dxl\";GetProjectNames()");
         StringTokenizer tokenizer = new StringTokenizer(result, "|");

         DefaultListModel model = new DefaultListModel();
         while(tokenizer.hasMoreTokens() == true)
         {
            model.addElement(tokenizer.nextToken());
         }
         projectList.setModel(model);
         
         if (model != null  && model.size() > 0) {
            projectList.setSelectedIndex(0);
         }
      }
      catch(RequirementsException e)
      {
         Frame hwnd = null;
         IProxyUserInterface cpProxyUserInterface = ProductHelper.getProxyUserInterface();
         
         if( cpProxyUserInterface != null)
         {
            hwnd = cpProxyUserInterface.getWindowHandle();
         }
         
         String msgText = NbBundle.getMessage(DoorsProjectDialog.class, "IDS_DOORSNOTAVAILABLEMESSAGE");
         String msgTitle = NbBundle.getMessage(DoorsProjectDialog.class, "IDS_DOORSNOTAVAILABLETITLE");
         
         IQuestionDialog cpQuestionDialog = UIFactory.createQuestionDialog();
         
         cpQuestionDialog.displaySimpleQuestionDialog( MessageDialogKindEnum.SQDK_OK,
                                                       MessageIconKindEnum.EDIK_ICONWARNING,
                                                       msgText ,
                                                       MessageResultKindEnum.SQDRK_RESULT_YES,
                                                       hwnd,
                                                       msgTitle  );
         errorFound = true;
      }
   }
    
    public boolean hasError () {
        return errorFound;
    }
    
    public String  performAction (Object action) {
        String selectedItem = null;
        if (action == DialogDescriptor.OK_OPTION) {
            selectedItem = (String)projectList.getSelectedValue();
        }
        return selectedItem;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane = new javax.swing.JScrollPane();
        projectList = new javax.swing.JList();
        projectListLabel = new javax.swing.JLabel();

        jScrollPane.setFocusable(false);
        jScrollPane.setViewportView(projectList);
        projectList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DoorsProjectDialog.class, "ACSN_PROJECT_LIST"));
        projectList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DoorsProjectDialog.class, "ACSD_PROJECT_LIST"));

        projectListLabel.setLabelFor(projectList);
        org.openide.awt.Mnemonics.setLocalizedText(projectListLabel, org.openide.util.NbBundle.getMessage(DoorsProjectDialog.class, "LBL_DoorsProjects"));
        projectListLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DoorsProjectDialog.class, "ACSD_PROJECT_LIST"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .add(projectListLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(projectListLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JList projectList;
    private javax.swing.JLabel projectListLabel;
    // End of variables declaration//GEN-END:variables
    
}

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
/*
 * AddResourceOverwriteDialog.java
 *
 * Created on June 2, 2004, 8:08 PM
 */

package org.netbeans.modules.visualweb.project.jsf.api;

import java.awt.Dialog;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  dey
 */
public class AddResourceOverwriteDialog extends javax.swing.JPanel {
    private File target = null;
    private File newTarget = null;
    private File fileReturn = null;

    /** Mode flag which indicates that the dialog will ask whether to overwrite or
     * create new when there is a file import conflict */
    public static final int CONFLICT_ASK = 0;

    /** Mode flag which indicates that the dialog will ask whether to overwrite or
     * create new when there is a file import conflict, AND in addition ask if
     * the user wants to always create or always replace (a "Yes To All" mechanism).
     * Use this only when you'return creating multiple resources.
     *
     * When using this be aware that once the user has used Always/Never, the mode
     * will change, so you should set it back to CONFLICT_ASK when you're done
     * with the batch operation.
     */
    public static final int CONFLICT_ASK_MANY = 1;

    /** Mode flag which indicates that the user always wants to replace, so take
     * this answer as a given without asking the user.
     */
    public static final int CONFLICT_OVERWRITE = 2;

    /** Mode flag which indicates that the user always wants to use the existing
     * resource, so take this answer as a given without asking the user
     */
    public static final int CONFLICT_USE_EXISTING = 3;

    /** Mode flag which indicates that the user always wants to create a new file
     * rather than replace, so take this answer as a given without asking the user.
     */
    public static final int CONFLICT_CREATE_NEW = 4;

    /** <p>
     * Operation for the AddResourceOverwriteDialog which determines whether
     * the dialog will show "Always/Never" buttons, or if the dialog will even
     * be shown at all (when the answer is already set in the flag). The default
     * value is CONFLICT_ASK.
     * </p>
     * <p>
     * Use this mode when you're going to be adding lots of resources
     * in batch; if so you'll want to set the mode to "CONFLICT_ASK_MANY" originally
     * and when you'return done, set it back to "CONFLICT_ASK".  Once the user
     * has answered for example "Always" in the dialog, the mode is changed to
     * a particular answer which causes the dialog to just bail out immediately
     * with getFile() returning the desired answer without user intervention.
     * </p>
     */
    private static int mode = CONFLICT_ASK;

    /** Creates new form AddResourceOverwriteDialog */
    public AddResourceOverwriteDialog(File target) {
        this.target = target;
        initComponents();
        initComponents2();
    }

    public static int getMode() {
        return mode;
    }

    public static void setMode(int value) {
        mode = value;
    }

    public void showDialog() {
        if (mode == CONFLICT_OVERWRITE || mode == CONFLICT_USE_EXISTING) {
            // PENDING: how does client distinguish between these two cases?
            fileReturn = target;
            return;
        } else if (mode == CONFLICT_CREATE_NEW) {
            fileReturn = newTarget;
            return;
        }

        DialogDescriptor desc = new DialogDescriptor(this, NbBundle.getMessage(AddResourceOverwriteDialog.class, "LBL_ResourceNameConflict"));
        desc.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
        desc.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        JButton alwaysButton = null;
        if (mode == CONFLICT_ASK_MANY) {
            alwaysButton = new JButton(NbBundle.getMessage(AddResourceOverwriteDialog.class, "LBL_Always"));
            desc.setAdditionalOptions(new Object[] { alwaysButton });
        }
        Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);        
        dlg.show();
        Object value = desc.getValue();
        if (value != NotifyDescriptor.OK_OPTION && value != alwaysButton) { 
            fileReturn = null;
            return;
        }
        if (renameButton.isSelected()) {
            fileReturn = newTarget;
            if (value == alwaysButton) {
                mode = CONFLICT_CREATE_NEW;
            }
        } else if (overwriteButton.isSelected()) {
            boolean wasDeleted = target.delete();
            fileReturn = target;
            if (value == alwaysButton) {
                mode = CONFLICT_OVERWRITE;
            }
        } else if (useExistingButton.isSelected()) {
            fileReturn = target;
            if (value == alwaysButton) {
                mode = CONFLICT_USE_EXISTING;
            }
        }
    }
    
    public File getFile() {
        return fileReturn;
    }
    
    private void initComponents2() {
        newTarget = findUniqueFile(target);        
        label.setText(NbBundle.getMessage(AddResourceOverwriteDialog.class, "LBL_AddResourceMsg", target.getName()));
        renameButton.setText(NbBundle.getMessage(AddResourceOverwriteDialog.class, "LBL_AddResourceRename", newTarget.getName()));
        overwriteButton.setText(NbBundle.getMessage(AddResourceOverwriteDialog.class, "LBL_AddResourceOverwrite"));
        useExistingButton.setText(NbBundle.getMessage(AddResourceOverwriteDialog.class, "LBL_AddResourceUseExisting"));
        renameButton.setSelected(false);
        overwriteButton.setSelected(false);
        useExistingButton.setSelected(true);
    }
    
    private File findUniqueFile(File old) {
        int count = 1;
        String fileName = old.getName();
        String extension = FileUtil.getExtension(fileName);
        String baseName = fileName.substring(0, fileName.lastIndexOf(extension) - 1);
        String testName = fileName;
        File dir = old.getParentFile();
        File newFile = null;
        while(newFile == null) {
            newFile = new File(dir, testName);
            if (!newFile.exists())
                return newFile;
            newFile = null;
            testName = baseName + "_" + count++ + "." + extension;  // NOI18N
        }
        return newFile;
    }
    
    

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        useExistingButton = new javax.swing.JRadioButton();
        renameButton = new javax.swing.JRadioButton();
        overwriteButton = new javax.swing.JRadioButton();
        label = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridLayout(0, 1));

        buttonGroup1.add(useExistingButton);
        useExistingButton.setSelected(true);
        useExistingButton.setText("Use existing file instead of the selected file");
        jPanel1.add(useExistingButton);

        buttonGroup1.add(renameButton);
        renameButton.setText("Rename existing file to <>");
        jPanel1.add(renameButton);

        buttonGroup1.add(overwriteButton);
        overwriteButton.setText("Overwrite existing file with the selected file");
        jPanel1.add(overwriteButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(jPanel1, gridBagConstraints);

        label.setText("The file <filename> already exists in the Resources folder. ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(label, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel label;
    private javax.swing.JRadioButton overwriteButton;
    private javax.swing.JRadioButton renameButton;
    private javax.swing.JRadioButton useExistingButton;
    // End of variables declaration//GEN-END:variables
    
}

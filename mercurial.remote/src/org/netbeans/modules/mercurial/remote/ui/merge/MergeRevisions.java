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
package org.netbeans.modules.mercurial.remote.ui.merge;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class MergeRevisions implements PropertyChangeListener {

    private final MergeRevisionsPanel panel;
    private final JButton okButton;
    private final JButton cancelButton;
    
    public MergeRevisions(VCSFileProxy repository, VCSFileProxy [] roots) {
        panel = new MergeRevisionsPanel(repository, roots);
         okButton = new JButton(NbBundle.getMessage(MergeRevisions.class, 
                     "CTL_MergeForm_Action_Merge")); // NOI18N
         okButton.getAccessibleContext().setAccessibleDescription(
                 NbBundle.getMessage(MergeRevisions.class, 
                 "ACSD_MergeForm_Action_Merge")); // NOI18N
         cancelButton = new JButton(NbBundle.getMessage(MergeRevisions.class, 
                 "CTL_MergeForm_Action_Cancel")); // NOI18N
         cancelButton.getAccessibleContext().setAccessibleDescription(
                 NbBundle.getMessage(MergeRevisions.class, 
                 "ACSD_MergeForm_Action_Cancel")); // NOI18N
        okButton.setEnabled(false);
        panel.addPropertyChangeListener(this);
        panel.loadRevisions();
    } 
    
    public boolean showDialog() {
        DialogDescriptor dialogDescriptor;
        dialogDescriptor = new DialogDescriptor(panel, 
                NbBundle.getMessage(MergeRevisions.class, "ASCD_MERGE_DIALOG")); // NOI18N

        dialogDescriptor.setOptions(new Object[] {okButton, cancelButton});
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(MergeRevisions.class, "ASCD_MERGE_DIALOG")); // NOI18N
        dialog.setVisible(true);
        dialog.setResizable(false);
        boolean ret = dialogDescriptor.getValue() == okButton;
        return ret;       
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (okButton != null && MergeRevisionsPanel.PROP_VALID.equals(evt.getPropertyName())) {
            boolean valid = (Boolean) evt.getNewValue();
            okButton.setEnabled(valid);
        }       
    }

    public String getSelectionRevision() {
        if (panel == null) {
            return null;
        }
        return panel.getSelectedRevisionCSetId();
    }
}

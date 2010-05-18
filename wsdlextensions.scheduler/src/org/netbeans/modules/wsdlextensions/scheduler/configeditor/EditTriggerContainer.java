/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.scheduler.configeditor;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerConstants;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerConstants.TriggerType;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerModel;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerModel.TriggerDetail;
import org.netbeans.modules.wsdlextensions.scheduler.utils.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author sunsoabi_edwong
 */
public class EditTriggerContainer extends DescriptionContainer
        implements SchedulerConstants {

    private TriggerEditor editor;
    
    public EditTriggerContainer() {
        super();
    }
    
    public void setFields(boolean edit, TriggerDetail td) {
        editor.setFields(edit, td);
    }
    
    public void captureFields(TriggerDetail td) {
        editor.validateFields(td);
        editor.captureFields(td);
    }
    
    public static void showDialog(final int triggerIdx,
            final SchedulerModel schedulerModel) {
        if (triggerIdx != -1) {
            TriggerDetail td = schedulerModel.getTriggers().get(triggerIdx);
            TriggerType type = TriggerType.toEnum(td.getType());
            if (type != null) {
                showDialog(type, triggerIdx, schedulerModel);
            }
        }
    }
    
    public static void showDialog(final TriggerType type,
            final int triggerIdx, final SchedulerModel schedulerModel) {
        final boolean edit = (triggerIdx > -1);
        
        final EditTriggerContainer editContainer = new EditTriggerContainer();
        switch (type) {
        case SIMPLE:
            editContainer.editor =
                    new SimpleTriggerPanel(editContainer, schedulerModel);
            break;
        case CRON:
            editContainer.editor =
                    new CronTriggerPanel(editContainer, schedulerModel);
            break;
        case HYBRID:
            editContainer.editor =
                    new HybridTriggerPanel(editContainer, schedulerModel);
            break;
        default:
            return;
        }
        
        editContainer.setOther(editContainer.editor.getComponent());
        
        final TriggerDetail td = edit
                ? schedulerModel.getTriggers().get(triggerIdx)
                : schedulerModel.createTriggerDetail();
        editContainer.setFields(edit, td);
        
        String title = NbBundle.getMessage(EditTriggerContainer.class,
                (edit ? "TLE_EDIT_TRIGGER" : "TLE_ADD_NEW_TRIGGER"),    //NOI18N
                type.getI18nName());
        
        JButton btnCommit = new JButton(edit ? NbBundle.getMessage(
                    EditTriggerContainer.class, "LBL_COMMIT_CHANGES")   //NOI18N
                : NbBundle.getMessage(EditTriggerContainer.class,
                        "LBL_ADD_TRIGGER", type.getI18nName()));        //NOI18N
        JButton btnCancel = new JButton(NbBundle.getMessage(
                EditTriggerContainer.class, "LBL_CANCEL"));             //NOI18N
        
        DialogDescriptor dd = new DialogDescriptor(editContainer, title, true,
                new Object[] {btnCommit, btnCancel}, btnCommit,
                DialogDescriptor.DEFAULT_ALIGN, null, null);
        dd.setClosingOptions(new Object[0]);
        final Dialog dlgEdit = DialogDisplayer.getDefault().createDialog(dd);
        
        btnCommit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    editContainer.captureFields(td);
                } catch (SchedulerArgumentException sae) {
                    editContainer.editor.showError(sae);
                    return;
                }
                
                if (edit) {
                    schedulerModel.editTrigger(triggerIdx);
                } else {
                    schedulerModel.addTrigger(td);
                    Utils.getSchedulerPrefs().put(TRIGGER_TYPE_KEY,
                            type.getProgName());
                }
                dlgEdit.dispose();
            }
        });
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dlgEdit.dispose();
            }
        });
        
        Utils.callFromEDT(true, new Runnable() {
            public void run() {
                dlgEdit.setVisible(true);
            }
        });
    }
}

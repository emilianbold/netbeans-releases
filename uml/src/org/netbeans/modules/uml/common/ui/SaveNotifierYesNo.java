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

package org.netbeans.modules.uml.common.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class SaveNotifierYesNo 
{
    private static  SaveNotifierYesNo instance = null;
    public final static Object SAVE_ALWAYS_OPTION = new Integer(9999);
    public final static int SAVE_DISCARD_CANCEL = 0;
    public final static int SAVE_CANCEL = 1;
    
    public static SaveNotifierYesNo getDefault()
    {
        if (instance == null)
            instance = new SaveNotifierYesNo();
        
        return instance;
    }
    
    /**
     * Creates a new instance of SaveNotifierYesNo
     */
    private SaveNotifierYesNo() 
    {
    }
    
    public Object displayNotifier(
            String dialogTitle, String message, int buttonType) 
    {
        DialogManager dmgr = new DialogManager(dialogTitle, message, buttonType);
        dmgr.prompt();
        
        return dmgr.getResult();
    }
    
    public Object displayNotifier(
            String dialogTitle, String saveType, String saveName) 
    {
        // create a default save-discard-cancel dialog with a default message
        DialogManager dmgr = new DialogManager(dialogTitle, saveType, saveName);
        dmgr.prompt();
        
        return dmgr.getResult();
    }
    
    
    private static class DialogManager implements ActionListener 
    {
        private DialogDescriptor dialogDesc = null;
        private Dialog dialog = null;
        private Object result = DialogDescriptor.CANCEL_OPTION;

        private final Object[] closeOptions =
        {
            DialogDescriptor.DEFAULT_OPTION,
            DialogDescriptor.NO_OPTION,
            DialogDescriptor.DEFAULT_OPTION,
            DialogDescriptor.YES_OPTION
        };
        
         public DialogManager(
            String dialogTitle, String saveType, String saveName) {
             
            this(dialogTitle,
                    NbBundle.getMessage(SaveNotifierYesNo.class, 
                    "LBL_SaveNotifierYesNoDialog_Question", saveType, saveName),
                    SaveNotifierYesNo.SAVE_DISCARD_CANCEL);
         }
        
        public DialogManager(
            String dialogTitle, String message, int buttonType)
        {
            JButton saveButton = new JButton(NbBundle.getMessage(
                SaveNotifierYesNo.class, "LBL_SaveButton")); // NOI18N
            
            saveButton.setActionCommand(NbBundle.getMessage(
                    SaveNotifierYesNo.class, "LBL_SaveButton")); // NOI18N

            Object buttonOptions[] = {
                saveButton, 
                DialogDescriptor.CANCEL_OPTION };
            
            
            if (buttonType == SaveNotifierYesNo.SAVE_DISCARD_CANCEL)
            {
                JButton discardButton = new JButton(NbBundle.getMessage(
                SaveNotifierYesNo.class, "LBL_DiscardButton")); // NOI18N
            
                discardButton.setActionCommand(NbBundle.getMessage(
                    SaveNotifierYesNo.class, "LBL_DiscardButton")); // NOI18N
            
                Mnemonics.setLocalizedText(
                    discardButton, NbBundle.getMessage(
                        SaveNotifierYesNo.class, "LBL_DiscardButton")); // NOI18N
                
                Object buttons[] = {
                    saveButton, 
                    discardButton,
                    DialogDescriptor.CANCEL_OPTION };
                
                buttonOptions = buttons;
            }
            
            dialogDesc = new DialogDescriptor(
                message,  // message to display
                dialogTitle, // title
                true, // modal?
                buttonOptions,
                saveButton, // default option
                DialogDescriptor.DEFAULT_ALIGN,
                null, // help context
                this, // button action listener
                false); // leaf?
            
            dialogDesc.setMessageType(DialogDescriptor.QUESTION_MESSAGE);
            dialogDesc.setClosingOptions(closeOptions);
            dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        }
        
        private void prompt() 
        {
            dialog.setVisible(true);
        }
        
        public void actionPerformed(ActionEvent actionEvent) 
        {
            if (actionEvent.getActionCommand().equalsIgnoreCase(
                NbBundle.getMessage(SaveNotifierYesNo.class, "LBL_SaveButton"))) // NOI18N
            {
                result = DialogDescriptor.YES_OPTION;
            }
            
            else if (actionEvent.getActionCommand().equalsIgnoreCase(
                NbBundle.getMessage(SaveNotifierYesNo.class, "LBL_DiscardButton"))) // NOI18N))
            {
                result = DialogDescriptor.NO_OPTION;
            }
            else // Cancel or 'x' box close
                result = DialogDescriptor.CANCEL_OPTION;
            
            dialog.setVisible(false);
            dialog.dispose();
        }
        
        public Object getResult() 
        {
            return result;
        }
    }
    
}

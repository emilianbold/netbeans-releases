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

package org.netbeans.modules.registration;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.netbeans.modules.reglib.StatusData;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;

/** The action that shows the Register dialog.
*
* @author Marek Slama
*/
public class RegisterAction extends CallableSystemAction implements ActionListener {
    private JButton registerNow = new JButton();
    private JButton registerLater = new JButton();
    private JButton registerNever = new JButton();
    private ActionListener l = new ActionListener () {
        public void actionPerformed (ActionEvent ev) {
            cmd = ev.getActionCommand();
        }
    };
    
    private String cmd;
    
    public RegisterAction () {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        
        registerNow.addActionListener(l);
        registerNow.setActionCommand(StatusData.STATUS_REGISTERED);
        //registerNow.setText(NbBundle.getMessage(RegisterAction.class,"LBL_RegisterNow"));
        Mnemonics.setLocalizedText(registerNow, NbBundle.getMessage(
                RegisterAction.class, "LBL_RegisterNow"));
        registerNow.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(RegisterAction.class,"ACSN_RegisterNow"));
        registerNow.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(RegisterAction.class,"ACSD_RegisterNow"));
        
        registerLater.addActionListener(l);
        registerLater.setActionCommand(StatusData.STATUS_LATER);
        //registerLater.setText(NbBundle.getMessage(RegisterAction.class,"LBL_RegisterLater"));
        Mnemonics.setLocalizedText(registerLater, NbBundle.getMessage(
                RegisterAction.class, "LBL_RegisterLater"));
        registerLater.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(RegisterAction.class,"ACSN_RegisterLater"));
        registerLater.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(RegisterAction.class,"ACSD_RegisterLater"));
        
        registerNever.addActionListener(l);
        registerNever.setActionCommand(StatusData.STATUS_NEVER);
        //registerNever.setText(NbBundle.getMessage(RegisterAction.class,"LBL_RegisterNever"));
        Mnemonics.setLocalizedText(registerNever, NbBundle.getMessage(
                RegisterAction.class, "LBL_RegisterNever"));
        registerNever.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(RegisterAction.class,"ACSN_RegisterNever"));
        registerNever.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(RegisterAction.class,"ACSD_RegisterNever"));
    }

    public void performAction () {
        NbConnection.updateStatus(StatusData.STATUS_REGISTERED);
    }
    
    public void showDialog () {
        DialogDescriptor descriptor = new DialogDescriptor(
            new ReminderPanel(),
            NbBundle.getMessage(RegisterAction.class, "Register_title"),
            true,
                new Object[] {registerNow, registerNever, registerLater},
                null,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setResizable(false);
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
        NbConnection.updateStatus(cmd);
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RegisterAction.class);
    }
    
    public String getName() {
        return NbBundle.getMessage(RegisterAction.class, "Register");
    }
    
}

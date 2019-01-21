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

package org.netbeans.modules.collab.ui;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;

/** The action that shows the Register dialog.
*
*/
public class ServerWarningAction extends CallableSystemAction implements ActionListener {

    private JButton okButton = new JButton();

    public ServerWarningAction () {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N

        Mnemonics.setLocalizedText(okButton, NbBundle.getMessage(
                ServerWarningAction.class, "LBL_OK"));
        okButton.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(ServerWarningAction.class,"ACSN_OK"));
        okButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ServerWarningAction.class,"ACSD_OK"));
    }

    public void performAction () {
    }
    
    public void showDialog () {
        //Disabled till we get final UI/text for dialog
        DialogDescriptor descriptor = new DialogDescriptor(
            new ServerWarningPanel(),
            NbBundle.getMessage(ServerWarningAction.class, "ServerWarning_title"),
            true,
            new Object[] {okButton},
            okButton,
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
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ServerWarningAction.class);
    }
    
    public String getName() {
        return NbBundle.getMessage(ServerWarningAction.class, "LBL_ServerWarningAction");
    }
    
}

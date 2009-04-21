/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.netbeans.modules.cnd.debugger.gdb.DebuggerStartException;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public final class DebugCoreAction implements ActionListener {
    private JButton bOk;
    private JButton bCancel;

    public void actionPerformed(ActionEvent e) {
        bOk = new JButton (NbBundle.getMessage (DebugCoreAction.class, "CTL_Ok")); // NOI18N
        bCancel = new JButton (NbBundle.getMessage (DebugCoreAction.class, "CTL_Cancel")); // NOI18N
        bOk.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage (DebugCoreAction.class, "ACSD_CTL_Ok")); // NOI18N
        bCancel.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage (DebugCoreAction.class, "ACSD_CTL_Cancel")); // NOI18N
        SelectCorePanel panel = new SelectCorePanel(bOk);
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(DebugCoreAction.class, "SELECT_CORE_TITLE")); // NOI18N
        dd.setOptions (new JButton[] {
            bOk, bCancel
        });
        if (DialogDisplayer.getDefault().notify(dd) == bOk) {
            try {
                GdbDebugger.debugCore(panel.getCorePath(), panel.getProjectInformation());
            } catch (DebuggerStartException dse) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(DebugCoreAction.class,
                       "ERR_UnexpectedDebugCoreFailure", panel.getCorePath()))); // NOI18N
            }
        }
    }
}

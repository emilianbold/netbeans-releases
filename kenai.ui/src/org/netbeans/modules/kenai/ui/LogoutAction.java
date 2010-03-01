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
package org.netbeans.modules.kenai.ui;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author Jan Becicka
 */
public final class LogoutAction extends AbstractAction {

    private static LogoutAction instance;
    private static final String LOGOUT = NbBundle.getMessage(LogoutAction.class, "CTL_LogoutAction");

    private LogoutAction() {
        super(LOGOUT);
    }

    public static synchronized LogoutAction getDefault() {
        if (instance==null) {
            instance=new LogoutAction();
        }
        return instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Collection<Kenai> kenais = KenaiManager.getDefault().getKenais();
        if (kenais.size() == 1) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    kenais.iterator().next().logout();
                }
            });
            return;
        }
        final LogoutPanel panel = new LogoutPanel();
        DialogDescriptor dd = new DialogDescriptor(
                panel,
                NbBundle.getMessage(LogoutAction.class, "LOGOUT_TITLE"),
                true,
                new Object[]{LOGOUT, DialogDescriptor.CANCEL_OPTION},
                LOGOUT,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);

        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == LOGOUT) {
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    for (Kenai k : panel.getSelectedKenais()) {
                        if (k.getStatus() != Kenai.Status.OFFLINE) {
                            k.logout();
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean isEnabled() {
        for (Kenai k: KenaiManager.getDefault().getKenais()) {
            if (k.getStatus()!=Kenai.Status.OFFLINE) {
                return true;
            }
        }
        return false;
    }
}

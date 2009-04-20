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

package org.netbeans.modules.cnd.remote.server;

import java.awt.Dialog;
import org.netbeans.modules.cnd.api.remote.ServerListDisplayer;
import org.netbeans.modules.cnd.ui.options.ServerListDisplayerEx;
import org.netbeans.modules.cnd.ui.options.ToolsCacheManager;
import org.netbeans.modules.cnd.remote.ui.EditServerListDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * ServerListDisplayer implementation
 * @author Vladimir Kvashin
 */
@ServiceProvider(service = ServerListDisplayer.class)
public class RemoteServerListDisplayer extends ServerListDisplayerEx {

    @Override
    protected boolean showServerListDialogImpl() {
        ToolsCacheManager cacheManager = new ToolsCacheManager();
        if (showServerListDialog(cacheManager)) {
            cacheManager.applyChanges();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean showServerListDialogImpl(ToolsCacheManager cacheManager) {
        EditServerListDialog dlg = new EditServerListDialog(cacheManager);
        DialogDescriptor dd = new DialogDescriptor(dlg, NbBundle.getMessage(RemoteServerList.class, "TITLE_EditServerList"), true,
                    DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);
        dlg.setDialogDescriptor(dd);
        dd.addPropertyChangeListener(dlg);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            cacheManager.setHosts(dlg.getHosts());
            cacheManager.setDefaultIndex(dlg.getDefaultIndex());
            return true;
        } else {
            return false;
        }
    }

}

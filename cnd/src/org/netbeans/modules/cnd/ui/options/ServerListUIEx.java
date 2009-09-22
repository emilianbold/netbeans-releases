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

package org.netbeans.modules.cnd.ui.options;

import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.remote.ServerListUI;
import org.openide.util.Lookup;

/**
 * Two modules - cnd.remote and cnd.core -
 * knows more about displaying server list dialog:
 * they share ToolsCacheManager.
 * That's why we had to extend ServerListDisplayer.
 *
 * @author Vladimir Kvashin
 */
public abstract class ServerListUIEx extends ServerListUI {

    /**
     * Displays server list dialog.
     * Allows to add, remove or modify servers in the list
     * @return true in the case user pressed OK, otherwise
     */
    protected abstract boolean showServerListDialogImpl(ToolsCacheManager cacheManager);

    public static boolean showServerListDialog(ToolsCacheManager cacheManager) {
        ServerListUI displayer = Lookup.getDefault().lookup(ServerListUI.class);
        if (displayer != null) {
            if (displayer instanceof ServerListUIEx) {
                return ((ServerListUIEx) displayer).showServerListDialogImpl(cacheManager);
            } else {
                Logger.getLogger("cnd.remote.logger").warning( //NOI18N
                        displayer.getClass().getName() + "should extend " + //NOI18N
                        ServerListUIEx.class.getSimpleName());
                return false;
            }
        } else {
            Logger.getLogger("cnd.remote.logger").warning( //NOI18N
                    "Can not find " + ServerListUIEx.class.getSimpleName()); //NOI18N
            return false;
        }
    }
}

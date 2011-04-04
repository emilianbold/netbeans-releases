/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.autoupdate.ui.api;

import java.util.List;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.modules.autoupdate.ui.wizards.InstallUnitWizard;
import org.netbeans.modules.autoupdate.ui.wizards.InstallUnitWizardModel;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;

/** Access to UI features of PluginManager that can be useful in other modules
 * as well. 
 * @since 1.21
 * 
 * @author Jirka Rechtacek
 */
public final class PluginManager {
    private PluginManager() {
    }

    /** Open standard dialog for installing set of modules. Shows it to the user,
     * asks for confirmation, license acceptance, etc. The whole operation requires
     * AWT dispatch thread access (to show the dialog) and blocks 
     * (until the user clicks through), so either call from AWT dispatch thread
     * directly, or be sure you hold no locks and block no progress of other
     * threads to avoid deadlocks.
<pre>
{@link OperationContainer}<InstallSupport> container = OperationContainer.createForInstall();
for ({@link UpdateUnit} u : {@link UpdateManager#getUpdateUnits(org.netbeans.api.autoupdate.UpdateManager.TYPE[]) UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE)}) {
    if (u.getCodeName().matches("org.my.favorite.module")) {
        if (u.getAvailableUpdates().isEmpty()) {
            continue;
        }
        container.add(u.getAvailableUpdates().get(0));
    }
}
PluginManager.openInstallWizard(container);
</pre>
     * 
     * @param container the container with list of modules for install
     * @return true if all the requested modules were successfullly installed, 
     *    false otherwise.
     */
    public static boolean openInstallWizard(OperationContainer<InstallSupport> container) {
        if (container == null) {
            throw new IllegalArgumentException ("OperationContainer cannot be null."); // NOI18N
        }
        List<OperationContainer.OperationInfo<InstallSupport>> all = container.listAll ();
        if (all.isEmpty ()) {
            throw new IllegalArgumentException ("OperationContainer cannot be empty."); // NOI18N
        }
        List<OperationContainer.OperationInfo<InstallSupport>> invalid = container.listInvalid();
        if (! invalid.isEmpty ()) {
            throw new IllegalArgumentException ("OperationContainer cannot contain invalid elements but " + invalid); // NOI18N
        }
        OperationInfo<InstallSupport> info = all.get (0);
        OperationType doOperation = info.getUpdateUnit ().getInstalled () == null ? OperationType.INSTALL : OperationType.UPDATE;
        return new InstallUnitWizard ().invokeWizard (new InstallUnitWizardModel (doOperation, container), false);
    }
}

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

package org.netbeans.modules.php.project.ui.actions;

import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.Utils;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Download files from remote connection.
 * @author Tomas Mysik
 */
public class DownloadCommand extends Command implements Displayable {
    public static final String ID = "download"; // NOI18N
    public static String DISPLAY_NAME = NbBundle.getMessage(DownloadCommand.class,
            "LBL_DownloadCommand");
    public DownloadCommand(PhpProject project) {
        super(project);
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @Override
    public void invokeAction(Lookup context) throws IllegalArgumentException {
        // XXX use visibility query!!!
        FileObject[] selectedFiles = CommandUtils.filesForSelectedNodes();
        assert selectedFiles.length > 0 : "At least one node must be selected for Upload action";

        FileObject[] sources = Utils.getSourceObjects(getProject());

        RemoteClient remoteClient = getRemoteClient();
        try {
            remoteClient.connect();
            remoteClient.download(sources[0], selectedFiles);
        } catch (RemoteException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                remoteClient.disconnect();
            } catch (RemoteException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public boolean isActionEnabled(Lookup context) throws IllegalArgumentException {
        // XXX add support for source directories&files
        return isRemoteConfigSelected();
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }
}

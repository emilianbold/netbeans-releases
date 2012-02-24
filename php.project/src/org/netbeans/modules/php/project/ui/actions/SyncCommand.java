/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.actions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.sync.SyncController;
import org.netbeans.modules.php.project.connections.sync.SyncController.SyncResult;
import org.netbeans.modules.php.project.connections.sync.SyncController.SyncResultProcessor;
import org.netbeans.modules.php.project.runconfigs.RunConfigRemote;
import org.netbeans.modules.php.project.ui.actions.support.Displayable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Synchronize remote and local files.
 */
public class SyncCommand extends RemoteCommand implements Displayable {

    static final Logger LOGGER = Logger.getLogger(SyncCommand.class.getName());

    public static final String ID = "synchronize"; // NOI18N
    @NbBundle.Messages("SyncCommand.label=Synchronize")
    public static final String DISPLAY_NAME = Bundle.SyncCommand_label();


    public SyncCommand(PhpProject project) {
        super(project);
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public boolean isFileSensitive() {
        return false;
    }

    @Override
    protected Runnable getContextRunnable(Lookup context) {
        return new Runnable() {
            @Override
            public void run() {
                synchronize();
            }
        };
    }

    void synchronize() {
        RemoteConfiguration remoteConfiguration = RunConfigRemote.forProject(getProject()).getRemoteConfiguration();
        InputOutput remoteLog = getRemoteLog(remoteConfiguration.getDisplayName());
        RemoteClient remoteClient = getRemoteClient(remoteLog);
        new SyncController(getProject(), remoteClient, remoteConfiguration).synchronize(new PrintSyncResultProcessor(remoteLog));
    }

    //~ Inner classes

    private static final class PrintSyncResultProcessor implements SyncResultProcessor {

        private final InputOutput remoteLog;


        public PrintSyncResultProcessor(InputOutput remoteLog) {
            this.remoteLog = remoteLog;
        }

        @NbBundle.Messages({
            "SyncCommand.download.title=Download",
            "SyncCommand.upload.title=Upload",
            "SyncCommand.delete.title=Delete",
        })
        @Override
        public void process(SyncResult result) {
            try {
                remoteLog.getOut().reset();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            remoteLog.select();
            processTransferInfo(result.getDownloadTransferInfo(), remoteLog, Bundle.SyncCommand_download_title());
            processTransferInfo(result.getUploadTransferInfo(), remoteLog, Bundle.SyncCommand_upload_title());
            processTransferInfo(result.getDeleteTransferInfo(), remoteLog, Bundle.SyncCommand_delete_title());
        }

    }

}

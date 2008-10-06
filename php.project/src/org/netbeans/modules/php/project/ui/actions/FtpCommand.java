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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.TransferFile;
import org.netbeans.modules.php.project.connections.TransferInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Radek Matous
 */
public abstract class FtpCommand extends Command {
    private static final char SEP_CHAR = '='; // NOI18N
    private static final int MAX_TYPE_SIZE = getFileTypeLabelMaxSize() + 2;
    private static final RequestProcessor RP = new RequestProcessor("FTP", 1);//NOI18N
    private static final Queue<Runnable> RUNNABLES = new ConcurrentLinkedQueue<Runnable>();
    private static final RequestProcessor.Task TASK = RP.create(new Runnable() {

        public void run() {
            Runnable toRun = RUNNABLES.poll();
            while (toRun != null) {
                toRun.run();
                toRun = RUNNABLES.poll();
            }

        }
    }, true);

    public FtpCommand(PhpProject project) {
        super(project);

    }

    @Override
    public final void invokeAction(Lookup context) throws IllegalArgumentException {
        if (!isRunConfigurationValid()) {
            // property not set yet
            return;
        }
        RUNNABLES.add(getContextRunnable(context));
        TASK.schedule(0);
    }

    @Override
    public final boolean isActionEnabled(Lookup context) throws IllegalArgumentException {
        // WARNING context can be null, see RunCommand.invokeAction()
        return isRemoteConfigSelected() && getRemoteConfiguration() != null && TASK.isFinished();
    }

    protected abstract Runnable getContextRunnable(Lookup context);

    @Override
    public final boolean asyncCallRequired() {
        return false;
    }

    protected InputOutput getFtpLog(String displayName) {
        InputOutput io = IOProvider.getDefault().getIO(NbBundle.getMessage(Command.class, "LBL_FtpLog", displayName), false);
        io.select();
        try {
            io.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return io;
    }

    protected RemoteClient getRemoteClient(InputOutput io) {
        return new RemoteClient(getRemoteConfiguration(), io, getRemoteDirectory());
    }

    protected RemoteConfiguration getRemoteConfiguration() {
        String configName = getRemoteConfigurationName();
        assert configName != null && configName.length() > 0 : "Remote configuration name must be selected";

        return RemoteConnections.get().remoteConfigurationForName(configName);
    }

    protected void processRemoteException(RemoteException remoteException) {
        String title = NbBundle.getMessage(Command.class, "LBL_FtpError");
        StringBuilder message = new StringBuilder(remoteException.getMessage());
        String remoteServerAnswer = remoteException.getRemoteServerAnswer();
        Throwable cause = remoteException.getCause();
        if (remoteServerAnswer != null && remoteServerAnswer.length() > 0) {
            message.append(NbBundle.getMessage(Command.class, "MSG_FtpErrorReason", remoteServerAnswer));
        } else if (cause != null) {
            message.append(NbBundle.getMessage(Command.class, "MSG_FtpErrorReason", cause.getMessage()));
        }
        NotifyDescriptor notifyDescriptor = new NotifyDescriptor(
                message.toString(),
                title,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.ERROR_MESSAGE,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(notifyDescriptor);
    }

    protected void processTransferInfo(TransferInfo transferInfo, InputOutput io) {
        OutputWriter out = io.getOut();
        OutputWriter err = io.getErr();

        out.println();
        out.println(NbBundle.getMessage(FtpCommand.class, "LBL_FtpSummary"));
        StringBuilder sep = new StringBuilder(20);
        for (int i = 0; i < sep.capacity(); i++) {
            sep.append(SEP_CHAR);
        }
        out.println(sep.toString());

        int maxRelativePath = getRelativePathMaxSize(transferInfo);
        long size = 0;
        int files = 0;
        if (transferInfo.hasAnyTransfered()) {
            out.println(NbBundle.getMessage(FtpCommand.class, "LBL_FtpSucceeded"));
            for (TransferFile file : transferInfo.getTransfered()) {
                printSuccess(out, maxRelativePath, file);
                if (file.isFile()) {
                    size += file.getSize();
                    files++;
                }
            }
        }

        if (transferInfo.hasAnyFailed()) {
            err.println(NbBundle.getMessage(FtpCommand.class, "LBL_FtpFailed"));
            for (Map.Entry<TransferFile, String> entry : transferInfo.getFailed().entrySet()) {
                printError(err, maxRelativePath, entry.getKey(), entry.getValue());
            }
        }

        if (transferInfo.hasAnyIgnored()) {
            err.println(NbBundle.getMessage(FtpCommand.class, "LBL_FtpIgnored"));
            for (Map.Entry<TransferFile, String> entry : transferInfo.getIgnored().entrySet()) {
                printError(err, maxRelativePath, entry.getKey(), entry.getValue());
            }
        }

        // summary
        long runtime = transferInfo.getRuntime();
        String timeUnit = NbBundle.getMessage(FtpCommand.class, "LBL_TimeUnitMilisecond");
        if (runtime > 1000) {
            runtime /= 1000;
            timeUnit = NbBundle.getMessage(FtpCommand.class, "LBL_TimeUnitSecond");
        }
        double s = size / 1024.0;
        String sizeUnit = NbBundle.getMessage(FtpCommand.class, "LBL_SizeUnitKilobyte");
        if (s > 1024) {
            s /= 1024.0;
            sizeUnit = NbBundle.getMessage(FtpCommand.class, "LBL_SizeUnitMegabyte");
        }
        Object[] params = new Object[] {
            runtime,
            timeUnit,
            files,
            s,
            sizeUnit,
        };
        out.println(NbBundle.getMessage(FtpCommand.class, "MSG_FtpRuntimeAndSize", params));
    }

    private void printSuccess(OutputWriter writer, int maxRelativePath, TransferFile file) {
        String msg = String.format("%-" + MAX_TYPE_SIZE + "s %-" + maxRelativePath + "s", getFileTypeLabel(file), file.getRelativePath());
        writer.println(msg);
    }

    private void printError(OutputWriter writer, int maxRelativePath, TransferFile file, String reason) {
        String msg = String.format("%-" + MAX_TYPE_SIZE + "s %-" + maxRelativePath + "s   %s", getFileTypeLabel(file), file.getRelativePath(), reason);
        writer.println(msg);
    }

    private String getFileTypeLabel(TransferFile file) {
        String type = null;
        if (file.isDirectory()) {
            type = "LBL_TypeDirectory"; // NOI18N
        } else if (file.isFile()) {
            type = "LBL_TypeFile"; // NOI18N
        } else {
            type = "LBL_TypeUnknown"; // NOI18N
        }
        return NbBundle.getMessage(FtpCommand.class, type);
    }

    private static int getFileTypeLabelMaxSize() {
        String str = NbBundle.getMessage(FtpCommand.class, "LBL_TypeDirectory");
        int max = str.length();
        str = NbBundle.getMessage(FtpCommand.class, "LBL_TypeFile");
        if (max < str.length()) {
            max = str.length();
        }
        str = NbBundle.getMessage(FtpCommand.class, "LBL_TypeUnknown");
        if (max < str.length()) {
            max = str.length();
        }
        return max;
    }

    private int getRelativePathMaxSize(TransferInfo transferInfo) {
        int max = getRelativePathMaxSize(transferInfo.getTransfered());
        int size = getRelativePathMaxSize(transferInfo.getFailed().keySet());
        if (size > max) {
            max = size;
        }
        size = getRelativePathMaxSize(transferInfo.getIgnored().keySet());
        if (size > max) {
            max = size;
        }
        return max + 2;
    }

    private int getRelativePathMaxSize(Collection<TransferFile> files) {
        int max = 0;
        for (TransferFile file : files) {
            int length = file.getRelativePath().length();
            if (length > max) {
                max = length;
            }
        }
        return max;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.nativeexecution.terminal.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.terminal.api.IOEmulation;
import org.netbeans.nativeexecution.terminal.ui.TargetSelector;
import org.netbeans.nativeexecution.terminal.util.EnvSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

public final class NativeExecutionTestAction implements ActionListener {

    private static TargetSelector cfgPanel = new TargetSelector();

    private static IOProvider getIOProvider() {
        IOProvider iop = IOProvider.get("Terminal");       // NOI18N
        if (iop == null) {
            System.out.printf("IOProviderActionSupport.getTermIOProvider() couldn't find our provider\n");
            iop = IOProvider.getDefault();
        }
        return iop;
    }

    public void actionPerformed(ActionEvent e) {
        DialogDescriptor dd = new DialogDescriptor(cfgPanel, "Configure dialog", // NOI18N
                true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);

        Dialog cfgDialog = DialogDisplayer.getDefault().createDialog(dd);
        cfgDialog.setVisible(true);

        if (dd.getValue() != DialogDescriptor.OK_OPTION) {
            return;
        }

        final ExecutionEnvironment env = cfgPanel.getExecutionEnvironment();

        EnvSupport.ensureConnected(env);

        final String cmd = cfgPanel.getCmd();
        final boolean runInPty = cfgPanel.isPtyMode();
        final boolean useTerminalIO = cfgPanel.isTerminalIO();

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);

        // Extract executable and arguments from the line... 
        int spidx = cmd.indexOf(' ');
        String exec;
        String[] argsArray;

        if (spidx < 0) {
            exec = cmd;
            argsArray = new String[0];
        } else {
            exec = cmd.substring(0, spidx);
            argsArray = cmd.substring(spidx + 1).split(" +"); // NOI18N
        }

        npb.setExecutable(exec).setArguments(argsArray);
        npb.setUsePty(runInPty);

        // We cannot use ExecutionService in case of TerminalIO (?) ;(

        if (useTerminalIO) {
            IOProvider iop = getIOProvider();
            InputOutput io = iop.getIO(cmd, true);
            io.select();

            if (IOEmulation.isSupported(io)) {
                npb.getEnvironment().put("TERM", IOEmulation.getEmulation(io)); // NOI18N
            } else {
                npb.getEnvironment().put("TERM", "dumb"); // NOI18N
            }

            try {
                PtySupport.connect(io, npb.call());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            IOProvider iop = IOProvider.getDefault();
            InputOutput io = iop.getIO(cmd, true);

            ExecutionDescriptor descr = new ExecutionDescriptor().controllable(true).frontWindow(true).
                    inputVisible(true).inputOutput(io).
                    outLineBased(true).showProgress(true);

            ExecutionService es = ExecutionService.newService(npb, descr, cmd);
            es.run();
        }
    }
}

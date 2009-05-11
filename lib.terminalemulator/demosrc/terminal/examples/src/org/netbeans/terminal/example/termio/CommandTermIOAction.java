/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.terminal.example.termio;

import javax.swing.JOptionPane;
import org.netbeans.terminal.example.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.IOProvider;

/**
 * Run a command under a pty and interact with it through _our_ implementation
 * of the io window.
 */
public final class CommandTermIOAction extends CallableSystemAction {

    private final TerminalIOProviderSupport support = new TerminalIOProviderSupport();

    public void performAction() {

        // Ask user what command they want to run
        String cmd = JOptionPane.showInputDialog("Command");
        if (cmd == null || cmd.trim().equals(""))
            return;

        IOProvider iop = TerminalIOProviderSupport.getIOProvider();
        support.executeCommand(iop, cmd);
    }

    public String getName() {
        return NbBundle.getMessage(CommandTermIOAction.class, "CTL_CommandTermIOAction");
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}

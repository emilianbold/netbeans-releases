/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.debugger.gdb.actions;

import org.netbeans.modules.cnd.debugger.gdb.disassembly.Disassembly;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public final class DisassemblyAction extends CallableSystemAction {

    public void performAction() {
        Disassembly.open();
    }

    public String getName() {
        return NbBundle.getMessage(DisassemblyAction.class, "CTL_DisassemblyAction");
    }

    @Override
    protected String iconResource() {
        return "org/netbeans/modules/cnd/debugger/gdb/resources/disassembly.png";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}

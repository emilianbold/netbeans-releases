/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.platform;

import org.netbeans.modules.python.platform.panels.PythonPlatformPanel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public final class PythonManagerAction extends CallableSystemAction {

    public void performAction() {
        PythonPlatformPanel.showPlatformManager();
    }

    public String getName() {
        return NbBundle.getMessage(PythonManagerAction.class, "CTL_PythonManagerAction");
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

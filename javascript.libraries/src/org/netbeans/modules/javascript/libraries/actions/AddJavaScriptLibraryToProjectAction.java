/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.javascript.libraries.actions;

import org.netbeans.modules.javascript.libraries.ui.AddJavaScriptLibraryToProjectPanel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public final class AddJavaScriptLibraryToProjectAction extends CallableSystemAction {

    public void performAction() {
       java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                final AddJavaScriptLibraryToProjectPanel dialog = new AddJavaScriptLibraryToProjectPanel(new javax.swing.JFrame(), true);
                dialog.showDialog();
            }
        });
    }

    public String getName() {
        return NbBundle.getMessage(AddJavaScriptLibraryToProjectAction.class, "CTL_TestAction");
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

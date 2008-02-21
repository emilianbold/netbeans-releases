/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.javascript.editing;

import java.awt.Dialog;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public final class BrowserSelectionAction extends CallableSystemAction {

    public void performAction() {
        BrowserPanel customizer = new BrowserPanel();
        JButton close =
                new JButton(NbBundle.getMessage(BrowserSelectionAction.class, "CTL_Close"));
        close.getAccessibleContext()
             .setAccessibleDescription(NbBundle.getMessage(BrowserSelectionAction.class, "AD_Close"));

        DialogDescriptor descriptor =
            new DialogDescriptor(customizer, NbBundle.getMessage(BrowserSelectionAction.class, "CTL_ChooseBrowser"),
                true, new Object[] { close }, close, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(BrowserSelectionAction.class), null);
        Dialog dlg = null;

        // TODO - do OK/Cancel here!
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
            final SupportedBrowsers supported = SupportedBrowsers.getInstance();
            supported.setSupported(customizer.getSelection());
            supported.setLanguageVersion(customizer.getChosenLanguage());
            // TODO - kick off a new parse job and lex job since we may have changed
            // the current language...
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
    }

    public String getName() {
        return NbBundle.getMessage(BrowserSelectionAction.class, "CTL_BrowserSelectionAction");
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
        return true;
    }
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.project;

import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.modules.versioning.system.cvss.ClientRuntime;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ui.actions.checkout.CheckoutAction;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.importcmd.ImportCommand;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

/**
 * Simple import command ExecutorSupport subclass.
 *
 * @author Petr Kuzel
 */
final class ImportExecutor extends ExecutorSupport {

    private final String module;
    private final String cvsRoot;

    public ImportExecutor(ImportCommand cmd, GlobalOptions options) {
        super(CvsVersioningSystem.getInstance(), cmd, options);
        module = cmd.getModule();
        cvsRoot = options.getCVSRoot();
    }

    protected void commandFinished(ClientRuntime.Result result) {
        if (result.getError() == null) {
            String msg = NbBundle.getMessage(ImportExecutor.class, "BK0009");
            NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.OK_CANCEL_OPTION);
            Object value = DialogDisplayer.getDefault().notify(descriptor);
            if (value == NotifyDescriptor.OK_OPTION) {
                CheckoutAction checkoutAction = (CheckoutAction) SystemAction.get(CheckoutAction.class);
                checkoutAction.checkout(cvsRoot, module);
            }
        }
    }

    // TODO detect conflics
    // test for "No conflicts created by this import"
}

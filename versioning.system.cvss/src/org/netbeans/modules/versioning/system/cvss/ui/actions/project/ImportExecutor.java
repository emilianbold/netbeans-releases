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

import java.io.File;

/**
 * Simple import command ExecutorSupport subclass.
 *
 * @author Petr Kuzel
 */
final class ImportExecutor extends ExecutorSupport {

    private final String module;
    private final String cvsRoot;
    private final boolean checkout;
    private final String workDir;

    /**
     * Creates new executor that on succesfull import
     * launches post checkout.
     *
     * @param cmd
     * @param options
     * @param checkout
     * @param workDir
     */
    public ImportExecutor(ImportCommand cmd, GlobalOptions options, boolean checkout, String workDir) {
        super(CvsVersioningSystem.getInstance(), cmd, options);
        module = cmd.getModule();
        cvsRoot = options.getCVSRoot();
        this.checkout = checkout;
        this.workDir = workDir;
    }

    protected void commandFinished(ClientRuntime.Result result) {
        if (result.getError() == null) {
            if (checkout) {
                CheckoutAction checkoutAction = (CheckoutAction) SystemAction.get(CheckoutAction.class);
                checkoutAction.checkout(cvsRoot, module, null, workDir, true);
            }
        }
    }

    // TODO detect conflics
    // test for "No conflicts created by this import"
}

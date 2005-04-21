/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.actions;

import org.openide.text.PrintSettings;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import java.awt.print.PrinterJob;


/** Sets up page for printing.
*/
public final class PageSetupAction extends CallableSystemAction {
    public PageSetupAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    public synchronized void performAction() {
        PrintSettings ps = (PrintSettings) PrintSettings.findObject(PrintSettings.class, true);
        PrinterJob pj = PrinterJob.getPrinterJob();
        ps.setPageFormat(pj.pageDialog(PrintSettings.getPageFormat(pj)));
    }

    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(PageSetupAction.class, "PageSetup");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(PageSetupAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/pageSetup.gif"; // NOI18N
    }
}

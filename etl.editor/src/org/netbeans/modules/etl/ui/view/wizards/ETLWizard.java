/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.etl.ui.view.wizards;

import java.awt.Dialog;
import java.awt.Dimension;

import javax.swing.SwingUtilities;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;

import com.sun.sql.framework.utils.Logger;

/**
 * Abstract base class for ETL Wizards. Interested classes should instantiate the correct
 * concrete implementation and call show() to display the wizard.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public abstract class ETLWizard {
    /* Log4J category string */
    private static final String LOG_CATEGORY = ETLWizard.class.getName();

    /** Common context for panels to exchange and store data. */
    protected ETLWizardContext context;

    /** Creates a new instance of ETLWizard */
    protected ETLWizard() {
        context = new ETLWizardContext();
    }

    /**
     * Gets context associated with this wizard.
     * 
     * @return ETLWizardContext instance associated with this wizard.
     */
    public ETLWizardContext getContext() {
        return context;
    }

    /**
     * Gets descriptor containing information on available panels. Concrete subclasses
     * should instantiate and define this descriptor.
     * 
     * @return WizardDescriptor associated with this object
     */
    public abstract WizardDescriptor getDescriptor();

    /**
     * Gets iterator used to cycle through available panels for this wizard. Concrete
     * subclasses should instantiate and define this iterator.
     * 
     * @return WizardDescriptor.Iterator associated with this object
     */
    public abstract WizardDescriptor.Iterator getIterator();

    /**
     * Displays the wizard and its associated panels.
     * 
     * @return true if user completed the wizard normally; false otherwise.
     */
    public boolean show() {
        // Set default return value.
        boolean response = false;

        try {
            initialize();

            final WizardDescriptor desc = getDescriptor();
            final Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
            dlg.setTitle(getDialogTitle());
            dlg.setPreferredSize(new Dimension(575, 425));

            if (context != null && desc != null) {
                context.setWizardDescriptor(desc);
            }

            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public synchronized void run() {
                        dlg.setVisible(true);
                    }
                });
            } else {
                dlg.setVisible(true);
            }

            if (desc != null) {
                if (desc.getValue() == WizardDescriptor.FINISH_OPTION) {
                    Logger.print(Logger.DEBUG, LOG_CATEGORY, "show()", "User finished the wizard."); // NOI18N

                    // Call method in concrete implementation to handle committal.
                    commit();
                    response = true;
                } else {
                    Logger.print(Logger.DEBUG, LOG_CATEGORY, "show()", "User closed or cancelled the wizard."); // NOI18N

                    // Call method in concrete implementation to handle cancellation.
                    cancel();
                }
            }
        } catch (Exception e) {
            Logger.printThrowable(Logger.DEBUG, LOG_CATEGORY, "show()", "Exception caught while performing wizard processing.", e); // NOI18N
            ErrorManager.getDefault().notify(e);
        } finally {
            // Call method in concrete implementation to do any necessary cleanup.
            cleanup();
        }

        return response;
    }

    /**
     * Performs processing to handle cancellation of this wizard.
     */
    protected abstract void cancel();

    /**
     * Performs processing to cleanup any resources used by this wizard.
     */
    protected abstract void cleanup();

    /**
     * Performs processing to handle committal of data gathered by this wizard.
     */
    protected abstract void commit();

    /**
     * Gets string label to display as title of dialog window.
     * 
     * @return String representing dialog title
     */
    protected String getDialogTitle() {
        return "ETL Wizard";
    }

    /**
     * Initializes the wizard.
     */
    protected abstract void initialize();
}


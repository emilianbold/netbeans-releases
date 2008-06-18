/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.dataview.editor.wizard;

import java.awt.Dialog;
import java.awt.Dimension;

import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;

/**
 * Abstract base class for Dataview Wizards. Interested classes should instantiate the correct
 * concrete implementation and call show() to display the wizard.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public abstract class DataViewWizard {
    protected DataViewWizardContext context;

    /** Creates a new instance of DataViewWizard */
    protected DataViewWizard() {
        context = new DataViewWizardContext();
    }

    /**
     * Gets context associated with this wizard.
     * 
     * @return DataViewWizardContext instance associated with this wizard.
     */
    public DataViewWizardContext getContext() {
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
            dlg.getAccessibleContext().setAccessibleDescription("This is the DataView Collaboration Definition Wizard");
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
                    commit();
                    response = true;
                } else {
                   cancel();
                }
            }
        } catch (Exception e) {
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
        return "DataView Wizard";
    }

    /**
     * Initializes the wizard.
     */
    protected abstract void initialize();
}


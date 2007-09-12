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

/*
 *
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.wizards;

import java.awt.Dialog;

import javax.swing.SwingUtilities;

import org.openide.WizardDescriptor;
import org.openide.DialogDisplayer;

/**
 * Abstract base class for JDBC Wizards. Interested classes should instantiate the correct concrete
 * implementation and call show() to display the wizard.
 * 
 * @author Jonathan Giron
 * @version 
 */
public abstract class JDBCWizard {

    /** Common context for panels to exchange and store data. */
    protected JDBCWizardContext context;

    /** Creates a new instance of JDBCWizard */
    protected JDBCWizard() {
        this.context = new JDBCWizardContext();
    }

    /**
     * Gets context associated with this wizard.
     * 
     * @return JDBCWizardContext instance associated with this wizard.
     */
    public JDBCWizardContext getContext() {
        return this.context;
    }

    /**
     * Gets descriptor containing information on available panels. Concrete subclasses should
     * instantiate and define this descriptor.
     * 
     * @return WizardDescriptor associated with this object
     */
    public abstract WizardDescriptor getDescriptor();

    /**
     * Gets iterator used to cycle through available panels for this wizard. Concrete subclasses
     * should instantiate and define this iterator.
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
            this.initialize();

            final WizardDescriptor desc = this.getDescriptor();
            final Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
            dlg.setTitle(this.getDialogTitle());
            dlg.setSize(650, 450);

            if (this.context != null && desc != null) {
                this.context.setWizardDescriptor(desc);
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
                    // Call method in concrete implementation to handle committal.
                    // commit();
                    response = true;
                } else {
                    // Call method in concrete implementation to handle cancellation.
                    this.cancel();
                }
            }
        } catch (final Exception e) {

        } finally {
            // Call method in concrete implementation to do any necessary cleanup.
            this.cleanup();
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
     * Gets string label to display as title of dialog window.
     * 
     * @return String representing dialog title
     */
    protected String getDialogTitle() {
        return "JDBC Wizard";
    }

    /**
     * Initializes the wizard.
     */
    protected abstract void initialize();
}

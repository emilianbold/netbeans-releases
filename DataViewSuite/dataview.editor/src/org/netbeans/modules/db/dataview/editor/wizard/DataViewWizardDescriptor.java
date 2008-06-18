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

import java.awt.Dimension;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * Abstract subclass of NetBeans wizard descriptor. DataView wizard descriptors should be
 * concrete subclasses of this class.
 */
public abstract class DataViewWizardDescriptor extends WizardDescriptor {

    private WizardDescriptor.Iterator iterator;

    /**
     * Constructs an instance of DataViewWizardDescriptor with the given iterator and context.
     * 
     * @param iter WizardDescriptor.Iterator instance to cycle through the panels defined
     *        by this descriptor
     * @param context DataViewWizardDescriptor to serve as intermediate storage for data collected
     *        by panels in this descriptor.
     */
    public DataViewWizardDescriptor(WizardDescriptor.Iterator iter, Object context) {
        super(iter, context);
        this.iterator = iter;
    }

    /**
     * Overrides parent implementation to set customized look and feel settings.
     * 
     * @see org.openide.WizardDescriptor#initialize
     */
    protected void initialize() {
        initializeLookAndFeel();

        super.initialize();
    }

    /**
     * Initializes look-and-feel of wizard panels.
     */
    protected void initializeLookAndFeel() {

        try {
            String nbBundle1 = NbBundle.getMessage(DataViewWizardDescriptor.class, "{0}");//mLoc.t("BUND009: {0}");
            // Sets message format used for EDIT001panel title; {0} indicates component
            // name, if any; {1} indicates step info as provided by iterator.
            setTitleFormat(new MessageFormat(
                    nbBundle1));
        } catch (MissingResourceException e) {
          java.util.logging.Logger.getLogger(DataViewWizardDescriptor.class.getName()).info("Could not locate key for title format.{0}"+ e.getMessage());
        }

        // Number the steps.
        putProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N

        // Optional: set the size of the left pane explicitly:
        putProperty("WizardPanel_leftDimension", new Dimension(184, 500)); // NOI18N

        // Make the left pane appear:
        putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N

        // Make the left pane show list of steps etc.:
        putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
    }

    // Called when user moves forward or backward etc.:
    /**
     * Called whenever user chooses a navigation button (back, next, etc.) on the wizard.
     * 
     * @see org.openide.WizardDescriptor#updateState
     */
    protected void updateState() {
        if (iterator instanceof DataViewWizardIterator) {
            DataViewWizardIterator myIterator = (DataViewWizardIterator) iterator;

            // Make the left pane show list of steps etc.:
            putProperty("WizardPanel_contentData", myIterator.getSteps()); // NOI18N
            putProperty("WizardPanel_contentSelectedIndex", new Integer(myIterator.getIndex())); // NOI18N
        }

        super.updateState();
    }
}


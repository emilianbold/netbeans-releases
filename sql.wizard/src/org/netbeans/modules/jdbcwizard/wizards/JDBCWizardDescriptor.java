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

import java.awt.Dimension;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * Abstract subclass of NetBeans wizard descriptor. JDBC wizard descriptors should be concrete
 * subclasses of this class.
 */
public abstract class JDBCWizardDescriptor extends WizardDescriptor {

    /* Manages navigation of wizard panels */
    private WizardDescriptor.Iterator iterator;

    /**
     * Constructs an instance of JDBCWizardDescriptor with the given iterator and context.
     * 
     * @param iter WizardDescriptor.Iterator instance to cycle through the panels defined by this
     *            descriptor
     * @param context JDBCWizardContext to serve as intermediate storage for data collected by
     *            panels in this descriptor.
     */
    public JDBCWizardDescriptor(final WizardDescriptor.Iterator iter, final Object context) {
        super(iter, context);
        this.iterator = iter;
    }

    /**
     * Overrides parent implementation to set customized look and feel settings.
     * 
     * @see org.openide.WizardDescriptor#initialize
     */
    protected void initialize() {
        this.initializeLookAndFeel();

        super.initialize();
    }

    /**
     * Initializes look-and-feel of wizard panels.
     */
    protected void initializeLookAndFeel() {

        try {
            // Sets message format used for panel title; {0} indicates component
            // name, if any; {1} indicates step info as provided by iterator.
            this.setTitleFormat(new MessageFormat(NbBundle.getMessage(JDBCWizardDescriptor.class,
                    "MSG_titleformat_wiz_default")));
        } catch (final MissingResourceException e) {

        }

        // Number the steps.
        this.putProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N

        // Optional: set the size of the left pane explicitly:
        this.putProperty("WizardPanel_leftDimension", new Dimension(184, 500)); // NOI18N

        // Optional: show a help tab with special info about the pane:
        this.putProperty("WizardPanel_helpDisplayed", Boolean.TRUE); // NOI18N

        // Make the left pane appear:
        this.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N

        // Make the left pane show list of steps etc.:
        this.putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
    }

    // Called when user moves forward or backward etc.:
    /**
     * Called whenever user chooses a navigation button (back, next, etc.) on the wizard.
     * 
     * @see org.openide.WizardDescriptor#updateState
     */
    protected void updateState() {
        if (this.iterator instanceof JDBCWizardIterator) {
            final JDBCWizardIterator myIterator = (JDBCWizardIterator) this.iterator;

            // Make the left pane show list of steps etc.:
            this.putProperty("WizardPanel_contentData", myIterator.getSteps()); // NOI18N
            this.putProperty("WizardPanel_contentSelectedIndex", Integer.valueOf(String.valueOf(myIterator.getIndex()))); // NOI18N
        }

        super.updateState();
    }
}

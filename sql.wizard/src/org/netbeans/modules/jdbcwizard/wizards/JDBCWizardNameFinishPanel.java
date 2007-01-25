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

import org.openide.WizardDescriptor;

/**
 * Extends name input panel by implementing the WizardDescriptor.FinishablePanel interface to allow
 * successful closure of the wizard from this panel.
 * 
 * @author
 */
public class JDBCWizardNameFinishPanel extends JDBCWizardNamePanel implements WizardDescriptor.FinishablePanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * No-arg constructor for this wizard descriptor.
     */
    public JDBCWizardNameFinishPanel() {
        super();
    }

    /**
     * Create the wizard finish panel descriptor, using the given owner and panel title.
     * 
     * @param myOwner JDBCWizard that owns this panel
     * @param panelTitle text to display as panel title
     */
    public JDBCWizardNameFinishPanel(final JDBCCollaborationWizard myOwner, final String panelTitle) {
        super(myOwner, panelTitle);
    }

    public boolean isFinishPanel() {
        return true;
    }
}

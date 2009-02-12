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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * 
 * Copyright 2009 Sun Microsystems, Inc.
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

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Accepts input of unique name for new JDBC Collaboration instance.
 * 
 * @author
 */
public class JDBCWizardNamePanel implements WizardDescriptor.Panel {

    protected String collabName;

    /* Set <ChangeListeners> */
    protected final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

    protected JDBCCollaborationWizard owner;

    protected JTextField textField;

    protected String title;

    /**
     * Create the wizard panel descriptor, using the given panel title, content panel
     * 
     * @param myOwner JDBCWizard that owns this panel
     * @param panelTitle text to display as panel title
     */
    public JDBCWizardNamePanel(final JDBCCollaborationWizard myOwner, final String panelTitle) {
        this.title = panelTitle;
        this.owner = myOwner;
    }

    /**
     * @see JDBCWizardPanel#addChangeListener
     */
    public final void addChangeListener(final ChangeListener l) {
        synchronized (this.listeners) {
            this.listeners.add(l);
        }
    }

    /**
     * @see JDBCWizardPanel#fireChangeEvent
     */
    public void fireChangeEvent() {
        Iterator it;

        synchronized (this.listeners) {
            it = new HashSet<ChangeListener>(this.listeners).iterator();
        }

        final ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    /**
     * @see JDBCWizardPanel#getComponent
     */
    public Component getComponent() {
        return new JDBCWizardNamePanelUI (this, owner, title);
    }

    /**
     * Gets current value of collaboration name as entered by user.
     * 
     * @return current user-specified name
     */
    public String getCollabName() {
        return this.collabName;
    }

    /**
     * @seeJDBCWizardPanel#getHelp
     */
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return new HelpCtx(JDBCWizardNamePanel.class);

    }

    /**
     * @see JDBCWizardPanel#isValid
     */
    public boolean isValid() {
        boolean returnVal = false;
        if (this.collabName != null) {
            returnVal = true;
        }
        return returnVal;
    }

    /**
     * @see JDBCWizardPanel#readSettings
     */
    public void readSettings(final Object settings) {
        WizardDescriptor wd = null;
        if (settings instanceof JDBCWizardContext) {
            final JDBCWizardContext wizardContext = (JDBCWizardContext) settings;
            wd = (WizardDescriptor) wizardContext.getProperty(JDBCWizardContext.WIZARD_DESCRIPTOR);
        } else if (settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
        }

        if (wd != null) {
            final String myCollabName = (String) wd.getProperty(JDBCCollaborationWizard.COLLABORATION_NAME);
            this.textField.setText(myCollabName);
        }
    }

    /**
     * @see JDBCWizardPanel#removeChangeListener
     */
    public final void removeChangeListener(final ChangeListener l) {
        synchronized (this.listeners) {
            this.listeners.remove(l);
        }
    }

    /**
     * @see JDBCWizardPanel#storeSettings
     */
    public void storeSettings(final Object settings) {
        WizardDescriptor wd = null;
        if (settings instanceof JDBCWizardContext) {
            final JDBCWizardContext wizardContext = (JDBCWizardContext) settings;
            wd = (WizardDescriptor) wizardContext.getProperty(JDBCWizardContext.WIZARD_DESCRIPTOR);
        } else if (settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
            this.owner.setDescriptor(wd);
        }

        if (wd != null) {

            final Object selectedOption = wd.getValue();
            if (NotifyDescriptor.CANCEL_OPTION == selectedOption || NotifyDescriptor.CLOSED_OPTION == selectedOption) {
                return;
            }

            wd.putProperty(JDBCCollaborationWizard.COLLABORATION_NAME, this.collabName);

        }
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.etl.ui.view.wizards;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;


/**
 * Accepts input of unique name for new ETL Collaboration instance.
 * 
 * @author Sanjeeth Duvuru
 * @version $Revision$
 */
public class ETLCollaborationWizardNamePanel extends JPanel implements WizardDescriptor.Panel {

    private static transient final Logger mLogger = Logger.getLogger(ETLCollaborationWizardNamePanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    
    class NameFieldKeyAdapter extends KeyAdapter {

        /**
         * Overrides default implementation to notify listeners of new collab name value
         * in associated textfield.
         * 
         * @param e KeyEvent to be handled
         */
        @Override
        public void keyReleased(KeyEvent e) {
            String collaborationName = ETLCollaborationWizardNamePanel.this.textField.getText();

            if (collaborationName != null && collaborationName.trim().length() != 0) {
                ETLCollaborationWizardNamePanel.this.collabName = collaborationName.trim();
            } else {
                ETLCollaborationWizardNamePanel.this.collabName = null;
            }

            ETLCollaborationWizardNamePanel.this.fireChangeEvent();
        }
    }
    protected String collabName;

    /* Set <ChangeListeners> */
    protected final Set listeners = new HashSet(1);
    protected ETLCollaborationWizard owner;
    protected JTextField textField;
    protected String title;

    /**
     * No-arg constructor for this wizard descriptor.
     */
    public ETLCollaborationWizardNamePanel() {
        setLayout(new BorderLayout());

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new GridBagLayout());

        // Top filler panel to absorb 20% of any expansion up and down the page.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        outerPanel.add(new JPanel(), gbc);

        // Text field label.
        String nbBundle1 = mLoc.t("BUND061: New Collaboration Name:");
        JLabel header = new JLabel(nbBundle1.substring(15));
        header.setDisplayedMnemonic(nbBundle1.substring(15).charAt(0));
        header.getAccessibleContext().setAccessibleName(nbBundle1.substring(15));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets.right = 10;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        outerPanel.add(header, gbc);

        // Text field.
        textField = new JTextField();
        textField.addKeyListener(new NameFieldKeyAdapter());

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        outerPanel.add(textField, gbc);

        // Bottom filler panel to absorb 80% of any expansion up and down the page.
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.8;
        outerPanel.add(new JPanel(), gbc);

        add(outerPanel, BorderLayout.CENTER);
    }

    /**
     * Create the wizard panel descriptor, using the given panel title, content panel
     * 
     * @param myOwner ETLWizard that owns this panel
     * @param panelTitle text to display as panel title
     */
    public ETLCollaborationWizardNamePanel(ETLCollaborationWizard myOwner, String panelTitle) {
        this();

        title = panelTitle;
        this.setName(title);
        owner = myOwner;
    }

    /**
     * @see ETLWizardPanel#addChangeListener
     */
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    /**
     * @see ETLWizardPanel#fireChangeEvent
     */
    public void fireChangeEvent() {
        Iterator it;

        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }

        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    /**
     * @see ETLWizardPanel#getComponent
     */
    public Component getComponent() {
        return this;
    }

    /**
     * Gets current value of collaboration name as entered by user.
     * 
     * @return current user-specified name
     */
    public String getCollabName() {
        return collabName;
    }

    /**
     * @see ETLWizardPanel#getHelp
     */
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Indicates whether current contents of collaboration name textfield correspond to
     * the name of an existing collaboration in the current project.
     * 
     * @return true if textfield contains the name of an existing collab; false otherwise
     */
    public boolean isDuplicateCollabName() {
        String collaborationName = textField.getText();
        collaborationName = (collaborationName != null) ? collaborationName.trim() : null;

        boolean duplicated = false;

        //TODO - verify implementation. Where do collaboration files live?
        FileObject fo = FileUtil.getConfigFile(collaborationName);

        if (fo != null) { // file exists
            duplicated = true;
            String nbBundle2 = mLoc.t("BUND062: An object already exists in this project with the name {0}.  Please enter a unique name.",collaborationName);
            NotifyDescriptor.Message d1 = new NotifyDescriptor.Message(nbBundle2.substring(15), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d1);
            textField.requestFocus();
        }
        return duplicated;
    }

    /**
     * @see ETLWizardPanel#isValid
     */
    @Override
    public boolean isValid() {
        boolean returnVal = false;
        if (collabName != null) {
            returnVal = true;
        }
        return returnVal;
    }

    /**
     * @see ETLWizardPanel#readSettings
     */
    public void readSettings(Object settings) {
        WizardDescriptor wd = null;
        if (settings instanceof ETLWizardContext) {
            ETLWizardContext wizardContext = (ETLWizardContext) settings;
            wd = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);
        } else if (settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
        }

        if (wd != null) {
            String myCollabName = (String) wd.getProperty(ETLCollaborationWizard.COLLABORATION_NAME);
            textField.setText(myCollabName);
        }
    }

    /**
     * @see ETLWizardPanel#removeChangeListener
     */
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
     * @see ETLWizardPanel#storeSettings
     */
    public void storeSettings(Object settings) {
        WizardDescriptor wd = null;
        if (settings instanceof ETLWizardContext) {
            ETLWizardContext wizardContext = (ETLWizardContext) settings;
            wd = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);
        } else if (settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
            this.owner.setDescriptor(wd);
        }

        if (wd != null) {
            final Object selectedOption = wd.getValue();
            if (NotifyDescriptor.CANCEL_OPTION == selectedOption || NotifyDescriptor.CLOSED_OPTION == selectedOption) {
                return;
            }
            wd.putProperty(ETLCollaborationWizard.COLLABORATION_NAME, collabName);
        }
    }
}


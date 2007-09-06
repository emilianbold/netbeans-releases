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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.OperationException;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class LicenseApprovalStep implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    private LicenseApprovalPanel panel;
    private Component component;
    private InstallUnitWizardModel model = null;
    private boolean isApproved = false;
    private WizardDescriptor wd;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    private static final String HEAD = "LicenseApprovalPanel_Header_Head";
    private static final String CONTENT = "LicenseApprovalPanel_Header_Content";
    
    /** Creates a new instance of OperationDescriptionStep */
    public LicenseApprovalStep (InstallUnitWizardModel model) {
        this.model = model;
    }
    public boolean isFinishPanel() {
        return false;
    }

    public Component getComponent() {
        if (component == null) {
            panel = new LicenseApprovalPanel (model);
            panel.addPropertyChangeListener (LicenseApprovalPanel.LICENSE_APPROVED, new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent arg0) {
                        isApproved = panel.isApproved ();
                        if (isApproved) {
                            model.modifyOptionsForDoOperation (wd);
                        } else {
                            model.modifyOptionsForStartWizard (wd);
                        }
                        fireChange ();
                    }
            });
            component = new PanelBodyContainer (getBundle (HEAD), getBundle (CONTENT), panel);
            component.setPreferredSize (OperationWizardModel.PREFFERED_DIMENSION);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return null;
    }

    public void readSettings (WizardDescriptor wd) {
        this.wd = wd;
        if (panel != null && panel.isApproved ()) {
            model.modifyOptionsForDoOperation (wd);
        } else {
            model.modifyOptionsForStartWizard (wd);
        }
    }

    public void storeSettings (WizardDescriptor wd) {
        if (WizardDescriptor.NEXT_OPTION.equals (wd.getValue ())) {
            model.addApprovedLicenses (panel.getLicenses ());
        } else {
            model.modifyOptionsForStartWizard (wd);
        }
        if (WizardDescriptor.CANCEL_OPTION.equals (wd.getValue ()) || WizardDescriptor.CLOSED_OPTION.equals (wd.getValue ())) {
            try {
                model.doCleanup (true);
            } catch (OperationException x) {
                Logger.getLogger (InstallUnitWizardModel.class.getName ()).log (Level.INFO, x.getMessage (), x);
            }
        }
    }

    public boolean isValid() {
        return isApproved;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        List<ChangeListener> templist;
        synchronized (this) {
            templist = new ArrayList<ChangeListener> (listeners);
        }
	for (ChangeListener l: templist) {
            l.stateChanged(e);
        }
    }

    private String getBundle (String key) {
        return NbBundle.getMessage (LicenseApprovalStep.class, key);
    }
}

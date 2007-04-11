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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.InstallSupport;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public final class InstallUnitWizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {
    
    private int index;
    private List<WizardDescriptor.Panel> panels = new ArrayList<WizardDescriptor.Panel> ();
    private InstallUnitWizardModel installModel;
    
    public InstallUnitWizardIterator (InstallUnitWizardModel model) {
        this.installModel = model;
        createPanels ();
        index = 0;
    }
    
    public InstallUnitWizardModel getModel () {
        assert installModel != null;
        return installModel;
    }
    
    private void createPanels () {
        assert panels != null && panels.isEmpty() : "Panels are still empty";
        panels.add (new OperationDescriptionStep<InstallSupport> (installModel));
        if (! installModel.allLicensesApproved ()) {
            panels.add (new LicenseApprovalStep (installModel));
        }
        panels.add (new InstallStep (installModel));
    }
    
    @SuppressWarnings ("unchecked") // XXX Can I fix it?
    public WizardDescriptor.Panel<WizardDescriptor> current () {
        assert panels != null;
        return panels.get (index);
    }
    
    public String name () {
        return NbBundle.getMessage (InstallUnitWizardIterator.class, "InstallUnitWizard_Title");
    }
    
    public boolean hasNext () {
        return index < panels.size () - 1;
    }
    
    public boolean hasPrevious () {
        return index > 0 && ! (current () instanceof InstallStep); //???
    }
    
    public void nextPanel () {
        if (!hasNext ()) {
            throw new NoSuchElementException ();
        }
        index++;
    }
    
    public void previousPanel () {
        if (!hasPrevious ()) {
            throw new NoSuchElementException ();
        }
        index--;
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener (ChangeListener l) {}
    public void removeChangeListener (ChangeListener l) {}
    
}

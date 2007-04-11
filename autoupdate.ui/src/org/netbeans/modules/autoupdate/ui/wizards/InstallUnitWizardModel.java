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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.modules.autoupdate.ui.Containers;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallUnitWizardModel extends OperationWizardModel {
    private Installer installer = null;
    private boolean doUpdate;
    private static Set<String> approvedLicences = new HashSet<String> ();
    private OperationContainer<InstallSupport> installContainer;
    private InstallSupport support;
    
    /** Creates a new instance of InstallUnitWizardModel */
    public InstallUnitWizardModel (OperationContainer<InstallSupport> container) {
        this.doUpdate = (container == Containers.forUpdate ()) || (container == Containers.forUpdateNbms());
        installContainer = container;
        support = installContainer.getSupport();
        assert support != null;
        assert doUpdate ? Containers.forUpdate () != null : Containers.forAvailable () != null : "The container must exist!";
    }
    
    public OperationType getOperation () {
        return doUpdate ? OperationWizardModel.OperationType.UPDATE : OperationWizardModel.OperationType.INSTALL;
    }
    
    public OperationContainer getContainer() {
        return installContainer;
    }
    
    public boolean allLicensesApproved () {
        for (UpdateElement el : getAllUpdateElements ()) {
            if (! approvedLicences.contains (el.getLicence ())) {
                return false;
            }
        }
        return true;
    }
    
    public void addApprovedLicenses (Collection<String> licences) {
        approvedLicences.addAll (licences);
    }
    
    public InstallSupport getSupport () {
        return support;
    }
    
    public void setInstaller (Installer i) {
        installer = i;
    }
    
    public Installer getInstaller () {
        return installer;
    }
    
    public void doCleanup () throws OperationException {
        assert getContainer () != null;
        InstallSupport supp = getSupport ();
        if (supp != null) {
            supp.doCancel ();
        }
    }

}

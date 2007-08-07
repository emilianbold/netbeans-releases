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
import java.util.Set;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.modules.autoupdate.ui.Containers;
import org.openide.util.Exceptions;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallUnitWizardModel extends OperationWizardModel {
    private Installer installer = null;
    private OperationType doOperation;
    private static Set<String> approvedLicences = new HashSet<String> ();
    private InstallSupport support;
    private InstallSupport additionallySupport = null;
    
    /** Creates a new instance of InstallUnitWizardModel */
    public InstallUnitWizardModel (OperationType doOperation) {
        this.doOperation = doOperation;
        assert getBaseContainer () != null : "The base container for operation " + doOperation + " must exist!";
    }
    
    public OperationType getOperation () {
        return doOperation;
    }
    
    public OperationContainer getBaseContainer () {
        OperationContainer<InstallSupport> c = null;
        switch (getOperation ()) {
        case INSTALL :
            c = Containers.forAvailable ();
            support = Containers.forAvailable ().getSupport ();
            break;
        case UPDATE :
            c = Containers.forUpdate ();
            support = Containers.forUpdate ().getSupport ();
            break;
        case LOCAL_DOWNLOAD :
            OperationContainer<InstallSupport> additionallyContainer;
            if (Containers.forUpdateNbms ().listAll ().isEmpty ()) {
                c = Containers.forAvailableNbms ();
                additionallyContainer = Containers.forUpdateNbms ();
            } else {
                c = Containers.forUpdateNbms ();
                additionallyContainer = Containers.forAvailableNbms ();
            }
            additionallySupport = additionallyContainer.getSupport ();
            support = c.getSupport ();
            break;
        }
        return c;
    }
    
    public OperationContainer<OperationSupport> getCustomHandledContainer () {
        return Containers.forCustomInstall ();
    }
    
    public boolean allLicensesApproved () {
        for (UpdateElement el : getVisibleUpdateElements (getAllUpdateElements (), false)) {
            if (el.getLicence () != null && ! approvedLicences.contains (el.getLicence ())) {
                return false;
            }
        }
        return true;
    }
    
    public void addApprovedLicenses (Collection<String> licences) {
        approvedLicences.addAll (licences);
    }
    
    public InstallSupport getInstallSupport () {
        return support;
    }
    
    public InstallSupport getAdditionallyInstallSupport () {
        return additionallySupport;
    }
    
    public void setInstaller (Installer i) {
        installer = i;
    }
    
    public Installer getInstaller () {
        return installer;
    }
    
    @Override
    public void doCleanup () throws OperationException {
        try {
            if (getBaseContainer ().getSupport () instanceof InstallSupport) {
                if (OperationType.LOCAL_DOWNLOAD == getOperation ()) {
                    InstallSupport asupp = Containers.forAvailableNbms ().getSupport ();
                    if (asupp != null) {
                        asupp.doCancel ();
                    }
                    InstallSupport usupp = Containers.forUpdateNbms ().getSupport ();
                    if (usupp != null) {
                        usupp.doCancel ();
                    }
                    Containers.forAvailableNbms ().removeAll ();
                    Containers.forUpdateNbms ().removeAll ();
                } else {
                    InstallSupport isupp = (InstallSupport) getBaseContainer ().getSupport ();
                    if (isupp != null) {
                        isupp.doCancel ();
                    }
                }
            } else {
                OperationSupport osupp = (OperationSupport) getBaseContainer ().getSupport ();
                if (osupp != null) {
                    osupp.doCancel ();
                }
            }
            OperationSupport osupp = getCustomHandledContainer ().getSupport ();
            if (osupp != null) {
                osupp.doCancel ();
            }
        } catch (Exception x) {
            Exceptions.printStackTrace (x);
        } finally {
            super.doCleanup ();
        }
    }

}

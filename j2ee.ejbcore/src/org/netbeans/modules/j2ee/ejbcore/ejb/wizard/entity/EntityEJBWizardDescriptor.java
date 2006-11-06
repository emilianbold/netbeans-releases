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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.session.SessionEJBWizardPanel;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class EntityEJBWizardDescriptor implements WizardDescriptor.FinishablePanel, ChangeListener {

    private EntityEJBWizardPanel p;
    
    private List changeListeners = new ArrayList();
    
    private WizardDescriptor wizardDescriptor;
    
    private boolean isWaitingForScan = false;

    public void addChangeListener(javax.swing.event.ChangeListener l) {
        changeListeners.add(l);
    }
    
    public java.awt.Component getComponent() {
        if (p == null) {
            p = new EntityEJBWizardPanel(this);
            // add listener to events which could cause valid status to change
        }
        return p;
    }
    
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(EntityEJBWizardDescriptor.class);
    }
    
    public boolean isValid() {
        // XXX add the following checks
        // p.getName = valid NmToken
        // p.getName not already in module
        if (wizardDescriptor == null) {
            return true;
        }
        Project project = Templates.getProject(wizardDescriptor);
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup ().lookup (J2eeModuleProvider.class);
        String j2eeVersion = j2eeModuleProvider.getJ2eeModule().getModuleVersion();
        if (EjbJar.VERSION_3_0.equals(j2eeVersion)) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(EntityEJBWizardDescriptor.class,"MSG_DisabledForEJB3")); //NOI18N
            return false;
        }
        boolean isLocalOrRemote = (p.isLocal() || p.isRemote());
        if (!isLocalOrRemote) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(EntityEJBWizardDescriptor.class,"ERR_RemoteOrLocal_MustBeSelected")); //NOI18N
            return false;
        }
        if (p.getPrimaryKeyClassName().trim().equals("")) { //NOI18N
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(EntityEJBWizardDescriptor.class,"ERR_PrimaryKeyNotEmpty")); //NOI18N
            return false;
        }
        
        //TODO: RETOUCHE waitScanFinished
//        if (JavaMetamodel.getManager().isScanInProgress()) {
//            if (!isWaitingForScan) {
//                isWaitingForScan = true;
//                RequestProcessor.getDefault().post(new Runnable() {
//                    public void run() {
//                        JavaMetamodel.getManager().waitScanFinished();
//                        isWaitingForScan = false;
//                        fireChangeEvent();
//                    }
//                });
//            }
//            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(SessionEJBWizardPanel.class,"scanning-in-progress")); //NOI18N
//            return false;
//        }
        String errorMessage = (String) wizardDescriptor.getProperty("WizardPanel_errorMessage");
        if (errorMessage == null || errorMessage.trim().equals("")) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", " "); //NOI18N
        }
        return true;
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
    }
    
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        changeListeners.remove(l);
    }
    
    public void storeSettings(Object settings) {
        
    }
    
    public boolean isCMP() {
        return p.isCMP();
    }
    
    public boolean hasRemote() {
        return p.isRemote();
    }

    public boolean hasLocal() {
        return p.isLocal();
    }

    public String getPrimaryKeyClassName() {
        return p.getPrimaryKeyClassName();
    }
    
    public boolean isFinishPanel() {
        return isValid();
    }
    
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (changeListeners) {
            it = new HashSet(changeListeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }

}


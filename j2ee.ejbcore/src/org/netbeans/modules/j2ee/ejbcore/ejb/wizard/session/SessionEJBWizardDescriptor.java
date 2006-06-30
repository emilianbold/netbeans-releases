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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class SessionEJBWizardDescriptor implements WizardDescriptor.FinishablePanel, ChangeListener {
    
    private SessionEJBWizardPanel p;
    private boolean isWaitingForScan = false;
    
    private List changeListeners = new ArrayList();

    private WizardDescriptor wizardDescriptor;

    public void addChangeListener(javax.swing.event.ChangeListener l) {
        changeListeners.add(l);
    }
    
    public java.awt.Component getComponent() {
        if (p == null) {
            p = new SessionEJBWizardPanel(this);
            // add listener to events which could cause valid status to change
        }
        return p;
    }
    
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(SessionEJBWizardDescriptor.class);
    }
    
    public boolean isValid() {
        // XXX add the following checks
        // p.getName = valid NmToken
        // p.getName not already in module
        if (wizardDescriptor == null) {
            return true;
        }
        boolean isLocalOrRemote = (p.isLocal() || p.isRemote());
        if (!isLocalOrRemote) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(SessionEJBWizardPanel.class,"ERR_RemoteOrLocal_MustBeSelected")); //NOI18N
            return false;
        }
        if (JavaMetamodel.getManager().isScanInProgress()) {
            if (!isWaitingForScan) {
                isWaitingForScan = true;
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        JavaMetamodel.getManager().waitScanFinished();
                        fireChangeEvent();
                    }
                });
            }
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(SessionEJBWizardPanel.class,"scanning-in-progress")); //NOI18N
            return false;
        }
        wizardDescriptor.putProperty("WizardPanel_errorMessage", " "); //NOI18N
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
    
    public boolean hasRemote() {
        return p.isRemote();
    }
    
    public boolean hasLocal() {
        return p.isLocal();
    }
    
    public boolean isStateful() {
        return !p.isStateless();
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


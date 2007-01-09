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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class MessageEJBWizardPanel implements WizardDescriptor.FinishablePanel {
    
    private MessageEJBVisualPanel wizardPanel;
    private final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
    private final WizardDescriptor wizardDescriptor;
    //TODO: RETOUCHE
//    private boolean isWaitingForScan = false;

    public MessageEJBWizardPanel(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }
    
    public boolean isValid() {
        Project project = Templates.getProject(wizardDescriptor);
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup ().lookup (J2eeModuleProvider.class);
        String j2eeVersion = j2eeModuleProvider.getJ2eeModule().getModuleVersion();
        if (!EjbJar.VERSION_3_0.equals(j2eeVersion) && !EjbJar.VERSION_2_1.equals(j2eeVersion)) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(MessageEJBWizardPanel.class,"MSG_WrongJ2EESpecVersion")); //NOI18N
            return false;
        }

        //TODO: RETOUCHE waitScanFinished
//        if (JavaMetamodel.getManager().isScanInProgress()) {
//            if (!isWaitingForScan) {
//                isWaitingForScan = true;
//                RequestProcessor.getDefault().post(new Runnable() {
//                    public void run() {
//                        JavaMetamodel.getManager().waitScanFinished();
//                        fireChangeEvent();
//                    }
//                });
//            }
//            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(MessageEJBWizardPanel.class,"scanning-in-progress")); //NOI18N
//            return false;
//        }

        // XXX add the following checks
        // p.getName = valid NmToken
        // p.getName not already in module
        // remote and or local is selected
        return true;
    }
    
    public void readSettings(Object settings) {
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
        changeListeners.remove(changeListener);
    }
    
    public void storeSettings(Object settings) {
        
    }
    
    public boolean isFinishPanel() {
        return isValid();
    }
    
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> iterator;
        synchronized (changeListeners) {
            iterator = new HashSet<ChangeListener>(changeListeners).iterator();
        }
        ChangeEvent changeEvent = new ChangeEvent(this);
        while (iterator.hasNext()) {
            iterator.next().stateChanged(changeEvent);
        }
    }

    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(this.getClass());
    }

    public java.awt.Component getComponent() {
        if (wizardPanel == null) {
            wizardPanel = new MessageEJBVisualPanel();
            // add listener to events which could cause valid status to change
        }
        return wizardPanel;
    }

    public boolean isQueue() {
        return wizardPanel.isQueue();
    }

}


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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.MultiTargetChooserPanel;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class MessageEJBWizardPanel implements WizardDescriptor.FinishablePanel {
    
    private MessageEJBWizardVisualPanel wizardPanel;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final WizardDescriptor wizardDescriptor;
    private final EJBNameOptions ejbNames;
    //TODO: RETOUCHE
//    private boolean isWaitingForScan = false;

    public MessageEJBWizardPanel(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
        this.ejbNames = new EJBNameOptions();
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }
    
    public boolean isValid() {
        Project project = Templates.getProject(wizardDescriptor);
        J2eeModuleProvider j2eeModuleProvider = project.getLookup ().lookup (J2eeModuleProvider.class);
        String j2eeVersion = j2eeModuleProvider.getJ2eeModule().getModuleVersion();
        if (!EjbJar.VERSION_3_0.equals(j2eeVersion) && !EjbJar.VERSION_2_1.equals(j2eeVersion)) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(MessageEJBWizardPanel.class,"MSG_WrongJ2EESpecVersion")); //NOI18N
            return false;
        }

        FileObject targetFolder = (FileObject) wizardDescriptor.getProperty(MultiTargetChooserPanel.TARGET_FOLDER);
        if (targetFolder != null) {
            String targetName = (String) wizardDescriptor.getProperty(MultiTargetChooserPanel.TARGET_NAME);
            String name = ejbNames.getMessageDrivenEjbClassPrefix() + targetName + ejbNames.getMessageDrivenEjbClassSuffix();
            if (targetFolder.getFileObject(name + ".java") != null) { // NOI18N
                wizardDescriptor.putProperty("WizardPanel_errorMessage", // NOI18N
                        NbBundle.getMessage(MessageEJBWizardPanel.class,"ERR_FileAlreadyExists", name + ".java")); //NOI18N
                return false;
            }
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
        
        // component/panel validation
        getComponent();
        if (wizardPanel.getDestination() == null) {
            wizardDescriptor.putProperty(
                    "WizardPanel_errorMessage", //NOI18N
                    NbBundle.getMessage(MessageEJBWizardPanel.class,"ERR_NoDestinationSelected"));
            return false;
        }
        // XXX warn about missing server (or error? or not needed?)
        if (!wizardPanel.isDestinationCreationSupportedByServerPlugin()) {
            wizardDescriptor.putProperty(
                    "WizardPanel_errorMessage", //NOI18N
                    NbBundle.getMessage(MessageEJBWizardPanel.class,"ERR_MissingServer"));
            //return false;
        }
        return true;
    }
    
    public void readSettings(Object settings) {
    }
    
    public void storeSettings(Object settings) {
        
    }
    
    public boolean isFinishPanel() {
        return isValid();
    }
    
    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(this.getClass());
    }

    public java.awt.Component getComponent() {
        if (wizardPanel == null) {
            Project project = Templates.getProject(wizardDescriptor);
            J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            MessageDestinationUiSupport.DestinationsHolder holder = MessageDestinationUiSupport.getDestinations(j2eeModuleProvider);
            wizardPanel = MessageEJBWizardVisualPanel.newInstance(
                    j2eeModuleProvider,
                    holder.getModuleDestinations(),
                    holder.getServerDestinations());
            wizardPanel.addPropertyChangeListener(MessageEJBWizardVisualPanel.CHANGED,
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent event) {
                            fireChangeEvent();
                        }
                    });
        }
        return wizardPanel;
    }

    /**
     * @see MessageDestinationPanel#getDestination()
     */
    public MessageDestination getDestination() {
        return wizardPanel.getDestination();
    }
}

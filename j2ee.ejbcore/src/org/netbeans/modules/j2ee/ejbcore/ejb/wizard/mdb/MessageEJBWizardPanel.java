/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
        if (!EjbJar.VERSION_3_1.equals(j2eeVersion) && !EjbJar.VERSION_3_0.equals(j2eeVersion) && !EjbJar.VERSION_2_1.equals(j2eeVersion)) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(MessageEJBWizardPanel.class,"MSG_WrongJ2EESpecVersion")); //NOI18N
            return false;
        }

        FileObject targetFolder = (FileObject) wizardDescriptor.getProperty(MultiTargetChooserPanel.TARGET_FOLDER);
        if (targetFolder != null) {
            String targetName = (String) wizardDescriptor.getProperty(MultiTargetChooserPanel.TARGET_NAME);
            String name = ejbNames.getMessageDrivenEjbClassPrefix() + targetName + ejbNames.getMessageDrivenEjbClassSuffix();
            if (targetFolder.getFileObject(name + ".java") != null) { // NOI18N
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
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
//            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(MessageEJBWizardPanel.class,"scanning-in-progress")); //NOI18N
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
                    WizardDescriptor.PROP_ERROR_MESSAGE, //NOI18N
                    NbBundle.getMessage(MessageEJBWizardPanel.class,"ERR_NoDestinationSelected"));
            return false;
        }
        // XXX warn about missing server (or error? or not needed?)
        if (!wizardPanel.isDestinationCreationSupportedByServerPlugin()) {
            wizardDescriptor.putProperty(
                    WizardDescriptor.PROP_ERROR_MESSAGE, //NOI18N
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

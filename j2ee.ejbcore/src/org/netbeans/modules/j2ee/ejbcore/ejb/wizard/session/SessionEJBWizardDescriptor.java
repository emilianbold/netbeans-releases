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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.MultiTargetChooserPanel;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class SessionEJBWizardDescriptor implements WizardDescriptor.FinishablePanel, ChangeListener {
    
    private SessionEJBWizardPanel wizardPanel;
    private final EJBNameOptions ejbNames;
    private final Project project;
    //TODO: RETOUCHE
//    private boolean isWaitingForScan = false;
    
    private final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

    private WizardDescriptor wizardDescriptor;

    public SessionEJBWizardDescriptor(Project project) {
        this.ejbNames = new EJBNameOptions();
        this.project = project;
    }
    
    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }
    
    public java.awt.Component getComponent() {
        if (wizardPanel == null) {
            wizardPanel = new SessionEJBWizardPanel(project, this);
            // add listener to events which could cause valid status to change
        }
        return wizardPanel;
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
        boolean isLocal = wizardPanel.isLocal();
        boolean isRemote = wizardPanel.isRemote();
        if (!isLocal && !isRemote && !J2eeProjectCapabilities.forProject(project).isEjb31LiteSupported()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(SessionEJBWizardDescriptor.class,"ERR_RemoteOrLocal_MustBeSelected")); //NOI18N
            return false;
        }
        
        FileObject targetFolder = (FileObject) wizardDescriptor.getProperty(MultiTargetChooserPanel.TARGET_FOLDER);
        if (targetFolder != null) {
            String targetName = (String) wizardDescriptor.getProperty(MultiTargetChooserPanel.TARGET_NAME);
            List<String> proposedNames = new ArrayList<String>();
            proposedNames.add(ejbNames.getSessionEjbClassPrefix() + targetName + ejbNames.getSessionEjbClassSuffix());
            if (isLocal) {
                proposedNames.add(ejbNames.getSessionLocalPrefix() + targetName + ejbNames.getSessionLocalSuffix());
                proposedNames.add(ejbNames.getSessionLocalHomePrefix() + targetName + ejbNames.getSessionLocalHomeSuffix());
            } 
            if (isRemote) {
                proposedNames.add(ejbNames.getSessionRemotePrefix() + targetName + ejbNames.getSessionRemoteSuffix());
                proposedNames.add(ejbNames.getSessionRemoteHomePrefix() + targetName + ejbNames.getSessionRemoteHomeSuffix());
            }
            for (String name : proposedNames) {
                if (targetFolder.getFileObject(name + ".java") != null) { // NOI18N
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                            NbBundle.getMessage(SessionEJBWizardDescriptor.class,"ERR_FileAlreadyExists", name + ".java")); //NOI18N
                    return false;
                }
            }

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
//            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(SessionEJBWizardPanel.class,"scanning-in-progress")); //NOI18N
//            return false;
//        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); //NOI18N
        return true;
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
        changeListeners.remove(changeListener);
    }
    
    public void storeSettings(Object settings) {
        
    }
    
    public boolean hasRemote() {
        return wizardPanel.isRemote();
    }
    
    public boolean hasLocal() {
        return wizardPanel.isLocal();
    }

    public String getSessionType() {
        return wizardPanel.getSessionType();
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

    public void stateChanged(ChangeEvent changeEvent) {
        fireChangeEvent();
    }

}


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

package org.netbeans.modules.projectimport.jbuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.projectimport.j2seimport.ui.BasicPanel;
import org.netbeans.modules.projectimport.j2seimport.ui.ProgressPanel;
import org.netbeans.modules.projectimport.j2seimport.ui.WarningMessage;
import org.netbeans.modules.projectimport.jbuilder.ui.JBWizardData;
import org.netbeans.modules.projectimport.jbuilder.ui.JBWizardPanel1;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.netbeans.modules.projectimport.j2seimport.AbstractProject;
import org.netbeans.modules.projectimport.j2seimport.ImportProcess;
import org.netbeans.modules.projectimport.j2seimport.ImportUtils;
import org.netbeans.modules.projectimport.j2seimport.ui.WizardSupport;

/**
 * Runs JBuilder Importer.
 *
 * @author Radek Matous
 */
public class ImportAction extends CallableSystemAction {
    
    public ImportAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }
    
    public void performAction() {
        final ImportProcess iProcess;
        try {
            BasicPanel[] panels = new BasicPanel[]{JBWizardPanel1.getInstance()};
            String title = NbBundle.getMessage(ImportAction.class, "CTL_WizardTitle"); // NOI18N
            JBWizardData wizardData = (JBWizardData)WizardSupport.show(title, panels, new JBWizardData());

            if (wizardData != null) {

                FileObject prjDir = FileUtil.toFileObject(wizardData.getDestinationDir());
                assert prjDir != null;

                iProcess = ImportUtils.createImportProcess(prjDir,wizardData.getProjectDefinition(),
                        wizardData.isIncludeDependencies());


                ProgressPanel.showProgress(iProcess);
                WarningMessage.showMessages(iProcess);

                // open created projects when importing finished
                OpenProjects.getDefault().open(iProcess.getProjectsToOpen(), true);
            }
        } catch (Throwable thr) {
            ErrorManager.getDefault().notify(thr);
        }
    }
    
    
    public String getName() {
        return NbBundle.getMessage(ImportAction.class, "CTL_MenuItem"); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
}

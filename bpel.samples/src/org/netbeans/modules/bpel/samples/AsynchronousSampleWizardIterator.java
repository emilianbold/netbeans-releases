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
package org.netbeans.modules.bpel.samples;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;

public class AsynchronousSampleWizardIterator extends SampleWizardIterator {
    private static final long serialVersionUID = 1L;
    
    public AsynchronousSampleWizardIterator() {}
    
    public static AsynchronousSampleWizardIterator createIterator() {
        return new AsynchronousSampleWizardIterator();
    }
    
    protected String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(AsynchronousSampleWizardIterator.class, "MSG_CreateAsynchronousSampleProject"),
        };
    }
    
    protected WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new AsynchronousSampleWizardPanel(),
        };
    }
    
    public Set<FileObject> instantiate() throws IOException {
        Set<FileObject> resultSet = super.instantiate();
        
        FileObject dirParent = getProject().getProjectDirectory().getParent();
        try {
            String name = (String) wiz.getProperty(NAME) + "Application";
            
            Set<FileObject> set = createAsynchronousCompositeApplicationProject(dirParent, name);
            Iterator<FileObject> iterator = set.iterator();
            while(iterator.hasNext())
                resultSet.add(iterator.next());
        } catch(FileNotFoundException fnfe) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,fnfe);
        } catch(IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,ioe);
        }
        ProjectChooser.setProjectsFolder(FileUtil.toFile(dirParent.getParent()));
        return resultSet;
    }
    
    private Set<FileObject> createAsynchronousCompositeApplicationProject(FileObject targetProjectDir, String name)
    throws FileNotFoundException, IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        
        FileObject compApptargetProjectDir = targetProjectDir.createFolder(name);
        assert compApptargetProjectDir != null : "targetProjectDir for AsynchronousCompositeApplicationProject project is null";
        
        FileObject trsCompositeApp = Repository.getDefault().
                getDefaultFileSystem().findResource("org-netbeans-modules-bpel-samples-resources/AsynchronousSampleApplication.zip");// NOI18N
        SoaSampleUtils.unZipFile(trsCompositeApp.getInputStream(),compApptargetProjectDir);
        
        SoaSampleUtils.setProjectName(compApptargetProjectDir,
                SoaSampleProjectProperties.COMPAPP_PROJECT_CONFIGURATION_NAMESPACE,
                name, "AsynchronousSampleApplication");
        // add JbiModule
        Project compAppProject = ProjectManager.getDefault().findProject(compApptargetProjectDir);
        SoaSampleUtils.addJbiModule(compAppProject, getProject());
        
        resultSet.add(compApptargetProjectDir);
        
        return resultSet;
    }
}

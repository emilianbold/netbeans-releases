/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.websvc.rest.samples.ui;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Liu
 * 
 */
public class HelloWorldSampleWizardIterator extends SampleWizardIterator {
    private static final long serialVersionUID = 1L;
    
    public HelloWorldSampleWizardIterator() {}
    
    public static HelloWorldSampleWizardIterator createIterator() {
        return new HelloWorldSampleWizardIterator();
    }
    
    protected String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(HelloWorldSampleWizardIterator.class, "MSG_CreateHelloWorldProject"),
        };
    }
    
    protected WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new HelloWorldSampleWizardPanel(),
        };
    }
    
    public Set instantiate() throws IOException {
        setProjectConfigNamespace(null);
        Set resultSet = super.instantiate();
        
        //replace tokens
        String[][] tokens = { {"HelloWorld", (String) wiz.getProperty(NAME)} };
        String[] files = 
            {   "web/WEB-INF/sun-web.xml", 
                "nbproject/project.properties", "nbproject/project.xml", 
                "build.xml"
            };        
        replaceTokens(getProject().getProjectDirectory(), files, tokens);
        
        FileObject dirParent = null;
        
        if( getProject()!= null && getProject().getProjectDirectory()!=null)
            dirParent = getProject().getProjectDirectory().getParent();
                
        // See issue 80520.
        // On some machines the project just created is not immediately detected.
        // For those cases use determine the directory with lines below.
        if(dirParent == null) {            
            dirParent = FileUtil.toFileObject(FileUtil.normalizeFile((File) wiz.getProperty(PROJDIR)));
        } 
        
        ProjectChooser.setProjectsFolder(FileUtil.toFile(dirParent.getParent()));
        return resultSet;
    }

    
}

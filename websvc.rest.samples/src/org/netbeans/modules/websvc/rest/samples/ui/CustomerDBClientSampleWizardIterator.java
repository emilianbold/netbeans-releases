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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Liu
 * 
 */
public class CustomerDBClientSampleWizardIterator extends SampleWizardIterator {
    private static final long serialVersionUID = 1L;
    
    public CustomerDBClientSampleWizardIterator() {}
    
    public static CustomerDBClientSampleWizardIterator createIterator() {
        return new CustomerDBClientSampleWizardIterator();
    }
    
    protected String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(CustomerDBClientSampleWizardIterator.class, "MSG_CreateCustomerDBClientProject"),
        };
    }
    
    protected WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new CustomerDBClientSampleWizardPanel()
        };
    }
    
    public Set instantiate() throws IOException {
        setProjectConfigNamespace(null);
        setAddJerseyLibrary(false);
        Set resultSet = super.instantiate();
        
        FileObject projDir = getProject().getProjectDirectory();
        String name = (String) wiz.getProperty(NAME);
        String[][] tokens = { {"__APP_NAME__", name} };
        replaceToken(projDir.getFileObject("web/index.jsp"), tokens); //NoI18n
        replaceToken(projDir.getFileObject("web/WEB-INF/sun-web.xml"), tokens); //NoI18n
        
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

    private void replaceToken(FileObject fo, String[][] tokens) throws IOException {
        if(fo == null)
            return;
        FileLock lock = fo.lock();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(fo)));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                for(int i=0;i<tokens.length;i++)
                    line = line.replaceAll(tokens[i][0], tokens[i][1]);
                sb.append(line);
                sb.append("\n");
            }
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8");
            try {
                writer.write(sb.toString());
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }        
    }
    
}

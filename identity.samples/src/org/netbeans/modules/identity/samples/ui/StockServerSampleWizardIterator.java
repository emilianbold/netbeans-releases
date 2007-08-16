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

package org.netbeans.modules.identity.samples.ui;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.identity.samples.util.SoaSampleProjectProperties;
import org.netbeans.modules.identity.samples.util.SoaSampleUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @author Srividhya Narayanan
 * @version 22 January 2006
 */
public class StockServerSampleWizardIterator extends SampleWizardIterator {
    private static final long serialVersionUID = 1L;
    
    public StockServerSampleWizardIterator() {}
    
    public static StockServerSampleWizardIterator createIterator() {
        return new StockServerSampleWizardIterator();
    }
    
    protected String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(StockServerSampleWizardIterator.class, "MSG_CreateStockServerProject"),
        };
    }
    
    protected WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new StockServerSampleWizardPanel(),
        };
    }
    
    public Set instantiate() throws IOException {
        setProjectConfigNamespace(SoaSampleProjectProperties.EAR_PROJECT_CONFIGURATION_NAMESPACE);
        Set resultSet = super.instantiate();
        
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
        
        Iterator iter = resultSet.iterator();
        while (iter.hasNext()) {
            FileObject dir = (FileObject) iter.next();
            if (dir.getName().contains("war")) {
                SoaSampleUtils.setPrivateProperty(dir, "project.StockQuoteService-ejb", 
                    getProject().getProjectDirectory().getPath() + "/StockQuoteService-ejb");
                
                break;
            }
        }
        
        return resultSet;
    }

    
}

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
package org.netbeans.modules.xslt.project.wizard;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.compapp.projects.base.ui.wizards.NewIcanproProjectWizardIterator;
import org.netbeans.modules.xslt.project.XsltproProjectGenerator;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import static org.netbeans.modules.xslt.project.XsltproConstants.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.openide.loaders.DataObject;

/**
 * Iterator for a wizard that needs to instantiate new xslt object.
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class NewXsltproProjectWizardIterator extends NewIcanproProjectWizardIterator {

    private static final long serialVersionUID = 1L;

    public NewXsltproProjectWizardIterator() {
        super();
    }

    @Override
    public Set instantiate() throws IOException {
        Set set = super.instantiate();
        createTMapFile(set);
        return set;
    }
    
    @Override
    protected void createProject(File dirF, String name, String j2eeLevel) throws IOException {
        XsltproProjectGenerator.createProject(dirF, name);
    }

    @Override
    protected String getDefaultTitle() {
        return NbBundle.getMessage(NewXsltproProjectWizardIterator.class, "LBL_XSLT_Wizard_Title"); //NOI18N   
    }

    @Override
    protected String getDefaultName() {
        return NbBundle.getMessage(NewXsltproProjectWizardIterator.class, "LBL_NPW1_DefaultProjectName"); //NOI18N
    }
    
    private void createTMapFile(Set resultSet) throws IOException {
        
        if (resultSet == null || resultSet.isEmpty()) {
            return;
        }
        
        FileObject fo = null;
        Iterator setIterator = resultSet.iterator();
        while (setIterator.hasNext()) {
            Object obj = setIterator.next();
            if (obj instanceof FileObject) {
                fo = (FileObject)obj;
                break;
            }
        }
        Project p = ProjectManager.getDefault().findProject(fo);
        if (p != null) {
            FileObject srcFo = Util.getProjectSource(p);
            FileObject tMapFo = FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem()
                    .findResource("org-netbeans-xsltpro/transformmap.xml"), //NOI18N
                    srcFo, "transformmap"); //NOI18N

            SoaUiUtil.fixEncoding(DataObject.find(tMapFo), srcFo);
        }
    }
}

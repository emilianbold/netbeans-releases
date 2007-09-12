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

/*
 * BusinessProcessHelperImpl.java Created on October 12, 2005, 2:22 PM To change
 * this template, choose Tools | Template Manager and open the template in the
 * editor.
 */

package org.netbeans.modules.bpel.core.helper.impl;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import org.netbeans.modules.bpel.core.BPELDataObject;
import org.netbeans.modules.bpel.core.helper.api.BusinessProcessHelper;

import org.netbeans.api.project.FileOwnerQuery;
import java.util.Collection;
import java.util.Enumeration;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.bpel.project.ProjectConstants;
import org.netbeans.modules.xml.retriever.catalog.Utilities;

/**
 * @author Praveen
 * @author ads
 */
public class BusinessProcessHelperImpl extends Object implements
        BusinessProcessHelper
{
    
    public BusinessProcessHelperImpl(
            BPELDataObject businessProcessDataObject )
    {
        super();
        myDataObject = businessProcessDataObject;
    }

    /** {@inheritDoc} */
    public Collection<FileObject> getWSDLFilesInProject() {
        Collection<FileObject> wsdlFiles = new ArrayList<FileObject>();

        FileObject projectDir = FileOwnerQuery.getOwner(
                getDataObject().getPrimaryFile())
                .getProjectDirectory();

        Project project = FileOwnerQuery
                .getOwner(getDataObject().getPrimaryFile());
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroup = sources
                .getSourceGroups(ProjectConstants.SOURCES_TYPE_BPELPRO);

        for (int i = 0; i < sourceGroup.length; i++) {
            Enumeration<? extends FileObject> filesInProject = sourceGroup[i]
                    .getRootFolder().getChildren(true);

            while (filesInProject.hasMoreElements()) {
                FileObject fo = filesInProject.nextElement();
                if (fo.getExt().toUpperCase().equals("WSDL")) { // NOI18N
                    wsdlFiles.add(fo);
                }
            }
        }
        return wsdlFiles;
    }

    /** {@inheritDoc} */
    public Collection<FileObject> getSchemaFilesInProject() {
        Collection<FileObject> schemaFiles = new ArrayList<FileObject>();

        FileObject projectDir = FileOwnerQuery.getOwner(
                getDataObject().getPrimaryFile())
                .getProjectDirectory();

        Project project = FileOwnerQuery
                .getOwner(getDataObject().getPrimaryFile());
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroup = sources
                .getSourceGroups(ProjectConstants.SOURCES_TYPE_BPELPRO);

        // Enumeration<FileObject> filesInProject =
        // projectDir.getChildren(true);
        for (int i = 0; i < sourceGroup.length; i++) {
            Enumeration<? extends FileObject> filesInProject = sourceGroup[i]
                    .getRootFolder().getChildren(true);

            while (filesInProject.hasMoreElements()) {
                FileObject fo = filesInProject.nextElement();
                if (fo.getExt().toUpperCase().equals("XSD")) { // NOI18N
                    schemaFiles.add(fo);
                }
            }
        }

        return schemaFiles;
    }

    /** {@inheritDoc} */
    public WSDLModel getWSDLModelFromUri( URI uri ) {
        WSDLModel wsdlModel = null;

        try {
            File file = new File(uri);
            ModelSource modelSource = Utilities.getModelSource(FileUtil
                    .toFileObject(file), true);
            wsdlModel = org.netbeans.modules.xml.wsdl.model.WSDLModelFactory
                    .getDefault().getModel(modelSource);
        }
        catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return wsdlModel;
    }

    /** {@inheritDoc} */
    public WSDLModel getWSDLModel() {
        // Implement post TPR3, since in TPR3 we do not know which WSDL is
        // associated with the BPEL file.
        return null;
    }

    /** {@inheritDoc} */
    public String getWSDLFile() {
        // Implement post TPR3, since in TPR3 we do not know which WSDL is
        // associated with the BPEL file.
        return null;
    }

    /** {@inheritDoc} */
    public URI getWSDLFileUri() {
        // Implement post TPR3, since in TPR3 we do not know which WSDL is
        // associated with the BPEL file.
        return null;
    }

    private BPELDataObject getDataObject() {
        return myDataObject;
    }

    private BPELDataObject myDataObject;
}

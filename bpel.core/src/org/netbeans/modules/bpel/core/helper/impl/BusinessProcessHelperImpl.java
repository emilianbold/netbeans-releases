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
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.xml.retriever.catalog.Utilities;

/**
 * @author Praveen
 * @author ads
 */
public class BusinessProcessHelperImpl extends Object implements BusinessProcessHelper {
    
    public BusinessProcessHelperImpl(BPELDataObject businessProcessDataObject) {
        super();
        myDataObject = businessProcessDataObject;
    }

    /** {@inheritDoc} */
    public Collection<FileObject> getWSDLFilesInProject() {
        Collection<FileObject> wsdlFiles = new ArrayList<FileObject>();
        FileObject projectDir = FileOwnerQuery.getOwner(getDataObject().getPrimaryFile()).getProjectDirectory();
        Project project = FileOwnerQuery.getOwner(getDataObject().getPrimaryFile());
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroup = sources.getSourceGroups(Utils.SOURCES_TYPE_BPELPRO);

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
        FileObject projectDir = FileOwnerQuery.getOwner(getDataObject().getPrimaryFile()).getProjectDirectory();
        Project project = FileOwnerQuery.getOwner(getDataObject().getPrimaryFile());
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroup = sources.getSourceGroups(Utils.SOURCES_TYPE_BPELPRO);

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
    @Deprecated
    public WSDLModel getWSDLModelFromUri( URI uri ) {
        return null;
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

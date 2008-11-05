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
package org.netbeans.modules.xslt.tmap.model.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.catalogsupport.ProjectConstants;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.retriever.catalog.Utilities.DocumentTypesEnum;
import org.netbeans.modules.xslt.tmap.model.spi.ExternalModelRetriever;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xslt.tmap.model.spi.ExternalModelRetriever.class)
public class ExternalModelRetrieverImpl implements ExternalModelRetriever {

    public ExternalModelRetrieverImpl() {
    }
    
    public Collection<WSDLModel> getWSDLModels( TMapModel model,
            String namespace ) 
    {
        if ( namespace == null ) {
            return Collections.emptyList();
        }
        List<WSDLModel> list = new LinkedList<WSDLModel>();
        
        collectWsdlModelsViaFS(model, namespace, list);
        
        return list;
    }
    
    private static void collectWsdlModelsViaFS( TMapModel model, 
            String namespace, List<WSDLModel> list ) 
    {
        FileObject[] files = getFilesByNamespace( model.getModelSource() ,
                namespace , DocumentTypesEnum.wsdl );
        for (FileObject file : files) {
            WSDLModel wsdlModel;
            ModelSource modelSource = Utilities.getModelSource( file , true );
            wsdlModel = WSDLModelFactory.getDefault().
                    getModel( modelSource );
            
            if ( wsdlModel != null ){
                list.add( wsdlModel );
            }
        }
    }
    
    
    private static FileObject[] getFilesByNamespace( ModelSource source ,
            String namespace , DocumentTypesEnum type ) 
    {
        assert namespace!= null;
        List<FileObject> list = new LinkedList<FileObject>();
        FileObject modelFo = source.getLookup().lookup(FileObject.class);
        // e.g. in case when this retriever is invoked from command line
        if (modelFo == null) {
            return new FileObject[0];
        }
        
        Project project =  FileOwnerQuery.getOwner(modelFo);
        List<SourceGroup> sourceGroups = new ArrayList<SourceGroup>();
        sourceGroups.addAll(Arrays.asList(
                ProjectUtils.getSources(project).getSourceGroups(
                ProjectConstants.SOURCES_TYPE_XML)));
        
        //        List<String> sourceGroupTypeList = new ArrayList<String>();
        //        sourceGroupTypeList.add(ProjectConstants.SOURCES_TYPE_XML);
        
        DefaultProjectCatalogSupport support = project.getLookup().
                lookup(DefaultProjectCatalogSupport.class);
        if (support != null) {
            Set<Project> refProjects = support.getProjectReferences();
            if (refProjects != null && refProjects.size() > 0) {
                for (Project refProject : refProjects) {
                    sourceGroups.addAll(Arrays.asList(
                            ProjectUtils.getSources(refProject).getSourceGroups(
                            ProjectConstants.SOURCES_TYPE_XML)));
                }
            }
        }
        
        /*
         * We perform search only in sources folder of project for avoiding
         * search in "build" folder.
         * Otherwise we will have duplicate models for files wuth same contents.
         * See bug description in #6423749 ( bugtruk ).
         */
        for (SourceGroup group : sourceGroups) {
            File file = FileUtil.toFile(group.getRootFolder());
            if (file == null) {
                return null;
            }
            Map<FileObject, String> map = Utilities.getFiles2NSMappingInProj(
                    file, type);
            for (Entry<FileObject, String> entry : map.entrySet()) {
                String ns = entry.getValue();
                if (namespace.equals(ns)) {
                    list.add(entry.getKey());
                }
            }
        }
        return list.toArray( new FileObject[ list.size()] );
    }
}

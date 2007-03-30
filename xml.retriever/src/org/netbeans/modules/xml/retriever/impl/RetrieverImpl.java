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

/*
 * RetrieverImpl.java
 *
 * Created on February 21, 2006, 9:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.retriever.*;
import org.netbeans.modules.xml.retriever.RetrieveEntry;
import org.netbeans.modules.xml.retriever.XMLCatalogProvider;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.Utilities.DocumentTypesEnum;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author girix
 */
public class RetrieverImpl extends Retriever {
    
    /** Creates a new instance of RetrieverImpl */
    public RetrieverImpl() {
    }
    
    File seedFile = null;
    RetrieverEngineImpl instance;
    public boolean startNewThread = false;
    
    public FileObject retrieveResource(FileObject destinationDir, URI relativePathToCatalogFile, URI resourceToRetrieve) throws UnknownHostException, URISyntaxException, IOException {
        return retrieveResource(destinationDir, relativePathToCatalogFile, resourceToRetrieve, false);
    }
    
    public FileObject retrieveResource(FileObject destinationDir,URI resourceToRetrieve) throws UnknownHostException, URISyntaxException, IOException {
        return retrieveResource(destinationDir, null, resourceToRetrieve);
    }
    
    
    public FileObject retrieveResourceClosureIntoSingleDirectory(FileObject destinationDir, URI resourceToRetrieve) throws UnknownHostException, URISyntaxException, IOException {
        return retrieveResource(destinationDir, null, resourceToRetrieve, true);
    }
    
    public FileObject retrieveResource(FileObject destinationDir, 
            URI relativePathToCatalogFile, URI resourceToRetrieve, 
            boolean save2singleFolder) 
            throws UnknownHostException, URISyntaxException, IOException {
        Project prj = FileOwnerQuery.getOwner(destinationDir);
        if(relativePathToCatalogFile == null){
            assert(prj != null);
            //check if this project has XMLCatalogProvider in its lookup
            XMLCatalogProvider catProvider = (XMLCatalogProvider) prj.getLookup().
                    lookup(XMLCatalogProvider.class);
            if(catProvider == null){
                //there is no catalog provider so just use the legacy projectwide catalog approach
                return retrieveResourceImpl(destinationDir, resourceToRetrieve, null, save2singleFolder);
            }
            relativePathToCatalogFile = catProvider.getProjectWideCatalog();
            if(relativePathToCatalogFile == null){
                //somehow this provider does not give me this info. So follow legacy.
                return retrieveResourceImpl(destinationDir, resourceToRetrieve, null, save2singleFolder);
            }
            //use this relativePathToCatalogFile for the new catalog file.
        }
        URI cfuri = null;
        if(!relativePathToCatalogFile.isAbsolute()){
            FileObject prjRtFO = prj.getProjectDirectory();
            cfuri = FileUtil.toFile(prjRtFO).toURI().resolve(relativePathToCatalogFile);
        }else{
            cfuri = relativePathToCatalogFile;
        }
        File cffile = new File(cfuri);
        if(!cffile.isFile())
            cffile.createNewFile();
        FileObject catalogFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(cffile));
        return retrieveResourceImpl(destinationDir, resourceToRetrieve, catalogFileObject, save2singleFolder);
    }
    
    private FileObject retrieveResourceImpl(FileObject destinationDir, 
            URI resourceToRetrieve, FileObject catalogFileObject, boolean save2singleFolder) 
            throws UnknownHostException, URISyntaxException, IOException {
        instance = new RetrieverEngineImpl(FileUtil.toFile(destinationDir), startNewThread);
        instance.setFileOverwrite(overwriteFiles);
        instance.setSave2SingleFolder(save2singleFolder);
        if(catalogFileObject != null)
            instance.setCatalogFile(catalogFileObject);
        instance.setShowErrorPopup(false);
        RetrieveEntry rent = null;
        rent = new RetrieveEntry(null, resourceToRetrieve.toString(), null, 
                null, DocumentTypesEnum.schema, this.retrieveRecursively);
        instance.addResourceToRetrieve(rent);
        instance.start();
        File result = instance.getSeedFileLocation();
        seedFile = result;
        if(result == null)
            return null;
        //createCatalog(result);
        return FileUtil.toFileObject(FileUtil.normalizeFile(result));
    }
    
    
    private Project getProject(File result) {
        FileObject fob = FileUtil.toFileObject(result);
        return FileOwnerQuery.getOwner(fob);
    }
    
    public File getProjectCatalog() {
        if(seedFile == null)
            return null;
        FileObject prjRootFo = getProject(seedFile).getProjectDirectory();
        File prjRt = FileUtil.toFile(prjRootFo);
        File catalogFile = new File(prjRt, CatalogWriteModel.PUBLIC_CATALOG_FILE_NAME+CatalogWriteModel.CATALOG_FILE_EXTENSION);
        return catalogFile;
    }
    
    public Map<RetrieveEntry, Exception> getRetrievedResourceExceptionMap() {
        if(instance != null)
            return instance.getRetrievedResourceExceptionMap();
        else
            return null;
    }
    
    public File retrieveResource(File targetFolder, URI source) throws UnknownHostException, URISyntaxException, IOException {
        FileObject fobj = retrieveResource(FileUtil.toFileObject(FileUtil.normalizeFile(targetFolder)), source);
        if(fobj != null)
            return FileUtil.toFile(fobj);
        return null;
    }

    boolean retrieveRecursively = true;
    public void setRecursiveRetrieve(boolean retrieveRecursively) {
        this.retrieveRecursively = retrieveRecursively;
    }

    boolean overwriteFiles = true;
    public void setOverwriteFilesWithSameName(boolean overwriteFiles) {
        this.overwriteFiles = overwriteFiles;
    }
    
}

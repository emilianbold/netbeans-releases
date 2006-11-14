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

package org.netbeans.modules.xml.retriever.catalog.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogElement;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author girix
 */
public class CatalogWriteModelImpl extends CatalogModelImpl implements CatalogWriteModel{
    
    public static final String PROPERTY_CHANGE_PROPERTY_KEY= "CatalogModelImpl.PropertyChange.Property";
    
    PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private static final Logger logger = Utilities.getLogger();
    
    private DocumentModel.State currentStateOfCatalog;
    
    private CatalogFileWrapper catalogWrapper = null;
    
    /**
     * Constructor for public catalog files
     */
    protected CatalogWriteModelImpl(Project prj) throws IOException{
        super(prj);
    }
    
    /**
     * Constructor for creating a CatalogWriteModel for the given file object.
     * The catalogFileObject should not be null
     */
    protected CatalogWriteModelImpl(FileObject catalogFileObject) throws IOException{
        super(catalogFileObject);
    }
    
    
    //for unit tests
    boolean unitTestSaveStrategy = false;
    public  CatalogWriteModelImpl(File myProjectRootFile) throws IOException{
        super(myProjectRootFile);
        unitTestSaveStrategy = true;
    }
    //For unit tests
    public CatalogWriteModelImpl(){
        
    }
    
    public URI searchURI(URI locationURI){
        if(locationURI == null)
            return null;
        bootStrapCatalog();
        if(catalogFileObject != null){
            //look up in the global catalog
            File publicCatalogFile = FileUtil.toFile(catalogFileObject);
            if(publicCatalogFile.isFile()){
                try {
                    URI strRes = resolveUsingApacheCatalog(publicCatalogFile, locationURI.toString());
                } catch (IOException ex) {
                    return null;
                } catch (CatalogModelException ex) {
                    return null;
                }
            }
        }
        return null;
    }
    
    
    public DocumentModel.State getState(){
        return currentStateOfCatalog;
    }
    
    public synchronized void addURI(URI locationURI, FileObject fileObj) throws IOException {
        URI fileObjURI = FileUtil.toFile(fileObj).toURI();
        addURI(locationURI, fileObjURI);
    }
    
    public synchronized void addURI(URI locationURI, URI fileObjURI) throws IOException {
        if(this.catalogFileObject == null)
            return;
        //remove the old entry if exists
        removeURI(locationURI);
        
        bootStrapCatalog();
        
        URI master = FileUtil.toFile(this.catalogFileObject).toURI();
        
        String finalDestStr = Utilities.relativize(master, fileObjURI);
        CatalogEntry catEnt = new CatalogEntryImpl(CatalogElement.system, locationURI.toString(), finalDestStr);
        catalogWrapper.addSystem(catEnt);
    }
    
    public String toString(){
        return "This Public Catalog FO:"+this.catalogFileObject;
    }
    
    public synchronized void removeURI(URI locationURI) throws IOException {
        logger.finer("ENTRING:"+locationURI);
        if(this.catalogFileObject == null)
            return;
        bootStrapCatalog();
        List<CatalogEntry> catEntList = catalogWrapper.getSystems();
        if(catEntList == null)
            return;
        CatalogEntry remVal = null;
        for(CatalogEntry catEnt : catEntList){
            if(catEnt.getSource().equals(locationURI.toString()))
                remVal = catEnt;
        }
        logger.finer("Removing Value: "+remVal);
        if(remVal == null)
            return;
        int index = catEntList.indexOf(remVal);
        catalogWrapper.deleteSystem(index);
        if(catEntList.size() == 1){
            //file has no entry, so just delete it
            logger.finer("There are no more entries so removing catalog file");
            //publicCatWrap.cleanInstance();
            //publicCatWrap = null;
        }
        logger.finer("RETURN: "+catEntList.size());
    }
    
    public Collection<CatalogEntry> getCatalogEntries() {
        if(this.catalogFileObject == null)
            return Collections.emptyList();
        bootStrapCatalog();
        List<CatalogEntry> catEntList = catalogWrapper.getSystems();
        if(catEntList == null)
            return Collections.emptyList();
        for(CatalogEntry catEnt: catEntList)
            ((CatalogEntryImpl)catEnt).setCatalogModel(this);
        return catEntList;
    }
    
    public boolean isWellformed() {
        bootStrapCatalog();
        currentStateOfCatalog = catalogWrapper.getCatalogState();
        if(currentStateOfCatalog == DocumentModel.State.NOT_WELL_FORMED)
            return false;
        else
            return true;
    }
    
    public FileObject getCatalogFileObject() {
        return this.catalogFileObject;
    }
    
    public void addPropertychangeListener(PropertyChangeListener pcl) {
        this.pcs.addPropertyChangeListener(PROPERTY_CHANGE_PROPERTY_KEY, pcl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        this.pcs.removePropertyChangeListener(PROPERTY_CHANGE_PROPERTY_KEY, pcl);
    }
    
    private synchronized void bootStrapCatalog(){
        if(catalogWrapper == null){
            try {
                catalogWrapper = CatalogFileWrapperDOMImpl.getInstance(this.catalogFileObject, unitTestSaveStrategy);
                
                if(catalogWrapper == null)
                    throw new IllegalStateException("Could not get CatalogFileWrapper");
                currentStateOfCatalog = catalogWrapper.getCatalogState();
                catalogWrapper.addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        pcs.firePropertyChange(evt);
                        currentStateOfCatalog = catalogWrapper.getCatalogState();
                    }
                });
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        if(catalogWrapper != null){
            if(catalogWrapper.getCatalogState() == DocumentModel.State.NOT_WELL_FORMED)
                throw new IllegalStateException("Catalog file not wellformed");
        }
        
        
    }
    
    public void addNextCatalog(URI nextCatalogFileURI, boolean relativize) throws IOException {
        if(this.catalogFileObject == null)
            return;
        
        String nextCatalogFileURIStr = nextCatalogFileURI.toString();
        if(nextCatalogFileURI.isAbsolute() && relativize){
            //then resolve URI relative to this catalog file
            nextCatalogFileURIStr = Utilities.relativize(FileUtil.toFile(this.catalogFileObject).
                    toURI(), nextCatalogFileURI);
        }
        
        try {
            removeNextCatalog(new URI(nextCatalogFileURIStr));
        } catch (URISyntaxException ex) {
        } catch (IOException ex) {
        }
        
        bootStrapCatalog();
        
        CatalogEntry catEnt = new CatalogEntryImpl(CatalogElement.nextCatalog,
                nextCatalogFileURIStr, null);
        catalogWrapper.addNextCatalog(catEnt);
    }
    
    public void removeNextCatalog(URI nextCatalogFileRelativeURI) throws IOException {
        logger.finer("ENTRING:"+nextCatalogFileRelativeURI);
        if(this.catalogFileObject == null)
            return;
        
        bootStrapCatalog();
        
        List<CatalogEntry> catEntList = catalogWrapper.getNextCatalogs();
        if(catEntList == null)
            return;
        CatalogEntry remVal = null;
        for(CatalogEntry catEnt : catEntList){
            if(catEnt.getSource().equals(nextCatalogFileRelativeURI.toString()))
                remVal = catEnt;
        }
        logger.finer("Removing Value: "+remVal);
        if(remVal == null)
            return;
        int index = catEntList.indexOf(remVal);
        catalogWrapper.deleteNextCatalog(index);
        if(catEntList.size() == 1){
            //file has no entry, so just delete it
            logger.finer("There are no more entries so removing catalog file");
            //publicCatWrap.cleanInstance();
            //publicCatWrap = null;
        }
        logger.finer("RETURN: "+catEntList.size());
        
    }
    
    
}

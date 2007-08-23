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
package org.netbeans.modules.xml.retriever.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.retriever.Retriever;
import org.netbeans.modules.xml.retriever.XMLCatalogProvider;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Samaresh
 */
public class Util {

    private Util() {
    }
    
    public static boolean retrieveAndCache(URI locationURI, FileObject sourceFileObject, boolean newThread) {
        URI privateCatalogURI = null;
        URI privateCacheURI = null;
        
        Project prj = FileOwnerQuery.getOwner(sourceFileObject);
        if(prj == null)
            return false;
        
        FileObject prjrtfo = prj.getProjectDirectory();
        File prjrt = FileUtil.toFile(prjrtfo);
        if(prjrt == null)
            return false;
        
        //determine the cache dir
        CacheDirectoryProvider cdp = (CacheDirectoryProvider) prj.getLookup().
                lookup(CacheDirectoryProvider.class);
        String catalogstr = Utilities.DEFAULT_PRIVATE_CATALOG_URI_STR;
        String cachestr = Utilities.DEFAULT_PRIVATE_CAHCE_URI_STR;
        try{
            if( (cdp != null) && (cdp.getCacheDirectory() != null) ){
                URI prjrturi = prjrt.toURI();
                URI cpduri = FileUtil.toFile(cdp.getCacheDirectory()).toURI();
                String cachedirstr = Utilities.relativize(prjrturi, cpduri);
                catalogstr = cachedirstr+"/"+Utilities.PRIVATE_CATALOG_URI_STR;
                cachestr = cachedirstr+"/"+Utilities.PRIVATE_CAHCE_URI_STR;
            }
            privateCatalogURI = new URI(catalogstr);
            privateCacheURI = new URI(cachestr);
        }catch(Exception e){
            return false;
        }
        
        //retrieve
        URI cacheURI = prjrt.toURI().resolve(privateCacheURI);
        File cacheFile = new File(cacheURI);
        if(!cacheFile.isDirectory())
            cacheFile.mkdirs();
        FileObject cacheFO = FileUtil.toFileObject(FileUtil.normalizeFile(cacheFile));
        if(cacheFO == null)
            return false;
        Retriever ret = Retriever.getDefault();
        FileObject result;
        try {
            ((RetrieverImpl) ret).setNewThread(newThread);
            result = ret.retrieveResource(cacheFO, privateCatalogURI, locationURI);
        } catch (UnknownHostException ex) {
            result = null;
        } catch (IOException ex) {
            result = null;
        } catch (URISyntaxException ex) {
            result = null;
        }
        
        /*if(result == null)
            return false;*/
        
        //add private catalog as next catalog file to the public and peer catalog
        XMLCatalogProvider catProv = (XMLCatalogProvider) prj.getLookup().
                lookup(XMLCatalogProvider.class);
        FileObject publicCatFO = null;
        FileObject peerCatFO = null;
        if(catProv != null){
            
            //get public catalog
            URI publicCatURI = catProv.getProjectWideCatalog();
            if(publicCatURI != null){

                URI pubcatURI = prjrt.toURI().resolve(publicCatURI);
                if(pubcatURI != null){

                    File pubcatFile = new File(pubcatURI);
                    if(!pubcatFile.isFile())
                        try {
                            pubcatFile.createNewFile();
                        } catch (IOException ex) {
                        }
                    publicCatFO = FileUtil.toFileObject(FileUtil.
                            normalizeFile(pubcatFile));
                }
            }
            
            //get peer catalog
            URI peerCatURI = catProv.getCatalog(sourceFileObject);
            if(peerCatURI != null){
                URI peercatURI = prjrt.toURI().resolve(peerCatURI);
                if(peercatURI != null){
                    File peercatFile = new File(peercatURI);
                    if(!peercatFile.isFile())
                        try {
                            peercatFile.createNewFile();
                        } catch (IOException ex) {
                        }
                    peerCatFO = FileUtil.toFileObject(FileUtil.
                            normalizeFile(peercatFile));
                }
            }
        }
        //get the catalog write model
        //add next cat entry to public catalog
        URI cacheCatFullURI = FileUtil.toFile(prjrtfo).toURI().resolve(privateCatalogURI);
        CatalogWriteModel catWriter = null;
        try {
            if(publicCatFO == null){
                //get the public catalog legacy way
                catWriter = CatalogWriteModelFactory.getInstance().
                        getCatalogWriteModelForProject(sourceFileObject);
            } else{
                catWriter = CatalogWriteModelFactory.getInstance().
                        getCatalogWriteModelForCatalogFile(publicCatFO);
            }
        } catch (CatalogModelException ex) {}
        if(catWriter == null){
            //return true. May be public cat had the priv cat entry already
            return true;
        }
        try {
            catWriter.addNextCatalog(cacheCatFullURI, true);
        } catch (IOException ex) {
        }
        
        //add the next cat entry to peer catalog
        if(publicCatFO != peerCatFO){
            //get the catalog write model
            catWriter = null;
            try {
                if(peerCatFO == null){
                    //get the public catalog legacy way
                    catWriter = CatalogWriteModelFactory.getInstance().
                            getCatalogWriteModelForProject(sourceFileObject);
                } else{
                    catWriter = CatalogWriteModelFactory.getInstance().
                            getCatalogWriteModelForCatalogFile(peerCatFO);
                }
            } catch (CatalogModelException ex) {}
            if(catWriter == null){
                //return true. May be public cat had the priv cat entry already
                return true;
            }
            try {
                catWriter.addNextCatalog(cacheCatFullURI, true);
            } catch (IOException ex) {
            }
        }
        return true;
    }
    
    public static FileObject getProjectCatalogFileObject(Project prj, boolean create) throws IOException {
        if(prj == null)
            return null;
        
        FileObject result = null;
        FileObject myProjectRootFileObject = prj.getProjectDirectory();
        
        //see if this prj has XMLCatalogProvider. If yes use it.
        XMLCatalogProvider catProv =  prj.getLookup().lookup(org.netbeans.modules.xml.retriever.XMLCatalogProvider.class);
        if(catProv != null){
            URI caturi = catProv.getProjectWideCatalog();
            if(caturi != null){
                caturi = FileUtil.toFile(myProjectRootFileObject).toURI().resolve(caturi);
                File catFile = new File(caturi);
                if(!catFile.isFile()){
                    catFile.createNewFile();
                }
                result = FileUtil.toFileObject(FileUtil.normalizeFile(catFile));
            }
        }
        
        if(result == null){
            String fileName = CatalogWriteModel.PUBLIC_CATALOG_FILE_NAME+CatalogWriteModel.CATALOG_FILE_EXTENSION;
            result = myProjectRootFileObject.getFileObject(fileName);
            if(result == null && create){
                result = myProjectRootFileObject.createData(fileName);
            }
        }
        return result;
    }
    

}

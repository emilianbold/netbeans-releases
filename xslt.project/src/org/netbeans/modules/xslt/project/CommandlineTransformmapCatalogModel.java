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
package org.netbeans.modules.xslt.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;


import javax.swing.text.Document;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;

import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.spi.TMapModelFactory;

import org.openide.util.lookup.Lookups;
import org.openide.util.Lookup;

/**
 *
 * This class helps to obtain TMapModel from transformmap file URI
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class CommandlineTransformmapCatalogModel  implements CatalogModel {

    static CommandlineTransformmapCatalogModel singletonCatMod = null;
    static File projectCatalogFileLocation = null;

    public CommandlineTransformmapCatalogModel() {
      URI catalogFileLocPath= CommandlineXsltProjectXmlCatalogProvider.getInstance().getProjectWideCatalogForWizard();
      if (catalogFileLocPath != null) {
          projectCatalogFileLocation = new File(catalogFileLocPath);
      }
    }

    /**
     * Gets the instance of this class internal API
     * @return current class instance
     */
    public static CommandlineTransformmapCatalogModel getDefault(){
        if (singletonCatMod == null){
            singletonCatMod = new CommandlineTransformmapCatalogModel();
        }
        return singletonCatMod;
    }

    private File getProjectCatalogXML() {
        if (projectCatalogFileLocation != null && projectCatalogFileLocation.exists()) {
            return projectCatalogFileLocation;
        } else {
            return null;
        }
    }    
    
    /**
     * Gets the  Model source for Transformmap URI
     * @param locationURI URI location of the Transformmap File
     * @return ModelSource return ModelSource
     * @throws org.netbeans.modules.xml.xam.locator.CatalogModelException 
     */
    public ModelSource getModelSource(URI locationURI) throws CatalogModelException {
       List<File> catalogFileList = new ArrayList<File>();
       File file = null;
       File projectCatalogXML = getProjectCatalogXML();
        if (projectCatalogXML != null) {
            catalogFileList.add(projectCatalogXML );
        }
       if (catalogFileList.size() > 0) {
            URI uri = null;

            try {
                uri = resolveUsingApacheCatalog(catalogFileList, locationURI.toString());
            }catch (IOException ioe) {
                
            }

            if (uri != null ) {
                file =new File(uri);
            } else {
                try {
                    file = new File(locationURI);
                }catch ( IllegalArgumentException ie) {
                    throw new CatalogModelException("Invalid URI"+locationURI.toString());
                }
            }
       } else {
            try {
                file = new File(locationURI);
            }catch ( IllegalArgumentException ie) {
                    throw new CatalogModelException("Invalid URI"+locationURI.toString());
           }
       }
        return createModelSource(file, true);
    }

    /**
     * Implementation of CatalogModel
     * @param locationURI 
     * @param modelSourceOfSourceDocument 
     * @throws org.netbeans.modules.xml.xam.locator.CatalogModelException 
     * @return 
     */
    public ModelSource getModelSource(URI locationURI,
                                      ModelSource modelSourceOfSourceDocument) throws CatalogModelException {
        if(locationURI == null) {
            return null;
        }
        
        URI resolvedURI = locationURI;
        
        if(modelSourceOfSourceDocument != null) {
            File sFile = (File) modelSourceOfSourceDocument.getLookup().lookup(File.class);
            
            if(sFile != null) {
                URI sURI = sFile.toURI();
                resolvedURI = sURI.resolve(locationURI);
            }
            
        }
        
        if(resolvedURI != null) {
            return getModelSource(resolvedURI);
        }
        
        return null;
    }

    /**
     * Implementation of CatalogModel
     * @param file 
     * @return 
     */
     protected Document getDocument(File file) throws CatalogModelException{
        Document result = null;

        if (result != null) return result;
        try {


            FileInputStream fis = new FileInputStream(file);
            byte buffer[] = new byte[fis.available()];
//                result = new org.netbeans.editor.BaseDocument(
//                        org.netbeans.modules.xml.text.syntax.XMLKit.class, false);
            result = new javax.swing.text.PlainDocument();
            result.remove(0, result.getLength());
            fis.read(buffer);
            fis.close();
            String str = new String(buffer);
            result.insertString(0,str,null);

        } catch (Exception dObjEx) {
            throw new CatalogModelException(file.getAbsolutePath()+" File Not Found");
        }

        return result;
    }   

    private TMapModelFactory getModelFactory() {
        TMapModelFactory factory =null;
        try {
            factory = TMapModelFactory.TMapModelFactoryAccess.getFactory();
        } catch (Exception cnfe) {
            throw new RuntimeException(cnfe);
        }
        return factory;
    }
    
    /**
     * Implementation of CatalogModel
     * @param file 
     * @param readOnly 
     * @throws org.netbeans.modules.xml.xam.locator.CatalogModelException 
     * @return 
     */
     public ModelSource createModelSource(File file, boolean readOnly) throws CatalogModelException{
         
         Lookup lookup = Lookups.fixed(new Object[]{
                file,
                getDocument(file),
                getDefault(),
              //  new StreamSource(file)
                file
            });
            
         return new ModelSource(lookup, readOnly);
     }
    
    /**
     * Creates Transformmap Model from file URI
     * @param locationURI 
     * @throws java.lang.Exception 
     * @return 
     */
    public TMapModel getTMapModel(URI locationURI) throws Exception {
        ModelSource source = getDefault().getModelSource(locationURI);
        TMapModel model = getModelFactory().getModel (source);
        model.sync();
        return model;
    }
    
    /**
     * Creates WSDL Model from file URI
     * @param locationURI 
     * @throws java.lang.Exception 
     * @return 
     */
    public WSDLModel getWsdlModel(URI locationURI) throws Exception {
        ModelSource source = getDefault().getModelSource(locationURI);
        if (source == null) {
            return null;
        }
        WSDLModel model = WSDLModelFactory.getDefault().getModel (source);
        return model;
    }    
    protected URI resolveUsingApacheCatalog(List<File> catalogFileList, String locationURI) throws CatalogModelException, IOException  {
        CatalogResolver catalogResolver;
        Catalog apacheCatalogResolverObj;    

        
        CatalogManager manager = new CatalogManager(null);
        manager.setUseStaticCatalog(false);
        manager.setPreferPublic(false);
        catalogResolver = new CatalogResolver(manager);
        //catalogResolver = new CatalogResolver(true);
        apacheCatalogResolverObj = catalogResolver.getCatalog();
        
        for(File catFile : catalogFileList){
            if (catFile.length() > 0) {
                try {
                    apacheCatalogResolverObj.parseCatalog(catFile.getAbsolutePath());
                } catch (Throwable ex) {
                    throw new CatalogModelException(ex);
                }
                
                String result = null;
                try {
                    result = apacheCatalogResolverObj.resolveSystem(locationURI);
                } catch (MalformedURLException ex) {
                    result = "";
                } catch (IOException ex) {
                    result = "";
                }
                if(result == null){
                    result = "";
                }else{
                    try {
                        //This is a workaround for a bug in resolver module on windows.
                        //the String returned by resolver is not an URI style
                        result = Utilities.normalizeURI(result);
                        URI uri = new URI(result);
                        if(uri.isOpaque()){
                            if(uri.getScheme().equalsIgnoreCase("file")){
                                StringBuffer resBuff = new StringBuffer(result);
                                result = resBuff.insert("file:".length(), "/").toString();
                            }
                        }
                    } catch (URISyntaxException ex) {
                        return null;
                    }
                }
                if(result.length() > 0 ){
                    try {
                        URI res =  new URI(result);
                        return res;
                    } catch (URISyntaxException ex) {
                    }
                }
            }
        }
        return null;
    }    

     public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
         //TODO implement this method.
         return null;
     }
     
     public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
         //TODO implement this method.
         return null;
     }    
}

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
package org.netbeans.modules.bpel.project.anttasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;

import java.util.Properties;

import javax.swing.text.Document;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;

import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.spi.BpelModelFactory;
import org.netbeans.modules.bpel.project.CommandlineBpelProjectXmlCatalogProvider;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;

import org.openide.util.lookup.Lookups;
import org.openide.util.Lookup;

/**
 * This class helps Bpel project to obtain the Bpel model given a
 * BPEL File URI
 * @author Sreenivasan Genipudi
 */
public class BPELCatalogModel implements CatalogModel {
    
    static BPELCatalogModel singletonCatMod = null;
    static File projectCatalogFileLocation = null;
    
        
    /**
     * Constructor
     */
    public BPELCatalogModel() {
      URI catalogFileLocPath= CommandlineBpelProjectXmlCatalogProvider.getInstance().getProjectWideCatalogForWizard();
      if (catalogFileLocPath != null) {
          projectCatalogFileLocation = new File(catalogFileLocPath);
      }
    }
    
    /**
     * Gets the instance of this class internal API
     * @return current class instance
     */
    public static BPELCatalogModel getDefault(){
        if (singletonCatMod == null){
            singletonCatMod = new BPELCatalogModel();
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
     * Gets the  Model source for BPEL URI
     * @param locationURI URI location of the BPEL File
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
    public ModelSource getModelSource(URI locationURI, ModelSource modelSourceOfSourceDocument) throws CatalogModelException {
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
    
    private BpelModelFactory getModelFactory() {
        BpelModelFactory factory =null;
        try {
            factory = (BpelModelFactory)//Lookups.metaInfServices(getClass().getClassLoader()).lookup(BpelModelFactory.class);            
                Lookup.getDefault().lookup(BpelModelFactory.class);
        }catch (Exception cnfe) {
         System.out.println(cnfe.getMessage());
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
     * Creates BPEL Model from BPEL URI
     * @param locationURI 
     * @throws java.lang.Exception 
     * @return 
     */
    public BpelModel getBPELModel(URI locationURI) throws Exception {
        ModelSource source = getDefault().getModelSource(locationURI);
        BpelModel model = getModelFactory().getModel (source);
        model.sync();
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
    /*
    private static String normalizeURI(String uriref) {
	StringBuilder newRef = new StringBuilder();
	byte[] bytes;
	
	if (uriref == null) {
	    return null;
	}
	
	try {
	    bytes = uriref.getBytes("UTF-8");
	} catch (UnsupportedEncodingException uee) {
	    // this can't happen
	    return uriref;
	}
	
	for (int count = 0; count < bytes.length; count++) {
	    int ch = bytes[count] & 0xFF;
	    
	    if ((ch <= 0x20)    // ctrl
	    || (ch > 0x7F)  // high ascii
	    || (ch == 0x22) // "
	    || (ch == 0x3C) // <
	    || (ch == 0x3E) // >
	    || (ch == 0x5C) // \
	    || (ch == 0x5E) // ^
	    || (ch == 0x60) // `
	    || (ch == 0x7B) // {
	    || (ch == 0x7C) // |
	    || (ch == 0x7D) // }
	    || (ch == 0x7F)) {
		newRef.append(encodedByte(ch));
	    } else {
		newRef.append((char) bytes[count]);
	    }
	}
	
	return newRef.toString();
    }    
    
    public static String encodedByte(int b) {
	String hex = Integer.toHexString(b).toUpperCase();
	if (hex.length() < 2) {
	    return "%0" + hex;
	} else {
	    return "%" + hex;
	}
    }   */ 
    
     public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
         //TODO FIXME: this is to fix a build break. Please implement this method.
         return null;
     }
     
     public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
         //TODO FIXME: this is to fix a build break. Please implement this method.
         return null;
     }    
}

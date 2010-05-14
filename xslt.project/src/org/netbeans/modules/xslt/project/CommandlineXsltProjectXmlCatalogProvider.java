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

import java.net.URI;

/**
 *
 * Basic Java class representing the XML Catalog Provider. This class is 
 * used by both in Populate Catalog Wizard and in Ant task for project building.
 * The reason for creation of this class is to eliminate the netbeans 
 * dependency XMLCatalogProvider has on Project API( FileObject)
 * @author Sreenivasan Genipudi
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class CommandlineXsltProjectXmlCatalogProvider {

    private String mCatalogXMLPath=null;
    private String mRetreiverPath =null;
    private URI mCatalogXMLURI = null;
    private String mSourceDir = null;
    private static CommandlineXsltProjectXmlCatalogProvider mInstance= null;
    private URI mCatlogXMLLocationForWizardURI = null;
   
    CommandlineXsltProjectXmlCatalogProvider() {
    }
    
    /**
     * Singleton
     * @return The current instance
     */
    public static CommandlineXsltProjectXmlCatalogProvider getInstance() {
        if (mInstance == null) {
            mInstance = new CommandlineXsltProjectXmlCatalogProvider();
        }
        return mInstance;
    }
    
    /**
     * Set the source directory
     * @param sourceDir Source directory
     */
    public void setSourceDirectory(String sourceDir) {
        mSourceDir = sourceDir;
        String projectDir=  mSourceDir +File.separator+ ".."+File.separator;
        String catalogXMLDir= projectDir+org.netbeans.modules.xml.retriever.XMLCatalogProvider.TYPE_RETRIEVED;
        mCatalogXMLPath =(catalogXMLDir+File.separator+"catalog.xml").replace('\\','/');;
        mRetreiverPath =(catalogXMLDir+File.separator+"src").replace('\\','/');
        
        mCatlogXMLLocationForWizardURI = new File((projectDir + File.separator + "catalog.xml").replace('\\','/')).toURI();
        mCatalogXMLURI = new File(mCatalogXMLPath).toURI();
    }
    
    public URI getProjectCatalogUri() {
        return mCatlogXMLLocationForWizardURI;
    }        

    /**
     * Set the catalog xml location
     * @param catalogXMLPath Catalog XML location
     */
    public  void setCatalogXMLPath(String catalogXMLPath) {
        mCatalogXMLPath = catalogXMLPath;
    }
    
    /**
     * Get the Retriever download location
     * @return Get the Retriever download location
     */
    public String getRetrieverPath() {
        return mRetreiverPath;
    }

    /**
     * Get the project wide Catalog
     * @return Location of Project wide catalog
     */
    public URI getProjectWideCatalog(){
        return mCatalogXMLURI;
    }   
    
    public URI getProjectWideCatalogForWizard(){
        return mCatlogXMLLocationForWizardURI;
    }        
}

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
        
        mCatlogXMLLocationForWizardURI = new File((projectDir + File.separator+"catalog.xml").replace('\\','/')).toURI();
        mCatalogXMLURI = new File(mCatalogXMLPath).toURI();
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

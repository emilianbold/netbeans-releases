/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.iep.project.anttasks.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.net.URI;


import javax.swing.text.Document;


import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;

import org.openide.util.lookup.Lookups;
import org.openide.util.Lookup;

public class CliIEPCatalogModel implements CatalogModel {
    
    static CliIEPCatalogModel singletonCatMod = null;
    
        
    /**
     * Constructor
     */
    public CliIEPCatalogModel() {
     
    }
    
    /**
     * Gets the instance of this class internal API
     * @return current class instance
     */
    public static CliIEPCatalogModel getDefault(){
        if (singletonCatMod == null){
            singletonCatMod = new CliIEPCatalogModel();
        }
        return singletonCatMod;
    }
    
    
    public ModelSource getModelSource(URI locationURI) throws CatalogModelException {
       File file = null;
        try {
            file = new File(locationURI);
        }catch ( IllegalArgumentException ie) {
                throw new CatalogModelException("Invalid URI"+locationURI.toString());
       }
       
        return createModelSource(file, true);
    }
    
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

     protected Document getDocument(File file) throws CatalogModelException{
        Document result = null;

        if (result != null) return result;
        try {


            FileInputStream fis = new FileInputStream(file);
            byte buffer[] = new byte[fis.available()];
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
    
    
     public ModelSource createModelSource(File file, boolean readOnly) throws CatalogModelException{
         
         Lookup lookup = Lookups.fixed(new Object[]{
                new IEPReadOnlyAccessProvider(),
                file,
                getDocument(file),
                getDefault(),
                file
                
            });
            
         return new ModelSource(lookup, readOnly);
     }
        
     public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
         return null;
     }
     
     public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
         return null;
     }    
}

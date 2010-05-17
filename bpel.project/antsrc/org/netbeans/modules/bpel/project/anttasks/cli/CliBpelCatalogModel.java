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
package org.netbeans.modules.bpel.project.anttasks.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;

import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.spi.BpelModelFactory;
import org.netbeans.modules.bpel.project.CommandlineBpelProjectXmlCatalogProvider;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;

import org.netbeans.modules.xml.reference.ReferenceUtil;
import org.netbeans.modules.xml.reference.ReferenceTraveller;
import org.netbeans.modules.bpel.project.anttasks.util.PackageCatalogArtifacts;

import org.openide.util.lookup.Lookups;
import org.openide.util.Lookup;

public class CliBpelCatalogModel implements CatalogModel {
    
    public static CliBpelCatalogModel getDefault() {
        return singletonCatMod;
    }
    
    public ModelSource getModelSource(URI locationURI) throws CatalogModelException {
//System.out.println();
//System.out.println();
//System.out.println();
//System.out.println("---------- GET MODEL SOURCE: " + locationURI + " " + locationURI.hashCode());
//System.out.println();
       List<File> catalogFileList = new ArrayList<File>();
       // # 154887
       collectCatalogXmlRecursively(CommandlineBpelProjectXmlCatalogProvider.getInstance().getProjectDir(), catalogFileList);
       File file = null;

       if (catalogFileList.size() > 0) {
//System.out.println("---------- 1");
            URI uri = null;

            try {
                uri = resolveUsingApacheCatalog(catalogFileList, locationURI);
//System.out.println("---------- 1: " + uri);
            }
            catch (IOException ioe) {
            }
//System.out.println("---------- 2: " + uri);
            if (uri != null ) {
                // # 157902
                file = getFileByURI(uri, catalogFileList);
//System.out.println("---------- 2.1 THIS: " + file);
            }
            else {
                try {
                    file = new File(locationURI);
//System.out.println("---------- 2.2: " + file);
                }
                catch (IllegalArgumentException ie) {
                    throw new CatalogModelException("Invalid URI: " + locationURI.toString());
                }
            }
       } else {
//System.out.println("---------- 3");
            try {
                file = new File(locationURI);
            }
            catch (IllegalArgumentException ie) {
                    throw new CatalogModelException("Invalid URI: " + locationURI.toString());
           }
       }
//System.out.println("--------- end.");
//System.out.println();
//System.out.println();
//System.out.println();
       return createModelSource(file, true);
    }

    private File getFileByURI(URI uri, List<File> catalogs) throws CatalogModelException {
        if ( !uri.getScheme().equals("nb-uri")) {
            return new File(uri);
        }
//System.out.println();
//System.out.println("uuuuuuuu projectCatalogFileLocation: " + projectCatalogFileLocation + " " + uri);
        FileObject file = null;

        for (File catalogFile : catalogs) {
            FileObject catalog = FileUtil.toFileObject(catalogFile);
            Project project = FileOwnerQuery.getOwner(catalog);
//System.out.println("uuuuuuuu project: " + project);
//System.out.println();
//System.out.println();
            file = PackageCatalogArtifacts.getCatalogSupport(project).resolveProjectProtocol(uri);
    
            if (file != null) {
                break;
            }
        }
        if (file == null) {
            throw new CatalogModelException("Can't resolve referenced file. Check the referenced resource: " + uri);
        }
        return FileUtil.toFile(file);
    }

    private void collectCatalogXmlRecursively(File projectDir, final List<File> catalogs) {
        Project project = FileOwnerQuery.getOwner(projectDir.toURI());

        ReferenceUtil.travelRecursively(project, new ReferenceTraveller() {
            public void travel(Project project) {
                File projectCatalogXML = getProjectCatalogXML(project);
//System.out.println("---------- projectCatalogXML: " + projectCatalogXML);

                if (projectCatalogXML != null) {
                     catalogs.add(projectCatalogXML);
                }
            }
        });
    }

    private File getProjectCatalogXML(Project project) {
        File projectFolder = FileUtil.toFile(project.getProjectDirectory());
        return CommandlineBpelProjectXmlCatalogProvider.getProjectCatalogFile(projectFolder.getAbsolutePath());
    }    
    
    public ModelSource getModelSource(URI locationURI, ModelSource modelSourceOfSourceDocument) throws CatalogModelException {
        if (locationURI == null) {
            return null;
        }
        URI resolvedURI = locationURI;
        
        if (modelSourceOfSourceDocument != null) {
            File sFile = (File) modelSourceOfSourceDocument.getLookup().lookup(File.class);
            
            if (sFile != null) {
                URI sURI = sFile.toURI();
                resolvedURI = sURI.resolve(locationURI);
            }
            
        }
//System.out.println();
//System.out.println("(((((((((((((: " + resolvedURI);
//System.out.println("(((((((((((((: " + resolvedURI.getScheme());
//System.out.println();
        
        if (resolvedURI != null && resolvedURI.getScheme().equals("file") && new File(resolvedURI).exists()) {
            return getModelSource(resolvedURI);
        }
        else {
//System.out.println();
//System.out.println("(((((((((((((: " + locationURI);
//System.out.println("(((((((((((((: " + resolvedURI + " " + new File(resolvedURI).exists());
//System.out.println();
            return getModelSource(locationURI);
        }
    }

    private Document getDocument(File file) throws CatalogModelException{
        try {
            FileInputStream inputStream = new FileInputStream(file);
            String encoding = null;

            if (inputStream.markSupported()) {
                encoding = EncodingUtil.detectEncoding(inputStream);
            }
//System.out.println("!!! encoding: " + encoding);

            if (encoding == null) {
                encoding = "UTF8"; // NOI18N
            }
            Reader reader = EncodingUtil.getUnicodeReader(inputStream, encoding);
            Document document = new PlainDocument();
            new DefaultEditorKit().read(reader, document, 0);

            return document;
        }
        catch (FileNotFoundException e) {
            throw new CatalogModelException("File " + file.getAbsolutePath() + " is not found."); // NOI18N
        }
        catch (IOException e) {
            throw new CatalogModelException("I/O problem with file " + file.getAbsolutePath()); // NOI18N
        }
        catch (BadLocationException e) {
            throw new CatalogModelException("Bad location in file " + file.getAbsolutePath() + ": " + e.getMessage()); // NOI18N
        }
    }   

    private BpelModelFactory getModelFactory() {
        try {
            return (BpelModelFactory) Lookup.getDefault().lookup(BpelModelFactory.class);
        }
        catch (Exception cnfe) {
            throw new RuntimeException(cnfe);
        }
    }
    
    public ModelSource createModelSource(File file, boolean readOnly) throws CatalogModelException{
         Lookup lookup = Lookups.fixed(new Object[]{
                file,
                getDocument(file),
                getDefault(),
                file
         });
         return new ModelSource(lookup, readOnly);
    }
    
    public BpelModel getBPELModel(URI locationURI) throws Exception {
        ModelSource source = getDefault().getModelSource(locationURI);
        BpelModel model = getModelFactory().getModel (source);
        model.sync();
        return model;
    }
    
    private URI resolveUsingApacheCatalog(List<File> catalogFileList, URI locationURI) throws CatalogModelException, IOException  {
//System.out.println();
//System.out.println();
//System.out.println("oooooooooooooooooo resolveUsingApacheCatalog: " + locationURI);
        CatalogResolver catalogResolver;
        Catalog apacheCatalogResolverObj;    
        CatalogManager manager = new CatalogManager(null);
        manager.setUseStaticCatalog(false);
        manager.setPreferPublic(false);
        manager.setIgnoreMissingProperties(true);
        catalogResolver = new CatalogResolver(manager);
        apacheCatalogResolverObj = catalogResolver.getCatalog();
        
        for (File catFile : catalogFileList){
            if (catFile.length() > 0) {
                try {
                    apacheCatalogResolverObj.parseCatalog(catFile.getAbsolutePath());
                }
                catch (Throwable ex) {
                    throw new CatalogModelException(ex);
                }
                String result = null;

                try {
//System.out.println("oooooooooooooooooo resolve: " + locationURI);
                    result = apacheCatalogResolverObj.resolveSystem(locationURI.toString());
//System.out.println("oooooooooooooooooo  result: " + result);
                } catch (MalformedURLException ex) {
                    result = "";
                } catch (IOException ex) {
                    result = "";
                }

                if (result == null){
                    result = "";
                }
                else {
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
        return null;
    }
     
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        return null;
    }    

    private static CliBpelCatalogModel singletonCatMod = new CliBpelCatalogModel();
}

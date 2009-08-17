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


package org.netbeans.modules.compapp.test.wsdl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import java.util.logging.Logger;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * WsdlSupport.java
 *
 * Created on February 2, 2006, 11:48 AM
 *
 */
public class WsdlSupport {
    private static final Logger mLog = Logger.getLogger("org.netbeans.modules.compapp.test.wsdl.WsdlSupport"); // NOI18N
    
    private String mWsdlUrl;
    private WSDLModel mWsdlModel;
    private SchemaTypeLoader mSchemaTypeLoader;
    private String mWsdlSupportErrStr = ""; //NOI18N
    
    /** Creates a new instance of WsdlSupport */
    public WsdlSupport(FileObject wsdlFileObject) {
        mWsdlUrl = "file:" + FileUtil.toFile(wsdlFileObject).getPath();
        
        try {
            // Although we are not modifying the WSDL, we still create an
            // editable ModelSource. See #111034.
            ModelSource wsdlModelSource = Utilities.createModelSource(wsdlFileObject, true);                      
            mWsdlModel = WSDLModelFactory.getDefault().getModel(wsdlModelSource);
        
            mSchemaTypeLoader = loadSchemaTypes(mWsdlUrl);
        } catch (Exception e) {
            String msg = NbBundle.getMessage(WsdlSupport.class, "LBL_Fail_to_load_schema_types", mWsdlUrl); // NOI18N
            if (e.getMessage() != null) {
                msg += "\n" + e.getMessage();  // NOI18N
                String newline = System.getProperty("line.separator"); // NOI18N
                msg += newline;
                msg += newline;
                msg += "The original error message is:"; // NOI18N
                msg += newline;
                msg += e.getMessage();  // NOI18N
            }
            mWsdlSupportErrStr += msg;
            
            // We don't want the (extra) raw stacktrace thrown in the user's face.
            // mLog.log(Level.SEVERE, msg, e);
            mLog.severe(msg);
        }
    }
    
    // This is only added for unit test purpose
    WsdlSupport(FileObject wsdlFile, ModelSource wsdlModelSource) {
        mWsdlUrl = "file:" + FileUtil.toFile(wsdlFile).getPath();
        
        try {
            // Although we are not modifying the WSDL, we still create an
            // editable ModelSource. See #111034.                    
            mWsdlModel = WSDLModelFactory.getDefault().getModel(wsdlModelSource);
        
            mSchemaTypeLoader = loadSchemaTypes(mWsdlUrl);
        } catch (Exception e) {
            String msg = NbBundle.getMessage(WsdlSupport.class, "LBL_Fail_to_load_schema_types", mWsdlUrl); // NOI18N
            if (e.getMessage() != null) {
                msg += "\n" + e.getMessage();  // NOI18N
                String newline = System.getProperty("line.separator"); // NOI18N
                msg += newline;
                msg += newline;
                msg += "The original error message is:"; // NOI18N
                msg += newline;
                msg += e.getMessage();  // NOI18N
            }
            mWsdlSupportErrStr += msg;
            
            // We don't want the (extra) raw stacktrace thrown in the user's face.
            // mLog.log(Level.SEVERE, msg, e);
            mLog.severe(msg);
        }
    }
    
    public WSDLModel getWsdlModel() {
        return mWsdlModel;
    }
        
    public SchemaTypeLoader getSchemaTypeLoader() {
        return mSchemaTypeLoader;
    }
    
    public String getWsdlSupportError() {
        return mWsdlSupportErrStr;
    }
    
    public void setWsdlSupportError(String s) {
        mWsdlSupportErrStr = s;
    }
    
    private static SchemaTypeLoader loadSchemaTypes(String wsdlUrl) 
            throws XmlException, SchemaException {
        
        // #112499
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(XmlObject.class.getClassLoader());
        
        try {
            Map<String, XmlObject> schemaTable = new HashMap<String, XmlObject>();        
            getSchemas(wsdlUrl, schemaTable, new ArrayList<String>());

            List<XmlObject> schemaList = 
                    new ArrayList<XmlObject>(schemaTable.values());        
            for (XmlObject schema : schemaList) {
                removeImportAndInclude(schema);
            }

            SchemaTypeLoader schemaTypes = loadSchemaTypes(schemaList);
            return schemaTypes;
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
    }
    
    private static SchemaTypeLoader loadSchemaTypes(List schemaList) throws XmlException {
        XmlOptions options = new XmlOptions();
        options.setCompileNoValidation();
        options.setCompileNoPvrRule();
        options.setCompileDownloadUrls();
        options.setCompileNoUpaRule();
        options.setValidateTreatLaxAsSkip();
        
        options.setLoadStripProcinsts();
        options.setCompileNoAnnotations();
        options.setLoadStripComments();
        options.setLoadStripWhitespace();
        options.setLoadTrimTextBuffer();
        
        ArrayList errorList = new ArrayList();
        options.setErrorListener(errorList);
        
        try {
            schemaList.add(
                    // IZ #112499
                    XmlObject.Factory
                    //org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Factory
                    .parse(WsdlSupport.class.getResource(
                    "/org/netbeans/modules/compapp/test/wsdl/resources/soapEncoding.xsd"))); // NOI18N
            XmlObject[] schemaArray = (XmlObject[])schemaList.toArray(new XmlObject[0]);
            return XmlBeans.loadXsd(schemaArray, options);
        } catch (IOException e) {
            //throw new SchemaException(e, errorList);
            e.printStackTrace();
            return null;
        }
    }
    
    private static void getSchemas(
            String wsdlUrl,
            Map<String, XmlObject> schemaTable,
            List<String> visitedSchemaWsdls)
            throws SchemaException {
        
        if(schemaTable.containsKey(wsdlUrl)) {
            return;
        }
        ArrayList errorList = new ArrayList();
        
        Map<String, XmlObject> result = new HashMap<String, XmlObject>();
        
        try {
            XmlOptions options = new XmlOptions();
            options.setCompileNoValidation();
            options.setSaveUseOpenFrag();
            options.setErrorListener(errorList);
            
            options.setLoadStripProcinsts();
            options.setCompileNoAnnotations();
            options.setLoadStripComments();
            options.setLoadStripWhitespace();
            options.setLoadTrimTextBuffer();
            
            options.setSaveSyntheticDocumentElement(
                    new QName("http://www.w3.org/2001/XMLSchema", "schema")); // NOI18N
            
            XmlObject xmlObject = XmlObject.Factory.parse(new URL(wsdlUrl), options);
            
            Document dom = (Document) xmlObject.getDomNode();
            Node domNode = dom.getDocumentElement();
            if (domNode.getLocalName().equals("schema") && // NOI18N
                domNode.getNamespaceURI().equals(
                    "http://www.w3.org/2001/XMLSchema")) { // NOI18N
                result.put(wsdlUrl, xmlObject);
            } else {
                XmlObject[] schemas = xmlObject.selectPath(
                        "declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:schema"); // NOI18N
                
                for (int i = 0; i < schemas.length; i++) {
                    XmlCursor xmlCursor = schemas[i].newCursor();
                    String xmlText = xmlCursor.getObject().xmlText(options);
                    schemas[i] = 
                            // IZ #112499
                            XmlObject.Factory
                            //org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Factory
                            .parse(xmlText, options);
                    schemas[i].documentProperties().setSourceName(wsdlUrl);
                    
                    result.put(wsdlUrl + "@" + (i+1), schemas[i]); // NOI18N
                }
                
                XmlObject[] wsdlImports = xmlObject.selectPath(
                        "declare namespace s='http://schemas.xmlsoap.org/wsdl/' .//s:import/@location"); // NOI18N
                for (int i = 0; i < wsdlImports.length; i++) {
                    String location = ((SimpleValue) wsdlImports[i]).getStringValue();
                    if (location != null) {
                        if (!location.startsWith("file:") && location.indexOf("://") <= 0) { // NOI18N
                            location = resolveRelativeUrl(wsdlUrl, location);
                        }
                        getSchemas(location, schemaTable, visitedSchemaWsdls);                        
                    }
                }
            }
            
            XmlObject[] schemas = (XmlObject[])result.values().toArray(
                    new XmlObject[result.size()]);
            
            for (int c = 0; c < schemas.length; c++) {
                xmlObject = schemas[c];
                
                XmlObject[] schemaImports = xmlObject.selectPath(
                        "declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:import/@schemaLocation"); // NOI18N
                XmlObject[] schemaIncludes = xmlObject.selectPath(
                        "declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:include/@schemaLocation"); // NOI18N
                List<XmlObject> schemaImportsList = Arrays.asList(schemaImports);
                List<XmlObject> schemaIncludesList = Arrays.asList(schemaIncludes);
                List<XmlObject> schemaImportsAndIncludes = new ArrayList<XmlObject>();
                schemaImportsAndIncludes.addAll(schemaImportsList);
                schemaImportsAndIncludes.addAll(schemaIncludesList);
                
                for (XmlObject schemaImportOrInclude : schemaImportsAndIncludes) {
                    String location = ((SimpleValue)schemaImportOrInclude).getStringValue();
                    if (location != null &&
                            // We will be adding soap encoding later. This is to
                            // avoid duplicate global type definition error.
                            !location.equals("http://schemas.xmlsoap.org/soap/encoding/")) { // NOI18N
                        if (!location.startsWith("file:") && location.indexOf("://") <= 0) { // NOI18N
                            location = resolveRelativeUrl(wsdlUrl, location);
                        }
                        if (!visitedSchemaWsdls.contains(location)) {
                            visitedSchemaWsdls.add(location);
                            getSchemas(location, schemaTable, visitedSchemaWsdls);
                        }
                    }
                }
            }
            schemaTable.putAll(result);
        } catch (Exception e) {
            throw new SchemaException(e, errorList);
        }
    }
    
    private static void removeImportAndInclude(XmlObject xmlObject) throws XmlException {
        XmlObject[] imports = xmlObject.selectPath(
                "declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:import"); // NOI18N
        
        for(int c = 0; c < imports.length; c++) {
            imports[c].newCursor().removeXml();
        }
        
        XmlObject[] includes = xmlObject.selectPath(
                "declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:include"); // NOI18N
        
        for(int c = 0; c < includes.length; c++) {
            includes[c].newCursor().removeXml();
        }
    }
    
    private static String resolveRelativeUrl(String baseUrl, String url) {
        if (baseUrl.startsWith("file:")) { // NOI18N
            FileObject fo = FileUtil.toFileObject(new File(baseUrl.substring(5)));
            
            try {
                ModelSource modelSourceOfSourceDocument = 
                        Utilities.createModelSource(fo, false);  

                CatalogWriteModel model = 
                        CatalogWriteModelFactory.getInstance().getCatalogWriteModelForProject(fo);

                ModelSource modelSource = 
                        model.getModelSource(new URI(url), modelSourceOfSourceDocument);
                FileObject targetFO = modelSource.getLookup().lookup(FileObject.class);
                // On *nix, fileObject.getPath() is missing the preceding "/".
                // System.out.println(targetFO);
                return "file:" + FileUtil.toFile(targetFO).getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        baseUrl = baseUrl.replaceAll("\\\\", "/"); // NOI18N
        int ix = baseUrl.lastIndexOf('/'); // NOI18N
        if (ix == -1) {
            ix = baseUrl.lastIndexOf('/'); // NOI18N
        }
        
        while(url.startsWith("../")) { // NOI18N
            int ix2 = baseUrl.lastIndexOf('/', ix-1); // NOI18N
            if(ix2 == -1) {
                break;
            }
            baseUrl = baseUrl.substring(0, ix2+1);
            ix = ix2;
            
            url = url.substring(3);
        }
        
        return baseUrl.substring(0, ix+1) + url;
    }
    
}

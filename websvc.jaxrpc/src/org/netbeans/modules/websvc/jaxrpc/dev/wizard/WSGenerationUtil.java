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

package org.netbeans.modules.websvc.jaxrpc.dev.wizard;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.jaxrpc.dev.dd.gen.wscreation.Bean;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.IndentEngine;
import org.openide.util.RequestProcessor;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class WSGenerationUtil {
    public static final String TEMPLATE_BASE = "/org/netbeans/modules/websvc/jaxrpc/dev/wizard/xsl/"; //NOI18N
    private String genDate;
    private String genAuthor;
    private Map templateCache = new HashMap();
    
    public static String getSelectedPackageName(FileObject targetFolder, Project p) {
        Sources sources = ProjectUtils.getSources(p);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = FileUtil.getRelativePath(groups [i].getRootFolder(), targetFolder);
        }
        if (packageName != null) {
            packageName = packageName.replaceAll("/", ".");
        }
        return packageName+"";
    }
    
    public String getBeanClassName(String wsName) {
        //TO-DO: We should get this from the module
        //Naming convention for web module is: servicename + Impl
        //Naming convention for ejb module is: servicename + Bean
        return wsName + "Impl"; //NOI18N
    }
    
    public String getSEIName(String wsName) {
        return  wsName + "SEI"; //NOI18N
    }
    
    public Bean getDefaultBean() {
        Bean b = new Bean();
        b.setCommentData(true);
        if (genDate == null) {
            genDate = DateFormat.getDateTimeInstance().format(new Date());
            genAuthor = System.getProperty("user.name");
        }
        b.setCommentDataAuthor(genAuthor); //NOI18N
        b.setCommentDataDate(genDate);
        return b;
    }
    
    public String getFullClassName(String pkg, String className) {
        return (pkg==null||pkg.length()==0)?className:pkg+"."+className; //NOI18N
    }
    
    public String generateClass(String template, Bean genData, FileObject pkg, boolean open) throws IOException {
        String clsName = genData.getClassnameName();
        clsName = FileUtil.findFreeFileName(pkg, clsName, "java"); //NOI18N
        genData.setClassnameName(clsName);
        generateClass(template, pkg, clsName, getStreamSource(genData), open);
        return clsName;
    }
    
    public String generateClass(String template, FileObject pkg, String clsName, String inputXml, boolean open)
    throws IOException {
        clsName = FileUtil.findFreeFileName(pkg, clsName, "java"); //NOI18N
        generateClass(template, pkg, clsName, getStreamSource(inputXml), open);
        return clsName;
    }
    
    public FileObject generateWSDL(String template, String wsName, String soapBinding, String portTypeName, FileObject folder, String wsdlName, StreamSource source) throws IOException {
        return generateWSDL(template, wsName, soapBinding, portTypeName, folder, null, wsdlName, source);
    }
    
    public FileObject generateWSDL(String template, String wsName, String soapBinding, String portTypeName, FileObject folder, FileObject originalFolder, String wsdlName, StreamSource source) throws IOException 
    {
        FileObject wsdlFile = folder.createData(FileUtil.findFreeFileName(folder, wsdlName, "wsdl"), "wsdl");  //NOI18N
        FileLock fl = null;
        OutputStream os = null;
        try {
            fl = wsdlFile.lock();
            os = new BufferedOutputStream(wsdlFile.getOutputStream(fl));
            Transformer transformer = getTransformer(template);
            transformer.setParameter("WSNAME", wsName);
            transformer.setParameter("SOAPBINDING", soapBinding);
            if (portTypeName != null) {
                transformer.setParameter("PORTTYPENAME", portTypeName);
            } else {
                return wsdlFile;
            }
            transformer.transform(source, new StreamResult(os));
            os.close();
        }
        catch(TransformerConfigurationException tce) {
            IOException ioe = new IOException();
            ioe.initCause(tce);
            throw ioe;
        }
        catch(TransformerException te) {
            IOException ioe = new IOException();
            ioe.initCause(te);
            throw ioe;
        }
        finally {
            if(os != null) {
                os.close();
            }
            if(fl != null) {
                fl.releaseLock();
            }
        }
        // Also copy the importing wsdl/schema files
        copyImportedSchemas(originalFolder,folder,wsdlFile);
        return wsdlFile;
    }
    
    public void generateClass(String template, FileObject pkg, String clsName, StreamSource source, boolean open) throws IOException {
        FileObject cFile = pkg.createData(clsName,"java"); //NOI18N
        FileLock fl = null;
        OutputStream os = null;
        Writer w = null;
        try {
            fl = cFile.lock();
            os = new BufferedOutputStream(cFile.getOutputStream(fl));
            getTransformer(template).transform(source, new StreamResult(new OutputStreamWriter(os,"UTF8")));
            os.close();
            fl.releaseLock();
            DataObject dobj = DataObject.find(cFile);
            final EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
            Document d = ec.openDocument();
            try {
                String fullText = d.getText(0,d.getLength());
                IndentEngine javaIndent = IndentEngine.find(d);
                StringWriter writer = new StringWriter(d.getLength());
                w = javaIndent.createWriter(d, 0, writer);
                w.write(fullText);
                w.close();
                d.remove(0, d.getLength());
                d.insertString(0, writer.getBuffer().toString(), null);
                ec.saveDocument();
            } catch (BadLocationException ble) {
                ErrorManager.getDefault().notify(ble);
            }
            if (open) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        ec.open();
                    }
                },1000);
            }
        } catch (TransformerConfigurationException tce) {
            IOException ioe = new IOException();
            ioe.initCause(tce);
            throw ioe;
        } catch (TransformerException te) {
            IOException ioe = new IOException();
            ioe.initCause(te);
            throw ioe;
        } finally {
            if (os != null)  {
                os.close();
            }
            if (w != null) {
                w.close();
            }
            if(fl != null) {
                fl.releaseLock();
            }
        }
    }
    
    public String getBaseName(String fullClassName) {
        return fullClassName.substring(fullClassName.lastIndexOf('.')+1); //NOI18N
    }
    
    private Transformer getTransformer(String template) throws TransformerConfigurationException {
        Templates t = (Templates) templateCache.get(template);
        if (t != null) {
            return t.newTransformer();
        }
        InputStream is = new BufferedInputStream(getClass().getResourceAsStream(template));
        TransformerFactory transFactory = TransformerFactory.newInstance();
        transFactory.setURIResolver(new URIResolver() {
            public Source resolve(String href, String base)
            throws TransformerException {
                InputStream is = getClass().getResourceAsStream(
                TEMPLATE_BASE + href.substring(href.lastIndexOf('/')+1));
                if (is == null) {
                    return null;
                }
                
                return new StreamSource(is);
            }
        });
        t = transFactory.newTemplates(new StreamSource(is));
        templateCache.put(template, t);
        return t.newTransformer();
    }
    
    private StreamSource getStreamSource(Bean genData) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            genData.write(bos);
        } finally {
            bos.close();
        }
        return new StreamSource(new ByteArrayInputStream(bos.toByteArray()));
    }
    
    private StreamSource getStreamSource(String xml) throws IOException {
        StringReader sr = new StringReader(xml);
        return new StreamSource(sr);
    }
    
    /** Static method to identify wsdl/schema files to import
    */
    static List /*String*/ getSchemaNames(FileObject fo, boolean fromWsdl) {
            List result = null;
            try {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    SAXParser saxParser = factory.newSAXParser();
                    ImportsHandler handler= (fromWsdl?(ImportsHandler)new WsdlImportsHandler():(ImportsHandler)new SchemaImportsHandler());
                    saxParser.parse(new InputSource(fo.getInputStream()), (DefaultHandler)handler);
                    result = handler.getSchemaNames();
            } catch(ParserConfigurationException ex) {
                    // Bogus WSDL, return null.
            } catch(SAXException ex) {
                    // Bogus WSDL, return null.
            } catch(IOException ex) {
                    // Bogus WSDL, return null.
            }

            return result;
    }
    
    private static interface ImportsHandler {
        public List getSchemaNames();
    }
    
    private static class WsdlImportsHandler extends DefaultHandler implements ImportsHandler {
        
        private static final String W3C_WSDL_SCHEMA = "http://schemas.xmlsoap.org/wsdl"; // NOI18N
        private static final String W3C_WSDL_SCHEMA_SLASH = "http://schemas.xmlsoap.org/wsdl/"; // NOI18N
        
        private List schemaNames;
        
        private boolean insideSchema;
        
        WsdlImportsHandler() {
            schemaNames = new ArrayList();
        }
        
        public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
            if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
                if("types".equals(localname)) { // NOI18N
                    insideSchema=true;
                }
                if("import".equals(localname)) { // NOI18N
                    String wsdlLocation = attributes.getValue("location"); //NOI18N
                    if (wsdlLocation!=null && wsdlLocation.indexOf("/")<0 && wsdlLocation.endsWith(".wsdl")) { //NOI18N
                        schemaNames.add(wsdlLocation);
                    }
                }
            }
            if(insideSchema && "import".equals(localname)) { // NOI18N
                String schemaLocation = attributes.getValue("schemaLocation"); //NOI18N
                if (schemaLocation!=null && schemaLocation.indexOf("/")<0 && schemaLocation.endsWith(".xsd")) { //NOI18N
                    schemaNames.add(schemaLocation);
                }
            }
        }
        
        public void endElement(String uri, String localname, String qname) throws SAXException {
            if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
                if("types".equals(localname)) { // NOI18N
                    insideSchema=false;
                }
            }
        }
        
        public List/*String*/ getSchemaNames() {
            return schemaNames;
        }
    }
    
    private static class SchemaImportsHandler extends DefaultHandler implements ImportsHandler {
        
        private List schemaNames;
     
        SchemaImportsHandler() {
            schemaNames = new ArrayList();
        }
        
        public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
            if("import".equals(localname)) { // NOI18N
                String schemaLocation = attributes.getValue("schemaLocation"); //NOI18N
                if (schemaLocation!=null && schemaLocation.indexOf("/")<0 && schemaLocation.endsWith(".xsd")) { //NOI18N
                    schemaNames.add(schemaLocation);
                }
            }
        }
        
        public List/*String*/ getSchemaNames() {
            return schemaNames;
        }
    }
    
    /* Recursive method that copies all necessary wsdl/schema files imported by FileObject to target folder
     */
    private synchronized void copyImportedSchemas(FileObject resourceFolder, FileObject targetFolder, FileObject fo) throws IOException {
        List schemaNames = getSchemaNames(fo,"wsdl".equals(fo.getExt())); //NOI18N
        Iterator it = schemaNames.iterator();
        while (it.hasNext()) {
            String schemaName = (String)it.next();
            FileObject schemaFile = resourceFolder.getFileObject(schemaName);
            if (schemaFile!=null) {
                FileObject target = targetFolder.getFileObject(schemaFile.getName(),schemaFile.getExt());
                if(target != null) {
                    target.delete();
                }
                //copy the schema file
                FileObject copy = schemaFile.copy(targetFolder,schemaFile.getName(),schemaFile.getExt());
                copyImportedSchemas(resourceFolder, targetFolder, copy);
            }
        }
    }
    
}


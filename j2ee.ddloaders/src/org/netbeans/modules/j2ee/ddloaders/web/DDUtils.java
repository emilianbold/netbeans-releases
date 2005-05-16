/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web;

import org.openide.filesystems.FileObject;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.impl.web.WebAppProxy;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import java.io.*;
import org.xml.sax.*;
//import java.util.*;


/**
 *
 * @author  mkuchtiak
 */
public class DDUtils {

    private static final String EXCEPTION_PREFIX="version:"; //NOI18N   
    
    /** Finds a name similar to requested that uniquely identifies 
     *  element between the other elements of the same name.
     *
     * @param elements checked elements
     * @param identifier name of tag that contains identification value
     * @param o object to be checked
     * @return a free element name
     */
    public static String findFreeName (CommonDDBean[] elements, String identifier, String name) {
        if (checkFreeName (elements, identifier, name)) {
            return name;
        }
        for (int i = 1;;i++) {
            String destName = name + "_"+i; // NOI18N
            if (checkFreeName (elements, identifier, destName)) {
                return destName;
            }
        }
    }
    
    /** Test if given name is free in given context.
     * @param elements checked elements
     * @param identifier name of tag that contains identification value
     * @param o object to be checked
     * @return true, if such name does not exists
     */
    private static boolean checkFreeName (CommonDDBean [] elements, String identifier, Object o) {
        for (int i=0; i<elements.length; i++) {
            Object val = elements[i].getValue (identifier);
            if (val != null && val.equals (o)) {
                return false;
            }
        }
        return true;
    }

    /**  Convenient method for getting the BaseBean object from CommonDDBean object
    */
    public static BaseBean getBaseBean(CommonDDBean bean) {
        if (bean instanceof BaseBean) return (BaseBean)bean;
        else if (bean instanceof WebAppProxy) return (BaseBean) ((WebAppProxy)bean).getOriginal();
        return null;
    }
    /*
    public static WebApp createBeanGraph(org.w3c.dom.Document doc) throws java.io.IOException{
        return createWebApp(doc, getVersion(doc));
    }
    
    public static String getVersion(org.w3c.dom.Document doc) {
        org.w3c.dom.Element root = doc.getDocumentElement();
        String ver = root.getAttribute("version"); //NOI18N
        if (ver==null || !ver.equals(WebApp.VERSION_2_4)) return WebApp.VERSION_2_3; //NOI18N
        else return ver;
    }
    
    public static WebApp createWebApp(org.w3c.dom.Document doc, String version) throws java.io.IOException{
        WebApp webApp=null;
        if (WebApp.VERSION_2_4.equals(version)) {
            webApp = org.netbeans.modules.j2ee.dd.impl.web.model_2_4.WebApp.createGraph(doc);
        } else {
            //webApp = org.netbeans.modules.web.dd.model_2_3.WebApp.createGraph(fo.getInputStream());
            webApp = org.netbeans.modules.j2ee.dd.impl.web.model_2_3.WebApp.createGraph(doc);
        }
        return webApp;
    }
    */
    
    public static WebApp createWebApp(java.io.InputStream is, String version) throws java.io.IOException, SAXException {
        try {
            if (WebApp.VERSION_2_3.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web.model_2_3.WebApp.createGraph(is);
            } else {
                return org.netbeans.modules.j2ee.dd.impl.web.model_2_4.WebApp.createGraph(is);
            }
        } catch (RuntimeException ex) {
            throw new SAXException (ex.getMessage());
        }
    }
    /** Parsing just for detecting the version  SAX parser used
    */
    public static String getVersion(InputSource is) throws IOException, SAXException {
        javax.xml.parsers.SAXParserFactory fact = javax.xml.parsers.SAXParserFactory.newInstance();
        fact.setValidating(false);
        try {
            javax.xml.parsers.SAXParser parser = fact.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setContentHandler(new VersionHandler());
            reader.setEntityResolver(DDResolver.getInstance());
            try {
                reader.parse(is);
            } catch (SAXException ex) {
                String message = ex.getMessage();
                if (message!=null && message.startsWith(EXCEPTION_PREFIX))
                    return message.substring(EXCEPTION_PREFIX.length());
                else throw new SAXException(org.openide.util.NbBundle.getMessage(DDProvider.class, "MSG_cannotParse"),ex);
            }
            throw new SAXException(org.openide.util.NbBundle.getMessage(DDProvider.class, "MSG_cannotFindRoot"));
        } catch(javax.xml.parsers.ParserConfigurationException ex) {
            throw new SAXException(org.openide.util.NbBundle.getMessage(DDProvider.class, "MSG_parserProblem"),ex);
        }
    }
    
    private static class VersionHandler extends org.xml.sax.helpers.DefaultHandler {
        public void startElement(String uri, String localName, String rawName, Attributes atts) throws SAXException {
            if ("web-app".equals(rawName)) { //NOI18N
                String version = atts.getValue("version"); //NOI18N
                throw new SAXException(EXCEPTION_PREFIX+(version==null?WebApp.VERSION_2_3:version));
            }
        }
    }
  
    private static class DDResolver implements EntityResolver {
        static DDResolver resolver;
        static synchronized DDResolver getInstance() {
            if (resolver==null) {
                resolver=new DDResolver();
            }
            return resolver;
        }        
        public InputSource resolveEntity (String publicId, String systemId) {
            String resource=null;
            // return a proper input source
            if ("-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN".equals(publicId)) { //NOI18N
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-app_2_3.dtd"; //NOI18N
            } else if ("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN".equals(publicId)) { //NOI18N
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-app_2_2.dtd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("web-app_2_4.xsd")) {
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-app_2_4.xsd"; //NOI18N
            }
            if (resource==null) return null;
            java.net.URL url = this.getClass().getResource(resource);
            return new InputSource(url.toString());
        }
    }
    
    private static class ErrorHandler implements org.xml.sax.ErrorHandler {
        private int errorType=-1;
        SAXParseException error;

        public void warning(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            if (errorType<0) {
                errorType=0;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }
        public void error(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            if (errorType<1) {
                errorType=1;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }        
        public void fatalError(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            errorType=2;
            throw sAXParseException;
        }
        
        public int getErrorType() {
            return errorType;
        }
        public SAXParseException getError() {
            return error;
        }        
    }
    
    public static SAXParseException parse (InputSource is) 
            throws org.xml.sax.SAXException, java.io.IOException {
        ErrorHandler errorHandler = new ErrorHandler();
        try {
            XMLReader reader = new org.apache.xerces.parsers.SAXParser();
            reader.setErrorHandler(errorHandler);
            reader.setEntityResolver(DDResolver.getInstance());
            reader.setFeature("http://apache.org/xml/features/validation/schema", true); // NOI18N
            reader.setFeature("http://xml.org/sax/features/validation",  true); // NOI18N
            reader.setFeature("http://xml.org/sax/features/namespaces",  true); // NOI18N
            reader.parse(is);
            SAXParseException error = errorHandler.getError();
            if (error!=null) return error;
        } catch (SAXException ex) {
            throw ex;
        }
        return null;
    }
}

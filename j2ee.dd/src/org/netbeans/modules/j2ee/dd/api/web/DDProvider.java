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

package org.netbeans.modules.j2ee.dd.api.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import org.netbeans.modules.j2ee.dd.impl.web.WebAppProxy;
import org.openide.filesystems.*;
import org.xml.sax.*;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.loaders.DataObject;

/**
 * Provides access to Deployment Descriptor root ({@link org.netbeans.modules.j2ee.dd.api.web.WebApp} object)
 *
 * @author  Milan Kuchtiak
 */

public final class DDProvider {
    private static DDProvider ddProvider;
    private Map ddMap;
    private Map dataObjectMap;
    private Map baseBeanMap;
    private Map errorMap;
    private FCA fileChangeListener;

    private static final String EXCEPTION_PREFIX="version:"; //NOI18N
    
    /** Creates a new instance of WebModule */
    private DDProvider() {
        ddMap=new java.util.HashMap(5);
        dataObjectMap = new java.util.HashMap(5);
        baseBeanMap=new java.util.HashMap(5);
        errorMap=new java.util.HashMap(5);
        fileChangeListener = new FCA ();
    }
    
    /**
    * Accessor method for DDProvider singleton
    * @return DDProvider object
    */
    public static synchronized DDProvider getDefault() {
        if (ddProvider==null) ddProvider = new DDProvider();
        return ddProvider;
    }
    

    private DataObject getDataObject(FileObject fileObject) {
        return (DataObject) dataObjectMap.get(fileObject);
    }

    public synchronized WebApp getDDRoot(DataObject dataObject) {
        final FileObject primaryFile = dataObject.getPrimaryFile();
        WebAppProxy webAppProxy = null;
        try {
            webAppProxy = getFromCache(primaryFile);
        } catch (IOException e) {
            webAppProxy = null;
        }
        if (webAppProxy == null) {
            webAppProxy = new WebAppProxy(null, null);
        }
        dataObjectMap.put(primaryFile, dataObject);
        return webAppProxy;
    }

    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clints planning to read only the deployment descriptor
     * or to listen to the changes.
     * @param fo FileObject representing the web.xml file
     * @return WebApp object - root of the deployment descriptor bean graph
     */
    public WebApp getDDRoot(FileObject fo) throws java.io.IOException {

        WebAppProxy webApp = getFromCache (fo);
        if (webApp!=null) return webApp;
        

        fo.addFileChangeListener(fileChangeListener);
        
        String version=null;
        SAXParseException error = null;
        try {
            WebApp original = getOriginalFromCache (fo);
            if (original == null) {
                version = getVersion(fo.getInputStream());
                // preparsing
                error = parse(fo);
                original = createWebApp(fo.getInputStream(), version);
                baseBeanMap.put(fo.getURL(), new WeakReference (original));
            } else {
                version = original.getVersion ();
                error = (SAXParseException) errorMap.get (fo.getURL ());
            }
            webApp=new WebAppProxy(original,version);
            if (error!=null) {
                webApp.setStatus(WebApp.STATE_INVALID_PARSABLE);
                webApp.setError(error);
            }
        } catch (SAXException ex) {
            webApp = new WebAppProxy(null,version);
            webApp.setStatus(WebApp.STATE_INVALID_UNPARSABLE);
            if (ex instanceof SAXParseException) {
                webApp.setError((SAXParseException)ex);
            } else if ( ex.getException() instanceof SAXParseException) {
                webApp.setError((SAXParseException)ex.getException());
            }
        }
        ddMap.put(fo.getURL(), new WeakReference (webApp));
        return webApp;
    }

    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clients planning to modify the deployment descriptor.
     * Finally the {@link org.netbeans.modules.j2ee.dd.api.web.WebApp#write(org.openide.filesystems.FileObject)} should be used
     * for writing the changes.
     * @param fo FileObject representing the web.xml file
     * @return WebApp object - root of the deployment descriptor bean graph
     */
    public WebApp getDDRootCopy(FileObject fo) throws java.io.IOException {
        return (WebApp)getDDRoot(fo).clone();
    }

    private WebAppProxy getFromCache (FileObject fo) throws java.io.IOException {
        WeakReference wr = (WeakReference) ddMap.get(fo.getURL ());
        if (wr == null) {
            return null;
        }
        WebAppProxy webApp = (WebAppProxy) wr.get ();
        if (webApp == null) {
            ddMap.remove (fo.getURL ());
        }
        return webApp;
    }
    
    private WebApp getOriginalFromCache (FileObject fo) throws java.io.IOException {
        WeakReference wr = (WeakReference) baseBeanMap.get(fo.getURL ());
        if (wr == null) {
            return null;
        }
        WebApp webApp = (WebApp) wr.get ();
        if (webApp == null) {
            baseBeanMap.remove (fo.getURL ());
            errorMap.remove (fo.getURL ());
            if (ddMap.get (fo.getURL ()) == null) {
            }
        }
        return webApp;
    }

    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param f File representing the web.xml file
     * @return WebApp object - root of the deployment descriptor bean graph
     */    
    public WebApp getDDRoot(File f) throws IOException, SAXException {
        return createWebApp(new FileInputStream(f), getVersion(new FileInputStream(f)));
    }
    
    /**  Convenient method for getting the BaseBean object from CommonDDBean object.
     * The j2eeserver module needs BaseBean to implement jsr88 API.
     * This is a temporary workaround until the implementation of jsr88 moves into ddapi
     * or the implementation in j2eeserver gets changed.
     * @deprecated do not use - temporary workaround that exposes the schema2beans implementation
     */
    public org.netbeans.modules.schema2beans.BaseBean getBaseBean(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean bean) {
        if (bean instanceof org.netbeans.modules.schema2beans.BaseBean) return (org.netbeans.modules.schema2beans.BaseBean)bean;
        else if (bean instanceof WebAppProxy) return (org.netbeans.modules.schema2beans.BaseBean) ((WebAppProxy)bean).getOriginal();
        return null;
    }

    private static WebApp createWebApp(java.io.InputStream is, String version) throws java.io.IOException, SAXException {
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
    private static String getVersion(java.io.InputStream is) throws java.io.IOException, SAXException {
        javax.xml.parsers.SAXParserFactory fact = javax.xml.parsers.SAXParserFactory.newInstance();
        fact.setValidating(false);
        try {
            javax.xml.parsers.SAXParser parser = fact.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setContentHandler(new VersionHandler());
            reader.setEntityResolver(DDResolver.getInstance());
            try {
                reader.parse(new InputSource(is));
            } catch (SAXException ex) {
                is.close();
                String message = ex.getMessage();
                if (message!=null && message.startsWith(EXCEPTION_PREFIX))
                    return message.substring(EXCEPTION_PREFIX.length());
                else throw new SAXException(NbBundle.getMessage(DDProvider.class, "MSG_cannotParse"),ex);
            }
            is.close();
            throw new SAXException(NbBundle.getMessage(DDProvider.class, "MSG_cannotFindRoot"));
        } catch(javax.xml.parsers.ParserConfigurationException ex) {
            throw new SAXException(NbBundle.getMessage(DDProvider.class, "MSG_parserProblem"),ex);
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
            } else if ("http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd".equals(systemId)) {
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
    
    public SAXParseException parse (FileObject fo) 
            throws org.xml.sax.SAXException, java.io.IOException {
        DDProvider.ErrorHandler errorHandler = new DDProvider.ErrorHandler();
        try {
            XMLReader reader = new org.apache.xerces.parsers.SAXParser();
            reader.setErrorHandler(errorHandler);
            reader.setEntityResolver(DDProvider.DDResolver.getInstance());
            reader.setFeature("http://apache.org/xml/features/validation/schema", true); // NOI18N
            reader.setFeature("http://xml.org/sax/features/validation",  true); // NOI18N
            reader.setFeature("http://xml.org/sax/features/namespaces",  true); // NOI18N
            reader.parse(new InputSource(fo.getInputStream()));
            SAXParseException error = errorHandler.getError();
            if (error!=null) return error;
        } catch (SAXException ex) {
            throw ex;
        }
        return null;
    }

    private class FCA extends FileChangeAdapter {
            public void fileChanged(FileEvent evt) {
                FileObject fo=evt.getFile();
                if (getDataObject(fo) != null) {
                    return;
                }
                try {
                    WebAppProxy webApp = getFromCache (fo);
                    WebApp orig = getOriginalFromCache (fo);
                    if (webApp!=null) {
                        String version = null;
                        try {
                            version = getVersion(fo.getInputStream());
                            // preparsing
                            SAXParseException error = parse(fo);
                            if (error!=null) {
                                webApp.setError(error);
                                webApp.setStatus(WebApp.STATE_INVALID_PARSABLE);
                            } else {
                                webApp.setError(null);
                                webApp.setStatus(WebApp.STATE_VALID);
                            }
                            WebApp original = createWebApp(fo.getInputStream(), version);
                            baseBeanMap.put(fo.getURL(), new WeakReference (original));
                            errorMap.put(fo.getURL(), webApp.getError ());
                            // replacing original file in proxy WebApp
                            if (!version.equals(webApp.getVersion())) {
                                webApp.setOriginal(original);
                            } else {// the same version
                                // replacing original file in proxy WebApp
                                if (webApp.getOriginal()==null) {
                                    webApp.setOriginal(original);
                                } else {
                                    webApp.getOriginal().merge(original,WebApp.MERGE_UPDATE);
                                }
                            }
                        } catch (SAXException ex) {
                            if (ex instanceof SAXParseException) {
                                webApp.setError((SAXParseException)ex);
                            } else if ( ex.getException() instanceof SAXParseException) {
                                webApp.setError((SAXParseException)ex.getException());
                            }
                            webApp.setStatus(WebApp.STATE_INVALID_UNPARSABLE);
                            webApp.setOriginal(null);
                            webApp.setProxyVersion(version);
                        }
                    } else if (orig != null) {
                        String version = null;
                        try {
                            version = getVersion(fo.getInputStream());
                            WebApp original = createWebApp(fo.getInputStream(), version);
                            if (original.getClass().equals (orig.getClass())) {
                                orig.merge(original,WebApp.MERGE_UPDATE);
                            } else {
                                baseBeanMap.put(fo.getURL(), new WeakReference (original));
                            }
                        } catch (SAXException ex) {
                            baseBeanMap.remove(fo.getURL());
                        }
                    }
                } catch (java.io.IOException ex){}
            }
        }
}
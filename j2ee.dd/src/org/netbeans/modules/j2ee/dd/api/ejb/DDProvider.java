/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.api.ejb;

import org.netbeans.modules.j2ee.dd.impl.ejb.EjbJarProxy;
import org.netbeans.modules.j2ee.dd.impl.common.DDProviderDataObject;
import org.netbeans.modules.j2ee.dd.impl.common.DDUtils;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Common;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.ErrorManager;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.apache.xerces.parsers.DOMParser;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;

/**
 * Provides access to Deployment Descriptor root ({@link org.netbeans.modules.j2ee.dd.api.ejb.EjbJar} object)
 *
 * @author  Milan Kuchtiak
 */

public final class DDProvider {
    private static final String EJB_20_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN"; //NOI18N
    private static final String EJB_11_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN"; //NOI18N
    private static final DDProvider ddProvider = new DDProvider();
    private Map ddMap;

    /** Creates a new instance of EjbModule */
    private DDProvider() {
        //ddMap=new java.util.WeakHashMap(5);
        ddMap = new HashMap(5);
    }

    /**
    * Accessor method for DDProvider singleton
    * @return DDProvider object
    */
    public static DDProvider getDefault() {
        return ddProvider;
    }

    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clints planning to read only the deployment descriptor
     * or to listen to the changes.
     * @param fo FileObject representing the ejb-jar.xml file
     * @return EjbJar object - root of the deployment descriptor bean graph
     */
    public synchronized EjbJar getDDRoot(FileObject fo) throws java.io.IOException {
        try {
            DataObject dataObject = DataObject.find(fo);
            if(dataObject instanceof DDProviderDataObject){
                return getDDRoot((DDProviderDataObject) dataObject);
            }
        } catch (DataObjectNotFoundException e) {
        }
        EjbJarProxy ejbJarProxy = getFromCache(fo);
        if (ejbJarProxy != null) {
            return ejbJarProxy;
        }

        fo.addFileChangeListener(new DDFileChangeListener());

        ejbJarProxy = DDUtils.createEjbJarProxy(fo.getInputStream());
        putToCache(fo, ejbJarProxy);
        return ejbJarProxy;
    }

    private synchronized EjbJar getDDRoot(final DDProviderDataObject ddProviderDataObject) throws java.io.IOException {
        EjbJarProxy ejbJarProxy = getFromCache(ddProviderDataObject) ;
        if (ejbJarProxy == null) {
            ejbJarProxy = DDUtils.createEjbJarProxy(ddProviderDataObject.createInputStream());
            putToCache(ddProviderDataObject, ejbJarProxy);
        }
        return ejbJarProxy;
    }

    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clients planning to modify the deployment descriptor.
     * Finally the {@link org.netbeans.modules.j2ee.dd.api.ejb.EjbJar#write(org.openide.filesystems.FileObject)} should be used
     * for writing the changes.
     * @param fo FileObject representing the ejb-jar.xml file
     * @return EjbJar object - root of the deployment descriptor bean graph
     */
    public EjbJar getDDRootCopy(FileObject fo) throws java.io.IOException {
        return (EjbJar)getDDRoot(fo).clone();
    }

    private EjbJarProxy getFromCache (Object o) {
        /*       WeakReference wr = (WeakReference) ddMap.get(o);
       if (wr == null) {
           return null;
       }
       EjbJarProxy ejbJarProxy = (EjbJarProxy) wr.get ();
       if (ejbJarProxy == null) {
           ddMap.remove (o);
       }
       return ejbJarProxy;*/
        return (EjbJarProxy) ddMap.get(o);
    }

    private void putToCache(Object o, EjbJarProxy ejbJarProxy) {
        ddMap.put(o, /*new WeakReference*/ (ejbJarProxy));
    }

    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param inputSource source representing the ejb-jar.xml file
     * @return EjbJar object - root of the deployment descriptor bean graph
     */
    public EjbJar getDDRoot(InputSource inputSource) throws IOException, SAXException {
        ErrorHandler errorHandler = new ErrorHandler();
        DOMParser parser = createParser(errorHandler);
        parser.parse(inputSource);
        Document document = parser.getDocument();
        SAXParseException error = errorHandler.getError();
        String version = extractVersion(document);
        EjbJar original = createEjbJar(version, document);
        EjbJarProxy ejbJarProxy = new EjbJarProxy(original, version);
        ejbJarProxy.setError(error);
        if (error != null) {
            ejbJarProxy.setStatus(EjbJar.STATE_INVALID_PARSABLE);
        } else {
            ejbJarProxy.setStatus(EjbJar.STATE_VALID);
        }
        return ejbJarProxy;
    }

    // PENDING j2eeserver needs BaseBean - this is a temporary workaround to avoid dependency of web project on DD impl
    /**  Convenient method for getting the BaseBean object from CommonDDBean object
     *
     */
    public BaseBean getBaseBean(CommonDDBean bean) {
        if (bean instanceof BaseBean) {
            return (BaseBean) bean;
        } else if (bean instanceof EjbJarProxy) {
            return (BaseBean) ((EjbJarProxy) bean).getOriginal();
        }
        return null;
    }

    private static EjbJar createEjbJar(String version, Document document) {
        if (EjbJar.VERSION_2_1.equals(version)) {
            return new org.netbeans.modules.j2ee.dd.impl.ejb.model_2_1.EjbJar(document, Common.USE_DEFAULT_VALUES);
        } else if (EjbJar.VERSION_2_0.equals(version)) {
            return new org.netbeans.modules.j2ee.dd.impl.ejb.model_2_0.EjbJar(document, Common.USE_DEFAULT_VALUES);
        } else {
            return null;
        }
    }

    /**
     * Extracts version of deployment descriptor.
     */
    private static String extractVersion(Document document) {
        // first check the doc type to see if there is one
        DocumentType dt = document.getDoctype();
        // This is the default version
        if (dt != null) {
            if (EJB_20_DOCTYPE.equals(dt.getPublicId())) {
                return EjbJar.VERSION_2_0;
            }
            if (EJB_11_DOCTYPE.equals(dt.getPublicId())){
                return EjbJar.VERSION_1_1;
            }
        }
        return EjbJar.VERSION_2_1;

    }

    private static DOMParser createParser(ErrorHandler errorHandler)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        DOMParser parser = new DOMParser();
        parser.setErrorHandler(errorHandler);
        parser.setEntityResolver(DDResolver.getInstance());
        // XXX do we need validation here, if no one is using this then
        // the dependency on xerces can be removed and JAXP can be used
        parser.setFeature("http://xml.org/sax/features/validation", true); //NOI18N
        parser.setFeature("http://apache.org/xml/features/validation/schema", true); // NOI18N
        parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true); //NOI18N
        return parser;
    }

    private static class DDResolver implements EntityResolver {
        static DDResolver resolver;
        static synchronized DDResolver getInstance() {
            if (resolver==null) {
                resolver=new DDResolver();
            }
            return resolver;
        }

        public InputSource resolveEntity(String publicId, String systemId) {
            // return a proper input source
            String resource;
            if (EJB_11_DOCTYPE.equals(publicId)) {
                resource = "/org/netbeans/modules/j2ee/dd/impl/resources/ejb-jar_1_1.dtd"; //NOI18N
            } else if (EJB_20_DOCTYPE.equals(publicId)) {
                resource = "/org/netbeans/modules/j2ee/dd/impl/resources/ejb-jar_2_0.dtd"; //NOI18N
            } else if ("http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd".equals(systemId)) {
                resource = "/org/netbeans/modules/j2ee/dd/impl/resources/ejb-jar_2_1.xsd"; //NOI18N
            } else {
                return null;
            }
            URL url = this.getClass().getResource(resource);
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

    private class DDFileChangeListener extends FileChangeAdapter {
        public void fileChanged(FileEvent evt) {
            FileObject fo = evt.getFile();
            try {
                EjbJarProxy ejbJarProxy = getFromCache(fo);
                if (ejbJarProxy != null) {
                    DDUtils.merge(ejbJarProxy, fo.getInputStream());
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
}

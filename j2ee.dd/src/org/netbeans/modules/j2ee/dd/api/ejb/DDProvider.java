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

package org.netbeans.modules.j2ee.dd.api.ejb;

import org.netbeans.modules.j2ee.metadata.MergedProvider;
import org.netbeans.modules.j2ee.metadata.MetadataUnit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.j2ee.dd.impl.ejb.EjbJarProxy;
import org.netbeans.modules.j2ee.dd.impl.common.DDProviderDataObject;
import org.netbeans.modules.j2ee.dd.impl.common.DDUtils;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Common;
import org.netbeans.modules.xml.api.EncodingUtil;
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import org.netbeans.modules.j2ee.dd.impl.ejb.EjbAnnotationListener;
import org.netbeans.modules.j2ee.metadata.NNMDRListener;

/**
 * Provides access to Deployment Descriptor root ({@link org.netbeans.modules.j2ee.dd.api.ejb.EjbJar} object)
 *
 * @author  Milan Kuchtiak
 */

public final class DDProvider {
    private static final String EJB_21_DOCTYPE = "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd"; //NOI18N
    private static final String EJB_20_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN"; //NOI18N
    private static final String EJB_11_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN"; //NOI18N
    private static final DDProvider ddProvider = new DDProvider();
    private Map ddMap;
    private Map<MetadataUnit, EjbJar> annotationDDMap;

    /** Creates a new instance of EjbModule */
    private DDProvider() {
        //ddMap=new java.util.WeakHashMap(5);
        ddMap = new HashMap(5);
        annotationDDMap = new WeakHashMap<MetadataUnit, EjbJar>(5);
    }

    /**
    * Accessor method for DDProvider singleton
    * @return DDProvider object
    */
    public static DDProvider getDefault() {
        return ddProvider;
    }

    public EjbJar getMergedDDRoot(MetadataUnit mu) throws IOException {
        if (mu == null) {
            return null;
        }
        EjbJar xmlRoot = getDDRoot(mu.getDeploymentDescriptor());
        //  for J2ee 1.4 and lower delegate to XML-only method
        if (xmlRoot != null && !xmlRoot.getVersion().equals(new BigDecimal(EjbJar.VERSION_3_0))) {
            return xmlRoot;
        }
        EjbJar annotationRoot = getAnnotationDDRoot(mu);
        if (xmlRoot instanceof EjbJarProxy) {
            xmlRoot = ((EjbJarProxy) xmlRoot).getOriginal();
        }
        if (annotationRoot instanceof EjbJarProxy) {
            annotationRoot = ((EjbJarProxy) annotationRoot).getOriginal();
        }
        return (EjbJar) MergedProvider.getDefault().getRoot((BaseBean) annotationRoot, (BaseBean) xmlRoot);
    }

    private synchronized EjbJar getAnnotationDDRoot(MetadataUnit mu) throws IOException {
        if (mu == null) {
            return null;
        }
        EjbJar ejbJar = annotationDDMap.get(mu);
        if (ejbJar != null) {
            return ejbJar;
        }
        ejbJar = new org.netbeans.modules.j2ee.dd.impl.ejb.model_3_0.EjbJar();
        annotationDDMap.put(mu, ejbJar);
        NNMDRListener.getDefault().addAnnotationListener(new EjbAnnotationListener(ejbJar, mu.getClassPath()));
        return ejbJar;
    }

    public List<EjbJar> getRoots() {
        synchronized (this) {
            return new ArrayList(annotationDDMap.values());
        }
    }
    
    public boolean isScanInProgress() {
        return NNMDRListener.getDefault().isScanInProgress();
    }
    
    public void waitScanFinished() {
        NNMDRListener.getDefault().waitScanFinished();
    }

    // used in: ddapi, ddloaders, ejbfreeform
    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clints planning to read only the deployment descriptor
     * or to listen to the changes.
     * @param fo FileObject representing the ejb-jar.xml file
     * @return EjbJar object - root of the deployment descriptor bean graph
     */
    public synchronized EjbJar getDDRoot(FileObject fo) throws java.io.IOException {
        if (fo == null) {
            return null;
        }
        try {
            DataObject dataObject = DataObject.find(fo);
            if(dataObject instanceof DDProviderDataObject){
                return getDDRoot0((DDProviderDataObject) dataObject);
            }
        } catch (DataObjectNotFoundException e) {
            return null; // should not occur
        }
        EjbJarProxy ejbJarProxy = null;
        synchronized (ddMap) {
            ejbJarProxy = getFromCache(fo);
            if (ejbJarProxy != null) {
                return ejbJarProxy;
            }
        }
        
        fo.addFileChangeListener(new DDFileChangeListener());

        ejbJarProxy = DDUtils.createEjbJarProxy(fo.getInputStream());
        putToCache(fo, ejbJarProxy);
        
        return ejbJarProxy;
    }

    private synchronized EjbJar getDDRoot0(final DDProviderDataObject ddProviderDataObject) throws java.io.IOException {
        EjbJarProxy ejbJarProxy = null;
        synchronized (ddMap) {
            ejbJarProxy = getFromCache(ddProviderDataObject) ;
            if (ejbJarProxy == null) {
                ejbJarProxy = DDUtils.createEjbJarProxy(ddProviderDataObject.createReader());
                putToCache(ddProviderDataObject, ejbJarProxy);
            }
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
        DocumentBuilder parser = createParser(errorHandler);
        parser.setEntityResolver(DDResolver.getInstance());
        Document document = parser.parse(inputSource);
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
        if (EjbJar.VERSION_3_0.equals(version)) {
            return new org.netbeans.modules.j2ee.dd.impl.ejb.model_3_0.EjbJar(document, Common.USE_DEFAULT_VALUES);
        } else if (EjbJar.VERSION_2_1.equals(version)) {
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
        String id = null;
        DocumentType dt = document.getDoctype();
        if (dt != null) {
            // it is DTD-based
            id = dt.getPublicId();
        } else {
            // it is XSD-based
            String schemaLocation = document.getDocumentElement().getAttribute("xsi:schemaLocation");
            if (schemaLocation != null) {
                id = schemaLocation.substring(schemaLocation.lastIndexOf(" ") + 1);
            }
        }
        // This is the default version
        if (id != null) {
            if (EJB_21_DOCTYPE.equals(id)) {
                return EjbJar.VERSION_2_1;
            }
            if (EJB_20_DOCTYPE.equals(id)) {
                return EjbJar.VERSION_2_0;
            }
            if (EJB_11_DOCTYPE.equals(id)){
                return EjbJar.VERSION_1_1;
            }
        }
        return EjbJar.VERSION_3_0;

    }

    private static DocumentBuilder createParser(ErrorHandler errorHandler)
            throws SAXException {
        DocumentBuilder parser=null;
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            parser = fact.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new SAXException(ex.getMessage());
        }
        parser.setErrorHandler(errorHandler);
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
            } else if ("http://java.sun.com/xml/ns/javaee/ejb-jar_3_0.xsd".equals(systemId)) {
                resource = "/org/netbeans/modules/j2ee/dd/impl/resources/ejb-jar_3_0.xsd"; //NOI18N
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
                synchronized (ddMap) {
                    EjbJarProxy ejbJarProxy = getFromCache(fo);
                    if (ejbJarProxy != null) {
                        String encoding = EncodingUtil.detectEncoding(new BufferedInputStream(fo.getInputStream()));
                        if (encoding == null) {
                            encoding = "UTF8";
                        }
                        DDUtils.merge(ejbJarProxy, new InputStreamReader(fo.getInputStream(), encoding));
                    }
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
}

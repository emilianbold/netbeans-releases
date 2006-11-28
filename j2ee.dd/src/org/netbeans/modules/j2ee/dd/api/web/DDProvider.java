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

package org.netbeans.modules.j2ee.dd.api.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.ClassPathSupport;
import org.netbeans.modules.j2ee.dd.impl.web.WebAppProxy;
import org.netbeans.modules.j2ee.dd.impl.web.WebParseUtils;
import org.netbeans.modules.j2ee.dd.impl.common.DDUtils;
import org.netbeans.modules.j2ee.metadata.MetadataUnit;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.xml.sax.*;
import java.util.Map;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Provides access to Deployment Descriptor root ({@link org.netbeans.modules.j2ee.dd.api.web.WebApp} object)
 *
 * @author  Milan Kuchtiak
 */

public final class DDProvider {
    private static DDProvider ddProvider;
    private Map ddMap;
    private Map<MetadataUnit, WebApp> annotationDDMap;
    private Map baseBeanMap;
    private Map errorMap;
    private FCA fileChangeListener;
    private Map musMap;
    
    /** Creates a new instance of WebModule */
    private DDProvider() {
        ddMap=new java.util.HashMap(5);
        annotationDDMap = new HashMap<MetadataUnit, WebApp>(5);
        baseBeanMap=new java.util.HashMap(5);
        errorMap=new java.util.HashMap(5);
        musMap=new HashMap(5);
        fileChangeListener = new FCA();
    }
    
    /**
     * Accessor method for DDProvider singleton
     * @return DDProvider object
     */
    public static synchronized DDProvider getDefault() {
        if (ddProvider==null) ddProvider = new DDProvider();
        return ddProvider;
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clints planning to read only the deployment descriptor
     * or to listen to the changes.
     * @param fo FileObject representing the web.xml file
     * @return WebApp object - root of the deployment descriptor bean graph
     */
    public WebApp getMergedDDRoot(FileObject fo) throws IOException {
        if (fo == null) {
            throw new IllegalArgumentException("FileObject is null");  //NOI18N;
        }
        
        WebModule wm = WebModule.getWebModule(fo);
        if(wm != null) {
            //the MetadataUnits are cached; a key is the WM's DD FO
            MetadataUnit mu = (MetadataUnit)musMap.get(wm.getDeploymentDescriptor());
            if(mu == null) {
                mu = new SimpleMetadataUnit(wm.getDeploymentDescriptor(), wm.getJavaSources());
                musMap.put(wm.getDeploymentDescriptor(), mu);
            }
            return getMergedDDRoot(mu);
        } else {
            return getDDRoot(fo);
        }
    }
    
    public WebApp getMergedDDRoot(MetadataUnit mu) throws IOException {
        WebApp xmlRoot = getDDRoot(mu.getDeploymentDescriptor());        
        if (xmlRoot != null) { // && !xmlRoot.getVersion().equals(WebApp.VERSION_2_5)) {
            // TODO find a better resolution for this hack
            return xmlRoot;
        }
        return null;
    }

    public WebApp getDDRoot(FileObject fo) throws java.io.IOException {
        WebAppProxy webApp = null;
        
        synchronized (ddMap) {
            webApp = getFromCache(fo);
            if (webApp!=null) {
                return webApp;
            }
        }
        
        fo.addFileChangeListener(fileChangeListener);
        
        String version = null;
        SAXParseException error = null;
        try {
            WebApp original = null;
            synchronized (baseBeanMap) {
                original = getOriginalFromCache(fo);
                if (original == null) {
                    version = WebParseUtils.getVersion(fo.getInputStream());
                    // preparsing
                    error = parse(fo);
                    original = DDUtils.createWebApp(fo.getInputStream(), version);
                    baseBeanMap.put(fo.getURL(), new WeakReference(original));
                    errorMap.put(fo.getURL(), error);
                } else {
                    version = original.getVersion();
                    error = (SAXParseException) errorMap.get(fo.getURL());
                }
            }
            webApp = new WebAppProxy(original, version);
            if (error != null) {
                webApp.setStatus(WebApp.STATE_INVALID_PARSABLE);
                webApp.setError(error);
            }
        } catch (SAXException ex) {
            webApp = new WebAppProxy(null, version);
            webApp.setStatus(WebApp.STATE_INVALID_UNPARSABLE);
            if (ex instanceof SAXParseException) {
                webApp.setError((SAXParseException) ex);
            } else if (ex.getException() instanceof SAXParseException) {
                webApp.setError((SAXParseException) ex.getException());
            }
        }
        ddMap.put(fo.getURL(), new WeakReference(webApp));
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
    
    private WebAppProxy getFromCache(FileObject fo) throws java.io.IOException {
        if (fo == null) {
            return null;
        }
        WeakReference wr = (WeakReference) ddMap.get(fo.getURL());
        if (wr == null) {
            return null;
        }
        WebAppProxy webApp = (WebAppProxy) wr.get();
        if (webApp == null) {
            ddMap.remove(fo.getURL());
        }
        return webApp;
    }
    
    private WebApp getOriginalFromCache(FileObject fo) throws java.io.IOException {
        WeakReference wr = (WeakReference) baseBeanMap.get(fo.getURL());
        if (wr == null) {
            return null;
        }
        WebApp webApp = (WebApp) wr.get();
        if (webApp == null) {
            baseBeanMap.remove(fo.getURL());
            errorMap.remove(fo.getURL());
            if (ddMap.get(fo.getURL()) == null) {
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
        return DDUtils.createWebApp(new FileInputStream(f), WebParseUtils.getVersion(new FileInputStream(f)));
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
    
    public SAXParseException parse(FileObject fo)
    throws org.xml.sax.SAXException, java.io.IOException {
        return WebParseUtils.parse(fo);
    }
  
    
    /**
     * Removes the entries associated with the given <code>fo</code> from
     * the various caches that this class utilizes.
     * @param fo
     */
    private void removeFromCache(FileObject fo){
        try{
            URL foUrl = fo.getURL();
            ddMap.remove(foUrl);
            baseBeanMap.remove(foUrl);
            errorMap.remove(foUrl);
            musMap.remove(fo);
        } catch (FileStateInvalidException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private class FCA extends FileChangeAdapter {
        public void fileChanged(FileEvent evt) {
            FileObject fo=evt.getFile();
            try {
                if (DataObject.find(fo) != null) {
                    return;
                }
            } catch (DataObjectNotFoundException e) {
            }
            try {
                synchronized (ddMap) {
                    synchronized (baseBeanMap) {
                        WebAppProxy webApp = getFromCache(fo);
                        WebApp orig = getOriginalFromCache(fo);
                        if (webApp!=null) {
                            String version = null;
                            try {
                                version = WebParseUtils.getVersion(fo.getInputStream());
                                // preparsing
                                SAXParseException error = parse(fo);
                                if (error!=null) {
                                    webApp.setError(error);
                                    webApp.setStatus(WebApp.STATE_INVALID_PARSABLE);
                                } else {
                                    webApp.setError(null);
                                    webApp.setStatus(WebApp.STATE_VALID);
                                }
                                WebApp original = DDUtils.createWebApp(fo.getInputStream(), version);
                                baseBeanMap.put(fo.getURL(), new WeakReference(original));
                                errorMap.put(fo.getURL(), webApp.getError());
                                webApp.merge(original, WebApp.MERGE_UPDATE);
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
                                version = WebParseUtils.getVersion(fo.getInputStream());
                                WebApp original = DDUtils.createWebApp(fo.getInputStream(), version);
                                if (original.getClass().equals(orig.getClass())) {
                                    orig.merge(original,WebApp.MERGE_UPDATE);
                                } else {
                                    baseBeanMap.put(fo.getURL(), new WeakReference(original));
                                }
                            } catch (SAXException ex) {
                                baseBeanMap.remove(fo.getURL());
                            }
                        }
                    }
                }
            } catch (java.io.IOException ex){}
        }
        
        public void fileDeleted(FileEvent fe) {
            // need to remove cache entries, see #76431.
            removeFromCache(fe.getFile());
        }
        
        
    }
    
    private class SimpleMetadataUnit implements MetadataUnit {
        
        private FileObject dd;
        private FileObject[] roots;
        
        public SimpleMetadataUnit(FileObject dd, FileObject[] javaSources) {
            this.dd = dd;
            this.roots = javaSources;
        }
        
        public FileObject getDeploymentDescriptor() {
            return dd;
        }
        
        public ClassPath getClassPath() {
            if (roots.length > 0) {
                FileObject fo = roots[0];
                return ClassPathSupport.createWeakProxyClassPath(new ClassPath[] {
                    ClassPath.getClassPath(fo, ClassPath.SOURCE),
                    ClassPath.getClassPath(fo, ClassPath.COMPILE)
                });
            } else {
                return org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(Collections.<PathResourceImplementation>emptyList());
            }
        }
        
        
        
    }
}

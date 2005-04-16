/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jspparser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

// PENDING - need to call reinitOptions when something changes (taglib, jar, web.xml)
// PENDING - separate to two classes, have a per-application instance of one of them

/**
 * @author Petr Jiricka
 */
public class JspParserImpl implements JspParserAPI {
    
    private HashMap parseSupports;
    
    private static Constructor webAppParserImplConstructor;
    
    /** Constructs a new Parser API implementation.
     */
    public JspParserImpl() {
        initializeLogger();
        // PENDING - we are preventing the garbage collection of 
        // Project-s and FileObject-s (wmRoots)
        parseSupports = new HashMap();
    }
    
    private static void initReflection() {
        if (webAppParserImplConstructor == null) {
// ext/jasper-runtime-5.0.19.jar ext/jasper-compiler-5.0.19.jar ext/commons-logging-api.jar ext/commons-el.jar            
            File files[] = new File[6];
            files[0] = InstalledFileLocator.getDefault().locate("ant/lib/ant.jar", null, false);
            files[1] = InstalledFileLocator.getDefault().locate("modules/ext/jasper-runtime-5.5.7.jar", null, false);
            files[2] = InstalledFileLocator.getDefault().locate("modules/ext/jasper-compiler-5.5.7.jar", null, false);
            files[3] = InstalledFileLocator.getDefault().locate("modules/ext/commons-logging-1.0.4.jar", null, false);
            files[4] = InstalledFileLocator.getDefault().locate("modules/ext/commons-el.jar", null, false);
            files[5] = InstalledFileLocator.getDefault().locate("modules/ext/jsp-parser-ext.jar", null, false);

            try {
                URL urls[] = new URL[files.length];
                for (int i = 0; i < files.length; i++) {
                    urls[i] = files[i].toURI().toURL();
                }
                ExtClassLoader urlCL = new ExtClassLoader(urls, JspParserImpl.class.getClassLoader());
                Class cl = urlCL.loadClass("org.netbeans.modules.web.jspparser_ext.WebAppParseSupport");
               
                webAppParserImplConstructor = cl.getDeclaredConstructor(new Class[] {WebModule.class});
            }
            catch (NoSuchMethodException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            catch (ClassNotFoundException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    
    
    
    private static boolean loggerInitialized = false;
    
    private static synchronized void initializeLogger() {
/*        if (!loggerInitialized) {
            Logger l = new DefaultLogger(null);
            l.setDefaultSink(new NullWriter());
            l.setName("JASPER_LOG"); // NOI18N
            //Logger.putLogger();
            loggerInitialized = true;
        }*/
    }
    
    public JspParserAPI.JspOpenInfo getJspOpenInfo(FileObject jspFile, WebModule wm, boolean useEditor) {
        if (wm == null)
            return new JspParserAPI.JspOpenInfo(false, "ISO-8859-1"); // NOI18N
        FileObject wmRoot = wm.getDocumentBase();
        if (wmRoot == null) {
            // PENDING - we could do a better job here in making up a fallback
            return new JspParserAPI.JspOpenInfo(false, "ISO-8859-1"); // NOI18N
        }
        WebAppParseProxy pp = getParseProxy(wm);
        if (pp != null)
            return pp.getJspOpenInfo(jspFile, useEditor);
        else
            return new JspParserAPI.JspOpenInfo(false, "ISO-8859-1"); // NOI18N
    }

    public JspParserAPI.ParseResult analyzePage(FileObject jspFile, WebModule wm, int errorReportingMode) {
        if (wm ==null)
            return getNoWebModuleResult(jspFile, null);
        FileObject wmRoot = wm.getDocumentBase();
        if (wmRoot == null) {
            return getNoWebModuleResult(jspFile, wm);
        }
        WebAppParseProxy pp = getParseProxy(wm);
        if (pp == null)
            return getNoWebModuleResult(jspFile, wm);
        return pp.analyzePage(jspFile, errorReportingMode);
    }
    
    /**
     * Returns the mapping of the 'global' tag library URI to the location (resource
     * path) of the TLD associated with that tag library. 
     * @param wmRoot the web module for which to return the map
     * @return Map which maps global tag library URI to the location 
     * (resource path) of its tld. The location is
     * returned as a String array:
     *    [0] The location
     *    [1] If the location is a jar file, this is the location of the tld.
     */
    public Map getTaglibMap(WebModule wm) throws IOException {
        FileObject wmRoot = wm.getDocumentBase();
        if (wmRoot == null) {
            throw new IOException();
        }
        WebAppParseProxy pp = getParseProxy(wm);
        return pp.getTaglibMap(true);
    }
    
    private synchronized WebAppParseProxy getParseProxy(WebModule wm) {
        JspParserImpl.WAParseSupportKey key = new JspParserImpl.WAParseSupportKey(wm);
        WebAppParseProxy pp = (WebAppParseProxy)parseSupports.get(key);
        if (pp == null) {
            pp = createParseProxy(wm);
            parseSupports.put(key, pp);
        }
        return pp;
    }
    
    private WebAppParseProxy createParseProxy(WebModule wm) {
        // PENDING - do caching for individual JSPs
        try {
            initReflection();
            return (WebAppParseProxy)webAppParserImplConstructor.newInstance(new Object[] {wm});
        }
        catch (IllegalAccessException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            e.printStackTrace();
        }
        catch (InstantiationException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            e.printStackTrace();
        }
        catch (InvocationTargetException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            e.printStackTrace();
        }
        return null;
        //return new WebAppParseSupport(wm);
    }
    
    public URLClassLoader getModuleClassLoader(WebModule wm) {
        WebAppParseProxy pp = getParseProxy(wm);
        return pp.getWAClassLoader();
    }
    
    private JspParserAPI.ParseResult getNoWebModuleResult(FileObject jspFile, WebModule wm) {
        JspParserAPI.ErrorDescriptor error = new JspParserAPI.ErrorDescriptor(null, jspFile, -1, -1, 
            NbBundle.getMessage(JspParserImpl.class, "MSG_webModuleNotFound", jspFile.getNameExt()), ""); // NOI18N
        return new JspParserAPI.ParseResult(new JspParserAPI.ErrorDescriptor[] {error});
    }
    
    
    /*
    public TagLibParseSupport.TagLibData createTagLibData(JspInfo.TagLibraryData info, FileSystem fs) {
        // PENDING - we are not using the cached stuff
        return new TagLibDataImpl(info);
    }
     */


    /** For use from instancedataobject in filesystem layer
     */
    /*public static JspParserImpl getParserImpl(FileObject fo, String str1) {
        return new JspParserImpl();
    }*/
    
    /**  Returns the mapping of 'global' tag library URI to the location
     * (resource path) of the TLD associated with that tag library.
     * The location is returned as a String array:
     *    [0] The location
     *    [1] If the location is a jar file, this is the location
     *        of the tld.
     */
/*    public Map getTagLibraryMappings(FileObject webModuleRoot) throws IOException {
        OptionsImpl options = new OptionsImpl(webModuleRoot);
        TldLocationsCache cache = options.getTldLocationsCache();
        TreeMap result = new TreeMap();
        try {
            Field ff = TldLocationsCache.class.getDeclaredField("mappings"); // NOI18N
            ff.setAccessible(true);
            Hashtable mappings = (Hashtable)ff.get(cache);
            for (Iterator it = mappings.keySet().iterator(); it.hasNext(); ) {
                Object elem = it.next();
                result.put(elem, mappings.get(elem));
            }
        }
        catch (NoSuchFieldException e) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
        }
        catch (IllegalAccessException e) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
        }
        return result;
    }*/
    
    
    private static class WAParseSupportKey {
        
        WebModule wm;
        FileObject wmRoot;
        
        WAParseSupportKey(WebModule wm) {
            this.wm = wm;
            this.wmRoot = wm.getDocumentBase();
        }
        
        public boolean equals(Object o) {

            if (o instanceof WAParseSupportKey) {
                WAParseSupportKey k = (WAParseSupportKey)o;
                //Two keys are the same only if the document roots are same.
                return wmRoot.getPath().equals(k.wmRoot.getPath());
            }
            return false;
        }
        
        public int hashCode() {
            //return wm.hashCode() + wmRoot.hashCode();
            // Two keys are the same only if the document roots are same. 
            return 0;
        }
    }
    
    private static class ExtClassLoader extends URLClassLoader {
        
        private static final AllPermission ALL_PERM = new AllPermission();
        
        public ExtClassLoader(URL[] classLoadingURLs, ClassLoader parent) {
            super(classLoadingURLs, parent);
        }
        
        protected PermissionCollection getPermissions(CodeSource codesource) {
            PermissionCollection perms = super.getPermissions(codesource);
            perms.add(ALL_PERM);
            return perms;
        }
    }
}

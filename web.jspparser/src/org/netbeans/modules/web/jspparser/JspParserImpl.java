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

package org.netbeans.modules.web.jspparser;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Iterator;
import java.lang.reflect.*;
import java.net.URLClassLoader;
import javax.servlet.ServletContext;

import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.compiler.ExtractPageData; // my private class
import org.apache.jasper.compiler.JspRuntimeContext;

import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;

import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Jiricka
 * @version 
 */

// PENDING - need to call reinitOptions when something changes (taglib, jar, web.xml)
// PENDING - separate to two classes, have a per-application instance of one of them

public class JspParserImpl implements JspParserAPI {
    
    private HashMap parseSupports;
    
    /** Constructs a new Parser API implementation.
     */
    public JspParserImpl() {
        initializeLogger();
        // PENDING - we are preventing the garbage collection of 
        // Project-s and FileObject-s (wmRoots)
        parseSupports = new HashMap();
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
    
    public synchronized JspParserAPI.JspOpenInfo getJspOpenInfo(FileObject jspFile, WebModule wm) {
        FileObject wmRoot = wm.getDocumentBase();
        if (wmRoot == null) {
            // PENDING - we could do a better job here in making up a fallback
            return new JspParserAPI.JspOpenInfo(false, "8859_1"); // NOI18N
        }
        WebAppParseSupport ps = getParseSupport(wm);
        return ps.getJspOpenInfo(jspFile);
    }

    public synchronized JspParserAPI.ParseResult analyzePage(FileObject jspFile, WebModule wm, int errorReportingMode) {
        FileObject wmRoot = wm.getDocumentBase();
        if (wmRoot == null) {
            return getNoWebModuleResult(jspFile, wm);
        }
        WebAppParseSupport ps = getParseSupport(wm);
        return ps.analyzePage(jspFile, errorReportingMode);
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
    public synchronized Map getTaglibMap(WebModule wm) throws IOException {
        FileObject wmRoot = wm.getDocumentBase();
        if (wmRoot == null) {
            throw new IOException();
        }
        WebAppParseSupport ps = getParseSupport(wm);
        return ps.getTaglibMap(true);
    }
    
    private synchronized WebAppParseSupport getParseSupport(WebModule wm) {
        JspParserImpl.WAParseSupportKey key = new JspParserImpl.WAParseSupportKey(wm);
        WebAppParseSupport ps = (WebAppParseSupport)parseSupports.get(key);
        if (ps == null) {
            ps = new WebAppParseSupport(wm);
            parseSupports.put(key, ps);
        }
        return ps;
    }
    
    public URLClassLoader getModuleClassLoader(WebModule wm) {
        WebAppParseSupport ps = getParseSupport(wm);
        return ps.getWAClassLoader();
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
                return (wm.equals(k.wm) &&
                        (wmRoot.equals(k.wmRoot)));
            }
            return false;
        }
        
        public int hashCode() {
            return wm.hashCode() + wmRoot.hashCode();
        }
    }
    
}

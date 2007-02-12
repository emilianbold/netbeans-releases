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
 *
 */
package org.netbeans.installer.downloader.services;

import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.xml.DomExternalizable;
import org.netbeans.installer.utils.xml.DomUtil;
import org.netbeans.installer.utils.xml.visitors.DomVisitor;
import org.netbeans.installer.utils.xml.visitors.RecursiveDomVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.StringUtils;

/**
 * @author Danila_Dugurov
 */
public class PersistentCache {
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private File stateFile;
    
    private final Map<URL, File> url2File = new HashMap<URL, File>();
    
    public PersistentCache() {
        stateFile = new File(DownloadManager.instance.getWd(), "cacheState.xml");
        if (!stateFile.exists()) {
            LogManager.log("cache file not exist so treat it as cache is Empty");
        } else load();
    }
    
    public boolean isIn(URL url) {
        return url2File.containsKey(url) && url2File.get(url).exists();
    }
    
    public File getByURL(URL url) {
        if (!isIn(url)) return null;
        return url2File.get(url);
    }
    
    public void put(URL key, File file) {
        url2File.put(key, file);
        dump();
    }
    
    public void clear() {
        for (File file : url2File.values()) {
            file.delete();
        }
    }
    
    public URL[] keys() {
        return (URL[]) url2File.keySet().toArray();
    }
    
    public boolean delete(URL url) {
        if (isIn(url)) return url2File.get(url).delete();
        return false;
    }
    
    private void load() {
        try {
            Document state = DomUtil.parseXmlFile(stateFile);
            final DomVisitor visitor = new RecursiveDomVisitor() {
                public void visit(Element element) {
                    final String name = element.getTagName();
                    if ("cacheEntry".equals(name)) {
                        final CacheEntry entry = new CacheEntry();
                        entry.readXML(element);
                        if (entry.file.exists() && entry.file.isFile())
                            url2File.put(entry.url, entry.file);
                    } else
                        super.visit(element);
                }
            };
            visitor.visit(state);
        } catch (ParseException ex) {
            LogManager.log(ex);
        } catch (IOException ex) {
            LogManager.log(ex);
        }
    }
    
    public synchronized void dump() {
        try {
            final Document document = DomUtil.parseXmlFile("<cache/>");
            final Element root = document.getDocumentElement();
            for (Map.Entry<URL, File> entry : url2File.entrySet()) {
                final CacheEntry cacheEntry = new CacheEntry(entry);
                DomUtil.addChild(root, cacheEntry);
            }
            DomUtil.writeXmlFile(document, stateFile);
        } catch (ParseException wontHappend) {
            LogManager.log("unparsable xml", wontHappend);
        } catch (IOException ex) {
            LogManager.log("i/o during loading persistentCache" , ex);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private static class CacheEntry implements DomExternalizable {
        
        private URL url;
        private File file;
        
        public CacheEntry() {//before read xml
        }
        
        public CacheEntry(Map.Entry<URL, File> entry) {
            this.url = entry.getKey();
            this.file = entry.getValue();
        }
        
        public void readXML(Element element) {
            final DomVisitor visitor = new RecursiveDomVisitor() {
                public void visit(Element element) {
                    final String name = element.getTagName();
                    if ("file".equals(name)) {
                        file = new File(element.getTextContent());
                    } else if ("url".equals(name)) {
                        try {
                            url = StringUtils.parseUrl(element.getTextContent());
                        } catch (ParseException e) {
                            ErrorManager.notifyDebug("Could not parse URL", e);
                        }
                    } else {
                        super.visit(element);
                    }
                }
            };
            visitor.visit(element);
        }
        
        public Element writeXML(Document document) {
            final Element root = document.createElement("cacheEntry");
            DomUtil.addElemet(root, "file", file.getAbsolutePath());
            DomUtil.addElemet(root, "url", url.toString());
            return root;
        }
    }
}
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

package org.netbeans.nbbuild;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Parses nbbuild/templates/modules.xml.
 * @author Jesse Glick
 */
final class ModuleListParser {
    
    /** all module entries, indexed by cnb */
    private final Map/*<String,Entry>*/ entries;
    
    /**
     * Parse a modules.xml file.
     */
    public ModuleListParser(File modulesXml) throws IOException, SAXException {
        Document mDoc = XMLUtil.parse(new InputSource(modulesXml.toURI().toString()),
                                      false, true, /*XXX*/null, null);
        entries = new HashMap();
        List/*<Element>*/ l = XMLUtil.findSubElements(mDoc.getDocumentElement());
        Iterator it = l.iterator();
        while (it.hasNext()) {
            Element el = (Element)it.next();
            Element pathEl = XMLUtil.findElement(el, "path", (String) null);
            String path = XMLUtil.findText(pathEl);
            Element cnbEl = XMLUtil.findElement(el, "cnb", (String) null);
            String cnb = XMLUtil.findText(cnbEl);
            Element jarEl = XMLUtil.findElement(el, "jar", (String) null);
            String jar;
            if (jarEl != null) {
                jar = XMLUtil.findText(jarEl);
            } else {
                jar = null;
            }
            entries.put(cnb, new Entry(path, cnb, jar));
        }
    }
    
    /**
     * Find one entry by code name base.
     * @param cnb the desired code name base
     * @return the matching entry or null
     */
    public Entry findByCodeNameBase(String cnb) {
        return (Entry)entries.get(cnb);
    }
    
    /**
     * One entry in the file.
     */
    public static final class Entry {
        
        private final String path;
        private final String cnb;
        private final String jar;
        
        Entry(String path, String cnb, String jar) {
            this.path = path;
            this.cnb = cnb;
            this.jar = jar;
        }
        
        /**
         * Get the path in nb_all, e.g. ant/grammar.
         */
        public String getPath() {
            return path;
        }
        
        /**
         * Get the code name base, e.g. org.netbeans.modules.ant.grammar.
         */
        public String getCnb() {
            return cnb;
        }
        
        /**
         * Get the JAR file path, e.g. modules/org-netbeans-modules-ant-grammar.jar.
         */
        public String getJar() {
            if (jar != null) {
                return jar;
            } else {
                // Default uses just cnb.
                return "modules/" + cnb.replace('.', '-') + ".jar";
            }
        }
        
    }

}

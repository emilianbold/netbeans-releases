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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** Task that scans Bundle.properties files for unused keys.
 *
 * @author Radim Kubacki
 */
public class CheckBundles extends Task {
    
    private static HashSet knownKeys;    
    
    private static String [] moduleKeys = new String [] {
        "OpenIDE-Module-Name",
        "OpenIDE-Module-Display-Category",
        "OpenIDE-Module-Long-Description",
        "OpenIDE-Module-Short-Description",
        "OpenIDE-Module-Package-Dependency-Message"
    };
    
    private File srcdir;

    public void setSrcdir(File f) {
        // Note: f will automatically be absolute (resolved from project basedir).
        if (!f.isDirectory())
            throw new IllegalArgumentException ("srcdir must be a directory");
        
        srcdir = f;
    }

    public void execute() throws BuildException {
        log("Scanning "+srcdir.getAbsolutePath(), Project.MSG_VERBOSE);

        Map knownNames = parseManifest(srcdir);

        Collection bundles = new ArrayList();
        Map sources = new TreeMap();


        try {        
            File dir = new File (srcdir, "src");
            if (dir.exists())
                scanSubdirs(dir, bundles, sources);
            dir = new File (srcdir, "libsrc");
            if (dir.exists())
                scanSubdirs(dir, bundles, sources);
            // XXX there are still a lot of unsplit bundles in e.g. core/src/
            // referred to from other submodules
            // XXX this technique does not work so well, though - should only scan
            // them for source files, not bundles...
            /*
            File[] subdirs = srcdir.listFiles();
            if (subdirs != null) {
                for (int i = 0; i < subdirs.length; i++) {
                    if (subdirs[i].isDirectory()) {
                        dir = new File(subdirs[i], "src");
                        if (dir.exists()) {
                            scanSubdirs(dir, bundles, sources);
                        }
                    }
                }
            }
             */
            check (bundles, sources, knownNames);
        }
        catch (Exception e) {
            throw new BuildException (e);
        }
    }
    
    private void scan (File file, Collection bundles, Map sources) throws Exception {
        File bundle = new File (file, "Bundle.properties");
        if (!bundle.exists()) {
            log("No bundle in "+file.getAbsolutePath()+". OK", Project.MSG_VERBOSE);
        }
        else {
            bundles.add (bundle);
        }

        addSources (file, sources);
    }

    private void check (Collection bundles, Map files, Map knownNames) {
        try {
            Iterator bIt = bundles.iterator();
            while (bIt.hasNext ()) {
                File bundle = (File)bIt.next();
                Iterator it = entries(bundle).entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    String key = (String) entry.getKey();
                    int line = ((Integer) entry.getValue()).intValue();
                    log("Looking for "+key, Project.MSG_DEBUG);
                    boolean found = false;
                    // module info or file name from layer
                    if (bundle.equals (knownNames.get(key))) {
                        log("Checked name "+key+" OK", Project.MSG_VERBOSE);
                        found = true;
                    }
                    else {
                        // java source in the same package
                        Object o = files.get (bundle.getParentFile());
                        log(" in "+o, Project.MSG_DEBUG);
                        String [] srcs = (String [])o;
                        for (int i=0; i<srcs.length; i++) {
                            if (srcs[i].indexOf("\""+key+"\"")>=0) {
                                log("Checking "+key+" OK", Project.MSG_VERBOSE);
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        // try other java sources
                        // XXX should skip moduleKeys, etc.
                        Iterator fIt = files.keySet().iterator();
                        while (fIt.hasNext()) {
                            File dir = (File)fIt.next();
                            String [] srcs = (String [])files.get (dir);
                            for (int i=0; i<srcs.length; i++) {
                                if (srcs[i].indexOf("\""+key+"\"")>=0) {
                                    log(bundle.getPath() + ":" + line + ": " + key + " used from " + dir.getPath(), Project.MSG_WARN);
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            log(bundle.getPath() + ":" + line + ": " + key + " NOT FOUND");
                        }
                    }
                }
            
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void scanSubdirs (File file, Collection bundles, Map srcs) throws Exception {
        log("scanSubdirs "+file, Project.MSG_DEBUG);
        File [] subdirs = file.listFiles(new FilenameFilter () {
                public boolean accept (File f, String name) {
                    return new File(f, name).isDirectory();
                }
            });
        for (int i = 0; i<subdirs.length; i++) {
            scan (subdirs[i], bundles, srcs);
            scanSubdirs (subdirs[i], bundles, srcs);
        }

    }
    
    /** Adds dir -> array of source texts */
    private void addSources (File dir, Map map) throws Exception {
        File [] files = dir.listFiles(new FilenameFilter () {
                public boolean accept(File dir, String name) {
                    if (name.endsWith(".java")) {
                        return true;
                    }
                    return false;
                }
            });
        String [] srcs = new String[files.length];
        for (int i=0; i<files.length; i++) {
            InputStream is = new BufferedInputStream (new FileInputStream(files[i]));
            byte [] arr = new byte [2048];
            srcs[i] = "";
            int len;
            while ((len = is.read(arr)) != -1) {
                srcs[i] = srcs[i]+ new String(arr, 0, len);
            }
        }
        map.put(dir, srcs);
        return;
    }
    
    /**
     * Get a list of keys in a bundle file, with accompanying line numbers.
     */
    private Map/*<String,int>*/ entries(File bundle) throws IOException {
        Map/*<String,int>*/ entries = new LinkedHashMap();
        BufferedReader r = new BufferedReader (new FileReader (bundle));
        String l;
        boolean multi = false;
        int line = 0;
        while ((l = r.readLine()) != null) {
            line++;
            if (!l.startsWith("#")) {
            
                int i = l.indexOf('=');
                if (i>0 && !multi) {
                    String key = l.substring(0,i).trim();
                    entries.put(key, new Integer(line));
                }
                if (l.endsWith("\\"))
                    multi = true;
                else
                    multi = false;
            }
        }
        return entries;
    }

    private Map parseManifest(File dir) {
        HashMap files = new HashMap(10);
        try {
            File mf = new File(srcdir, "manifest.mf");
            if (!mf.exists()) {
                log("Manifest file not found", Project.MSG_VERBOSE);
                return files;
            }
            
            log("Found manifest", Project.MSG_VERBOSE);
            
            Manifest m = new Manifest(new FileInputStream(mf));
            Attributes attr = m.getMainAttributes();

            // Try to find bundle
            String lb = (attr == null)? null: (String)attr.getValue("OpenIDE-Module-Localizing-Bundle");
            if (lb != null) {
                File lbundle = new File(srcdir.getAbsolutePath()+File.separator+"src"+File.separatorChar+lb);
                log("Recognized localizing bundle "+lbundle, Project.MSG_VERBOSE);
                for (int i=0; i<moduleKeys.length; i++) {
                    files.put(moduleKeys[i], lbundle);
                }
            }

            // Try to find XML layer
            String xml = (attr == null)? null: (String)attr.getValue("OpenIDE-Module-Layer");
            File xmlFile = null;
            if (xml != null) {
                xmlFile = new File (srcdir.getAbsolutePath()+File.separator+"src"+File.separator+xml);
            }
            if (xmlFile != null && xmlFile.exists()) {
                SAXParserFactory f = SAXParserFactory.newInstance();
                f.setValidating(false);
                SAXParser p = f.newSAXParser();
                XMLReader reader = p.getXMLReader();
                reader.setEntityResolver(new EntityResolver () {
                        public InputSource resolveEntity (String publicId, String systemId)
                        {
                            log ("resolveEntity "+publicId+", "+systemId, Project.MSG_DEBUG);
                            // if ("-//NetBeans//DTD Filesystem 1.0//EN".equals (publicId)
                            // ||  "-//NetBeans//DTD Filesystem 1.1//EN".equals (publicId)) 
                            return new InputSource (new ByteArrayInputStream(new byte[0]));
                        }
                    });
                reader.setContentHandler (new SAXHandler(files));
                reader.parse(new InputSource (xmlFile.toURL().toExternalForm()) );
            }
        }
        catch (Exception e) {
            throw new BuildException(e);
        }
        
        return files;
    }
    
    private class SAXHandler extends DefaultHandler {

        private String path;

        private Map map;
        
        /** Creates a new instance of XmlTest */
        public SAXHandler(Map map) {
            this.map = map;
        }

        public void startDocument() throws SAXException {
            super.startDocument();
            path = "";
        }

        public void endElement(String uri, String lname, String name) throws SAXException {
            super.endElement(uri, lname, name);
            if ("folder".equals(name) || "file".equals(name)) {
                int i = path.lastIndexOf('/');
                path = (i>0)? path.substring(0, i): "";
            }
        }

        public void startElement(String uri, String lname, String name, org.xml.sax.Attributes attributes) throws SAXException {
            super.startElement(uri, lname, name, attributes);
            // log("Handling  "+uri+", "+lname+", "+name+", "+attributes, Project.MSG_DEBUG);
            if ("folder".equals(name) || "file".equals(name)) {
                String f = attributes.getValue("name");
                if (name != null) {
                    path += (path.length()==0)? f: "/"+f;
                }
            }
            else if ("attr".equals(name)) {
                String a = attributes.getValue("name");
                if ("SystemFileSystem.localizingBundle".equals(a)) {
                    String val = attributes.getValue("stringvalue");
                    String lfilename = srcdir.getAbsolutePath()+File.separator+"src"+File.separator+val.replace('.',File.separatorChar)+".properties";
                    File lfile = new File(lfilename);
                    log("Recognized file "+path+" with name localized in "+lfile, Project.MSG_VERBOSE);
                    for (int i=0; i<moduleKeys.length; i++) {
                        map.put(path, lfile);
                    }
                }
            }
        }

    }
}


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

// See #13931.

package org.netbeans.nbbuild;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.IndexItem;
import javax.help.TOCItem;
import javax.help.TreeItem;
import javax.help.TreeItemFactory;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Mapper;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Task to check various aspects of JavaHelp helpsets.
 * <ol>
 * <li>General parsability as far as JavaHelp is concerned.
 * <li>Map IDs are not duplicated.
 * <li>Map IDs point to real HTML files (and anchors where specified).
 * <li>TOC/Index navigators refer to real map IDs.
 * <li>HTML links in reachable HTML files point to valid places (including anchors).
 * </ol>
 * @author Jesse Glick, Marek Slama
 */
public class CheckHelpSetsBin extends Task {
    
    private List<FileSet> filesets = new ArrayList<FileSet>();
    
    private ClassLoader globalClassLoader;
    
    private Map<String, ClassLoader> classLoaderMap;
    
    private Set<String> excludedModulesSet;
    
    /** Add a fileset with one or more helpsets in it.
     * <strong>Only</strong> the <samp>*.hs</samp> should match!
     * All other files will be found from it.
     */
    public void addFileset(FileSet fs) {
        filesets.add(fs);
    }
    
    private URLClassLoader createGlobalClassLoader (File dir, String [] files) {
        List<File> globalFileList = new ArrayList<File>();
        URL [] globalClassPath = null;
        for (int i = 0; i < files.length; i++) {
            List<File> fileList = new ArrayList<File>();
            File moduleJar = new File(dir, files[i]);
            fileList.add(moduleJar);
            boolean hsFound = false;

            JarFile jar = null;
            Manifest manifest = null;
            try {
                jar = new JarFile(moduleJar);
                manifest = jar.getManifest();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (manifest == null) {
                log("Manifest is not present in jar. Skipping.", Project.MSG_WARN);
                continue;
            }
            File parent = moduleJar.getParentFile();
            java.util.jar.Attributes attrs = manifest.getMainAttributes();

            String value = attrs.getValue("OpenIDE-Module");
            if (value == null) {
                log("Attribute OpenIDE-Module is not present in manifest. Skipping.", Project.MSG_WARN);
                continue;
            }
            //Look for *.hs
            for (Enumeration en = jar.entries(); en.hasMoreElements(); ) {
                JarEntry je = (JarEntry) en.nextElement();
                if (je.getName().endsWith(".hs")) {
                    hsFound = true;
                }
            }
            value = attrs.getValue("Class-Path");
            if (value != null) {
                StringTokenizer tok = new StringTokenizer(value);
                while (tok.hasMoreElements()) {
                    String s = tok.nextToken();
                    File extJar = new File(parent, s);
                    fileList.add(extJar);
                    try {
                        jar = new JarFile(extJar);
                    } catch (IOException ex) {
                        log("Error: Cannot open file: " + extJar, Project.MSG_WARN);
                        ex.printStackTrace();
                    }
                    //Look for *.hs
                    for (Enumeration en = jar.entries(); en.hasMoreElements(); ) {
                        JarEntry je = (JarEntry) en.nextElement();
                        if (je.getName().endsWith(".hs")) {
                            hsFound = true;
                        }
                    }
                }
            }
            if (hsFound) {
                globalFileList.addAll(fileList);
            }
        }
        globalClassPath = new URL[globalFileList.size()];
        for (int i = 0; i < globalFileList.size(); i++) {
            try {
                globalClassPath[i] = globalFileList.get(i).toURI().toURL();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return new URLClassLoader(globalClassPath,this.getClass().getClassLoader().getParent(),new CheckHelpSetsBin.NbDocsStreamHandler.Factory());
    }

    private Map<String, ClassLoader> createClassLoaderMap (File dir, String [] files) {
        Map<String, ClassLoader> m = new HashMap<String, ClassLoader>();
        for (int i = 0; i < files.length; i++) {
            List<File> fileList = new ArrayList<File>();
            File moduleJar = new File(dir, files[i]);
            fileList.add(moduleJar);
            boolean hsFound = false;
            URL [] classPath = null;

            JarFile jar = null;
            Manifest manifest = null;
            try {
                jar = new JarFile(moduleJar);
                manifest = jar.getManifest();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (manifest == null) {
                log("Manifest is not present in jar. Skipping.", Project.MSG_WARN);
                continue;
            }
            File parent = moduleJar.getParentFile();
            java.util.jar.Attributes attrs = manifest.getMainAttributes();
            String value = attrs.getValue("Class-Path");
            if (value != null) {
                StringTokenizer tok = new StringTokenizer(value);
                while (tok.hasMoreElements()) {
                    String s = tok.nextToken();
                    File extJar = new File(parent, s);
                    fileList.add(extJar);
                }
                classPath = new URL[fileList.size()];
                for (int j = 0; j < fileList.size(); j++) {
                    try {
                        classPath[j] = fileList.get(j).toURI().toURL();
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            String key = attrs.getValue("OpenIDE-Module");
            if (key == null) {
                log("Attribute OpenIDE-Module is not present in manifest. Skipping.", Project.MSG_WARN);
                continue;
            }
            int pos = key.indexOf("/");
            if (pos != -1) {
                key = key.substring(0,pos);
            }
            //Look for *.hs
            for (Enumeration en = jar.entries(); en.hasMoreElements(); ) {
                JarEntry je = (JarEntry) en.nextElement();
                if (je.getName().endsWith(".hs")) {
                    hsFound = true;
                }
            }
            value = attrs.getValue("Class-Path");
            if (value != null) {
                StringTokenizer tok = new StringTokenizer(value);
                while (tok.hasMoreElements()) {
                    String s = tok.nextToken();
                    File extJar = new File(parent, s);
                    try {
                        jar = new JarFile(extJar);
                    } catch (IOException ex) {
                        log("Error: Cannot open file: " + extJar, Project.MSG_WARN);
                        ex.printStackTrace();
                    }
                    //Look for *.hs
                    for (Enumeration en = jar.entries(); en.hasMoreElements(); ) {
                        JarEntry je = (JarEntry) en.nextElement();
                        if (je.getName().endsWith(".hs")) {
                            hsFound = true;
                        }
                    }
                }
            }
            if (hsFound) {
                ClassLoader clParent = this.getClass().getClassLoader().getParent();
                URLClassLoader moduleClassLoader = new URLClassLoader(classPath,clParent,new CheckHelpSetsBin.NbDocsStreamHandler.Factory());
                m.put(key,moduleClassLoader);
            }
        }
        return m;
    }
    
    private Set<String> parseExcludeModulesProperty (String prop) {
        excludedModulesSet = new HashSet<String>();
        if (prop == null) {
            return excludedModulesSet;
        }
        String [] arr = prop.split(",");
        for (int i = 0; i < arr.length; i++) {
            excludedModulesSet.add(arr[i]);
        }
        return excludedModulesSet;
    }
    
    public void execute() throws BuildException {
        try {
            URL.setURLStreamHandlerFactory(new CheckHelpSetsBin.NbDocsStreamHandler.Factory());
        } catch (Error ex) {
            log("StreamHandlerFactory already set", Project.MSG_WARN);
        }
        String p = getProject().getProperty("javahelpbin.exclude.modules");
        excludedModulesSet = parseExcludeModulesProperty(p);
        Iterator it = filesets.iterator();
        while (it.hasNext()) {
            FileSet fs = (FileSet)it.next();
            FileScanner scanner = fs.getDirectoryScanner(getProject());
            File dir = scanner.getBasedir();
            String[] files = scanner.getIncludedFiles();
            
            globalClassLoader = createGlobalClassLoader(dir,files);
            classLoaderMap = createClassLoaderMap(dir,files);
            
            for (int i = 0; i < files.length; i++) {
                List<File> fileList = new ArrayList<File>();
                File moduleJar = new File(dir, files[i]);
                fileList.add(moduleJar);
                URL [] classPath = null;
                
                JarFile jar = null;
                Manifest manifest = null;
                try {
                    jar = new JarFile(moduleJar);
                    manifest = jar.getManifest();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (manifest == null) {
                    log("Manifest is not present in jar. Skipping.", Project.MSG_WARN);
                    continue;
                }
                File parent = moduleJar.getParentFile();
                java.util.jar.Attributes attrs = manifest.getMainAttributes();
                        
                String key = attrs.getValue("OpenIDE-Module");
                if (key == null) {
                    log("Attribute OpenIDE-Module is not present in manifest. Skipping.", Project.MSG_WARN);
                    continue;
                }
                int pos = key.indexOf("/");
                if (pos != -1) {
                    key = key.substring(0,pos);
                }
                if (excludedModulesSet.contains(key)) {
                    log("", Project.MSG_WARN);
                    log("* * * *", Project.MSG_WARN);
                    log("Skip module: " + key, Project.MSG_WARN);
                    log("* * * *", Project.MSG_WARN);
                    continue;
                }
                URLClassLoader classLoader = (URLClassLoader) classLoaderMap.get(key);
                if (classLoader == null) {
                    //If module class loader was not added to map it does not contain
                    //any helpset => skip it.
                    continue;
                }
                log("", Project.MSG_WARN);
                log("* * * *", Project.MSG_WARN);
                log("Parsing module: " + key, Project.MSG_WARN);
                log("* * * *", Project.MSG_WARN);
                //Look for *.hs
                for (Enumeration en = jar.entries(); en.hasMoreElements(); ) {
                    JarEntry je = (JarEntry) en.nextElement();
                    if (je.getName().endsWith(".hs")) {
                        URLClassLoader moduleClassLoader = (URLClassLoader) classLoaderMap.get(key);
                        URL hsURL = moduleClassLoader.findResource(je.getName());
                        checkHelpSetURL(hsURL,globalClassLoader,moduleClassLoader,classLoaderMap,moduleJar);
                    }
                }
                String value = attrs.getValue("Class-Path");
                if (value != null) {
                    StringTokenizer tok = new StringTokenizer(value);
                    while (tok.hasMoreElements()) {
                        String s = tok.nextToken();
                        File extJar = new File(parent, s);
                        try {
                            jar = new JarFile(extJar);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        //Look for *.hs
                        for (Enumeration en = jar.entries(); en.hasMoreElements(); ) {
                            JarEntry je = (JarEntry) en.nextElement();
                            if (je.getName().endsWith(".hs")) {
                                ClassLoader moduleClassLoader = classLoaderMap.get(key);
                                URL hsURL = moduleClassLoader.getResource(je.getName());
                                checkHelpSetURL(hsURL,globalClassLoader,moduleClassLoader,classLoaderMap,extJar);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void checkHelpSetURL
    (URL hsURL, ClassLoader globalClassLoader, ClassLoader moduleClassLoader, java.util.Map classLoaderMap, File extJar) {
        HelpSet hs = null;
        try {
            hs = new HelpSet(moduleClassLoader, hsURL);
        } catch (HelpSetException ex) {
            ex.printStackTrace();
        }
        javax.help.Map map = hs.getCombinedMap();
        Enumeration e = map.getAllIDs();
        Set<URI> okurls = new HashSet<URI>(1000);
        Set<URI> badurls = new HashSet<URI>(1000);
        Set<URI> cleanurls = new HashSet<URI>(1000);
        while (e.hasMoreElements()) {
            javax.help.Map.ID id = (javax.help.Map.ID)e.nextElement();
            URL u = null;
            try {
                u = id.getURL();
            } catch (MalformedURLException ex) {
                log("id:" + id, Project.MSG_WARN);
                ex.printStackTrace();
            }
            if (u == null) {
                throw new BuildException("Bogus map ID: " + id.id, new Location(extJar.getAbsolutePath()));
            }
            log("Checking ID " + id.id, Project.MSG_VERBOSE);
            try {
                //System.out.println("CALL OF CheckLinks.scan");
                List<String> errors = new ArrayList<String>();
                CheckLinks.scan(this, globalClassLoader, classLoaderMap, id.id, "",
                new URI(u.toExternalForm()), okurls, badurls, cleanurls, false, false, false, 2, 
                Collections.<Mapper>emptyList(), errors);
                for (String error : errors) {
                    log(error, Project.MSG_WARN);
                }
                //System.out.println("RETURN OF CheckLinks.scan");
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /* Unused:
    private void checkHelpSet(File hsfile) throws Exception {
        log("Checking helpset: " + hsfile);
        HelpSet hs = new HelpSet(null, hsfile.toURI().toURL());
        javax.help.Map map = hs.getCombinedMap();
        log("Parsed helpset, checking map IDs in TOC/Index navigators...");
        NavigatorView[] navs = hs.getNavigatorViews();
        for (int i = 0; i < navs.length; i++) {
            String name = navs[i].getName();
            File navfile = new File(hsfile.getParentFile(), (String)navs[i].getParameters().get("data"));
            if (! navfile.exists()) throw new BuildException("Navigator " + name + " not found", new Location(navfile.getAbsolutePath()));
            if (navs[i] instanceof IndexView) {
                log("Checking index navigator " + name, Project.MSG_VERBOSE);
                IndexView.parse(navfile.toURI().toURL(), hs, Locale.getDefault(), new VerifyTIFactory(hs, map, navfile, false));
            } else if (navs[i] instanceof TOCView) {
                log("Checking TOC navigator " + name, Project.MSG_VERBOSE);
                TOCView.parse(navfile.toURI().toURL(), hs, Locale.getDefault(), new VerifyTIFactory(hs, map, navfile, true));
            } else {
                log("Skipping non-TOC/Index view: " + name, Project.MSG_VERBOSE);
            }
        }
        log("Checking for duplicate map IDs...");
        HelpSet.parse(hsfile.toURI().toURL(), null, new VerifyHSFactory());
        log("Checking links from help map and between HTML files...");
        Enumeration e = map.getAllIDs();
        Set<URI> okurls = new HashSet<URI>(1000);
        Set<URI> badurls = new HashSet<URI>(1000);
        Set<URI> cleanurls = new HashSet<URI>(1000);
        while (e.hasMoreElements()) {
            javax.help.Map.ID id = (javax.help.Map.ID)e.nextElement();
            URL u = map.getURLFromID(id);
            if (u == null) {
                throw new BuildException("Bogus map ID: " + id.id, new Location(hsfile.getAbsolutePath()));
            }
            log("Checking ID " + id.id, Project.MSG_VERBOSE);
            try {
                CheckLinks.scan(this, null, null, id.id, "", 
                new URI(u.toExternalForm()), okurls, badurls, cleanurls, false, false, false, 2,
                Collections.<Mapper>emptyList());
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    */
    
    private final class VerifyTIFactory implements TreeItemFactory {
        
        private final HelpSet hs;
        private final javax.help.Map map;
        private final File navfile;
        private final boolean toc;
        public VerifyTIFactory(HelpSet hs, javax.help.Map map, File navfile, boolean toc) {
            this.hs = hs;
            this.map = map;
            this.navfile = navfile;
            this.toc = toc;
        }
        
        // The useful method:
        
        public TreeItem createItem(String str, Hashtable hashtable, HelpSet helpSet, Locale locale) {
            String target = (String)hashtable.get("target");
            if (target != null) {
                if (! map.isValidID(target, hs)) {
                    log(navfile + ": invalid map ID: " + target, Project.MSG_WARN);
                } else {
                    log("OK map ID: " + target, Project.MSG_VERBOSE);
                }
            }
            return createItem();
        }
        
        // Filler methods:
        
        public java.util.Enumeration listMessages() {
            return Collections.enumeration(Collections.<String>emptyList());
        }
        
        public void processPI(HelpSet helpSet, String str, String str2) {
        }
        
        public void reportMessage(String str, boolean param) {
            log(str, param ? Project.MSG_VERBOSE : Project.MSG_WARN);
        }
        
        public void processDOCTYPE(String str, String str1, String str2) {
        }
        
        public void parsingStarted(URL uRL) {
        }
        
        public DefaultMutableTreeNode parsingEnded(DefaultMutableTreeNode defaultMutableTreeNode) {
            return defaultMutableTreeNode;
        }
        
        public TreeItem createItem() {
            if (toc) {
                return new TOCItem();
            } else {
                return new IndexItem();
            }
        }
        
    }
    
    private final class VerifyHSFactory extends HelpSet.DefaultHelpSetFactory {
        
        private Set<String> ids = new HashSet<String>(1000);
        
        public void processMapRef(HelpSet hs, Hashtable attrs) {
            try {
                URL map = new URL(hs.getHelpSetURL(), (String)attrs.get("location"));
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setValidating(false);
                factory.setNamespaceAware(false);
                SAXParser parser = factory.newSAXParser();
                parser.parse(new InputSource(map.toExternalForm()), new Handler(map.getFile()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        private final class Handler extends DefaultHandler {
            
            private final String map;
            public Handler(String map) {
                this.map = map;
            }
            
            public void startElement(String uri, String lname, String name, Attributes attributes) throws SAXException {
                if (name.equals("mapID")) {
                    String target = attributes.getValue("target");
                    if (target != null) {
                        if (ids.add(target)) {
                            log("Found map ID: " + target, Project.MSG_DEBUG);
                        } else {
                            log(map + ": duplicated ID: " + target, Project.MSG_WARN);
                        }
                    }
                }
            }
            
            public InputSource resolveEntity(String pub, String sys) throws SAXException {
                if (pub.equals("-//Sun Microsystems Inc.//DTD JavaHelp Map Version 1.0//EN") ||
                        pub.equals("-//Sun Microsystems Inc.//DTD JavaHelp Map Version 2.0//EN")) {
                    // Ignore.
                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                } else {
                    return null;
                }
            }
            
        }
        
    }
    
    public static class NbDocsStreamHandler extends URLStreamHandler {

        public static class Factory implements URLStreamHandlerFactory {

            public URLStreamHandler createURLStreamHandler(String protocol) {
                if (protocol.equals("nbdocs")) { // NOI18N
                    return new CheckHelpSetsBin.NbDocsStreamHandler();
                } else {
                    return null;
                }
            }
        }

        /** Make a URLConnection for nbdocs: URLs.
         * @param u the URL
         * @throws IOException if the wrong protocol
         * @return the connection
         */
        protected URLConnection openConnection(URL u) throws IOException {
            if (u.getProtocol().equals("nbdocs")) { // NOI18N
                return new NbDocsURLConnection(u);
            } else {
                throw new IOException();
            }
        }

        /** A URL connection that reads from the docs classloader.
         */
        private static class NbDocsURLConnection extends URLConnection {

            /** underlying URL connection
             */
            private URLConnection real = null;

            /** any associated exception while handling
             */
            private IOException exception = null;

            /** Make the connection.
             * @param u URL to connect to
             */
            public NbDocsURLConnection(URL u) {
                super(u);
            }

            /** Connect to the URL.
             * Actually look up and open the underlying connection.
             * @throws IOException for the usual reasons
             */
            public synchronized void connect() throws IOException {
                if (exception != null) {
                    IOException e = exception;
                    exception = null;
                    throw e;
                }
                if (! connected) {
                    real.connect();
                    connected = true;
                }
            }

            /** Maybe connect, if not keep track of the problem.
             */
            private void tryToConnect() {
                if (connected || exception != null) return;
                try {
                    connect();
                } catch (IOException ioe) {
                    exception = ioe;
                }
            }

            /** Get a URL header.
             * @param n index of the header
             * @return the header value
             */
            public String getHeaderField(int n) {
                tryToConnect();
                if (connected)
                    return real.getHeaderField(n);
                else
                    return null;
            }

            /** Get the name of a header.
             * @param n the index
             * @return the header name
             */
            public String getHeaderFieldKey(int n) {
                tryToConnect();
                if (connected)
                    return real.getHeaderFieldKey(n);
                else
                    return null;
            }

            /** Get a header by name.
             * @param key the header name
             * @return the value
             */
            public String getHeaderField(String key) {
                tryToConnect();
                if (connected)
                    return real.getHeaderField(key);
                else
                    return null;
            }

            /** Get an input stream on the connection.
             * @throws IOException for the usual reasons
             * @return a stream to the object
             */
            public InputStream getInputStream() throws IOException {
                connect();
                return real.getInputStream();
            }

            /** Get an output stream on the object.
             * @throws IOException for the usual reasons
             * @return an output stream writing to it
             */
            public OutputStream getOutputStream() throws IOException {
                connect();
                return real.getOutputStream();
            }

            /** Get the type of the content.
             * @return the MIME type
             */
            public String getContentType() {
                tryToConnect();
                if (connected)
                    return real.getContentType();
                else
                    return "application/octet-stream"; // NOI18N
            }

            /** Get the length of content.
             * @return the length in bytes
             */
            public int getContentLength() {
                tryToConnect();
                if (connected)
                    return real.getContentLength();
                else
                    return 0;
            }

        }

    }
        
}

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

// See #13931.

package org.netbeans.nbbuild;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.FileSet;

import javax.help.*;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/** Task to check various aspects of JavaHelp helpsets.
 * <ol>
 * <li>General parsability as far as JavaHelp is concerned.
 * <li>Map IDs are not duplicated.
 * <li>Map IDs point to real HTML files (and anchors where specified).
 * <li>TOC/Index navigators refer to real map IDs.
 * <li>HTML links in reachable HTML files point to valid places (including anchors).
 * </ol>
 * @author Jesse Glick
 */
public class CheckHelpSets extends Task {
    
    private List filesets = new ArrayList(); // List<FileSet>
    
    /** Add a fileset with one or more helpsets in it.
     * <strong>Only</strong> the <samp>*.hs</samp> should match!
     * All other files will be found from it.
     */
    public void addFileset(FileSet fs) {
        filesets.add(fs);
    }
    
    public void execute() throws BuildException {
        Iterator it = filesets.iterator();
        while (it.hasNext()) {
            FileSet fs = (FileSet)it.next();
            FileScanner scanner = fs.getDirectoryScanner(project);
            File dir = scanner.getBasedir();
            String[] files = scanner.getIncludedFiles();
            for (int i = 0; i < files.length; i++) {
                File helpset = new File(dir, files[i]);
                try {
                    checkHelpSet(helpset);
                } catch (BuildException be) {
                    throw be;
                } catch (Exception e) {
                    throw new BuildException("Error checking helpset", e, new Location(helpset.getAbsolutePath()));
                }
            }
        }
    }
    
    private void checkHelpSet(File hsfile) throws Exception {
        log("Checking helpset: " + hsfile);
        HelpSet hs = new HelpSet(null, hsfile.toURL());
        javax.help.Map map = hs.getCombinedMap();
        log("Parsed helpset, checking map IDs in TOC/Index navigators...");
        NavigatorView[] navs = hs.getNavigatorViews();
        for (int i = 0; i < navs.length; i++) {
            String name = navs[i].getName();
            File navfile = new File(hsfile.getParentFile(), (String)navs[i].getParameters().get("data"));
            if (! navfile.exists()) throw new BuildException("Navigator " + name + " not found", new Location(navfile.getAbsolutePath()));
            if (navs[i] instanceof IndexView) {
                log("Checking index navigator " + name, Project.MSG_VERBOSE);
                IndexView.parse(navfile.toURL(), hs, Locale.getDefault(), new VerifyTIFactory(hs, map, navfile, false));
            } else if (navs[i] instanceof TOCView) {
                log("Checking TOC navigator " + name, Project.MSG_VERBOSE);
                TOCView.parse(navfile.toURL(), hs, Locale.getDefault(), new VerifyTIFactory(hs, map, navfile, true));
            } else {
                log("Skipping non-TOC/Index view: " + name, Project.MSG_VERBOSE);
            }
        }
        log("Checking for duplicate map IDs...");
        HelpSet.parse(hsfile.toURL(), null, new VerifyHSFactory());
        log("Checking links from help map and between HTML files...");
        Enumeration e = map.getAllIDs();
        Set okurls = new HashSet(1000); // Set<URL>
        Set badurls = new HashSet(1000); // Set<URL>
        Set cleanurls = new HashSet(1000); // Set<URL>
        while (e.hasMoreElements()) {
            javax.help.Map.ID id = (javax.help.Map.ID)e.nextElement();
            URL u = map.getURLFromID(id);
            if (u == null) {
                throw new BuildException("Bogus map ID: " + id.id, new Location(hsfile.getAbsolutePath()));
            }
            log("Checking ID " + id.id, Project.MSG_VERBOSE);
            CheckLinks.scan(this, id.id, u, okurls, badurls, cleanurls, false, 2);
        }
    }
    
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
            return Collections.enumeration(Collections.EMPTY_LIST);
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
        
        private Set ids = new HashSet(1000); // Set<String>
        
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
        
        // Using SAX 1 because Ant does not always ship with SAX 2:
        private final class Handler extends HandlerBase {
            
            private final String map;
            public Handler(String map) {
                this.map = map;
            }
            
            public void startElement(String name, AttributeList attr) {
                if (name.equals("mapID")) {
                    String target = attr.getValue("target");
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
                if (pub.equals("-//Sun Microsystems Inc.//DTD JavaHelp Map Version 1.0//EN")) {
                    // Ignore.
                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                } else {
                    return super.resolveEntity(pub, sys);
                }
            }
            
        }
        
    }
    
}

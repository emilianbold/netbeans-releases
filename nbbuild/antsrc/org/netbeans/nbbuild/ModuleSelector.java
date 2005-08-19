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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Selector that accepts modules based on their code base name.
 *
 * @author Jaroslav Tulach
 */
public final class ModuleSelector extends org.apache.tools.ant.types.selectors.BaseExtendSelector {
    private HashSet excludeModules;
    private HashSet excludeClusters;
    private HashMap/*<String,String*/ fileToOwningModule;
    
    /** Creates a new instance of ModuleSelector */
    public ModuleSelector() {
    }

    public boolean isSelected(File dir, String filename, File file) throws BuildException {
        validate();
        
        if (file.isDirectory()) {
            log("Skipping directory: " + file, Project.MSG_VERBOSE);
            return false;
        }
        
        String module = null;
        if (file.getName().endsWith(".jar")) {
            try {
                JarFile jar = new JarFile(file);
                Manifest m = jar.getManifest();
                if (m == null) {
                    module = null;
                } else {
                    module = m.getMainAttributes().getValue("OpenIDE-Module");
                }
                jar.close();
            } catch (IOException ex) {
                throw new BuildException("Problem with file: " + file, ex);
            }
        }

        String name = file.getName();
        File p = file.getParentFile();
        for(;;) {
            if (excludeClusters.contains(p.getName())) {
                log("Excluded cluster: " + p.getName() + " for " + file, Project.MSG_VERBOSE);
                return false;
            }
            
            if (module == null && fileToOwningModule != null) {
                module = (String)fileToOwningModule.get(name);
            }
            
            if (dir.equals(p)) {
                break;
            }
            name = p.getName() + '/' + name;
            p = p.getParentFile();
        }
        
        if (module == null) {
            log("No module in: " + file, Project.MSG_VERBOSE);
            return false;
        }
        int slash = module.indexOf('/');
        if (slash >= 0) {
            module = module.substring(0, slash);
        }
        
        if (excludeModules.contains(module)) {
            log("Excluded module: " + file, Project.MSG_VERBOSE);
            return false;
        }

        log("Accepted file: " + file, Project.MSG_VERBOSE);
        return true;
    }

    public void verifySettings() {
        if (excludeClusters != null) {
            return;
        }
        
        excludeClusters = new HashSet();
        excludeModules = new HashSet();
        
        Parameter[] arr = getParameters();
        if (arr == null) {
            return;
        }
        
        for (int i = 0; i < arr.length; i++) {
            if ("excludeModules".equals(arr[i].getName())) {
                parse(arr[i].getValue(), excludeModules);
                log("Will excludeModules: " + excludeModules, Project.MSG_VERBOSE);
                continue;
            }
            if ("excludeClusters".equals(arr[i].getName())) {
                parse(arr[i].getValue(), excludeClusters);
                log("Will excludeClusters: " + excludeClusters, Project.MSG_VERBOSE);
                continue;
            }
            if ("updateTrackingFiles".equals(arr[i].getName())) {
                fileToOwningModule = new HashMap();
                try {
                    readUpdateTracking(arr[i].getValue(), fileToOwningModule);
                } catch (IOException ex) {
                    throw new BuildException(ex);
                } catch (ParserConfigurationException ex) {
                    throw new BuildException(ex);
                } catch (SAXException ex) {
                    throw new BuildException(ex);
                }
                log("Will accept these files: " + fileToOwningModule.keySet(), Project.MSG_VERBOSE);
                continue;
            }
            setError("Unknown parameter: " + arr[i].getName());
        }
    }
    
    private static void parse(String tokens, Set to) {
        StringTokenizer tok = new StringTokenizer(tokens, ", \n");
        
        while(tok.hasMoreElements()) {
            to.add(tok.nextToken());
        }
    }

    private void readUpdateTracking(String tokens, final HashMap files) throws SAXException, IOException, ParserConfigurationException {
        StringTokenizer tok = new StringTokenizer(tokens, File.pathSeparator);
        
        javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setValidating(false);
        final SAXParser parser = factory.newSAXParser();

        class MyHandler extends DefaultHandler {
            public File where;
            public String module;
            
            public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
                if (qName.equals("file")) {
                    String file = attributes.getValue("name");
                    if (file == null) {
                        throw new BuildException("<file/> without name attribute in " + where);
                    }
                    
                    files.put(file, module);
                }
            }
            
            public void iterate(StringTokenizer tok) throws SAXException, IOException {
                while(tok.hasMoreElements()) {
                    where = new File(tok.nextToken());
                    
                    module = where.getName();
                    if (module.endsWith(".xml")) {
                        module = module.substring(0, module.length() - 4);
                    }
                    module = module.replace('-', '.');

                    try {
                        log("Parsing " + where, Project.MSG_VERBOSE);
                        parser.parse(where, this);
                    } catch (SAXException ex) {
                        throw new BuildException("Wrong file " + where, ex);
                    }
                }
            }
        }
        MyHandler handler = new MyHandler();
        handler.iterate (tok);
        
        
    }
}

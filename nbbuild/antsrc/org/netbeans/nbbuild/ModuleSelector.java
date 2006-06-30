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
    private HashSet includeClusters;
    private HashSet excludeClusters;
    private HashMap/*<String,String*/ fileToOwningModule;
    private boolean acceptExcluded;
    
    /** Creates a new instance of ModuleSelector */
    public ModuleSelector() {
    }

    public boolean isSelected(File dir, String filename, File file) throws BuildException {
        validate();
     
        Boolean check = checkSelected(dir, filename, file);
        if (check == null) {
            return false;
        }
        
        if (acceptExcluded) {
            log("Reverting the accepted state", Project.MSG_VERBOSE);
            return !check.booleanValue();
        } else {
            return check.booleanValue();
        }
    }
    
    private Boolean checkSelected(File dir, String filename, File file) throws BuildException {
        if (file.isDirectory()) {
            log("Skipping directory: " + file, Project.MSG_VERBOSE);
            return null;
        }
        
        String module = null;
        if (file.getName().endsWith(".jar")) {
            try {
                JarFile jar = new JarFile(file);
                Manifest m = jar.getManifest();
                if (m != null) {
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

            if (new File(p, "update_tracking").isDirectory()) { // else includeClusters does not work
                String cluster = p.getName();
                
                if (!includeClusters.isEmpty() && !includeClusters.contains(cluster)) {
                    log("Not included cluster: " + cluster + " for " + file, Project.MSG_VERBOSE);
                    return null;
                }

                if (includeClusters.isEmpty() && excludeClusters.contains(cluster)) {
                    log("Excluded cluster: " + cluster + " for " + file, Project.MSG_VERBOSE);
                    return null;
                }
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
            return null;
        }
        int slash = module.indexOf('/');
        if (slash >= 0) {
            module = module.substring(0, slash);
        }
        
        if (excludeModules.contains(module)) {
            log("Excluded module: " + file, Project.MSG_VERBOSE);
            return Boolean.FALSE;
        }

        log("Accepted file: " + file, Project.MSG_VERBOSE);
        return Boolean.TRUE;
    }

    public void verifySettings() {
        if (includeClusters != null) {
            return;
        }
        
        includeClusters = new HashSet();
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
            if ("includeClusters".equals(arr[i].getName())) {
                parse(arr[i].getValue(), includeClusters);
                log("Will includeClusters: " + includeClusters, Project.MSG_VERBOSE);
                continue;
            }
            if ("excludeClusters".equals(arr[i].getName())) {
                parse(arr[i].getValue(), excludeClusters);
                log("Will excludeClusters: " + excludeClusters, Project.MSG_VERBOSE);
                continue;
            }
            if ("excluded".equals(arr[i].getName())) {
                acceptExcluded = Boolean.valueOf(arr[i].getValue()).booleanValue();
                log("Will acceptExcluded: " + acceptExcluded, Project.MSG_VERBOSE);
                continue;
            }
            if ("updateTrackingFiles".equals(arr[i].getName())) {
                fileToOwningModule = new HashMap();
                try {
                    readUpdateTracking(getProject(), arr[i].getValue(), fileToOwningModule);
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

    static void readUpdateTracking(final Project p, String tokens, final HashMap files) throws SAXException, IOException, ParserConfigurationException {
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
                    
                    files.put(file.replace(File.separatorChar, '/'), module);
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
                        if (p != null) {
                            p.log("Parsing " + where, Project.MSG_VERBOSE);
                        }
                        parser.parse(where, this);
                    } catch (SAXException ex) {
                        throw new BuildException("Wrong file " + where, ex);
                    }
                    
                    // the update tracking file belongs to the moduel as well
                    files.put(where.getParentFile().getName() + '/' + where.getName(), module);
                }
            }
        }
        MyHandler handler = new MyHandler();
        handler.iterate (tok);
        
        
    }
}

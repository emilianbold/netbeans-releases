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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Task to sort the list of modules in a suite by their declared build dependencies.
 * @author Jesse Glick
 */
public class SortSuiteModules extends Task {

    private Path unsortedModules;
    /**
     * Set a list of modules in the suite.
     * Each entry should be a project base directory.
     */
    public void setUnsortedModules(Path unsortedModules) {
        this.unsortedModules = unsortedModules;
    }
    
    private String sortedModulesProperty;
    /**
     * Set a property name in which to store a sorted path of module base directories.
     */
    public void setSortedModulesProperty(String sortedModulesProperty) {
        this.sortedModulesProperty = sortedModulesProperty;
    }
    
    public SortSuiteModules() {}
    
    public void execute() throws BuildException {
        if (unsortedModules == null) {
            throw new BuildException("Must set unsortedModules");
        }
        if (sortedModulesProperty == null) {
            throw new BuildException("Must set sortedModulesProperty");
        }
        Map/*<String,File>*/ basedirsByCNB = new TreeMap();
        Map/*<String,List<String>>*/ buildDeps = new HashMap();
        String[] pieces = unsortedModules.list();
        for (int i = 0; i < pieces.length; i++) {
            File d = new File(pieces[i]);
            File projectXml = new File(d, "nbproject" + File.separatorChar + "project.xml");
            if (!projectXml.isFile()) {
                throw new BuildException("Cannot open " + projectXml, getLocation());
            }
            Document doc;
            try {
                doc = XMLUtil.parse(new InputSource(projectXml.toURI().toString()), false, true, null, null);
            } catch (IOException e) {
                throw new BuildException("Error parsing " + projectXml + ": " + e, e, getLocation());
            } catch (SAXException e) {
                throw new BuildException("Error parsing " + projectXml + ": " + e, e, getLocation());
            }
            Element config = XMLUtil.findElement(doc.getDocumentElement(), "configuration", ParseProjectXml.PROJECT_NS);
            if (config == null) {
                throw new BuildException("Malformed project file " + projectXml, getLocation());
            }
            Element data = XMLUtil.findElement(config, "data", ParseProjectXml.NBM_NS);
            if (data == null) {
                throw new BuildException("Malformed project file " + projectXml, getLocation());
            }
            Element cnbEl = XMLUtil.findElement(data, "code-name-base", ParseProjectXml.NBM_NS);
            if (cnbEl == null) {
                throw new BuildException("Malformed project file " + projectXml, getLocation());
            }
            String cnb = XMLUtil.findText(cnbEl);
            basedirsByCNB.put(cnb, d);
            List/*<String>*/ deps = new LinkedList();
            Element depsEl = XMLUtil.findElement(data, "module-dependencies", ParseProjectXml.NBM_NS);
            if (depsEl == null) {
                throw new BuildException("Malformed project file " + projectXml, getLocation());
            }
            Iterator it = XMLUtil.findSubElements(depsEl).iterator();
            while (it.hasNext()) {
                Element dep = (Element) it.next();
                if (XMLUtil.findElement(dep, "build-prerequisite", ParseProjectXml.NBM_NS) == null) {
                    continue;
                }
                Element cnbEl2 = XMLUtil.findElement(dep, "code-name-base", ParseProjectXml.NBM_NS);
                if (cnbEl2 == null) {
                    throw new BuildException("Malformed project file " + projectXml, getLocation());
                }
                String cnb2 = XMLUtil.findText(cnbEl2);
                deps.add(cnb2);
            }
            buildDeps.put(cnb, deps);
        }
        Iterator it = buildDeps.values().iterator();
        while (it.hasNext()) {
            List/*<String>*/ deps = (List) it.next();
            deps.retainAll(basedirsByCNB.keySet());
        }
        // Stolen from org.openide.util.Utilities.topologicalSort, with various simplifications:
        List/*<String>*/ cnbs = new ArrayList();
        List cRev = new ArrayList(basedirsByCNB.keySet());
        Collections.reverse(cRev);
        Map finished = new HashMap();
        it = cRev.iterator();
        while (it.hasNext()) {
            if (!visit((String) it.next(), buildDeps, finished, cnbs)) {
                throw new BuildException("Cycles detected in dependency graph, cannot sort", getLocation());
            }
        }
        StringBuffer path = new StringBuffer();
        it = cnbs.iterator();
        while (it.hasNext()) {
            String cnb = (String) it.next();
            assert basedirsByCNB.containsKey(cnb);
            if (path.length() > 0) {
                path.append(File.pathSeparatorChar);
            }
            path.append(((File) basedirsByCNB.get(cnb)).getAbsolutePath());
        }
        getProject().setNewProperty(sortedModulesProperty, path.toString());
    }
    
    private static /*<String>*/ boolean visit(String node, Map/*<String,List<String>>*/ edges, Map/*<String,Boolean>*/ finished, List/*<String>*/ r) {
        Boolean b = (Boolean) finished.get(node);
        if (b != null) {
            return b.booleanValue();
        }
        List e = (List) edges.get(node);
        if (e != null) {
            finished.put(node, Boolean.FALSE);
            Iterator it = e.iterator();
            while (it.hasNext()) {
                if (!visit((String) it.next(), edges, finished, r)) {
                    return false;
                }
            }
        }
        finished.put(node, Boolean.TRUE);
        r.add(node);
        return true;
    }
    
}

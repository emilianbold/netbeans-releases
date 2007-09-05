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

package org.netbeans.modules.ruby.rubyproject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This was originally org.netbeans.modules.ant.freeform.spi.support.Util
 * Miscellaneous helper methods.
 * @author Jesse Glick, David Konecny
 */
public class Util {
    
    private Util() {}
    
    /** Return true iff the given line seems to be colored using ANSI terminal escape codes */
    public static boolean containsAnsiColors(String line) {
        // RSpec will color output with ANSI color sequence terminal escapes
        return line.indexOf("\033[") != -1; // NOI18N
    }
    
    /**
     * Remove ANSI terminal escape codes from a line.
     */
    public static String stripAnsiColors(String line) {
        StringBuilder sb = new StringBuilder(line.length());
        int index = 0;
        int max = line.length();
        while (index < max) {
            int nextEscape = line.indexOf("\033[", index);
            if (nextEscape == -1) {
                nextEscape = line.length();
            }
            
            for (int n = (nextEscape == -1) ? max : nextEscape; index < n; index++) {
                sb.append(line.charAt(index));
            }

            if (nextEscape != -1) {
                for (; index < max; index++) {
                    char c = line.charAt(index);
                    if (c == 'm') {
                        index++;
                        break;
                    }
                }
            }
        }

        return sb.toString();
    }

    
    // XXX XML methods copied from ant/project... make a general API of these instead?
    
    /**
     * Search for an XML element in the direct children of a parent.
     * DOM provides a similar method but it does a recursive search
     * which we do not want. It also gives a node list and we want
     * only one result.
     * @param parent a parent element
     * @param name the intended local name
     * @param namespace the intended namespace
     * @return the one child element with that name, or null if none or more than one
     */
    public static Element findElement(Element parent, String name, String namespace) {
        Element result = null;
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element)l.item(i);
                if (name.equals(el.getLocalName()) && namespace.equals(el.getNamespaceURI())) {
                    if (result == null) {
                        result = el; // XXX Uhm, why don't we just return it???
                    } else {
                        return null;
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Extract nested text from an element.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent element
     * @return the nested text, or null if none was found
     */
    public static String findText(Element parent) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)l.item(i);
                return text.getNodeValue();
            }
        }
        return null;
    }
    
    /**
     * Find all direct child elements of an element.
     * More useful than {@link Element#getElementsByTagNameNS} because it does
     * not recurse into recursive child elements.
     * Children which are all-whitespace text nodes are ignored; others cause
     * an exception to be thrown.
     * @param parent a parent element in a DOM tree
     * @return a list of direct child elements (may be empty)
     * @throws IllegalArgumentException if there are non-element children besides whitespace
     */
    public static List<Element> findSubElements(Element parent) throws IllegalArgumentException {
        NodeList l = parent.getChildNodes();
        List<Element> elements = new ArrayList<Element>(l.getLength());
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element)n);
            } else if (n.getNodeType() == Node.TEXT_NODE) {
                String text = ((Text)n).getNodeValue();
                if (text.trim().length() > 0) {
                    throw new IllegalArgumentException("non-ws text encountered in " + parent + ": " + text); // NOI18N
                }
            } else if (n.getNodeType() == Node.COMMENT_NODE) {
                // skip
            } else {
                throw new IllegalArgumentException("unexpected non-element child of " + parent + ": " + n); // NOI18N
            }
        }
        return elements;
    }

    /**
     * Finds AuxiliaryConfiguration for the given project helper. The method
     * finds project associated with the helper and searches 
     * AuxiliaryConfiguration in project's lookup.
     *
     * @param helper instance of project's RakeProjectHelper
     * @return project's AuxiliaryConfiguration
     */
    public static AuxiliaryConfiguration getAuxiliaryConfiguration(RakeProjectHelper helper) {
        try {
            Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
            AuxiliaryConfiguration aux = p.getLookup().lookup(AuxiliaryConfiguration.class);
            assert aux != null;
            return aux;
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }

//    /** 
//     * Relativize given file against the original project and if needed use 
//     * ${project.dir} property as base. If file cannot be relativized
//     * the absolute filepath is returned.
//     * @param projectBase original project base folder
//     * @param freeformBase Freeform project base folder
//     * @param location location to relativize
//     * @return text suitable for storage in project.xml representing given location
//     */
//    public static String relativizeLocation(File projectBase, File freeformBase, File location) {
//        if (CollocationQuery.areCollocated(projectBase, location)) {
//            if (projectBase.equals(freeformBase)) {
//                return PropertyUtils.relativizeFile(projectBase, location);
//            } else if (projectBase.equals(location) && ProjectConstants.PROJECT_LOCATION_PREFIX.endsWith("/")) { // NOI18N
//                return ProjectConstants.PROJECT_LOCATION_PREFIX.substring(0, ProjectConstants.PROJECT_LOCATION_PREFIX.length() - 1);
//            } else {
//                return ProjectConstants.PROJECT_LOCATION_PREFIX + PropertyUtils.relativizeFile(projectBase, location);
//            }
//        } else {
//            return location.getAbsolutePath();
//        }
//    }

    /**
     * Resolve given string value (e.g. "${project.dir}/lib/lib1.jar")
     * to a File.
     * @param evaluator evaluator to use for properties resolving
     * @param freeformProjectBase freeform project base folder
     * @param val string to be resolved as file
     * @return resolved File or null if file could not be resolved
     */
    public static File resolveFile(PropertyEvaluator evaluator, File freeformProjectBase, String val) {
        String location = evaluator.evaluate(val);
        if (location == null) {
            return null;
        }
        return PropertyUtils.resolveFile(freeformProjectBase, location);
    }

//    /**
//     * Returns location of original project base folder. The location can be dirrerent
//     * from NetBeans metadata project folder.
//     * @param helper RakeProjectHelper associated with the project
//     * @param evaluator PropertyEvaluator associated with the project
//     * @return location of original project base folder
//     */
//    public static File getProjectLocation(RakeProjectHelper helper, PropertyEvaluator evaluator) {
//        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
//        String loc = evaluator.getProperty(ProjectConstants.PROP_PROJECT_LOCATION);
//        if (loc != null) {
//            return helper.resolveFile(loc);
//        } else {
//            return FileUtil.toFile(helper.getProjectDirectory());
//        }
//    }

    /**
     * Append child element to the correct position according to given
     * order.
     * @param parent parent to which the child will be added
     * @param el element to be added
     * @param order order of the elements which must be followed
     */
    public static void appendChildElement(Element parent, Element el, String[] order) {
        Element insertBefore = null;
        List l = Arrays.asList(order);
        int index = l.indexOf(el.getLocalName());
        assert index != -1 : el.getLocalName()+" was not found in "+l; // NOI18N
        Iterator it = Util.findSubElements(parent).iterator();
        while (it.hasNext()) {
            Element e = (Element)it.next();
            int index2 = l.indexOf(e.getLocalName());
            assert index2 != -1 : e.getLocalName()+" was not found in "+l; // NOI18N
            if (index2 > index) {
                insertBefore = e;
                break;
            }
        }
        parent.insertBefore(el, insertBefore);
    }
    
//    /**Get the "default" (user-specified) ant script for the given freeform project.
//     * Please note that this method may return <code>null</code> if there is no such script.
//     *
//     * WARNING: This method is there only for a limited set of usecases like the profiler plugin.
//     * It should not be used by the freeform project natures.
//     *
//     * @param prj the freeform project
//     * @return the "default" ant script or <code>null</code> if there is no such a script
//     * @throws IllegalArgumentException if the passed project is not a freeform project.
//     */
//    public static FileObject getDefaultAntScript(Project prj) throws IllegalArgumentException {
//        ProjectAccessor accessor = prj.getLookup().lookup(ProjectAccessor.class);
//        
//        if (accessor == null) {
//            throw new IllegalArgumentException("Only FreeformProjects are supported.");
//        }
//        
//        return FreeformProjectGenerator.getAntScript(accessor.getHelper(), accessor.getEvaluator());
//    }
    
}

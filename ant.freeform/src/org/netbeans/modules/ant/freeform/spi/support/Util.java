/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.ant.freeform.spi.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.FreeformProjectType;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

/**
 * Miscellaneous helper methods.
 * @author Jesse Glick, David Konecny
 */
public class Util {
    
    private Util() {}
    
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
                        result = el;
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
     * @param helper instance of project's AntProjectHelper
     * @return project's AuxiliaryConfiguration
     */
    public static AuxiliaryConfiguration getAuxiliaryConfiguration(AntProjectHelper helper) {
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

    /** 
     * Relativize given file against the original project and if needed use 
     * ${project.dir} property as base. If file cannot be relativized
     * the absolute filepath is returned.
     * @param projectBase original project base folder
     * @param freeformBase Freeform project base folder
     * @param location location to relativize
     * @return text suitable for storage in project.xml representing given location
     */
    public static String relativizeLocation(File projectBase, File freeformBase, File location) {
        if (CollocationQuery.areCollocated(projectBase, location)) {
            if (projectBase.equals(freeformBase)) {
                return PropertyUtils.relativizeFile(projectBase, location);
            } else if (projectBase.equals(location) && ProjectConstants.PROJECT_LOCATION_PREFIX.endsWith("/")) { // NOI18N
                return ProjectConstants.PROJECT_LOCATION_PREFIX.substring(0, ProjectConstants.PROJECT_LOCATION_PREFIX.length() - 1);
            } else {
                return ProjectConstants.PROJECT_LOCATION_PREFIX + PropertyUtils.relativizeFile(projectBase, location);
            }
        } else {
            return location.getAbsolutePath();
        }
    }

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

    /**
     * Returns location of original project base folder. The location can be dirrerent
     * from NetBeans metadata project folder.
     * @param helper AntProjectHelper associated with the project
     * @param evaluator PropertyEvaluator associated with the project
     * @return location of original project base folder
     */
    public static File getProjectLocation(AntProjectHelper helper, PropertyEvaluator evaluator) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        String loc = evaluator.getProperty(ProjectConstants.PROP_PROJECT_LOCATION);
        if (loc != null) {
            return helper.resolveFile(loc);
        } else {
            return FileUtil.toFile(helper.getProjectDirectory());
        }
    }

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
    
    /**Get the "default" (user-specified) ant script for the given freeform project.
     * Please note that this method may return <code>null</code> if there is no such script.
     *
     * WARNING: This method is there only for a limited set of usecases like the profiler plugin.
     * It should not be used by the freeform project natures.
     *
     * @param prj the freeform project
     * @return the "default" ant script or <code>null</code> if there is no such a script
     * @throws IllegalArgumentException if the passed project is not a freeform project.
     */
    public static FileObject getDefaultAntScript(Project prj) throws IllegalArgumentException {
        ProjectAccessor accessor = prj.getLookup().lookup(ProjectAccessor.class);
        
        if (accessor == null) {
            throw new IllegalArgumentException("Only FreeformProjects are supported.");
        }
        
        return FreeformProjectGenerator.getAntScript(accessor.getHelper(), accessor.getEvaluator());
    }
    
    /**
     * Convert an XML fragment from one namespace to another.
     */
    private static Element translateXML(Element from, String namespace) {
        Element to = from.getOwnerDocument().createElementNS(namespace, from.getLocalName());
        NodeList nl = from.getChildNodes();
        int length = nl.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nl.item(i);
            Node newNode;
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                newNode = translateXML((Element) node, namespace);
            } else {
                newNode = node.cloneNode(true);
            }
            to.appendChild(newNode);
        }
        NamedNodeMap m = from.getAttributes();
        for (int i = 0; i < m.getLength(); i++) {
            Node attr = m.item(i);
            to.setAttribute(attr.getNodeName(), attr.getNodeValue());
        }
        return to;
    }

    /**
     * Namespace of data used in {@link #getPrimaryConfigurationData} and {@link #putPrimaryConfigurationData}.
     * @since org.netbeans.modules.ant.freeform/1 1.15
     */
    public static final String NAMESPACE = "http://www.netbeans.org/ns/freeform-project/2"; // NOI18N

    /**
     * Replacement for {@link AntProjectHelper#getPrimaryConfigurationData}
     * taking into account the /1 -> /2 upgrade.
     * @param helper a project helper
     * @return data in {@link #NAMESPACE}, converting /1 data if needed
     * @since org.netbeans.modules.ant.freeform/1 1.15
     */
    public static Element getPrimaryConfigurationData(final AntProjectHelper helper) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Element>() {
            public Element run() {
                AuxiliaryConfiguration ac = helper.createAuxiliaryConfiguration();
                Element data = ac.getConfigurationFragment(FreeformProjectType.NAME_SHARED, NAMESPACE, true);
                if (data != null) {
                    return data;
                } else {
                    return translateXML(helper.getPrimaryConfigurationData(true), NAMESPACE);
                }
            }
        });
    }

    /**
     * Replacement for {@link AntProjectHelper#putPrimaryConfigurationData}
     * taking into account the /1 -> /2 upgrade.
     * Always pass the /2 data, which will be converted to /1 where legal.
     * @param helper a project helper
     * @param data data in {@link #NAMESPACE}
     * @throws IllegalArgumentException if the incoming data is not in {@link #NAMESPACE}
     * @since org.netbeans.modules.ant.freeform/1 1.15
     */
    public static void putPrimaryConfigurationData(final AntProjectHelper helper, final Element data) {
        if (!data.getNamespaceURI().equals(FreeformProjectType.NS_GENERAL)) {
            throw new IllegalArgumentException("Bad namespace"); // NOI18N
        }
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                Element dataAs1 = translateXML(data, FreeformProjectType.NS_GENERAL_1);
                if (SCHEMA_1 == null) {
                    try {
                        SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                        SCHEMA_1 = f.newSchema(FreeformProject.class.getResource("resources/freeform-project-general.xsd")); // NOI18N
                        SCHEMA_2 = f.newSchema(FreeformProject.class.getResource("resources/freeform-project-general-2.xsd")); // NOI18N
                    } catch (SAXException e) {
                        Exceptions.printStackTrace(e); // cf. #169994
                        putPrimaryConfigurationDataAs1(helper, dataAs1);
                    }
                }
                try {
                    XMLUtil.validate(dataAs1, SCHEMA_1);
                    putPrimaryConfigurationDataAs1(helper, dataAs1);
                } catch (SAXException x1) {
                    try {
                        XMLUtil.validate(data, SCHEMA_2);
                        putPrimaryConfigurationDataAs2(helper, data);
                    } catch (SAXException x2) {
                        assert false : x2.getMessage() + "; rejected content: " + format(data);
                        putPrimaryConfigurationDataAs1(helper, dataAs1);
                    }
                }
                return null;
            }
        });
    }
    private static Schema SCHEMA_1, SCHEMA_2;
    private static void putPrimaryConfigurationDataAs1(AntProjectHelper helper, Element data) {
        helper.createAuxiliaryConfiguration().removeConfigurationFragment(FreeformProjectType.NAME_SHARED, NAMESPACE, true);
        helper.putPrimaryConfigurationData(data, true);
    }
    private static void putPrimaryConfigurationDataAs2(AntProjectHelper helper, Element data) {
        Document doc = data.getOwnerDocument();
        Element dummy1 = doc.createElementNS(FreeformProjectType.NS_GENERAL_1, FreeformProjectType.NAME_SHARED);
        // Make sure it is not invalid.
        dummy1.appendChild(doc.createElementNS(FreeformProjectType.NS_GENERAL_1, "name")). // NOI18N
                appendChild(doc.createTextNode(findText(findElement(data, "name", NAMESPACE)))); // NOI18N
        helper.putPrimaryConfigurationData(dummy1, true);
        helper.createAuxiliaryConfiguration().putConfigurationFragment(data, true);
    }
    private static String format(Element data) {
        LSSerializer ser = ((DOMImplementationLS) data.getOwnerDocument().getImplementation().getFeature("LS", "3.0")).createLSSerializer();
        try {
            ser.getDomConfig().setParameter("format-pretty-print", true);
            ser.getDomConfig().setParameter("xml-declaration", false);
        } catch (DOMException ignore) {}
        return ser.writeToString(data);
    }

}

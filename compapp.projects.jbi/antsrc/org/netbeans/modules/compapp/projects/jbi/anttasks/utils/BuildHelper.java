/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.compapp.projects.jbi.anttasks.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.JbiProjectType;
import org.netbeans.modules.compapp.projects.jbi.descriptor.XmlUtil;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.projects.jbi.util.EditableProperties;
import org.netbeans.modules.sun.manager.jbi.management.model.ComponentInformationParser;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.w3c.dom.NodeList;
import static org.netbeans.modules.compapp.projects.jbi.JbiConstants.*;

/**
 * A set of utilities used in the CompApp project build task.
 *
 * @author jqian
 */
public class BuildHelper {

    // See org.netbeans.spi.project.support.ant.ReferenceHelper
    public static final String ANT_PROJECT_REFERENCE_NAMESPACE_1 = "http://www.netbeans.org/ns/ant-project-references/1"; // NOI18N
    public static final String ANT_PROJECT_REFERENCE_NAMESPACE_2 = "http://www.netbeans.org/ns/ant-project-references/2"; // NOI18N


    /**
     * Gets the short name (w/o the compapp name prefix) of the JBI Service Unit.
     *
     * @param jbiSU &lt;service-unit&gt; DOM element in the jbi document
     *
     * @return  service unit short name
     */
    public static String getJBIServiceUnitShortName(Element jbiSU) {
        // We can not derive the SU name (w/o the compapp name prefix) from
        // the jar name. We can derive it from the SU identification name.
        // See JbiProjectProperties.generateServiceUnitElement().
        /*
        Element target = (Element) jbiSU.getElementsByTagName(JBI_TARGET_ELEM_NAME).item(0);
        Element artifactsZip = (Element) target.getElementsByTagName(JBI_ARTIFACTS_ZIP_ELEM_NAME).item(0);
        String zipFileName = artifactsZip.getFirstChild().getNodeValue();
        // Java EE application can have extension '.war' and '.ear'
        //assert zipFileName.endsWith(".jar");
        return zipFileName.substring(0, zipFileName.length() - 4);
         */

        Element suID = (Element) jbiSU.getElementsByTagName(JBI_IDENTIFICATION_ELEM_NAME).item(0);
        Element suName = (Element) suID.getElementsByTagName(JBI_NAME_ELEM_NAME).item(0);
        String compAppSuName = suName.getTextContent();

        Element jbiSA = (Element) jbiSU.getParentNode();
        Element saID = (Element) jbiSA.getElementsByTagName(JBI_IDENTIFICATION_ELEM_NAME).item(0);
        Element saName = (Element) saID.getElementsByTagName(JBI_NAME_ELEM_NAME).item(0);
        String compAppName = saName.getTextContent();

        // Strip the compAppName and '-' from compApp_SuName
        return compAppSuName.substring(compAppName.length() + 1);
    }

    /**
     * Gets the binding components' namespace-to-name mapping.
     *
     * @param project   an Ant project
     * 
     * @return a map mapping binding component's namespace to name
     */
    public static Map<String, String> getBCNamespaceToNameMap(Project project) {

        Map<String, String> ret = new HashMap<String, String>();

        String projPath = project.getProperty("basedir") + File.separator;
        String cnfDir = project.getProperty((JbiProjectProperties.META_INF));
        String bcInfo = projPath + cnfDir + File.separator +
                JbiProject.BINDING_COMPONENT_INFO_FILE_NAME;
        File bcFile = new File(bcInfo);
        if (bcFile.exists()) {
            try {
                List<JBIComponentStatus> compList =
                        ComponentInformationParser.parse(bcFile);
                for (JBIComponentStatus comp : compList) {
                    String compName = comp.getName();
                    List<String> nsList = comp.getNamespaces();
                    for (String ns : nsList) {
                        ret.put(ns, compName);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Gets all the binding component names from ComponentInformation.xml.
     *
     * @param ciFileLoc    file location for ComponentInformation.xml
     */
    public static List<String> loadBindingComponentNames(String ciFileLoc) 
            throws Exception {

        List<String> ret = new ArrayList<String>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);

        File ciFile = new File(ciFileLoc);
        if (!ciFile.exists()) {
            throw new FileNotFoundException(ciFileLoc + " is missing.");
        }

        Document document =
                factory.newDocumentBuilder().parse(ciFile);
        NodeList compInfoNodeList = document.getElementsByTagName("component-info");

        for (int i = 0, isize = compInfoNodeList.getLength(); i < isize; i++) {
            Element compInfo = (Element) compInfoNodeList.item(i);
            Element typeElement = (Element) compInfo.getElementsByTagName("type").item(0);
            String compType = typeElement.getFirstChild().getNodeValue();
            if (compType.equalsIgnoreCase("binding")) {
                Element nameElement = (Element) compInfo.getElementsByTagName("name").item(0);
                String compName = nameElement.getFirstChild().getNodeValue();
                ret.add(compName);
            }
        }

        return ret;
    }

    @SuppressWarnings("deprecation")
    public static String getServiceAssemblyID(Project p) {
        String saID = p.getProperty(JbiProjectProperties.SERVICE_ASSEMBLY_ID);
        if (saID == null) { // for backward compatibility until project is updated
            saID = p.getProperty(JbiProjectProperties.ASSEMBLY_UNIT_UUID);
        }
        return saID;
    }

    @SuppressWarnings("deprecation")
    public static String getServiceAssemblyDescription(Project p) {
        String saDescription = p.getProperty(JbiProjectProperties.SERVICE_ASSEMBLY_DESCRIPTION);
        if (saDescription == null) { // for backward compatibility until project is updated
            saDescription = p.getProperty(JbiProjectProperties.ASSEMBLY_UNIT_DESCRIPTION);
        }
        return saDescription;
    }

    @SuppressWarnings("deprecation")
    public static String getServiceUnitDescription(Project p) {
        String saDescription = p.getProperty(JbiProjectProperties.SERVICE_UNIT_DESCRIPTION);
        if (saDescription == null) { // for backward compatibility until project is updated
            saDescription = p.getProperty(JbiProjectProperties.APPLICATION_SUB_ASSEMBLY_DESCRIPTION);
        }
        return saDescription;
    }

    /**
     * Gets a list of external service unit names in the given compapp project.
     *
     * @param project   an Ant project
     *
     * @return  external service units' names
     */
    public static List<String> getExternalServiceUnitNames(Project project) {

        List<String> ret = new ArrayList<String>();

        File projectXmlFile = new File(project.getBaseDir(), "nbproject/project.xml");

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(projectXmlFile);

            // 1. get all project names
            NodeList foreignProjs = doc.getElementsByTagNameNS(
                    ANT_PROJECT_REFERENCE_NAMESPACE_1, "foreign-project" // NOI18N
                    ); // FIXME

            for (int i = 0; i < foreignProjs.getLength(); i++) {
                Element foreignProj = (Element) foreignProjs.item(i);
                String foreignProjName = foreignProj.getTextContent();
                ret.add(foreignProjName);
            }

            // 2. remove internal su project names
            NodeList libs = doc.getElementsByTagNameNS(
                    JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library" // NOI18N
                    );

            for (int i = 0; i < libs.getLength(); i++) {
                Element lib = (Element) libs.item(i);
                String cpItem = lib.getTextContent();
                // cpItem's format: reference.<project-name>.<target>
                String projName = cpItem.substring(cpItem.indexOf(".") + 1,
                        cpItem.lastIndexOf("."));
                ret.remove(projName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Fixes missing library references in the given compapp project's project.xml.
     *
     * @param project   Ant project
     */
    private static void fixBrokenLibraryReferencesInProjectXml(Project project) {

        File projectXmlFile = new File(project.getBaseDir(), "nbproject/project.xml");

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(projectXmlFile);

            // 1. get a list of library references (in the format of
            // "reference.<project-name>.<target>") for all foreign projects
            List<String> libraryReferences = new ArrayList<String>();

            NodeList references = doc.getElementsByTagNameNS(
                    ANT_PROJECT_REFERENCE_NAMESPACE_1, "reference" // NOI18N
                    ); // FIXME

            for (int i = 0; i < references.getLength(); i++) {
                Element reference = (Element) references.item(i);

                NodeList foreignProjects = reference.getElementsByTagName("foreign-project");
                String foreignProjName = foreignProjects.item(0).getTextContent();

                NodeList targets = reference.getElementsByTagName("target");
                String target = targets.item(0).getTextContent();

                libraryReferences.add("reference." + foreignProjName + "." + target);
            }

            // 2. get a list of existing library references
            NodeList datas = doc.getElementsByTagNameNS(
                    JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "data"
                    );
            Element data = (Element) datas.item(0);

            NodeList libs = doc.getElementsByTagNameNS(
                    JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library" 
                    );

            List<String> existingLibraryReferences = new ArrayList<String>();
            for (int i = libs.getLength() - 1; i >= 0; i--) {
                Element lib = (Element) libs.item(i);
                existingLibraryReferences.add(lib.getTextContent());
            }

            // 3. add the missing library references
            libraryReferences.removeAll(existingLibraryReferences);

            for (String libraryReference : libraryReferences) {
                Element library = doc.createElementNS(
                        JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library"
                        );
                library.appendChild(doc.createTextNode(libraryReference));
                data.appendChild(library);
            }
            
            XmlUtil.writeToFile(projectXmlFile.getAbsolutePath(), doc);

        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    /**
     * Fixes the library references in the given compapp project.
     *
     * @param project  Ant project
     * @param task     Ant task
     */
    // (See IZ #150911)
    // The previous compapp project system (up to NB 6.5.1 included in
    // GFESB v2.1) contains the following bug:
    // When both Java EE modules and non-EE modules are added to a
    // compapp project, the project's meta data (project.xml) gets corrupted.
    // That is, the non-EE module projects' references don't get persisted in
    // the "included-library" section because of the EE modules' presence.
    // However, this issue is invisible to the end-user because the
    // "included-library" data is never utilized before.
    //
    // Now we are going to use "included-library" to indicate whether
    // an SU project is internal or external, that is, whether an SU project
    // is included in the SA deployment or not, we need to fix the above issue
    // for all the existing compapp projects first.
    //
    // Since "jbi.content.javaee.jars" is a redundant property and exists only
    // when the compapp project contains (or contained) EE modules, we are
    // going to use its presence as a marker to indicate the project's meta data
    // needs a fix. Once the meta data is fixed, we will remove the
    // "jbi.content.javaee.jars" property and use the combination of
    // "jbi.content.additional" and "jbi.content.component" to compute it
    // when needed.
    public static void fixBrokenLibraryReferences(Project project, Task task) {

        String eeJars = project.getProperty(JbiProjectProperties.JBI_JAVAEE_JARS);
        if (eeJars != null) {
            // This compapp project contains (or contained) Java EE modules,
            // and it needs a fix.
            task.log("Fixing broken library references...");

            // 1. Fix (possible) missing included-libraries in project.xml
            fixBrokenLibraryReferencesInProjectXml(project);

            // 2. Clear "jbi.content.javaee.jars" property from project.properties
            EditableProperties properties = new EditableProperties();
            File propertyFile = new File(project.getBaseDir(), "nbproject/project.properties");

            FileOutputStream os = null;
            try {
                properties.load(new FileInputStream(propertyFile));

                properties.remove(JbiProjectProperties.JBI_JAVAEE_JARS);
                // add a comment in the property file preferrably

                os = new FileOutputStream(propertyFile);
                properties.store(os);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }
}

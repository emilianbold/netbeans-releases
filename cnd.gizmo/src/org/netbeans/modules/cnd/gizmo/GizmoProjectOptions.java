/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.cnd.gizmo;

import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.openide.util.Mutex;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author mt154047
 */
public class GizmoProjectOptions {

    private AuxiliaryConfiguration aux;
    private static final String GizmoData = "gizmo-options"; //NOI18N
    private static final String GizmoDataCollectorEnabled = "gizmo-datacollector-enabled"; //NOI18N
    private static final String GizmoDataCollectorName = "gizmo-datacollector-name"; //NOI18N
    private static final String GizmoSelectedToolsName = "gizmo-selected-tools-names"; //NOI18N
    private static final String GizmoToolName = "tool-name"; //NOI18N
    private static final String GizmoUserIntreactionRequiredActionsEnabled = "gizmo-user-interaction-required-actions-enabled"; //NOI18N
    private final String namespace;
    private final boolean shared;

    // constructors
    public GizmoProjectOptions(Project project, boolean shared) {
        this.shared = shared;
        aux = ProjectUtils.getAuxiliaryConfiguration(project);

        AntBasedProjectType antPrj = (project.getLookup().lookup(AntBasedProjectType.class));
        namespace = antPrj.getPrimaryConfigurationDataElementNamespace(shared);
    }

    public GizmoProjectOptions(Project project) {
        this(project, false);
    }

    // options
    public boolean getDataCollectorEnabled() {
        String value = doLoad(GizmoDataCollectorEnabled);
        return str2bool(value);
    }

    public void setDataCollectorEnabled(Boolean enabled) {
        doSave(GizmoDataCollectorEnabled, enabled.toString());
    }

//// options
    public String[] getSelectedTools() {
        String[] values = doLoadChildrenContent(GizmoSelectedToolsName);
        return values;
    }

    public void setSelectedTools(String[] toolNames) {
        doSave(GizmoSelectedToolsName, GizmoToolName, toolNames);
    }

  // options
    public boolean getUserInteractionRequiredActionsEnabled() {
        String value = doLoad(GizmoUserIntreactionRequiredActionsEnabled);
        return str2bool(value);
    }

    public void setUserInteractionRequiredActionsEnabled(Boolean enabled) {
        doSave(GizmoUserIntreactionRequiredActionsEnabled, enabled.toString());
    }

    public String getDataCollectorName() {
        String value = doLoad(GizmoDataCollectorName);
        return value;
    }

    public void setDataCollectorName(String name) {
        doSave(GizmoDataCollectorName, name);
    }

    // private methods, default value - false
    private boolean str2bool(String value) {
        return (value == null) || (value.length() == 0) ? false : Boolean.parseBoolean(value);
    }

    private Element getConfigurationFragment() {
        Element data = aux.getConfigurationFragment(GizmoData, namespace, shared);
        if (data == null) {
            data = createDocument(namespace, shared ? "project" : "project-private").createElementNS(namespace, GizmoData); //NOI18N
        }
        if (data == null) {
            System.err.println("GizmoProjectOptions: Failed to load and create configuration fragment (" + GizmoData + " : " + namespace + ")"); //NOI18N
        }
        return data;
    }

    private Element getNode(Element configurationFragment, String name) {
        NodeList nodes = configurationFragment.getElementsByTagNameNS(namespace, name);
        Element node;
        if (nodes.getLength() == 0) {
            node = configurationFragment.getOwnerDocument().createElementNS(namespace, name);
            configurationFragment.appendChild(node);
        } else {
            node = (Element) nodes.item(0);
        }
        return node;
    }

    private String doLoad(final String name) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {

            public String run() {
                Element configurationFragment = getConfigurationFragment();
                if (configurationFragment == null) {
                    return null;
                }
                return getNode(configurationFragment, name).getTextContent();
            }
        });
    }

    private String[] doLoadChildrenContent(final String name) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String[]>() {

            public String[] run() {
                Element configurationFragment = getConfigurationFragment();
                if (configurationFragment == null) {
                    return null;
                }
                Node n = getNode(configurationFragment, name);

                NodeList list = n.getChildNodes();
                if (list == null || list.getLength() == 0){
                    return null;
                }
                List<String> result = new ArrayList<String>();
                for (int i = 0, size = list.getLength(); i < size; i++){
                    if (list.item(i).getNodeType() == Node.ELEMENT_NODE){
                        result.add(list.item(i).getTextContent());
                    }
                }
                return result.toArray(new String[0]);
            }
        });
    }

    private void doSave(final String name, final String childName, final String[] values) {
        ProjectManager.mutex().writeAccess(new Runnable() {

            public void run() {
                Element configurationFragment = getConfigurationFragment();
                if (configurationFragment != null) {
                    Element oldElement = getNode(configurationFragment, name);
                    Element newElement = configurationFragment.getOwnerDocument().createElementNS(namespace, name);
                    for (int i = 0; i < values.length; i++){
                        Node child = configurationFragment.getOwnerDocument().createElement(childName);
                        child.setTextContent(values[i]);
                        newElement.appendChild(child);
                    }
                    configurationFragment.removeChild(oldElement);
                    //configurationFragment.getOwnerDocument().replaceChild(newElement, oldElement);
                     configurationFragment.appendChild(newElement);
                    aux.putConfigurationFragment(configurationFragment, shared);
                }
            }
        });
    }

    private void doSave(final String name, final String value) {
        ProjectManager.mutex().writeAccess(new Runnable() {

            public void run() {
                Element configurationFragment = getConfigurationFragment();
                if (configurationFragment != null) {
                    Element el = getNode(configurationFragment, name);
                    el.setTextContent(value);
                    aux.putConfigurationFragment(configurationFragment, shared);
                }
            }
        });
    }

    // utility
    private static Document createDocument(String ns, String root) throws DOMException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            return factory.newDocumentBuilder().getDOMImplementation().createDocument(ns, root, null);
        } catch (ParserConfigurationException ex) {
            throw (DOMException) new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot create parser").initCause(ex); // NOI18N
        }
    }
}


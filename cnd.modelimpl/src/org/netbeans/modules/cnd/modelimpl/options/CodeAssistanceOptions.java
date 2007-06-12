/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.options;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.openide.util.Mutex;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Sergey Grinev
 */
public class CodeAssistanceOptions {
    
    private AuxiliaryConfiguration aux;
    
    private static final String CodeAssistanceData = "code-assistance-data"; //NOI18N
    private static final String CodeModelEnabled = "code-model-enabled"; //NOI18N
    
    private final String namespace;
    private final boolean shared;
    
    // constructors
    
    public CodeAssistanceOptions(Project project, boolean shared) {
        this.shared = shared;
        aux = ((AuxiliaryConfiguration) project.getLookup().lookup(AuxiliaryConfiguration.class));
        
        AntBasedProjectType antPrj = ((AntBasedProjectType) project.getLookup().lookup(AntBasedProjectType.class));
        namespace = antPrj.getPrimaryConfigurationDataElementNamespace(shared);
    }
    
    public CodeAssistanceOptions(Project project) {
        this(project, false);
    }
    
    // options
    
    public Boolean getCodeAssistanceEnabled() {
        String value = doLoad(CodeModelEnabled);
        return value != "" && value != null ? new Boolean(value) : Boolean.TRUE;
    }
    
    public void setCodeAssistanceEnabled(Boolean enabled) {
        doSave(CodeModelEnabled, enabled.toString());
    }
    
    // private methods
    
    private Element getConfigurationFragment() {
        Element data = aux.getConfigurationFragment(CodeAssistanceData, namespace, shared);
        if (data == null) {
            data = createDocument(namespace, shared ? "project" : "project-private").createElementNS(namespace, CodeAssistanceData); //NOI18N
        }
        if (data == null) {
            System.err.println("CodeAssistanceOptions: Failed to load and create configuration fragment (" +
                    CodeAssistanceData + " : " + namespace + ")"); //NOI18N
        }
        return data;
    }
    
    private Element getNode(Element configurationFragment, String name) {
        NodeList nodes = configurationFragment.getElementsByTagNameNS(namespace, CodeModelEnabled);
        Element node;
        if (nodes.getLength() == 0) {
            node = configurationFragment.getOwnerDocument().createElementNS(namespace, CodeModelEnabled);
            configurationFragment.appendChild(node);
        } else {
            node = (Element)nodes.item(0);
        }
        return node;
    }
    
    private String doLoad(final String name) {
        return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element configurationFragment = getConfigurationFragment();
                if (configurationFragment == null) {
                    return null;
                }
                return getNode(configurationFragment, CodeModelEnabled).getTextContent();
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
            throw (DOMException)new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot create parser").initCause(ex); // NOI18N
        }
    }
}


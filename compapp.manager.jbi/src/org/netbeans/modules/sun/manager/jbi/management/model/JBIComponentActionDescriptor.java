/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.sun.manager.jbi.management.model;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.sun.manager.jbi.actions.MBeanOperationAction;
import org.netbeans.modules.sun.manager.jbi.actions.MBeanOperationGroupAction;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * JBI Component action descriptor.
 * 
 * @author jqian
 */
public class JBIComponentActionDescriptor {

    private static final String ACTION_NAMESPACE = "http://sun.com/jbi/components/ActionableMBeans";
    private static final String ACTION = "Action"; // NOI18N
    private static final String DISPLAY_NAME = "DisplayName"; // NOI18N
    private static final String DESCRIPTION = "Description"; // NOI18N   
    private static final String MBEAN_KEY = "MBeanKey"; // NOI18N
    private static final String OPERATION_NAME = "OperationName"; // NOI18N
    private static final String ENABLED = "Enabled"; // NOI18N
    private static final String IS_GROUP = "isGroup"; // NOI18N

    private String mBeanKey;
    private String displayName;
    private String description;
    private String operationName;
    private boolean enabled;
    private boolean isGroup;
    private List<JBIComponentActionDescriptor> children;

    /**
     * Creates a new group action descriptor.
     */
    private JBIComponentActionDescriptor(String displayName,
            String description) {
        this.displayName = displayName;
        this.description = description;
        isGroup = true;
    }

    /**
     * Creates a new leaf action descriptor.
     */
    private JBIComponentActionDescriptor(
            String displayName,
            String description,
            String mBeanKey,
            String operationName,
            boolean enabled) {
        this.displayName = displayName;
        this.description = description;
        this.mBeanKey = mBeanKey;
        this.operationName = operationName;
        this.enabled = enabled;
        isGroup = false;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMBeanKey() {
        return mBeanKey;
    }

    public String getOperationName() {
        return operationName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDescription() {
        return Utils.getTooltip(description);
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void addChild(JBIComponentActionDescriptor descriptor) {
        if (children == null) {
            children = new LinkedList<JBIComponentActionDescriptor>();
        }
        children.add(descriptor);
    }

    public List<JBIComponentActionDescriptor> getChildren() {
        return children;
    }

    /**
     * Gets a list of node actions from the action XML.
     * 
     * @param actionXML xml describing the component-specific actions
     * 
     * @return a list of node actions
     */
    public static List<MBeanOperationAction> getActions(String actionXML) {

        JBIComponentActionDescriptor actionDescriptor = null;

        try {
            actionDescriptor = parse(actionXML);
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }

        // The root group action is a dummy one.
        MBeanOperationGroupAction dummyRootAction = 
                (MBeanOperationGroupAction) getAction(actionDescriptor);
       
        return dummyRootAction.getActions();       
    }

    private static MBeanOperationAction getAction(
            JBIComponentActionDescriptor actionDescriptor) {

        MBeanOperationAction action = null;
        
        if (actionDescriptor != null) {
            String displayName = actionDescriptor.getDisplayName();
            String description = actionDescriptor.getDescription();
            
            if (actionDescriptor.isGroup()) {
                MBeanOperationGroupAction groupAction = 
                        new MBeanOperationGroupAction(displayName, description);
                for (JBIComponentActionDescriptor childDescriptor : actionDescriptor.getChildren()) {
                    MBeanOperationAction childAction = getAction(childDescriptor);
                    groupAction.addAction(childAction);
                }
                action = groupAction;
            } else {
                String mBeanKey = actionDescriptor.getMBeanKey();
                String operationName = actionDescriptor.getOperationName();
                boolean isEnabled = actionDescriptor.isEnabled();

                action = new MBeanOperationAction(
                        mBeanKey, operationName,
                        displayName, description, isEnabled);
            }
        }

        return action;
    }

    /**
     * Parses the component action xml data.
     * 
     * @return the top level action descriptor
     * 
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    private static JBIComponentActionDescriptor parse(String actionXmlData)
            throws ParserConfigurationException, IOException, SAXException {

        if (actionXmlData == null) {
            return null;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(
                new InputSource(new StringReader(actionXmlData)));

        JBIComponentActionDescriptor dummyRootDescriptor =
                new JBIComponentActionDescriptor("", ""); // NOI18N

        Element root = document.getDocumentElement();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element &&
                    ((Element) child).getNamespaceURI().equals(ACTION_NAMESPACE) &&
                    ((Element) child).getLocalName().equals(ACTION)) {
                dummyRootDescriptor.addChild(parseActionElement((Element) child));
            }
        }

        return dummyRootDescriptor;
    }

    private static JBIComponentActionDescriptor parseActionElement(
            Element actionElement) {

        JBIComponentActionDescriptor childDescriptor;

        String displayName =
                actionElement.getElementsByTagNameNS(
                ACTION_NAMESPACE, DISPLAY_NAME).
                item(0).getTextContent();
        String description =
                actionElement.getElementsByTagNameNS(
                ACTION_NAMESPACE, DESCRIPTION).
                item(0).getTextContent();

        String isGroup = actionElement.getAttribute(IS_GROUP);
        if (isGroup != null && isGroup.length() > 0 &&
                Boolean.parseBoolean(isGroup)) {
            childDescriptor =
                    new JBIComponentActionDescriptor(displayName, description);

            NodeList children = actionElement.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child instanceof Element &&
                        ((Element) child).getNamespaceURI().equals(ACTION_NAMESPACE) &&
                        ((Element) child).getLocalName().equals(ACTION)) {
                    childDescriptor.addChild(parseActionElement((Element) child));
                }
            }
        } else {
            String mBeanKey =
                    actionElement.getElementsByTagNameNS(
                    ACTION_NAMESPACE, MBEAN_KEY).
                    item(0).getTextContent();
            String operationName =
                    actionElement.getElementsByTagNameNS(
                    ACTION_NAMESPACE, OPERATION_NAME).
                    item(0).getTextContent();

            boolean enabled = false; // default value
            Element enabledElement = (Element) actionElement.getElementsByTagNameNS(
                    ACTION_NAMESPACE, ENABLED).item(0);
            if (enabledElement != null) { // this element is optional
                enabled = Boolean.parseBoolean(
                        enabledElement.getTextContent());
            }

            childDescriptor =
                    new JBIComponentActionDescriptor(
                    displayName, description,
                    mBeanKey, operationName, enabled);
        }

        return childDescriptor;
    }
}

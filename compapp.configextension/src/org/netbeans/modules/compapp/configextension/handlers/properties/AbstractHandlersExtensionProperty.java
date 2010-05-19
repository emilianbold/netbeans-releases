/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.compapp.configextension.handlers.properties;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentFactory;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.nodes.ConsumesNode;
import org.netbeans.modules.compapp.casaeditor.properties.spi.ExtensionProperty;
import org.netbeans.modules.compapp.configextension.handlers.model.Handler;
import org.netbeans.modules.compapp.configextension.handlers.model.HandlerParameter;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Extension property of handler chain.
 *
 * @author jqian
 */
public abstract class AbstractHandlersExtensionProperty
        extends ExtensionProperty<List> {

    private static final String INIT_PROPERTIES = "init-properties"; // NOI18N
    private static final String PROPERTY = "property"; // NOI18N
    private static final String CLASS_NAME = "classname"; // NOI18N
    private static final String PROJECT_PATH = "projectpath"; // NOI18N
    private static final String JAR_PATHS = "jarpaths"; // NOI18N
    private static final String NAME = "name"; // NOI18N
    private static final String VALUE = "value"; // NOI18N
    private CasaNode mNode;

    public AbstractHandlersExtensionProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            String propertyName,
            String displayName,
            String description) {
        super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                List.class,
                propertyName, displayName, description); 
        mNode = node;
    }

    /**
     * Gets a list of fully qualified class names for the handler.
     *
     * @param inbound   <code>true</code> for inbound communication, or
     *                  <code>false</code> for outbound communication
     *
     * @return a list of fully qualified class names for the handler
     */
    protected abstract List<String> getHandlerBaseClassNames(boolean inbound);

    /**
     * Gets the handler extension namespace URI.
     *
     * @return the handler extension namespace URI
     */
    protected abstract String getHandlerNamespaceURI();

    /**
     * Gets the handler extension element name.
     * For example, "handler" for JAX-WS, or "filter" for JAX-RS.
     *
     * @return the handler extension element name
     */
    protected abstract String getHandlerElementName();


    @Override
    public PropertyEditor getPropertyEditor() {

        PropertyEditor handlerChainPropertyEditor =
                new HandlerChainPropertyEditor();
        try {
            List<Handler> value = getValue();
            handlerChainPropertyEditor.setValue(value);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return handlerChainPropertyEditor;
    }

    @Override
    public List<Handler> getValue()
            throws IllegalAccessException, InvocationTargetException {

        List<Handler> ret = new ArrayList<Handler>();

        CasaComponent component = getComponent();

//        String handlerNamespaceURI = getHandlerNamespaceURI();

        for (CasaExtensibilityElement handlerExtElement : component.getExtensibilityElements()) {
            // FIXME: why assertion failure after rebuild compapp with the
            // same endpoint selected?
//            assert handlerExtElement.getQName().equals(
//                    new QName(handlerNamespaceURI, getHandlerElementName()));

            String name = handlerExtElement.getAttribute(NAME);
            String className = handlerExtElement.getAttribute(CLASS_NAME);
            String projectPath = handlerExtElement.getAttribute(PROJECT_PATH);
            String jarPaths = handlerExtElement.getAttribute(JAR_PATHS);
            List<HandlerParameter> parameters = new ArrayList<HandlerParameter>();

            List<CasaExtensibilityElement> propertyContainerExtElement =
                    handlerExtElement.getExtensibilityElements();
            if (propertyContainerExtElement != null &&
                    propertyContainerExtElement.size() > 0) {
                assert propertyContainerExtElement.size() == 1; // &&
//                        propertyContainerExtElement.get(0).getQName().equals(
//                        new QName(handlerNamespaceURI, INIT_PROPERTIES));

                for (CasaExtensibilityElement propertyExtElement :
                    propertyContainerExtElement.get(0).getExtensibilityElements()) {
//                    assert propertyExtElement.getQName().equals(
//                            new QName(handlerNamespaceURI, PROPERTY));

                    String propertyName = propertyExtElement.getAttribute(NAME);
                    String propertyValue = propertyExtElement.getAttribute(VALUE);

                    HandlerParameter parameter = new HandlerParameter();
                    parameter.setName(propertyName);
                    parameter.setValue(propertyValue);
                    parameters.add(parameter);
                }
            }

            Handler handler = new Handler();
            handler.setName(name);
            handler.setClassName(className);
            handler.setProjectPath(projectPath);
            handler.setJarPaths(jarPaths);
            handler.setParameters(parameters);

            ret.add(handler);
        }

        return ret;
    }

    @Override
    public void setValue(List value)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {

        CasaComponent component = getComponent();
        CasaWrapperModel model = getModel();
        CasaComponentFactory factory = model.getFactory();

        Document document =
                getModel().getRootComponent().getPeer().getOwnerDocument();

        List<CasaExtensibilityElement> handlerExtElements =
                new ArrayList<CasaExtensibilityElement>();

        String handlerNamespaceURI = getHandlerNamespaceURI();
        String handlerElementName = getHandlerElementName();

        if (value != null) {
            for (Object item : value) {
                Handler handler = (Handler) item;
                String name = handler.getName();
                String className = handler.getClassName();
                String projectPath = handler.getProjectPath();
                String jarPaths = handler.getJarPathsAsString();
                List<HandlerParameter> parameters = handler.getParameters();

                Element handlerDomElement =
                        document.createElementNS(handlerNamespaceURI, handlerElementName);
                CasaExtensibilityElement handlerExtElement =
                        (CasaExtensibilityElement) factory.create(
                        handlerDomElement, component);
                handlerExtElement.setAttribute(NAME, name);
                handlerExtElement.setAttribute(CLASS_NAME, className);
                handlerExtElement.setAttribute(PROJECT_PATH, projectPath);
                handlerExtElement.setAttribute(JAR_PATHS, jarPaths);

                if (parameters != null && parameters.size() > 0) {
                    Element propertyContainerDomElement =
                            document.createElementNS(handlerNamespaceURI, INIT_PROPERTIES);
                    CasaExtensibilityElement propertyContainerExtElement =
                            (CasaExtensibilityElement) factory.create(
                            propertyContainerDomElement, handlerExtElement);
                    handlerExtElement.addExtensibilityElement(propertyContainerExtElement);

                    for (HandlerParameter parameter : parameters) {
                        Element propertyDomElement =
                                document.createElementNS(handlerNamespaceURI, PROPERTY);
                        CasaExtensibilityElement propertyExtElement =
                                (CasaExtensibilityElement) factory.create(
                                propertyDomElement, propertyContainerExtElement);
                        propertyExtElement.setAttribute(NAME, parameter.getName());
                        propertyExtElement.setAttribute(VALUE, parameter.getValue());

                        propertyContainerExtElement.addExtensibilityElement(propertyExtElement);
                    }
                }

                handlerExtElements.add(handlerExtElement);
            }
        }

        model.startTransaction();
        try {
            // clear existing extensibility child elements
            for (CasaExtensibilityElement extElement : component.getExtensibilityElements()) {
                component.removeExtensibilityElement(extElement);
            }

            // re-add extensibility child elements
            for (CasaExtensibilityElement handlerExtElement : handlerExtElements) {
                component.addExtensibilityElement(handlerExtElement);
            }
        } finally {
            if (model.isIntransaction()) {
                model.endTransaction();
            }
        }
        
        if (component.getParent() == null) {
            model.addExtensibilityElement(extensionPointComponent,
                    (CasaExtensibilityElement) component);
        }
    }

    class HandlerChainPropertyEditor extends PropertyEditorSupport
            implements ExPropertyEditor {

        private HandlerChainCustomEditor customEditor;

        @Override
        public String getAsText() {
            List value = (List) getValue();

            StringBuffer ret = new StringBuffer(); 

            if (value != null && value.size() > 0) {
                boolean first = true;

                ret.append("["); // NOI18N
                for (Object item : value) {
                    Handler handler = (Handler) item;
                    if (!first) {
                        ret.append(", "); // NOI18N
                    }
                    ret.append(handler.getName());
                    first = false;
                }
                ret.append("]"); // NOI18N
            }

            return ret.toString();
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public java.awt.Component getCustomEditor() {
            @SuppressWarnings("unchecked")
            List<Handler> handlers = (List<Handler>) getValue();

            boolean inbound = getNode() instanceof ConsumesNode;
            List<String> handlerBaseClassNames = getHandlerBaseClassNames(inbound);

            customEditor = new HandlerChainCustomEditor(canWrite(),
                    handlerBaseClassNames, handlers);
            return customEditor;
        }

        public void attachEnv(PropertyEnv env) {
            // Disable direct inline text editing.
            env.getFeatureDescriptor().setValue("canEditAsText", false); // NOI18N

            // Add validation.
            env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            env.addVetoableChangeListener(new VetoableChangeListener() {

                public void vetoableChange(PropertyChangeEvent ev)
                        throws PropertyVetoException {
                    if (PropertyEnv.PROP_STATE.equals(ev.getPropertyName())) {
                        // customEditor.validateValue();
                    }
                }
            });

            env.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent ev) {
                    if (PropertyEnv.PROP_STATE.equals(ev.getPropertyName())) {
                        try {
                            setValue(customEditor.getPropertyValue());
                        } catch (Exception ex) {
                            // set failed..
                        }
                    }
                }
            });
        }
    }
}

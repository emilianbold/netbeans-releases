/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javaee.wildfly.ide.commands;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.security.auth.callback.CallbackHandler;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.javaee.wildfly.JBTargetModuleID;
import org.netbeans.modules.javaee.wildfly.WildFlyDeploymentFactory;
import org.netbeans.modules.javaee.wildfly.config.JBossDatasource;
import org.netbeans.modules.javaee.wildfly.config.JBossMessageDestination;
import org.netbeans.modules.javaee.wildfly.ide.ui.JBPluginProperties;
import org.netbeans.modules.javaee.wildfly.nodes.JBDatasourceNode;
import org.netbeans.modules.javaee.wildfly.nodes.JBDestinationNode;
import org.netbeans.modules.javaee.wildfly.nodes.JBEjbModuleNode;
import org.netbeans.modules.javaee.wildfly.nodes.JBWebModuleNode;
import org.openide.util.Lookup;

/**
 *
 * @author ehugonnet
 */
public class WildflyClient {

    private static final Logger LOGGER = Logger.getLogger(WildflyClient.class.getName());

    private static final String SERVER_STATE = "server-state"; // NOI18N
    private static final String WEB_SUBSYSTEM = "undertow"; // NOI18N
    private static final String EJB3_SUBSYSTEM = "ejb3"; // NOI18N
    private static final String DATASOURCES_SUBSYSTEM = "datasources"; // NOI18N
    private static final String DATASOURCE_TYPE = "data-source"; // NOI18N

    private static final String MESSAGING_SUBSYSTEM = "messaging"; // NOI18N
    private static final String HORNETQ_SERVER_TYPE = "hornetq-server"; // NOI18N
    private static final String JMSQUEUE_TYPE = "jms-queue"; // NOI18N
    private static final String JMSTOPIC_TYPE = "jms-topic"; // NOI18N

    private final String serverAddress;
    private final int serverPort;
    private final CallbackHandler handler;
    private final InstanceProperties ip;
    private Object client;

    private Map<String, Object> clientConstants;

    private Map<String, Object> modelDescriptionConstants;

    /**
     * Get the value of serverPort
     *
     * @return the value of serverPort
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Get the value of serverAddress
     *
     * @return the value of serverAddress
     */
    public String getServerAddress() {
        return serverAddress;
    }

    public WildflyClient(InstanceProperties ip, String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.ip = ip;
        handler = new Authentication().getCallbackHandler();
    }

    public WildflyClient(InstanceProperties ip, String serverAddress, int serverPort, String login,
            String password) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.ip = ip;
        handler = new Authentication(login, password.toCharArray()).getCallbackHandler();
    }

    // ModelControllerClient
    private synchronized Object getClient(WildFlyDeploymentFactory.WildFlyClassLoader cl) {
        if (client == null) {
            try {
                this.client = createClient(cl);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, null, ex);
                return null;
            }
        }
        return this.client;
    }

    private synchronized void close() {
        try {
            if (this.client != null) {
                closeClient(WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip), client);
            }
            this.client = null;
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    public void shutdownServer() throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object shutdownOperation = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, shutdownOperation, getModelDescriptionConstant(cl, "OP")), getModelDescriptionConstant(cl, "SHUTDOWN"));
            executeAsync(cl, shutdownOperation, null);
            close();
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    public synchronized boolean isServerRunning() {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object statusOperation = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, statusOperation, getClientConstant(cl, "OP")), getClientConstant(cl, "READ_ATTRIBUTE_OPERATION"));
            setModelNodeChildEmptyList(cl, getModelNodeChild(cl, statusOperation, getClientConstant(cl, "OP_ADDR")));
            setModelNodeChildString(cl, getModelNodeChild(cl, statusOperation, getClientConstant(cl, "NAME")), SERVER_STATE);
            // ModelNode
            Object response = executeOnModelNode(cl, statusOperation);
            return getClientConstant(cl, "SUCCESS").equals(modelNodeAsString(cl, getModelNodeChild(cl, response, getClientConstant(cl, "OUTCOME"))))
                    && !getClientConstant(cl, "CONTROLLER_PROCESS_STATE_STARTING").equals(modelNodeAsString(cl, getModelNodeChild(cl, response, getModelDescriptionConstant(cl, "RESULT"))))
                    && !getClientConstant(cl, "CONTROLLER_PROCESS_STATE_STOPPING").equals(modelNodeAsString(cl, getModelNodeChild(cl, response, getModelDescriptionConstant(cl, "RESULT"))));
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, null, ex);
            close();
            return false;
        }
    }

    // ModelNode
    private synchronized Object executeOnModelNode(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Object clientLocal = getClient(cl);
        Method method = clientLocal.getClass().getMethod("execute", modelClazz);
        return method.invoke(clientLocal, modelNode);
    }

    // ModelNode
    private synchronized Object executeOnOperation(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object operation) throws IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class operationClazz = cl.loadClass("org.jboss.as.controller.client.Operation"); // NOI18N
        Object clientLocal = getClient(cl);
        Method method = clientLocal.getClass().getMethod("execute", operationClazz);
        return method.invoke(clientLocal, operation);
    }

    private synchronized Future<?> executeAsync(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, Object operationMessageHandler) throws IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Class handlerClazz = cl.loadClass("org.jboss.as.controller.client.OperationMessageHandler"); // NOI18N
        Object clientLocal = getClient(cl);
        Method method = clientLocal.getClass().getMethod("executeAsync", modelClazz, handlerClazz);
        return (Future) method.invoke(clientLocal, modelNode, operationMessageHandler);
    }

    public Collection<JBModule> listAvailableModules() throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<JBModule> modules = new ArrayList<JBModule>();
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, null);
            // ModelNode
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true);
            // ModelNode
            Object response = executeOnModelNode(cl, readDeployments);
            String httpPort = ip.getProperty(JBPluginProperties.PROPERTY_PORT);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object result = readResult(cl, response);
                // List<ModelNode>
                List webapps = modelNodeAsList(cl, result);
                for (Object application : webapps) {
                    String applicationName = modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, application), getClientConstant(cl, "NAME")));
                    // ModelNode
                    Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, readResult(cl, application), getClientConstant(cl, "SUBSYSTEM")), WEB_SUBSYSTEM);
                    JBModule module = new JBModule(applicationName, true);
                    if (modelNodeIsDefined(cl, deployment)) {
                        String url = "http://" + serverAddress + ':' + httpPort + modelNodeAsString(cl, getModelNodeChild(cl, deployment, "context-root"));
                        module.setUrl(url);
                    }
                    modules.add(module);
                }
            }
            return modules;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    public Collection<JBWebModuleNode> listWebModules(Lookup lookup) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<JBWebModuleNode> modules = new ArrayList<JBWebModuleNode>();
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, null);
            // ModelNode
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true);
            // ModelNode
            Object response = executeOnModelNode(cl, readDeployments);
            String httpPort = ip.getProperty(JBPluginProperties.PROPERTY_PORT);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object result = readResult(cl, response);
                // List<ModelNode>
                List webapps = modelNodeAsList(cl, result);
                for (Object application : webapps) {
                    String applicationName = modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, application), getClientConstant(cl, "NAME")));
                    if (applicationName.endsWith(".war")) {
                        // ModelNode
                        Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, readResult(cl, application), getClientConstant(cl, "SUBSYSTEM")), WEB_SUBSYSTEM);
                        if (modelNodeIsDefined(cl, deployment)) {
                            String url = "http://" + serverAddress + ':' + httpPort + modelNodeAsString(cl, getModelNodeChild(cl, deployment, "context-root"));
                            modules.add(new JBWebModuleNode(applicationName, lookup, url));
                        } else {
                            modules.add(new JBWebModuleNode(applicationName, lookup, null));
                        }
                    }
                }
            }
            return modules;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }
    
    
    public String getWebModuleURL(String webModuleName) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<JBWebModuleNode> modules = new ArrayList<JBWebModuleNode>();
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, webModuleName);
            // ModelNode
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true);
            // ModelNode
            Object response = executeOnModelNode(cl, readDeployments);
            String httpPort = ip.getProperty(JBPluginProperties.PROPERTY_PORT);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object result = readResult(cl, response);
                // List<ModelNode>
                List webapps = modelNodeAsList(cl, result);
                for (Object application : webapps) {
                    String applicationName = modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, application), getClientConstant(cl, "NAME")));
                    if (applicationName.endsWith(".war")) {
                        // ModelNode
                        Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, readResult(cl, application), getClientConstant(cl, "SUBSYSTEM")), WEB_SUBSYSTEM);
                        if (modelNodeIsDefined(cl, deployment)) {
                            return "http://" + serverAddress + ':' + httpPort + modelNodeAsString(cl, getModelNodeChild(cl, deployment, "context-root"));
                            }
                    }
                }
            }
            return "";
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    public Collection<JBEjbModuleNode> listEJBModules(Lookup lookup) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<JBEjbModuleNode> modules = new ArrayList<JBEjbModuleNode>();
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, null);
            // ModelNode
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true);
            // ModelNode
            Object response = executeOnModelNode(cl, readDeployments);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object result = readResult(cl, response);
                // List<ModelNode>
                List ejbs = modelNodeAsList(cl, result);
                for (Object ejb : ejbs) {
                    // ModelNode
                    Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, readResult(cl, ejb), getClientConstant(cl, "SUBSYSTEM")), EJB3_SUBSYSTEM);
                    if (modelNodeIsDefined(cl, deployment)) {
                        modules.add(new JBEjbModuleNode(modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, ejb), getClientConstant(cl, "NAME"))), lookup, true));
                    }
                }
            }
            return modules;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }
    
    public boolean startModule(JBTargetModuleID tmid) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, tmid.getModuleID());
            // ModelNode
            Object enableDeployment = createOperation(cl, getClientConstant(cl, "DEPLOYMENT_REDEPLOY_OPERATION"), deploymentAddressModelNode);
            Object result = executeOnModelNode(cl, enableDeployment);
            if(isSuccessfulOutcome(cl, result)) {
                tmid.setContextURL(getWebModuleURL(tmid.getModuleID()));
                return true;
            }
            return false;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    public boolean startModule(String moduleName) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, moduleName);
            // ModelNode
            Object enableDeployment = createOperation(cl, getClientConstant(cl, "DEPLOYMENT_REDEPLOY_OPERATION"), deploymentAddressModelNode);
            Object result = executeOnModelNode(cl, enableDeployment);
            return isSuccessfulOutcome(cl, result);
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    public boolean stopModule(String moduleName) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, moduleName);
            // ModelNode
            Object enableDeployment = createOperation(cl, getClientConstant(cl, "DEPLOYMENT_UNDEPLOY_OPERATION"), deploymentAddressModelNode);
            return isSuccessfulOutcome(cl, executeOnModelNode(cl, enableDeployment));
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    public boolean undeploy(String fileName) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, fileName);

            // ModelNode
            final Object undeploy = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, undeploy, getClientConstant(cl, "OP")), getClientConstant(cl, "COMPOSITE"));
            setModelNodeChildEmptyList(cl, getModelNodeChild(cl, undeploy, getModelDescriptionConstant(cl, "ADDRESS")));
            // ModelNode
            Object steps = getModelNodeChild(cl, undeploy, getClientConstant(cl, "STEPS"));
            addModelNodeChild(cl, steps, createOperation(cl, getClientConstant(cl, "DEPLOYMENT_UNDEPLOY_OPERATION"), deploymentAddressModelNode));
            addModelNodeChild(cl, steps, createRemoveOperation(cl, deploymentAddressModelNode));
            return isSuccessfulOutcome(cl, executeOnModelNode(cl, undeploy));
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    public boolean deploy(DeploymentContext deployment) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            String fileName = deployment.getModuleFile().getName();
            undeploy(fileName);

            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, fileName);

            // ModelNode
            final Object deploy = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, deploy, getClientConstant(cl, "OP")), getClientConstant(cl, "COMPOSITE"));
            setModelNodeChildEmptyList(cl, getModelNodeChild(cl, deploy, getModelDescriptionConstant(cl, "ADDRESS")));
            // ModelNode
            Object steps = getModelNodeChild(cl, deploy, getClientConstant(cl, "STEPS"));
            // ModelNode
            Object addModule = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, addModule, getClientConstant(cl, "OP")), getClientConstant(cl, "ADD"));
            setModelNodeChildString(cl, getModelNodeChildAtPath(cl, addModule,
                    new Object[]{getModelDescriptionConstant(cl, "ADDRESS"), getClientConstant(cl, "DEPLOYMENT")}), fileName);
            setModelNodeChildString(cl, getModelNodeChild(cl, addModule, getClientConstant(cl, "RUNTIME_NAME")), fileName);
            setModelNodeChildBytes(cl, getModelNodeChild(cl, getModelNodeChildAtIndex(cl, getModelNodeChild(cl, addModule, getClientConstant(cl, "CONTENT")), 0),
                    getModelDescriptionConstant(cl, "BYTES")), deployment.getModule().getArchive().asBytes());

            addModelNodeChild(cl, steps, addModule);
            addModelNodeChild(cl, steps, createOperation(cl, getClientConstant(cl, "DEPLOYMENT_REDEPLOY_OPERATION"), deploymentAddressModelNode));
            // ModelNode
            Object result = executeOnModelNode(cl, deploy);
            return isSuccessfulOutcome(cl, result);
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    public Collection<JBDatasourceNode> listDatasources(Lookup lookup) throws IOException {
        Set<Datasource> datasources = listDatasources();
        List<JBDatasourceNode> modules = new ArrayList<JBDatasourceNode>(datasources.size());
        for (Datasource ds : datasources) {
            modules.add(new JBDatasourceNode(((JBossDatasource) ds).getName(), ds, lookup));
        }
        return modules;
    }

    public Set<Datasource> listDatasources() throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            Set<Datasource> listedDatasources = new HashSet<Datasource>();
            // ModelNode
            final Object readDatasources = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readDatasources, getClientConstant(cl, "OP")), getClientConstant(cl, "READ_CHILDREN_NAMES_OPERATION"));

            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), DATASOURCES_SUBSYSTEM);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readDatasources, getModelDescriptionConstant(cl, "ADDRESS")), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readDatasources, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readDatasources, getClientConstant(cl, "CHILD_TYPE")), DATASOURCE_TYPE);

            // ModelNode
            Object response = executeOnModelNode(cl, readDatasources);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List names = modelNodeAsList(cl, readResult(cl, response));
                for (Object datasourceName : names) {
                    listedDatasources.add(getDatasource(cl, modelNodeAsString(cl, datasourceName)));
                }
            }
            return listedDatasources;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    private JBossDatasource getDatasource(WildFlyDeploymentFactory.WildFlyClassLoader cl, String name) throws IOException {
        try {
            // ModelNode
            final Object readDatasource = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readDatasource, getClientConstant(cl, "OP")), getClientConstant(cl, "READ_RESOURCE_OPERATION"));
            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), DATASOURCES_SUBSYSTEM);
            values.put(DATASOURCE_TYPE, name);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readDatasource, getModelDescriptionConstant(cl, "ADDRESS")), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readDatasource, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            // ModelNode
            Object response = executeOnModelNode(cl, readDatasource);
            if (isSuccessfulOutcome(cl, response)) {
                // ModelNode
                Object datasource = readResult(cl, response);
                return new JBossDatasource(name, modelNodeAsString(cl, getModelNodeChild(cl, datasource, "jndi-name")),
                        modelNodeAsString(cl, getModelNodeChild(cl, datasource, "connection-url")),
                        modelNodeAsString(cl, getModelNodeChild(cl, datasource, "user-name")),
                        modelNodeAsString(cl, getModelNodeChild(cl, datasource, "password")),
                        modelNodeAsString(cl, getModelNodeChild(cl, datasource, "driver-class")));
            }
            return null;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    public Collection<JBDestinationNode> listDestinations(Lookup lookup) throws IOException {
        Set<MessageDestination> destinations = listDestinations();
        List<JBDestinationNode> modules = new ArrayList<JBDestinationNode>(destinations.size());
        for (MessageDestination destination : destinations) {
            modules.add(new JBDestinationNode(destination.getName(), destination, lookup));
        }
        return modules;
    }

    public Set<MessageDestination> listDestinations() throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            Set<MessageDestination> destinations = new HashSet<MessageDestination>();
            // ModelNode
            final Object readHornetQServers = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readHornetQServers, getClientConstant(cl, "OP")), getClientConstant(cl, "READ_CHILDREN_NAMES_OPERATION"));

            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), MESSAGING_SUBSYSTEM);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readHornetQServers, getModelDescriptionConstant(cl, "ADDRESS")), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readHornetQServers, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            setModelNodeChildString(cl, getModelNodeChild(cl, readHornetQServers, getClientConstant(cl, "CHILD_TYPE")), HORNETQ_SERVER_TYPE);

            // ModelNode
            Object response = executeOnModelNode(cl, readHornetQServers);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List names = modelNodeAsList(cl, readResult(cl, response));
                for (Object hornetqServer : names) {
                    String hornetqServerName = modelNodeAsString(cl, hornetqServer);
                    destinations.addAll(getJMSDestinationForServer(hornetqServerName, Type.QUEUE));
                    destinations.addAll(getJMSDestinationForServer(hornetqServerName, Type.TOPIC));
                }
            }
            return destinations;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    private Set<JBossMessageDestination> getJMSDestinationForServer(String serverName, Type messageType) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            Set<JBossMessageDestination> listedDestinations = new HashSet<JBossMessageDestination>();
            // ModelNode
            final Object readQueues = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, getClientConstant(cl, "OP")),
                    getModelDescriptionConstant(cl, "READ_CHILDREN_RESOURCES_OPERATION"));

            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), MESSAGING_SUBSYSTEM);
            values.put(HORNETQ_SERVER_TYPE, serverName);
            // ModelNode
            Object path = createPathAddressAsModelNode(cl, values);
            setModelNodeChild(cl, getModelNodeChild(cl, readQueues, getModelDescriptionConstant(cl, "ADDRESS")), path);
            setModelNodeChild(cl, getModelNodeChild(cl, readQueues, getModelDescriptionConstant(cl, "RECURSIVE_DEPTH")), 0);
            if (messageType == Type.QUEUE) {
                setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, getClientConstant(cl, "CHILD_TYPE")), JMSQUEUE_TYPE);
            } else {
                setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, getClientConstant(cl, "CHILD_TYPE")), JMSTOPIC_TYPE);
            }

            // ModelNode
            Object response = executeOnModelNode(cl, readQueues);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List destinations = modelNodeAsList(cl, readResult(cl, response));
                for (Object detination : destinations) {
                    List entries = modelNodeAsList(cl, getModelNodeChild(cl, modelNodeAsPropertyForValue(cl, detination), "entries"));
                    for (Object entry : entries) {
                        listedDestinations.add(new JBossMessageDestination(modelNodeAsString(cl, entry), messageType));
                    }
                }
            }
            return listedDestinations;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IOException(ex);
        } catch (InvocationTargetException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        }
    }

    private Object createClient(WildFlyDeploymentFactory.WildFlyClassLoader cl) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Class clazz = cl.loadClass("org.jboss.as.controller.client.ModelControllerClient$Factory"); // NOI18N
        Method method = clazz.getDeclaredMethod("create", String.class, int.class, CallbackHandler.class
        );
        return method.invoke(
                null, serverAddress, serverPort, handler);
    }

    private static void closeClient(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object client) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Method method = client.getClass().getMethod("close", new Class[]{});
        method.invoke(client, (Object[]) null);
    }

    // ModelNode
    private Object createDeploymentPathAddressAsModelNode(WildFlyDeploymentFactory.WildFlyClassLoader cl, String name)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class paClazz = cl.loadClass("org.jboss.as.controller.PathAddress"); // NOI18N
        Class peClazz = cl.loadClass("org.jboss.as.controller.PathElement"); // NOI18N

        Method peFactory = peClazz.getDeclaredMethod("pathElement",// NOI18N
                name != null ? new Class[]{String.class, String.class} : new Class[]{String.class});
        Object pe = peFactory.invoke(null,
                name != null ? new Object[]{getClientConstant(cl, "DEPLOYMENT"), name} : new Object[]{getClientConstant(cl, "DEPLOYMENT")});// NOI18N

        Object array = Array.newInstance(peClazz, 1);
        Array.set(array, 0, pe);
        Method paFactory = paClazz.getDeclaredMethod("pathAddress", array.getClass()); // NOI18N
        Object pa = paFactory.invoke(null, array);

        Method toModelNode = pa.getClass().getMethod("toModelNode", (Class<?>[]) null); // NOI18N
        return toModelNode.invoke(pa, (Object[]) null);
    }

    // ModelNode
    private Object createPathAddressAsModelNode(WildFlyDeploymentFactory.WildFlyClassLoader cl, LinkedHashMap<Object, Object> elements)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class paClazz = cl.loadClass("org.jboss.as.controller.PathAddress"); // NOI18N
        Class peClazz = cl.loadClass("org.jboss.as.controller.PathElement"); // NOI18N

        Method peFactory = peClazz.getDeclaredMethod("pathElement", new Class[]{String.class, String.class});
        Object array = Array.newInstance(peClazz, elements.size());
        int i = 0;
        for (Map.Entry<Object, Object> entry : elements.entrySet()) {
            Array.set(array, i, peFactory.invoke(null, new Object[]{entry.getKey(), entry.getValue()}));
            i++;
        }

        Method paFactory = paClazz.getDeclaredMethod("pathAddress", array.getClass()); // NOI18N
        Object pa = paFactory.invoke(null, array);

        Method toModelNode = pa.getClass().getMethod("toModelNode", (Class<?>[]) null); // NOI18N
        return toModelNode.invoke(pa, (Object[]) null);
    }

    // ModelNode
    private static Object createOperation(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object name, Object modelNode)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("createOperation", new Class[]{String.class, modelClazz});
        return method.invoke(null, name, modelNode);
    }

    // ModelNode
    private static Object createReadResourceOperation(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, boolean recursive)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("createReadResourceOperation", new Class[]{modelClazz, boolean.class});
        return method.invoke(null, modelNode, recursive);
    }

    // ModelNode
    private static Object createRemoveOperation(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("createRemoveOperation", new Class[]{modelClazz});
        return method.invoke(null, modelNode);
    }

    // ModelNode
    private static Object readResult(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("readResult", new Class[]{modelClazz});
        return method.invoke(null, modelNode);
    }

    // ModelNode
    private static Object getModelNodeChild(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, Object name) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("get", String.class);
        return method.invoke(modelNode, name);
    }

    // ModelNode
    private static Object getModelNodeChildAtIndex(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, int index) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("get", int.class);
        return method.invoke(modelNode, index);
    }

    // ModelNode
    private static Object getModelNodeChildAtPath(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, Object[] path) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("get", String[].class);
        Object array = Array.newInstance(String.class, path.length);
        for (int i = 0; i < path.length; i++) {
            Array.set(array, i, path[i]);
        }
        return method.invoke(modelNode, array);
    }

    // ModelNode
    private static Object createModelNode(WildFlyDeploymentFactory.WildFlyClassLoader cl) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        return modelClazz.newInstance();
    }

    // ModelNode
    private static Object setModelNodeChildString(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, Object value) throws IllegalAccessException,
            ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        assert value != null;
        Method method = modelNode.getClass().getMethod("set", String.class);
        return method.invoke(modelNode, value);
    }

    // ModelNode
    private static Object setModelNodeChild(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, Object value) throws IllegalAccessException,
            ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        assert value != null;
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = modelNode.getClass().getMethod("set", modelClazz);
        return method.invoke(modelNode, value);
    }

    // ModelNode
    private static Object setModelNodeChild(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, int value) throws IllegalAccessException,
            ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("set", int.class);
        return method.invoke(modelNode, value);
    }

    // ModelNode
    private static Object setModelNodeChildEmptyList(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IllegalAccessException,
            ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        Method method = modelNode.getClass().getMethod("setEmptyList", (Class<?>[]) null);
        return method.invoke(modelNode, (Object[]) null);
    }

    // ModelNode
    private static Object setModelNodeChildBytes(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, byte[] value) throws IllegalAccessException,
            ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        Method method = modelNode.getClass().getMethod("set", byte[].class);
        return method.invoke(modelNode, value);
    }

    // ModelNode
    private static Object addModelNodeChild(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, Object toAddModelNode) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = modelNode.getClass().getMethod("add", modelClazz);
        return method.invoke(modelNode, toAddModelNode);
    }

    private static boolean modelNodeIsDefined(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("isDefined", (Class<?>[]) null);
        return (Boolean) method.invoke(modelNode, (Object[]) null);
    }

    private static String modelNodeAsString(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("asString", (Class<?>[]) null);
        return (String) method.invoke(modelNode, (Object[]) null);
    }

    private static String modelNodeAsPropertyForName(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("asProperty", (Class<?>[]) null);
        Object property = method.invoke(modelNode, (Object[]) null);
        method = property.getClass().getMethod("getName", (Class<?>[]) null);
        return (String) method.invoke(property, (Object[]) null);
    }

    private static Object modelNodeAsPropertyForValue(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("asProperty", (Class<?>[]) null);
        Object property = method.invoke(modelNode, (Object[]) null);
        method = property.getClass().getMethod("getValue", (Class<?>[]) null);
        return method.invoke(property, (Object[]) null);
    }

    // List<ModelNode>
    private static List modelNodeAsList(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("asList", (Class<?>[]) null);
        return (List) method.invoke(modelNode, (Object[]) null);
    }

    private boolean isSuccessfulOutcome(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("isSuccessfulOutcome", modelClazz);
        return (Boolean) method.invoke(null, modelNode);
    }

    private Object getClientConstant(WildFlyDeploymentFactory.WildFlyClassLoader cl, String name) throws ClassNotFoundException, IllegalAccessException {
        if (clientConstants == null) {
            clientConstants = new HashMap<String, Object>();
            Class clazz = cl.loadClass("org.jboss.as.controller.client.helpers.ClientConstants"); // NOI18N
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                int modifiers = f.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                    clientConstants.put(f.getName(), f.get(null));
                }
            }
        }
        return clientConstants.get(name);
    }

    private Object getModelDescriptionConstant(WildFlyDeploymentFactory.WildFlyClassLoader cl, String name) throws ClassNotFoundException, IllegalAccessException {
        if (modelDescriptionConstants == null) {
            modelDescriptionConstants = new HashMap<String, Object>();
            Class clazz = cl.loadClass("org.jboss.as.controller.descriptions.ModelDescriptionConstants"); // NOI18N
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                int modifiers = f.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                    modelDescriptionConstants.put(f.getName(), f.get(null));
                }
            }
        }
        return modelDescriptionConstants.get(name);
    }
}

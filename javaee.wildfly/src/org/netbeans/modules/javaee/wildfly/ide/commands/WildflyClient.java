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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.callback.CallbackHandler;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.javaee.wildfly.WildflyTargetModuleID;
import org.netbeans.modules.javaee.wildfly.WildFlyDeploymentFactory;
import org.netbeans.modules.javaee.wildfly.config.JBossDatasource;
import org.netbeans.modules.javaee.wildfly.config.JBossMessageDestination;
import org.netbeans.modules.javaee.wildfly.ide.ui.JBPluginProperties;
import org.netbeans.modules.javaee.wildfly.nodes.JBDatasourceNode;
import org.netbeans.modules.javaee.wildfly.nodes.JBDestinationNode;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyEjbModuleNode;
import org.netbeans.modules.javaee.wildfly.nodes.JBWebModuleNode;
import org.openide.util.Lookup;

import static org.netbeans.modules.javaee.wildfly.ide.commands.WildflyManagementAPI.*;
import org.netbeans.modules.javaee.wildfly.nodes.JBEarApplicationNode;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyEJBComponentNode;

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
                this.client = createClient(cl, serverAddress, serverPort, handler);
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
                String applicationName = modelNodeAsString(cl, getModelNodeChild(cl, result, getClientConstant(cl, "NAME")));
                if (applicationName.endsWith(".war")) {
                    // ModelNode
                    Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, result, getClientConstant(cl, "SUBSYSTEM")), WEB_SUBSYSTEM);
                    if (modelNodeIsDefined(cl, deployment)) {
                        return "http://" + serverAddress + ':' + httpPort + modelNodeAsString(cl, getModelNodeChild(cl, deployment, "context-root"));
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

    public Collection<WildflyEjbModuleNode> listEJBModules(Lookup lookup) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<WildflyEjbModuleNode> modules = new ArrayList<WildflyEjbModuleNode>();
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
                        List<WildflyEJBComponentNode> ejbInstances = new ArrayList<WildflyEJBComponentNode>();
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEJBComponentNode.Type.ENTITY));
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEJBComponentNode.Type.MDB));
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEJBComponentNode.Type.SINGLETON));
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEJBComponentNode.Type.STATEFULL));
                        ejbInstances.addAll(listEJBs(cl, deployment, WildflyEJBComponentNode.Type.STATELESS));
                        modules.add(new WildflyEjbModuleNode(modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, ejb), getClientConstant(cl, "NAME"))), lookup, ejbInstances, true));
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

    public boolean startModule(WildflyTargetModuleID tmid) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            // ModelNode
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, tmid.getModuleID());
            // ModelNode
            Object enableDeployment = createOperation(cl, getClientConstant(cl, "DEPLOYMENT_REDEPLOY_OPERATION"), deploymentAddressModelNode);
            Object result = executeOnModelNode(cl, enableDeployment);
            if (isSuccessfulOutcome(cl, result)) {
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
        List<MessageDestination> destinations = listDestinations();
        List<JBDestinationNode> modules = new ArrayList<JBDestinationNode>(destinations.size());
        for (MessageDestination destination : destinations) {
            modules.add(new JBDestinationNode(destination.getName(), destination, lookup));
        }
        return modules;
    }

    public List<JBDestinationNode> listDestinationForDeployment(Lookup lookup, String jeeDeploymentName) throws IOException {
        List<MessageDestination> destinations = listDestinationForDeployment(jeeDeploymentName);
        List<JBDestinationNode> modules = new ArrayList<JBDestinationNode>(destinations.size());
        for (MessageDestination destination : destinations) {
            modules.add(new JBDestinationNode(destination.getName(), destination, lookup));
        }
        return modules;
    }

    public List<MessageDestination> listDestinationForDeployment(String deployment) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<MessageDestination> destinations = new ArrayList<MessageDestination>();
            // ModelNode
            final Object readHornetQServers = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readHornetQServers, getClientConstant(cl, "OP")), getClientConstant(cl, "READ_CHILDREN_NAMES_OPERATION"));

            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "DEPLOYMENT"), deployment);
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
                    destinations.addAll(getJMSDestinationForServerDeployment(deployment, hornetqServerName, Type.QUEUE));
                    destinations.addAll(getJMSDestinationForServerDeployment(deployment, hornetqServerName, Type.TOPIC));
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

    public List<MessageDestination> listDestinations() throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<MessageDestination> destinations = new ArrayList<MessageDestination>();
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

    private List<JBossMessageDestination> getJMSDestinationForServerDeployment(String deployment, String serverName, Type messageType) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<JBossMessageDestination> listedDestinations = new ArrayList<JBossMessageDestination>();
            // ModelNode
            final Object readQueues = createModelNode(cl);
            setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, getClientConstant(cl, "OP")),
                    getModelDescriptionConstant(cl, "READ_CHILDREN_RESOURCES_OPERATION"));

            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "DEPLOYMENT"), deployment);
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
            setModelNodeChildString(cl, getModelNodeChild(cl, readQueues, getClientConstant(cl, "INCLUDE_RUNTIME")), "true");

            // ModelNode
            Object response = executeOnModelNode(cl, readQueues);
            if (isSuccessfulOutcome(cl, response)) {
                // List<ModelNode>
                List destinations = modelNodeAsList(cl, readResult(cl, response));
                for (Object destination : destinations) {
                    Object value = modelNodeAsPropertyForValue(cl, destination);
                    if (modelNodeHasChild(cl, value, "entries")) {
                        List entries = modelNodeAsList(cl, getModelNodeChild(cl, modelNodeAsPropertyForValue(cl, destination), "entries"));
                        for (Object entry : entries) {
                            listedDestinations.add(new JBossMessageDestination(modelNodeAsString(cl, entry), messageType));
                        }
                    } else {
                        listedDestinations.add(new JBossMessageDestination(modelNodeAsPropertyForName(cl, destination), messageType));
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

    private List<JBossMessageDestination> getJMSDestinationForServer(String serverName, Type messageType) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<JBossMessageDestination> listedDestinations = new ArrayList<JBossMessageDestination>();
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
                for (Object destination : destinations) {
                    Object value = modelNodeAsPropertyForValue(cl, destination);
                    if (modelNodeHasChild(cl, value, "entries")) {
                        List entries = modelNodeAsList(cl, getModelNodeChild(cl, modelNodeAsPropertyForValue(cl, destination), "entries"));
                        for (Object entry : entries) {
                            listedDestinations.add(new JBossMessageDestination(modelNodeAsString(cl, entry), messageType));
                        }
                    } else {
                        listedDestinations.add(new JBossMessageDestination(modelNodeAsPropertyForName(cl, destination), messageType));
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

    public boolean addMessageDestinations(final Collection<JBossMessageDestination> destinations) throws IOException {
        boolean result = isServerRunning();
        if (result) {
            for (JBossMessageDestination destination : destinations) {
                result = result && addMessageDestination(destination);
            }
        }
        return result;
    }

    public boolean addMessageDestination(JBossMessageDestination destination) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            LinkedHashMap<Object, Object> values = new LinkedHashMap<Object, Object>();
            values.put(getClientConstant(cl, "SUBSYSTEM"), MESSAGING_SUBSYSTEM);
            values.put("hornetq-server", "default");
            if (destination.getType() == Type.QUEUE) {
                values.put("jms-queue", destination.getName());
            } else {
                values.put("jms-topic", destination.getName());
            }
            Object address = createPathAddressAsModelNode(cl, values);
            Object operation = setModelNodeChild(cl, getModelNodeChild(cl, createAddOperation(cl, address), "entries"), destination.getJndiNames());
            Object response = executeOnOperation(cl, operation);
            return (isSuccessfulOutcome(cl, response));
        } catch (ClassNotFoundException ex) {
            return false;
        } catch (IllegalAccessException ex) {
            return false;
        } catch (NoSuchMethodException ex) {
            return false;
        } catch (InvocationTargetException ex) {
            return false;
        } catch (InstantiationException ex) {
            return false;
        }
    }

    public Collection listEarApplications(Lookup lookup) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List<JBEarApplicationNode> modules = new ArrayList<JBEarApplicationNode>();
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, null);
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true);
            Object response = executeOnModelNode(cl, readDeployments);
            if (isSuccessfulOutcome(cl, response)) {
                Object result = readResult(cl, response);
                List applications = modelNodeAsList(cl, result);
                for (Object application : applications) {
                    String applicationName = modelNodeAsString(cl, getModelNodeChild(cl, readResult(cl, application), getClientConstant(cl, "NAME")));
                    if (applicationName.endsWith(".ear")) {
                        modules.add(new JBEarApplicationNode(applicationName, lookup));
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

    public Collection listEarSubModules(Lookup lookup, String jeeApplicationName) throws IOException {
        try {
            WildFlyDeploymentFactory.WildFlyClassLoader cl = WildFlyDeploymentFactory.getInstance().getWildFlyClassLoader(ip);
            List modules = new ArrayList();
            Object deploymentAddressModelNode = createDeploymentPathAddressAsModelNode(cl, jeeApplicationName);
            Object readDeployments = createReadResourceOperation(cl, deploymentAddressModelNode, true);
            Object response = executeOnModelNode(cl, readDeployments);
            if (isSuccessfulOutcome(cl, response)) {
                String httpPort = ip.getProperty(JBPluginProperties.PROPERTY_PORT);
                Object result = readResult(cl, response);
                List subDeployments = modelNodeAsList(cl, getModelNodeChild(cl, result, "subdeployment"));
                for (Object subDeployment : subDeployments) {
                    String applicationName = modelNodeAsPropertyForName(cl, subDeployment);
                    if (applicationName.endsWith(".war")) {
                        // ModelNode
                        Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, modelNodeAsPropertyForValue(cl, subDeployment), getClientConstant(cl, "SUBSYSTEM")), WEB_SUBSYSTEM);
                        if (modelNodeIsDefined(cl, deployment)) {
                            String url = "http://" + serverAddress + ':' + httpPort + modelNodeAsString(cl, getModelNodeChild(cl, deployment, "context-root"));
                            modules.add(new JBWebModuleNode(applicationName, lookup, url));
                        } else {
                            modules.add(new JBWebModuleNode(applicationName, lookup, null));
                        }
                    } else if (applicationName.endsWith(".jar")) {
                        // ModelNode
                        Object deployment = getModelNodeChild(cl, getModelNodeChild(cl, modelNodeAsPropertyForValue(cl, subDeployment), getClientConstant(cl, "SUBSYSTEM")), EJB3_SUBSYSTEM);
                        if (modelNodeIsDefined(cl, deployment)) {
                            List<WildflyEJBComponentNode> ejbs = new ArrayList<WildflyEJBComponentNode>();
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEJBComponentNode.Type.ENTITY));
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEJBComponentNode.Type.MDB));
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEJBComponentNode.Type.SINGLETON));
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEJBComponentNode.Type.STATEFULL));
                            ejbs.addAll(listEJBs(cl, deployment, WildflyEJBComponentNode.Type.STATELESS));
                            modules.add(new WildflyEjbModuleNode(applicationName, lookup, ejbs, true));
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

    private List<WildflyEJBComponentNode> listEJBs(WildFlyDeploymentFactory.WildFlyClassLoader cl,
            Object deployment, WildflyEJBComponentNode.Type type) throws IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        List<WildflyEJBComponentNode> modules = new ArrayList<WildflyEJBComponentNode>();
        if (modelNodeHasDefinedChild(cl, deployment, type.getPropertyName())) {
            List ejbs = modelNodeAsList(cl, getModelNodeChild(cl, deployment, type.getPropertyName()));
            for (Object ejb : ejbs) {
                modules.add(new WildflyEJBComponentNode(modelNodeAsPropertyForName(cl, ejb), type));
            }
        }
        return modules;
    }
}

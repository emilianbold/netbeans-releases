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
 * Contributor(s):
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
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.netbeans.modules.j2ee.weblogic9.WLConnectionSupport;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.ResourceNode.ResourceNodeType;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.UnregisterCookie;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class JdbcRetriever {
    
    private static final Logger LOGGER = Logger.getLogger(JdbcRetriever.class.getName());
    private static final int WAIT_TIME = 60000;
    
    private static final int TIMEOUT = 120000;
    
    enum JdbcNodeTypes {
        RESOURCES,
        POOL;
    }
    
    JdbcRetriever(Lookup lookup) {
        data = new AtomicReference<List<JDBCDataBean>>();
        this.lookup = lookup;
    }
    

    Node createJDBCPoolsNode() {
        JDBCResourcesChildFactory factory = new JDBCResourcesChildFactory( 
                JdbcNodeTypes.POOL);
        return new ResourceNode( factory, ResourceNodeType.JDBC_POOL,
                NbBundle.getMessage(JdbcRetriever.class, "LBL_JDBCPools"));
    }

    Node createJDBCResourcesNode() {
        JDBCResourcesChildFactory factory = new JDBCResourcesChildFactory(
                JdbcNodeTypes.RESOURCES );
        return new ResourceNode(factory,ResourceNodeType.JDBC_RESOURCES ,
                NbBundle.getMessage(JdbcRetriever.class, "LBL_JDBCResources"));
    }
    
    void clean() {
        data.set(null);
    }
    
    private void retrieve(){
        synchronized (this){
            if ( isRetrieveStarted ){
                return;
            }
            isRetrieveStarted = true;
        }
        data.set(null);
        WLDeploymentManager manager = lookup.lookup(WLDeploymentManager.class);

        WLConnectionSupport support = new WLConnectionSupport(manager);
        List<JDBCDataBean> list = Collections.emptyList();

        try {
            list = support.executeAction(new WLConnectionSupport.
                    JMXDomainRuntimeServiceAction<List<JDBCDataBean>>() {

                @Override
                public List<JDBCDataBean> call(MBeanServerConnection con) throws Exception {
                    List<JDBCDataBean> list = new LinkedList<JDBCDataBean>();
                    
                    ObjectName service = getRootService();
                    
                    ObjectName[] adminServers = (ObjectName[]) con
                            .getAttribute(service, "ServerRuntimes");    // NOI18N
                    Set<String> adminNames = new HashSet<String>();
                    for (ObjectName adminServer : adminServers) {
                        adminNames.add(con
                                .getAttribute(adminServer, "Name")
                                    .toString());// NOI18N
                    }

                    ObjectName objectName = (ObjectName) con.getAttribute(
                            service, "DomainConfiguration");            // NOI18N
                    ObjectName objectNames[] = (ObjectName[]) con
                            .getAttribute(objectName, "SystemResources"); // NOI18N

                    for (ObjectName resource : objectNames) {
                        String type = con.getAttribute(resource, "Type")
                                .toString();// NOI18N
                        if ("JDBCSystemResource".equals(type)) { // NOI18N
                            ObjectName dataSource = (ObjectName) con
                                    .getAttribute(resource, "JDBCResource"); // NOI18N
                            ObjectName[] targets = (ObjectName[]) con
                                    .getAttribute(resource, "Targets"); // NOI18N

                            String name = con.getAttribute(dataSource,
                                    "Name").toString(); // NOI18N
                            boolean foundAdminServer = false;
                            for (ObjectName target : targets) {
                                String targetServer = con.getAttribute(
                                        target, "Name").toString(); // NOI18N
                                if (adminNames.contains(targetServer)) {
                                    foundAdminServer = true;
                                }
                            }
                            if (!foundAdminServer) {
                                continue;
                            }

                            ObjectName dataSourceParams = (ObjectName) con
                                    .getAttribute(dataSource,
                                            "JDBCDataSourceParams"); // NOI18N
                            String jndiNames[] = (String[]) con
                                    .getAttribute(dataSourceParams,
                                            "JNDINames"); // NOI18N
                            JDBCDataBean bean = new JDBCDataBean(name,
                                    jndiNames);
                            list.add(bean);
                        }
                    }
                    return list;
                }

            });
        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
        } finally {
            data.compareAndSet(null , list);
            synchronized (this) {
                isRetrieveStarted = false;
                notifyAll();
            }
        }
    }
    
    private class JDBCResourcesChildFactory extends ChildFactory<ResourceNode> 
        implements RefreshModulesCookie 
    {
        
        JDBCResourcesChildFactory(JdbcNodeTypes type){
            this.type = type;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesCookie#refresh()
         */
        @Override
        public final void refresh() {
            clean();
            refresh(false);
        }

        /* (non-Javadoc)
         * @see org.openide.nodes.ChildFactory#createKeys(java.util.List)
         */
        @Override
        protected boolean createKeys( List<ResourceNode> children ) {
            synchronized (JdbcRetriever.this) {
                while (isRetrieveStarted) {
                    try {
                        JdbcRetriever.this.wait();
                    }
                    catch (InterruptedException e) {
                    }
                }
            }
            List<JDBCDataBean> jdbcDataBeans = data.get();
            if ( jdbcDataBeans != null ){
                if (type == JdbcNodeTypes.POOL) {
                    for (JDBCDataBean jdbcDataBean : jdbcDataBeans) {
                        children.add(new ResourceNode(Children.LEAF, 
                                ResourceNodeType.JDBC,jdbcDataBean.getName(), 
                                new UnregisterJdbcPool( jdbcDataBean.getName(),
                                        this , lookup ) ));
                    }
                }
                else if (type == JdbcNodeTypes.RESOURCES) {
                    for (JDBCDataBean jdbcDataBean : jdbcDataBeans) {
                        String[] jndiNames = jdbcDataBean.getJndiNames();
                        for (String name : jndiNames) {
                            children.add(new ResourceNode(Children.LEAF, 
                                    ResourceNodeType.JDBC, name, 
                                    new UnregisterJdbcJndiName( name ,
                                            this , lookup) ));
                        }
                    }
                }
                return true;
            }
            retrieve();
            return false;
        }
        
        /* (non-Javadoc)
         * @see org.openide.nodes.ChildFactory#createNodeForKey(java.lang.Object)
         */
        @Override
        protected Node createNodeForKey( ResourceNode key ) {
            return key;
        }
        
        private JdbcNodeTypes type ;
    }
    
    private static class UnregisterJdbcJndiName implements UnregisterCookie {

        UnregisterJdbcJndiName( String jndiName , 
                RefreshModulesCookie cookie, Lookup lookup ) 
        {
            this.jndiName = jndiName;
            this.cookie = cookie;
            this.lookup = lookup;
        }

        @Override
        public void unregister() {
            WLDeploymentManager manager = lookup
                    .lookup(WLDeploymentManager.class);

            WLConnectionSupport support = new WLConnectionSupport(manager);
            try {
                support.executeAction(new WLConnectionSupport.JMXEditAction<Void>() {

                    @Override
                    public Void call(MBeanServerConnection con) throws Exception {
                        ObjectName service = getRootService();

                        ObjectName config = (ObjectName) con.getAttribute(
                                service, "DomainConfiguration"); // NOI18N
                        ObjectName resources[] = (ObjectName[]) con
                                .getAttribute(config, "SystemResources"); // NOI18N

                        ObjectName manager = (ObjectName) con.getAttribute(
                                service, "ConfigurationManager"); // NOI18N
                        ObjectName domainConfigRoot = (ObjectName) con.invoke(
                                manager, "startEdit", new Object[] {
                                        WAIT_TIME, TIMEOUT }, new String[] {
                                "java.lang.Integer", "java.lang.Integer" });
                        if ( domainConfigRoot == null ){
                            // Couldn't get the lock
                            throw new UnableLockException();
                        }

                        for (ObjectName resource : resources) {
                            String type = con
                                    .getAttribute(resource, "Type")
                                    .toString();// NOI18N
                            if ("JDBCSystemResource".equals(type)) { // NOI18N
                                ObjectName jdbcResource = (ObjectName) con
                                        .getAttribute(resource,
                                                "JDBCResource"); // NOI18N
                                ObjectName params = (ObjectName) con
                                        .getAttribute(jdbcResource,
                                                "JDBCDataSourceParams"); // NOI18N
                                con.invoke(params, "removeJNDIName",
                                        new Object[] { jndiName },
                                        new String[] { "java.lang.String" }); // NOI18N
                            }
                        }
                        con.invoke(manager, "save", null, null); // NOI18N
                        ObjectName activationTask = (ObjectName) con
                                .invoke(manager, "activate",
                                        new Object[] { TIMEOUT },
                                        new String[] { "java.lang.Long" }); // NOI18N
                        con.invoke(activationTask, "waitForTaskCompletion", null, null);
                        return null;
                    }


                });
            }
            catch (UnableLockException e ){
                failNotify();
            }
            catch ( MBeanException e ){
                Exception targetException = e.getTargetException();
                if ( targetException.getClass().getCanonicalName().equals(
                        "weblogic.management.mbeanservers.edit.EditTimedOutException"))
                {
                    failNotify();
                }
            }
            catch (Exception e) {
                LOGGER.log(Level.INFO, null, e);
            }
            cookie.refresh();
        }
        
        private void failNotify(){
            NotifyDescriptor notDesc = new NotifyDescriptor.Message(
                    NbBundle.getMessage(JdbcRetriever.class, "MSG_UnableUnregister"),
                    NotifyDescriptor.ERROR_MESSAGE );
            DialogDisplayer.getDefault().notify(notDesc);
        }
        
        private String jndiName;
        private RefreshModulesCookie cookie;
        private Lookup lookup;
    }
    
    private static class UnregisterJdbcPool implements UnregisterCookie {

        UnregisterJdbcPool( String dataSourceName , 
                RefreshModulesCookie cookie, Lookup lookup ) 
        {
            this.dataSource = dataSourceName;
            this.cookie = cookie;
            this.lookup = lookup;
        }

        @Override
        public void unregister() {
            WLDeploymentManager manager = lookup
                    .lookup(WLDeploymentManager.class);

            WLConnectionSupport support = new WLConnectionSupport(manager);
            try {
                support.executeAction(new WLConnectionSupport.JMXEditAction<Void>() {

                    @Override
                    public Void call(MBeanServerConnection con) throws Exception {
                        StringBuilder dataSourceCanonicalName = new StringBuilder(
                                "com.bea:Name="); // NOI18N
                        dataSourceCanonicalName.append(dataSource);
                        dataSourceCanonicalName
                                .append(",Type=JDBCSystemResource");// NOI18N
                        ObjectName dataSource = new ObjectName(
                                dataSourceCanonicalName.toString());
                        remove(con, dataSource);
                        return null;
                    }
                    
                    private void remove( MBeanServerConnection connection,
                            ObjectName dataSource ) throws AttributeNotFoundException,
                            InstanceNotFoundException, MBeanException, ReflectionException,
                            IOException, MalformedObjectNameException, UnableLockException
                    {
                        ObjectName service = getRootService();
                        ObjectName manager =(ObjectName) connection.getAttribute(service, 
                                        "ConfigurationManager");                // NOI18N
                        ObjectName domainConfigRoot = (ObjectName)connection.invoke(manager, 
                                "startEdit", new Object[]{ WAIT_TIME, TIMEOUT}, 
                                    new String[]{ "java.lang.Integer", "java.lang.Integer"});
                        if ( domainConfigRoot == null ){
                         // Couldn't get the lock
                            throw new UnableLockException();
                        }
                        
                        ObjectName targets[] = (ObjectName[]) connection.getAttribute(
                                dataSource, "Targets"); // NOI18N
                        for (ObjectName target : targets) {
                            connection
                                    .invoke(dataSource,
                                            "removeTarget",
                                            new Object[] { target },
                                            new String[] { "javax.management.ObjectName" }); // NOI18N
                        }
                        connection.invoke(manager, "save", null, null);                // NOI18N
                        ObjectName  activationTask = (ObjectName)connection.invoke(manager, 
                                "activate", new Object[]{TIMEOUT}, 
                                    new String[]{"java.lang.Long"});                // NOI18N
                        connection.invoke(activationTask, "waitForTaskCompletion", null, null);
                    }


                });
            }
            catch ( UnableLockException e ){
                failNotify();
            }
            catch ( MBeanException e ){
                Exception targetException = e.getTargetException();
                if ( targetException.getClass().getCanonicalName().equals(
                        "weblogic.management.mbeanservers.edit.EditTimedOutException"))
                {
                    failNotify();
                }
            }
            catch (Exception e) {
                LOGGER.log(Level.INFO, null, e);
            }
            cookie.refresh();
        }
        
        private void failNotify(){
            NotifyDescriptor notDesc = new NotifyDescriptor.Message(
                    NbBundle.getMessage(JdbcRetriever.class, "MSG_UnableUnregister"),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(notDesc);
        }
        
        private String dataSource;
        private RefreshModulesCookie cookie;
        private Lookup lookup;
    }
    
    private static class JDBCDataBean {
        JDBCDataBean( String poolName , String[] jndiNames ){
            name = poolName;
            this.jndiNames = jndiNames;
        }
        
        String getName(){
            return name;
        }
        
        String[] getJndiNames(){
            return jndiNames;
        }
        
        private String name;
        private String  jndiNames[];
    }
    
    private static class UnableLockException extends Exception {

        private static final long serialVersionUID = 1491526792800773444L;

    }
    

    private AtomicReference<List<JDBCDataBean>> data;
    private boolean isRetrieveStarted;
    private Lookup lookup;
}

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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.WLClassLoaderSupport;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.ResourceNode.ResourceNodeType;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesCookie;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class JDBCRetriever {
    
    private static final Logger LOGGER = Logger.getLogger(JDBCRetriever.class.getName());
    
    enum JdbcNodeTypes {
        RESOURCES,
        POOL;
    }
    
    JDBCRetriever(Lookup lookup) {
        data = new AtomicReference<List<JDBCDataBean>>();
        this.lookup = lookup;
    }
    

    Node createJDBCPoolsNode() {
        JDBCResourcesChildFactory factory = new JDBCResourcesChildFactory( 
                JdbcNodeTypes.POOL);
        return new ResourceNode( factory, ResourceNodeType.JDBC_POOL,
                NbBundle.getMessage(JDBCRetriever.class, "LBL_JDBCPools"));
    }

    Node createJDBCResourcesNode() {
        JDBCResourcesChildFactory factory = new JDBCResourcesChildFactory(
                JdbcNodeTypes.RESOURCES);
        return new ResourceNode(factory,ResourceNodeType.JDBC_RESOURCES ,
                NbBundle.getMessage(JDBCRetriever.class, "LBL_JDBCResources"));
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
        final StringBuilder builder  = new StringBuilder("service:jmx:iiop://");  // NOI18N
        WLDeploymentManager manager = lookup.lookup(WLDeploymentManager.class);
        InstanceProperties instanceProperties = manager.getInstanceProperties();
        String host = instanceProperties.getProperty(WLPluginProperties.HOST_ATTR);
        String port = instanceProperties.getProperty(WLPluginProperties.PORT_ATTR);
        if ( (host== null || host.trim().length() ==0 &&  
                (port== null || port.trim().length() ==0 )))
        {
            Properties domainProperties = WLPluginProperties.getDomainProperties( 
                    instanceProperties.getProperty( WLPluginProperties.DOMAIN_ROOT_ATTR));
            host = domainProperties.getProperty(WLPluginProperties.HOST_ATTR);
            port = domainProperties.getProperty(WLPluginProperties.PORT_ATTR);
        }
        builder.append( host.trim() );
        builder.append(":");            //  NOI18N
        builder.append(port.trim());
        builder.append( "/jndi/weblogic.management.mbeanservers.domainruntime");//  NOI18N
        
        final HashMap<String, String> env = new HashMap<String, String>();
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, 
                     "weblogic.management.remote");         // NOI18M
        env.put(javax.naming.Context.SECURITY_PRINCIPAL, manager.
                getInstanceProperties().getProperty(
                        InstanceProperties.USERNAME_ATTR).toString());
        env.put(javax.naming.Context.SECURITY_CREDENTIALS, manager.
                getInstanceProperties().getProperty(
                        InstanceProperties.PASSWORD_ATTR).toString());

        WLClassLoaderSupport support = new WLClassLoaderSupport(manager);
        List<JDBCDataBean> list = Collections.emptyList();

        try {
            list = support.executeAction(new Callable<List<JDBCDataBean>>() {

                @Override
                public List<JDBCDataBean> call() throws Exception {
                    JMXServiceURL serviceUrl = new JMXServiceURL(builder.toString());
                    JMXConnector jmxConnector = JMXConnectorFactory.newJMXConnector(
                            serviceUrl, env);
                    jmxConnector.connect();
                    MBeanServerConnection con = jmxConnector.getMBeanServerConnection();

                    ObjectName service = new ObjectName(
                            "com.bea:Name=DomainRuntimeService,"
                                    + "Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");//  NOI18N
                    ObjectName objectName  = (ObjectName)con.getAttribute(service,
                            "DomainConfiguration");                         //  NOI18N
                    ObjectName objectNames[]  = (ObjectName[] )con.getAttribute(objectName,
                            "SystemResources");                             //  NOI18N

                    List<JDBCDataBean> list = new LinkedList<JDBCDataBean>();
                    for (ObjectName resource : objectNames) {
                        String type = con.getAttribute(resource, "Type").toString();//  NOI18N
                        if ( "JDBCSystemResource".equals(type)){            //  NOI18N
                            ObjectName dataSource = (ObjectName)con.getAttribute(
                                    resource, "JDBCResource");              //  NOI18N
                            String name = con.getAttribute(dataSource,
                                     "Name").toString();                    //  NOI18N
                             ObjectName dataSourceParams = (ObjectName)con.getAttribute(dataSource,
                                     "JDBCDataSourceParams");               //  NOI18N
                             String jndiNames[] = (String[])con.getAttribute(dataSourceParams,
                                     "JNDINames");                          //  NOI18N
                             JDBCDataBean bean = new JDBCDataBean(name, jndiNames);
                             list.add( bean );
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
            refresh(false);
        }

        /* (non-Javadoc)
         * @see org.openide.nodes.ChildFactory#createKeys(java.util.List)
         */
        @Override
        protected boolean createKeys( List<ResourceNode> children ) {
            synchronized (JDBCRetriever.this) {
                while (isRetrieveStarted) {
                    try {
                        JDBCRetriever.this.wait();
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
                                ResourceNodeType.JDBC,jdbcDataBean.getName()));
                    }
                }
                else if (type == JdbcNodeTypes.RESOURCES) {
                    for (JDBCDataBean jdbcDataBean : jdbcDataBeans) {
                        String[] jndiNames = jdbcDataBean.getJndiNames();
                        for (String name : jndiNames) {
                            children.add(new ResourceNode(Children.LEAF, 
                                    ResourceNodeType.JDBC, name));
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
    

    private AtomicReference<List<JDBCDataBean>> data;
    private boolean isRetrieveStarted;
    private Lookup lookup;
}

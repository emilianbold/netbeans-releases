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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.netbeans.modules.j2ee.weblogic9.WLConnectionSupport;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.ResourceNode.ResourceNodeType;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.UnregisterCookie;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author ads
 *
 */
class JdbcRetriever {
    
    private static final String JDBC = "jdbc";                  // NOI18N
    
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

        WLConnectionSupport support = manager.getConnectionSupport();
        List<JDBCDataBean> list = Collections.emptyList();

        try {
            list = support.executeAction(new WLConnectionSupport.
                    JMXRuntimeAction<List<JDBCDataBean>>() {

                @Override
                public List<JDBCDataBean> call(MBeanServerConnection con, ObjectName service) throws Exception {
                    List<JDBCDataBean> list = new LinkedList<JDBCDataBean>();
                    
                    ObjectName[] adminServers = (ObjectName[]) con
                            .getAttribute(service, "ServerRuntimes");    // NOI18N
                    Set<String> adminNames = new HashSet<String>();
                    for (ObjectName adminServer : adminServers) {
                        adminNames.add(con
                                .getAttribute(adminServer, "Name")
                                    .toString());// NOI18N
                    }

                    ObjectName config = (ObjectName) con.getAttribute(
                            service, "DomainConfiguration");            // NOI18N
                    findSystemJdbc(con, list, adminNames, config);
                    findDeployedJdbc( con, list , adminNames, config);
                    return list;
                }

                private void findDeployedJdbc( MBeanServerConnection con,
                        List<JDBCDataBean> list, Set<String> adminNames,
                        ObjectName config ) throws MBeanException,
                        AttributeNotFoundException, InstanceNotFoundException,
                        ReflectionException, IOException
                {
                    ObjectName applications[] = (ObjectName[]) con
                        .getAttribute(config, "AppDeployments");                    // NOI18N
                    for (ObjectName application : applications) {
                        String type = con.getAttribute( application, "ModuleType"). // NOI18N
                            toString();
                        if ( type.equals(JDBC)){
                            boolean foundAdminServer = false;
                            ObjectName[] targets = (ObjectName[]) con
                                .getAttribute(application, "Targets");  // NOI18N
                            for (ObjectName target : targets) {
                                String targetServer = con.getAttribute(
                                        target, "Name").toString();     // NOI18N
                                if (adminNames.contains(targetServer)) {
                                    foundAdminServer = true;
                                }
                            }
                            if (!foundAdminServer) {
                                continue;
                            }
                            String path = (String)con.getAttribute( application, 
                                    "AbsoluteSourcePath");              // NOI18N
                            String name = (String)con.getAttribute( application, 
                                    "Name");                            // NOI18N
                            if ( path != null ){
                                addDeployedDataSource( path , list , name );
                            }
                        }
                    }
                    
                }

                private void addDeployedDataSource( String path,
                        List<JDBCDataBean> list , String deplName )
                {
                    try {
                        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                        JdbcConfigHandler handler = new JdbcConfigHandler();
                        FileObject jdbcConfig = FileUtil.toFileObject( FileUtil.
                                normalizeFile( new File(path)));
                        parser.parse(new BufferedInputStream(
                                jdbcConfig.getInputStream()), handler);
                        List<String> jndiNames = handler.getJndiNames();
                        list.add( new JDBCDataBean( handler.getName(), 
                                jndiNames.toArray(new String[jndiNames.size()]), deplName));
                    }
                    catch (ParserConfigurationException e) {
                        LOGGER.log(Level.INFO, null, e);
                    }
                    catch (SAXException e) {
                        LOGGER.log(Level.INFO, null, e);
                    }
                    catch (FileNotFoundException e) {
                        LOGGER.log(Level.INFO, null, e);
                    }
                    catch (IOException e) {
                        LOGGER.log(Level.INFO, null, e);
                    }
                }

                private void findSystemJdbc( MBeanServerConnection con,
                        List<JDBCDataBean> list, Set<String> adminNames,
                        ObjectName objectName ) throws MBeanException,
                        AttributeNotFoundException, InstanceNotFoundException,
                        ReflectionException, IOException
                {
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
                        String name = jdbcDataBean.getName();
                        if ( jdbcDataBean.isApplication() ){
                            name = jdbcDataBean.getDeploymentName();
                        }
                        children.add(new ResourceNode(Children.LEAF, 
                                ResourceNodeType.JDBC,jdbcDataBean.getName(), 
                                new UnregisterJdbcPool( name,
                                        this , lookup ) ));
                    }
                }
                else if (type == JdbcNodeTypes.RESOURCES) {
                    for (JDBCDataBean jdbcDataBean : jdbcDataBeans) {
                        String[] jndiNames = jdbcDataBean.getJndiNames();
                        boolean isApplication = jdbcDataBean.isApplication();
                        for (String name : jndiNames) {
                            // no "unregister" action if jdbc data source is deployed application 
                            ResourceNode node = new ResourceNode(Children.LEAF, 
                                    ResourceNodeType.JDBC, name, isApplication ? null :  
                                    new UnregisterJdbcJndiName( name ,
                                            this , lookup) );
                            children.add( node );
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

            WLConnectionSupport support = manager.getConnectionSupport();
            try {
                support.executeAction(new WLConnectionSupport.JMXEditAction<Void>() {

                    @Override
                    public Void call(MBeanServerConnection con, ObjectName service) throws Exception {
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

        UnregisterJdbcPool( String name , 
                RefreshModulesCookie cookie, Lookup lookup ) 
        {
            this.resourceName = name;
            this.cookie = cookie;
            this.lookup = lookup;
        }

        @Override
        public void unregister() {
            WLDeploymentManager manager = lookup
                    .lookup(WLDeploymentManager.class);

            WLConnectionSupport support = manager.getConnectionSupport();
            try {
                support.executeAction(new WLConnectionSupport.JMXEditAction<Void>() {

                    @Override
                    public Void call(MBeanServerConnection con, ObjectName service) throws Exception {
                        StringBuilder dataSourceCanonicalName = new StringBuilder(
                                "com.bea:Name="); // NOI18N
                        dataSourceCanonicalName.append(resourceName);
                        dataSourceCanonicalName
                                .append(",Type=JDBCSystemResource");// NOI18N
                        ObjectName dataSourceBean = new ObjectName(
                                dataSourceCanonicalName.toString());
                        disable(con, service, dataSourceBean);
                        return null;
                    }
                    
                    private void disable( MBeanServerConnection connection,
                            ObjectName service, ObjectName dataSourceBean) 
                            throws AttributeNotFoundException,
                            InstanceNotFoundException, MBeanException, ReflectionException,
                            IOException, MalformedObjectNameException, UnableLockException
                    {
                        ObjectName manager =(ObjectName) connection.getAttribute(service, 
                                        "ConfigurationManager");                // NOI18N
                        ObjectName domainConfigRoot = (ObjectName)connection.invoke(manager, 
                                "startEdit", new Object[]{ WAIT_TIME, TIMEOUT}, 
                                    new String[]{ "java.lang.Integer", "java.lang.Integer"});
                        if ( domainConfigRoot == null ){
                         // Couldn't get the lock
                            throw new UnableLockException();
                        }
                        
                        try {
                            ObjectName targets[]  = (ObjectName[]) connection.getAttribute(
                                dataSourceBean, "Targets"); // NOI18N
                            for (ObjectName target : targets) {
                                connection
                                        .invoke(dataSourceBean,
                                                "removeTarget",
                                                new Object[] { target },
                                                new String[] { "javax.management.ObjectName" }); // NOI18N
                            }
                        }
                        catch( InstanceNotFoundException e ){
                            /*
                             *  This is not system config JDBC resource bean. This is
                             *  deployed JDBC resource  .
                             */
                            StringBuilder deploymentCanonicalName = 
                                new StringBuilder("com.bea:Name=");       // NOI18N
                            deploymentCanonicalName.append( resourceName);
                            deploymentCanonicalName
                                .append(",Type=AppDeployment");             // NOI18N
                            ObjectName application = new ObjectName( 
                                    deploymentCanonicalName.toString());
                            ObjectName targets[]  = (ObjectName[]) connection.getAttribute(
                                    application, "Targets"); // NOI18N
                            for (ObjectName target : targets) {
                                connection
                                        .invoke(application,
                                                "removeTarget",
                                                new Object[] { target },
                                                new String[] { "javax.management.ObjectName" }); // NOI18N
                            }
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
        
        private String resourceName;
        private RefreshModulesCookie cookie;
        private Lookup lookup;
    }
    
    private static class JDBCDataBean {
        JDBCDataBean( String poolName , String[] jndiNames ){
            this( poolName , jndiNames , null);
        }
        
        JDBCDataBean( String poolName , String[] jndiNames , String deploymentName){
            name = poolName;
            this.jndiNames = jndiNames;
            this.deploymentName = deploymentName;
        }
        
        String getName(){
            return name;
        }
        
        String[] getJndiNames(){
            return jndiNames;
        }
        
        boolean isApplication(){
            return deploymentName != null;
        }
        
        String getDeploymentName(){
            return deploymentName; 
        }
        
        private String name;
        private String  jndiNames[];
        private String deploymentName;
    }
    
    private static class UnableLockException extends Exception {

        private static final long serialVersionUID = 1491526792800773444L;

    }
    
    private static class JdbcConfigHandler extends DefaultHandler {
        private static final String DATA_SOURCE_PARAMS = "jdbc-data-source-params";
        
        @Override
        public void startElement( String uri, String localName, String qName,
                Attributes attributes ) throws SAXException
        {
            content = null;
            if ( DATA_SOURCE_PARAMS.equals(getUnprefixedName(qName))){        // NOI18N
                dataSourceParamsStarted = true;
            }
        }
        
        @Override
        public void endElement( String uri, String localName, String qName )
                throws SAXException
        {
            if ( name == null && "name".equals(getUnprefixedName(qName))){         // NOI18N
                name = content;
            }
            else if ( DATA_SOURCE_PARAMS.equals(getUnprefixedName(qName))){        // NOI18N
                dataSourceParamsStarted = false;
            }
            else if ( dataSourceParamsStarted && "jndi-name".equals(//NOI18N
                    getUnprefixedName(qName)))
            {
                jndiNames.add( content );
            }
        }
        
        @Override
        public void characters(char[] ch, int start, int length) {
            content = new String(ch, start, length);
        }
        
        String getName(){
            return name;
        }
        
        List<String> getJndiNames(){
            return jndiNames;
        }
        
        private String getUnprefixedName( String name ){
            if ( name.contains(":")){
                return name.substring(name.indexOf(":")+1);
            }
            else {
                return name;
            }
        }
        
        private String content;
        private String name;
        private boolean dataSourceParamsStarted;
        private List<String> jndiNames = new LinkedList<String>();
    }
    

    private AtomicReference<List<JDBCDataBean>> data;
    private boolean isRetrieveStarted;
    private Lookup lookup;
}

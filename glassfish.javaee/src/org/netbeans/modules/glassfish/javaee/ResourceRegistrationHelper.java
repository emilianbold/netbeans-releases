/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.ServerCommand;
import org.netbeans.modules.glassfish.spi.ServerCommand.GetPropertyCommand;
import org.netbeans.modules.glassfish.spi.ServerCommand.SetPropertyCommand;
import org.netbeans.modules.glassfish.spi.TreeParser;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author vkraemer
 */
public class ResourceRegistrationHelper {
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
    private static final int TIMEOUT = 2000;

    private ResourceRegistrationHelper() {
    }

    public static void deployResources(File root, Hk2DeploymentManager dm) {
        Set<File> resourceDirs = getResourceDirs(root);
        deployResources(resourceDirs,dm);
    }

    private static void deployResources(Set<File> resourceDirs, Hk2DeploymentManager dm)  {
        for(File resourceDir: resourceDirs) {
            try {
                registerResourceDir(resourceDir,dm);
            } catch (ConfigurationException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, "some data sources may not be deployed", ex);
            }
        }
    }

    private static Set<File> getResourceDirs(File file){
        Set<File> retVal = new TreeSet<File>();
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        SourceFileMap sourceFileMap = SourceFileMap.findSourceMap(fo);
        if (sourceFileMap != null) {
            retVal.addAll(Arrays.asList(sourceFileMap.getEnterpriseResourceDirs()));
        }
        return retVal;
    }

    private static boolean registerResourceDir(File resourceDir, Hk2DeploymentManager dm) throws ConfigurationException {
        boolean succeeded = false;
        File sunResourcesXml = new File(resourceDir, "sun-resources.xml"); //NOI18N
        if(sunResourcesXml.exists()) {
            checkUpdateServerResources(sunResourcesXml, dm);
            GlassfishModule commonSupport = dm.getCommonServerSupport();
            AddResourcesCommand cmd = new AddResourcesCommand(sunResourcesXml.getAbsolutePath());
            Future<OperationState> result = commonSupport.execute(cmd);
            try {
                if(result.get(TIMEOUT, TIMEOUT_UNIT) == OperationState.COMPLETED) {
                    succeeded = true;
                }
            } catch (TimeoutException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
                throw new ConfigurationException(ex.getLocalizedMessage(), ex);
            } catch (InterruptedException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
                throw new ConfigurationException(ex.getLocalizedMessage(), ex);
            } catch (ExecutionException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
                throw new ConfigurationException(ex.getLocalizedMessage(), ex);
            }
        }
        return succeeded;
    }

    private static void checkUpdateServerResources(File sunResourcesXml, Hk2DeploymentManager dm){
          Map<String, String> changedData = new HashMap<String, String>();
          List<TreeParser.Path> pathList = new ArrayList<TreeParser.Path>();
          ResourceFinder cpFinder = new ResourceFinder("name"); // NOI18N
          pathList.add(new TreeParser.Path("/resources/jdbc-connection-pool", cpFinder)); // NOI18N
          ResourceFinder jdbcFinder = new ResourceFinder("jndi-name"); // NOI18N
          pathList.add(new TreeParser.Path("/resources/jdbc-resource", jdbcFinder)); // NOI18N
          ResourceFinder connectorPoolFinder = new ResourceFinder("name"); // NOI18N
          pathList.add(new TreeParser.Path("/resources/connector-connection-pool", connectorPoolFinder)); // NOI18N
          ResourceFinder connectorFinder = new ResourceFinder("jndi-name"); // NOI18N
          pathList.add(new TreeParser.Path("/resources/connector-resource", connectorFinder)); // NOI18N
          ResourceFinder aoFinder = new ResourceFinder("jndi-name"); // NOI18N
          pathList.add(new TreeParser.Path("/resources/admin-object-resource", aoFinder)); // NOI18N
          ResourceFinder mailFinder = new ResourceFinder("jndi-name"); // NOI18N
          pathList.add(new TreeParser.Path("/resources/mail-resource", mailFinder)); // NOI18N
                    
          try {
            TreeParser.readXml(sunResourcesXml, pathList);
          } catch (IllegalStateException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
          }
          Map<String, String> allRemoteData = getResourceData("resources.*", dm); // NOI18N
          changedData = checkResources(cpFinder, "resources.jdbc-connection-pool.", allRemoteData, changedData, dm); // NOI18N
          changedData = checkResources(jdbcFinder, "resources.jdbc-resource.", allRemoteData, changedData, dm); // NOI18N
          changedData = checkResources(connectorPoolFinder, "resources.connector-connection-pool.", allRemoteData, changedData, dm); // NOI18N
          changedData = checkResources(connectorFinder, "resources.connector-resource.", allRemoteData, changedData, dm); // NOI18N
          changedData = checkResources(aoFinder, "resources.admin-object-resource.", allRemoteData, changedData, dm); // NOI18N
          changedData = checkResources(mailFinder, "resources.mail-resource.", allRemoteData, changedData, dm); // NOI18N

          if(changedData.size() > 0) {
            putResourceData(changedData, dm);
          }
    }

    private static Map<String, String> checkResources(ResourceFinder resourceFinder, String prefix, Map<String, String> allRemoteData, Map<String, String> changedData, Hk2DeploymentManager dm) {
        List<String> resources = resourceFinder.getResourceNames();
        for (int i = 0; i < resources.size(); i++) {
            String jndiName = resources.get(i);
            Map<String, String> localData = resourceFinder.getResourceData().get(jndiName);
            String remoteKey = prefix + jndiName + "."; // NOI18N
            Map<String, String> remoteData = new HashMap<String, String>();
            Iterator itr = allRemoteData.keySet().iterator();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                if(key.startsWith(remoteKey)){
                    remoteData.put(key, allRemoteData.get(key));
                }
            }
            if (remoteData.size() > 0) {
                changedData = getChangedData(remoteData, localData, changedData, remoteKey);
            }
        }
        return changedData;
    }
    
    private static Map<String, String> getChangedData(Map<String, String> remoteData, Map<String, String> localData, Map<String, String> changedData, String resourceKey) {
        List<String> props = new ArrayList<String>();
        Iterator<String> keys = remoteData.keySet().iterator();
        Set<String> localKeySet = localData.keySet();
        while (keys.hasNext()) {
            String remoteDataKey = keys.next();
            String remoteValue = remoteData.get(remoteDataKey);
            String[] split = remoteDataKey.split(resourceKey);
            String key = split[1];
            if (key.indexOf("property.") != -1) { // NOI18N
                props.add(key);
            }
            String localValue = (String) localData.get(key);
            if (localValue != null) {
                if ((remoteValue == null) || ((remoteValue != null) && (!localValue.equals(remoteValue)))) {
                    changedData.put(remoteDataKey, localValue);
                }
            } else {
                if (localKeySet.contains(key)) {
                    if (remoteValue != null) {
                        changedData.put(remoteDataKey, localValue);
                    }
                }
            }
        }
        keys = localData.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.indexOf("property.") != -1) { // NOI18N
                if (!props.contains(key)) {
                    String remoteKey = resourceKey + key;
                    changedData.put(remoteKey, localData.get(key));
                }
            }
        }
        return changedData;
    }

    public static final class AddResourcesCommand extends ServerCommand {

        public AddResourcesCommand(String sunResourcesXmlPath) {
            super("add-resources"); // NOI18N
            query = "xml_file_name=" + sunResourcesXmlPath; // NOI18N
        }

    }

    public static Map<String, String> getResourceData(String query, Hk2DeploymentManager dm) {
        try {
            GetPropertyCommand cmd = new ServerCommand.GetPropertyCommand(query); 
            Future<OperationState> task = dm.getCommonServerSupport().execute(cmd);
            OperationState state = task.get();
            if (state == OperationState.COMPLETED) {
                Map<String,String> retVal = cmd.getData();
                if (retVal.isEmpty())
                    Logger.getLogger("glassfish-javaee").log(Level.INFO, null, new IllegalStateException(query+" has no data"));  // NOI18N
                return retVal;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
        }
        return new HashMap<String,String>();
    }

    public static void putResourceData(Map<String, String> data, Hk2DeploymentManager dm) {
        Set<String> keys = data.keySet();
        for (String k : keys) {
            String name = k;
            String value = data.get(k);
            try {
                GlassfishModule support = dm.getCommonServerSupport();
                SetPropertyCommand spc = support.getCommandFactory().getSetPropertyCommand(name, value);
                Future<OperationState> task = support.execute(spc);
                OperationState state = task.get();
            } catch (InterruptedException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
            } catch (ExecutionException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
            }
        }
    }

    public static class ResourceFinder extends TreeParser.NodeReader {

        private Map<String, String> properties = null;
        private Map<String, Map<String, String>> resourceData = new HashMap<String, Map<String, String>>();

        private final String nameKey;

        public ResourceFinder(String in_nameKey) {
            nameKey = in_nameKey;
        }
        
        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            properties = new HashMap<String, String>();

            String resourceName = attributes.getValue(nameKey);
            properties.put(nameKey, resourceName);  //NOI18N

            int attrLen = attributes.getLength();
            for (int i = 0; i < attrLen; i++) {
                String name = attributes.getLocalName(i);
                String value = attributes.getValue(i);
                if (name != null && name.length() > 0 && value != null && value.length() > 0) {
                    properties.put(name, value);
                }
            }
        }

        @Override
        public void readChildren(String qname, Attributes attributes) throws SAXException {
            if (null != properties && null != attributes) {
                String propName = qname + "." + attributes.getValue("name"); // NO18N
                properties.put(propName, attributes.getValue("value"));  //NOI18N
            }
        }

        @Override
        public void endNode(String qname) throws SAXException {
            String poolName = properties.get(nameKey);  //NOI18N
            resourceData.put(poolName, properties);
        }

        public List<String> getResourceNames() {
            return new ArrayList<String>(resourceData.keySet());
        }

        public Map<String, Map<String, String>> getResourceData() {
            return Collections.unmodifiableMap(resourceData);
        }
    }
    
}

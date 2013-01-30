/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cloud.oracle;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.api.server.properties.InstancePropertiesManager;
import org.netbeans.libs.oracle.cloud.api.CloudSDKHelper;
import org.openide.util.ChangeSupport;

/**
 * Manager of all Oracle Cloud accounts registered in the IDE (usually just one).
 */
public class OracleInstanceManager {

    private static final String ORACLE_IP_NAMESPACE = "cloud.oracle"; // NOI18N
    
    static final String PREFIX = "org.netbeans.modules.cloud.oracle."; // NOI18N
    static final String USERNAME = "username"; // NOI18N
    static final String PASSWORD = "password"; // NOI18N
    private static final String NAME = "name"; // NOI18N
    private static final String ADMIN_URL = "admin-url"; // NOI18N
    private static final String DATA_CENTER = "data-center"; // NOI18N
    private static final String IDENTITY_DOMAIN = "identity-domain"; // NOI18N
    private static final String JAVA_SERVICE_NAME = "java-service-name"; // NOI18N
    private static final String DATABASE_SERVICE_NAME = "db-service-name"; // NOI18N
    private static final String ON_PREMISE_SERVICE_INSTANCE_ID = "on-premise"; // NOI18N
    
    private static OracleInstanceManager instance;
    private List<OracleInstance> instances = new ArrayList<OracleInstance>();
    private ChangeSupport listeners;
    
    private static final Logger LOG = Logger.getLogger(OracleInstanceManager.class.getSimpleName());
    
    
    public static synchronized OracleInstanceManager getDefault() {
        if (instance == null) {
            instance = new OracleInstanceManager();
        }
        return instance;
    }
    
    private OracleInstanceManager() {
        listeners = new ChangeSupport(this);
        init();
    }
    
    private void init() {
       instances.addAll(load());
       notifyChange();
    }
    
    private void notifyChange() {
       listeners.fireChange();
    }

    public List<OracleInstance> getInstances() {
        return instances;
    }
    
    public void add(OracleInstance ai) {
        store(ai);
        instances.add(ai);
        notifyChange();
    }
    
    private void store(OracleInstance ai) {
        InstanceProperties props = InstancePropertiesManager.getInstance().createProperties(ORACLE_IP_NAMESPACE);
        saveUsernameAndPassword(ai);
        props.putString(ADMIN_URL, ai.getAdminURL());
        props.putString(DATA_CENTER, ai.getDataCenter());
        props.putString(IDENTITY_DOMAIN, ai.getIdentityDomain());
        props.putString(JAVA_SERVICE_NAME, ai.getJavaServiceName());
        props.putString(DATABASE_SERVICE_NAME, ai.getDatabaseServiceName());
        props.putString(NAME, ai.getName());
        if (ai.getOnPremiseServerInstanceId() != null) {
            props.putString(ON_PREMISE_SERVICE_INSTANCE_ID, ai.getOnPremiseServerInstanceId());
        }
    }
        
    public void update(OracleInstance ai) {
        for(InstanceProperties props : InstancePropertiesManager.getInstance().getProperties(ORACLE_IP_NAMESPACE)) {
            String name = props.getString(NAME, null); // NOI18N
            if (name.equals(ai.getName())) {
                props.putString(ADMIN_URL, ai.getAdminURL());
                props.putString(DATA_CENTER, ai.getDataCenter());
                props.putString(IDENTITY_DOMAIN, ai.getIdentityDomain());
                props.putString(JAVA_SERVICE_NAME, ai.getJavaServiceName());
                props.putString(DATABASE_SERVICE_NAME, ai.getDatabaseServiceName());
                if (ai.getOnPremiseServerInstanceId() == null) {
                    props.removeKey(ON_PREMISE_SERVICE_INSTANCE_ID);
                } else {
                    props.putString(ON_PREMISE_SERVICE_INSTANCE_ID, ai.getOnPremiseServerInstanceId());
                }
                saveUsernameAndPassword(ai);
                notifyChange();
                break;
            }
        }
    }
    
    public boolean exist(String adminURL, String identityDomain, String javaServiceName, String user) {
        for (OracleInstance oi : getInstances()) {
            if (adminURL.equals(oi.getAdminURL()) &&
                    identityDomain.equals(oi.getIdentityDomain()) &&
                    // for now ignore java service name; one cloud account can 
                    // have just one Java service, right??
                    //javaServiceName.equals(oi.getJavaServiceName()) &&
                    user.equals(oi.getUser())) {
                return true;
            }
        }
        return false;
    }
    
    private static void saveUsernameAndPassword(OracleInstance ai) {
        Keyring.save(PREFIX+USERNAME+"."+ai.getName(), ai.getUser().toCharArray(), "Oracle Cloud Username"); // NOI18N
        Keyring.save(PREFIX+PASSWORD+"."+ai.getName(), ai.getPassword().toCharArray(), "Oracle Cloud Password"); // NOI18N
    }
    
    private static List<OracleInstance> load() {
        List<OracleInstance> result = new ArrayList<OracleInstance>();
        for(InstanceProperties props : InstancePropertiesManager.getInstance().getProperties(ORACLE_IP_NAMESPACE)) {
            String name = props.getString(NAME, null); // NOI18N
            assert name != null : "Instance without name";
            String adminURL = props.getString(ADMIN_URL, ""); // NOI18N

            String identityDomain = props.getString(IDENTITY_DOMAIN, "undefined"); // NOI18N
            String javaServiceName = props.getString(JAVA_SERVICE_NAME, "undefined"); // NOI18N
            String databaseServiceName = props.getString(DATABASE_SERVICE_NAME, ""); // NOI18N
            String onPremise = props.getString(ON_PREMISE_SERVICE_INSTANCE_ID, null); // NOI18N
            String dataCenter = props.getString(DATA_CENTER, "us1"); // NOI18N
            String sdkFolder = CloudSDKHelper.getSDKFolder();
            result.add(new OracleInstance(name, adminURL, dataCenter, identityDomain, javaServiceName, databaseServiceName, onPremise, sdkFolder));
        }
        return result;
    }

    public void addChangeListener(ChangeListener l) {
        listeners.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.removeChangeListener(l);
    }

    void remove(OracleInstance ai) {
        for (InstanceProperties props : InstancePropertiesManager.getInstance().getProperties(ORACLE_IP_NAMESPACE)) {
            if (ai.getName().equals(props.getString(NAME, null))) { // NOI18N
                props.remove();
                break;
            }
        }
        instances.remove(ai);
        ai.deregisterJ2EEServerInstances();
        notifyChange();
    }
}

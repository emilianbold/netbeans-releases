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
package org.netbeans.modules.cloud.oracle.serverplugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory2;
import org.netbeans.modules.j2ee.weblogic9.config.EarDeploymentConfiguration;
import org.netbeans.modules.j2ee.weblogic9.config.WarDeploymentConfiguration;

/**
 *
 */
public class ModuleConfigurationFactoryImpl implements ModuleConfigurationFactory2 {

    public ModuleConfigurationFactoryImpl() {
    }
    
    @Override
    public ModuleConfiguration create(J2eeModule j2eeModule, String instanceUrl) throws ConfigurationException {
        if (J2eeModule.Type.WAR.equals(j2eeModule.getType())) {
            return new CustomWarDeploymentConfiguration(j2eeModule);
        } else if (J2eeModule.Type.EAR.equals(j2eeModule.getType())) {
            return new EarDeploymentConfiguration(j2eeModule);
        }
        throw new ConfigurationException("Not supported module: " + j2eeModule.getType());
    }

    @Override
    public ModuleConfiguration create(J2eeModule j2eeModule) throws ConfigurationException {
        return new CustomWarDeploymentConfiguration(j2eeModule);
    }
    
    private static class CustomWarDeploymentConfiguration extends WarDeploymentConfiguration {

        public CustomWarDeploymentConfiguration(J2eeModule j2eeModule) {
            super(j2eeModule);
        }
        
        @Override
        public Set<Datasource> getDatasources() throws ConfigurationException {
            // below code is pointless as such data source cannot be used for anything
            // practical right now as there are no APIs to implement it
            //
            //return Collections.<Datasource>singleton(new CloudDatasource("TBD"));
            // replace "TBD" with name of db service
            //
            return Collections.emptySet();
        }
        
        @Override
        public boolean supportsCreateDatasource() {
            return false;
        }
        
    }

    private static class CustomEarDeploymentConfiguration extends EarDeploymentConfiguration {

        public CustomEarDeploymentConfiguration(J2eeModule j2eeModule) {
            super(j2eeModule);
        }
        
        @Override
        public Set<Datasource> getDatasources() throws ConfigurationException {
            // below code is pointless as such data source cannot be used for anything
            // practical right now as there are no APIs to implement it
            //
            //return Collections.<Datasource>singleton(new CloudDatasource("TBD"));
            // replace "TBD" with name of db service
            //
            return Collections.emptySet();
        }
        
        @Override
        public boolean supportsCreateDatasource() {
            return false;
        }
        
    }
    
//    private static class CloudDatasource implements Datasource {
//
//        private String jndiName;
//
//        public CloudDatasource(String jndiName) {
//            this.jndiName = jndiName;
//        }
//        
//        @Override
//        public String getJndiName() {
//            return jndiName;
//        }
//
//        @Override
//        public String getUrl() {
//            return "http://"+jndiName+"-nbtrial.db.cloud.oracle.com/apex/";
//        }
//
//        @Override
//        public String getUsername() {
//            return "";
//        }
//
//        @Override
//        public String getPassword() {
//            return "";
//        }
//
//        @Override
//        public String getDriverClassName() {
//            return "undefined";
//        }
//
//        @Override
//        public String getDisplayName() {
//            return "Oracle Database Cloud Service";
//        }
//        
//    }
    
}

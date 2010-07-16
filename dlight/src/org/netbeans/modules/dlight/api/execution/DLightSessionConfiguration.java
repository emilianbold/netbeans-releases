/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.api.execution;

import org.netbeans.modules.dlight.api.impl.DLightSessionConfigurationAccessor;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;


/**
 * This class represents session's configuration 
 * @author mt154047
 */
public final class DLightSessionConfiguration {

    static{
        DLightSessionConfigurationAccessor.setDefault(new DLightSessionConfigurationAccesorImpl());
    }
    
    private String dlightConfigurationName;
    private DLightConfiguration dlightConfiguration;
    private String storageID;
    private String sessionName;
    private DLightTarget target;
    private final Mode sesionMode;
    private boolean isUsingSharedStorage = false;

    public DLightSessionConfiguration(){
        this(Mode.ALL);
    }


    public DLightSessionConfiguration(Mode sessionMode){
        this.sesionMode = sessionMode;
    }

    public void setSharedStorageKey(String storageUniqueID){
        this.storageID = storageUniqueID;
        isUsingSharedStorage = storageID != null && !storageID.trim().equals("");
    }

    public void setSessionName(String sessionName){
        this.sessionName = sessionName;
    }

    public void setDLightTarget(DLightTarget dlightTarget){
        this.target = dlightTarget;
    }

    public void setDLightConfigurationName(String dlightConfigurationName){
        this.dlightConfigurationName = dlightConfigurationName;
    }

    public void setDLightConfiguration(DLightConfiguration configuration){
        this.dlightConfiguration = configuration;
    }



    @Override
    public String toString() {
        return "DLight [" + dlightConfiguration == null ? dlightConfigurationName : dlightConfiguration.getConfigurationName() + "] Session Creation for " + target;//NOI18N
    }

    public enum Mode{
        ALL,
        ANALYZE,
    }


    private static final class DLightSessionConfigurationAccesorImpl extends DLightSessionConfigurationAccessor{

        @Override
        public String getSessionName(DLightSessionConfiguration configuration) {
            return configuration.sessionName;
        }

        @Override
        public String getDLightConfigurationName(DLightSessionConfiguration configuration) {
            return configuration.dlightConfigurationName;
        }

        @Override
        public DLightConfiguration getDLightConfiguration(DLightSessionConfiguration configuration) {
            return configuration.dlightConfiguration;
        }

        @Override
        public DLightTarget getDLightTarget(DLightSessionConfiguration configuration) {
            return configuration.target;
        }

        @Override
        public Mode getSessionMode(DLightSessionConfiguration configuration) {
            return configuration.sesionMode;
        }

        @Override
        public boolean isUsingSharedStorage(DLightSessionConfiguration configuration) {
            return configuration.isUsingSharedStorage;
        }

        @Override
        public String getSharedStorageUniqueKey(DLightSessionConfiguration configuration) {
            return configuration.storageID;
        }
        
    }


}

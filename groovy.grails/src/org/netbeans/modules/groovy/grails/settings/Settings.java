/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.grails.settings;

import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.openide.util.NbPreferences;

/**
 *
 * @author schmidtm
 */
public class Settings {

    private static Settings instance = null;
    private static final String GRAILS_HOME_KEY = "grailsHome";
    private static final String GRAILS_PORT_KEY = "grailsPrj-Port-";
    private static final String GRAILS_ENV_KEY = "grailsPrj-Env-";
    private static final String GRAILS_DEPLOY_KEY = "grailsPrj-Deploy-";
    private static final String GRAILS_AUTODEPLOY_KEY = "grailsPrj-Autodeploy-";
    
    protected Settings () {}
    
    public static Settings getInstance() {
        if(instance == null){
            instance = new Settings();
        }
        
        return instance;
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule( Settings.class );
    }
    
    public String getGrailsBase() {
        return getPreferences().get(GRAILS_HOME_KEY, null);
        }

    public void setGrailsBase(String path) {
        getPreferences().put(GRAILS_HOME_KEY, path);
        }
    
    String getProjectName(Project prj){
        assert prj != null;
        
        ProjectInformation info = prj.getLookup().lookup(ProjectInformation.class);
        assert info != null;
        return info.getName();
    }            
    
    String getPortKey(Project prj){
        assert prj != null;
        return GRAILS_PORT_KEY+getProjectName(prj);
    }
    
    String getEnvKey(Project prj){
        assert prj != null;
        return GRAILS_ENV_KEY+getProjectName(prj);
    }
    
    String getDeployKey(Project prj){
        assert prj != null;
        return GRAILS_DEPLOY_KEY+getProjectName(prj);
    }
    
    String getAutodeployKey(Project prj){
        assert prj != null;
        return GRAILS_AUTODEPLOY_KEY+getProjectName(prj);
    }
    
    // Which port should we run on
    
    public String getPortForProject(Project prj){
        assert prj != null;
        return getPreferences().get(getPortKey(prj), null);
        }
    
    public void setPortForProject(Project prj, String port){
        assert prj != null;
        assert port != null;
        
        getPreferences().put(getPortKey(prj), port);
        
    }
    
    // which Environment should we use (Test, Production, Development, etc.)
    
    public String getEnvForProject(Project prj){
        assert prj != null;
        return getPreferences().get(getEnvKey(prj), null);
        }
    
    public void setEnvForProject(Project prj, String env){
        assert prj != null;
        assert env != null;
        
        getPreferences().put(getEnvKey(prj), env);
        
    }
    
    // Should we Autodeploy right after a 'grails war' command?
    
    public boolean getAutoDeployFlagForProject(Project prj){
        assert prj != null;
        return getPreferences().getBoolean(getAutodeployKey(prj), false);
        }
    
    public void setAutoDeployFlagForProject(Project prj, boolean flag){
        assert prj != null;
        
        getPreferences().putBoolean(getAutodeployKey(prj), flag);
        
    }    
    
    // Where should the WAR-File be deployed to?
    
    public String getDeployDirForProject(Project prj){
        assert prj != null;
        return getPreferences().get(getDeployKey(prj), null);
        }
    
    public void setDeployDirForProject(Project prj, String dir){
        assert prj != null;
        assert dir != null;
        
        getPreferences().put(getDeployKey(prj), dir);
        
    }    
    
    
}

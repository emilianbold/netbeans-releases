/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.Map;

import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

/** SystemOption to store the list of open projects
 *  XXX Should be removed later and changed either to registry
 *      or something else
 */
public class OpenProjectListSettings extends SystemOption {

    private static final String DIR_NAMES = "DirectoryNames"; //NOI18N
    private static final String LAST_OPEN_PROJECT_DIR = "LastOpenProjectDir"; //NOI18N
    private static final String OPEN_SUBPROJECTS = "OpenSubprojects"; //NOI18N
    private static final String OPEN_AS_MAIN = "OpenAsMain"; //NOI18N
    private static final String MAIN_PROJECT_DIR = "MainProjectDir"; //NOI18N
    private static final String RECENT_PROJECTS_DIR_NAMES = "RecentProjectsDirectoryNames"; //NOI18N
    private static final String PROP_PROJECTS_FOLDER = "projectsFolder";    //NOI18N
    private static final String TEMPLATE_USAGE_LRUS = "templateUsageLrus"; // NOI18N
    
    // PERSISTENCE
    private static final long serialVersionUID = 8754987489474L;

    
    public static OpenProjectListSettings getInstance() {
        return (OpenProjectListSettings)SystemOption.findObject( OpenProjectListSettings.class, true );
    }
    
    public String displayName() {
        return NbBundle.getMessage (OpenProjectListSettings.class,"TXT_UISettings"); //NOI18N
    }        

    public List getDirNames() {
        List list = (List)getProperty( DIR_NAMES );
        return list == null ? new ArrayList( 3 ) : list;
    }

    public void setDirNames( List list ) {
        putProperty( DIR_NAMES, list, true  );
    }
    
    public boolean isOpenSubprojects() {        
        Boolean value = (Boolean)getProperty( OPEN_SUBPROJECTS );        
        return value == null ? true : value.booleanValue();
    }
    
    public void setOpenSubprojects( boolean openSubprojects ) {
        putProperty( OPEN_SUBPROJECTS, openSubprojects ? Boolean.TRUE : Boolean.FALSE, true );
    }
    
    public boolean isOpenAsMain() {        
        Boolean value = (Boolean)getProperty( OPEN_AS_MAIN );        
        return value == null ? true : value.booleanValue();
    }
    
    public void setOpenAsMain( boolean openAsMain ) {
        putProperty( OPEN_AS_MAIN, openAsMain ? Boolean.TRUE : Boolean.FALSE, true );
    }
    
    public String getMainProjectDir() {
        return (String)getProperty( MAIN_PROJECT_DIR );
    }
    
    public void setMainProjectDir( String mainProjectDir ) {
        putProperty( MAIN_PROJECT_DIR, mainProjectDir, true  );
    }
    
    public String getLastOpenProjectDir() {
        return (String)getProperty( LAST_OPEN_PROJECT_DIR );
    }
    
    public void setLastOpenProjectDir( String path ) {
        putProperty( LAST_OPEN_PROJECT_DIR, path, true  );
    }

    public List getRecentProjectsDirNames() {
        List list = (List)getProperty( RECENT_PROJECTS_DIR_NAMES );
        return list == null ? new ArrayList( 5 ) : list;
    }

    public void setRecentProjectsDirNames( List list ) {
        putProperty( RECENT_PROJECTS_DIR_NAMES, list, true  );
    }

    public File getProjectsFolder () {
        String result = (String) this.getProperty (PROP_PROJECTS_FOLDER);
        if (result == null) {
            result = System.getProperty("user.home");   //NOI18N
        }
        return new File(result);
    }

    public void setProjectsFolder (File folder) {
        if (folder == null) {
            this.putProperty(PROP_PROJECTS_FOLDER,null);
        }
        else {
            this.putProperty(PROP_PROJECTS_FOLDER, folder.getAbsolutePath());
        }
    }
    
    public Map getTemplateUsageLRUs() {
        return (Map)getProperty( TEMPLATE_USAGE_LRUS );       
    }
    
    public void setTemplateUsageLRUs( Map map ) {
        putProperty( TEMPLATE_USAGE_LRUS, map, true  );
    }

}


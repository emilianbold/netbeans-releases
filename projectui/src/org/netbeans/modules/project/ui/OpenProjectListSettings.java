/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import javax.swing.filechooser.FileSystemView;
import org.openide.filesystems.FileUtil;
import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

/** SystemOption to store the list of open projects
 *  XXX Should be removed later and changed either to registry
 *      or something else
 */
public class OpenProjectListSettings extends SystemOption {

    private static final String OPEN_PROJECTS_URLS = "OpenProjectsURLs"; //NOI18N
    private static final String LAST_OPEN_PROJECT_DIR = "LastOpenProjectDir"; //NOI18N
    private static final String OPEN_SUBPROJECTS = "OpenSubprojects"; //NOI18N
    private static final String OPEN_AS_MAIN = "OpenAsMain"; //NOI18N
    private static final String MAIN_PROJECT_URL = "MainProjectURL"; //NOI18N
    private static final String RECENT_PROJECTS_URLS = "RecentProjectsURLs"; //NOI18N
    private static final String PROP_PROJECTS_FOLDER = "projectsFolder"; //NOI18N
    private static final String RECENT_TEMPLATES = "recentlyUsedTemplates"; // NOI18N
    private static final String PROP_PROJECT_CATEGORY = "lastSelectedProjectCategory"; //NOI18N
    private static final String PROP_PROJECT_TYPE = "lastSelectedProjectType"; //NOI18N
    
    // PERSISTENCE
    private static final long serialVersionUID = 8754987489474L;
    
    
    public static OpenProjectListSettings getInstance() {
        return (OpenProjectListSettings)SystemOption.findObject( OpenProjectListSettings.class, true );
    }
    
    public String displayName() {
        return NbBundle.getMessage (OpenProjectListSettings.class,"TXT_UISettings"); //NOI18N
    }        

    public List/*<URL>*/ getOpenProjectsURLs() {
        List list = (List)getProperty( OPEN_PROJECTS_URLS );
        return list == null ? new ArrayList( 3 ) : list;
    }

    public void setOpenProjectsURLs( List/*<URL>*/ list ) {
        putProperty( OPEN_PROJECTS_URLS, list, true  );
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
    
    public URL getMainProjectURL() {
        return (URL)getProperty( MAIN_PROJECT_URL );
    }
    
    public void setMainProjectURL( URL mainProjectURL ) {
        putProperty( MAIN_PROJECT_URL, mainProjectURL, true  );
    }
    
    public String getLastOpenProjectDir() {
        String result = (String)getProperty( LAST_OPEN_PROJECT_DIR );
        if (result == null) {
            result = System.getProperty("user.home");   //NOI18N
        }
        return result;
    }
    
    public void setLastOpenProjectDir( String path ) {
        putProperty( LAST_OPEN_PROJECT_DIR, path, true  );
    }

    public List/*<URL>*/ getRecentProjectsURLs() {
        List list = (List)getProperty( RECENT_PROJECTS_URLS );
        return list == null ? new ArrayList( 5 ) : list;
    }

    public void setRecentProjectsURLs( List/*<URL>*/ list ) {
        putProperty( RECENT_PROJECTS_URLS, list, true  );
    }

    public File getProjectsFolder () {
        String result = (String) this.getProperty (PROP_PROJECTS_FOLDER);
        if (result == null) {            
            result = System.getProperty("user.home");   //NOI18N
        }
        return FileUtil.normalizeFile(new File(result));
    }

    public void setProjectsFolder (File folder) {
        if (folder == null) {
            this.putProperty(PROP_PROJECTS_FOLDER,null);
        }
        else {
            this.putProperty(PROP_PROJECTS_FOLDER, folder.getAbsolutePath());
        }
    }
    
    public List /*<String>*/ getRecentTemplates() {        
        List list = (List)getProperty( RECENT_TEMPLATES );               
        return list == null ? new ArrayList( 100 ) : list;       
    }
    
    public void setRecentTemplates( List /*<String>*/ templateNames ) {
        putProperty( RECENT_TEMPLATES, templateNames, true  );
    }
    
    public String getLastSelectedProjectCategory () {
        return (String) getProperty (PROP_PROJECT_CATEGORY);
    }
    
    public void setLastSelectedProjectCategory (String category) {
        putProperty(PROP_PROJECT_CATEGORY,category,true);
    }
    
    public String getLastSelectedProjectType () {
        return (String) getProperty (PROP_PROJECT_TYPE);
    }
    
    public void setLastSelectedProjectType (String type) {
        putProperty(PROP_PROJECT_TYPE,type,true);
    }

}

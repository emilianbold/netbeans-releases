package org.netbeans.modules.java.j2seproject.ui;

import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

public class FoldersListSettings extends SystemOption {

    static final long serialVersionUID = 2386225041150479082L;

    private static final String LAST_EXTERNAL_TEST_ROOT = "testRoot";   //NOI18N

    private static final String LAST_EXTERNAL_SOURCE_ROOT = "srcRoot";  //NOI18N

    private static final String NEW_PROJECT_COUNT = "newProjectCount"; //NOI18N
    
    private static final String NEW_APP_COUNT = "newApplicationCount";  //NOI18N
    
    private static final String NEW_LIB_COUNT = "newLibraryCount"; //NOI18N

    private static final String SHOW_AGAIN_BROKEN_REF_ALERT = "showAgainBrokenRefAlert"; //NOI18N

    public String displayName() {
        return NbBundle.getMessage (FoldersListSettings.class,"TXT_J2SEProjectFolderList");
    }


    public String getLastExternalSourceRoot () {
        return (String) getProperty(LAST_EXTERNAL_SOURCE_ROOT);
    }

    public void setLastExternalSourceRoot (String path) {
        putProperty (LAST_EXTERNAL_SOURCE_ROOT, path, true);
    }

    public String getLastExternalTestRoot () {
        return (String) getProperty(LAST_EXTERNAL_TEST_ROOT);
    }

    public void setLastExternalTestRoot (String path) {
        putProperty (LAST_EXTERNAL_TEST_ROOT, path, true);
    }

    public int getNewProjectCount () {
        Integer value = (Integer) getProperty (NEW_PROJECT_COUNT);
        return value == null ? 0 : value.intValue();
    }

    public void setNewProjectCount (int count) {
        this.putProperty(NEW_PROJECT_COUNT, new Integer(count),true);
    }
    
    public int getNewApplicationCount () {
        Integer value = (Integer) getProperty (NEW_APP_COUNT);
        return value == null ? 0 : value.intValue();
    }
    
    public void setNewApplicationCount (int count) {
        this.putProperty(NEW_APP_COUNT, new Integer(count),true);
    }
    
    public int getNewLibraryCount () {
        Integer value = (Integer) getProperty (NEW_LIB_COUNT);
        return value == null ? 0 : value.intValue();
    }
    
    public void setNewLibraryCount (int count) {
        this.putProperty(NEW_LIB_COUNT, new Integer(count),true);
    }

    public boolean isShowAgainBrokenRefAlert() {
        Boolean b = (Boolean)getProperty(SHOW_AGAIN_BROKEN_REF_ALERT);
        return b == null ? true : b.booleanValue();
    }
    
    public void setShowAgainBrokenRefAlert(boolean again) {
        this.putProperty(SHOW_AGAIN_BROKEN_REF_ALERT, Boolean.valueOf(again), true);
    }

    public static FoldersListSettings getDefault () {
        return (FoldersListSettings) SystemOption.findObject (FoldersListSettings.class, true);
    }
}

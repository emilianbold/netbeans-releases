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

    public static FoldersListSettings getDefault () {
        return (FoldersListSettings) SystemOption.findObject (FoldersListSettings.class, true);
    }
}

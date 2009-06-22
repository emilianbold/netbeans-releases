/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-200? Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.frameworks.facelets.ui.sampleprojects;

//import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

//TODO migrate to preferences api!!!

public class FoldersListSettings /*extends SystemOption*/ {

    static final long serialVersionUID = -5L;
    
    private static final String LAST_EXTERNAL_SOURCE_ROOT = "srcRoot";  //NOI18N

    private static final String NEW_PROJECT_COUNT = "newProjectCount"; //NOI18N
    
//    private static final String SHOW_AGAIN_BROKEN_REF_ALERT = "showAgainBrokenRefAlert"; //NOI18N

    public String displayName() {
        return NbBundle.getMessage (FoldersListSettings.class, "TXT_WebProjectFolderList"); //NOI18N
    }

//    public String getLastExternalSourceRoot () {
//        return (String) getProperty(LAST_EXTERNAL_SOURCE_ROOT);
//    }
//
//    public void setLastExternalSourceRoot (String path) {
//        putProperty (LAST_EXTERNAL_SOURCE_ROOT, path, true);
//    }

    public int getNewProjectCount () {
        return 1; //:-)
//        Integer value = (Integer) getProperty (NEW_PROJECT_COUNT);
//        return value == null ? 0 : value.intValue();
    }

    public void setNewProjectCount (int count) {
//        this.putProperty(NEW_PROJECT_COUNT, new Integer(count),true);
    }
    
//    public boolean isShowAgainBrokenRefAlert() {
//        Boolean b = (Boolean)getProperty(SHOW_AGAIN_BROKEN_REF_ALERT);
//        return b == null ? true : b.booleanValue();
//    }
//    
//    public void setShowAgainBrokenRefAlert(boolean again) {
//        this.putProperty(SHOW_AGAIN_BROKEN_REF_ALERT, Boolean.valueOf(again), true);
//    }

    public static FoldersListSettings getDefault () {
//        return (FoldersListSettings) SystemOption.findObject (FoldersListSettings.class, true);
        return new FoldersListSettings();
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2me.cdc.project.ui;

import org.openide.util.NbBundle;
import java.io.File;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Misnamed storage of information application to the new cdcproject wizard.
 */
public class CDCFoldersListSettings {

    private static Preferences prefs=NbPreferences.forModule(CDCFoldersListSettings.class);
    
    private static final long serialVersionUID = 2386225041150479082L;
    private static final String NEW_PROJECT_COUNT = "newProjectCount"; //NOI18N
    private static final String NEW_APP_COUNT = "newApplicationCount";  //NOI18N
    private static final String NEW_LIB_COUNT = "newLibraryCount"; //NOI18N
    private static final String LAST_USED_CP_FOLDER = "lastUsedClassPathFolder";    //NOI18N
    private static final String LAST_USED_ARTIFACT_FOLDER = "lastUsedArtifactFolder"; //NOI18N
    

    
    public static String displayName() {
        return NbBundle.getMessage(CDCFoldersListSettings.class, "TXT_CDCProjectFolderList");
    }

    public static int getNewProjectCount () {
        return prefs.getInt (NEW_PROJECT_COUNT,0);
    }

    public static void setNewProjectCount (int count) {
        prefs.putInt(NEW_PROJECT_COUNT, count);
    }
    
    public static int getNewApplicationCount () {
        return prefs.getInt (NEW_APP_COUNT,0);
    }
    
    public static void setNewApplicationCount (int count) {
        prefs.putInt(NEW_APP_COUNT, count);
    }
    
    public static int getNewLibraryCount () {
        return prefs.getInt(NEW_LIB_COUNT,0);
    }
    
    public static void setNewLibraryCount (int count) {
        prefs.putInt(NEW_LIB_COUNT, count);
    }

    public static File getLastUsedClassPathFolder () {
        String lucpr = prefs.get(LAST_USED_CP_FOLDER,System.getProperty("user.home"));       
        return new File (lucpr);
    }

    public static void setLastUsedClassPathFolder (File folder) {
        assert folder != null : "ClassPath root can not be null";
        String path = folder.getAbsolutePath();
        prefs.put(LAST_USED_CP_FOLDER, path);
    }

    public static File getLastUsedArtifactFolder () {
        String folder = prefs.get(LAST_USED_ARTIFACT_FOLDER,System.getProperty("user.home"));        
        return new File (folder);
    }

    public static void setLastUsedArtifactFolder (File folder) {
        assert folder != null : "Folder can not be null";
        String path = folder.getAbsolutePath();
        prefs.put (LAST_USED_ARTIFACT_FOLDER, path);
    }   
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.avatar_js.project.ui.wizards;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Storage of information for the new Avatar.js project wizard.
 */
public class WizardSettings {
    
    static final String PROP_WTYPE = "wizard-type";     // NOI18N
    static final String PROP_NAME = "name";             // NOI18N
    static final String PROP_PRJ_DIR = "projdir";       // NOI18N
    static final String PROP_NAME_INDEX = "nameIndex";  // NOI18N
    static final String PROP_MAIN_FILE = "mainFile";    // NOI18N
    static final String PROP_SOURCE_ROOT = "sourceRoot";// NOI18N
    
    static final String PROP_JAVA_PLATFORM = "javaPlatform";    // NOI18N
    static final String PROP_AVATAR_LIBS = "avatarLibsFolder";  // NOI18N
    static final String PROP_AVATAR_JAR = "avatarLibJAR";       // NOI18N
    
    static final String PROP_SERVER_FILE_NAME = "serverName";   // NOI18N
    static final String PROP_SERVER_FILE_FOLDER = "serverFolder";// NOI18N
    static final String PROP_SERVER_FILE_PORT = "serverPort";   // NOI18N

    private WizardSettings() {}
    
    private static final String NEW_PROJECT_COUNT = "newProjectCount"; //NOI18N

    private static final String NEW_APP_COUNT = "newApplicationCount";  //NOI18N

    private static final String NEW_LIB_COUNT = "newLibraryCount"; //NOI18N

    private static Preferences getPreferences() {
        return NbPreferences.forModule(WizardSettings.class);
    }

    public static int getNewProjectCount() {
        return getPreferences().getInt(NEW_PROJECT_COUNT, 0);
    }

    public static void setNewProjectCount(int count) {
        getPreferences().putInt(NEW_PROJECT_COUNT, count);
    }
    
    public static int getNewApplicationCount() {
        return getPreferences().getInt(NEW_APP_COUNT, 0);
    }
    
    public static void setNewApplicationCount(int count) {
        getPreferences().putInt(NEW_APP_COUNT, count);
    }
    
    public static int getNewLibraryCount() {
        return getPreferences().getInt(NEW_LIB_COUNT, 0);
    }
    
    public static void setNewLibraryCount(int count) {
        getPreferences().putInt(NEW_LIB_COUNT, count);
    }

}

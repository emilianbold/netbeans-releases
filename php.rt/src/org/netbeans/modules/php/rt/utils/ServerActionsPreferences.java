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
package org.netbeans.modules.php.rt.utils;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author avk
 */
public class ServerActionsPreferences {
    
    private static final int FILE_OVERWRITE_YES = 1; // NOI18N
    private static final int FILE_OVERWRITE_NO = 0; // NOI18N
    private static final int FILE_OVERWRITE_ASK_ME = 2; // NOI18N

    private static final ServerActionsPreferences INSTANCE = new ServerActionsPreferences();
    
    private static final String FILE_UPLOAD_BEFORE_RUN = "upload-file-before-run"; // NOI18N
   
    private static final String FILE_OVERWRITE = "overwrite-file"; // NOI18N
    
    private ServerActionsPreferences() {}
    
    public static ServerActionsPreferences getInstance() {
        return INSTANCE;
    }

    public void setUploadBeforeRun(final boolean upload){
        getPreferences().putBoolean(FILE_UPLOAD_BEFORE_RUN, upload);
    }
    
    public boolean getUploadBeforeRun(){
        return getPreferences().getBoolean(FILE_UPLOAD_BEFORE_RUN, true);
    }
    
    public void setFileOverwrite(final Boolean overwrite){
        int value = FILE_OVERWRITE_ASK_ME;
        if (overwrite != null){
            value = overwrite.booleanValue() ? FILE_OVERWRITE_YES : FILE_OVERWRITE_NO;
        }
        getPreferences().putInt(FILE_OVERWRITE, value);
    }
    
    public Boolean getFileOverwrite(){
        int value = getPreferences().getInt(FILE_OVERWRITE, FILE_OVERWRITE_ASK_ME);
        if (value == FILE_OVERWRITE_NO){
            return new Boolean(false);
        } else if (value == FILE_OVERWRITE_YES){
            return new Boolean(true);
        } else {
            return null;
        }
    }
    
    public void addPreferenceChangeListener(PreferenceChangeListener pcl){
        getPreferences().addPreferenceChangeListener(pcl);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener pcl){
        getPreferences().removePreferenceChangeListener(pcl);
    }
    
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(ServerActionsPreferences.class);
    }
    
    
}

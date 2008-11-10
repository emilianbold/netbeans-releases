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
package org.netbeans.modules.localhistory;

import java.io.File;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * Manages the Local History Settings
 * 
 * @author Tomas Stupka
 */
public class LocalHistorySettings {
    
    private static final LocalHistorySettings INSTANCE = new LocalHistorySettings();
    
    private static final String LAST_SELECTED_ENTRY = "RevertFileChanges.lastSelected";         // NOI18N  
    private static final String PROP_TTL = "timeToLive";                                        // NOI18N  
    private static final String PROP_KEEP_STORED = "filesToKeepStored";                         // NOI18N  
    
            
    /** Creates a new instance of LocalHistorySettings */
    private LocalHistorySettings() {
    }
    
    public static LocalHistorySettings getInstance() {        
        return INSTANCE;
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(LocalHistorySettings.class);
    }

    public void setTTL(int ttl) {
        getPreferences().putInt(PROP_TTL, ttl);
    }
    
    public int getTTL() {
        return getPreferences().getInt(PROP_TTL, 7);
    }    

    public long getTTLMillis() {
        return ((long) getTTL()) * 24 * 60 * 60 * 1000;
    }   
    
    public void setLastSelectedEntry(File file, long ts) {
        getPreferences().putLong(LAST_SELECTED_ENTRY + "#" + file.getAbsoluteFile(), ts);
    }
    
    public long getLastSelectedEntry(File file) {
        return getPreferences().getLong(LAST_SELECTED_ENTRY  + "#" + file.getAbsoluteFile(), -1);
    }
    
}

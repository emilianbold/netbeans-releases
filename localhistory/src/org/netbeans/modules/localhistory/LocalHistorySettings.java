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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.localhistory;

import java.io.File;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * Manages the Local Hisotry Settings
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

    public void setKeepStored(int keep) {       
        getPreferences().putInt(PROP_KEEP_STORED, keep);
    }    
    
    public int getKeepStored() {       
        return getPreferences().getInt(PROP_KEEP_STORED, 5);
    }    
    
    public void setTTL(int ttl) {       
        getPreferences().putInt(PROP_TTL, ttl);
    }
    
    public int getTTL() {               
        return getPreferences().getInt(PROP_TTL, 7);
    }    

    public int getTTLMillis() {               
        return getTTL() * 24 * 60 * 60 * 1000;
    }   
    
    public void setLastSelectedEntry(File file, long ts) {
        getPreferences().putLong(LAST_SELECTED_ENTRY + "#" + file.getAbsoluteFile(), ts);
    }
    
    public long getLastSelectedEntry(File file) {
        return getPreferences().getLong(LAST_SELECTED_ENTRY  + "#" + file.getAbsoluteFile(), -1);
    }
    
}

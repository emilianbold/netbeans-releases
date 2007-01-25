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

import org.openide.ErrorManager;

/**
 *
 * Manages the Local Hisotry Settings
 * 
 * XXX this is dummy
 * XXX options 
 * @author Tomas Stupka
 */
public class LocalHistorySettings {
    
    /** Creates a new instance of LocalHistorySettings */
    public LocalHistorySettings() {
    }
    
    public static long getTTL() {       
        // XXX need options                                
        String ttl = System.getProperty("netbeans.localhistory.ttl");
        if( ttl != null && !ttl.trim().equals("") ) {
            try {
                return Long.parseLong(ttl) * 24 * 60 * 60 * 1000; // supposed to be specified in days
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);                                       
            }
        }
        return 7 * 24 * 60 * 60 * 1000; 
    }    

    public static Long getMaxFileSize() {
        String max = System.getProperty("netbeans.localhistory.maxFileSize");        
        if( max != null && !max.trim().equals("") ) {
            try {
                return Long.parseLong(max);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);                                       
            }
        }
        return 1024L * 1024L;
    }
    
    public static String getExludedFileNames() {
        String excluded = System.getProperty("netbeans.localhistory.excludedFiles");        
        if( excluded != null && !excluded.trim().equals("") ) {
            return excluded;
        } else {
            return ".*(\\.class|\\.jar|\\.zip|\\.rar|\\.gz|\\.bz|\\.tgz|\\.tar|\\.gif|\\.jpg|\\.jpeg|\\.png|\\.nbm)"; // this is going to be a very very long list ...   
        }        
    }
    
}

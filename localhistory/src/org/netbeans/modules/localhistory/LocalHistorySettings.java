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

/**
 *
 * XXX this is dummy
 * @author Tomas Stupka
 */
public class LocalHistorySettings {
    
    /** Creates a new instance of LocalHistorySettings */
    public LocalHistorySettings() {
    }
    
    public static long getTTL() {
        return 7 * 24 * 60 * 60 * 1000; // XXX need options                                
    }    

    public static Long getMaxFileSize() {
        return 1024L * 1024L;
    }
    
    public static String getExludedFileNames() {
        return ".*(\\.class|\\.jar|\\.zip|\\.rar|\\.gz|\\.bz|\\.tgz|\\.tar|\\.gif|\\.jpg|\\.jpeg|\\.png|\\.nbm)"; // this is going to be a very very long list ...
    }
    
}

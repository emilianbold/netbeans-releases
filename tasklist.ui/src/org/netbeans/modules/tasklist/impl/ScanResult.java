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

package org.netbeans.modules.tasklist.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;

/**
 *
 * @author S. Aubrecht
 */
class ScanResult {
    
    private Map<FileTaskScanner, Result> scanTimes = new HashMap<FileTaskScanner, Result>( 8 );
    
    public ScanResult() {
    }
    
    public boolean isUpToDate( FileObject resource, FileTaskScanner scanner ) {
        boolean res = false;
        Result type = scanTimes.get( scanner );
        if( null != type && null != resource ) {
            res = resource.lastModified().before( type.scanTime );
        }
        return res;
    }
    
    public void put( FileTaskScanner scanner, List<? extends Task> tasks ) {
        Result res = scanTimes.get( scanner );
        if( null == res ) {
            res = new Result();
            scanTimes.put( scanner, res );
        }
        res.scanTime = new Date();
        if( tasks.isEmpty())
            res.tasks = null;
        else
            res.tasks = new ArrayList<Task>( tasks );
    }
    
    public void get( FileTaskScanner scanner, List<Task> tasks ) {
        Result res = scanTimes.get( scanner );
        if( null != res && null != res.tasks ) {
            tasks.addAll( res.tasks );
        }
    }
    
    public void remove( FileTaskScanner scanner ) {
        scanTimes.remove( scanner );
    }
    
    public boolean isEmpty() {
        return scanTimes.isEmpty();
    }
    
    private static class Result {
        private Date scanTime;
        private List<Task> tasks;
    }
}

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

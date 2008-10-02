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
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;

/**
 * 
 * @author S. Aubrecht
 */
class TaskCache {
    
    //TODO use URLs instead of FileObjects
    private WeakHashMap<FileObject, ScanResult> cache = new WeakHashMap<FileObject, ScanResult>();
    
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    /** Creates a new instance of TaskCache */
    TaskCache() {
    }
    
    public boolean isUpToDate( FileObject resource, FileTaskScanner scanner ) {
        boolean retValue = false;
        lock.readLock().lock();
        ScanResult scanRes = cache.get( resource );
        if( null != scanRes ) {
            retValue = scanRes.isUpToDate( resource, scanner );
        }
        lock.readLock().unlock();
        return retValue;
    }
    
    /**
     * The given file was just scanned for tasks of the given type.
     * 
     * @param tasks Tasks found in the file, may be null.
     */
    public void scanned( FileObject resource, FileTaskScanner scanner, List<? extends Task> tasks ) {
        lock.writeLock().lock();
        
        ScanResult scanRes = cache.get( resource );
        if( null == scanRes ) {
            scanRes = new ScanResult();
            cache.put( resource, scanRes );
        }
        scanRes.put( scanner, tasks );
        
        lock.writeLock().unlock();
    }
    
    public void getTasks( FileObject resource, FileTaskScanner scanner, List<Task> tasks ) {
        lock.readLock().lock();
        ScanResult scanRes = cache.get( resource );
        if( null != scanRes ) {
            scanRes.get( scanner, tasks );
        }
        lock.readLock().unlock();
    }
    
    /**
     * All files must be rescanned for the given task type
     */
    public void clear( FileTaskScanner scanner ) {
        lock.writeLock().lock();
        
        ArrayList<FileObject> toRemove = null;
        for( FileObject rc : cache.keySet() ) {
            ScanResult scanRes = cache.get( rc );
            scanRes.remove( scanner );
            if( scanRes.isEmpty() ) {
                if( null == toRemove ) {
                    toRemove = new ArrayList<FileObject>();
                }
                toRemove.add( rc );
            }
        }
        if( null != toRemove ) {
            for( FileObject rc : toRemove ) {
                cache.remove( rc );
            }
        }
        lock.writeLock().unlock();
    }
    
    public void clear( FileTaskScanner scanner, FileObject[] resources ) {
        lock.writeLock().lock();
        
        for( FileObject rc : resources ) {
            ScanResult scanRes = cache.get( rc );
            if( null != scanRes ) {
                scanRes.remove( scanner );
                if( scanRes.isEmpty() ) {
                    cache.remove( rc );
                }
            }
        }
        lock.writeLock().unlock();
    }

    public void clear( FileObject resource ) {
        lock.writeLock().lock();
        ScanResult scanRes = cache.get( resource );
        if( null != scanRes ) {
            cache.remove( resource );
        }
        lock.writeLock().unlock();
    }

    public void clear() {
        lock.writeLock().lock();
        cache.clear();
        lock.writeLock().unlock();
    }
}

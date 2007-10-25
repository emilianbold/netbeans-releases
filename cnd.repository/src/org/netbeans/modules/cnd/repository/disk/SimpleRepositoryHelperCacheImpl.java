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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.repository.disk;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.netbeans.modules.cnd.repository.disk.api.RepositoryFilesHelperCacheStrategy;
import org.netbeans.modules.cnd.repository.sfs.ConcurrentFileRWAccess;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.util.RepositoryCacheMap;


class SimpleRepositoryHelperCacheImpl implements RepositoryFilesHelperCacheStrategy { 
    private static volatile SimpleRepositoryHelperCacheImpl instance;
    private static Lock     lock = new ReentrantLock();
    
    private RepositoryCacheMap<String, ConcurrentFileRWAccess> nameToFileCache;
    
    public static SimpleRepositoryHelperCacheImpl getInstance(int newCapacity) {
        
        if (instance == null) {
            try {
                lock.lock();
                if (instance == null) {
                    instance =new SimpleRepositoryHelperCacheImpl(newCapacity);
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }
    
    public void adjustCapacity(int newCapacity) throws IOException {
        Set<ConcurrentFileRWAccess> removedFiles = nameToFileCache.adjustCapacity(newCapacity);
        
        if (removedFiles == null)
            return ;
        
        for (ConcurrentFileRWAccess fileToRemove: removedFiles) {
            try {
                fileToRemove.getLock().writeLock().lock();
                
                if (fileToRemove.getFD().valid()) {
                    fileToRemove.close();
                }
                
            } finally {
                fileToRemove.getLock().writeLock().unlock();
            }
        }
    }
    
    protected SimpleRepositoryHelperCacheImpl (int openFilesLimit) {
        nameToFileCache = new RepositoryCacheMap<String, ConcurrentFileRWAccess>(openFilesLimit);
    }
    
    public String lookupInCacheName(Key id) {
        return null;
    }
    
    public ConcurrentFileRWAccess lookupInCacheFile(String fileName) {
        return nameToFileCache.get(fileName);
    }
    
    public void cacheNameRemove(String fileName) throws IOException {
        ConcurrentFileRWAccess removedFile = nameToFileCache.remove(fileName);
        if (removedFile != null) {
            try {
                removedFile.getLock().writeLock().lock();
                
                if (removedFile.getFD().valid() )
                    removedFile.close();
                
            }  finally {
                removedFile.getLock().writeLock().unlock();
            }
        }
    }
    
    public void putCacheFile(String fileName, ConcurrentFileRWAccess aFile) throws IOException {
        ConcurrentFileRWAccess removedFile = nameToFileCache.put(fileName, aFile);
        if (removedFile != null) {
            try {
                removedFile.getLock().writeLock().lock();
                if (removedFile.getFD().valid()) {
                    removedFile.close();
                }
                
            } finally {
                removedFile.getLock().writeLock().unlock();
            }
        }
    }

    public void putCacheName(Key id, String fileName) {
    }

    public void cacheFileRemove(ConcurrentFileRWAccess aFile) {
    }
}
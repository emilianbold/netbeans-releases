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

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import org.netbeans.modules.cnd.repository.sfs.ConcurrentBufferedRWAccess;
import org.netbeans.modules.cnd.repository.sfs.ConcurrentFileRWAccess;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 *
 * @author Nickolay Dalmatov
 */
public class FilesAccessStrategyImpl implements FilesAccessStrategy {
    
    private RepositoryHelperCache theCache;
    private Object cacheLock = new String("Repository file cache lock"); //NOI18N
    
    /** Creates a new instance of SimpleRepositoryFilesHelper */
    public FilesAccessStrategyImpl(int openFilesLimit) {
        theCache = RepositoryHelperCache.getInstance(openFilesLimit);
    }

    public Persistent read(Key key, PersistentFactory factory) throws IOException {
        ConcurrentFileRWAccess fis = null;
        try {
            fis = getFile(key, true);
            if( fis == null ) {
                return null;
            }
            long size = fis.size();
            Persistent obj = fis.read(factory, 0, (int)size);
            return obj;
        } finally {
            if (fis != null) {
                fis.getLock().readLock().unlock();
            }
        }
    }

    public void write(Key key, PersistentFactory factory, Persistent object) throws IOException {
        ConcurrentFileRWAccess fos = null;
        try {
            fos = getFile(key, false);
            if (fos != null) {
                int size = fos.write(factory, object, 0);
                fos.truncate(size);
            } 
        } finally {
            if (fos != null) {
                fos.getLock().writeLock().unlock();
            }
        }
        
    }
    
    private ConcurrentFileRWAccess getFile(Key id, final boolean readOnly) throws IOException {
        assert id != null;
        
        String fileName = resolveFileName(id);
        assert fileName != null;
        ConcurrentFileRWAccess   aFile;
        boolean keepLocked = false;
        
        do {
            aFile = getFileByName(fileName, ! readOnly);
            
            if (aFile == null) {
                break;
            }
            
            try {
                if (readOnly) {
                    aFile.getLock().readLock().lock();
                } else {
                    aFile.getLock().writeLock().lock();
                }
                if (aFile.getFD().valid()) {
                    keepLocked = true;
                    break;
                }
            }  finally {
                if (!keepLocked) {
                    if (readOnly) {
                        aFile.getLock().readLock().unlock();
                    } else {
                        aFile.getLock().writeLock().unlock();
                    }
                }
            }
            
        } while (true);
        
        return aFile;
    }
    
    public void removeFile(Key id) throws IOException{
        
        String fileName = resolveFileName( id);
        assert fileName != null;
        
        theCache.removeFile(fileName);
        
        File toDelete = new File(fileName);
        toDelete.delete();
        
    }
    
    //
    
    private final static char START_CHAR = 'z';
    private final static char SEPARATOR_CHAR = '-';
    
    private static String resolveFileName(Key id) throws IOException {
        
        assert id != null;
        int size = id.getDepth();
        
        StringBuffer    nameBuffer = new StringBuffer(""); //NOI18N

        if( size == 0 ) {
            nameBuffer.append(id.getUnit());
        }
        else {
            for (int i = 0 ; i < size; ++i) {
                nameBuffer.append(id.getAt(i));
                nameBuffer.append(SEPARATOR_CHAR);
            }
        }

        for (int j = 0 ; j < id.getSecondaryDepth(); ++j) {
            nameBuffer.append(id.getSecondaryAt(j)  + SEPARATOR_CHAR);
        }

        String fileName = nameBuffer.toString();

        fileName = URLEncoder.encode(fileName, Stats.ENCODING);

        fileName = StorageAllocator.getInstance().getUnitStorageName(id.getUnit().toString()) + 
                StorageAllocator.getInstance().reduceString(fileName);

        return fileName;
    }
    
    private ConcurrentFileRWAccess getFileByName(String fileName, boolean create) throws IOException {
        assert fileName != null;
        
        ConcurrentFileRWAccess aFile = theCache.getFile(fileName);
        
        if (aFile == null) {
	    synchronized( cacheLock ) {
		aFile = theCache.getFile(fileName);
		if (aFile == null) {
		    File fileToCreate = new File(fileName);
		    if (fileToCreate.exists()) {
			aFile = new ConcurrentBufferedRWAccess(fileToCreate); //NOI18N
			theCache.putFile(fileName, aFile);
		    } else if (create) {
			String aDirName = fileToCreate.getParent();
			File  aDir = new File(aDirName);

			if (aDir.exists() || aDir.mkdirs()) {
			    aFile = new ConcurrentBufferedRWAccess(fileToCreate); //NOI18N
			    theCache.putFile(fileName, aFile);
			}
		    }
		}
	    }
        }
        
        return aFile;
    }
    
    
    public void setOpenFilesLimit(int limit) throws IOException {
        theCache.adjustCapacity(limit);
    }
    
    public void closeUnit(String unitName) {
    }
}

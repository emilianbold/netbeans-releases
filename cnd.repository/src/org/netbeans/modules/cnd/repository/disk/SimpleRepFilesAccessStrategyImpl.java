/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.repository.disk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.netbeans.modules.cnd.repository.disk.api.RepFilesAccessStrategy;
import org.netbeans.modules.cnd.repository.disk.api.RepositoryFilesHelperCacheStrategy;
import org.netbeans.modules.cnd.repository.impl.*;
import org.netbeans.modules.cnd.repository.sfs.ConcurrentBufferedRWAccess;
import org.netbeans.modules.cnd.repository.sfs.ConcurrentFileRWAccess;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 *
 * @author Nickolay Dalmatov
 */
public class SimpleRepFilesAccessStrategyImpl implements RepFilesAccessStrategy {
    
    
    private String theRepositoryBase;
    private RepositoryFilesHelperCacheStrategy theCache;
    
    /** Creates a new instance of SimpleRepositoryFilesHelper */
    public SimpleRepFilesAccessStrategyImpl(int openFilesLimit) {
        theCache = new SimpleRepositoryHelperCacheImpl(openFilesLimit);
    }
    
    public ConcurrentFileRWAccess getFileForObj(Key id, boolean read) throws FileNotFoundException, IOException {
        assert id != null;
        
        String fileName = resolveFileName(id);
        assert fileName != null;
        ConcurrentFileRWAccess   aFile;
        boolean keepLocked = false;
        
        do {
            aFile = getFileByName(fileName, read?false:true);
            
            if (aFile == null)
                break;
            
            try {
                if (read) {
                    aFile.readLock().lock();
                } else {
                    aFile.writeLock().lock();
                }
                if (aFile.getFD().valid()) {
                    keepLocked = true;
                    break;
                }
            }  catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (!keepLocked) {
                    if (read) {
                        aFile.readLock().unlock();
                    } else {
                        aFile.writeLock().unlock();
                    }
                }
            }
            
        } while (true);
        
        return aFile;
    }
    
    public void removeObjForKey(Key id) {
        
        String fileName = resolveFileName( id);
        assert fileName != null;
        
        theCache.cacheNameRemove(fileName);
        
        File toDelete = new File(fileName);
        toDelete.delete();
        
    }
    
    //
    public void setRepositoryBase(String aBaseDir) {
        theRepositoryBase = aBaseDir;
    }
    
    private final static char START_CHAR = 'z';
    
    protected String resolveFileName(Key id) {
        /*if (Stats.isDebug)
        {
            String fileName = theRepositoryBase + File.separator + "dummy"; // NOI18N
            theCache.putCacheName (id, fileName);
            return fileName;
        }*/
        assert id != null;
        int size = id.getDepth();
        assert size != 0;
        
        String fileName = theCache.lookupInCacheName(id);
        
        if (fileName == null) {
            
            StringBuffer nameBuffer = new StringBuffer(StorageAllocator.getInstance().getUnitStorageName(id.getUnit()));
            /*try {
                nameBuffer.append(URLEncoder.encode(id.getUnit(), Stats.ENCODING));
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }*/
            for (int i = 0 ; i < size; ++i) {
                nameBuffer.append(File.separator);
                nameBuffer.append(START_CHAR);
                try {
                    nameBuffer.append(URLEncoder.encode(id.getAt(i), Stats.ENCODING));
                } catch (UnsupportedEncodingException ex) {
                    // we use system default encoding, it can't be missed
                    ex.printStackTrace();
                }
            }
            
            for (int j = 0 ; j < id.getSecondaryDepth(); ++j) {
                nameBuffer.append(File.separator);
                nameBuffer.append(START_CHAR);
                nameBuffer.append(id.getSecondaryAt(j));
            }
            
            fileName = nameBuffer.toString();
            theCache.putCacheName(id, fileName);
        }
        
        return fileName;
    }
    
    protected ConcurrentFileRWAccess getFileByName(String fileName, boolean create) {
        assert fileName != null;
        
        ConcurrentFileRWAccess aFile = theCache.lookupInCacheFile(fileName);
        
        try {
            if (aFile == null) {
                File fileToCreate = new File(fileName);
                if (fileToCreate.exists()) {
                    aFile = new ConcurrentBufferedRWAccess(fileToCreate); //NOI18N
                    theCache.putCacheFile(fileName, aFile);
                    fileToCreate = null;
                } else if (create) {
                    String aDirName = fileToCreate.getParent();
                    File  aDir = new File(aDirName);
                    
                    if (aDir.exists() || aDir.mkdirs()) {
                        aFile = new ConcurrentBufferedRWAccess(fileToCreate); //NOI18N
                        theCache.putCacheFile(fileName, aFile);
                    }
                    aDir = null;
                    fileToCreate = null;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        
        return aFile;
    }
    
    
    public void setOpenFilesLimit(int limit) {
        theCache.adjustCapacity(limit);
    }
    
    public void closeUnit(String unitName) {
        File unitStorage = new File(StorageAllocator.getInstance().getUnitStorageName(unitName));
        assert unitStorage.isDirectory();
        deleteDirectory(unitStorage);
    }
    
    static public boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }
}

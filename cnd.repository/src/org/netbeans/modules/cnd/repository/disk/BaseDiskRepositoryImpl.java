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

import java.io.FileNotFoundException;
import java.io.IOException;
import org.netbeans.modules.cnd.repository.disk.api.RepFilesAccessStrategy;
import org.netbeans.modules.cnd.repository.impl.*;
import org.netbeans.modules.cnd.repository.sfs.ConcurrentFileRWAccess;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;

/**
 * The implementation of the repository, which uses HDD
 * @author Nickolay Dalmatov 
 */
public class BaseDiskRepositoryImpl extends AbstractDiskRepository {
    
    public static int    defaultRepositoryOpenFilesLimit = 20; 
    
    private RepFilesAccessStrategy theFilesHelper;
    private int openFilesLimit = defaultRepositoryOpenFilesLimit;
    
    /** Creates a new instance of BaseDiskRepository */
    public BaseDiskRepositoryImpl() {
        super();
        // use the simple helper implementation
        theFilesHelper = new SimpleRepFilesAccessStrategyImpl(defaultRepositoryOpenFilesLimit);
    }
    
    public void setRepositoryBaseDirectory (String aBaseDir) {
        theFilesHelper.setRepositoryBase(aBaseDir);
    }
    
    public void setOpenFilesLimit (int limit) {
        openFilesLimit = limit;
        theFilesHelper.setOpenFilesLimit(openFilesLimit);
    }
    
    /** Creates a new instance of SimpleDiskRepository */
    public BaseDiskRepositoryImpl(RepFilesAccessStrategy aFilesHelper) {
        theFilesHelper = aFilesHelper;
    }
    
    public void setFilesHelper(RepFilesAccessStrategy aNavigator) {
        theFilesHelper = aNavigator;
    }
    
    public void write(Key id, final Persistent obj) throws IOException  {
        assert id != null;
        assert obj != null;
        
        // get the factory
        final PersistentFactory theFactory = id.getPersistentFactory();
        assert theFactory != null;
	ConcurrentFileRWAccess fos = theFilesHelper.getFileForObj(id, false);
	if (fos != null) {
            int size = fos.write(theFactory, obj,0);
            fos.truncate(size);
            fos.writeLock().unlock();
	} else {
            throw new FileNotFoundException("Failed to create disk cache"); // NOI18N
        }
    }
    
    public boolean maintenance(long timeout) throws IOException {
	return false;
    }
    
    public Persistent get(Key id) {
        assert id != null;
        Persistent obj = null;
        
        // get the factory
        final PersistentFactory theFactory = id.getPersistentFactory();
        assert theFactory != null;
        try {
            ConcurrentFileRWAccess fis = theFilesHelper.getFileForObj(id, true);

            if (fis != null) {
                // read
                long size = fis.size();
                obj = fis.read(theFactory, 0, (int)size);
                fis.readLock().unlock();
            }
        }  catch (Exception ex) {
            ex.printStackTrace();
        } 
        
        return obj;
    }
    
    public void remove(Key id) {
        assert id != null;
        
        theFilesHelper.removeObjForKey(id);
    }

    public void closeUnit(String unitName) {
        theFilesHelper.closeUnit(unitName);
    }

    public void shutdown() {
        theFilesHelper.setOpenFilesLimit(0);
        theFilesHelper.setOpenFilesLimit(openFilesLimit);
    }
}

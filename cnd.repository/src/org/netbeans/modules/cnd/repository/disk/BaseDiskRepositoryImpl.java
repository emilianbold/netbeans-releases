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
import org.netbeans.modules.cnd.repository.disk.api.RepFilesAccessStrategy;
import org.netbeans.modules.cnd.repository.sfs.ConcurrentFileRWAccess;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.util.RepositoryExceptionImpl;
import org.netbeans.modules.cnd.repository.util.RepositoryListenersManager;

/**
 * The implementation of the repository, which uses HDD
 * @author Nickolay Dalmatov 
 */
public class BaseDiskRepositoryImpl extends AbstractDiskRepository {
    
    final public static int    DEFAULT_REPOSITORY_OPEN_FILES_LIMIT = 20; 
    
    private RepFilesAccessStrategy theFilesHelper;
    private int openFilesLimit = DEFAULT_REPOSITORY_OPEN_FILES_LIMIT;
    
    /** Creates a new instance of BaseDiskRepository */
    public BaseDiskRepositoryImpl() {
        super();
        // use the simple helper implementation
        theFilesHelper = new SimpleRepFilesAccessStrategyImpl(DEFAULT_REPOSITORY_OPEN_FILES_LIMIT);
    }
    
    public void setOpenFilesLimit (int limit) throws IOException {
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
    
    public void write(Key id, final Persistent obj) {
        assert id != null;
        assert obj != null;
        
        // get the factory
        final PersistentFactory theFactory = id.getPersistentFactory();
        assert theFactory != null;
        ConcurrentFileRWAccess fos = null;
        try {
            fos = theFilesHelper.getFileForObj(id, false);
            if (fos != null) {
                int size = fos.write(theFactory, obj,0);
                fos.truncate(size);
            } 
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    id.getUnit().toString(), new RepositoryExceptionImpl(ex));
        } finally {
            if (fos != null) {
                fos.getLock().writeLock().unlock();
            }
        }
    }
    
    public boolean maintenance(long timeout) {
	return false;
    }
    
    public Persistent get(Key id) {
        assert id != null;
        Persistent obj = null;
        
        // get the factory
        final PersistentFactory theFactory = id.getPersistentFactory();
        assert theFactory != null;
        ConcurrentFileRWAccess fis = null;
        try {
            fis = theFilesHelper.getFileForObj(id, true);

            if (fis != null) {
                // read
                long size = fis.size();
                obj = fis.read(theFactory, 0, (int)size);
            }
        }  catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    id.getUnit().toString(), new RepositoryExceptionImpl(ex));
        } finally {
            if (fis != null) {
                fis.getLock().readLock().unlock();
            }
        }
        
        return obj;
    }
    
    public void remove(Key id) {
        assert id != null;
        try {
        theFilesHelper.removeObjForKey(id);
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    id.getUnit().toString(), new RepositoryExceptionImpl(ex));
        }
    }

    public void close() throws IOException {
        theFilesHelper.setOpenFilesLimit(0);
        theFilesHelper.setOpenFilesLimit(openFilesLimit);        
    }

    public int getFragmentationPercentage() {
        return 0;
    }
}

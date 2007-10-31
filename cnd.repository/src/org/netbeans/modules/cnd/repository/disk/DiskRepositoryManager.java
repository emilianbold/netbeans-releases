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
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.queue.RepositoryQueue;
import org.netbeans.modules.cnd.repository.queue.RepositoryThreadManager;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryListener;
import org.netbeans.modules.cnd.repository.translator.RepositoryTranslatorImpl;
import org.netbeans.modules.cnd.repository.util.RepositoryExceptionImpl;
import org.netbeans.modules.cnd.repository.util.RepositoryListenersManager;

/**
 *
 * @author Sergey Grinev
 */
public final class DiskRepositoryManager extends AbstractDiskRepository implements Repository {
    private final Map<String, UnitDiskRepository> repositories;
    
    private static DiskRepositoryManager    instance = new DiskRepositoryManager();
    private final RepositoryQueue           queue;
    private final RepositoryThreadManager   threadManager;
    private final Persistent                removedObject; 
    private final ReadWriteLock             rwLock;

    public static DiskRepositoryManager getInstance() {
        return instance;
    }
    
    private DiskRepositoryManager() {
        removedObject   = new RemovedPersistent();
        rwLock          = new ReentrantReadWriteLock(true);
        threadManager   = new RepositoryThreadManager(this, rwLock);
	queue           = threadManager.startup();
        repositories    = new ConcurrentHashMap<String, UnitDiskRepository>();
    }
    
    private AbstractDiskRepository getCreateRepository(Key key) throws IOException {
        assert key != null;

        final String unitName = key.getUnit();
        assert unitName != null;
        
        UnitDiskRepository repository = repositories.get(unitName);
            
        if (repository == null) {
            synchronized (repositories) {
                repository = repositories.get(unitName);
                if (repository == null) {
                    if (RepositoryListenersManager.getInstance().fireUnitOpenedEvent(unitName)) {
                        RepositoryTranslatorImpl.loadUnitIndex(unitName);
                        repository = new UnitDiskRepository(unitName);
                        repositories.put(unitName, repository);
                    }
                }
            }
        }
        
        return repository;
    }
    
    public void put(Key id, Persistent obj) {
	    queue.addLast(id, obj);
    }
    
    public void write(Key key, Persistent object) {
        try {
            AbstractDiskRepository diskRep = getCreateRepository(key);

            if (diskRep == null)
                return;
            
            if (object instanceof  RemovedPersistent) {
                diskRep.remove(key);
            } else {
                diskRep.write(key, object);
            }
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    key.getUnit(),new RepositoryExceptionImpl(ex));
        }
    }    
    
   public Persistent get(Key key) {
       assert key != null;

       try {
            AbstractDiskRepository diskRep = getCreateRepository(key);
            if (diskRep != null) {
                return diskRep.get(key);
            }
        } catch (Throwable ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    key.getUnit(),new RepositoryExceptionImpl(ex));
        }
       
       return null;
    }
    
    
    public void remove(Key key) {
        queue.addLast(key, removedObject);
    }    
    
    public void waitForQueue() throws InterruptedException {
        if (queue != null) {
            while (!queue.disposable()) {
                Thread.sleep(50);
                queue.onIdle();
            }
        }
    }
    
    public void shutdown() {
        if( threadManager != null ) {
            threadManager.shutdown();
        }
        
        close();
    }
    
    private void iterateWith(Visitor visitor){
        for (Entry<String, UnitDiskRepository> entry: repositories.entrySet()) {
            try {
                visitor.visit(entry.getValue());
            } catch (Throwable exc) {
                RepositoryListenersManager.getInstance().fireAnException(
                        entry.getKey(), new RepositoryExceptionImpl(exc));
            }
        }
    }



    public boolean maintenance(long timeout) {
            if( repositories.size() == 0 ) {
                return false;
            }

            Collection<UnitDiskRepository> values = repositories.values();
            UnitDiskRepository[] sfs = (UnitDiskRepository[]) values.toArray(new UnitDiskRepository[values.size()]);
            Arrays.sort(sfs, new FragmentationComparator());
            boolean needMoreTime = false;
            long start = System.currentTimeMillis();
            for (int i = 0; i < sfs.length; i++) {
                if( timeout <= 0 ) {
                    needMoreTime = true;
                    break;
                }
                
                if( sfs[i].maintenance(timeout) ) {
                    needMoreTime = true;
                }
                timeout -= (System.currentTimeMillis() - start);
            }
            return needMoreTime;
    }

    public void openUnit(String unitName) {
    }
 

    public void closeUnit(final String unitName, final boolean cleanRepository, Set<String> requiredUnits) {
        
        try {
            rwLock.writeLock().lock();
        
	    Collection<RepositoryQueue.Entry> removedEntries = queue.clearQueue(new UnitFilter(unitName));
            if (!cleanRepository) {
		for( RepositoryQueue.Entry entry : removedEntries ) {
		    write(entry.getKey(), entry.getValue());
		}
            }
            
            AbstractDiskRepository repository = repositories.remove(unitName);
            
            if (repository != null) {
                try {
                    repository.close();
                } catch (Throwable exc) {
                    RepositoryListenersManager.getInstance().fireAnException(unitName, 
                            new RepositoryExceptionImpl(exc));
                }
            }
            
        } finally {
            rwLock.writeLock().unlock();
        }
            
        //clean the repository cach files here if it is necessary
        //
        StorageAllocator allocator = StorageAllocator.getInstance();
        if (cleanRepository) {
            allocator.deleteUnitFiles(unitName, true);
        }
        allocator.closeUnit(unitName);
    }
    
    public void removeUnit(String unitName) {
	closeUnit(unitName, true, Collections.<String>emptySet());
    }

    public void close() {
        boolean saveAll = true;
        try {
            rwLock.writeLock().lock();
            cleanAndWriteQueue();
            iterateWith(new CloseVisitor());
            repositories.clear();
        } finally {
            rwLock.writeLock().unlock();
        }

    }

    public int getFragmentationPercentage() throws IOException {
        return 0;
    }

    public void hang(Key key, Persistent obj) {
    }

    public void debugClear() {
        try {
            rwLock.writeLock().lock();
            cleanAndWriteQueue();
        } finally {
            rwLock.writeLock().unlock();
        }        
    }
    
    private void cleanAndWriteQueue() {
	Collection<RepositoryQueue.Entry> removedEntries = queue.clearQueue(new AllFilter());
	for( RepositoryQueue.Entry entry : removedEntries ) {
	    write(entry.getKey(), entry.getValue());
	}
    }

    public void cleanCaches() {
        StorageAllocator.getInstance().cleanRepositoryCaches();
    }

    public void registerRepositoryListener(final RepositoryListener aListener) {
    }

    public void unregisterRepositoryListener(final RepositoryListener aListener) {
    }

    public void startup(int persistMechanismVersion) {
    }

    static private class RemovedPersistent implements Persistent {
        
    }

    private interface Visitor {
        void visit(AbstractDiskRepository repository) throws IOException ;
    }

    private static class UnitFilter implements RepositoryQueue.Filter {

        private String unitName;

        public UnitFilter(String unitName) {
            this.unitName = unitName;
        }

        public boolean accept(Key key, Persistent value) {
            return key.getUnit().equals(unitName);
        }
    }
    
   private class AllFilter implements RepositoryQueue.Filter {
        public boolean accept(Key key, Persistent value) {
            return true;
        }
    }    

    static private class CloseVisitor implements Visitor {

        public CloseVisitor() {
            super();
        }

        public void visit(AbstractDiskRepository repository) throws IOException {
            repository.close();
        }
    }
    
   private static class FragmentationComparator implements Comparator<UnitDiskRepository>, Serializable {
        private static final long serialVersionUID = 7249069246763182397L;

        public int compare(UnitDiskRepository o1, UnitDiskRepository o2) {
            return o2.getFragmentationPercentage() - o1.getFragmentationPercentage();
        }
    }    
}

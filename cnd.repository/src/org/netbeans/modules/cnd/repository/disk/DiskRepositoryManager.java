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

 

    public void closeUnit(final String unitName, final boolean cleanRepository, Set<String> requiredUnits) {
        
        try {
            rwLock.writeLock().lock();
        
            if (cleanRepository) {
                queue.clearQueue(new ValidatorCleaner(unitName));
            } else {
                queue.clearQueue(new ValidatorSaver(unitName));                
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
            queue.clearQueue(new ValidatorSaveAll(saveAll));  
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
            queue.clearQueue(new ValidatorSaveAll(true));  
        } finally {
            rwLock.writeLock().unlock();
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

    private static class ValidatorCleaner implements RepositoryQueue.Validator {

        private String unitName;

        public ValidatorCleaner(String unitName) {
            super();
            this.unitName = unitName;
        }

        public boolean isValid(Key key, Persistent value) {
            return !key.getUnit().equals(unitName);
        }
    }
    
   private class ValidatorSaveAll implements RepositoryQueue.Validator {
       private boolean saveAll;


        public ValidatorSaveAll(boolean saveAll) {
            super();
            this.saveAll = saveAll;
        }

        public boolean isValid(Key key, Persistent value) {
            if (this.saveAll) {
                write(key, value);
            }
            return false;
        }
    }    

    private class ValidatorSaver implements RepositoryQueue.Validator {

        private String unitName;

        public ValidatorSaver(String unitName) {
            super();
            this.unitName = unitName;
        }

        public boolean isValid(Key key, Persistent value) {
            boolean result = !key.getUnit().equals(unitName);
            if (!result) {
                write(key, value);
            }
            return result;
        }
    }

    private static class VisitorUnitCloser implements Visitor {

        private String unitName;

        private boolean cleanRepository;

        public VisitorUnitCloser(String unitName, boolean cleanRepository) {
            super();
            this.unitName = unitName;
            this.cleanRepository = cleanRepository;
        }

        public void visit(AbstractDiskRepository repository) throws IOException {
            repository.close();
        }
    }

    static private class MaintenanceVisitor implements Visitor {
            public boolean unfulfilled = false;
            private final long timeout;

            MaintenanceVisitor(final long timeout) {
                this.timeout = timeout;
            }
            
            public void visit(AbstractDiskRepository repository) throws IOException {
                    unfulfilled |= repository.maintenance(timeout);
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

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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.repository.queue.RepositoryQueue;
import org.netbeans.modules.cnd.repository.queue.RepositoryThreadManager;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;

/**
 *
 * @author Sergey Grinev
 */
public final class DiskRepositoryManager extends AbstractDiskRepository {
    
    private static DiskRepositoryManager    instance = new DiskRepositoryManager();
    private final RepositoryQueue           queue;
    private final RepositoryThreadManager   threadManager;
    private final AbstractDiskRepository    defBehRepository;
    private final AbstractDiskRepository    nonDefBehRepository;
    private final Persistent                removedObject; 
    private final ReadWriteLock             rwLock = new ReentrantReadWriteLock(true);

    public static DiskRepositoryManager getInstance() {
        return instance;
    }
    
    private DiskRepositoryManager() {
        defBehRepository = new FilePerUnitDiskRepositoryImpl();
        BaseDiskRepositoryImpl bdri = new BaseDiskRepositoryImpl();
        removedObject  = new RemovedPersistent();
        bdri.setOpenFilesLimit(30);
        nonDefBehRepository = bdri;
        threadManager = new RepositoryThreadManager(this, rwLock);
	queue = threadManager.startup();
    }
    
    private AbstractDiskRepository getRepository(Key key) {
        if (key.getBehavior() == Key.Behavior.Default) {
            return defBehRepository;
        } else {
            return nonDefBehRepository;
        }
    }
    
    public void put(Key id, Persistent obj) {
	    queue.addLast(id, obj);
    }
    
    public void waitForQueue() throws InterruptedException {
        if (queue != null) {
            while (!queue.disposable()) {
                Thread.sleep(50);
                queue.onIdle();
            }
        }
    }
    
    public void shutdown(final boolean clean) throws IOException {
	if( threadManager != null ) {
	    threadManager.shutdown();
	}
        iterateWith(new Visitor() {
            public void visit(AbstractDiskRepository repository) throws IOException {
                repository.shutdown(clean);
            }
        });
    }
    
    private void iterateWith(Visitor visitor) throws IOException{
        visitor.visit(defBehRepository);
        visitor.visit(nonDefBehRepository);
    }

    public void write(Key key, Persistent object) throws IOException {
        if (object instanceof  RemovedPersistent) {
            getRepository(key).remove(key);
        } else {
            getRepository(key).write(key, object);
        }
    }

    public boolean maintenance(final long timeout) throws IOException {
        class MaintenanceVisitor implements Visitor {
            public boolean unfulfilled = false;
            public IOException exception;
            public void visit(AbstractDiskRepository repository) {
                try {
                    unfulfilled |= repository.maintenance(timeout);
                } catch (IOException ex) {
                    this.exception = ex;
                }
            }
        }
        MaintenanceVisitor mv = new MaintenanceVisitor();
        iterateWith(mv);
        if (mv.exception != null)
            throw mv.exception;
        return mv.unfulfilled;
    }

    public Persistent get(Key key) throws IOException {
        return getRepository(key).get(key);
    }
    


    
    public void remove(Key key) {
        queue.addLast(key, removedObject);
//        getRepository(key).remove(key);
    }

    public void closeUnit(final String unitName, final boolean cleanRepository) throws IOException {
        
        try {
            rwLock.writeLock().lock();
        
            if (cleanRepository) {
                
                queue.clearQueue(new RepositoryQueue.Validator() {
                    public boolean isValid(Key key, Persistent value) {
                        return !key.getUnit().equals(unitName);
                    }
                });
                
            } else {
                
                queue.clearQueue(new RepositoryQueue.Validator() {
                    public boolean isValid(Key key, Persistent value) {
                        boolean result = !key.getUnit().equals(unitName);
                        
                        if (!result) {
                            try {
                                getRepository(key).write(key, value);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                        return result;
                    }
                });                
            }
            
            iterateWith(new Visitor() {
                public void visit(AbstractDiskRepository repository) throws IOException {
                    repository.closeUnit(unitName, cleanRepository);
                }
            });
        } finally {
            rwLock.writeLock().unlock();
        }
            
        //clean the repository cach files here if it is necessary
        //
        StorageAllocator allocator = StorageAllocator.getInstance();
        if (cleanRepository) {
            allocator.deleteUnitFiles(unitName);
        }
        allocator.closeUnit(unitName);
    }
    
    private class RemovedPersistent implements Persistent {
        
    }

    private interface Visitor {
        void visit(AbstractDiskRepository repository) throws IOException ;
    }
}

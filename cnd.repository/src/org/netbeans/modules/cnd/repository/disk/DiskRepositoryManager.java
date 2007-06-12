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
import org.netbeans.modules.cnd.repository.impl.*;
import org.netbeans.modules.cnd.repository.queue.RepositoryQueue;
import org.netbeans.modules.cnd.repository.queue.RepositoryThreadManager;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 *
 * @author Sergey Grinev
 */
public final class DiskRepositoryManager extends AbstractDiskRepository {
    
    private static DiskRepositoryManager instance = new DiskRepositoryManager();
    private RepositoryQueue queue;
    private RepositoryThreadManager threadManager;

    public static DiskRepositoryManager getInstance() {
        return instance;
    }
    
//    private Map<Key.Behavior, AbstractDiskRepository> repositories = new HashMap<Key.Behavior, AbstractDiskRepository>();
//    public void registerRepository(Key.Behavior behavior, AbstractDiskRepository repository) {
//        repositories.put(behavior, repository);
//    }

    private AbstractDiskRepository defaultRepository;
    private AbstractDiskRepository bdri;
    
    private DiskRepositoryManager() {
        defaultRepository = new FilePerUnitDiskRepositoryImpl();
        if (!Stats.writeToASingleFile) {
            BaseDiskRepositoryImpl bdri = new BaseDiskRepositoryImpl();
            bdri.setOpenFilesLimit(30);
            this.bdri = bdri;
        }
	if( Stats.useThreading ) {
	    threadManager = new RepositoryThreadManager(this);
	    queue = threadManager.startup();
	}
    }
    
    private AbstractDiskRepository getRepository(Key key) {
        if (key.getBehavior() == Key.Behavior.Default || Stats.writeToASingleFile) {
            return defaultRepository;
        } else {
            return bdri;
        }
    }
    
    public void put(Key id, Persistent obj) {
	if( Stats.useThreading ) {
	    queue.addLast(id, obj);
	} else {
	    try {
		write(id, obj);
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	}
    }
    
    public void waitForQueue() throws InterruptedException {
        if (queue != null) {
            while (!queue.disposable()) {
                Thread.sleep(50);
                queue.onIdle();
            }
        }
    }
    
    public void shutdown(final boolean clean) {
	if( threadManager != null ) {
	    threadManager.shutdown();
	}
        iterateWith(new Visitor() {
            public void visit(AbstractDiskRepository repository) {
                repository.shutdown(clean);
            }
        });
    }
    
    private void iterateWith(Visitor visitor) {
//        for (AbstractDiskRepository repository : repositories.values()) {
//            visitor.visit(repository)
//        }
        visitor.visit(defaultRepository);
        if (!Stats.writeToASingleFile) {
	    visitor.visit(bdri);
        }
    }

    private Object lockClearWrite = new String("Repository ClearWrite lock");
    
    private Key currentKey;
    
    public void write(Key key, Persistent object) throws IOException {
        currentKey = key;
        synchronized ( lockClearWrite ) {
            if (currentKey != null) {
                getRepository(currentKey).write(currentKey, object);
            }
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

    public Persistent get(Key key) {
        return getRepository(key).get(key);
    }

    public void remove(Key key) {
        getRepository(key).remove(key);
    }

    public void closeUnit(final String unitName, final boolean cleanRepository) {
        synchronized ( lockClearWrite ) {
            
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
                public void visit(AbstractDiskRepository repository) {
                    repository.closeUnit(unitName, cleanRepository);
                }
            });
            
            //clean the repository cach files here if it is necessary
            //
            StorageAllocator allocator = StorageAllocator.getInstance();
            if (cleanRepository) {
                allocator.deleteUnitFiles(unitName);
            }
            
            allocator.closeUnit(unitName);
            
            if (currentKey != null && unitName.equals(currentKey.getUnit())) {
                currentKey = null;
            }
        }
    }

    private interface Visitor {
        void visit(AbstractDiskRepository repository) ;
    }
}

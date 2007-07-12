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
import org.netbeans.modules.cnd.repository.sfs.FileStorage;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.util.RepositoryExceptionImpl;
import org.netbeans.modules.cnd.repository.util.RepositoryListenersManager;

/**
 *
 * @author Nickolay Dalmatov
 */
public class UnitDiskRepository extends AbstractDiskRepository {
    private AbstractDiskRepository    defBehRepository;
    private AbstractDiskRepository    nonDefBehRepository;
    private String                    unitName;
    
    /** Creates a new instance of UnitDiskRepository */
    public UnitDiskRepository(final String unitName) throws IOException {
       assert unitName != null;
       
       this.unitName = unitName;
       defBehRepository = FileStorage.create(unitName); 
       nonDefBehRepository = new BaseDiskRepositoryImpl();
    }
    
    protected AbstractDiskRepository getRepository(Key key) {
        assert key != null;
        
        if (key.getBehavior() == Key.Behavior.Default) {
            return defBehRepository;
        } else {
            return nonDefBehRepository;
        }
    }

    public Persistent get(Key key) throws IOException {
        assert key != null;
        return getRepository(key).get(key);
    }

    public void remove(Key key) throws IOException {
        assert key != null;
        getRepository(key).remove(key);
    }

    public void close() throws IOException {
        assert unitName != null;
        assert unitName.equals(this.unitName);
        
        defBehRepository.close();
        nonDefBehRepository.close();
    }

    public void write(Key key, Persistent object) {
        assert key != null;
        assert object != null;
        try {
            getRepository(key).write(key, object);
        } catch (Exception ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    unitName, new RepositoryExceptionImpl(ex));
        }
    }

    public boolean maintenance(long timeout) {
        boolean needMoreTime = false;
        try {
            needMoreTime |= defBehRepository.maintenance(timeout);
        } catch (Exception ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    unitName, new RepositoryExceptionImpl(ex));
        }
        return needMoreTime;
    }

    public int getFragmentationPercentage() {
        try {
            return defBehRepository.getFragmentationPercentage();
        } catch (IOException ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    unitName, new RepositoryExceptionImpl(ex));
        }
        return 0;
    }
}

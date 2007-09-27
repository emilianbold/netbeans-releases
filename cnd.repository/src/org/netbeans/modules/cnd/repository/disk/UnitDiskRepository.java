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

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
import org.netbeans.modules.cnd.repository.sfs.FileStorage;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.util.RepositoryExceptionImpl;
import org.netbeans.modules.cnd.repository.util.RepositoryListenersManager;

/**
 *
 * @author Nickolay Dalmatov
 */
public class UnitImpl implements Unit {
    
    private Storage    singleFileStorage;
    private Storage    multyFileStorage;
    private String  unitName;
    
    public UnitImpl(final String unitName) throws IOException {
       assert unitName != null;
       
       this.unitName = unitName;
       singleFileStorage = FileStorage.create(new File(StorageAllocator.getInstance().getUnitStorageName(unitName)));
       multyFileStorage = new MultyFileStorage(this.getName());
    }
    
    protected Storage getStorage(Key key) {
        assert key != null;
        
        if (key.getBehavior() == Key.Behavior.Default) {
            return singleFileStorage;
        } else {
            return multyFileStorage;
        }
    }

    public Persistent get(Key key) throws IOException {
        assert key != null;
        return getStorage(key).get(key);
    }

    public void remove(Key key) throws IOException {
        assert key != null;
        getStorage(key).remove(key);
    }

    public void close() throws IOException {
        assert unitName != null;
        assert unitName.equals(this.unitName);
        
        singleFileStorage.close();
        multyFileStorage.close();
    }

    public void write(Key key, Persistent object) {
        assert key != null;
        assert object != null;
        try {
            getStorage(key).write(key, object);
        } catch (Exception ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    unitName, new RepositoryExceptionImpl(ex));
        }
    }

    public boolean defragment(long timeout) {
        boolean needMoreTime = false;
        try {
            needMoreTime |= singleFileStorage.defragment(timeout);
        } catch (Exception ex) {
            RepositoryListenersManager.getInstance().fireAnException(
                    unitName, new RepositoryExceptionImpl(ex));
        }
        return needMoreTime;
    }

    public int getFragmentationPercentage() throws IOException {
        return singleFileStorage.getFragmentationPercentage();
    }

    public String getName() {
        return unitName;
    }
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.db.derby;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.impl.SQLDataStorageFactory;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author masha
 */
@ServiceProvider(service = org.netbeans.modules.dlight.spi.storage.DataStorageFactory.class, position = 100)
public final class DerbyDataStorageFactory extends SQLDataStorageFactory<DerbyDataStorage> {

    static final String DERBY_DATA_STORAGE_TYPE = "db:sql:derby"; // NOI18N
    private final Collection<DataStorageType> supportedStorageTypes = new ArrayList<DataStorageType>();

    public DerbyDataStorageFactory() {
        supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType(DERBY_DATA_STORAGE_TYPE));
        supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID));
        supportedStorageTypes.addAll(super.getStorageTypes());
    }

    @Override
    public Collection<DataStorageType> getStorageTypes() {
        return supportedStorageTypes;
    }

    @Override
    public DerbyDataStorage createStorage() {
        try {
            return new DerbyDataStorage();
        } catch (SQLException ex) {
            DLightLogger.getLogger(DerbyDataStorageFactory.class).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}

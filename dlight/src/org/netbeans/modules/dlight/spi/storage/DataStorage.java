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
package org.netbeans.modules.dlight.spi.storage;

import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import java.util.Collection;
import java.util.List;

/**
 * DataStorage stores information collected by DataCollectors.
 *
 * {@link org.netbeans.modules.dlight.spi.storage.DataStorageType} is
 * used to identify the way of communication with DataStorage
 * thus {@link org.netbeans.modules.dlight.spi.collector.DataCollector}
 * that want to store some information into the DataStorage
 * need to support the same {@link org.netbeans.modules.dlight.spi.storage.DataStorageType}.
 * The same is for {@link org.netbeans.modules.dlight.spi.dataprovider.DataProvider},
 * that wants to read data from the DataStorage.
 */
public interface DataStorage {

    /**
     * Checks if storage contains data described by <param>data</param>
     * @param data data to check in storage. In case <param>data</param>
     * describes some virtual table and its {@link org.netbeans.modules.dlight.api.storage.DataTableMetadata#getSourceTables()} method
     * returns not empty list, this method will check if all source tables this virtual table(view)
     * is built from  exists in the storage.
     * @return <code>true</code> if storage contains <param>data</param>,
     * <code>false</code> otherwise
     */
    boolean hasData(DataTableMetadata data);

    /**
     * Adds rows <code>data</code> to the table with name <code>tableName</code> of this
     * soprage.
     * @param tableName table name to add data into
     * @param data data to add
     */
    void addData(String tableName, List<DataRow> data);

    /**
     * Please be sure this method will return not null for the object
     * which will be registered in Services (using default public constructor
     * without parameter)
     * @return storage types
     */
    Collection<DataStorageType> getStorageTypes();

    /**
     * Checks if DataStorage supports {@link org.netbeans.modules.dlight.spi.storage.DataStorageType}
     * @param storageType storage type to check
     * @return <code>true</code> if storage supports storageType, <code>false</code> otherwise
     */
    boolean supportsType(DataStorageType storageType);

    /**
     * Creates tables: invoked to create {@link org.netbeans.modules.dlight.api.storage.DataTableMetadata} needed
     * @param tableMetadatas tables decsription to create in the storage
     */
    void createTables(List<DataTableMetadata> tableMetadatas);

    /**
     * Close storage
     * @return <code>true</code> if succeeded, <code>false</code> otherwise
     */
    boolean shutdown();

}

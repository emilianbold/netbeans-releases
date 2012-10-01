/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.cnd.repository.translator;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.repository.api.RepositoryTranslation;
import org.netbeans.modules.cnd.repository.disk.StorageAllocator;
import org.netbeans.modules.cnd.repository.util.IntToStringCache;
import org.netbeans.modules.cnd.repository.util.UnitCodec;

/**
 * This class is responsible for int <-> String translation for both
 *  1) file names
 *  2) unit names
 * It is also responsible for master index processing
 * and the required units verification.
 * 
 * The required units issue is caused by the following two circumstances:
 * a) Each unit stores its own int to string table that is used for decoding its keys
 * b) A unit can store other units (required units) keys
 * 
 * By required units verification we prevent the following situation
 * (which otherwise causes a huge mess).
 * Consider two projects, App1, App2 and a library Lib  * that is required for both App1 and App2.
 * User performs the following steps:
 * 1) Opens App1 - App1 and Lib persistence is created. Then closes IDE.
 * 2) Now Lib persistence is erased (well, if user makes this by hands, we aren't responsible...
 * but it can happen if user opens Lib and IDE exits abnormally - then upon  Lib reopen
 * its persistence is erased)
 * 3) User opens App2 - Lib persistence is recreated 
 * 4) User opens App1 again.
 * Now App1 contains Lin keys, but Lib now contain int/string tables that are quite different!!!
 * 
 * To prevent this situation, the following algorithm is used:
 * - Each unit's int/string table has a timestamp of its creation.
 * - When a unit closes, it stores all required units timestamps.
 * - When a unit opens, it checks that required units timestamps are the same
 * as they were at closure. Otherwise persistence is invalidated (erased)
 * for main unit and requires units.
 * 
 * @author Nickolay Dalmatov
 */
public class RepositoryTranslatorImpl {

    /**
     * It is 
     * 1) an int/string table of the unit names
     * 2) a container for int/string table for each unit's file names (a table per unit)
     * (stores units timestamps as well)
     */
    private UnitsCache unitNamesCache = null;
    private final Object initLock = new Object();
    private boolean loaded = false;
    private final StorageAllocator storageAllocator;
    private final UnitCodec unitCodec;

    private static final int DEFAULT_VERSION_OF_PERSISTENCE_MECHANIZM = 0;
    private static int version = DEFAULT_VERSION_OF_PERSISTENCE_MECHANIZM;

    /** Creates a new instance of RepositoryTranslatorImpl */
    public RepositoryTranslatorImpl(StorageAllocator storageAllocator, UnitCodec unitCodec) {
        this.storageAllocator = storageAllocator;
        this.unitCodec = unitCodec;
    }

    public int getFileIdByName(int unitId, final CharSequence fileName) {
        assert fileName != null;
        unitId = unitCodec.removeRepositoryID(unitId);
        final IntToStringCache unitFileNames = getUnitFileNames(unitId);
        return unitFileNames.getId(fileName);
    }

    public CharSequence getFileNameById(int unitId, final int fileId) {
        unitId = unitCodec.removeRepositoryID(unitId);
        final IntToStringCache fileNames = getUnitFileNames(unitId);
        // #215449 - IndexOutOfBoundsException in RepositoryTranslatorImpl.getFileNameById
        if (fileNames.size() <= fileId) {
            StringBuilder message = new StringBuilder();
            message.append("Unit: ").append(getUnitName(unitId)); //NOI18N
            message.append(" FileIndex: ").append(fileId); //NOI18N
            message.append(" CacheSize: ").append(fileNames.size()); //NOI18N
            StackTraceElement[] cacheCreationStack = fileNames.getCreationStack();
            if (cacheCreationStack == null) {
                throw new IllegalArgumentException(message.toString());
            } else {
                Exception cause = new Exception("Files cache creation stack"); //NOI18N
                cause.setStackTrace(cacheCreationStack);
                throw new IllegalArgumentException(message.toString(), cause);
            }
        }
        final CharSequence fileName = fileNames.getValueById(fileId);
        return fileName;
    }

    public CharSequence getFileNameByIdSafe(int unitId, final int fileId) {
        unitId = unitCodec.removeRepositoryID(unitId);
        final IntToStringCache fileNames = getUnitFileNames(unitId);
        final CharSequence fileName = fileNames.containsId(fileId) ? fileNames.getValueById(fileId) : "?"; // NOI18N
        return fileName;
    }

    public int getUnitId(CharSequence unitName) {
        if (!unitNamesCache.containsValue(unitName)) {
            // NB: this unit can't be open (since there is no such unit in unitNamesCache)
            // so we are just removing some ocassionally existing in persisntence files
            storageAllocator.deleteUnitFiles(unitName, false);
        }
        int unitId = unitNamesCache.getId(unitName);
        unitId = unitCodec.addRepositoryID(unitId);
        return unitId;
    }

    public CharSequence getUnitName(int unitId) {
        unitId = unitCodec.removeRepositoryID(unitId);
        return unitNamesCache.getValueById(unitId);
    }

    public CharSequence getUnitNameSafe(int unitId) {
        unitId = unitCodec.removeRepositoryID(unitId);
        return unitNamesCache.containsId(unitId) ? unitNamesCache.getValueById(unitId) : "No Index " + unitId + " in " + unitNamesCache; // NOI18N
    }

    public static int getVersion() {
        return version;
    }

    public void closeUnit(CharSequence unitName, Set<CharSequence> requiredUnits) {
        if (requiredUnits != null) {
            unitNamesCache.updateReqUnitInfo(unitName, requiredUnits);
        }
        unitNamesCache.storeUnitIndex(unitName);
        unitNamesCache.removeFileNames(unitName);
    }

    public void shutdown() {
        unitNamesCache.storeMasterIndex();
        storageAllocator.purgeCaches();
    }

    public void loadUnitIndex(final CharSequence unitName) {
        unitNamesCache.loadUnitIndex(unitName, new HashSet<CharSequence>());
    }

    public void removeUnit(final CharSequence unitName) {
        unitNamesCache.removeUnit(unitName);
    }

    public void startup(int newVersion, UnitCodec unitCodec) {
        version = newVersion;
        init(unitCodec);
    }

    private IntToStringCache getUnitFileNames(int unitId) {
        unitId = unitCodec.removeRepositoryID(unitId);
        return unitNamesCache.getFileNames(unitId);
    }

    private void init(UnitCodec unitCodec) {
        boolean aLoaded = loaded;
        if (!aLoaded) {
            synchronized (initLock) {
                if (!loaded) {
                    unitNamesCache = new UnitsCache(storageAllocator, unitCodec);
                    loaded = true;
                }
            }
        }
    }
}

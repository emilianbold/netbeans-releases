/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.repository.disk;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptions;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.util.IntToValueList;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;

/**
 *
 * @author akrasny
 */
public final class LayerIndex {

    private final File cacheDirectoryFile;
    private int version;
    private static final String INDEX_FILE_NAME = "index";//NOI18N
    private static final String UNITS_INDEX_FILE_NAME = "project-index";//NOI18N
    private long lastModificationTime;
    private final List<FileSystem> fileSystems = new ArrayList<FileSystem>();
    private final List<UnitDescriptor> units = new ArrayList<UnitDescriptor>();
    private final Map<Integer, IntToValueList<CharSequence>> fileNamesTable = new HashMap<Integer, IntToValueList<CharSequence>>();
    private final Map<Integer, List<Integer>> dependencies = new HashMap<Integer, List<Integer>>();

    LayerIndex(URI cacheDirectory) {
        this.cacheDirectoryFile = new File(cacheDirectory.getRawPath());
    }

    boolean load(int persistMechanismVersion, boolean recreate, boolean recreateOnFail) {
        this.version = persistMechanismVersion;

        File indexFile = new File(cacheDirectoryFile, INDEX_FILE_NAME); 
        if (recreate) {
            if (cacheDirectoryFile.canWrite()) {
                RepositoryImplUtil.deleteDirectory(cacheDirectoryFile, false);
                fileSystems.clear();
                units.clear();
                dependencies.clear();
                return true;        
            } 
            return false;
        }

        // If no index file - it's OK.
        if (!indexFile.exists()) {
            return true;
        }

        DataInputStream in = null;
        try {
            in = RepositoryImplUtil.getBufferedDataInputStream(indexFile);
            int storedVersion = in.readInt();
            if (storedVersion != persistMechanismVersion) {
                if (recreateOnFail) {
                    RepositoryImplUtil.deleteDirectory(cacheDirectoryFile, false);
                    fileSystems.clear();
                    units.clear();
                    dependencies.clear();
                    return true;
                }                
                return false;
            }
            lastModificationTime = in.readLong();
            int unitsCount = in.readInt();
            int fileSystemsCount = in.readInt();

            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                trace("Reading master index (%d) units\n", unitsCount); // NOI18N
                trace("Reading master index (%d) fileSystems\n", fileSystemsCount); // NOI18N
            }

            // Read list of filesystems
            for (int i = 0; i < fileSystemsCount; i++) {
                String fs = in.readUTF();
                fileSystems.add(CndFileUtils.decodeFileSystem(fs));
            }

            for (int i = 0; i < unitsCount; i++) {
                String unitName = in.readUTF();
                int fsIdx = in.readInt();
                long unitModificationTime = in.readLong();
                if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                    trace("\tRead %s@%s ts=%d\n", unitName, fileSystems.get(fsIdx), unitModificationTime); // NOI18N
                }
                units.add(new UnitDescriptor(unitName, fileSystems.get(fsIdx)));
                List<Integer> depList = new ArrayList<Integer>();
                int depListSize = in.readInt();
                for (int j = 0; j < depListSize; j++) {
                    int unitID = in.readInt();
                    long ts = in.readLong();
                    // TODO: timestamp for validation!
                    depList.add(unitID);
                }

                dependencies.put(i, depList);
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        if (recreateOnFail) {
            RepositoryImplUtil.deleteDirectory(cacheDirectoryFile, false);
            fileSystems.clear();
            units.clear();
            dependencies.clear();
            return true;
        }

        return false;
    }

    void store() throws IOException {
        final long currentTime = System.currentTimeMillis();
        File indexFile = new File(cacheDirectoryFile, INDEX_FILE_NAME); 
        DataOutputStream out = null;
        try {
            out = RepositoryImplUtil.getBufferedDataOutputStream(indexFile);
            out.writeInt(version);
            out.writeLong(currentTime);

            int unitsCount = units.size();
            out.writeInt(unitsCount);

            int fileSystemsCount = fileSystems.size();
            out.writeInt(fileSystemsCount);

            // Read list of filesystems
            for (int i = 0; i < fileSystemsCount; i++) {
                out.writeUTF(CndFileUtils.codeFileSystem(fileSystems.get(i)).toString());
            }

            for (int i = 0; i < unitsCount; i++) {
                UnitDescriptor ud = units.get(i);
                out.writeUTF(ud.getName().toString());
                out.writeInt(fileSystems.indexOf(ud.getFileSystem()));

                out.writeLong(currentTime); // TODO: This is wrong ;)

                List<Integer> depMap = dependencies.get(i);
                out.writeInt(depMap == null ? 0 : depMap.size());
                if (depMap != null) {
                    for (Integer depID : depMap) {
                        out.writeInt(depID);
                        out.writeLong(currentTime);
                    }
                }
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private void trace(String format, Object... args) {
        Object[] newArgs = new Object[args.length + 1];
        newArgs[0] = Long.valueOf(System.currentTimeMillis());
        System.arraycopy(args, 0, newArgs, 1, args.length);
        System.err.printf("MasterIndex [%d] " + format, newArgs);
    }

    List<UnitDescriptor> getUnitsTable() {
        return Collections.unmodifiableList(units);
    }

    List<FileSystem> getFileSystemsTable() {
        return Collections.unmodifiableList(fileSystems);
    }

    // 5
    List<CharSequence> getFileNameTable(int unitIdx) {
        final IntToValueList<CharSequence> result = fileNamesTable.get(unitIdx);
        return result == null ? Collections.<CharSequence>emptyList() : Collections.unmodifiableList(result.getTable());
    }

    void loadUnitIndex(int unitIdx) throws IOException {
        File indexFile = getUnitIndexFile(unitIdx);
        if (!indexFile.exists()) {
            return;
        }

        DataInputStream in = null;
        try {
            in = RepositoryImplUtil.getBufferedDataInputStream(indexFile);
            IntToValueList<CharSequence> filesCache = IntToValueList.<CharSequence>createFromStream(
                    in, units.get(unitIdx).getName(), IntToValueList.CHAR_SEQUENCE_FACTORY);
            fileNamesTable.put(unitIdx, filesCache);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    void storeFilesTable(Integer unitIDInLayer, List<CharSequence> filesList) throws IOException {
        File indexFile = getUnitIndexFile(unitIDInLayer);
        DataOutputStream out = null;
        try {
            out = RepositoryImplUtil.getBufferedDataOutputStream(indexFile);
            IntToValueList<CharSequence> list = IntToValueList.<CharSequence>createEmpty(units.get(unitIDInLayer).getName());
            int i = 0;
            for (CharSequence file : filesList) {
                list.set(i++, file);
            }
            list.write(out);
        } catch (FileNotFoundException ex) {
            RepositoryExceptions.throwException(this, ex);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private File getUnitIndexFile(int unitIdx) {
        File unitDir = new File(cacheDirectoryFile, "" + unitIdx); // NOI18N
        if (!unitDir.exists()) {
            unitDir.mkdirs();
        }
        return new File(unitDir, UNITS_INDEX_FILE_NAME);
    }

    int registerUnit(UnitDescriptor unitDescriptor) {
        units.add(unitDescriptor);
        return units.size() - 1;
    }

    void removeUnit(int unitIDInLayer) {
        if (unitIDInLayer >= units.size()) {
            return;
        }
        units.remove(unitIDInLayer);
    }

    int registerFile(int unitIDInLayer, CharSequence fileName) {
        List<CharSequence> table = getFileNameTable(unitIDInLayer);
        table.add(fileName);
        return table.size() - 1;
    }

    int registerFileSystem(FileSystem fileSystem) {
        fileSystems.add(fileSystem);
        return fileSystems.size() - 1;
    }

    void closeUnit(int unitIdx, boolean cleanRepository, Set<Integer> requiredUnits) {
        dependencies.put(unitIdx, requiredUnits == null ? new ArrayList<Integer>() : new ArrayList<Integer>(requiredUnits));
    }
}

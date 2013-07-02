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

package org.netbeans.modules.cnd.repository.disk;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.repository.api.CacheLocation;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.util.RepositoryListenersManager;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 * @author Sergey Grinev
 */
public class StorageAllocator {

    private final File diskRepository;
    
    public StorageAllocator(CacheLocation cacheLocation) {
        diskRepository = cacheLocation.getLocation();
    };

    private Map<CharSequence, String> unit2path = new ConcurrentHashMap<CharSequence, String>();
    
    public String reduceString (String name) {
        if (name.length() > 128) {
            int hashCode = name.hashCode();
            name = name.substring(0,64) + "--" + name.substring(name.length() - 32); // NOI18N
            name += hashCode;
        }
        return name;
    }

    public String getUnitStorageName(CharSequence unit) {
        String path = unit2path.get(unit);
        if (path == null) {
            String prefix = unit.toString();
            try {
                prefix = URLEncoder.encode(prefix, Stats.ENCODING);
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace(System.err);
            } 
            
            prefix = reduceString(prefix);
            
            File pathFile = new File(getUnitCacheBaseDirectory(unit), prefix);

            path = pathFile + File.separator;
            
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }

            unit2path.put(unit, path);
        }
        return path;
    }
    
    public void closeUnit(CharSequence unitName) {
        unit2path.remove(unitName);
    }
    
    public boolean renameUnitDirectory (int unitId, CharSequence oldUnitName, CharSequence newUnitName) {
        deleteUnitFiles(newUnitName, true);
        File newUnitStorage = new File(getUnitStorageName(newUnitName));
        File oldUnitStorage = new File(getUnitStorageName(oldUnitName));
        return oldUnitStorage.renameTo(newUnitStorage);
    }

    private void deleteUnitFiles(CharSequence unitName, boolean removeUnitFolder) {
	if( Stats.TRACE_UNIT_DELETION ) { System.err.printf("Deleting unit files for %s\n", unitName); }
        String path = getUnitStorageName(unitName);
        File pathFile = new File (path);
        deleteDirectory(pathFile, removeUnitFolder);
    }

    public void deleteUnitFiles (int unitId, CharSequence unitName, boolean removeUnitFolder) {
        deleteUnitFiles(unitName, removeUnitFolder);
        RepositoryListenersManager.getInstance().fireUnitRemovedEvent(unitId, unitName);
    }
    
    public static void deleteDirectory(File path, boolean deleteDir) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            if (files != null) {
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i], true);
                    } else {
                        if (!files[i].delete()) {
                            if (!CndUtils.isUnitTestMode() || Stats.TRACE_IZ_224249) {
                                System.err.println("Cannot delete repository file "+files[i].getAbsolutePath());
                                if (Stats.TRACE_IZ_224249) {
                                    CndUtils.threadsDump();
                                }
                            }
                        }
                    }
                }
            }
            if (deleteDir) {
                if (!path.delete()) {
                    System.err.println("Cannot delete repository folder "+path.getAbsolutePath());
                }
            }
        }
    }
    public void cleanRepositoryCaches() {
        deleteDirectory(diskRepository, false);
    }

    /**
     * Finds and deletes outdated cache entries. All directories that
     * have not been modified within last 2 weeks are considered outdated.
     */
    public void purgeCaches() {
        File[] unitDirs = diskRepository.listFiles();
        if (unitDirs != null && 0 < unitDirs.length) {
            long now = System.currentTimeMillis();
            for (File unitDir : unitDirs) {
                if (unitDir.isDirectory() && unitDir.lastModified() + PURGE_TIMEOUT < now) {
                    if (Stats.TRACE_UNIT_DELETION) { System.err.println("Purging outdated unit directory " + unitDir); }
                    deleteDirectory(unitDir, true);
                }
            }
        }
    }

    public File getCacheBaseDirectory() {
        return diskRepository;
    }

    private File getUnitCacheBaseDirectory(CharSequence unit) {
//        RepositoryCacheDirectoryProvider provider = Lookup.getDefault().lookup(RepositoryCacheDirectoryProvider.class);
//        if (provider != null) {
//            File dir = provider.getUnitCacheBaseDirectory(unit);
//            if (dir != null) {
//                return dir;
//            }
//        }
        return diskRepository;
    }

    private static final long PURGE_TIMEOUT = 14 * 24 * 3600 * 1000l; // 14 days
}

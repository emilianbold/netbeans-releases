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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 *
 * @author Sergey Grinev
 */
public class StorageAllocator {
    private final static StorageAllocator instance = new StorageAllocator();
    private String diskRepositoryPath;
    
    private StorageAllocator() {
        diskRepositoryPath = System.getProperty("cnd.repository.cache.path");
        if (diskRepositoryPath == null) {
            long index = 0;
            diskRepositoryPath = System.getProperty("java.io.tmpdir");
            
            diskRepositoryPath += File.separator +         //NOI18N
                    System.getProperty("user.name") +  "-cnd65-caches-";  //NOI18N
            
            File diskRepositoryFile = new File(diskRepositoryPath + index);
            // find name for directory which is not occupied by file

            while (diskRepositoryFile.exists() && !diskRepositoryFile.isDirectory()) {
                diskRepositoryFile = new File(diskRepositoryPath + ++index);
            }
            // create directory if needed
            if (!diskRepositoryFile.exists()) {
                diskRepositoryFile.mkdirs();
            }
            diskRepositoryPath = diskRepositoryFile.getAbsolutePath();
            
            //System.out.println("Repository location is " + diskRepositoryPath);
        }
    };
    
    public static StorageAllocator getInstance() {
        return instance;
    }
    
    private Map<String, String> unit2path = new ConcurrentHashMap<String, String>();
    
    public String getCachePath() {
        return diskRepositoryPath;
    }
    
    public String reduceString (String name) {
        if (name.length() > 128) {
            int hashCode = name.hashCode();
            name = name.substring(0,64) + "--" + name.substring(name.length() - 32); // NOI18N
            name += hashCode;
        }
        return name;
    }

    public String getUnitStorageName(String unit) {
        String path = unit2path.get(unit);
        if (path == null) {
            String prefix = unit;
            try {
                prefix = URLEncoder.encode(unit, Stats.ENCODING);
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            } 
            
            prefix = reduceString(prefix);
            
            path = getCachePath() + File.separator + prefix + File.separator; // NOI18N
            
            File pathFile = new File (path);
            
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }

            unit2path.put(unit, path);
        }
        return path;
    }
    
    public void closeUnit(String unitName) {
        unit2path.remove(unitName);
    }
    
    public void deleteUnitFiles (String unitName, boolean removeUnitFolder) {
	if( Stats.TRACE_UNIT_DELETION ) System.err.printf("Deleting unit files for %s\n", unitName);
        String path = getUnitStorageName(unitName);
        File pathFile = new File (path);
        deleteDirectory(pathFile, removeUnitFolder);
    }
    
    private void deleteDirectory(File path, boolean deleteDir) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i], true);
                } else {
                    files[i].delete();
                }
            }
        }
        if (deleteDir) {
            path.delete() ;
        }
    }
    public void cleanRepositoryCaches() {
        File repositoryPath = new File(diskRepositoryPath);
        deleteDirectory(repositoryPath, false);
    }
}

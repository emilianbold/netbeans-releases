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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
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
            diskRepositoryPath = System.getProperty("java.io.tmpdir") + File.separator + System.getProperty("user.name") + "-" + "modelcache"; //NOI18N
            File diskRepositoryFile = new File(diskRepositoryPath);
            // find name for directory which is not occupied by file
            long index = 0;
            while (diskRepositoryFile.exists() && !diskRepositoryFile.isDirectory()) {
                diskRepositoryFile = new File(diskRepositoryPath + ++index);
            }
            // create directory if needed
            if (!diskRepositoryFile.exists()) {
                diskRepositoryFile.mkdirs();
            }
            diskRepositoryPath = diskRepositoryFile.getAbsolutePath();
        }
    };
    
    public static StorageAllocator getInstance() {
        return instance;
    }
    
    private Map<String, String> unit2path = new HashMap<String, String>();
    
    public String getCachePath() {
        return diskRepositoryPath;
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
            if (prefix.length() > 128) {
                prefix = prefix.substring(0,64) + "--" + prefix.substring(prefix.length() - 64); // NOI18N
            }
            int cnt = 2;
            path = getCachePath() + File.separator + prefix; // NOI18N
            while( new File(path).exists()) {
                path = getCachePath() + prefix + "-" + cnt++;
            }
            path += File.separator;
            new File(path).mkdir();
            unit2path.put(unit, path);
        }
        return path;
    }
    
    public void closeUnit(String unitName) {
        unit2path.remove(unitName);
    }
}

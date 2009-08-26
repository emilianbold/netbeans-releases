/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.subversion.util.SvnUtils;

/**
 *
 * @author tomas
 */
public class CacheIndex {

    private static CacheIndex instance;
    private Map<String, Set<String>> index = new HashMap<String, Set<String>>();
    
    public static CacheIndex getInstance() {
        if (instance == null) {
            instance = new CacheIndex();
        }
        return instance;
    }

    public void addToIndex(File file) {
        assert file != null;
        if(file == null) {
            return;
        }

        File parent = file.getParentFile();
        String key = parent.getAbsolutePath();
        Set<String> s = index.get(key);
        if(s == null) {
            s = new HashSet<String>();
            index.put(key, s);
        }
        s.add(file.getAbsolutePath());
        ensureParents(parent, file);
    }

    public void adjustIndex(String path, Set<File> files) {
        if(files == null || files.size() == 0) {
            removeEntries(path);
        } else {
            addEntries( files, path);
        }
    }

    private void addEntries(Set<File> files, String path) {
        Set<String> s = new HashSet<String>(files.size());
        for (File file : files) {
            s.add(file.getAbsolutePath());
        }
        index.put(path, s);
        File file = new File(path);
        ensureParents(file.getParentFile(), file);
    }

    private void ensureParents(File parent, File file) {
        file = parent;
        while (true) {
            parent = parent.getParentFile();
            if (parent == null) {
                break;
            }
            String key = parent.getAbsolutePath();
            Set<String> s = index.get(key);
            if (s == null) {
                if (!SvnUtils.isManaged(parent)) {
                    break;
                }
                s = new HashSet<String>();
                index.put(key, s);
            }
            s.add(file.getAbsolutePath());
            file = parent;
        }
    }

    private void removeEntries(String path) {
        index.remove(path);
        File file = new File(path);
        File parent = file.getParentFile();

        while(parent != null) {
            String key = parent.getAbsolutePath();
            Set<String> s = index.get(key);
            if(s.size() == 1) {
                index.remove(key);
            } else {
                s.remove(key);
                break;
            }
            parent = parent.getParentFile();
        }
    }

}

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

package org.netbeans.modules.turbo;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Tomas Stupka
 */
public abstract class CacheIndex {

    private Map<File, Set<File>> index = new ConcurrentHashMap<File, Set<File>>();

    public CacheIndex() { }

    /**
     * Returns true if the given file is managed by the particular vcs
     * @param file 
     * @return true if the given file is managed by the particular vcs otherwise false
     */
    protected abstract boolean isManaged(File file);

    public File[] get(File key) {
        if(key == null) {
            return new File[0];
        }
        Set<File> ret = index.get(key);
        if(ret == null) {
            return new File[0];
        }
        synchronized(ret) {
            return ret.toArray(new File[ret.size()]);
        }
    }

    public File[] getAllValues() {
        Collection<Set<File>> values = index.values();
        Set<File> ret = new HashSet();
        for (Set<File> valuesSet : values) {
            synchronized(valuesSet) {
                for (File v : valuesSet) {
                    ret.add(v);
                }
            }
        }
        return ret.toArray(new File[ret.size()]);
    }

    public void addToIndex(File file) {
        assert file != null;
        if(file == null) {
            return;
        }
        File parent = file.getParentFile();        
        Set<File> set = index.get(parent);
        if(set == null) {
            set = Collections.synchronizedSet(new HashSet<File>());
            index.put(parent, set);
        }
        synchronized(set) {
            set.add(file);
        }
        ensureParents(parent);
    }

    public void adjustIndex(File file, Set<File> files) {
        if(files == null || files.size() == 0) {
            removeEntries(file);
        } else {
            addEntries(files, file);
        }
    }

    private void addEntries(Set<File> files, File file) {
        Set<File> set = new HashSet<File>(files.size());
        for (File f : files) {
            set.add(f);
        }
        index.put(file, set);
        ensureParents(file);
    }

    private void ensureParents(File file) {
        while (true) {
            File parent = file.getParentFile();
            if (parent == null) {
                break;
            }
            Set<File> set = index.get(parent);
            if (set == null) {
                if (!isManaged(parent)) {
                    break;
                }
                set = new HashSet<File>();
                index.put(parent, set);
            }
            synchronized(set) {
                set.add(file);
            }
            file = parent;
        }
    }

    private void removeEntries(File file) {
        index.remove(file);
        File parent = file.getParentFile();

        while(parent != null) {
            Set<File> set = index.get(parent);
            if(set == null) {
                break;
            }
            if(set.size() == 1) {
                index.remove(parent);
            } else {
                synchronized(set) {
                    set.remove(file);
                }
                break;
            }
            parent = parent.getParentFile();
        }
    }

}

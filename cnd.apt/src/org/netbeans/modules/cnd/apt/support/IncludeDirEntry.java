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

package org.netbeans.modules.cnd.apt.support;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class IncludeDirEntry {
    private static final int MANAGER_DEFAULT_CAPACITY;
    private static final int MANAGER_DEFAULT_SLICED_NUMBER;
    static {
        int nrProc = CndUtils.getConcurrencyLevel();
        if (nrProc <= 4) {
            MANAGER_DEFAULT_SLICED_NUMBER = 32;
            MANAGER_DEFAULT_CAPACITY = 512;
        } else {
            MANAGER_DEFAULT_SLICED_NUMBER = 128;
            MANAGER_DEFAULT_CAPACITY = 128;
        }
    }
    private static final IncludeDirStorage storage = new IncludeDirStorage(MANAGER_DEFAULT_SLICED_NUMBER, MANAGER_DEFAULT_CAPACITY);

    private final File file;
    private final boolean exists;
    private final boolean isFramework;
    private final CharSequence asCharSeq;

    public IncludeDirEntry(File file, boolean exists, boolean framework, CharSequence asCharSeq) {
        this.file = file;
        this.exists = exists;
        this.isFramework = framework;
        this.asCharSeq = asCharSeq;
    }

    public static IncludeDirEntry get(String dir) {
        CharSequence key = FilePathCache.getManager().getString(dir);
        Map<CharSequence, IncludeDirEntry> delegate = storage.getDelegate(key);
        synchronized (delegate) {
            IncludeDirEntry out = delegate.get(key);
            if (out == null) {
                File file = new File(dir);
                String asString = file.getAbsolutePath();
                boolean framework = asString.endsWith("/Frameworks"); // NOI18N
                CharSequence asCharSeq = FilePathCache.getManager().getString(asString);
                boolean exists = CndFileUtils.isExistingDirectory(file, asString);
                out = new IncludeDirEntry(file, exists, framework, asCharSeq);
                delegate.put(key, out);
            }
            return out;
        }
    }

    public CharSequence getAsSharedCharSequence() {
        return asCharSeq;
    }

    public boolean isFramework() {
        return isFramework;
    }

    public boolean isExistingDirectory() {
        return exists;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return (exists ? "" : "NOT EXISTING ") + asCharSeq; // NOI18N
    }

    public String getAsString() {
        return file.getPath();
    }

    /*package*/static void disposeCache() {
        storage.dispose();
    }

    private static final class IncludeDirStorage {

        private final WeakHashMap<CharSequence, IncludeDirEntry>[] instances;
        private final int segmentMask; // mask

        private IncludeDirStorage(int sliceNumber, int initialCapacity) {
            // Find power-of-two sizes best matching arguments
            int ssize = 1;
            while (ssize < sliceNumber) {
                ssize <<= 1;
            }
            segmentMask = ssize - 1;
            @SuppressWarnings("unchecked")
            WeakHashMap<CharSequence, IncludeDirEntry>[] ar = new WeakHashMap[ssize];
            for (int i = 0; i < ar.length; i++) {
                ar[i] = new WeakHashMap<CharSequence, IncludeDirEntry>(initialCapacity);
            }
            instances = ar;
        }

        private Map<CharSequence, IncludeDirEntry> getDelegate(CharSequence key) {
            int index = key.hashCode() & segmentMask;
            return instances[index];
        }

        @SuppressWarnings("unchecked")
        public final IncludeDirEntry getSharedUID(CharSequence key) {
            return getDelegate(key).get(key);
        }

        public final void dispose() {
            for (int i = 0; i < instances.length; i++) {
                if (instances[i].size() > 0) {
                    if (CndTraceFlags.TRACE_SLICE_DISTIBUTIONS) {
                        System.out.println("Include Dir Cache " + instances[i].size()); // NOI18N
                        Map<Class, Integer> keyClasses = new HashMap<Class, Integer>();
                        for (Object o : instances[i].keySet()) {
                            if (o != null) {
                                incCounter( keyClasses, o);
                            }
                        }
                        for (Map.Entry<Class, Integer> e : keyClasses.entrySet()) {
                            System.out.println("   " + e.getValue() + " of " + e.getKey().getName()); // NOI18N
                        }
                    }
                    instances[i].clear();
                }
            }
        }

        private void incCounter(Map<Class, Integer> uidClasses, Object o) {
            Integer num = uidClasses.get(o.getClass());
            if (num != null) {
                num = new Integer(num.intValue() + 1);
            } else {
                num = new Integer(1);
            }
            uidClasses.put(o.getClass(), num);
        }
    }
}

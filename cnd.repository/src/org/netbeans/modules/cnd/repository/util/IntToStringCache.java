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
package org.netbeans.modules.cnd.repository.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.translator.RepositoryTranslatorImpl;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;

/**
 * Maps strings to integers and vice versa.
 * Used to make persistence storage more compact
 */
public final class IntToStringCache {

    private final List<CharSequence> cache;
    private final StackTraceElement[] creationStack;
    private final int version;
    private final long timestamp;

    public IntToStringCache() {
        this(System.currentTimeMillis());
    }

    public IntToStringCache(long timestamp) {
        creationStack = Stats.TRACE_IZ_215449 ? Thread.currentThread().getStackTrace() : null;
        this.cache = new ArrayList<CharSequence>();
        this.version = RepositoryTranslatorImpl.getVersion();
        this.timestamp = timestamp;
    }

    public IntToStringCache(DataInput stream) throws IOException {
        creationStack = Stats.TRACE_IZ_215449 ? Thread.currentThread().getStackTrace() : null;
        assert stream != null;

        cache = new ArrayList<CharSequence>();
        version = stream.readInt();

        timestamp = stream.readLong();

        int size = stream.readInt();

        for (int i = 0; i < size; i++) {
            String value = stream.readUTF();
            CharSequence v;
            if (value.equals("")) {
                v = null;
            } else {
                v = FilePathCache.getManager().getString(value);
            }
            cache.add(v);
        }
    }

    /*
     * Persists the master index: unit name <-> integer index
     *
     */
    public void write(DataOutput stream) throws IOException {
        assert cache != null;
        assert stream != null;

        stream.writeInt(version);
        stream.writeLong(timestamp);

        int size = cache.size();
        stream.writeInt(size);

        for (int i = 0; i < size; i++) {
            CharSequence value = cache.get(i);
            if (value == null) {
                stream.writeUTF("");
            } else {
                stream.writeUTF(value.toString());
            }
        }
    }

    /*
     * This is a simple cache that keeps last found index by string.
     * Cache reduces method consuming time in 10 times (on huge projects).
     */
    private static final class Lock {}
    private final Object oneItemCacheLock = new Lock();
    private CharSequence oneItemCacheString; // Cached last string
    private int oneItemCacheInt; // Cached last index

    public int getId(CharSequence value) {
        CharSequence prevString = null;
        int prevInt = 0;
        synchronized (oneItemCacheLock) {
            prevString = oneItemCacheString;
            prevInt = oneItemCacheInt;
        }
        if (value.equals(prevString)) {
            return prevInt;
        }

        int id = cache.indexOf(value);
        if (id == -1) {
            synchronized (cache) {
                id = cache.indexOf(value);
                if (id == -1) {
                    id = makeId(value);
                }
            }
        }

        synchronized (oneItemCacheLock) {
            oneItemCacheString = value;
            oneItemCacheInt = id;
        }
        return id;
    }

    /**
     * synchronization is controlled by calling getId() method
     */
    private int makeId(CharSequence value) {
        cache.add(value);
        return cache.indexOf(value);
    }

    public CharSequence getValueById(int id) {
        return cache.get(id);
    }

    public boolean containsId(int id) {
        return 0 <= id && id < cache.size();
    }

    public int size() {
        return cache.size();
    }

    public int getVersion() {
        return version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public StackTraceElement[] getCreationStack() {
        return creationStack;
    }
}

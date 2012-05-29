/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.apt.support;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTInclude;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class APTFileCacheEntry {
    private final Map<Integer, PostIncludeData> cache;
    private final Map<Integer, Boolean> evalData;
    private final CharSequence filePath;
    private final boolean serial;
    private APTFileCacheEntry(CharSequence filePath, boolean concurrent, Map<Integer, PostIncludeData> storage, Map<Integer, Boolean> eval) {
        assert (filePath != null);
        this.filePath = filePath;
        this.serial = concurrent;
        this.cache = storage;
        this.evalData = eval;
    }

    /*package*/static APTFileCacheEntry toSerial(APTFileCacheEntry entry) {
        return new APTFileCacheEntry(entry.filePath, true, new HashMap<Integer, PostIncludeData>(entry.cache), new HashMap<Integer, Boolean>(entry.evalData));
    }

    /*package*/static APTFileCacheEntry createConcurrentEntry(CharSequence filePath) {
        return create(filePath, false);
    }

    /*package*/static APTFileCacheEntry createSerialEntry(CharSequence filePath) {
        return create(filePath, true);
    }

    private static APTFileCacheEntry create(CharSequence filePath, boolean serial) {
        return new APTFileCacheEntry(filePath, serial, serial ? new HashMap<Integer, PostIncludeData>() : new ConcurrentHashMap<Integer, PostIncludeData>(), serial ? new HashMap<Integer, Boolean>() : new ConcurrentHashMap<Integer, Boolean>());
    }

    public boolean isSerial() {
        return serial;
    }

    private static volatile int includeHits = 0;
    private static volatile int evalHits = 0;
    /** must be called under lock */
    /*package*/ PostIncludeData getPostIncludeState(APTInclude node) {
        PostIncludeData data = getIncludeData(node);
        assert data != null;
        if (data.getPostIncludeMacroState() != null) {
            includeHits++;
            if (APTTraceFlags.TRACE_APT_CACHE && needTraceValue(includeHits)) {
                System.err.println("INCLUDE HIT " + includeHits + " cache for line:" + node.getToken().getLine() + " in " + filePath);
            }
        }
        return data;
    }

    /*package*/ Object getIncludeLock(APTInclude node) {
        return getIncludeData(node);
    }

    /*package*/ Boolean getEvalResult(APT node) {
        Boolean out = evalData.get(node.getOffset());
        if (APTTraceFlags.TRACE_APT_CACHE) {
            if (out != null && needTraceValue(evalHits++)) {
                System.err.println("EVAL HIT " + evalHits + " cache for line:" + node.getToken().getLine() + " as " + out + " in " + filePath);
            }
        }
        return out;
    }

    /*package*/ void setEvalResult(APT node, boolean result) {
        evalData.put(node.getOffset(), Boolean.valueOf(result));
    }

    private PostIncludeData getIncludeData(APTInclude node) {
        Integer key = Integer.valueOf(node.getOffset());
        PostIncludeData data = cache.get(key);
        if (data == null) {
            // create empty object
            data = new PostIncludeData();
            PostIncludeData prev = serial ? cache.put(key, data) : ((ConcurrentMap<Integer, PostIncludeData>)cache).putIfAbsent(key, data);
            if (prev != null) {
                data = prev;
            }
        }
        return data;
    }

    /** must be called under lock or must be serial */
    /*package*/ void setIncludeData(APTInclude node, PostIncludeData newData) {
        Integer key = Integer.valueOf(node.getOffset());
        PostIncludeData old = cache.get(key);
        assert old != null;
        assert !old.hasPostIncludeMacroState() : filePath + " serial=" + serial + " for node " + node + " already has post macro state";
        if (serial) {
            cache.put(key, newData);
        } else {
            boolean replaced = ((ConcurrentMap<Integer, PostIncludeData>) cache).replace(key, old, newData);
            assert replaced : "old empty entry must be replaced by new one";
        }
    }
    
    public CharSequence getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "APT cache " + (isSerial() ? "Serial" : "Shared") + " with " + cache.size() + " entries for " + filePath; // NOI18N
    }

    private boolean needTraceValue(int val) {
//        return CharSequenceKey.ComparatorIgnoreCase.compare(filePath, "/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/include/math.h") == 0;
        return val % 10 == 0;
    }
}

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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTMacroMap.State;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class APTFileCacheEntry {
    private final ConcurrentMap<Integer, IncludeData> cache = new ConcurrentHashMap<Integer, IncludeData>();
    private final Map<Integer, Boolean> evalData = new HashMap<Integer, Boolean>();
    private final CharSequence filePath;
    private static boolean TRACE = false;
    public APTFileCacheEntry(CharSequence filePath) {
        assert (filePath != null);
        this.filePath = filePath;
    }

    private static volatile int includeHits = 0;
    private static volatile int evalHits = 0;
    /** must be called under lock */
    /*package*/ APTMacroMap.State getPostIncludeMacroState(APTInclude node) {
        IncludeData data = getIncludeData(node);
        assert data != null;
        if (data.postIncludeMacroState != null) {
            includeHits++;
            if (TRACE) {
                if (needTraceValue(includeHits)) {
                    System.err.println("INCLUDE HIT " + includeHits + " cache for line:" + node.getToken().getLine() + " in " + filePath);
                }
            }
        }
        return data.postIncludeMacroState;
    }

    /** must be called under lock */
    /*package*/ void setPostIncludeMacroState(APTInclude node, APTMacroMap.State state) {
        IncludeData data = getIncludeData(node);
        assert data.postIncludeMacroState == null;
        data.postIncludeMacroState = state;
    }

    /*package*/ Object getIncludeLock(APTInclude node) {
        return getIncludeData(node);
    }

    /*package*/ Boolean getEvalResult(APT node) {
        Boolean out = evalData.get(node.getOffset());
        if (TRACE) {
            if (out != null && needTraceValue(evalHits++)) {
                System.err.println("EVAL HIT " + evalHits + " cache for line:" + node.getToken().getLine() + " as " + out + " in " + filePath);
            }
        }
        return out;
    }

    /*package*/ void setEvalResult(APT node, boolean result) {
        evalData.put(node.getOffset(), Boolean.valueOf(result));
    }

    private IncludeData getIncludeData(APTInclude node) {
        Integer key = Integer.valueOf(node.getOffset());
        IncludeData data = cache.get(key);
        if (data == null) {
            data = new IncludeData(null);
            IncludeData prev = cache.putIfAbsent(key, data);
            if (prev != null) {
                data = prev;
            }
        }
        return data;
    }

    public CharSequence getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "APT cache for " + filePath; // NOI18N
    }

    private boolean needTraceValue(int val) {
//        return CharSequenceKey.ComparatorIgnoreCase.compare(filePath, "/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/include/math.h") == 0;
        return val % 10 == 0;
    }

    private static final class IncludeData {
        private volatile APTMacroMap.State postIncludeMacroState;

        public IncludeData(State state) {
            this.postIncludeMacroState = state;
        }
    }
}

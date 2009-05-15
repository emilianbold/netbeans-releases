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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTMacroMap.State;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class APTFileCacheEntry {
    private final ConcurrentMap<APTInclude, Data> cache = new ConcurrentHashMap<APTInclude, Data>();
    private final CharSequence filePath;
    private static boolean TRACE = false;
    public APTFileCacheEntry(CharSequence filePath) {
        assert (filePath != null);
        this.filePath = filePath;
    }
    
    /*package*/ APTMacroMap.State getPostIncludeMacroState(APTInclude node) {
        Data data = cache.get(node);
        if (data != null && data.state != null) {
            if (TRACE) {
                System.err.println("HIT cache for :" + node.getToken().getLine() + ":" + node.getText() + " in " + filePath);
            }
            return data.state;
        }
        return null;
    }

    /*package*/ void setPostIncludeMacroState(APTInclude node, APTMacroMap.State state) {
        if (TRACE) {
            System.err.println("PUT cache for cache for [line " + node.getToken().getLine() + "]:" + node.getText() + " in " + filePath);
        }
        cache.get(node).state = state;
    }

    /*package*/ synchronized Object getLock(APTInclude node) {
        Data data = new Data(null);
        cache.put(node, data);
        return data.lock;
    }

    public CharSequence getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "APT cache for " + filePath; // NOI18N
    }

    private static final class Data {
        APTMacroMap.State state;
        private final Object lock = new Object();

        public Data(State state) {
            this.state = state;
        }
    }
}

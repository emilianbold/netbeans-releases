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

package org.netbeans.modules.cnd.apt.debug;

import org.netbeans.modules.cnd.debug.DebugUtils;

/**
 * A common place for APT tracing flags that are used by several classes
 * @author Vladimir Voskresensky
 */
public interface APTTraceFlags {
    public static final boolean APT_SHARE_MACROS = DebugUtils.getBoolean("apt.share.macros", true); // NOI18N

    public static final boolean APT_SHARE_TEXT = DebugUtils.getBoolean("apt.share.text", true); // NOI18N
    //public static final boolean APT_USE_STORAGE_SET = DebugUtils.getBoolean("apt.share.storage", true); // NOI18N
    public static final boolean APT_NON_RECURSE_VISIT = DebugUtils.getBoolean("apt.nonrecurse.visit", true); // NOI18N

    public static final int     BUF_SIZE = 8192*Integer.getInteger("cnd.file.buffer", 1).intValue(); // NOI18N
    
    public static final boolean OPTIMIZE_INCLUDE_SEARCH = DebugUtils.getBoolean("cnd.optimize.include.search", true); // NOI18N

    public static final boolean TRACE_APT = Boolean.getBoolean("cnd.apt.trace"); // NOI18N
    public static final boolean TRACE_APT_LEXER = Boolean.getBoolean("cnd.aptlexer.trace"); // NOI18N
    public static final boolean TRACE_APT_CACHE = Boolean.getBoolean("cnd.apt.cache.hits"); // NOI18N

    public static final boolean USE_APT_TEST_TOKEN = Boolean.getBoolean("cnd.apt.apttoken"); // NOI18N

    public static final boolean TEST_APT_SERIALIZATION = DebugUtils.getBoolean("cnd.cache.apt", false); // NOI18N

    public static final boolean APT_DISPOSE_TOKENS = DebugUtils.getBoolean("apt.dispose.tokens", false); // NOI18N
    
    public static final boolean APT_USE_SOFT_REFERENCE = DebugUtils.getBoolean("apt.soft.reference", true); // NOI18N
    
    public static final boolean APT_ABSOLUTE_INCLUDES = DebugUtils.getBoolean("apt.absolute.include", true); // NOI18N
    
    public static final boolean APT_RECURSIVE_BUILD = DebugUtils.getBoolean("apt.recursive.build", true); // NOI18N
}

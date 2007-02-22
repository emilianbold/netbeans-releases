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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.apt.debug;

/**
 * A common place for APT tracing flags that are used by several classes
 * @author Vladimir Voskresensky
 */
public interface APTTraceFlags {

    public static final boolean APT_SHARE_TEXT = DebugUtils.getBoolean("apt.share.text", true); // NOI18N
    public static final boolean APT_NON_RECURSE_VISIT = DebugUtils.getBoolean("apt.nonrecurse.visit", false); // NOI18N

    public static final int     BUF_SIZE = 8192*Integer.getInteger("cnd.file.buffer", 1).intValue(); // NOI18N
    
    public static final boolean OPTIMIZE_INCLUDE_SEARCH = DebugUtils.getBoolean("cnd.optimize.include.search", true); // NOI18N

    public static final boolean TRACE_APT = Boolean.getBoolean("cnd.apt.trace"); // NOI18N
    public static final boolean TRACE_APT_LEXER = Boolean.getBoolean("cnd.aptlexer.trace"); // NOI18N

    public static final boolean USE_APT_TEST_TOKEN = Boolean.getBoolean("cnd.apt.apttoken"); // NOI18N

    public static final boolean TEST_APT_SERIALIZATION = DebugUtils.getBoolean("cnd.cache.apt", false); // NOI18N

    public static final boolean APT_DISPOSE_TOKENS = DebugUtils.getBoolean("apt.dispose.tokens", false); // NOI18N
    
    public static final boolean APT_USE_SOFT_REFERENCE = DebugUtils.getBoolean("apt.soft.reference", true); // NOI18N
    
    public static final boolean APT_ABSOLUTE_INCLUDES = DebugUtils.getBoolean("apt.absolute.include", false); // NOI18N
    
    public static final boolean APT_RECURSIVE_BUILD = DebugUtils.getBoolean("apt.recursive.build", true); // NOI18N
}

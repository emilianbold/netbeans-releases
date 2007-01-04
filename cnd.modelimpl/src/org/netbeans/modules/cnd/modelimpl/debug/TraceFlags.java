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

package org.netbeans.modules.cnd.modelimpl.debug;

import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;

/**
 * A common place for tracing flags that are used by several classes
 * @author Vladimir Kvashim
 */
public interface TraceFlags {
    
    public static final boolean TRACE_PARSER_QUEUE_DETAILS = Boolean.getBoolean("cnd.parser.queue.trace.details");
    public static final boolean TRACE_PARSER_QUEUE = TRACE_PARSER_QUEUE_DETAILS || Boolean.getBoolean("cnd.parser.queue.trace");
    
    public static final boolean TIMING_PARSE_PER_FILE_DEEP = Boolean.getBoolean("cnd.modelimpl.timing.per.file.deep");
    public static final boolean TIMING_PARSE_PER_FILE_FLAT = Boolean.getBoolean("cnd.modelimpl.timing.per.file.flat");
    public static final boolean TIMING = Boolean.getBoolean("cnd.modelimpl.timing");
    public static final boolean REPORT_PARSING_ERRORS = Boolean.getBoolean("parser.report.errors");
    public static final boolean DUMP_AST = Boolean.getBoolean("parser.collect.ast");
    public static final boolean DUMP_PROJECT_ON_OPEN = DebugUtils.getBoolean("cnd.dump.project.on.open", false);
    
    /** 
     * swithces off parsing function bodies
     */
    public static final boolean EXCLUDE_COMPOUND = DebugUtils.getBoolean("cnd.modelimpl.excl.compound", true);
    
    public static final boolean APT_CHECK_GET_STATE = DebugUtils.getBoolean("apt.check.get.state", true);
 
    public static final int     BUF_SIZE = APTTraceFlags.BUF_SIZE;
    
    /**
     * switches for cache
     */ 
    public static final boolean CACHE_AST = DebugUtils.getBoolean("cnd.cache.ast", false);
    public static final boolean TRACE_CACHE = DebugUtils.getBoolean("cnd.trace.cache", false);
    public static final boolean USE_AST_CACHE = DebugUtils.getBoolean("cnd.use.ast.cache", true);
    public static final boolean CACHE_SKIP_APT_VISIT = DebugUtils.getBoolean("cnd.cache.skip.apt.visit", false);

    public static final boolean CACHE_SKIP_SAVE = DebugUtils.getBoolean("cnd.cache.skip.save", true);
    
    public static final boolean TRACE_MODEL_STATE = Boolean.getBoolean("cnd.modelimpl.installer.trace");

    public static final boolean USE_CANONICAL_PATH = DebugUtils.getBoolean("cnd.modelimpl.use.canonical.path", true);
    
    public static final boolean CHECK_MEMORY = DebugUtils.getBoolean("cnd.check.memory", false);
    
    public static final boolean DUMP_PARSE_RESULTS = DebugUtils.getBoolean("cnd.dump.parse.results", false);
    public static final boolean DUMP_REPARSE_RESULTS = DebugUtils.getBoolean("cnd.dump.reparse.results", false);
    
    public static final boolean DEBUG = Boolean.getBoolean("org.netbeans.modules.cnd.modelimpl.trace")  || Boolean.getBoolean("cnd.modelimpl.trace");
    
}

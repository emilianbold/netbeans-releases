/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.modelimpl.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.debug.DebugUtils;

/**
 * A common place for tracing flags that are used by several classes
 * @author Vladimir Kvashim
 */
public class TraceFlags {
    public static final boolean TRACE_CPU_CPP = false;
    public static final boolean TRACE_PARSER_QUEUE_DETAILS = Boolean.getBoolean("cnd.parser.queue.trace.details"); // NOI18N
    public static final boolean TRACE_PARSER_PROGRESS = Boolean.getBoolean("cnd.parser.progress.trace"); // NOI18N
    public static final boolean TRACE_PARSER_QUEUE = TRACE_PARSER_QUEUE_DETAILS || Boolean.getBoolean("cnd.parser.queue.trace"); // NOI18N
    public static final boolean TRACE_PARSER_QUEUE_POLL = TRACE_PARSER_QUEUE || Boolean.getBoolean("cnd.parser.queue.trace.poll"); // NOI18N
    public static final boolean TRACE_CLOSE_PROJECT = DebugUtils.getBoolean("cnd.trace.close.project", false); // NOI18N
    public static final boolean TIMING_PARSE_PER_FILE_DEEP = Boolean.getBoolean("cnd.modelimpl.timing.per.file.deep"); // NOI18N
    public static final boolean TIMING_PARSE_PER_FILE_FLAT = Boolean.getBoolean("cnd.modelimpl.timing.per.file.flat"); // NOI18N
    public static final boolean TIMING = Boolean.getBoolean("cnd.modelimpl.timing"); // NOI18N
    public static final int     SUSPEND_PARSE_TIME = Integer.getInteger("cnd.modelimpl.sleep", 0); // NOI18N
    public static final boolean REPORT_PARSING_ERRORS = Boolean.getBoolean("parser.report.errors"); // NOI18N
    public static final boolean DUMP_AST = Boolean.getBoolean("parser.collect.ast"); // NOI18N
    public static final boolean DUMP_PROJECT_ON_OPEN = DebugUtils.getBoolean("cnd.dump.project.on.open", false); // NOI18N

    public static final String TRACE_FILE_NAME = System.getProperty("cnd.modelimpl.trace.file");

    /** 
     * swithces off parsing function bodies
     */
    public static final boolean EXCLUDE_COMPOUND = DebugUtils.getBoolean("cnd.modelimpl.excl.compound", true); // NOI18N
    
    public static final boolean APT_CHECK_GET_STATE = DebugUtils.getBoolean("apt.check.get.state", false); // NOI18N
 
    public static final int     BUF_SIZE = APTTraceFlags.BUF_SIZE;
    
    /**
     * switches for cache
     */ 
    public static final boolean CACHE_AST = DebugUtils.getBoolean("cnd.cache.ast", false); // NOI18N
    public static final boolean TRACE_CACHE = DebugUtils.getBoolean("cnd.trace.cache", false); // NOI18N
    public static final boolean USE_AST_CACHE = DebugUtils.getBoolean("cnd.use.ast.cache", false); // NOI18N
    public static final boolean CACHE_SKIP_APT_VISIT = DebugUtils.getBoolean("cnd.cache.skip.apt.visit", false); // NOI18N
    public static final boolean CACHE_FILE_STATE = DebugUtils.getBoolean("cnd.cache.file.state", true); // NOI18N
    public static final boolean USE_WEAK_MEMORY_CACHE = DebugUtils.getBoolean("cnd.cache.key.object", true); // NOI18N
    public static final boolean APT_FILE_CACHE_ENTRY = DebugUtils.getBoolean("cnd.apt.cache.entry", true); //NOI18N

    public static final boolean CACHE_SKIP_SAVE = DebugUtils.getBoolean("cnd.cache.skip.save", true); // NOI18N
    
    public static final boolean TRACE_MODEL_STATE = Boolean.getBoolean("cnd.modelimpl.installer.trace"); // NOI18N

    public static final boolean USE_CANONICAL_PATH = DebugUtils.getBoolean("cnd.modelimpl.use.canonical.path", false); // NOI18N
    public static final boolean SYMLINK_AS_OWN_FILE = DebugUtils.getBoolean("cnd.modelimpl.symlink.as.file", true); // NOI18N
    
    public static final boolean CHECK_MEMORY = DebugUtils.getBoolean("cnd.check.memory", false); // NOI18N
    
    public static final boolean DUMP_PARSE_RESULTS = DebugUtils.getBoolean("cnd.dump.parse.results", false); // NOI18N
    public static final boolean DUMP_REPARSE_RESULTS = DebugUtils.getBoolean("cnd.dump.reparse.results", false); // NOI18N
    
    public static final boolean DEBUG = Boolean.getBoolean("org.netbeans.modules.cnd.modelimpl.trace")  || Boolean.getBoolean("cnd.modelimpl.trace"); // NOI18N
    
    //public static final boolean USE_REPOSITORY = DebugUtils.getBoolean("cnd.modelimpl.use.repository", true); // NOI18N
    public static final boolean PERSISTENT_REPOSITORY = DebugUtils.getBoolean("cnd.modelimpl.persistent", true); // NOI18N
    //public static final boolean RESTORE_CONTAINER_FROM_UID = DebugUtils.getBoolean("cnd.modelimpl.use.uid.container", true); // NOI18N
    //public static final boolean UID_CONTAINER_MARKER = true;

    public static final boolean CLEAN_MACROS_AFTER_PARSE = DebugUtils.getBoolean("cnd.clean.macros.after.parse", true); // NOI18N
    
    public static final boolean SET_UNNAMED_QUALIFIED_NAME = DebugUtils.getBoolean("cnd.modelimpl.fqn.unnamed", false); // NOI18N
    public static final boolean TRACE_UNNAMED_DECLARATIONS = DebugUtils.getBoolean("cnd.modelimpl.trace.unnamed", false); // NOI18N

    public static final boolean TRACE_REGISTRATION = DebugUtils.getBoolean("cnd.modelimpl.trace.registration", false); // NOI18N
    public static final boolean TRACE_DISPOSE = DebugUtils.getBoolean("cnd.modelimpl.trace.dispose", false); // NOI18N

    public static final boolean CLOSE_AFTER_PARSE = DebugUtils.getBoolean("cnd.close.ide.after.parse", false); // NOI18N
    public static final int     CLOSE_TIMEOUT = Integer.getInteger("cnd.close.ide.timeout",0); // in seconds // NOI18N

    public static final boolean USE_DEEP_REPARSING_TRACE = DebugUtils.getBoolean("cnd.modelimpl.use.deep.repersing.trace", false); // NOI18N
    public static final boolean DEEP_REPARSING_OPTIMISTIC = DebugUtils.getBoolean("cnd.modelimpl.use.deep.repersing.optimistic", false); // NOI18N

    
    public static final boolean SAFE_REPOSITORY_ACCESS = DebugUtils.getBoolean("cnd.modelimpl.repository.safe.access", false); // NOI18N

    // see IZ#101952 and IZ#101953
    public static final boolean SAFE_UID_ACCESS = DebugUtils.getBoolean("cnd.modelimpl.safe.uid", true); // NOI18N
    
    public static final boolean TRACE_CANONICAL_FIND_FILE = DebugUtils.getBoolean("cnd.modelimpl.trace.canonical.find", false); // NOI18N

    public static final boolean NEED_TO_TRACE_UNRESOLVED_INCLUDE = DebugUtils.getBoolean("cnd.modelimpl.trace.failed.include", false); // NOI18N
    public static final boolean TRACE_VALIDATION = DebugUtils.getBoolean("cnd.modelimpl.trace.validation", false); // NOI18N

    public static final boolean TRACE_XREF_REPOSITORY = DebugUtils.getBoolean("cnd.modelimpl.trace.xref.repository", false); // NOI18N

    public static final boolean TRACE_REPOSITORY_LISTENER = DebugUtils.getBoolean("cnd.repository.listener.trace", false); // NOI18N
    public static final boolean TRACE_UP_TO_DATE_PROVIDER = DebugUtils.getBoolean("cnd.uptodate.trace", false); // NOI18N
    public static final boolean TRACE_PROJECT_COMPONENT_RW = DebugUtils.getBoolean("cnd.project.compoment.rw.trace", false); // NOI18N

    public static final boolean TRACE_RESOLVED_LIBRARY = DebugUtils.getBoolean("cnd.project.trace.resolved.library", false); // NOI18N
    
    public static final boolean TRACE_EXTERNAL_CHANGES = DebugUtils.getBoolean("cnd.modelimpl.trace.external.changes", false); // NOI18N
    
    public static final boolean TRACE_ERROR_PROVIDER = DebugUtils.getBoolean("cnd.modelimpl.trace.error.provider", false); // NOI18N
    public static final boolean PARSE_STATISTICS = DebugUtils.getBoolean("cnd.parse.statistics", false); // NOI18N
    public static final boolean TRACE_PC_STATE = DebugUtils.getBoolean("cnd.pp.condition.state.trace", false); // NOI18N
    public static final boolean TRACE_PC_STATE_COMPARISION = DebugUtils.getBoolean("cnd.pp.condition.comparision.trace", false); // NOI18N

    public static final int REPARSE_DELAY = DebugUtils.getInt("cnd.reparse.delay", 1001); // NOI18N

    // experimental expression evaluator for template instantiations
    public static boolean EXPRESSION_EVALUATOR = DebugUtils.getBoolean("cnd.modelimpl.expression.evaluator", true); // NOI18N

    public static final List<String> logMacros;
    static {
         String text = System.getProperty("parser.log.macro"); //NOI18N
         if (text != null && text.length() > 0) {
             List<String> l = new ArrayList<String>();
             for (StringTokenizer stringTokenizer = new StringTokenizer(text, ","); stringTokenizer.hasMoreTokens();) { //NOI18N
                 l.add(stringTokenizer.nextToken());
             }
             logMacros = Collections.unmodifiableList(l);
         } else {
             logMacros = null;
         }
    }

    private TraceFlags() {
    }
}

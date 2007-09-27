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

package org.netbeans.modules.cnd.debugger.gdb.proxy;

/**
 *
 * @author gordonp
 */
public interface GdbMiDefinitions {

    // GDB/MI commands
    public final String MI_CMD_FILE_EXEC_FILE        = "-file-exec-file "; // NOI18N
    public final String MI_CMD_FILE_EXEC_AND_SYMBOLS = "-file-exec-and-symbols "; // NOI18N
    public final String MI_CMD_FILE_LIST_EXEC_SOURCE_FILE = "-file-list-exec-source-file"; // NOI18N
    public final String MI_CMD_EXEC_RUN              = "-exec-run "; // NOI18N
    public final String MI_CMD_EXEC_STEP             = "-exec-step "; // NOI18N
    public final String MI_CMD_EXEC_NEXT             = "-exec-next "; // NOI18N
    public final String MI_CMD_EXEC_CONTINUE         = "-exec-continue "; // NOI18N
    public final String MI_CMD_EXEC_ABORT            = "-exec-abort "; // NOI18N
    public final String MI_CMD_EXEC_FINISH           = "-exec-finish "; // NOI18N
    public final String MI_CMD_BREAK_DELETE          = "-break-delete "; // NOI18N
    public final String MI_CMD_BREAK_INSERT          = "-break-insert "; // NOI18N
    public final String MI_CMD_BREAK_ENABLE          = "-break-enable "; // NOI18N
    public final String MI_CMD_BREAK_DISABLE         = "-break-disable "; // NOI18N
    public final String MI_CMD_GDB_EXIT              = "-gdb-exit "; // NOI18N
    public final String MI_CMD_STACK_INFO_FRAME      = "-stack-info-frame "; // NOI18N
    public final String MI_CMD_STACK_LIST_FRAMES     = "-stack-list-frames"; // NOI18N
    public final String MI_CMD_STACK_LIST_LOCALS     = "-stack-list-locals "; // NOI18N
    public final String MI_CMD_STACK_SELECT_FRAME    = "-stack-select-frame "; // NOI18N
    public final String MI_CMD_VAR_CREATE            = "-var-create "; // NOI18N
    public final String MI_CMD_VAR_DELETE            = "-var-delete "; // NOI18N
    public final String MI_CMD_VAR_EVALUATE_EXPR     = "-var-evaluate-expression "; // NOI18N
    public final String MI_CMD_VAR_ASSIGN            = "-var-assign "; // NOI18N
    public final String MI_CMD_DATA_EVALUATE_EXPRESSION    = "-data-evaluate-expression "; // NOI18N
    public final String MI_CMD_GDB_SET_ENVIRONMENT   = "-gdb-set environment "; // NOI18N
    public final String MI_CMD_INFO_THREADS          = "info threads"; // NOI18N
    public final String MI_CMD_INFO_PROC             = "info proc"; // NOI18N
    
    // GDB/MI Options
    public final String ALL_VALUES                  = "--all-values"; // NOI18N
    public final String SIMPLE_VALUES              = " --simple-values"; // NOI18N
    
    // GDB/MI replies
    public final String MI_REPLY_DONE                = "^done"; // NOI18N
    public final String MI_REPLY_ERROR               = "^error"; // NOI18N
    public final String MI_REPLY_RUNNING             = "^running"; // NOI18N
    public final String MI_REPLY_STOPPED             = "*stopped,"; // NOI18N
    public final String MI_REPLY_EXITED              = "*stopped,reason=\"exited"; // NOI18N
    public final String MI_REPLY_DONE_BKPT           = "^done,bkpt={"; // NOI18N
    public final String MI_REPLY_DONE_LOCALS         = "^done,locals=["; // NOI18N
    public final String MI_REPLY_DONE_WHERE          = "^done,stack=["; // NOI18N
    public final String MI_REPLY_INFO_THREADS        = "&\"info threads\\n\"\n"; // NOI18N
    public final String MI_REPLY_INFO_PROC           = "&\"info proc\\n\"\n"; // NOI18N
    
    // GDB/CLI commands
    public final String CLI_CMD_FILE      = "file "; // NOI18N
    public final String CLI_CMD_RUN       = "run "; // NOI18N
    public final String CLI_CMD_KILL      = "kill "; // NOI18N
    public final String CLI_CMD_SET_NEW_CONSOLE = "set new-console"; // NOI18N
    
}

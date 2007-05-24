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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

/**
 * Copyright (c) 2010, Oracle. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sun.glassfishesb.wlm.console;

/**
 *
 * @author Kirill Sorokin, Kirill.Sorokin@Sun.COM
 */
public interface Constants {
    // Debug ---------------------------------------------------------------------------------------
    String DEBUG_GENERATION_START_TIME_MARKER_ATTRIBUTE =
            "com.sun.glassfishesb.wlm.console.debug.generation-start-time-marker"; // NOI18N
    String DEBUG_RENDERING_START_TIME_MARKER_ATTRIBUTE =
            "com.sun.glassfishesb.wlm.console.debug.rendering-start-time-marker"; // NOI18N

    // Pages ---------------------------------------------------------------------------------------
    String ROOT_PAGE_URL =
            ""; // NOI18N
    String HELP_PAGE_URL =
            "/help.jsp"; // NOI18N
    String INDEX_PAGE_URL =
            "/index.jsp"; // NOI18N
    String LOGIN_PAGE_URL =
            "/login.jsp"; // NOI18N
    String LOGIN_FAILED_PAGE_URL =
            "/login-failed.jsp"; // NOI18N
    String TASK_PAGE_URL =
            "/task.jsp"; // NOI18N

    // Handlers ------------------------------------------------------------------------------------
    String TASK_TYPE_PO_SAMPLE =
            "{http://jbi.com.sun/wfse/samples/purchase-order/wf}" + // NOI18N
            "ApprovePurchaseTask"; // NOI18N

    // Login and logout ----------------------------------------------------------------------------
    String LOGOUT_PARAMETER = 
            "logout"; // NOI18N
    String LOGIN_FAILED_MARKER_ATTRIBUTE =
            "com.sun.glassfishesb.wlm.console.login-failed-marker"; // NOI18N
    String USER_ID_ATTRIBUTE =
            "com.sun.glassfishesb.wlm.console.user-id"; // NOI18N

    // Task ----------------------------------------------------------------------------------------
    long UNKNOWN_TASK_ID =
            -1L;

    String TASK_ID_PARAMETER =
            "id"; // NOI18N

    String TASK_ACTION_PARAMETER =
            "do"; // NOI18N
    String REASSIGN_TO_USER_PARAMETER =
            "reassignToUser"; // NOI18N
    String REASSIGN_TO_GROUP_PARAMETER =
            "reassignToGroup"; // NOI18N

    String TASK_ID_ATTRIBUTE =
            "com.sun.glassfishesb.wlm.console.task-id"; // NOI18N
    String TASK_ATTRIBUTE =
            "com.sun.glassfishesb.wlm.console.task"; // NOI18N
    String TASK_INPUT_DATA_ATTRIBUTE =
            "com.sun.glassfishesb.wlm.console.task-input-data"; // NOI18N
    String TASK_OUTPUT_DATA_ATTRIBUTE =
            "com.sun.glassfishesb.wlm.console.task-output-data"; // NOI18N
    String TASK_OUTPUT_DATA_EXCEPTION_ATTRIBUTE =
            "com.sun.glassfishesb.wlm.console.task-output-data-exception"; // NOI18N

    String TASK_OUTPUT_DATA_READ_ONLY_ATTRIBUTE =
            "com.sun.glassfishesb.wlm.console.task-output-data-read-only"; // NOI18N
    String TASK_OUTPUT_HANDLER_MODE_ATTRIBUTE =
            "com.sun.glassfishesb.wlm.console.task-output-handler-mode"; // NOI18N

    String CLAIM_ACTION =
            "claim"; // NOI18N
    String REASSIGN_ACTION =
            "reassign"; // NOI18N
    String SAVE_ACTION =
            "save"; // NOI18N
    String SAVE_AND_COMPLETE_ACTION =
            "save-and-complete"; // NOI18N
    String COMPLETE_ACTION =
            "complete"; // NOI18N
    String REVOKE_ACTION =
            "revoke"; // NOI18N
    String ESCALATE_ACTION =
            "escalate"; // NOI18N

    String OUTPUT_MODE =
            "output-mode"; // NOI18N
    String PARSE_MODE =
            "parse-mode"; // NOI18N

    // HTML entities -------------------------------------------------------------------------------
    String AMPERSAND =
            "&"; // NOI18N
    String LEFT_TAG =
            "<"; // NOI18N
    String RIGHT_TAG =
            ">"; // NOI18N
    String DOUBLE_QUOTE =
            "\""; // NOI18N
    String SINGLE_QUOTE =
            "'"; // NOI18N
    String NEW_LINE =
            "\n"; // NOI18N

    String AMPERSAND_ENTITY =
            "&amp;"; // NOI18N
    String LEFT_TAG_ENTITY =
            "&lt;"; // NOI18N
    String RIGHT_TAG_ENTITY =
            "&gt;"; // NOI18N
    String DOUBLE_QUOTE_ENTITY =
            "&quot;"; // NOI18N
    String SINGLE_QUOTE_ENTITY =
            "&#39;"; // NOI18N
    String MDASH_ENTITY =
            "&mdash;"; // NOI18N
    String NEW_LINE_ENTITY =
            "<br/>"; // NOI18N

    // Miscellanea ---------------------------------------------------------------------------------
    String LOCALE_ATTRIBUTE =
            "com.sun.glassfishesb.wlm.console.locale"; // NOI18N
    String CLASSPATH_SEPARATOR =
            "."; // NOI18N
    String UTF8 =
            "UTF-8"; // NOI18N

    // Localizing bundle ---------------------------------------------------------------------------
    String BUNDLE_LOCAL_NAME =
            "Messages"; // NOI18N

    String KEY_SYSTEM_MESSAGENOTFOUND =
            "system.message-not-found"; // NOI18N
    String KEY_SYSTEM_UNKNOWNPAGE =
            "system.unknown-page"; // NOI18N
    String KEY_SYSTEM_FAILEDTOFORMATELEMENT =
            "system.failed-to-format-element"; // NOI18N

    String KEY_GLOBAL_TITLE =
            "global.title"; // NOI18N
    String KEY_GLOBAL_NOTLOGGEDIN =
            "global.not-logged-in"; // NOI18N
    String KEY_GLOBAL_LOGGEDINAS =
            "global.logged-in-as"; // NOI18N
    String KEY_GLOBAL_ALLTASKS =
            "global.all-tasks"; // NOI18N
    String KEY_GLOBAL_MYTASKS =
            "global.my-tasks"; // NOI18N
    String KEY_GLOBAL_LASTQUERY =
            "global.last-query"; // NOI18N
    String KEY_GLOBAL_HELP =
            "global.help"; // NOI18N
    String KEY_GLOBAL_LOGOUT =
            "global.logout"; // NOI18N
    String KEY_GLOBAL_LOGIN =
            "global.login"; // NOI18N
    String KEY_GLOBAL_OR =
            "global.or"; // NOI18N

    String KEY_PREFIX_TASK_STATUS =
            "task.status."; // NOI18N
    String KEY_PREFIX_ORDER_FIELD =
            "order.field."; // NOI18N

    String KEY_PAGES_NO_TASK_WERE_FOUND 
            = "pages.index.no-tasks-were-found"; // NOI18N

    String KEY_PAGES_HELP_PAGETITLE =
            "pages.help.page-title"; // NOI18N
    String KEY_PAGES_INDEX_PAGETITLE =
            "pages.index.page-title"; // NOI18N
    String KEY_PAGES_LOGIN_PAGETITLE =
            "pages.login.page-title"; // NOI18N
    String KEY_PAGES_LOGIN_FAILED_PAGETITLE =
            "pages.login-failed.page-title"; // NOI18N
    String KEY_PAGES_TASK_PAGETITLE =
            "pages.task.page-title"; // NOI18N

    String KEY_PAGES_LOGIN_USERNAME =
            "pages.login.username"; // NOI18N
    String KEY_PAGES_LOGIN_PASSWORD =
            "pages.login.password"; // NOI18N
    String KEY_PAGES_LOGIN_SUBMIT =
            "pages.login.submit"; // NOI18N
    String KEY_PAGES_LOGIN_LOGINFAILED =
            "pages.login.login-failed"; // NOI18N

    String KEY_PAGES_INDEX_SORTBY =
            "pages.index.sort-by"; // NOI18N
    String KEY_PAGES_INDEX_ID =
            "pages.index.id"; // NOI18N
    String KEY_PAGES_INDEX_TITLE =
            "pages.index.title"; // NOI18N
    String KEY_PAGES_INDEX_STATUS =
            "pages.index.status"; // NOI18N
    String KEY_PAGES_INDEX_SUBMITTEDON =
            "pages.index.submitted-on"; // NOI18N
    String KEY_PAGES_INDEX_ASSIGNEDTO =
            "pages.index.assigned-to"; // NOI18N
    String KEY_PAGES_INDEX_CLAIMEDBY =
            "pages.index.claimed-by"; // NOI18N
    String KEY_PAGES_INDEX_DEADLINE =
            "pages.index.deadline"; // NOI18N
    String KEY_PAGES_INDEX_CLAIM =
            "pages.index.claim"; // NOI18N
    String KEY_PAGES_INDEX_COMPLETE =
            "pages.index.complete"; // NOI18N
    String KEY_PAGES_INDEX_TASKSPERPAGE =
            "pages.index.tasks-per-page"; // NOI18N
    String KEY_PAGES_INDEX_HIGHPRIORITY =
            "pages.index.high-priority"; // NOI18N
    String KEY_PAGES_INDEX_NORMALPRIORITY =
            "pages.index.normal-priority"; // NOI18N
    String KEY_PAGES_INDEX_LOWPRIORITY =
            "pages.index.low-priority"; // NOI18N

    String KEY_PAGES_PAGES
            = "pages.includes.pages"; // NOI18N
    String KEY_PAGES_PAGES_FIRST =
            "pages.includes.pages.first"; // NOI18N
    String KEY_PAGES_PAGES_PREVIOUS =
            "pages.includes.pages.previous"; // NOI18N
    String KEY_PAGES_PAGES_NEXT =
            "pages.includes.pages.next"; // NOI18N
    String KEY_PAGES_PAGES_LAST =
            "pages.includes.pages.last"; // NOI18N

    String KEY_PAGES_SEARCH_SWITCHTO =
            "pages.includes.search.switch-to"; // NOI18N
    String KEY_PAGES_SEARCH_BASICSEARCH =
            "pages.includes.search.basic-search"; // NOI18N
    String KEY_PAGES_SEARCH_ADVANCEDSEARCH =
            "pages.includes.search.advanced-search"; // NOI18N
    String KEY_PAGES_SEARCH_STATUSES =
            "pages.includes.search.statuses"; // NOI18N
    String KEY_PAGES_SEARCH_OWNERS =
            "pages.includes.search.owners"; // NOI18N
    String KEY_PAGES_SEARCH_USERS =
            "pages.includes.search.users"; // NOI18N
    String KEY_PAGES_SEARCH_GROUPS =
            "pages.includes.search.groups"; // NOI18N
    String KEY_PAGES_SEARCH_OWNERSHINT =
            "pages.includes.search.owners-hint"; // NOI18N
    String KEY_PAGES_SEARCH_TEXTSEARCH =
            "pages.includes.search.text-search"; // NOI18N
    String KEY_PAGES_SEARCH_SEARCH =
            "pages.includes.search.search"; // NOI18N
    String KEY_PAGES_SEARCH_SEARCHLABEL =
            "pages.includes.search.search-label"; // NOI18N

    String KEY_PAGES_TASK_STATUS = 
            "pages.task.status"; // NOI18N
    String KEY_PAGES_TASK_SUBMITTEDON = 
            "pages.task.submitted-on"; // NOI18N
    String KEY_PAGES_TASK_ASSIGNEDTO = 
            "pages.task.assigned-to"; // NOI18N
    String KEY_PAGES_TASK_PRIORITY = 
            "pages.task.priority"; // NOI18N
    String KEY_PAGES_TASK_DEADLINE = 
            "pages.task.deadline"; // NOI18N
    String KEY_PAGES_TASK_CLAIMEDBY = 
            "pages.task.claimed-by"; // NOI18N
    String KEY_PAGES_TASK_CLAIM = 
            "pages.task.claim"; // NOI18N
    String KEY_PAGES_TASK_REASSIGN = 
            "pages.task.reassign"; // NOI18N
    String KEY_PAGES_TASK_TOAUSER = 
            "pages.task.to-a-user"; // NOI18N
    String KEY_PAGES_TASK_TOAGROUP = 
            "pages.task.to-a-group"; // NOI18N
    String KEY_PAGES_TASK_PARSEERROR = 
            "pages.task.parse-error"; // NOI18N
    String KEY_PAGES_TASK_SAVE = 
            "pages.task.save"; // NOI18N
    String KEY_PAGES_TASK_SAVEANDCOMPLETE = 
            "pages.task.save-and-complete"; // NOI18N
    String KEY_PAGES_TASK_REVOKE =
            "pages.task.revoke"; // NOI18N
    String KEY_PAGES_TASK_NOSUCHTASKERROR = 
            "pages.task.no-such-task-error"; // NOI18N
    String KEY_PAGES_TASK_ACTIONERROR =
            "pages.task.action-error"; // NOI18N
    String KEY_PAGES_TASK_CLAIMFAILED =
            "pages.task.claim-failed"; // NOI18N
    String KEY_PAGES_TASK_REASSIGNFAILED =
            "pages.task.reassign-failed"; // NOI18N
    String KEY_PAGES_TASK_SAVEFAILED =
            "pages.task.save-failed"; // NOI18N
    String KEY_PAGES_TASK_COMPLETEFAILED =
            "pages.task.complete-failed"; // NOI18N
    String KEY_PAGES_TASK_REVOKEFAILED =
            "pages.task.revoke-failed"; // NOI18N

    String KEY_HANDLERS_POS_WRONGAMOUNT =
            "handlers.pos.wrong-amount"; // NOI18N
    String KEY_HANDLERS_POS_PRODUCT =
            "handlers.pos.product"; // NOI18N
    String KEY_HANDLERS_POS_PURCHASER =
            "handlers.pos.puchaser"; // NOI18N
    String KEY_HANDLERS_POS_RESULT =
            "handlers.pos.result"; // NOI18N
    String KEY_HANDLERS_POS_DESCRIPTION =
            "handlers.pos.description"; // NOI18N
    String KEY_HANDLERS_POS_APPROVED =
            "handlers.pos.approved"; // NOI18N
    String KEY_HANDLERS_POS_REJECTED =
            "handlers.pos.rejected"; // NOI18N
}

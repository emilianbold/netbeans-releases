<%--
 - Copyright (c) 2009, Sun Microsystems, Inc. All rights reserved.
 -
 - Redistribution and use in source and binary forms, with or without
 - modification, are permitted provided that the following conditions are met:
 -
 - * Redistributions of source code must retain the above copyright notice,
 -   this list of conditions and the following disclaimer.
 -
 - * Redistributions in binary form must reproduce the above copyright notice,
 -   this list of conditions and the following disclaimer in the documentation
 -   and/or other materials provided with the distribution.
 -
 - * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 -   may be used to endorse or promote products derived from this software without
 -   specific prior written permission.
 -
 - THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 - AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 - IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 - ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 - LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 - CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 - SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 - INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 - CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 - ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 - THE POSSIBILITY OF SUCH DAMAGE.
--%>

<%--
    @author Kirill Sorokin, Kirill.Sorokin@Sun.COM
--%>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="com.sun.glassfishesb.wlm.console.*" %>
<%@page import="java.io.*" %>
<%@page import="java.util.logging.*" %>
<%@page import="sun.com.jbi.wfse.wsdl.taskcommon.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Constants.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Utils.*" %>
<%@include file="WEB-INF/includes/common.jsp" %>

<%
    final Logger logger = Logger.getLogger("com.sun.glassfishesb.wlm.console");

    // Ths variable will be used below as an indicator that we tried to parse the output data and
    // failed. The output processor should not fetch the current output data from the request
    // attribute, but should use the submitted data instead.
    Exception tsk_outputDataParseException = null;

    // This one has the same usage, except for the cause of the error. It is filled in, if
    // an exception occurs while processing the task action.
    Exception tsk_actionException = null;

    final Locale tsk_locale = (Locale) request.getAttribute(LOCALE_ATTRIBUTE);
    final String tsk_userId = (String) request.getAttribute(USER_ID_ATTRIBUTE);
    
    final long tsk_taskId = getTaskId(request);
    request.setAttribute(TASK_ID_ATTRIBUTE, tsk_taskId);

    final String tsk_action = (String) request.getParameter(TASK_ACTION_PARAMETER);

    TaskType tsk_task = WsUtils.getTask(tsk_taskId, tsk_userId);
    request.setAttribute(TASK_ATTRIBUTE, tsk_task);

    if (tsk_task != null) {
        // We assume that task input and outptu will be an org.w3c.dom.Element. It might not, as
        // the API suggests, but that is somewhat veeery unlikely.. :)
        Element tsk_inputData = (Element) WsUtils.getTaskInputData(tsk_taskId, tsk_userId);
        request.setAttribute(TASK_INPUT_DATA_ATTRIBUTE, tsk_inputData);

        Element tsk_outputData = (Element) WsUtils.getTaskOutputData(tsk_taskId, tsk_userId);
        request.setAttribute(TASK_OUTPUT_DATA_ATTRIBUTE, tsk_outputData);

        try {
            ResultCodeType result = ResultCodeType.SUCCESS;

            if (CLAIM_ACTION.equals(tsk_action)) {
                result = WsUtils.claimTask(tsk_taskId, tsk_userId);
                if (result != ResultCodeType.SUCCESS) {
                    throw new Exception(getMessage(
                            KEY_PAGES_TASK_CLAIMFAILED, tsk_locale));
                }
            } else if (REASSIGN_ACTION.equals(tsk_action)) {
                final String tsk_toUser = request.getParameter(REASSIGN_TO_USER_PARAMETER);
                final String tsk_toGroup = request.getParameter(REASSIGN_TO_GROUP_PARAMETER);

                result = WsUtils.reassignTask(tsk_taskId, tsk_userId,
                        tsk_toUser == null ? "" : tsk_toUser,
                        tsk_toGroup == null ? "" : tsk_toGroup,
                        "",
                        "");
                if (result != ResultCodeType.SUCCESS) {
                    throw new Exception(getMessage(
                            KEY_PAGES_TASK_REASSIGNFAILED, tsk_locale));
                }

                response.sendRedirect(request.getContextPath() + INDEX_PAGE_URL);
                return;
            } else if (SAVE_ACTION.equals(tsk_action) ||
                    SAVE_AND_COMPLETE_ACTION.equals(tsk_action)) {

                // Use the output handler to parse the data and put it to
                // the TASK_OUTPUT_DATA_ATTRIBUTE.
                request.setAttribute(TASK_OUTPUT_HANDLER_MODE_ATTRIBUTE, PARSE_MODE);

                try {
                    %><%@include file="WEB-INF/includes/output-handler.jsp" %><%

                    request.removeAttribute(TASK_INPUT_DATA_ATTRIBUTE);
                } catch (Exception e) {
                    tsk_outputDataParseException = e;
                }

                if (tsk_outputDataParseException == null) {
                    tsk_outputData = (Element) request.getAttribute(TASK_OUTPUT_DATA_ATTRIBUTE);

                    result = WsUtils.setTaskOutputData(tsk_taskId, tsk_userId, tsk_outputData);
                    if (result != ResultCodeType.SUCCESS) {
                        Exception e = new Exception(getMessage(
                                KEY_PAGES_TASK_SAVEFAILED, tsk_locale));

                        request.setAttribute(
                                TASK_OUTPUT_DATA_EXCEPTION_ATTRIBUTE, e);
                        throw e;
                    }

                    if (SAVE_AND_COMPLETE_ACTION.equals(tsk_action)) {
                        result = WsUtils.completeTask(tsk_taskId, tsk_userId);
                        if (result != ResultCodeType.SUCCESS) {
                            throw new Exception(getMessage(
                                    KEY_PAGES_TASK_COMPLETEFAILED, tsk_locale));
                        }
                    }
                } else {
                    request.setAttribute(
                            TASK_OUTPUT_DATA_EXCEPTION_ATTRIBUTE, tsk_outputDataParseException);
                }
            } else if (COMPLETE_ACTION.equals(tsk_action)) {
                result = WsUtils.completeTask(tsk_taskId, tsk_userId);
                if (result != ResultCodeType.SUCCESS) {
                    throw new Exception(getMessage(
                            KEY_PAGES_TASK_COMPLETEFAILED, tsk_locale));
                }
            } else if (REVOKE_ACTION.equals(tsk_action)) {
                result = WsUtils.revokeTask(tsk_taskId, tsk_userId);
                if (result != ResultCodeType.SUCCESS) {
                    throw new Exception(getMessage(
                            KEY_PAGES_TASK_REVOKEFAILED, tsk_locale));
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            tsk_actionException = e;
        }

        if ((tsk_action != null) &&
                (tsk_outputDataParseException == null) && (tsk_actionException == null)) {

            response.sendRedirect(request.getContextPath() + TASK_PAGE_URL +
                    "?" + TASK_ID_PARAMETER + "=" + tsk_taskId);
            return;
        }
    }
%>

<%@include file="WEB-INF/includes/header.jsp" %>

<% if (tsk_actionException != null) { %>
    <div id="errorMessage">
        <%= getMessage(KEY_PAGES_TASK_ACTIONERROR, tsk_locale, tsk_actionException.getMessage()) %>
    </div>
<% } %>

<% if (tsk_task != null) { %>
    <div id="taskHeader">
        <div id="taskHeaderTitle"><%= escapeHtml(tsk_task.getTitle()) %>
                <span id="taskHeaderId">(#<%= tsk_task.getTaskId() %>)</span></div>

        <table id="taskHeaderDetails" cellspacing="0">
            <tr>
                <th class="TaskHeaderStatus">
                        <%= getMessage(KEY_PAGES_TASK_STATUS, tsk_locale) %></th>
                <td class="TaskHeaderStatus"><%= formatStatus(tsk_task, tsk_locale) %></td>
                <th class="TaskHeaderSubmittedOn">
                        <%= getMessage(KEY_PAGES_TASK_SUBMITTEDON, tsk_locale) %></th>
                <td class="TaskHeaderSubmittedOn"><%= formatSubmittedOn(tsk_task) %></td>
                <th class="TaskHeaderAssignedTo">
                        <%= getMessage(KEY_PAGES_TASK_ASSIGNEDTO, tsk_locale) %></th>
                <td class="TaskHeaderAssignedTo"><%= formatAssignedTo(tsk_task) %></td>
            </tr>
            <tr>
                <th class="TaskHeaderPriority">
                        <%= getMessage(KEY_PAGES_TASK_PRIORITY, tsk_locale) %></th>
                <td class="TaskHeaderPriority"><%= formatPriority(tsk_task) %></td>
                <th class="TaskHeaderDeadline">
                        <%= getMessage(KEY_PAGES_TASK_DEADLINE, tsk_locale) %></th>
                <td class="TaskHeaderDeadline"><%= formatDeadline(tsk_task) %></td>
                <th class="TaskHeaderClaimedBy">
                        <%= getMessage(KEY_PAGES_TASK_CLAIMEDBY, tsk_locale) %></th>
                <td class="TaskHeaderClaimedBy"><%= formatClaimedBy(tsk_task) %></td>
            </tr>
        </table>
    </div>

    <div id="taskInput">
        <div id="taskInputData">
            <% { %>
            <%@include file="WEB-INF/includes/input-handler.jsp" %>
            <% } %>
        </div>

        <% if ((tsk_task.getStatus() == TaskStatus.ASSIGNED) ||
                        (tsk_task.getStatus() == TaskStatus.ESCALATED)) { %>
            <div id="taskInputMenu">
                <form id="taskInputForm" action="task.jsp?id=<%= tsk_taskId %>" method="post">
                    <input id="inputTaskAction" type="hidden" name="do" value=""/>

                    <span id="taskInputMenuClaim">
                        <input class="Button"
                                type="submit"
                                value="<%= getMessage(KEY_PAGES_TASK_CLAIM, tsk_locale) %>"
                                onclick="document.getElementById('inputTaskAction').value='claim';"/>

                        <small>
                            <%= getMessage(KEY_GLOBAL_OR, tsk_locale) %>
                            <a href="javascript: switchClaimAndReassign();">
                                <%= getMessage(KEY_PAGES_TASK_REASSIGN, tsk_locale) %>
                            </a>
                        </small>
                    </span>

                    <span id="taskInputMenuReassign" style="display: none">
                        <input class="Button"
                                type="submit"
                                value="<%= getMessage(KEY_PAGES_TASK_REASSIGN, tsk_locale) %>"
                                onclick="clearDefaultValues(['reassignToUser', 'reassignToGroup']);
                                    document.getElementById('inputTaskAction').value = 'reassign';"/>
                        <input type="text" name="reassignToUser" id="reassignToUser" class="DefaultValue"
                                value="<%= getMessage(KEY_PAGES_TASK_TOAUSER, tsk_locale) %>"
                                onfocus="clearDefaultValue(this)"
                                onblur="restoreDefaultValue(this, '<%= getMessage(KEY_PAGES_TASK_TOAUSER, tsk_locale) %>')"/>
                        <input type="text" name="reassignToGroup" id="reassignToGroup" class="DefaultValue"
                                value="<%= getMessage(KEY_PAGES_TASK_TOAGROUP, tsk_locale) %>"
                                onfocus="clearDefaultValue(this)"
                                onblur="restoreDefaultValue(this, '<%= getMessage(KEY_PAGES_TASK_TOAGROUP, tsk_locale) %>')"/>

                        <small><%= getMessage(KEY_GLOBAL_OR, tsk_locale) %>
                            <a href="javascript: switchClaimAndReassign();">
                                <%= getMessage(KEY_PAGES_TASK_CLAIM, tsk_locale) %>
                            </a>
                        </small>
                    </span>

                </form>
            </div>
        <% } %>
    </div>

    <% if (tsk_task.getStatus() != TaskStatus.ASSIGNED) { %>
        <%
            // Decide whether we need a read-write output, or a read-only one.
            if ((tsk_task.getStatus() == TaskStatus.CLAIMED) &&
                    tsk_task.getClaimedBy().equals(tsk_userId)) {

                request.setAttribute(TASK_OUTPUT_DATA_READ_ONLY_ATTRIBUTE, Boolean.FALSE);
            } else {
                request.setAttribute(TASK_OUTPUT_DATA_READ_ONLY_ATTRIBUTE, Boolean.TRUE);
            }

            // Define the mode of operation for the output handler.
            request.setAttribute(TASK_OUTPUT_HANDLER_MODE_ATTRIBUTE, OUTPUT_MODE);
        %>

        <div id="taskOutput">
            <% if (tsk_outputDataParseException != null) { %>
                <div id="taskOutputDataParseErrorMessage">
                    <%= getMessage(KEY_PAGES_TASK_PARSEERROR, tsk_locale,
                            tsk_outputDataParseException.getLocalizedMessage()) %>
                </div>
            <% } %>

            <div id="taskOutputData">
                <form id="taskOutputDataForm" action="task.jsp?id=<%= tsk_taskId %>" method="post">
                    <input id="outputTaskAction" type="hidden" name="do" value=""/>

                    <% { %>
                    <%@include file="WEB-INF/includes/output-handler.jsp"%>
                    <% } %>

                    <% if ((tsk_task.getStatus() == TaskStatus.CLAIMED) &&
                            tsk_task.getClaimedBy().equals(tsk_userId)) { %>
                        <div id="taskOutputMenu">
                            <input class="Button"
                                    type="submit"
                                    value="<%= getMessage(KEY_PAGES_TASK_SAVE, tsk_locale) %>"
                                    onclick="document.getElementById('outputTaskAction').value='save';"/>
                            <%--<small><%= getMessage(KEY_GLOBAL_OR, tsk_locale) %></small>--%>
                            <input class="Button"
                                    type="submit"
                                    value="<%= getMessage(KEY_PAGES_TASK_SAVEANDCOMPLETE, tsk_locale) %>"
                                    onclick="document.getElementById('outputTaskAction').value='save-and-complete';"/>
                            <%--<small><%= getMessage(KEY_GLOBAL_OR, tsk_locale) %></small>--%>
                            <input class="Button"
                                    type="submit"
                                    value="<%= getMessage(KEY_PAGES_TASK_REVOKE, tsk_locale) %>"
                                    onclick="document.getElementById('outputTaskAction').value='revoke';"/>
                        </div>
                    <% } %>
                </form>
            </div>
        </div>
    <% } %>

<% } else { %>
    <div id="errorMessage">
        <%= getMessage(KEY_PAGES_TASK_NOSUCHTASKERROR, tsk_locale) %>
    </div>
<% } %>

<%@include file="WEB-INF/includes/footer.jsp"%>

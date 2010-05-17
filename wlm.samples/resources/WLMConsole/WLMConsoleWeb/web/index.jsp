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
    @author Alexey Anjeleevich, Alexey.Anjeleevich@Sun.COM
--%>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@page import="com.sun.glassfishesb.wlm.console.*" %>
<%@page import="sun.com.jbi.wfse.wsdl.taskcommon.*" %>
<%@page import="java.util.*" %>
<%@page import="java.net.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Constants.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Utils.*" %>
<%@include file="WEB-INF/includes/common.jsp" %>

<%
    final String idx_indexTarget = request.getContextPath() + INDEX_PAGE_URL;
    
    final Locale idx_locale = (Locale) request.getAttribute(LOCALE_ATTRIBUTE);
    final String idx_userId = (String) request.getAttribute(USER_ID_ATTRIBUTE);

    if ("last-query".equalsIgnoreCase(request.getQueryString())) {
        final TaskListPage idx_last_page = (TaskListPage) session
                .getAttribute(TaskListPage.SESSION_ATTRIBUTE);
        response.sendRedirect(idx_indexTarget
                + ((idx_last_page != null)
                        ? idx_last_page.createRedirect()
                        : ""));
        return;
    } else if ("my-tasks".equalsIgnoreCase(request.getQueryString())) {
        String idx_encodedUserId = "";
        try {
            idx_encodedUserId = URLEncoder.encode(idx_userId, "UTF-8");
        } catch (Exception ex) {
            // never happens
        }

        final StringBuilder idx_redirectBuilder = new StringBuilder();
        idx_redirectBuilder.append("?status=claimed");
        idx_redirectBuilder.append("&status=escalated");
        idx_redirectBuilder.append("&status=completed");
        idx_redirectBuilder.append("&status=expired");
        idx_redirectBuilder.append("&status=failed");
        idx_redirectBuilder.append("&users=");
        idx_redirectBuilder.append(idx_encodedUserId);

        response.sendRedirect(idx_indexTarget + idx_redirectBuilder.toString());
        return;
    }

    final TaskListPage idx_page = TaskListPage.get(request);
    final List<TaskType> idx_taskList = idx_page.fetchTasks();

    if (!"".equals(idx_page.createRedirect())) {
        session.setAttribute(TaskListPage.SESSION_ATTRIBUTE, idx_page);
    }
%>
<%@include file="WEB-INF/includes/header.jsp" %>
<%@include file="WEB-INF/includes/search.jsp" %>

<table id="paginatorTop" cellspacing="0">
    <tr>
        <td id="paginatorTopPages">
            <% { %>
            <%@include file="WEB-INF/includes/pages.jsp" %>
            <% } %>
        </td>
        <td id="sortByLabel"><%= getMessage(KEY_PAGES_INDEX_SORTBY, idx_locale) %></td>
        <td id="sortByField">
            <form id="changeOrder" action="<%= idx_indexTarget %>" method="get">
                <select name="order" onchange="return submitForm('changeOrder')">
                    <% for (SortField idx_field : SortField.values()) { %>
                    <option <%=(idx_field == idx_page.getSortField()) ? "selected" : "" %>
                            value="<%=idx_field.value() %>">
                        <%= formatOrder(idx_field, idx_locale) %>
                    </option>
                    <% } %>
                </select>
                <% if (!idx_page.isDefaultFirstTask()) { %>
                    <input type="hidden" name="start" value="<%= idx_page.getFirstTask() %>"/>
                <% } %>
                <% if (!idx_page.isDefaultPageSize()) { %>
                    <input type="hidden" name="size" value="<%= idx_page.getPageSize() %>"/>
                <% } %>
                <% if (idx_page.isAdvancedSearchMode()) { %>
                    <% if (idx_page.hasStatuses()) { %>
                        <% for (TaskStatus idx_status : idx_page.getStatuses()) { %>
                            <input type="hidden" name="status"
                                    value="<%=idx_status.value().toLowerCase() %>" />
                        <% } %>
                    <% } %>
                    <% if (idx_page.hasUsers()) { %>
                        <input type="hidden" name="users"
                                value="<%=idx_page.getUsersString(true) %>" />
                    <% } %>
                    <% if (idx_page.hasGroups()) { %>
                        <input type="hidden" name="groups"
                                value="<%=idx_page.getGroupsString(true) %>" />
                    <% } %>
                    <% if (idx_page.hasSearchString()) { %>
                        <input type="hidden" name="search"
                                value="<%=idx_page.getSearchString(true) %>" />
                    <% } %>
                <% } else if (idx_page.isBasicSearchMode()) { %>
                    <input type="hidden" name="search"
                            value="<%=idx_page.getSearchString(true) %>"/>
                <% } %>
            </form>
        </td>
    </tr>
</table>

<% if (idx_taskList.isEmpty()) { %>
<div id="noTasksWereFound"><%= getMessage(KEY_PAGES_NO_TASK_WERE_FOUND, idx_locale) %></div>
<% } else { %>

<table cellspacing="0" id="tasksList">
    <thead>
        <tr>
            <th class="ID"><%= getMessage(KEY_PAGES_INDEX_ID, idx_locale) %></th>
            <th class="Title"><%= getMessage(KEY_PAGES_INDEX_TITLE, idx_locale) %></th>
            <th class="Status"><%= getMessage(KEY_PAGES_INDEX_STATUS, idx_locale) %></th>
            <th class="SubmittedOn"><%= getMessage(KEY_PAGES_INDEX_SUBMITTEDON, idx_locale) %></th>
            <th class="AssignedTo"><%= getMessage(KEY_PAGES_INDEX_ASSIGNEDTO, idx_locale) %></th>
            <th class="ClaimedBy"><%= getMessage(KEY_PAGES_INDEX_CLAIMEDBY, idx_locale) %></th>
            <th class="Deadline"><%= getMessage(KEY_PAGES_INDEX_DEADLINE, idx_locale) %></th>
        </tr>
    </thead>
    <tbody>
        <%
            boolean idx_oddRow = true;
        %>

        <% for (TaskType idx_task : idx_taskList) { %>
        <tr class="<%= idx_oddRow ? "OddRow" : "EvenRow" %>">
            <td class="ID Priority<%= formatPriority(idx_task) %>"><%= idx_task.getTaskId() %></td>
            <td class="Title">
                <a href="task.jsp?id=<%= idx_task.getTaskId() %>"><%= formatTitle(idx_task) %></a>
                <% if (idx_task.getStatus() == TaskStatus.ASSIGNED) { %>
                <div class="Actions">
                    <a href="task.jsp?id=<%= idx_task.getTaskId() %>&do=claim">
                        <%= getMessage(KEY_PAGES_INDEX_CLAIM, idx_locale) %>
                    </a>
                </div>
                <% } %>
            </td>
            <td class="Status"><%= formatStatus(idx_task, idx_locale) %></td>
            <td class="SubmittedOn"><%= formatSubmittedOn(idx_task) %></td>
            <td class="AssignedTo"><%= formatAssignedTo(idx_task) %></td>
            <td class="ClaimedBy"><%= formatClaimedBy(idx_task) %></td>
            <td class="Deadline"><%= formatDeadline(idx_task) %></td>
        </tr>
        <%
            idx_oddRow = !idx_oddRow;
        %>
        <% } %>
    </tbody>
</table>
<% } %>

<table id="paginatorBottom" cellspacing="0">
    <tr>
        <td id="paginatorBottomPages">
            <% { %>
            <%@include file="WEB-INF/includes/pages.jsp" %>
            <% } %>
        </td>
        <td id="tasksPerPageLabel"><%= getMessage(KEY_PAGES_INDEX_TASKSPERPAGE, idx_locale) %></td>
        <td id="tasksPerPageField">
            <form id="changePageCount" action="<%= idx_indexTarget %>" method="get">
                <select name="size" onchange="return submitForm('changePageCount')">
                    <option <%= (idx_page.getPageSize() == 10) ? "selected" : "" %>
                            value="10">10</option>
                    <option <%= (idx_page.getPageSize() == 20) ? "selected" : "" %>
                            value="20">20</option>
                    <option <%= (idx_page.getPageSize() == 30) ? "selected" : "" %>
                            value="30">30</option>
                    <option <%= (idx_page.getPageSize() == 50) ? "selected" : "" %>
                            value="50">50</option>
                    <option <%= (idx_page.getPageSize() == 100) ? "selected" : "" %>
                            value="100">100</option>
                </select>
                <% if (!idx_page.isDefaultFirstTask()) { %>
                <input type="hidden" name="start" value="<%= idx_page.getFirstTask() %>"/>
                <% } %>
                <% if (!idx_page.isDefaultSortField()) { %>
                <input type="hidden" name="order" value="<%= idx_page.getSortField().value() %>"/>
                <% } %>
                <% if (idx_page.isAdvancedSearchMode()) { %>
                    <% if (idx_page.hasStatuses()) { %>
                        <% for (TaskStatus idx_status : idx_page.getStatuses()) { %>
                            <input type="hidden" name="status"
                                    value="<%=idx_status.value().toLowerCase() %>" />
                        <% } %>
                    <% } %>
                    <% if (idx_page.hasUsers()) { %>
                        <input type="hidden" name="users"
                                value="<%=idx_page.getUsersString(true) %>" />
                    <% } %>
                    <% if (idx_page.hasGroups()) { %>
                        <input type="hidden" name="groups"
                                value="<%=idx_page.getGroupsString(true) %>" />
                    <% } %>
                    <% if (idx_page.hasSearchString()) { %>
                        <input type="hidden" name="search"
                                value="<%=idx_page.getSearchString(true) %>" />
                    <% } %>
                <% } else if (idx_page.isBasicSearchMode()) { %>
                    <input type="hidden" name="search"
                            value="<%=idx_page.getSearchString(true) %>" />
                <% } %>
            </form>
        </td>
    </tr>
</table>

<table cellspacing="0" id="priorityInfo">
    <tr>
        <td class="Priority10" style="border:1px solid #ccc">&nbsp;&nbsp;&nbsp;</td>
        <td>&nbsp;&#151;&nbsp;</td>
        <td><%= getMessage(KEY_PAGES_INDEX_HIGHPRIORITY, idx_locale) %></td>
        <td>&nbsp;</td>
        <td class="Priority5" style="border:1px solid #ccc">&nbsp;&nbsp;&nbsp;</td>
        <td>&nbsp;&#151;&nbsp;</td>
        <td><%= getMessage(KEY_PAGES_INDEX_NORMALPRIORITY, idx_locale) %></td>
        <td>&nbsp;</td>
        <td class="Priority1" style="border:1px solid #ccc">&nbsp;&nbsp;&nbsp;</td>
        <td>&nbsp;&#151;&nbsp;</td>
        <td><%= getMessage(KEY_PAGES_INDEX_LOWPRIORITY, idx_locale) %></td>
    </tr>
</table>

<%@include file="WEB-INF/includes/footer.jsp" %>

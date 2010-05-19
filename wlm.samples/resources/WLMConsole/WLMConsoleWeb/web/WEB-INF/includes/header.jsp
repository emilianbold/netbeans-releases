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
    @author Alexey Anjeleevich, Alexey.Anjeleevich@Sun.COM
--%>
<%@page import="com.sun.glassfishesb.wlm.console.*" %>
<%@page import="java.util.*" %>
<%@page import="sun.com.jbi.wfse.wsdl.taskcommon.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Constants.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Utils.*" %>

<%
    request.setAttribute(DEBUG_RENDERING_START_TIME_MARKER_ATTRIBUTE, System.currentTimeMillis());

    final String hdr_userId = (String) request.getAttribute(USER_ID_ATTRIBUTE);
    final Locale hdr_locale = (Locale) request.getAttribute(LOCALE_ATTRIBUTE);
    final TaskType hdr_task = (TaskType) request.getAttribute(TASK_ATTRIBUTE);

    final String hdr_titleArg0 = hdr_userId;
    final String hdr_titleArg1 = hdr_task != null ? "" + hdr_task.getTaskId() : "";
    final String hdr_titleArg2 = hdr_task != null ? "" + hdr_task.getTitle() : "";
    final String hdr_titleArg3 = hdr_task != null ? "" + hdr_task.getTaskDefId() : "";

    final String hdr_indexTarget = request.getContextPath() + INDEX_PAGE_URL;

    final TaskListPage hdr_lastQuery = (TaskListPage)
            session.getAttribute(TaskListPage.SESSION_ATTRIBUTE);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    
    <title><%= getPageTitle(request.getServletPath(), hdr_locale,
                    hdr_titleArg0, hdr_titleArg1, hdr_titleArg2, hdr_titleArg3) %></title>

    <link type="text/css" rel="stylesheet" href="css/common.css"/>
    <link type="text/css" rel="stylesheet" href="css/login.css"/>
    <link type="text/css" rel="stylesheet" href="css/search.css"/>
    <link type="text/css" rel="stylesheet" href="css/task.css"/>
    <link type="text/css" rel="stylesheet" href="css/tasks-list.css"/>

    <link type="text/css" rel="stylesheet" href="css/handlers/default.css"/>
    <link type="text/css" rel="stylesheet" href="css/handlers/purchase-order-sample.css"/>

    <script type="text/javascript" src="js/task.js"></script>

    <script type="text/javascript">
        var datePattern = "<%= getDatePattern(hdr_locale) %>";
        var currentDate = new Date();
        var timeRegexp = /(\d\d?\D\d\d?)\D\d\d?/;
        
        function printDate(utcms) {
            var taskDate = new Date(utcms);
            var timeString = taskDate.toLocaleTimeString().replace(timeRegexp, "$1");

            if (taskDate.getDate() == currentDate.getDate()) {
                document.write(timeString);
            } else {
                var dateString = datePattern.
                        replace("YYYY", taskDate.getFullYear()).
                        replace("YY", formatNumber(taskDate.getFullYear() % 100, 2)).
                        replace("Y", taskDate.getFullYear() % 100).
                        replace("MM", formatNumber(taskDate.getMonth() + 1, 2)).
                        replace("M", taskDate.getMonth() + 1).
                        replace("DD", formatNumber(taskDate.getDate(), 2)).
                        replace("D", taskDate.getDate());

                document.write(dateString + "&nbsp;" + timeString);
            }
        }

        function formatNumber(number, length) {
            var result = "" + number;

            while (result.length < length) {
                result = "0" + result;
            }

            return result;
        }

        function submitForm(formId) {
            document.getElementById(formId).submit();
            return true;
        }
    </script>
</head>
<body>
    <div id="wrapper">
        <div id="header" title="<%= getMessage(KEY_GLOBAL_TITLE, hdr_locale) %>"></div>
        <div id="menu">
            <div id="menuPages">
                <% if (hdr_userId != null) { %>
                    <a href="<%= hdr_indexTarget %>">
                            <%= getMessage(KEY_GLOBAL_ALLTASKS, hdr_locale) %>
                    </a>
                    |
                    <a href="<%= hdr_indexTarget + "?my-tasks" %>">
                            <%= getMessage(KEY_GLOBAL_MYTASKS, hdr_locale) %>
                    </a>
                    <% if ((hdr_lastQuery != null) &&
                            !"".equals(hdr_lastQuery.createRedirect())) { %>
                        |
                        <a href="<%= hdr_indexTarget + "?last-query" %>">
                            <%= getMessage(KEY_GLOBAL_LASTQUERY, hdr_locale) %>
                        </a>
                    <% } %>
                <% } else { %>
                    &nbsp;
                <% } %>
            </div>
            <div id="menuOther">
                <% if (hdr_userId != null) { %>
                <%= getMessage(KEY_GLOBAL_LOGGEDINAS, hdr_locale) %>
                    <b><%= hdr_userId %></b>
                    |
                    <a href="<%= hdr_indexTarget %>?logout=true">
                        <%= getMessage(KEY_GLOBAL_LOGOUT, hdr_locale) %>
                    </a>
                <% } else { %>
                    <%= getMessage(KEY_GLOBAL_NOTLOGGEDIN, hdr_locale) %>
                    <% if (!LOGIN_PAGE_URL.equals(request.getServletPath())) { %>
                        |
                        <a href="<%= request.getContextPath() + LOGIN_PAGE_URL %>">
                            <%= getMessage(KEY_GLOBAL_LOGIN, hdr_locale) %>
                        </a>
                    <% } %>
                <% } %>
                |
                <a href="<%= request.getContextPath() + HELP_PAGE_URL %>">
                    <%= getMessage(KEY_GLOBAL_HELP, hdr_locale) %>
                </a>
            </div>
        </div>

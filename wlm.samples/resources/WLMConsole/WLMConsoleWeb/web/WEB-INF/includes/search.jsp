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
<%@page import="com.sun.glassfishesb.wlm.console.*" %>
<%@page import="sun.com.jbi.wfse.wsdl.taskcommon.*" %>
<%@page import="java.util.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Constants.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Utils.*" %>

<%
    Locale sch_locale = (Locale) request.getAttribute(LOCALE_ATTRIBUTE);
    TaskListPage sch_page = TaskListPage.get(request);

    Set<TaskStatus> sch_statuses = new HashSet<TaskStatus>(sch_page.getStatuses());
    if (sch_statuses.isEmpty()) {
        sch_statuses.add(TaskStatus.ASSIGNED);
        sch_statuses.add(TaskStatus.CLAIMED);
    }

    boolean sch_advanced = sch_page.isAdvancedSearchMode();
    boolean sch_basic = sch_page.isBasicSearchMode();

    final String sch_indexTarget = request.getContextPath() + INDEX_PAGE_URL;
%>

<script type="text/javascript">
    function switchToAdvancedSearch() {
        document.getElementById("basicSearch").style.display = "none";
        document.getElementById("advancedSearch").style.display = "";
    }

    function switchToBasicSearch() {
        document.getElementById("advancedSearch").style.display = "none";
        document.getElementById("basicSearch").style.display = "";
    }
</script>

<div class="SearchBlock" id="basicSearch" <%=(sch_advanced) ? "style=\"display:none\"" : "" %>>
    <form id="basicSearchForm" action="<%= sch_indexTarget %>" method="get">
        <table cellpadding="0" cellspacing="0" border="0">
            <tr>
                <td><%= getMessage(KEY_PAGES_SEARCH_SEARCHLABEL, sch_locale) %></td>
                <td>&nbsp;</td>
                <td><input size="32" name="search" value="<%=(sch_basic) ? sch_page.getSearchString(true) : "" %>"/></td>
                <td>&nbsp;</td>
                <td><input class="Button" type="submit" value="<%= getMessage(KEY_PAGES_SEARCH_SEARCH, sch_locale) %>" /></td>
            </tr>
        </table>
        <% if (!sch_page.isDefaultSortField()) { %>
            <input type="hidden" name="order" value="<%=sch_page.getSortField().value() %>"/>
        <% } %>
        <% if (!sch_page.isDefaultPageSize()) { %>
            <input type="hidden" name="size" value="<%=sch_page.getPageSize() %>"/>
        <% } %>
    </form>
    <div class="SwitchSearch">
        <%= getMessage(KEY_PAGES_SEARCH_SWITCHTO, sch_locale) %> 
        <a href="javascript: switchToAdvancedSearch();">
            <%= getMessage(KEY_PAGES_SEARCH_ADVANCEDSEARCH, sch_locale) %>
        </a>
    </div>
</div>

<div class="SearchBlock" id="advancedSearch"
        <%=(!sch_advanced) ? "style=\"display:none\"" : "" %>>
    <form id="advancedSearchForm" action="<%= sch_indexTarget %>" method="get">
        <table cellpadding="0" cellspacing="0" border="0">
            <tr>
                <th class="StatusesColumn">
                    <%= getMessage(KEY_PAGES_SEARCH_STATUSES, sch_locale) %>
                </th>
                <th class="OwnersColumn">
                    <%= getMessage(KEY_PAGES_SEARCH_OWNERS, sch_locale) %>
                </th>
                <th class="KeywordsColumn" width="100%">
                    <%= getMessage(KEY_PAGES_SEARCH_TEXTSEARCH, sch_locale) %>
                </th>
            </tr>
            <tr valign="top">
                <td class="StatusesColumn"><select size="6" multiple="true" name="status">
                        <option value="<%=TaskStatus.ASSIGNED.value().toLowerCase() %>" <%=(sch_statuses.contains(TaskStatus.ASSIGNED)) ? "selected" : "" %>><%=formatStatus(TaskStatus.ASSIGNED, sch_locale) %></option>
                        <option value="<%=TaskStatus.CLAIMED.value().toLowerCase() %>" <%=(sch_statuses.contains(TaskStatus.CLAIMED)) ? "selected" : "" %>><%=formatStatus(TaskStatus.CLAIMED, sch_locale) %></option>
                        <option value="<%=TaskStatus.ESCALATED.value().toLowerCase() %>" <%=(sch_statuses.contains(TaskStatus.ESCALATED)) ? "selected" : "" %>><%=formatStatus(TaskStatus.ESCALATED, sch_locale) %></option>
                        <option value="<%=TaskStatus.COMPLETED.value().toLowerCase() %>" <%=(sch_statuses.contains(TaskStatus.COMPLETED)) ? "selected" : "" %>><%=formatStatus(TaskStatus.COMPLETED, sch_locale) %></option>
                        <option value="<%=TaskStatus.EXPIRED.value().toLowerCase() %>" <%=(sch_statuses.contains(TaskStatus.EXPIRED)) ? "selected" : "" %>><%=formatStatus(TaskStatus.EXPIRED, sch_locale) %></option>
                        <option value="<%=TaskStatus.FAILED.value().toLowerCase() %>" <%=(sch_statuses.contains(TaskStatus.FAILED)) ? "selected" : "" %>><%=formatStatus(TaskStatus.FAILED, sch_locale) %></option>
                    </select>
                </td>
                <td class="OwnersColumn">
                    <table cellspacing="0" cellpadding="0" border="0">
                        <tr>
                            <td><%= getMessage(KEY_PAGES_SEARCH_USERS, sch_locale) %> </td>
                            <td>&nbsp;</td>
                            <td><input name="users" value="<%=(sch_advanced) ? sch_page.getUsersString(true) : "" %>"/></td>
                        </tr>
                        <tr>
                            <td><%= getMessage(KEY_PAGES_SEARCH_GROUPS, sch_locale) %> </td>
                            <td>&nbsp;</td>
                            <td style="padding:4pt 0"><input name="groups" value="<%=(sch_advanced) ? sch_page.getGroupsString(true) : "" %>"/></td>
                        </tr>
                    </table>
                    <div class="Help">
                        <%= getMessage(KEY_PAGES_SEARCH_OWNERSHINT, sch_locale) %>
                    </div>
                </td>
                <td class="KeywordsColumn">
                    <div>
                        <input name="search" value="<%= (sch_advanced) ? sch_page.getSearchString(true) : "" %>"/>
                    </div>
                </td>
            </tr>
        </table>
        <div id="searchButtonContainer"><input class="Button" type="submit" value="<%= getMessage(KEY_PAGES_SEARCH_SEARCH, sch_locale) %>"/></div>
        <% if (!sch_page.isDefaultSortField()) { %>
            <input type="hidden" name="order" value="<%=sch_page.getSortField().value() %>"/>
        <% } %>
        <% if (!sch_page.isDefaultPageSize()) { %>
            <input type="hidden" name="size" value="<%=sch_page.getPageSize() %>"/>
        <% } %>
    </form>
    <div class="SwitchSearch">
        <%= getMessage(KEY_PAGES_SEARCH_SWITCHTO, sch_locale) %>
        <a href="javascript: switchToBasicSearch();">
            <%= getMessage(KEY_PAGES_SEARCH_BASICSEARCH, sch_locale) %>
        </a>
    </div>
</div>

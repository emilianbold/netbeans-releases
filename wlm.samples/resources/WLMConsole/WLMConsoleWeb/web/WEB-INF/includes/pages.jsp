<%--
 - Copyright (c) 2010, Oracle. All rights reserved.
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
 - * Neither the name of Oracle nor the names of its contributors
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
<%@page import="java.util.*" %>
<%@page import="com.sun.glassfishesb.wlm.console.*" %>
<%@page import="sun.com.jbi.wfse.wsdl.taskcommon.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Constants.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Utils.*" %>

<%
    final Locale pgs_locale = (Locale) request.getAttribute(LOCALE_ATTRIBUTE);
    final TaskListPage pgs_page = TaskListPage.get(request);

    int pgs_firstTask = pgs_page.getFirstTask();
    int pgs_pageSize = pgs_page.getPageSize();
    int pgs_totalTaskCount = pgs_page.getTotalTaskCount();

    int pgs_currentPage = (pgs_firstTask / pgs_pageSize) + 1;
    int pgs_pageCount = (pgs_totalTaskCount + pgs_pageSize - 1) / pgs_pageSize;

    if (pgs_currentPage > pgs_pageCount) {
        pgs_currentPage = 1;
    }

    boolean pgs_leftEllipsis = (pgs_pageCount > 9) && (pgs_currentPage > 5);
    boolean pgs_rightEllipsis = (pgs_pageCount > 9) && (pgs_currentPage + 5 <= pgs_pageCount);

    int pgs_first;
    int pgs_last;

    if (!pgs_leftEllipsis) {
        pgs_first = 1;
        pgs_last = (pgs_rightEllipsis) ? 7 : Math.min(pgs_pageCount, 9);
    } else if (!pgs_rightEllipsis) {
        pgs_last = pgs_pageCount;
        pgs_first = pgs_pageCount - 6;
    } else {
        pgs_first = pgs_currentPage - 2;
        pgs_last = pgs_currentPage + 2;
    }

    final String pgs_indexTarget = request.getContextPath() + INDEX_PAGE_URL;
%>

<div class="Pages">
<% if (pgs_totalTaskCount > 0) { %>
    <span class="Title"><%= getMessage(KEY_PAGES_PAGES, pgs_locale) %></span>
    <% if (pgs_currentPage > 1) { %>
        <a href="<%= pgs_indexTarget %><%=pgs_page.createPageHRef(pgs_currentPage - 1) %>"><%= getMessage(KEY_PAGES_PAGES_PREVIOUS, pgs_locale) %></a>
    <% } else { %>
        <span class="DisabledAnchor"><%= getMessage(KEY_PAGES_PAGES_PREVIOUS, pgs_locale) %></span>
    <% } %>

    <% if (pgs_leftEllipsis) { %>
        <a href="<%= pgs_indexTarget %><%=pgs_page.createPageHRef(1) %>">1</a>
        <span class="Ellipsis">...</span>
    <% } %>

    <% for (int pgs_pageNumber = pgs_first; pgs_pageNumber <= pgs_last; pgs_pageNumber++) { %>
        <% if (pgs_pageNumber == pgs_currentPage) { %>
            <span class="CurrentPage"><%=pgs_pageNumber %></span>
        <% } else { %>
            <a href="<%= pgs_indexTarget %><%=pgs_page.createPageHRef(pgs_pageNumber) %>"><%=pgs_pageNumber %></a>
        <% } %>
    <% } %>

    <% if (pgs_rightEllipsis) { %>
        <span class="Ellipsis">...</span>
        <a href="<%= pgs_indexTarget %><%=pgs_page.createPageHRef(pgs_pageCount) %>"><%=pgs_pageCount %></a>
    <% } %>

    <% if (pgs_currentPage < pgs_pageCount) { %>
        <a href="<%= pgs_indexTarget %><%=pgs_page.createPageHRef(pgs_currentPage + 1) %>"><%= getMessage(KEY_PAGES_PAGES_NEXT, pgs_locale) %></a>
    <% } else { %>
        <span class="DisabledAnchor"><%= getMessage(KEY_PAGES_PAGES_NEXT, pgs_locale) %></span>
    <% } %>
<% } else { %>
    &nbsp;
<% } %>
</div>

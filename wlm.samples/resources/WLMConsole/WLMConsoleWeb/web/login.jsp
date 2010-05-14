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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.sun.glassfishesb.wlm.console.*"%>
<%@include file="WEB-INF/includes/common.jsp"%>

<%
    final Locale lgn_locale = (Locale) request.getAttribute(Constants.LOCALE_ATTRIBUTE);
%>

<%@include file="WEB-INF/includes/header.jsp"%>

<% if (request.getAttribute(Constants.LOGIN_FAILED_MARKER_ATTRIBUTE) != null) { %>
<div id="errorMessage">
    <%= Utils.getMessage(Constants.KEY_PAGES_LOGIN_LOGINFAILED, lgn_locale) %>
</div>
<% } %>

<form id="loginMenuForm" method="post" action="j_security_check">
<table id="loginMenu">
    <tr>
        <td>
            <%=Utils.getMessage(Constants.KEY_PAGES_LOGIN_USERNAME, lgn_locale)%>
        </td>
        <td>
            <input id="loginMenuUsername" name="j_username" type="text"/>
        </td>
    </tr>
    <tr>
        <td>
            <%=Utils.getMessage(Constants.KEY_PAGES_LOGIN_PASSWORD, lgn_locale)%>
        </td>
        <td>
            <input id="loginMenuPassword" name="j_password" type="password"/>
        </td>
    </tr>
    <tr>
        <td colspan="2" id="loginMenuSubmitContainer">
            <input class="Button" type="submit" id="loginMenuSubmit" name="loginSubmit"
                    value="<%=Utils.getMessage(Constants.KEY_PAGES_LOGIN_SUBMIT, lgn_locale)%>"/>
        </td>
    </tr>
</table>
</form>

<%@include file="WEB-INF/includes/footer.jsp"%>

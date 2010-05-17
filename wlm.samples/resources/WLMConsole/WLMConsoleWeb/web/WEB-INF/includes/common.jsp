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
<%@page import="com.sun.glassfishesb.wlm.console.*"%>
<%@page import="java.util.*"%>
<%@page import="static com.sun.glassfishesb.wlm.console.Constants.*" %>
<%@page import="static com.sun.glassfishesb.wlm.console.Utils.*" %>

<%
    request.setAttribute(DEBUG_GENERATION_START_TIME_MARKER_ATTRIBUTE, System.currentTimeMillis());

    request.setCharacterEncoding(UTF8);

    final Locale cmn_locale = request.getLocale();
    request.setAttribute(LOCALE_ATTRIBUTE, cmn_locale);

    // Zero. Check whether we're at '/' and redirect to 'index.jsp', if we are. Works only (and not
    // bullet-proof as well) if the context path is not "".
    final String cmn_contextPath = request.getContextPath();
    if (!cmn_contextPath.equals("") && request.getRequestURI().endsWith(cmn_contextPath + "/")) {
        response.sendRedirect(request.getContextPath() + INDEX_PAGE_URL);
        return;
    }

    // First check whether someone asked us to log out. If that is true, invalidate the session
    // and send the clever guy to the index page.
    final String cmn_logout = request.getParameter(LOGOUT_PARAMETER);
    if (cmn_logout != null) {
        session.invalidate();

        response.sendRedirect(request.getContextPath() + INDEX_PAGE_URL);
        return;
    }

    final String cmn_userId =
            request.getUserPrincipal() == null ? null : request.getUserPrincipal().getName();

    // Then, if we're not logged in, we should be redirected to the login page. Unless we're
    // already there of course. :) We would not redirect from the "login failed" page either. If
    // we're logged in we should redirect _from_ the login and "login failed" pages.
    if (cmn_userId == null) {
        if (!LOGIN_PAGE_URL.equals(request.getServletPath()) &&
                !LOGIN_FAILED_PAGE_URL.equals(request.getServletPath()) &&
                !HELP_PAGE_URL.equals(request.getServletPath())) {

            response.sendRedirect(request.getContextPath() + LOGIN_PAGE_URL);
            return;
        }
    } else {
        request.setAttribute(USER_ID_ATTRIBUTE, cmn_userId);

        if (LOGIN_PAGE_URL.equals(request.getServletPath()) ||
                LOGIN_FAILED_PAGE_URL.equals(request.getServletPath())) {

            response.sendRedirect(request.getContextPath() + INDEX_PAGE_URL);
            return;
        }
    }
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!--
  Copyright 2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<%
  if (request.getParameter("logoff") != null) {
    session.invalidate();
    response.sendRedirect("index.jsp");
    return;
  }
%>
<html>
<head>
<title>Protected Page for Examples</title>
</head>
<body bgcolor="white">

You are logged in as remote user <b><c:out value="${pageContext.request.remoteUser}" /></b>
in session <b>${pageContext.session.id}</b><br><br>

<%
  if (request.getUserPrincipal() != null) {
%>
    Your user principal name is
    <b><%= request.getUserPrincipal().getName() %></b><br><br>
<%
  } else {
%>
    No user principal could be identified.<br><br>
<%
  }
%>

<c:set var="role" value="${pageContext.request.getParameter('role')}" />
<c:choose>
<c:when test="${role == null}">
    <c:set var="role" value="" />
</c:when>
</c:choose>

<c:choose>
<c:when test="${LENGTH[role] > 0}">
    <c:choose>
        <c:when test="${pageContext.request.isUserInRole(role) == true}">
            <c:out value="You have been granted role" />
            <b><c:out value="${role}" /></b><br><br>">
        </c:when>
        <c:otherwise>
            You have <i>not</i> been granted role
            <b><c:out value="${role}" /></b><br><br>">
        </c:otherwise>
    </c:choose>
</c:when>
</c:choose>

To check whether your username has been granted a particular role,
enter it here:
<form method="GET" action='<%= response.encodeURL("index.jsp") %>'>
<input type="text" name="role" value="${role}" />">
</form>
<br><br>

If you have configured this app for form-based authentication, you can log
off by clicking
<a href='<%= response.encodeURL("index.jsp?logoff=true") %>'>here</a>.
This should cause you to be returned to the logon page after the redirect
that is performed.

</body>
</html>

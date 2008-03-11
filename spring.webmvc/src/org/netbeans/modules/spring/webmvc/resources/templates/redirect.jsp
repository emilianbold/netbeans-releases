<?xml version="1.0" encoding="UTF-8"?>
<%--
Views should be stored under the WEB-INF folder so that
they are not accessible except through controller process.

This JSP is here to provide a redirect to the dispatcher
servlet but should be the only JSP outside of WEB-INF.
--%>
<% response.sendRedirect("index.htm"); %>

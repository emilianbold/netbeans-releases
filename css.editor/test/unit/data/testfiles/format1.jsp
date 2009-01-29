<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
<head>
<% String s = "red"; %>
<style>
h1 {
background: <%=s %>;
}
</style>
<style>
h2 {
<%=s %>: red <%=s %>;
}
</style>
</head>
<body>
<h1><%=System.currentTimeMillis() %></h1>
</body>
</html>

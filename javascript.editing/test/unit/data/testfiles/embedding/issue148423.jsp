<%-- 
    Document   : index
    Created on : Sep 25, 2008, 11:46:50 AM
    Author     : dlindsey
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h2>Hello World!</h2>
		  
		  <%
		  String FIELD_UNIQUE_RESOURCE_ID_TYPE = "";
		  String FIELD_UNIQUE_RESOURCE_ID_VALUE = "";
		  int r = 1;
		  %>
		  
		<select name="<%= FIELD_UNIQUE_RESOURCE_ID_TYPE %><%= r %>" onchange="onExpandCollapse('nameDOBIDWidget<%= r %>',document.form.<%= FIELD_UNIQUE_RESOURCE_ID_TYPE %><%= r %>, document.form.<%= FIELD_UNIQUE_RESOURCE_ID_VALUE %><%= r %>)">
			</select>
		  
	      <select name="<%= FIELD_UNIQUE_RESOURCE_ID_TYPE %><%= r %>" onchange="onExpandCollapse('nameDOBIDWidget<%= r %>',document.form.<%= FIELD_UNIQUE_RESOURCE_ID_TYPE %><%= r %>, document.form.<%= FIELD_UNIQUE_RESOURCE_ID_VALUE %><%= r %>)">
			</select>
			
			
	      <select name="<%= FIELD_UNIQUE_RESOURCE_ID_TYPE %><%= r %>" onchange="onExpandCollapse('nameDOBIDWidget<%= r %>',document.form.<%= FIELD_UNIQUE_RESOURCE_ID_TYPE %><%= r %>, document.form.<%= FIELD_UNIQUE_RESOURCE_ID_VALUE %><%= r %>)">
			</select>
						
	
    </body>
</html>


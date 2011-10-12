<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<HTML>
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
<HEAD><TITLE> 
	Calendar: A JSP APPLICATION
</TITLE></HEAD>


<BODY BGCOLOR="white">

<%@ page language="java" import="cal.*" %>
<jsp:useBean id="table" scope="session" class="cal.TableBean" />

<%
	table.processRequest(request);
	if (table.getProcessError() == false) {
%>

<CENTER>
<TABLE WIDTH=60% BGCOLOR=yellow CELLPADDING=15>
<TR>
<TD ALIGN=CENTER> <A HREF=cal1.jsp?date=prev> Prev </A>
<TD ALIGN=CENTER> <c:out value="Calendar: ${table.date}" /></TD>
<TD ALIGN=CENTER> <A HREF=cal1.jsp?date=next> Next </A>
</TR>
</TABLE>

<!-- the main table -->
<TABLE WIDTH=60% BGCOLOR=lightblue BORDER=1 CELLPADDING=10>
<TR>
<TH> Time </TH>
<TH> Appointment </TH>
</TR>
<FORM METHOD="POST" ACTION="cal1.jsp">
    <c:forEach begin="0" end="${table.entries.rows - 1}" var="index">
        <c:set var="entry" value="${table.entries.getEntry(index)}" />
        <TR>
	<TD> 
	<A HREF="cal2.jsp?time=${entry.hour}">
            <c:out value="${entry.hour}" />
        </A>
	</TD>
	<TD BGCOLOR="${entry.color}">
            <c:out value="${entry.description}" />
	</TD> 
	</TR>
    </c:forEach>
</FORM>
</TABLE>
<BR>

<TABLE WIDTH=60% BGCOLOR=yellow CELLPADDING=15>
<TR>
<TD ALIGN=CENTER>
    <c:out value="${table.name} : ${table.email}" />
</TD>
</TR>
</TABLE>
</CENTER>

<%
	} else {
%>
<font size=5>
    You must enter your name and email address correctly.
</font>
<%
	}
%>

</BODY>
</HTML>

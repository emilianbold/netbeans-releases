<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
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

<head>
    <title> Calendar: A JSP APPLICATION </title>
</head>

<body BGCOLOR="white">
<jsp:useBean id="table" scope="session" class="cal.TableBean" />
<c:set var="time" value="${pageContext.request.getParameter('time')}" />

<font size=5> Please add the following event:
<br/> <h3> <c:out value="Date ${table.date}" />
<br/> Time <c:out value="${time}" /></h3>
</font>
<form method="post" action="cal1.jsp">
    <br/> <input name="date" type=hidden value="current" />
    <br/> <input name="time" type=hidden value="<c:out value="${time}" />" />
    <br/> <h2> Description of the event <input name="description" type=text size=20 /> </h2>
    <br/> <input type=submit value="submit" />
</form>

</body>
</html>

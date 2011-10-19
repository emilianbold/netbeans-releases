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

<body bgcolor="white">
<font size=5 color="red">
<jsp:useBean id="foo" scope="page" class="checkbox.CheckTest" />
<jsp:setProperty name="foo" property="fruit" param="fruit" />

<hr/>
The checked fruits (got using request) are:
<br/>

<ul>
<c:set var="requestFruits" value="${pageContext.request.getParameterValues('fruit')}" />
<c:choose>
    <c:when test="${requestFruits != null}">
        <c:forEach var="fruit" items="${requestFruits}">
            <li>
                <c:out value="${fruit}" />
            </li>
        </c:forEach>
    </c:when>
    <c:otherwise>
        <c:out value="None selected" />
    </c:otherwise>
</c:choose>
</ul>
            
<hr/>
The checked fruits (got using beans) are:
<br/>

<ul>
<c:set var="beanFruits" value="${foo.fruit}" />
<c:choose>
    <c:when test="${beanFruits[0].equals('1') == false}">
        <c:forEach var="fruit" items="${beanFruits}">
            <li>
                <c:out value="${fruit}" />
            </li>
        </c:forEach>
    </c:when>
    <c:otherwise>
        <c:out value="None selected" />
    </c:otherwise>
</c:choose>
</ul>

</font>
</body>
</html>

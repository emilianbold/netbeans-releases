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
<h1> Request Information </h1>
<font size="4">

<c:out value="JSP Request Method: ${pageContext.request.method}" /><br/>
<c:out value="Request URI: ${pageContext.request.requestURI}" /><br/>
<c:out value="Request Protocol: ${pageContext.request.protocol}" /><br/>
<c:out value="Servlet path: ${pageContext.request.servletPath}" /><br/>
<c:out value="Path info: ${pageContext.request.pathInfo}" /><br/>
<c:out value="Query string: ${pageContext.request.queryString}" /><br/>
<c:out value="Content length: ${pageContext.request.contentLength}" /><br/>
<c:out value="Content type: ${pageContext.request.contentType}" /><br/>
<c:out value="Server name: ${pageContext.request.serverName}" /><br/>
<c:out value="Server port: ${pageContext.request.serverPort}" /><br/>
<c:out value="Remote user: ${pageContext.request.remoteUser}" /><br/>
<c:out value="Remote address: ${pageContext.request.remoteAddr}" /><br/>
<c:out value="Remote host: ${pageContext.request.remoteHost}" /><br/>
<c:out value="Authorization scheme: ${pageContext.request.authType}" /><br/>
<c:out value="Locale: ${pageContext.request.locale}" /><br/>

<hr/>
The browser you are using is <c:out value="${pageContext.request.getHeader('User-agent')}" />
<hr/>

</font>
</body>
</html>

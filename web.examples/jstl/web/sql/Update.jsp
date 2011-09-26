<%@page import="org.apache.taglibs.standard.examples.util.DataSourceProvider"%>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: SQL action examples</title>
</head>
<body bgcolor="#FFFFFF">

<h1>SQL Update Execution</h1>


<%
    DataSourceProvider dataSource = new DataSourceProvider(session);
%>

<hr>

<sql:transaction dataSource="<%=dataSource %>">

  <sql:update var="newTable">
    create table mytable (
      nameid int primary key,
      name varchar(80)
    )
  </sql:update>

<h2>Inserting three rows into table</h2>
  <sql:update var="updateCount">
    INSERT INTO mytable VALUES (1,'Paul Oakenfold')
  </sql:update>
  <sql:update var="updateCount">
    INSERT INTO mytable VALUES (2,'Timo Maas')
  </sql:update>
  <sql:update var="updateCount">
    INSERT INTO mytable VALUES (3,'Paul van Dyk')
  </sql:update>

<p>DONE: Inserting three rows into table</p>


  <sql:query var="deejays">
    SELECT * FROM mytable
  </sql:query>

</sql:transaction>

<%-- An example showing how to populate a table --%>
<table border="1">
  <%-- Get the column names for the header of the table --%>
  <c:forEach var="columnName" items="${deejays.columnNames}">
    <th><c:out value="${columnName}"/></th>
  </c:forEach>

  <%-- Get the value of each column while iterating over rows --%>
  <c:forEach var="row" items="${deejays.rows}">
    <tr>
    <c:forEach var="column" items="${row}">
      <td><c:out value="${column.value}"/></td>
    </c:forEach>
  </tr>
  </c:forEach>
</table>


<h2>Deleting second row from table</h2>

  <sql:update var="updateCount" dataSource="<%=dataSource %>">
    DELETE FROM mytable WHERE nameid=2
  </sql:update>

<p>DONE: Deleting second row from table</p>

<sql:query var="deejays" dataSource="<%=dataSource %>">
  SELECT * FROM mytable
</sql:query>


<%-- Yet another example showing how to populate a table --%>
<table border="1">
  <c:forEach var="row" items="${deejays.rows}" varStatus="status">
    <%-- Get the column names for the header of the table --%>
    <c:choose>
      <c:when test="${status.count == 1}">
        <%-- Each row is a Map object key'd by the column name --%>
        <tr>
        <c:forEach var="metaData" items="${row}">
          <th><c:out value="${metaData.key}"/></th>
        </c:forEach>
        </tr>
      </c:when>
    </c:choose>
    <tr>
    <c:forEach var="column" items="${row}">
      <%-- Get the value of each column while iterating over rows --%>
      <td><c:out value="${column.value}"/></td>
    </c:forEach>
  </tr>
  </c:forEach>
</table>


<sql:update var="newTable" dataSource="<%=dataSource %>">
  drop table mytable
</sql:update>


</body>
</html>

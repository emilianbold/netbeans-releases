<%@page import="org.apache.taglibs.standard.examples.util.DataSourceProvider"%>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>

<html>
<head>
  <title>JSTL: SQL action examples</title>
</head>
<body bgcolor="#FFFFFF">

<h1>SQL Transactions</h1>

<%
    DataSourceProvider dataSource = new DataSourceProvider(session);
%>

<p>You can group transactions together using the &lt;sql:transaction&gt; tag.</p>

<h2>Creating table using a transaction</h2>

<sql:transaction dataSource="<%=dataSource%>">
  <sql:update var="newTable">
    create table mytable (
      nameid int primary key,
      name varchar(80)
    )
  </sql:update>
</sql:transaction>

<p>DONE: Creating table using a transaction</p>

<hr>

<h2>Populating table in one transaction</h2>

<sql:transaction dataSource="<%=dataSource%>">
  <sql:update var="updateCount">
    INSERT INTO mytable VALUES (1,'Paul Oakenfold')
  </sql:update>
  <sql:update var="updateCount">
    INSERT INTO mytable VALUES (2,'Timo Maas')
  </sql:update>
  <sql:update var="updateCount">
    INSERT INTO mytable VALUES (3,'Paul van Dyk')
  </sql:update>
</sql:transaction>

<p>DONE: Populating table in one transaction</p>

<sql:update var="newTable" dataSource="<%=dataSource%>">
  drop table mytable
</sql:update>

</body>
</html>

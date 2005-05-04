<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<html>
  <head>
    <title>Hello</title>
  </head>

  <body bgcolor="white">
    <img src="duke.waving.gif"> 
    <h2>Hello, my name is Duke. What's yours?</h2>
    <form method="get" action="HelloServlet">
      <input type="text" name="username" size="25">
      <br/>
      <input type="submit" value="Submit">
      <input type="reset" value="Reset">
    </form>

  </body>
</html>

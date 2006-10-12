<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        
        <% public int fce(int a, int b, <!--CC -->int c)
        
        if ((MyClass.a == MyClass.b) && <!--CC -->(MyClass.c == MyClass.d))
        %>
        
        <h1><!--CC -->JSP Page</h1>
        <jsp:useBean id="myBean" scope="page" class="java.util.Date" />
        <form name="FORM_1"><!--CC -->
            <input type="text" name="imput_1" <!--CC -->value="HELLO WORLD" readonly disabled />
            <input type="radio" name="" value="NO" />
            <input type="submit" value="OK" /><!--CC -->
            <input type="reset" value="RESET" />
            <b></b>
        </form>
        <%--
    This example uses JSTL, uncomment the taglib directive above.
    To test, display the page like this: index.jsp?sayHello=true&name=Murphy
    --%>
        <%--
    <c:if test="${param.sayHello}">
        <!-- Let's welcome the user ${param.name} -->
        Hello ${param.name}!
    </c:if>
    --%>
        <%@ include file="WEB-INF/web.xml"%>
        
        <% 
        <!--CC 43-->
                public int first(int i, String str){
            if (true){<!--CC -->
                    switch(number){
                        case 10:<!--CC -->
                            sayHallo();
                        case 11:
                            a=b;<!--CC -->
                        default:
                            continue;
                    }
            }
        %>    
        
        
    </body>
</html>

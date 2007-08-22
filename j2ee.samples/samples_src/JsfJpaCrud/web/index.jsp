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

    <h1>JSP Page</h1>
    <br/>
    <a href="./faces/purchaseOrder/List.jsp">List of PurchaseOrder</a>
    <br/>
    <a href="./faces/productCode/List.jsp">List of ProductCode</a>
    <br/>
    <a href="./faces/product/List.jsp">List of Product</a>
    <br/>
    <a href="./faces/microMarket/List.jsp">List of MicroMarket</a>
    <br/>
    <a href="./faces/manufacturer/List.jsp">List of Manufacturer</a>
    <br/>
    <a href="./faces/discountCode/List.jsp">List of DiscountCode</a>
    <br/>
    <a href="./faces/customer/List.jsp">List of Customer</a>
    <br/>
    <a href="./faces/welcomeJSF.jsp">JavaServer Faces Welcome Page</a>
    
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
    
    </body>
</html>

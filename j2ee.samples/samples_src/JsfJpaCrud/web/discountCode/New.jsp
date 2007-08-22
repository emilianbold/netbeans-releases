<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>New DiscountCode</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>New discountCode</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="DiscountCode:"/>
                    <h:inputText id="discountCode" value="#{discountCode.discountCode.discountCode}" title="DiscountCode" />
                    <h:outputText value="Rate:"/>
                    <h:inputText id="rate" value="#{discountCode.discountCode.rate}" title="Rate" />
                </h:panelGrid>
                <h:commandLink action="#{discountCode.create}" value="Create"/>
                <br>
                <h:commandLink action="discountCode_list" value="Show All DiscountCode"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

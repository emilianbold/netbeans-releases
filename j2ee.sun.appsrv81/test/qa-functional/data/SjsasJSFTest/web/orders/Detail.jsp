<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Detail of Orders</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Detail of orders</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="OrderNum:"/>
                    <h:outputText value="#{orders.orders.orderNum}" title="OrderNum" />
                    <h:outputText value="Quantity:"/>
                    <h:outputText value="#{orders.orders.quantity}" title="Quantity" />
                    <h:outputText value="ShippingCost:"/>
                    <h:outputText value="#{orders.orders.shippingCost}" title="ShippingCost" />
                    <h:outputText value="SalesDate:"/>
                    <h:outputText value="#{orders.orders.salesDate}" title="SalesDate" />
                    <h:outputText value="ShippingDate:"/>
                    <h:outputText value="#{orders.orders.shippingDate}" title="ShippingDate" />
                    <h:outputText value="FreightCompany:"/>
                    <h:outputText value="#{orders.orders.freightCompany}" title="FreightCompany" />
                    <h:outputText value="CustomerId:"/>
                    <h:outputText value="#{orders.orders.customerId}" title="CustomerId" />
                    <h:outputText value="ProductId:"/>
                    <h:outputText value="#{orders.orders.productId}" title="ProductId" />
                </h:panelGrid>
                <h:commandLink action="#{orders.editSetup}" value="Edit">
                    <f:param name="orderNum" value="#{orders.orders.orderNum}"/>
                </h:commandLink>
                <br>
                <h:commandLink action="orders_list" value="Show All Orders"/>
                <br>
                <a href="/SjsasJSFTest/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

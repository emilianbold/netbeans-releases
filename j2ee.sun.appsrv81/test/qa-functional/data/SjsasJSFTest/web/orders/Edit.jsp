<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Edit Orders</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Edit orders</h1>
            <h:form>
                <h:inputHidden value="#{orders.orders}" immediate="true"/>
                <h:panelGrid columns="2">
                    <h:outputText value="OrderNum:"/>
                    <h:outputText value="#{orders.orders.orderNum}" title="OrderNum" />
                    <h:outputText value="Quantity:"/>
                    <h:inputText id="quantity" value="#{orders.orders.quantity}" title="Quantity" />
                    <h:outputText value="ShippingCost:"/>
                    <h:inputText id="shippingCost" value="#{orders.orders.shippingCost}" title="ShippingCost" />
                    <h:outputText value="SalesDate (MM/dd/yyyy):"/>
                    <h:inputText id="salesDate" value="#{orders.orders.salesDate}" title="SalesDate" >
                        <f:convertDateTime type="DATE" pattern="MM/dd/yyyy" />
                    </h:inputText>
                    <h:outputText value="ShippingDate (MM/dd/yyyy):"/>
                    <h:inputText id="shippingDate" value="#{orders.orders.shippingDate}" title="ShippingDate" >
                        <f:convertDateTime type="DATE" pattern="MM/dd/yyyy" />
                    </h:inputText>
                    <h:outputText value="FreightCompany:"/>
                    <h:inputText id="freightCompany" value="#{orders.orders.freightCompany}" title="FreightCompany" />
                    <h:outputText value="CustomerId:"/>
                    <h:selectOneMenu id="customerId" value="#{orders.orders.customerId}" title="CustomerId">
                        <f:selectItems value="#{orders.customerIds}"/>
                    </h:selectOneMenu>
                    <h:outputText value="ProductId:"/>
                    <h:selectOneMenu id="productId" value="#{orders.orders.productId}" title="ProductId">
                        <f:selectItems value="#{orders.productIds}"/>
                    </h:selectOneMenu>
                </h:panelGrid>
                <h:commandLink action="#{orders.edit}" value="Save"/>
                <br>
                <h:commandLink action="orders_list" value="Show All Orders"/>
                <br>
                <a href="/SjsasJSFTest/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

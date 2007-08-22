<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>New PurchaseOrder</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>New purchaseOrder</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="OrderNum:"/>
                    <h:inputText id="orderNum" value="#{purchaseOrder.purchaseOrder.orderNum}" title="OrderNum" />
                    <h:outputText value="Quantity:"/>
                    <h:inputText id="quantity" value="#{purchaseOrder.purchaseOrder.quantity}" title="Quantity" />
                    <h:outputText value="ShippingCost:"/>
                    <h:inputText id="shippingCost" value="#{purchaseOrder.purchaseOrder.shippingCost}" title="ShippingCost" />
                    <h:outputText value="SalesDate (MM/dd/yyyy):"/>
                    <h:inputText id="salesDate" value="#{purchaseOrder.purchaseOrder.salesDate}" title="SalesDate" >
                        <f:convertDateTime type="DATE" pattern="MM/dd/yyyy" />
                    </h:inputText>
                    <h:outputText value="ShippingDate (MM/dd/yyyy):"/>
                    <h:inputText id="shippingDate" value="#{purchaseOrder.purchaseOrder.shippingDate}" title="ShippingDate" >
                        <f:convertDateTime type="DATE" pattern="MM/dd/yyyy" />
                    </h:inputText>
                    <h:outputText value="FreightCompany:"/>
                    <h:inputText id="freightCompany" value="#{purchaseOrder.purchaseOrder.freightCompany}" title="FreightCompany" />
                    <h:outputText value="CustomerId:" rendered="#{purchaseOrder.purchaseOrder.customerId == null}"/>
                    <h:selectOneMenu id="customerId" value="#{purchaseOrder.purchaseOrder.customerId}" title="CustomerId" rendered="#{purchaseOrder.purchaseOrder.customerId == null}">
                        <f:selectItems value="#{purchaseOrder.customerIds}"/>
                    </h:selectOneMenu>
                    <h:outputText value="ProductId:" rendered="#{purchaseOrder.purchaseOrder.productId == null}"/>
                    <h:selectOneMenu id="productId" value="#{purchaseOrder.purchaseOrder.productId}" title="ProductId" rendered="#{purchaseOrder.purchaseOrder.productId == null}">
                        <f:selectItems value="#{purchaseOrder.productIds}"/>
                    </h:selectOneMenu>
                </h:panelGrid>
                <h:commandLink action="#{purchaseOrder.createFromCustomer}" value="Create" rendered="#{purchaseOrder.purchaseOrder.customerId != null and purchaseOrder.purchaseOrder.productId == null}"/>
                <h:commandLink action="#{purchaseOrder.createFromProduct}" value="Create" rendered="#{purchaseOrder.purchaseOrder.productId != null and purchaseOrder.purchaseOrder.customerId == null}"/>
                <h:commandLink action="#{purchaseOrder.create}" value="Create"/>
                <br>
                <h:commandLink action="purchaseOrder_list" value="Show All PurchaseOrder"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

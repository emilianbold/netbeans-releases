<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Detail of PurchaseOrder</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Detail of purchaseOrder</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="OrderNum:"/>
                    <h:outputText value="#{purchaseOrder.purchaseOrder.orderNum}" title="OrderNum" />
                    <h:outputText value="Quantity:"/>
                    <h:outputText value="#{purchaseOrder.purchaseOrder.quantity}" title="Quantity" />
                    <h:outputText value="ShippingCost:"/>
                    <h:outputText value="#{purchaseOrder.purchaseOrder.shippingCost}" title="ShippingCost" />
                    <h:outputText value="SalesDate:"/>
                    <h:outputText value="#{purchaseOrder.purchaseOrder.salesDate}" title="SalesDate" />
                    <h:outputText value="ShippingDate:"/>
                    <h:outputText value="#{purchaseOrder.purchaseOrder.shippingDate}" title="ShippingDate" />
                    <h:outputText value="FreightCompany:"/>
                    <h:outputText value="#{purchaseOrder.purchaseOrder.freightCompany}" title="FreightCompany" />
                    <h:outputText value="CustomerId:"/>
                    <h:outputText value="#{purchaseOrder.purchaseOrder.customerId}" title="CustomerId" />
                    <h:outputText value="ProductId:"/>
                    <h:outputText value="#{purchaseOrder.purchaseOrder.productId}" title="ProductId" />
                </h:panelGrid>
                <h:commandLink action="purchaseOrder_edit" value="Edit" />
                <br>
                <h:commandLink action="purchaseOrder_list" value="Show All PurchaseOrder"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

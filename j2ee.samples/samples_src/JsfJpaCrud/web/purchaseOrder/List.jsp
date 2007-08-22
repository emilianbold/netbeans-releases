<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>List PurchaseOrder</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Listing PurchaseOrders</h1>
            <h:form>
                <h:commandLink action="#{purchaseOrder.createSetup}" value="New PurchaseOrder"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
                <br>
                <h:outputText value="Item #{purchaseOrder.firstItem + 1}..#{purchaseOrder.lastItem} of #{purchaseOrder.itemCount}"/>&nbsp;
                <h:commandLink action="#{purchaseOrder.prev}" value="Previous #{purchaseOrder.batchSize}" rendered="#{purchaseOrder.firstItem >= purchaseOrder.batchSize}"/>&nbsp;
                <h:commandLink action="#{purchaseOrder.next}" value="Next #{purchaseOrder.batchSize}" rendered="#{purchaseOrder.lastItem + purchaseOrder.batchSize <= purchaseOrder.itemCount}"/>&nbsp;
                <h:commandLink action="#{purchaseOrder.next}" value="Remaining #{purchaseOrder.itemCount - purchaseOrder.lastItem}"
                               rendered="#{purchaseOrder.lastItem < purchaseOrder.itemCount && purchaseOrder.lastItem + purchaseOrder.batchSize > purchaseOrder.itemCount}"/>
                <h:dataTable value='#{purchaseOrder.purchaseOrders}' var='item' border="1" cellpadding="2" cellspacing="0">
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="OrderNum"/>
                        </f:facet>
                        <h:commandLink action="#{purchaseOrder.detailSetup}" value="#{item.orderNum}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Quantity"/>
                        </f:facet>
                        <h:outputText value="#{item.quantity}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="ShippingCost"/>
                        </f:facet>
                        <h:outputText value="#{item.shippingCost}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="SalesDate"/>
                        </f:facet>
                        <h:outputText value="#{item.salesDate}">
                            <f:convertDateTime type="DATE" pattern="MM/dd/yyyy" />
                        </h:outputText>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="ShippingDate"/>
                        </f:facet>
                        <h:outputText value="#{item.shippingDate}">
                            <f:convertDateTime type="DATE" pattern="MM/dd/yyyy" />
                        </h:outputText>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="FreightCompany"/>
                        </f:facet>
                        <h:outputText value="#{item.freightCompany}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="CustomerId"/>
                        </f:facet>
                        <h:commandLink action="#{customer.detailSetup}">
                            <f:param name="customerId" value="#{item.customerId.customerId}"/>
                            <h:outputText value="#{item.customerId.customerId}"/>
                        </h:commandLink>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="ProductId"/>
                        </f:facet>
                        <h:commandLink action="#{product.detailSetup}">
                            <f:param name="productId" value="#{item.productId.productId}"/>
                            <h:outputText value="#{item.productId.productId}"/>
                        </h:commandLink>
                    </h:column>
                    <h:column>
                        <h:commandLink value="Destroy" action="#{purchaseOrder.destroy}">
                            <f:param name="orderNum" value="#{item.orderNum}"/>
                        </h:commandLink>
                        <h:outputText value=" "/>
                        <h:commandLink value="Edit" action="#{purchaseOrder.editSetup}">
                            <f:param name="orderNum" value="#{item.orderNum}"/>
                        </h:commandLink>
                    </h:column>
                </h:dataTable>
            </h:form>
        </f:view>
    </body>
</html>

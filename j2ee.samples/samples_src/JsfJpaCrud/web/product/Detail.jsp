<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Detail of Product</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Detail of product</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="ProductId:"/>
                    <h:outputText value="#{product.product.productId}" title="ProductId" />
                    <h:outputText value="PurchaseCost:"/>
                    <h:outputText value="#{product.product.purchaseCost}" title="PurchaseCost" />
                    <h:outputText value="QuantityOnHand:"/>
                    <h:outputText value="#{product.product.quantityOnHand}" title="QuantityOnHand" />
                    <h:outputText value="Markup:"/>
                    <h:outputText value="#{product.product.markup}" title="Markup" />
                    <h:outputText value="Available:"/>
                    <h:outputText value="#{product.product.available}" title="Available" />
                    <h:outputText value="Description:"/>
                    <h:outputText value="#{product.product.description}" title="Description" />
                    <h:outputText value="ManufacturerId:"/>
                    <h:outputText value="#{product.product.manufacturerId}" title="ManufacturerId" />
                    <h:outputText value="ProductCode:"/>
                    <h:outputText value="#{product.product.productCode}" title="ProductCode" />
                </h:panelGrid>
                <h2>List of PurchaseOrder</h2>
                <h:outputText rendered="#{empty product.product.purchaseOrderCollection}">
                    No PurchaseOrder<br>
                </h:outputText>
                <h:dataTable value="#{product.product.purchaseOrderCollection}" var="item" 
                    border="1" cellpadding="2" cellspacing="0" 
                    rendered="#{not empty product.product.purchaseOrderCollection}">
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="OrderNum"/>
                        </f:facet>
                        <h:commandLink action="#{purchaseOrder.detailSetup}">
                            <f:param name="orderNum" value="#{item.orderNum}"/>
                            <h:outputText value="#{item.orderNum}"/>
                        </h:commandLink>
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
                        <h:commandLink value="Destroy" action="#{purchaseOrder.destroyFromProduct}">
                            <f:param name="orderNum" value="#{item.orderNum}"/>
                        </h:commandLink>
                        <h:outputText value=" "/>
                        <h:commandLink value="Edit" action="#{purchaseOrder.editSetup}">
                            <f:param name="orderNum" value="#{item.orderNum}"/>
                        </h:commandLink>
                    </h:column>
                </h:dataTable>
                <h:commandLink value="New PurchaseOrder" action="#{purchaseOrder.createFromProductSetup}">
                    <f:param name="relatedId" value="#{product.product.productId}"/>
                </h:commandLink>
                <br>
                <br>
                <h:commandLink action="product_edit" value="Edit" />
                <br>
                <h:commandLink action="product_list" value="Show All Product"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

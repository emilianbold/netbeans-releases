<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>New Product</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>New product</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="ProductId:"/>
                    <h:inputText id="productId" value="#{product.product.productId}" title="ProductId" />
                    <h:outputText value="PurchaseCost:"/>
                    <h:inputText id="purchaseCost" value="#{product.product.purchaseCost}" title="PurchaseCost" />
                    <h:outputText value="QuantityOnHand:"/>
                    <h:inputText id="quantityOnHand" value="#{product.product.quantityOnHand}" title="QuantityOnHand" />
                    <h:outputText value="Markup:"/>
                    <h:inputText id="markup" value="#{product.product.markup}" title="Markup" />
                    <h:outputText value="Available:"/>
                    <h:inputText id="available" value="#{product.product.available}" title="Available" />
                    <h:outputText value="Description:"/>
                    <h:inputText id="description" value="#{product.product.description}" title="Description" />
                    <h:outputText value="ManufacturerId:" rendered="#{product.product.manufacturerId == null}"/>
                    <h:selectOneMenu id="manufacturerId" value="#{product.product.manufacturerId}" title="ManufacturerId" rendered="#{product.product.manufacturerId == null}">
                        <f:selectItems value="#{product.manufacturerIds}"/>
                    </h:selectOneMenu>
                    <h:outputText value="ProductCode:" rendered="#{product.product.productCode == null}"/>
                    <h:selectOneMenu id="productCode" value="#{product.product.productCode}" title="ProductCode" rendered="#{product.product.productCode == null}">
                        <f:selectItems value="#{product.productCodes}"/>
                    </h:selectOneMenu>
                </h:panelGrid>
                <h:commandLink action="#{product.createFromManufacturer}" value="Create" rendered="#{product.product.manufacturerId != null and product.product.productCode == null}"/>
                <h:commandLink action="#{product.createFromProductCode}" value="Create" rendered="#{product.product.productCode != null and product.product.manufacturerId == null}"/>
                <h:commandLink action="#{product.create}" value="Create"/>
                <br>
                <h:commandLink action="product_list" value="Show All Product"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

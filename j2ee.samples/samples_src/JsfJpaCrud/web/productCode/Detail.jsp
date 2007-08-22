<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Detail of ProductCode</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Detail of productCode</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="ProdCode:"/>
                    <h:outputText value="#{productCode.productCode.prodCode}" title="ProdCode" />
                    <h:outputText value="DiscountCode:"/>
                    <h:outputText value="#{productCode.productCode.discountCode}" title="DiscountCode" />
                    <h:outputText value="Description:"/>
                    <h:outputText value="#{productCode.productCode.description}" title="Description" />
                </h:panelGrid>
                <h2>List of Product</h2>
                <h:outputText rendered="#{empty productCode.productCode.productCollection}">
                    No Product<br>
                </h:outputText>
                <h:dataTable value="#{productCode.productCode.productCollection}" var="item" 
                    border="1" cellpadding="2" cellspacing="0" 
                    rendered="#{not empty productCode.productCode.productCollection}">
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="ProductId"/>
                        </f:facet>
                        <h:commandLink action="#{product.detailSetup}">
                            <f:param name="productId" value="#{item.productId}"/>
                            <h:outputText value="#{item.productId}"/>
                        </h:commandLink>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="PurchaseCost"/>
                        </f:facet>
                        <h:outputText value="#{item.purchaseCost}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="QuantityOnHand"/>
                        </f:facet>
                        <h:outputText value="#{item.quantityOnHand}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Markup"/>
                        </f:facet>
                        <h:outputText value="#{item.markup}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Available"/>
                        </f:facet>
                        <h:outputText value="#{item.available}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Description"/>
                        </f:facet>
                        <h:outputText value="#{item.description}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="ManufacturerId"/>
                        </f:facet>
                        <h:commandLink action="#{manufacturer.detailSetup}">
                            <f:param name="manufacturerId" value="#{item.manufacturerId.manufacturerId}"/>
                            <h:outputText value="#{item.manufacturerId.manufacturerId}"/>
                        </h:commandLink>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="ProductCode"/>
                        </f:facet>
                        <h:commandLink action="#{productCode.detailSetup}">
                            <f:param name="prodCode" value="#{item.productCode.prodCode}"/>
                            <h:outputText value="#{item.productCode.prodCode}"/>
                        </h:commandLink>
                    </h:column>
                    <h:column>
                        <h:commandLink value="Destroy" action="#{product.destroyFromProductCode}">
                            <f:param name="productId" value="#{item.productId}"/>
                        </h:commandLink>
                        <h:outputText value=" "/>
                        <h:commandLink value="Edit" action="#{product.editSetup}">
                            <f:param name="productId" value="#{item.productId}"/>
                        </h:commandLink>
                    </h:column>
                </h:dataTable>
                <h:commandLink value="New Product" action="#{product.createFromProductCodeSetup}">
                    <f:param name="relatedId" value="#{productCode.productCode.prodCode}"/>
                </h:commandLink>
                <br>
                <br>
                <h:commandLink action="productCode_edit" value="Edit" />
                <br>
                <h:commandLink action="productCode_list" value="Show All ProductCode"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

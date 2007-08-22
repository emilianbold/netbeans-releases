<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>List Product</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Listing Products</h1>
            <h:form>
                <h:commandLink action="#{product.createSetup}" value="New Product"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
                <br>
                <h:outputText value="Item #{product.firstItem + 1}..#{product.lastItem} of #{product.itemCount}"/>&nbsp;
                <h:commandLink action="#{product.prev}" value="Previous #{product.batchSize}" rendered="#{product.firstItem >= product.batchSize}"/>&nbsp;
                <h:commandLink action="#{product.next}" value="Next #{product.batchSize}" rendered="#{product.lastItem + product.batchSize <= product.itemCount}"/>&nbsp;
                <h:commandLink action="#{product.next}" value="Remaining #{product.itemCount - product.lastItem}"
                               rendered="#{product.lastItem < product.itemCount && product.lastItem + product.batchSize > product.itemCount}"/>
                <h:dataTable value='#{product.products}' var='item' border="1" cellpadding="2" cellspacing="0">
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="ProductId"/>
                        </f:facet>
                        <h:commandLink action="#{product.detailSetup}" value="#{item.productId}"/>
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
                        <h:outputText value="#{item.manufacturerId}"/>
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
                        <h:commandLink value="Destroy" action="#{product.destroy}">
                            <f:param name="productId" value="#{item.productId}"/>
                        </h:commandLink>
                        <h:outputText value=" "/>
                        <h:commandLink value="Edit" action="#{product.editSetup}">
                            <f:param name="productId" value="#{item.productId}"/>
                        </h:commandLink>
                    </h:column>
                </h:dataTable>
            </h:form>
        </f:view>
    </body>
</html>

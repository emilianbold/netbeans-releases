<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Detail of Manufacturer</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Detail of manufacturer</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="ManufacturerId:"/>
                    <h:outputText value="#{manufacturer.manufacturer.manufacturerId}" title="ManufacturerId" />
                    <h:outputText value="Name:"/>
                    <h:outputText value="#{manufacturer.manufacturer.name}" title="Name" />
                    <h:outputText value="Addressline1:"/>
                    <h:outputText value="#{manufacturer.manufacturer.addressline1}" title="Addressline1" />
                    <h:outputText value="Addressline2:"/>
                    <h:outputText value="#{manufacturer.manufacturer.addressline2}" title="Addressline2" />
                    <h:outputText value="City:"/>
                    <h:outputText value="#{manufacturer.manufacturer.city}" title="City" />
                    <h:outputText value="State:"/>
                    <h:outputText value="#{manufacturer.manufacturer.state}" title="State" />
                    <h:outputText value="Zip:"/>
                    <h:outputText value="#{manufacturer.manufacturer.zip}" title="Zip" />
                    <h:outputText value="Phone:"/>
                    <h:outputText value="#{manufacturer.manufacturer.phone}" title="Phone" />
                    <h:outputText value="Fax:"/>
                    <h:outputText value="#{manufacturer.manufacturer.fax}" title="Fax" />
                    <h:outputText value="Email:"/>
                    <h:outputText value="#{manufacturer.manufacturer.email}" title="Email" />
                    <h:outputText value="Rep:"/>
                    <h:outputText value="#{manufacturer.manufacturer.rep}" title="Rep" />
                </h:panelGrid>
                <h2>List of Product</h2>
                <h:outputText rendered="#{empty manufacturer.manufacturer.productCollection}">
                    No Product<br>
                </h:outputText>
                <h:dataTable value="#{manufacturer.manufacturer.productCollection}" var="item" 
                    border="1" cellpadding="2" cellspacing="0" 
                    rendered="#{not empty manufacturer.manufacturer.productCollection}">
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
                        <h:commandLink value="Destroy" action="#{product.destroyFromManufacturer}">
                            <f:param name="productId" value="#{item.productId}"/>
                        </h:commandLink>
                        <h:outputText value=" "/>
                        <h:commandLink value="Edit" action="#{product.editSetup}">
                            <f:param name="productId" value="#{item.productId}"/>
                        </h:commandLink>
                    </h:column>
                </h:dataTable>
                <h:commandLink value="New Product" action="#{product.createFromManufacturerSetup}">
                    <f:param name="relatedId" value="#{manufacturer.manufacturer.manufacturerId}"/>
                </h:commandLink>
                <br>
                <br>
                <h:commandLink action="manufacturer_edit" value="Edit" />
                <br>
                <h:commandLink action="manufacturer_list" value="Show All Manufacturer"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

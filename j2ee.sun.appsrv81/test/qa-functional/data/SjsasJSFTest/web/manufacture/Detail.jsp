<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Detail of Manufacture</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Detail of manufacture</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="ManufactureId:"/>
                    <h:outputText value="#{manufacture.manufacture.manufactureId}" title="ManufactureId" />
                    <h:outputText value="Name:"/>
                    <h:outputText value="#{manufacture.manufacture.name}" title="Name" />
                    <h:outputText value="Addressline1:"/>
                    <h:outputText value="#{manufacture.manufacture.addressline1}" title="Addressline1" />
                    <h:outputText value="Addressline2:"/>
                    <h:outputText value="#{manufacture.manufacture.addressline2}" title="Addressline2" />
                    <h:outputText value="City:"/>
                    <h:outputText value="#{manufacture.manufacture.city}" title="City" />
                    <h:outputText value="State:"/>
                    <h:outputText value="#{manufacture.manufacture.state}" title="State" />
                    <h:outputText value="Zip:"/>
                    <h:outputText value="#{manufacture.manufacture.zip}" title="Zip" />
                    <h:outputText value="Phone:"/>
                    <h:outputText value="#{manufacture.manufacture.phone}" title="Phone" />
                    <h:outputText value="Fax:"/>
                    <h:outputText value="#{manufacture.manufacture.fax}" title="Fax" />
                    <h:outputText value="Email:"/>
                    <h:outputText value="#{manufacture.manufacture.email}" title="Email" />
                    <h:outputText value="Rep:"/>
                    <h:outputText value="#{manufacture.manufacture.rep}" title="Rep" />
                </h:panelGrid>
                <h2>List of Product</h2>
                <h:outputText rendered="#{empty manufacture.manufacture.product}">
                    No Product<br>
                </h:outputText>
                <h:dataTable value="#{manufacture.manufacture.product}" var="item" 
                    border="1" cellpadding="2" cellspacing="0" 
                    rendered="#{not empty manufacture.manufacture.product}">
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
                            <h:outputText value="ManufactureId"/>
                        </f:facet>
                        <h:commandLink action="#{manufacture.detailSetup}">
                            <f:param name="manufactureId" value="#{item.manufactureId.manufactureId}"/>
                            <h:outputText value="#{item.manufactureId.manufactureId}"/>
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
                        <h:commandLink value="Destroy" action="#{product.destroyFromManufacture}">
                            <f:param name="productId" value="#{item.productId}"/>
                        </h:commandLink>
                        <h:outputText value=" "/>
                        <h:commandLink value="Edit" action="#{product.editSetup}">
                            <f:param name="productId" value="#{item.productId}"/>
                        </h:commandLink>
                    </h:column>
                </h:dataTable>
                <h:commandLink value="New Product" action="#{product.createFromManufactureSetup}">
                    <f:param name="relatedId" value="#{manufacture.manufacture.manufactureId}"/>
                </h:commandLink>
                <br>
                <br>
                <h:commandLink action="#{manufacture.editSetup}" value="Edit">
                    <f:param name="manufactureId" value="#{manufacture.manufacture.manufactureId}"/>
                </h:commandLink>
                <br>
                <h:commandLink action="manufacture_list" value="Show All Manufacture"/>
                <br>
                <a href="/SjsasJSFTest/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

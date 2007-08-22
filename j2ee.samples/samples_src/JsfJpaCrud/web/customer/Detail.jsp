<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Detail of Customer</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Detail of customer</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="CustomerId:"/>
                    <h:outputText value="#{customer.customer.customerId}" title="CustomerId" />
                    <h:outputText value="Zip:"/>
                    <h:outputText value="#{customer.customer.zip}" title="Zip" />
                    <h:outputText value="Name:"/>
                    <h:outputText value="#{customer.customer.name}" title="Name" />
                    <h:outputText value="Addressline1:"/>
                    <h:outputText value="#{customer.customer.addressline1}" title="Addressline1" />
                    <h:outputText value="Addressline2:"/>
                    <h:outputText value="#{customer.customer.addressline2}" title="Addressline2" />
                    <h:outputText value="City:"/>
                    <h:outputText value="#{customer.customer.city}" title="City" />
                    <h:outputText value="State:"/>
                    <h:outputText value="#{customer.customer.state}" title="State" />
                    <h:outputText value="Phone:"/>
                    <h:outputText value="#{customer.customer.phone}" title="Phone" />
                    <h:outputText value="Fax:"/>
                    <h:outputText value="#{customer.customer.fax}" title="Fax" />
                    <h:outputText value="Email:"/>
                    <h:outputText value="#{customer.customer.email}" title="Email" />
                    <h:outputText value="CreditLimit:"/>
                    <h:outputText value="#{customer.customer.creditLimit}" title="CreditLimit" />
                    <h:outputText value="DiscountCode:"/>
                    <h:outputText value="#{customer.customer.discountCode}" title="DiscountCode" />
                </h:panelGrid>
                <h2>List of PurchaseOrder</h2>
                <h:outputText rendered="#{empty customer.customer.purchaseOrderCollection}">
                    No PurchaseOrder<br>
                </h:outputText>
                <h:dataTable value="#{customer.customer.purchaseOrderCollection}" var="item" 
                    border="1" cellpadding="2" cellspacing="0" 
                    rendered="#{not empty customer.customer.purchaseOrderCollection}">
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
                        <h:commandLink value="Destroy" action="#{purchaseOrder.destroyFromCustomer}">
                            <f:param name="orderNum" value="#{item.orderNum}"/>
                        </h:commandLink>
                        <h:outputText value=" "/>
                        <h:commandLink value="Edit" action="#{purchaseOrder.editSetup}">
                            <f:param name="orderNum" value="#{item.orderNum}"/>
                        </h:commandLink>
                    </h:column>
                </h:dataTable>
                <h:commandLink value="New PurchaseOrder" action="#{purchaseOrder.createFromCustomerSetup}">
                    <f:param name="relatedId" value="#{customer.customer.customerId}"/>
                </h:commandLink>
                <br><br>
                <h:commandLink action="customer_edit" value="Edit" />
                <br>
                <h:commandLink action="customer_list" value="Show All Customer"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

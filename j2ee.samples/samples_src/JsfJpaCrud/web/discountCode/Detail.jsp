<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Detail of DiscountCode</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Detail of discountCode</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="DiscountCode:"/>
                    <h:outputText value="#{discountCode.discountCode.discountCode}" title="DiscountCode" />
                    <h:outputText value="Rate:"/>
                    <h:outputText value="#{discountCode.discountCode.rate}" title="Rate" />
                </h:panelGrid>
                <h2>List of Customer</h2>
                <h:outputText rendered="#{empty discountCode.discountCode.customerCollection}">
                    No Customer<br>
                </h:outputText>
                <h:dataTable value="#{discountCode.discountCode.customerCollection}" var="item" 
                    border="1" cellpadding="2" cellspacing="0" 
                    rendered="#{not empty discountCode.discountCode.customerCollection}">
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="CustomerId"/>
                        </f:facet>
                        <h:commandLink action="#{customer.detailSetup}">
                            <f:param name="customerId" value="#{item.customerId}"/>
                            <h:outputText value="#{item.customerId}"/>
                        </h:commandLink>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Zip"/>
                        </f:facet>
                        <h:outputText value="#{item.zip}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Name"/>
                        </f:facet>
                        <h:outputText value="#{item.name}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Addressline1"/>
                        </f:facet>
                        <h:outputText value="#{item.addressline1}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Addressline2"/>
                        </f:facet>
                        <h:outputText value="#{item.addressline2}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="City"/>
                        </f:facet>
                        <h:outputText value="#{item.city}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="State"/>
                        </f:facet>
                        <h:outputText value="#{item.state}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Phone"/>
                        </f:facet>
                        <h:outputText value="#{item.phone}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Fax"/>
                        </f:facet>
                        <h:outputText value="#{item.fax}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Email"/>
                        </f:facet>
                        <h:outputText value="#{item.email}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="CreditLimit"/>
                        </f:facet>
                        <h:outputText value="#{item.creditLimit}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="DiscountCode"/>
                        </f:facet>
                        <h:commandLink action="#{discountCode.detailSetup}">
                            <f:param name="discountCode" value="#{item.discountCode.discountCode}"/>
                            <h:outputText value="#{item.discountCode.discountCode}"/>
                        </h:commandLink>
                    </h:column>
                    <h:column>
                        <h:commandLink value="Destroy" action="#{customer.destroyFromDiscountCode}">
                            <f:param name="customerId" value="#{item.customerId}"/>
                        </h:commandLink>
                        <h:outputText value=" "/>
                        <h:commandLink value="Edit" action="#{customer.editSetup}">
                            <f:param name="customerId" value="#{item.customerId}"/>
                        </h:commandLink>
                    </h:column>
                </h:dataTable>
                <h:commandLink value="New Customer" action="#{customer.createFromDiscountCodeSetup}">
                    <f:param name="relatedId" value="#{discountCode.discountCode.discountCode}"/>
                </h:commandLink>
                <br>
                <br>
                <h:commandLink action="discountCode_edit" value="Edit" />
                <br>
                <h:commandLink action="discountCode_list" value="Show All DiscountCode"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

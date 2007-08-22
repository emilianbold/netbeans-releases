<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>List Customer</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Listing Customers</h1>
            <h:form>
                <h:commandLink action="#{customer.createSetup}" value="New Customer"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
                <br>
                <h:outputText value="Item #{customer.firstItem + 1}..#{customer.lastItem} of #{customer.itemCount}"/>&nbsp;
                <h:commandLink action="#{customer.prev}" value="Previous #{customer.batchSize}" rendered="#{customer.firstItem >= customer.batchSize}"/>&nbsp;
                <h:commandLink action="#{customer.next}" value="Next #{customer.batchSize}" rendered="#{customer.lastItem + customer.batchSize <= customer.itemCount}"/>&nbsp;
                <h:commandLink action="#{customer.next}" value="Remaining #{customer.itemCount - customer.lastItem}"
                               rendered="#{customer.lastItem < customer.itemCount && customer.lastItem + customer.batchSize > customer.itemCount}"/>
                <h:dataTable value='#{customer.customers}' var='item' border="1" cellpadding="2" cellspacing="0">
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="CustomerId"/>
                        </f:facet>
                        <h:commandLink action="#{customer.detailSetup}" value="#{item.customerId}"/>
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
                            <f:param name="discountCodeId" value="#{item.discountCode.discountCode}"/>
                            <h:outputText value="#{item.discountCode.discountCode}"/>
                        </h:commandLink>
                    </h:column>
                    <h:column>
                        <h:commandLink value="Destroy" action="#{customer.destroy}">
                            <f:param name="customerId" value="#{item.customerId}"/>
                        </h:commandLink>
                        <h:outputText value=" "/>
                        <h:commandLink value="Edit" action="#{customer.editSetup}">
                            <f:param name="customerId" value="#{item.customerId}"/>
                        </h:commandLink>
                    </h:column>
                </h:dataTable>
            </h:form>
        </f:view>
    </body>
</html>

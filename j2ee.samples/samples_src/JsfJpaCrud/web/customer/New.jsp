<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>New Customer</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>New customer</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="CustomerId:"/>
                    <h:inputText id="customerId" value="#{customer.customer.customerId}" title="CustomerId" />
                    <h:outputText value="Zip:"/>
                    <h:inputText id="zip" value="#{customer.customer.zip}" title="Zip" />
                    <h:outputText value="Name:"/>
                    <h:inputText id="name" value="#{customer.customer.name}" title="Name" />
                    <h:outputText value="Addressline1:"/>
                    <h:inputText id="addressline1" value="#{customer.customer.addressline1}" title="Addressline1" />
                    <h:outputText value="Addressline2:"/>
                    <h:inputText id="addressline2" value="#{customer.customer.addressline2}" title="Addressline2" />
                    <h:outputText value="City:"/>
                    <h:inputText id="city" value="#{customer.customer.city}" title="City" />
                    <h:outputText value="State:"/>
                    <h:inputText id="state" value="#{customer.customer.state}" title="State" />
                    <h:outputText value="Phone:"/>
                    <h:inputText id="phone" value="#{customer.customer.phone}" title="Phone" />
                    <h:outputText value="Fax:"/>
                    <h:inputText id="fax" value="#{customer.customer.fax}" title="Fax" />
                    <h:outputText value="Email:"/>
                    <h:inputText id="email" value="#{customer.customer.email}" title="Email" />
                    <h:outputText value="CreditLimit:"/>
                    <h:inputText id="creditLimit" value="#{customer.customer.creditLimit}" title="CreditLimit" />
                    <h:outputText value="DiscountCode:" rendered="#{customer.customer.discountCode == null}"/>
                    <h:selectOneMenu id="discountCode" value="#{customer.customer.discountCode}" title="DiscountCode" rendered="#{customer.customer.discountCode == null}">
                        <f:selectItems value="#{customer.discountCodes}"/>
                    </h:selectOneMenu>
                </h:panelGrid>
                <h:commandLink action="#{customer.createFromDiscountCode}" value="Create" rendered="#{customer.customer.discountCode != null}"/>
                <h:commandLink action="#{customer.create}" value="Create" rendered="#{customer.customer.discountCode == null}"/>
                <br>
                <h:commandLink action="customer_list" value="Show All Customer"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

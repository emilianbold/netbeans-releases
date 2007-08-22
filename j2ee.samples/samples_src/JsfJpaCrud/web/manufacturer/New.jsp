<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>New Manufacturer</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>New manufacturer</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="ManufacturerId:"/>
                    <h:inputText id="manufacturerId" value="#{manufacturer.manufacturer.manufacturerId}" title="ManufacturerId" />
                    <h:outputText value="Name:"/>
                    <h:inputText id="name" value="#{manufacturer.manufacturer.name}" title="Name" />
                    <h:outputText value="Addressline1:"/>
                    <h:inputText id="addressline1" value="#{manufacturer.manufacturer.addressline1}" title="Addressline1" />
                    <h:outputText value="Addressline2:"/>
                    <h:inputText id="addressline2" value="#{manufacturer.manufacturer.addressline2}" title="Addressline2" />
                    <h:outputText value="City:"/>
                    <h:inputText id="city" value="#{manufacturer.manufacturer.city}" title="City" />
                    <h:outputText value="State:"/>
                    <h:inputText id="state" value="#{manufacturer.manufacturer.state}" title="State" />
                    <h:outputText value="Zip:"/>
                    <h:inputText id="zip" value="#{manufacturer.manufacturer.zip}" title="Zip" />
                    <h:outputText value="Phone:"/>
                    <h:inputText id="phone" value="#{manufacturer.manufacturer.phone}" title="Phone" />
                    <h:outputText value="Fax:"/>
                    <h:inputText id="fax" value="#{manufacturer.manufacturer.fax}" title="Fax" />
                    <h:outputText value="Email:"/>
                    <h:inputText id="email" value="#{manufacturer.manufacturer.email}" title="Email" />
                    <h:outputText value="Rep:"/>
                    <h:inputText id="rep" value="#{manufacturer.manufacturer.rep}" title="Rep" />
                </h:panelGrid>
                <h:commandLink action="#{manufacturer.create}" value="Create"/>
                <br>
                <h:commandLink action="manufacturer_list" value="Show All Manufacturer"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

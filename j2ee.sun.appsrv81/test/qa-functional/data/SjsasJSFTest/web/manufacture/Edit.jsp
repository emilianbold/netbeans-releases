<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Edit Manufacture</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Edit manufacture</h1>
            <h:form>
                <h:inputHidden value="#{manufacture.manufacture}" immediate="true"/>
                <h:panelGrid columns="2">
                    <h:outputText value="ManufactureId:"/>
                    <h:outputText value="#{manufacture.manufacture.manufactureId}" title="ManufactureId" />
                    <h:outputText value="Name:"/>
                    <h:inputText id="name" value="#{manufacture.manufacture.name}" title="Name" />
                    <h:outputText value="Addressline1:"/>
                    <h:inputText id="addressline1" value="#{manufacture.manufacture.addressline1}" title="Addressline1" />
                    <h:outputText value="Addressline2:"/>
                    <h:inputText id="addressline2" value="#{manufacture.manufacture.addressline2}" title="Addressline2" />
                    <h:outputText value="City:"/>
                    <h:inputText id="city" value="#{manufacture.manufacture.city}" title="City" />
                    <h:outputText value="State:"/>
                    <h:inputText id="state" value="#{manufacture.manufacture.state}" title="State" />
                    <h:outputText value="Zip:"/>
                    <h:inputText id="zip" value="#{manufacture.manufacture.zip}" title="Zip" />
                    <h:outputText value="Phone:"/>
                    <h:inputText id="phone" value="#{manufacture.manufacture.phone}" title="Phone" />
                    <h:outputText value="Fax:"/>
                    <h:inputText id="fax" value="#{manufacture.manufacture.fax}" title="Fax" />
                    <h:outputText value="Email:"/>
                    <h:inputText id="email" value="#{manufacture.manufacture.email}" title="Email" />
                    <h:outputText value="Rep:"/>
                    <h:inputText id="rep" value="#{manufacture.manufacture.rep}" title="Rep" />
                </h:panelGrid>
                <h:commandLink action="#{manufacture.edit}" value="Save"/>
                <br>
                <h:commandLink action="manufacture_list" value="Show All Manufacture"/>
                <br>
                <a href="/SjsasJSFTest/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

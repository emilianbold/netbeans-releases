<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Edit MicroMarket</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Edit microMarket</h1>
            <h:form>
                <h:inputHidden value="#{microMarket.microMarket}" immediate="true"/>
                <h:panelGrid columns="2">
                    <h:outputText value="ZipCode:"/>
                    <h:outputText value="#{microMarket.microMarket.zipCode}" title="ZipCode" />
                    <h:outputText value="Radius:"/>
                    <h:inputText id="radius" value="#{microMarket.microMarket.radius}" title="Radius" />
                    <h:outputText value="AreaLength:"/>
                    <h:inputText id="areaLength" value="#{microMarket.microMarket.areaLength}" title="AreaLength" />
                    <h:outputText value="AreaWidth:"/>
                    <h:inputText id="areaWidth" value="#{microMarket.microMarket.areaWidth}" title="AreaWidth" />
                </h:panelGrid>
                <h:commandLink action="#{microMarket.edit}" value="Save"/>
                <br>
                <h:commandLink action="microMarket_list" value="Show All MicroMarket"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

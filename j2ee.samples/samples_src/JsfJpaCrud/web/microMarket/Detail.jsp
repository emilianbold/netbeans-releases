<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Detail of MicroMarket</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Detail of microMarket</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="ZipCode:"/>
                    <h:outputText value="#{microMarket.microMarket.zipCode}" title="ZipCode" />
                    <h:outputText value="Radius:"/>
                    <h:outputText value="#{microMarket.microMarket.radius}" title="Radius" />
                    <h:outputText value="AreaLength:"/>
                    <h:outputText value="#{microMarket.microMarket.areaLength}" title="AreaLength" />
                    <h:outputText value="AreaWidth:"/>
                    <h:outputText value="#{microMarket.microMarket.areaWidth}" title="AreaWidth" />
                </h:panelGrid>
                <h:commandLink action="microMarket_edit" value="Edit" />
                <br>
                <h:commandLink action="microMarket_list" value="Show All MicroMarket"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>

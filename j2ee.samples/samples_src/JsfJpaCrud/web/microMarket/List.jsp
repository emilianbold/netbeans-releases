<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>List MicroMarket</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Listing MicroMarkets</h1>
            <h:form>
                <h:commandLink action="#{microMarket.createSetup}" value="New MicroMarket"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
                <br>
                <h:outputText value="Item #{microMarket.firstItem + 1}..#{microMarket.lastItem} of #{microMarket.itemCount}"/>&nbsp;
                <h:commandLink action="#{microMarket.prev}" value="Previous #{microMarket.batchSize}" rendered="#{microMarket.firstItem >= microMarket.batchSize}"/>&nbsp;
                <h:commandLink action="#{microMarket.next}" value="Next #{microMarket.batchSize}" rendered="#{microMarket.lastItem + microMarket.batchSize <= microMarket.itemCount}"/>&nbsp;
                <h:commandLink action="#{microMarket.next}" value="Remaining #{microMarket.itemCount - microMarket.lastItem}"
                               rendered="#{microMarket.lastItem < microMarket.itemCount && microMarket.lastItem + microMarket.batchSize > microMarket.itemCount}"/>
                <h:dataTable value='#{microMarket.microMarkets}' var='item' border="1" cellpadding="2" cellspacing="0">
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="ZipCode"/>
                        </f:facet>
                        <h:commandLink action="#{microMarket.detailSetup}" value="#{item.zipCode}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Radius"/>
                        </f:facet>
                        <h:outputText value="#{item.radius}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="AreaLength"/>
                        </f:facet>
                        <h:outputText value="#{item.areaLength}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="AreaWidth"/>
                        </f:facet>
                        <h:outputText value="#{item.areaWidth}"/>
                    </h:column>
                    <h:column>
                        <h:commandLink value="Destroy" action="#{microMarket.destroy}">
                            <f:param name="zipCode" value="#{item.zipCode}"/>
                        </h:commandLink>
                        <h:outputText value=" "/>
                        <h:commandLink value="Edit" action="#{microMarket.editSetup}">
                            <f:param name="zipCode" value="#{item.zipCode}"/>
                        </h:commandLink>
                    </h:column>
                </h:dataTable>
            </h:form>
        </f:view>
    </body>
</html>

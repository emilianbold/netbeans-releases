<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>List Manufacturer</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Listing Manufacturers</h1>
            <h:form>
                <h:commandLink action="#{manufacturer.createSetup}" value="New Manufacturer"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
                <br>
                <h:outputText value="Item #{manufacturer.firstItem + 1}..#{manufacturer.lastItem} of #{manufacturer.itemCount}"/>&nbsp;
                <h:commandLink action="#{manufacturer.prev}" value="Previous #{manufacturer.batchSize}" rendered="#{manufacturer.firstItem >= manufacturer.batchSize}"/>&nbsp;
                <h:commandLink action="#{manufacturer.next}" value="Next #{manufacturer.batchSize}" rendered="#{manufacturer.lastItem + manufacturer.batchSize <= manufacturer.itemCount}"/>&nbsp;
                <h:commandLink action="#{manufacturer.next}" value="Remaining #{manufacturer.itemCount - manufacturer.lastItem}"
                               rendered="#{manufacturer.lastItem < manufacturer.itemCount && manufacturer.lastItem + manufacturer.batchSize > manufacturer.itemCount}"/>
                <h:dataTable value='#{manufacturer.manufacturers}' var='item' border="1" cellpadding="2" cellspacing="0">
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="ManufacturerId"/>
                        </f:facet>
                        <h:commandLink action="#{manufacturer.detailSetup}" value="#{item.manufacturerId}"/>
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
                            <h:outputText value="Zip"/>
                        </f:facet>
                        <h:outputText value="#{item.zip}"/>
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
                            <h:outputText value="Rep"/>
                        </f:facet>
                        <h:outputText value="#{item.rep}"/>
                    </h:column>
                    <h:column>
                        <h:commandLink value="Destroy" action="#{manufacturer.destroy}">
                            <f:param name="manufacturerId" value="#{item.manufacturerId}"/>
                        </h:commandLink>
                        <h:outputText value=" "/>
                        <h:commandLink value="Edit" action="#{manufacturer.editSetup}">
                            <f:param name="manufacturerId" value="#{item.manufacturerId}"/>
                        </h:commandLink>
                    </h:column>
                </h:dataTable>
            </h:form>
        </f:view>
    </body>
</html>

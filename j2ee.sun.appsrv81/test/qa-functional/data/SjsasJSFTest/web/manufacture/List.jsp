<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>List Manufacture</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Listing Manufactures</h1>
            <h:form>
                <h:commandLink action="#{manufacture.createSetup}" value="New Manufacture"/>
                <br>
                <a href="/SjsasJSFTest/index.jsp">Back to index</a>
                <br>
                <h:outputText value="Item #{manufacture.firstItem + 1}..#{manufacture.lastItem} of #{manufacture.itemCount}"/>&nbsp;
                <h:commandLink action="#{manufacture.prev}" value="Previous #{manufacture.batchSize}" rendered="#{manufacture.firstItem >= manufacture.batchSize}"/>&nbsp;
                <h:commandLink action="#{manufacture.next}" value="Next #{manufacture.batchSize}" rendered="#{manufacture.lastItem + manufacture.batchSize <= manufacture.itemCount}"/>&nbsp;
                <h:commandLink action="#{manufacture.next}" value="Remaining #{manufacture.itemCount - manufacture.lastItem}"
                rendered="#{manufacture.lastItem < manufacture.itemCount && manufacture.lastItem + manufacture.batchSize > manufacture.itemCount}"/><h:dataTable value='#{manufacture.manufactures}' var='item' border="1" cellpadding="2" cellspacing="0">
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="ManufactureId"/>
                        </f:facet>
                        <h:commandLink action="#{manufacture.detailSetup}">
                            <f:param name="manufactureId" value="#{item.manufactureId}"/>
                            <h:outputText value="#{item.manufactureId}"/>
                        </h:commandLink>
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
                        <h:commandLink value="Destroy" action="#{manufacture.destroy}">
                            <f:param name="manufactureId" value="#{item.manufactureId}"/>
                        </h:commandLink>
                        <h:outputText value=" "/>
                        <h:commandLink value="Edit" action="#{manufacture.editSetup}">
                            <f:param name="manufactureId" value="#{item.manufactureId}"/>
                        </h:commandLink>
                    </h:column>
                </h:dataTable>
            </h:form>
        </f:view>
    </body>
</html>

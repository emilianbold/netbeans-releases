<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>List DiscountCode</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Listing DiscountCodes</h1>
            <h:form>
                <h:commandLink action="#{discountCode.createSetup}" value="New DiscountCode"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
                <br>
                <h:outputText value="Item #{discountCode.firstItem + 1}..#{discountCode.lastItem} of #{discountCode.itemCount}"/>&nbsp;
                <h:commandLink action="#{discountCode.prev}" value="Previous #{discountCode.batchSize}" rendered="#{discountCode.firstItem >= discountCode.batchSize}"/>&nbsp;
                <h:commandLink action="#{discountCode.next}" value="Next #{discountCode.batchSize}" rendered="#{discountCode.lastItem + discountCode.batchSize <= discountCode.itemCount}"/>&nbsp;
                <h:commandLink action="#{discountCode.next}" value="Remaining #{discountCode.itemCount - discountCode.lastItem}"
                               rendered="#{discountCode.lastItem < discountCode.itemCount && discountCode.lastItem + discountCode.batchSize > discountCode.itemCount}"/>
                <h:dataTable value='#{discountCode.discountCodes}' var='item' border="1" cellpadding="2" cellspacing="0">
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="DiscountCode"/>
                        </f:facet>
                        <h:commandLink action="#{discountCode.detailSetup}" value="#{item.discountCode}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Rate"/>
                        </f:facet>
                        <h:outputText value="#{item.rate}"/>
                    </h:column>
                    <h:column>
                        <h:commandLink value="Destroy" action="#{discountCode.destroy}">
                            <f:param name="discountCodeId" value="#{item.discountCode}"/>
                        </h:commandLink>
                        <h:outputText value=" "/>
                        <h:commandLink value="Edit" action="#{discountCode.editSetup}">
                            <f:param name="discountCodeId" value="#{item.discountCode}"/>
                        </h:commandLink>
                    </h:column>
                </h:dataTable>
            </h:form>
        </f:view>
    </body>
</html>

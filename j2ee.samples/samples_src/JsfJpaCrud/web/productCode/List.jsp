<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>List ProductCode</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Listing ProductCodes</h1>
            <h:form>
                <h:commandLink action="#{productCode.createSetup}" value="New ProductCode"/>
                <br>
                <a href="/JsfJpaCrud/index.jsp">Back to index</a>
                <br>
                <h:outputText value="Item #{productCode.firstItem + 1}..#{productCode.lastItem} of #{productCode.itemCount}"/>&nbsp;
                <h:commandLink action="#{productCode.prev}" value="Previous #{productCode.batchSize}" rendered="#{productCode.firstItem >= productCode.batchSize}"/>&nbsp;
                <h:commandLink action="#{productCode.next}" value="Next #{productCode.batchSize}" rendered="#{productCode.lastItem + productCode.batchSize <= productCode.itemCount}"/>&nbsp;
                <h:commandLink action="#{productCode.next}" value="Remaining #{productCode.itemCount - productCode.lastItem}"
                               rendered="#{productCode.lastItem < productCode.itemCount && productCode.lastItem + productCode.batchSize > productCode.itemCount}"/>
                <h:dataTable value='#{productCode.productCodes}' var='item' border="1" cellpadding="2" cellspacing="0">
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="ProdCode"/>
                        </f:facet>
                        <h:commandLink action="#{productCode.detailSetup}" value="#{item.prodCode}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="DiscountCode"/>
                        </f:facet>
                        <h:outputText value="#{item.discountCode}"/>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="Description"/>
                        </f:facet>
                        <h:outputText value="#{item.description}"/>
                    </h:column>
                    <h:column>
                        <h:commandLink value="Destroy" action="#{productCode.destroy}">
                            <f:param name="prodCode" value="#{item.prodCode}"/>
                        </h:commandLink>
                        <h:outputText value=" "/>
                        <h:commandLink value="Edit" action="#{productCode.editSetup}">
                            <f:param name="prodCode" value="#{item.prodCode}"/>
                        </h:commandLink>
                    </h:column>
                </h:dataTable>
            </h:form>
        </f:view>
    </body>
</html>

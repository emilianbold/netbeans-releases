<%--
	/*
	 * Copyright (c) 2008, Sun Microsystems, Inc. All rights reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions are met:
	 * 
	 * * Redistributions of source code must retain the above copyright notice,
	 *   this list of conditions and the following disclaimer.
	 * 
	 * * Redistributions in binary form must reproduce the above copyright notice,
	 *   this list of conditions and the following disclaimer in the documentation
	 *   and/or other materials provided with the distribution.
	 *
	 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
	 *   may be used to endorse or promote products derived from this software without
	 *   specific prior written permission.
	 * 
	 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
	 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
	 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
	 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
	 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
	 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
	 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
	 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
	 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
	 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
	 * THE POSSIBILITY OF SUCH DAMAGE.
	 */
--%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<f:view>
    <html>
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
            <title>Listing MicroMarket Items</title>
            <link rel="stylesheet" type="text/css" href="/JsfJpaCrud/faces/jsfcrud.css" />
        </head>
        <body>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Listing MicroMarket Items</h1>
            <h:form styleClass="jsfcrud_list_form">
                <h:outputText escape="false" value="(No MicroMarket Items Found)<br />" rendered="#{microMarket.pagingInfo.itemCount == 0}" />
                <h:panelGroup rendered="#{microMarket.pagingInfo.itemCount > 0}">
                    <h:outputText value="Item #{microMarket.pagingInfo.firstItem + 1}..#{microMarket.pagingInfo.lastItem} of #{microMarket.pagingInfo.itemCount}"/>&nbsp;
                    <h:commandLink action="#{microMarket.prev}" value="Previous #{microMarket.pagingInfo.batchSize}" rendered="#{microMarket.pagingInfo.firstItem >= microMarket.pagingInfo.batchSize}"/>&nbsp;
                    <h:commandLink action="#{microMarket.next}" value="Next #{microMarket.pagingInfo.batchSize}" rendered="#{microMarket.pagingInfo.lastItem + microMarket.pagingInfo.batchSize <= microMarket.pagingInfo.itemCount}"/>&nbsp;
                    <h:commandLink action="#{microMarket.next}" value="Remaining #{microMarket.pagingInfo.itemCount - microMarket.pagingInfo.lastItem}"
                                   rendered="#{microMarket.pagingInfo.lastItem < microMarket.pagingInfo.itemCount && microMarket.pagingInfo.lastItem + microMarket.pagingInfo.batchSize > microMarket.pagingInfo.itemCount}"/>
                    <h:dataTable value="#{microMarket.microMarketItems}" var="item" border="0" cellpadding="2" cellspacing="0" rowClasses="jsfcrud_odd_row,jsfcrud_even_row" rules="all" style="border:solid 1px">
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="ZipCode"/>
                            </f:facet>
                            <h:outputText value=" #{item.zipCode}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="Radius"/>
                            </f:facet>
                            <h:outputText value=" #{item.radius}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="AreaLength"/>
                            </f:facet>
                            <h:outputText value=" #{item.areaLength}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="AreaWidth"/>
                            </f:facet>
                            <h:outputText value=" #{item.areaWidth}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText escape="false" value="&nbsp;"/>
                            </f:facet>
                            <h:commandLink value="Show" action="#{microMarket.detailSetup}">
                                <f:param name="jsfcrud.currentMicroMarket" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][item][microMarket.converter].jsfcrud_invoke}"/>
                            </h:commandLink>
                            <h:outputText value=" "/>
                            <h:commandLink value="Edit" action="#{microMarket.editSetup}">
                                <f:param name="jsfcrud.currentMicroMarket" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][item][microMarket.converter].jsfcrud_invoke}"/>
                            </h:commandLink>
                            <h:outputText value=" "/>
                            <h:commandLink value="Destroy" action="#{microMarket.destroy}">
                                <f:param name="jsfcrud.currentMicroMarket" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][item][microMarket.converter].jsfcrud_invoke}"/>
                            </h:commandLink>
                        </h:column>
                    </h:dataTable>
                </h:panelGroup>
                <br />
                <h:commandLink action="#{microMarket.createSetup}" value="New MicroMarket"/>
                <br />
                <h:commandLink value="Index" action="welcome" immediate="true" />
                
            </h:form>
        </body>
    </html>
</f:view>

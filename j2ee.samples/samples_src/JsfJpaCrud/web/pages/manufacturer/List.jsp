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
            <title>Listing Manufacturer Items</title>
            <link rel="stylesheet" type="text/css" href="/JsfJpaCrud/faces/jsfcrud.css" />
        </head>
        <body>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Listing Manufacturer Items</h1>
            <h:form styleClass="jsfcrud_list_form">
                <h:outputText escape="false" value="(No Manufacturer Items Found)<br />" rendered="#{manufacturer.pagingInfo.itemCount == 0}" />
                <h:panelGroup rendered="#{manufacturer.pagingInfo.itemCount > 0}">
                    <h:outputText value="Item #{manufacturer.pagingInfo.firstItem + 1}..#{manufacturer.pagingInfo.lastItem} of #{manufacturer.pagingInfo.itemCount}"/>&nbsp;
                    <h:commandLink action="#{manufacturer.prev}" value="Previous #{manufacturer.pagingInfo.batchSize}" rendered="#{manufacturer.pagingInfo.firstItem >= manufacturer.pagingInfo.batchSize}"/>&nbsp;
                    <h:commandLink action="#{manufacturer.next}" value="Next #{manufacturer.pagingInfo.batchSize}" rendered="#{manufacturer.pagingInfo.lastItem + manufacturer.pagingInfo.batchSize <= manufacturer.pagingInfo.itemCount}"/>&nbsp;
                    <h:commandLink action="#{manufacturer.next}" value="Remaining #{manufacturer.pagingInfo.itemCount - manufacturer.pagingInfo.lastItem}"
                                   rendered="#{manufacturer.pagingInfo.lastItem < manufacturer.pagingInfo.itemCount && manufacturer.pagingInfo.lastItem + manufacturer.pagingInfo.batchSize > manufacturer.pagingInfo.itemCount}"/>
                    <h:dataTable value="#{manufacturer.manufacturerItems}" var="item" border="0" cellpadding="2" cellspacing="0" rowClasses="jsfcrud_odd_row,jsfcrud_even_row" rules="all" style="border:solid 1px">
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="ManufacturerId"/>
                            </f:facet>
                            <h:outputText value=" #{item.manufacturerId}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="Name"/>
                            </f:facet>
                            <h:outputText value=" #{item.name}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="Addressline1"/>
                            </f:facet>
                            <h:outputText value=" #{item.addressline1}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="Addressline2"/>
                            </f:facet>
                            <h:outputText value=" #{item.addressline2}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="City"/>
                            </f:facet>
                            <h:outputText value=" #{item.city}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="State"/>
                            </f:facet>
                            <h:outputText value=" #{item.state}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="Zip"/>
                            </f:facet>
                            <h:outputText value=" #{item.zip}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="Phone"/>
                            </f:facet>
                            <h:outputText value=" #{item.phone}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="Fax"/>
                            </f:facet>
                            <h:outputText value=" #{item.fax}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="Email"/>
                            </f:facet>
                            <h:outputText value=" #{item.email}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="Rep"/>
                            </f:facet>
                            <h:outputText value=" #{item.rep}"/>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText escape="false" value="&nbsp;"/>
                            </f:facet>
                            <h:commandLink value="Show" action="#{manufacturer.detailSetup}">
                                <f:param name="jsfcrud.currentManufacturer" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][item][manufacturer.converter].jsfcrud_invoke}"/>
                            </h:commandLink>
                            <h:outputText value=" "/>
                            <h:commandLink value="Edit" action="#{manufacturer.editSetup}">
                                <f:param name="jsfcrud.currentManufacturer" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][item][manufacturer.converter].jsfcrud_invoke}"/>
                            </h:commandLink>
                            <h:outputText value=" "/>
                            <h:commandLink value="Destroy" action="#{manufacturer.destroy}">
                                <f:param name="jsfcrud.currentManufacturer" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][item][manufacturer.converter].jsfcrud_invoke}"/>
                            </h:commandLink>
                        </h:column>
                    </h:dataTable>
                </h:panelGroup>
                <br />
                <h:commandLink action="#{manufacturer.createSetup}" value="New Manufacturer"/>
                <br />
                <h:commandLink value="Index" action="welcome" immediate="true" />
                
            </h:form>
        </body>
    </html>
</f:view>

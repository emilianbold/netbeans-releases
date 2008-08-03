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
            <title>ProductCode Detail</title>
            <link rel="stylesheet" type="text/css" href="/JsfJpaCrud/faces/jsfcrud.css" />
        </head>
        <body>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>ProductCode Detail</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="ProdCode:"/>
                    <h:outputText value="#{productCode.productCode.prodCode}" title="ProdCode" />
                    <h:outputText value="DiscountCode:"/>
                    <h:outputText value="#{productCode.productCode.discountCode}" title="DiscountCode" />
                    <h:outputText value="Description:"/>
                    <h:outputText value="#{productCode.productCode.description}" title="Description" />
                    <h:outputText value="ProductCollection:" />
                    <h:panelGroup>
                        <h:outputText rendered="#{empty productCode.productCode.productCollection}" value="(No Items)"/>
                        <h:dataTable value="#{productCode.productCode.productCollection}" var="item" 
                                     border="0" cellpadding="2" cellspacing="0" rowClasses="jsfcrud_odd_row,jsfcrud_even_row" rules="all" style="border:solid 1px" 
                                     rendered="#{not empty productCode.productCode.productCollection}">
                            <h:column>
                                <f:facet name="header">
                                    <h:outputText value="ProductId"/>
                                </f:facet>
                                <h:outputText value=" #{item.productId}"/>
                            </h:column>
                            <h:column>
                                <f:facet name="header">
                                    <h:outputText value="PurchaseCost"/>
                                </f:facet>
                                <h:outputText value=" #{item.purchaseCost}"/>
                            </h:column>
                            <h:column>
                                <f:facet name="header">
                                    <h:outputText value="QuantityOnHand"/>
                                </f:facet>
                                <h:outputText value=" #{item.quantityOnHand}"/>
                            </h:column>
                            <h:column>
                                <f:facet name="header">
                                    <h:outputText value="Markup"/>
                                </f:facet>
                                <h:outputText value=" #{item.markup}"/>
                            </h:column>
                            <h:column>
                                <f:facet name="header">
                                    <h:outputText value="Available"/>
                                </f:facet>
                                <h:outputText value=" #{item.available}"/>
                            </h:column>
                            <h:column>
                                <f:facet name="header">
                                    <h:outputText value="Description"/>
                                </f:facet>
                                <h:outputText value=" #{item.description}"/>
                            </h:column>
                            <h:column>
                                <f:facet name="header">
                                    <h:outputText value="ManufacturerId"/>
                                </f:facet>
                                <h:outputText value=" #{item.manufacturerId}"/>
                            </h:column>
                            <h:column>
                                <f:facet name="header">
                                    <h:outputText value="ProductCode"/>
                                </f:facet>
                                <h:outputText value=" #{item.productCode}"/>
                            </h:column>
                            <h:column>
                                <f:facet name="header">
                                    <h:outputText escape="false" value="&nbsp;"/>
                                </f:facet>
                                <h:commandLink value="Show" action="#{product.detailSetup}">
                                    <f:param name="jsfcrud.currentProductCode" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][productCode.productCode][productCode.converter].jsfcrud_invoke}"/>
                                    <f:param name="jsfcrud.currentProduct" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][item][product.converter].jsfcrud_invoke}"/>
                                    <f:param name="jsfcrud.relatedController" value="productCode" />
                                    <f:param name="jsfcrud.relatedControllerType" value="jsf.ProductCodeController" />
                                </h:commandLink>
                                <h:outputText value=" "/>
                                <h:commandLink value="Edit" action="#{product.editSetup}">
                                    <f:param name="jsfcrud.currentProductCode" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][productCode.productCode][productCode.converter].jsfcrud_invoke}"/>
                                    <f:param name="jsfcrud.currentProduct" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][item][product.converter].jsfcrud_invoke}"/>
                                    <f:param name="jsfcrud.relatedController" value="productCode" />
                                    <f:param name="jsfcrud.relatedControllerType" value="jsf.ProductCodeController" />
                                </h:commandLink>
                                <h:outputText value=" "/>
                                <h:commandLink value="Destroy" action="#{product.destroy}">
                                    <f:param name="jsfcrud.currentProductCode" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][productCode.productCode][productCode.converter].jsfcrud_invoke}"/>
                                    <f:param name="jsfcrud.currentProduct" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][item][product.converter].jsfcrud_invoke}"/>
                                    <f:param name="jsfcrud.relatedController" value="productCode" />
                                    <f:param name="jsfcrud.relatedControllerType" value="jsf.ProductCodeController" />
                                </h:commandLink>
                            </h:column>
                        </h:dataTable>
                    </h:panelGroup>
                </h:panelGrid>
                <br />
                <h:commandLink action="#{productCode.destroy}" value="Destroy">
                    <f:param name="jsfcrud.currentProductCode" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][productCode.productCode][productCode.converter].jsfcrud_invoke}" />
                </h:commandLink>
                <br />
                <br />
                <h:commandLink action="#{productCode.editSetup}" value="Edit">
                    <f:param name="jsfcrud.currentProductCode" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][productCode.productCode][productCode.converter].jsfcrud_invoke}" />
                </h:commandLink>
                <br />
                <h:commandLink action="#{productCode.createSetup}" value="New ProductCode" />
                <br />
                <h:commandLink action="#{productCode.listSetup}" value="Show All ProductCode Items"/>
                <br />
                <h:commandLink value="Index" action="welcome" immediate="true" />
            </h:form>
        </body>
    </html>
</f:view>

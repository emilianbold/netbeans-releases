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
            <title>PurchaseOrder Detail</title>
            <link rel="stylesheet" type="text/css" href="/JsfJpaCrud/faces/jsfcrud.css" />
        </head>
        <body>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>PurchaseOrder Detail</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="OrderNum:"/>
                    <h:outputText value="#{purchaseOrder.purchaseOrder.orderNum}" title="OrderNum" />
                    <h:outputText value="Quantity:"/>
                    <h:outputText value="#{purchaseOrder.purchaseOrder.quantity}" title="Quantity" />
                    <h:outputText value="ShippingCost:"/>
                    <h:outputText value="#{purchaseOrder.purchaseOrder.shippingCost}" title="ShippingCost" />
                    <h:outputText value="SalesDate:"/>
                    <h:outputText value="#{purchaseOrder.purchaseOrder.salesDate}" title="SalesDate" >
                        <f:convertDateTime type="DATE" pattern="MM/dd/yyyy" />
                    </h:outputText>
                    <h:outputText value="ShippingDate:"/>
                    <h:outputText value="#{purchaseOrder.purchaseOrder.shippingDate}" title="ShippingDate" >
                        <f:convertDateTime type="DATE" pattern="MM/dd/yyyy" />
                    </h:outputText>
                    <h:outputText value="FreightCompany:"/>
                    <h:outputText value="#{purchaseOrder.purchaseOrder.freightCompany}" title="FreightCompany" />
                    <h:outputText value="CustomerId:"/>
                    <h:panelGroup>
                        <h:outputText value=" #{purchaseOrder.purchaseOrder.customerId}"/>
                        <h:panelGroup rendered="#{purchaseOrder.purchaseOrder.customerId != null}">
                            <h:outputText value=" ("/>
                            <h:commandLink value="Show" action="#{customer.detailSetup}">
                                <f:param name="jsfcrud.currentPurchaseOrder" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][purchaseOrder.purchaseOrder][purchaseOrder.converter].jsfcrud_invoke}"/>
                                <f:param name="jsfcrud.currentCustomer" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][purchaseOrder.purchaseOrder.customerId][customer.converter].jsfcrud_invoke}"/>
                                <f:param name="jsfcrud.relatedController" value="purchaseOrder"/>
                                <f:param name="jsfcrud.relatedControllerType" value="jsf.PurchaseOrderController"/>
                            </h:commandLink>
                            <h:outputText value=" "/>
                            <h:commandLink value="Edit" action="#{customer.editSetup}">
                                <f:param name="jsfcrud.currentPurchaseOrder" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][purchaseOrder.purchaseOrder][purchaseOrder.converter].jsfcrud_invoke}"/>
                                <f:param name="jsfcrud.currentCustomer" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][purchaseOrder.purchaseOrder.customerId][customer.converter].jsfcrud_invoke}"/>
                                <f:param name="jsfcrud.relatedController" value="purchaseOrder"/>
                                <f:param name="jsfcrud.relatedControllerType" value="jsf.PurchaseOrderController"/>
                            </h:commandLink>
                            <h:outputText value=" "/>
                            <h:commandLink value="Destroy" action="#{customer.destroy}">
                                <f:param name="jsfcrud.currentPurchaseOrder" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][purchaseOrder.purchaseOrder][purchaseOrder.converter].jsfcrud_invoke}"/>
                                <f:param name="jsfcrud.currentCustomer" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][purchaseOrder.purchaseOrder.customerId][customer.converter].jsfcrud_invoke}"/>
                                <f:param name="jsfcrud.relatedController" value="purchaseOrder"/>
                                <f:param name="jsfcrud.relatedControllerType" value="jsf.PurchaseOrderController"/>
                            </h:commandLink>
                            <h:outputText value=" )"/>
                        </h:panelGroup>
                    </h:panelGroup>
                    <h:outputText value="ProductId:"/>
                    <h:panelGroup>
                        <h:outputText value=" #{purchaseOrder.purchaseOrder.productId}"/>
                        <h:panelGroup rendered="#{purchaseOrder.purchaseOrder.productId != null}">
                            <h:outputText value=" ("/>
                            <h:commandLink value="Show" action="#{product.detailSetup}">
                                <f:param name="jsfcrud.currentPurchaseOrder" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][purchaseOrder.purchaseOrder][purchaseOrder.converter].jsfcrud_invoke}"/>
                                <f:param name="jsfcrud.currentProduct" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][purchaseOrder.purchaseOrder.productId][product.converter].jsfcrud_invoke}"/>
                                <f:param name="jsfcrud.relatedController" value="purchaseOrder"/>
                                <f:param name="jsfcrud.relatedControllerType" value="jsf.PurchaseOrderController"/>
                            </h:commandLink>
                            <h:outputText value=" "/>
                            <h:commandLink value="Edit" action="#{product.editSetup}">
                                <f:param name="jsfcrud.currentPurchaseOrder" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][purchaseOrder.purchaseOrder][purchaseOrder.converter].jsfcrud_invoke}"/>
                                <f:param name="jsfcrud.currentProduct" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][purchaseOrder.purchaseOrder.productId][product.converter].jsfcrud_invoke}"/>
                                <f:param name="jsfcrud.relatedController" value="purchaseOrder"/>
                                <f:param name="jsfcrud.relatedControllerType" value="jsf.PurchaseOrderController"/>
                            </h:commandLink>
                            <h:outputText value=" "/>
                            <h:commandLink value="Destroy" action="#{product.destroy}">
                                <f:param name="jsfcrud.currentPurchaseOrder" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][purchaseOrder.purchaseOrder][purchaseOrder.converter].jsfcrud_invoke}"/>
                                <f:param name="jsfcrud.currentProduct" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][purchaseOrder.purchaseOrder.productId][product.converter].jsfcrud_invoke}"/>
                                <f:param name="jsfcrud.relatedController" value="purchaseOrder"/>
                                <f:param name="jsfcrud.relatedControllerType" value="jsf.PurchaseOrderController"/>
                            </h:commandLink>
                            <h:outputText value=" )"/>
                        </h:panelGroup>
                    </h:panelGroup>
                </h:panelGrid>
                <br />
                <h:commandLink action="#{purchaseOrder.destroy}" value="Destroy">
                    <f:param name="jsfcrud.currentPurchaseOrder" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][purchaseOrder.purchaseOrder][purchaseOrder.converter].jsfcrud_invoke}" />
                </h:commandLink>
                <br />
                <br />
                <h:commandLink action="#{purchaseOrder.editSetup}" value="Edit">
                    <f:param name="jsfcrud.currentPurchaseOrder" value="#{jsfcrud_class['jsf.util.JsfUtil'].jsfcrud_method['getAsConvertedString'][purchaseOrder.purchaseOrder][purchaseOrder.converter].jsfcrud_invoke}" />
                </h:commandLink>
                <br />
                <h:commandLink action="#{purchaseOrder.createSetup}" value="New PurchaseOrder" />
                <br />
                <h:commandLink action="#{purchaseOrder.listSetup}" value="Show All PurchaseOrder Items"/>
                <br />
                <h:commandLink value="Index" action="welcome" immediate="true" />
            </h:form>
        </body>
    </html>
</f:view>

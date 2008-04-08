<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
    may be used to endorse or promote products derived from this software without
    specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
  THE POSSIBILITY OF SUCH DAMAGE.
-->
<jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <webuijsf:page id="page1">
            <webuijsf:html id="html1">
                <webuijsf:head id="head1">
                    <webuijsf:link id="link1" url="/resources/stylesheet.css"/>
                </webuijsf:head>
                <webuijsf:body id="body1" style="-rave-layout: grid">
                    <webuijsf:form binding="#{Page1.form1}" id="form1" virtualFormsConfig="vcrControlsVForm | | mainPanel:vcrControls:next mainPanel:vcrControls:last mainPanel:vcrControls:prev mainPanel:vcrControls:first , crdVForm | | mainPanel:crudControls:delete mainPanel:crudControls:create , saveVForm | mainPanel:tripForm:toCity mainPanel:tripForm:fromCity mainPanel:tripForm:tripType mainPanel:tripForm:depDateCalendar | mainPanel:crudControls:save , personVForm | mainPanel:personForm:personId | mainPanel:personForm:personId , resetVForm | | mainPanel:crudControls:cancel">
                        <div style="position: absolute; left: 0px; top: 0px">
                            <jsp:directive.include file="Masthead.jspf"/>
                        </div>
                        <h:panelGrid id="mainPanel" style="margin: 5px; padding: 5px; height: 100%; left: 0px; top: 160px; position: absolute; width: 760px">
                            <h:panelGrid columns="4" id="personForm" style="">
                                <webuijsf:label id="person" text="Label"/>
                                <webuijsf:dropDown binding="#{Page1.personId}" converter="#{Page1.personIdConverter}" id="personId"
                                    items="#{Page1.personDataProvider.options['PERSON.PERSONID,PERSON.NAME']}"
                                    onChange="webui.suntheme.common.timeoutSubmitForm(this.form, 'mainPanel:personForm:personId');" valueChangeListenerExpression="#{Page1.personId_processValueChange}"/>
                                <webuijsf:label id="title1" text="Label"/>
                                <webuijsf:staticText id="title"/>
                            </h:panelGrid>
                            <h:panelGrid columns="4" id="vcrControls" style="">
                                <webuijsf:button actionExpression="#{Page1.first_action}" binding="#{Page1.first}" id="first"
                                    imageURL="/resources/pagination_first.gif" text="Button"/>
                                <webuijsf:button actionExpression="#{Page1.prev_action}" binding="#{Page1.prev}" id="prev"
                                    imageURL="/resources/pagination_prev.gif" text="Button"/>
                                <webuijsf:button actionExpression="#{Page1.next_action}" binding="#{Page1.next}" id="next"
                                    imageURL="/resources/pagination_next.gif" text="Button"/>
                                <webuijsf:button actionExpression="#{Page1.last_action}" binding="#{Page1.last}" id="last"
                                    imageURL="/resources/pagination_last.gif" text="Button"/>
                            </h:panelGrid>
                            <h:panelGrid columns="3" id="tripForm" style="width: 100%">
                                <webuijsf:label id="label1" text="Dep Date"/>
                                <webuijsf:calendar binding="#{Page1.depDateCalendar}" id="depDateCalendar"/>
                                <webuijsf:message for="depDateCalendar" id="message1" showDetail="false" showSummary="true"/>
                                <webuijsf:label id="label2" text="Dep City"/>
                                <webuijsf:textField id="fromCity" text="#{Page1.tripDataProvider.value['TRIP.DEPCITY']}" validatorExpression="#{Page1.fromCity_validate}"/>
                                <webuijsf:message for="fromCity" id="message2" showDetail="false" showSummary="true"/>
                                <webuijsf:label id="label3" text="Dest City"/>
                                <webuijsf:textField id="toCity" text="#{Page1.tripDataProvider.value['TRIP.DESTCITY']}" validatorExpression="#{Page1.toCity_validate}"/>
                                <webuijsf:message for="toCity" id="message3" showDetail="false" showSummary="true"/>
                                <webuijsf:label id="label4" text="Trip Type"/>
                                <webuijsf:dropDown binding="#{Page1.tripType}" converter="#{Page1.tripTypeConverter}" id="tripType" items="#{Page1.triptypeDataProvider.options['TRIPTYPE.TRIPTYPEID,TRIPTYPE.NAME']}"/>
                            </h:panelGrid>
                            <h:panelGrid columns="4" id="crudControls" style="">
                                <webuijsf:button actionExpression="#{Page1.create_action}" binding="#{Page1.create}" id="create" text="Create"/>
                                <webuijsf:button actionExpression="#{Page1.save_action}" binding="#{Page1.save}" id="save" text="Save"/>
                                <webuijsf:button actionExpression="#{Page1.cancel_action}" binding="#{Page1.cancel}" id="cancel" text="Cancel Create"/>
                                <webuijsf:button actionExpression="#{Page1.delete_action}" binding="#{Page1.delete}" id="delete" text="Delete"/>
                            </h:panelGrid>
                            <webuijsf:messageGroup id="messageGroup1"/>
                        </h:panelGrid>
                    </webuijsf:form>
                </webuijsf:body>
            </webuijsf:html>
        </webuijsf:page>
    </f:view>
</jsp:root>

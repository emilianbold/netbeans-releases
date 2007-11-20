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
        <webuijsf:page binding="#{UpdatePage.page1}" id="page1">
            <webuijsf:html binding="#{UpdatePage.html1}" id="html1">
                <webuijsf:head binding="#{UpdatePage.head1}" id="head1">
                    <webuijsf:link binding="#{UpdatePage.link1}" id="link1" url="/resources/stylesheet.css"/>
                </webuijsf:head>
                <webuijsf:body binding="#{UpdatePage.body1}" id="body1" style="-rave-layout: grid">
                    <webuijsf:form binding="#{UpdatePage.form1}" id="form1" virtualFormsConfig="cancelForm | | mainPanel:buttonPanel:cancel">
                        <div style="position: absolute; left: 0px; top: 0px">
                            <jsp:directive.include file="Masthead.jspf"/>
                        </div>
                        <h:panelGrid binding="#{UpdatePage.mainPanel}" id="mainPanel" style="margin: 5px; padding: 5px; height: 100%; left: 0px; top: 160px; position: absolute; width: 760px">
                            <h:panelGrid binding="#{UpdatePage.tripPanel}" columns="2" id="tripPanel" style="">
                                <webuijsf:label binding="#{UpdatePage.label1}" id="label1" text="Date"/>
                                <webuijsf:calendar binding="#{UpdatePage.dateCalendar}" id="dateCalendar" maxDate="#{SessionBean1.maxDate}" minDate="#{SessionBean1.minDate}"/>
                                <webuijsf:label binding="#{UpdatePage.label2}" id="label2" text="From"/>
                                <webuijsf:textField binding="#{UpdatePage.fromCity}" id="fromCity" text="#{UpdatePage.tripDataProvider.value['TRIP.DEPCITY']}" validatorExpression="#{UpdatePage.fromCity_validate}"/>
                                <webuijsf:label binding="#{UpdatePage.label3}" id="label3" text="To"/>
                                <webuijsf:textField binding="#{UpdatePage.toCity}" id="toCity" text="#{UpdatePage.tripDataProvider.value['TRIP.DESTCITY']}" validatorExpression="#{UpdatePage.toCity_validate}"/>
                                <webuijsf:label binding="#{UpdatePage.label4}" id="label4" text="Trip Type"/>
                                <webuijsf:dropDown binding="#{UpdatePage.tripType}" converter="#{CreatePage.tripTypeConverter}" id="tripType"
                                    items="#{CreatePage.triptypeDataProvider.options['TRIPTYPE.TRIPTYPEID,TRIPTYPE.NAME']}"
                                    onChange="webui.suntheme.common.timeoutSubmitForm(this.form, 'mainPanel:tripPanel:tripType');" valueChangeListenerExpression="#{UpdatePage.tripType_processValueChange}"/>
                            </h:panelGrid>
                            <h:panelGrid binding="#{UpdatePage.buttonPanel}" columns="2" id="buttonPanel" style="">
                                <webuijsf:button actionExpression="#{UpdatePage.updateButton_action}" binding="#{UpdatePage.updateButton}" id="updateButton" text="Update"/>
                                <webuijsf:button actionExpression="#{UpdatePage.cancel_action}" binding="#{UpdatePage.cancel}" id="cancel" text="Cancel"/>
                            </h:panelGrid>
                            <webuijsf:messageGroup binding="#{UpdatePage.messageGroup}" id="messageGroup"/>
                        </h:panelGrid>
                    </webuijsf:form>
                </webuijsf:body>
            </webuijsf:html>
        </webuijsf:page>
    </f:view>
</jsp:root>

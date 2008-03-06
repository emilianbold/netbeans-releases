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
                <webuijsf:body id="body1" style="background-color: #657882; -rave-layout: grid">
                    <webuijsf:form id="form1">
                        <div style="left: 0px; top: 0px; position: absolute">
                            <jsp:directive.include file="Header.jspf"/>
                        </div>
                        <h:panelGrid columns="3" id="mainPanel" style="padding: 5px; height: 100%; margin-top: 20px; left: 0px; top: 200px; position: absolute; width: 830px">
                            <webuijsf:label for="personId" id="label1" text="Employee:"/>
                            <webuijsf:dropDown binding="#{Page1.personId}" converter="#{Page1.personIdConverter}" id="personId"
                                items="#{Page1.personDataProvider.options['PERSON.PERSONID,PERSON.NAME']}" onChange="webui.suntheme.common.timeoutSubmitForm(this.form, 'mainPanel:personId');"/>
                            <webuijsf:message for="personId" id="message1" showDetail="false" showSummary="true"/>
                            <webuijsf:staticText id="staticText1"/>
                            <webuijsf:table augmentTitle="false" id="trips" title="Trips" width="420">
                                <webuijsf:tableRowGroup id="tableRowGroup1" rows="10" sourceData="#{Page1.tripDataProvider}" sourceVar="currentRow">
                                    <webuijsf:tableColumn headerText="Trip ID" id="tableColumn1" sort="TRIP.TRIPID">
                                        <webuijsf:button actionExpression="#{Page1.button1_action}" binding="#{Page1.button1}" id="button1" text="#{currentRow.value['TRIP.TRIPID']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Type" id="tableColumn2" sort="TRIPTYPE.NAME">
                                        <webuijsf:staticText id="staticText3" text="#{currentRow.value['TRIPTYPE.NAME']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Date" id="tableColumn3" sort="TRIP.DEPDATE">
                                        <webuijsf:staticText id="staticText4" text="#{currentRow.value['TRIP.DEPDATE']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Departure" id="tableColumn4" sort="TRIP.DEPCITY">
                                        <webuijsf:staticText id="staticText5" text="#{currentRow.value['TRIP.DEPCITY']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Destination" id="tableColumn5" sort="TRIP.DESTCITY">
                                        <webuijsf:staticText id="staticText6" text="#{currentRow.value['TRIP.DESTCITY']}"/>
                                    </webuijsf:tableColumn>
                                </webuijsf:tableRowGroup>
                            </webuijsf:table>
                            <webuijsf:messageGroup id="messageGroup1"/>
                        </h:panelGrid>
                    </webuijsf:form>
                </webuijsf:body>
            </webuijsf:html>
        </webuijsf:page>
    </f:view>
</jsp:root>

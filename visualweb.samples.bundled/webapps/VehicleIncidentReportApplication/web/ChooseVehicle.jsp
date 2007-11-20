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
        <webuijsf:page binding="#{ChooseVehicle.page1}" id="page1">
            <webuijsf:html binding="#{ChooseVehicle.html1}" id="html1">
                <webuijsf:head binding="#{ChooseVehicle.head1}" id="head1">
                    <webuijsf:link binding="#{ChooseVehicle.link1}" id="link1" url="/resources/stylesheet.css"/>
                </webuijsf:head>
                <webuijsf:body binding="#{ChooseVehicle.body1}" id="body1" style="-rave-layout: grid">
                    <webuijsf:form binding="#{ChooseVehicle.form1}" id="form1">
                        <div style="position: absolute; left: 0px; top: 0px">
                            <jsp:directive.include file="Header.jspf"/>
                        </div>
                        <h:panelGrid binding="#{ChooseVehicle.content}" id="content" style="left: 0px; top: 240px; position: absolute; width: 800px" styleClass="contents">
                            <h:panelGrid binding="#{ChooseVehicle.contentGrid}" id="contentGrid">
                                <h:panelGrid binding="#{ChooseVehicle.messagePanel}" id="messagePanel" style="width: 100%">
                                    <webuijsf:messageGroup binding="#{ChooseVehicle.messageGroup1}" id="messageGroup1"/>
                                </h:panelGrid>
                                <h:panelGrid binding="#{ChooseVehicle.paddingPanel}" id="paddingPanel" style="height: 20px; width: 100%"/>
                                <h:panelGrid binding="#{ChooseVehicle.dataGrid}" id="dataGrid" style="">
                                    <webuijsf:table augmentTitle="false" binding="#{ChooseVehicle.table1}" id="table1" paginateButton="true"
                                        paginationControls="true" title="Vehicles" width="720">
                                        <webuijsf:tableRowGroup binding="#{ChooseVehicle.tableRowGroup1}" emptyDataMsg="No vehicles found." id="tableRowGroup1"
                                            rows="4" sourceData="#{ChooseVehicle.vehicleDataProvider}" sourceVar="currentRow">
                                            <webuijsf:tableColumn binding="#{ChooseVehicle.tableColumn1}" headerText="State" id="tableColumn1" sort="STATE.STATENAME">
                                                <webuijsf:staticText binding="#{ChooseVehicle.staticText1}" id="staticText1" text="#{currentRow.value['STATE.STATENAME']}"/>
                                            </webuijsf:tableColumn>
                                            <webuijsf:tableColumn binding="#{ChooseVehicle.tableColumn2}" headerText="License Plate" id="tableColumn2" sort="VEHICLE.LICENSEPLATE">
                                                <webuijsf:staticText binding="#{ChooseVehicle.staticText2}" id="staticText2" text="#{currentRow.value['VEHICLE.LICENSEPLATE']}"/>
                                            </webuijsf:tableColumn>
                                            <webuijsf:tableColumn binding="#{ChooseVehicle.tableColumn3}" headerText="Make" id="tableColumn3" sort="VEHICLE.MAKE">
                                                <webuijsf:staticText binding="#{ChooseVehicle.staticText3}" id="staticText3" text="#{currentRow.value['VEHICLE.MAKE']}"/>
                                            </webuijsf:tableColumn>
                                            <webuijsf:tableColumn binding="#{ChooseVehicle.tableColumn4}" headerText="Model" id="tableColumn4" sort="VEHICLE.MODEL">
                                                <webuijsf:staticText binding="#{ChooseVehicle.staticText4}" id="staticText4" text="#{currentRow.value['VEHICLE.MODEL']}"/>
                                            </webuijsf:tableColumn>
                                            <webuijsf:tableColumn binding="#{ChooseVehicle.tableColumn5}" headerText="Color" id="tableColumn5" sort="VEHICLE.COLOR">
                                                <webuijsf:staticText binding="#{ChooseVehicle.staticText5}" id="staticText5" text="#{currentRow.value['VEHICLE.COLOR']}"/>
                                            </webuijsf:tableColumn>
                                            <webuijsf:tableColumn binding="#{ChooseVehicle.tableColumn6}" id="tableColumn6">
                                                <webuijsf:button actionExpression="#{ChooseVehicle.report_action}" binding="#{ChooseVehicle.report}" id="report" text="Report"/>
                                            </webuijsf:tableColumn>
                                        </webuijsf:tableRowGroup>
                                    </webuijsf:table>
                                </h:panelGrid>
                            </h:panelGrid>
                        </h:panelGrid>
                        <div style="position: absolute; left: 0px; top: 600px">
                            <jsp:directive.include file="Footer.jspf"/>
                        </div>
                    </webuijsf:form>
                </webuijsf:body>
            </webuijsf:html>
        </webuijsf:page>
    </f:view>
</jsp:root>

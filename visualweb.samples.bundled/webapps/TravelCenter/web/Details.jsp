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
        <webuijsf:page binding="#{Details.page1}" id="page1">
            <webuijsf:html binding="#{Details.html1}" id="html1">
                <webuijsf:head binding="#{Details.head1}" id="head1">
                    <webuijsf:link binding="#{Details.link1}" id="link1" url="/resources/stylesheet.css"/>
                </webuijsf:head>
                <webuijsf:body binding="#{Details.body1}" id="body1" style="background-color: #657881; -rave-layout: grid">
                    <webuijsf:form binding="#{Details.form1}" id="form1">
                        <div style="position: absolute; left: 0px; top: 0px">
                            <jsp:directive.include file="Header.jspf"/>
                        </div>
                        <h:panelGrid binding="#{Details.mainPanel}" columns="2" id="mainPanel" style="padding: 5px; height: 100%; margin-top: 20px; left: 0px; top: 200px; position: absolute; width: 830px">
                            <webuijsf:staticText binding="#{Details.staticText1}" id="staticText1"/>
                            <webuijsf:button actionExpression="#{Details.backButton_action}" binding="#{Details.backButton}" id="backButton" text="Back to Trips"/>
                            <webuijsf:label binding="#{Details.label1}" id="label1" text="Employee:"/>
                            <webuijsf:staticText binding="#{Details.personsName}" id="personsName" text="#{Details.personDataProvider1.value['PERSON.NAME']}"/>
                            <webuijsf:staticText binding="#{Details.staticText2}" id="staticText2"/>
                            <webuijsf:table augmentTitle="false" binding="#{Details.flights}" id="flights" title="Flight Information" width="924">
                                <webuijsf:tableRowGroup binding="#{Details.tableRowGroup1}" id="tableRowGroup1" rows="10"
                                    sourceData="#{Details.flightDataProvider}" sourceVar="currentRow">
                                    <webuijsf:tableColumn binding="#{Details.tableColumn15}" headerText="Airline" id="tableColumn15" sort="FLIGHT.AIRLINENAME">
                                        <webuijsf:staticText binding="#{Details.staticText19}" id="staticText19" text="#{currentRow.value['FLIGHT.AIRLINENAME']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn10}" headerText="Flight" id="tableColumn10" sort="FLIGHT.FLIGHTNUM">
                                        <webuijsf:staticText binding="#{Details.staticText14}" id="staticText14" text="#{currentRow.value['FLIGHT.FLIGHTNUM']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn11}" headerText="Departs" id="tableColumn11" sort="FLIGHT.DEPTIME">
                                        <webuijsf:staticText binding="#{Details.staticText15}" id="staticText15" text="#{currentRow.value['FLIGHT.DEPTIME']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn12}" headerText="From" id="tableColumn12" sort="FLIGHT.DEPAIRPORT">
                                        <webuijsf:staticText binding="#{Details.staticText16}" id="staticText16" text="#{currentRow.value['FLIGHT.DEPAIRPORT']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn13}" headerText="Arrives" id="tableColumn13" sort="FLIGHT.ARRTIME">
                                        <webuijsf:staticText binding="#{Details.staticText17}" id="staticText17" text="#{currentRow.value['FLIGHT.ARRTIME']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn14}" headerText="At" id="tableColumn14" sort="FLIGHT.ARRAIRPORT">
                                        <webuijsf:staticText binding="#{Details.staticText18}" id="staticText18" text="#{currentRow.value['FLIGHT.ARRAIRPORT']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn3}" headerText="Direction" id="tableColumn3" sort="FLIGHT.DIRECTION">
                                        <webuijsf:staticText binding="#{Details.staticText7}" id="staticText7" text="#{currentRow.value['FLIGHT.DIRECTION']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn16}" headerText="Status" id="tableColumn16" sort="FLIGHT.BOOKINGSTATUS">
                                        <webuijsf:staticText binding="#{Details.staticText20}" id="staticText20" text="#{currentRow.value['FLIGHT.BOOKINGSTATUS']}"/>
                                    </webuijsf:tableColumn>
                                </webuijsf:tableRowGroup>
                            </webuijsf:table>
                            <webuijsf:staticText binding="#{Details.staticText3}" id="staticText3"/>
                            <webuijsf:table augmentTitle="false" binding="#{Details.autoRental}" id="autoRental" title="Auto Rental Information" width="801">
                                <webuijsf:tableRowGroup binding="#{Details.tableRowGroup2}" id="tableRowGroup2" rows="10"
                                    sourceData="#{Details.carrentalDataProvider}" sourceVar="currentRow">
                                    <webuijsf:tableColumn binding="#{Details.tableColumn6}" headerText="Company" id="tableColumn6" sort="CARRENTAL.PROVIDER">
                                        <webuijsf:staticText binding="#{Details.staticText10}" id="staticText10" text="#{currentRow.value['CARRENTAL.PROVIDER']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn21}" headerText="Type" id="tableColumn21" sort="CARRENTAL.CARTYPE">
                                        <webuijsf:staticText binding="#{Details.staticText25}" id="staticText25" text="#{currentRow.value['CARRENTAL.CARTYPE']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn18}" headerText="City" id="tableColumn18" sort="CARRENTAL.CITY">
                                        <webuijsf:staticText binding="#{Details.staticText22}" id="staticText22" text="#{currentRow.value['CARRENTAL.CITY']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn19}" headerText="Pickup" id="tableColumn19" sort="CARRENTAL.PICKUPDATE">
                                        <webuijsf:staticText binding="#{Details.staticText23}" id="staticText23" text="#{currentRow.value['CARRENTAL.PICKUPDATE']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn20}" headerText="Return" id="tableColumn20" sort="CARRENTAL.RETURNDATE">
                                        <webuijsf:staticText binding="#{Details.staticText24}" id="staticText24" text="#{currentRow.value['CARRENTAL.RETURNDATE']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn22}" headerText="Rate" id="tableColumn22" sort="CARRENTAL.RATE">
                                        <webuijsf:staticText binding="#{Details.staticText26}" id="staticText26" text="#{currentRow.value['CARRENTAL.RATE']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn23}" headerText="Status" id="tableColumn23" sort="CARRENTAL.BOOKINGSTATUS">
                                        <webuijsf:staticText binding="#{Details.staticText27}" id="staticText27" text="#{currentRow.value['CARRENTAL.BOOKINGSTATUS']}"/>
                                    </webuijsf:tableColumn>
                                </webuijsf:tableRowGroup>
                            </webuijsf:table>
                            <webuijsf:staticText binding="#{Details.staticText4}" id="staticText4"/>
                            <webuijsf:table augmentTitle="false" binding="#{Details.hotel}" id="hotel" title="Hotel Information" width="549">
                                <webuijsf:tableRowGroup binding="#{Details.tableRowGroup3}" id="tableRowGroup3" rows="10"
                                    sourceData="#{Details.hotelDataProvider}" sourceVar="currentRow">
                                    <webuijsf:tableColumn binding="#{Details.tableColumn9}" headerText="Hotel" id="tableColumn9" sort="HOTEL.HOTELNAME">
                                        <webuijsf:staticText binding="#{Details.staticText13}" id="staticText13" text="#{currentRow.value['HOTEL.HOTELNAME']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn25}" headerText="Checkin" id="tableColumn25" sort="HOTEL.CHECKINDATE">
                                        <webuijsf:staticText binding="#{Details.staticText29}" id="staticText29" text="#{currentRow.value['HOTEL.CHECKINDATE']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn26}" headerText="Checkout" id="tableColumn26" sort="HOTEL.CHECKOUTDATE">
                                        <webuijsf:staticText binding="#{Details.staticText30}" id="staticText30" text="#{currentRow.value['HOTEL.CHECKOUTDATE']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn27}" headerText="Guest" id="tableColumn27" sort="HOTEL.GUESTS">
                                        <webuijsf:staticText binding="#{Details.staticText31}" id="staticText31" text="#{currentRow.value['HOTEL.GUESTS']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn binding="#{Details.tableColumn28}" headerText="Status" id="tableColumn28" sort="HOTEL.BOOKINGSTATUS">
                                        <webuijsf:staticText binding="#{Details.staticText32}" id="staticText32" text="#{currentRow.value['HOTEL.BOOKINGSTATUS']}"/>
                                    </webuijsf:tableColumn>
                                </webuijsf:tableRowGroup>
                            </webuijsf:table>
                        </h:panelGrid>
                    </webuijsf:form>
                </webuijsf:body>
            </webuijsf:html>
        </webuijsf:page>
    </f:view>
</jsp:root>

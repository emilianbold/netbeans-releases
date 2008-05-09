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
                <webuijsf:body id="body1" style="background-color: #657881; -rave-layout: grid">
                    <webuijsf:form id="form1">
                        <div style="position: absolute; left: 0px; top: 0px">
                            <jsp:directive.include file="Header.jspf"/>
                        </div>
                        <h:panelGrid columns="2" id="mainPanel" style="padding: 5px; height: 100%; margin-top: 20px; left: 0px; top: 200px; position: absolute; width: 830px">
                            <webuijsf:staticText id="staticText1"/>
                            <webuijsf:button actionExpression="#{Details.backButton_action}" id="backButton" text="Back to Trips"/>
                            <webuijsf:label id="label1" text="Employee:"/>
                            <webuijsf:staticText id="personsName" text="#{Details.personDataProvider1.value['PERSON.NAME']}"/>
                            <webuijsf:staticText id="staticText2"/>
                            <webuijsf:table augmentTitle="false" id="flights" title="Flight Information" width="924">
                                <webuijsf:tableRowGroup id="tableRowGroup1" rows="10" sourceData="#{Details.flightDataProvider}" sourceVar="currentRow">
                                    <webuijsf:tableColumn headerText="Airline" id="tableColumn15" sort="FLIGHT.AIRLINENAME">
                                        <webuijsf:staticText id="staticText19" text="#{currentRow.value['FLIGHT.AIRLINENAME']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Flight" id="tableColumn10" sort="FLIGHT.FLIGHTNUM">
                                        <webuijsf:staticText id="staticText14" text="#{currentRow.value['FLIGHT.FLIGHTNUM']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Departs" id="tableColumn11" sort="FLIGHT.DEPTIME">
                                        <webuijsf:staticText id="staticText15" text="#{currentRow.value['FLIGHT.DEPTIME']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="From" id="tableColumn12" sort="FLIGHT.DEPAIRPORT">
                                        <webuijsf:staticText id="staticText16" text="#{currentRow.value['FLIGHT.DEPAIRPORT']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Arrives" id="tableColumn13" sort="FLIGHT.ARRTIME">
                                        <webuijsf:staticText id="staticText17" text="#{currentRow.value['FLIGHT.ARRTIME']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="At" id="tableColumn14" sort="FLIGHT.ARRAIRPORT">
                                        <webuijsf:staticText id="staticText18" text="#{currentRow.value['FLIGHT.ARRAIRPORT']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Direction" id="tableColumn3" sort="FLIGHT.DIRECTION">
                                        <webuijsf:staticText id="staticText7" text="#{currentRow.value['FLIGHT.DIRECTION']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Status" id="tableColumn16" sort="FLIGHT.BOOKINGSTATUS">
                                        <webuijsf:staticText id="staticText20" text="#{currentRow.value['FLIGHT.BOOKINGSTATUS']}"/>
                                    </webuijsf:tableColumn>
                                </webuijsf:tableRowGroup>
                            </webuijsf:table>
                            <webuijsf:staticText id="staticText3"/>
                            <webuijsf:table augmentTitle="false" id="autoRental" title="Auto Rental Information" width="801">
                                <webuijsf:tableRowGroup id="tableRowGroup2" rows="10" sourceData="#{Details.carrentalDataProvider}" sourceVar="currentRow">
                                    <webuijsf:tableColumn headerText="Company" id="tableColumn6" sort="CARRENTAL.PROVIDER">
                                        <webuijsf:staticText id="staticText10" text="#{currentRow.value['CARRENTAL.PROVIDER']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Type" id="tableColumn21" sort="CARRENTAL.CARTYPE">
                                        <webuijsf:staticText id="staticText25" text="#{currentRow.value['CARRENTAL.CARTYPE']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="City" id="tableColumn18" sort="CARRENTAL.CITY">
                                        <webuijsf:staticText id="staticText22" text="#{currentRow.value['CARRENTAL.CITY']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Pickup" id="tableColumn19" sort="CARRENTAL.PICKUPDATE">
                                        <webuijsf:staticText id="staticText23" text="#{currentRow.value['CARRENTAL.PICKUPDATE']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Return" id="tableColumn20" sort="CARRENTAL.RETURNDATE">
                                        <webuijsf:staticText id="staticText24" text="#{currentRow.value['CARRENTAL.RETURNDATE']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Rate" id="tableColumn22" sort="CARRENTAL.RATE">
                                        <webuijsf:staticText id="staticText26" text="#{currentRow.value['CARRENTAL.RATE']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Status" id="tableColumn23" sort="CARRENTAL.BOOKINGSTATUS">
                                        <webuijsf:staticText id="staticText27" text="#{currentRow.value['CARRENTAL.BOOKINGSTATUS']}"/>
                                    </webuijsf:tableColumn>
                                </webuijsf:tableRowGroup>
                            </webuijsf:table>
                            <webuijsf:staticText id="staticText4"/>
                            <webuijsf:table augmentTitle="false" id="hotel" title="Hotel Information" width="549">
                                <webuijsf:tableRowGroup id="tableRowGroup3" rows="10" sourceData="#{Details.hotelDataProvider}" sourceVar="currentRow">
                                    <webuijsf:tableColumn headerText="Hotel" id="tableColumn9" sort="HOTEL.HOTELNAME">
                                        <webuijsf:staticText id="staticText13" text="#{currentRow.value['HOTEL.HOTELNAME']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Checkin" id="tableColumn25" sort="HOTEL.CHECKINDATE">
                                        <webuijsf:staticText id="staticText29" text="#{currentRow.value['HOTEL.CHECKINDATE']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Checkout" id="tableColumn26" sort="HOTEL.CHECKOUTDATE">
                                        <webuijsf:staticText id="staticText30" text="#{currentRow.value['HOTEL.CHECKOUTDATE']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Guest" id="tableColumn27" sort="HOTEL.GUESTS">
                                        <webuijsf:staticText id="staticText31" text="#{currentRow.value['HOTEL.GUESTS']}"/>
                                    </webuijsf:tableColumn>
                                    <webuijsf:tableColumn headerText="Status" id="tableColumn28" sort="HOTEL.BOOKINGSTATUS">
                                        <webuijsf:staticText id="staticText32" text="#{currentRow.value['HOTEL.BOOKINGSTATUS']}"/>
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

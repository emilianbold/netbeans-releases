<?xml version="1.0" encoding="UTF-8"?>
<!--
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
-->
<jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <webuijsf:page binding="#{Page1.page1}" id="page1">
            <webuijsf:html binding="#{Page1.html1}" id="html1">
                <webuijsf:head binding="#{Page1.head1}" id="head1">
                    <webuijsf:link binding="#{Page1.link1}" id="link1" url="/resources/stylesheet.css"/>
                </webuijsf:head>
                <webuijsf:body binding="#{Page1.body1}" id="body1" style="-rave-layout: grid">
                    <webuijsf:form binding="#{Page1.form1}" id="form1" virtualFormsConfig="newTrip | mainPanel:addTripParentPanel:newTripInfoPanel:tripType mainPanel:addTripParentPanel:newTripInfoPanel:departureCity mainPanel:addTripParentPanel:newTripInfoPanel:departureDate mainPanel:addTripParentPanel:newTripInfoPanel:destinationCity | mainPanel:addTripParentPanel:newTripInfoPanel:addButton , deleteSelected | mainPanel:tripsPanel:trips:tableRowGroup1:tableColumn8:selectedTripCheckbox | mainPanel:tripsPanel:buttonsPanel:deleteButton , saveChanges | mainPanel:tripsPanel:trips:tableRowGroup1:tableColumn5:tripsDestinationCity mainPanel:tripsPanel:trips:tableRowGroup1:tableColumn6:tripsTripType mainPanel:tripsPanel:trips:tableRowGroup1:tableColumn4:tripsDepartureCity mainPanel:tripsPanel:trips:tableRowGroup1:tableColumn3:tripsDepartureDate | mainPanel:tripsPanel:buttonsPanel:updateButton , selectedPerson | mainPanel:personInfoPanel:personId | mainPanel:personInfoPanel:personId">
                        <div style="position: absolute; left: 0px; top: 0px">
                            <jsp:directive.include file="Masthead.jspf"/>
                        </div>
                        <h:panelGrid binding="#{Page1.mainPanel}" id="mainPanel" style="padding: 20px; left: 0px; top: 160px; position: absolute" width="768">
                            <h:panelGrid binding="#{Page1.personInfoPanel}" columns="4" id="personInfoPanel" style="height: 70px" width="455">
                                <webuijsf:label binding="#{Page1.label1}" id="label1" text="Person:"/>
                                <webuijsf:dropDown binding="#{Page1.personId}" converter="#{Page1.personIdConverter}" id="personId"
                                    items="#{Page1.personDataProvider.options['PERSON.PERSONID,PERSON.NAME']}"
                                    onChange="webui.suntheme.common.timeoutSubmitForm(this.form, 'mainPanel:personInfoPanel:personId');" valueChangeListenerExpression="#{Page1.personId_processValueChange}"/>
                                <webuijsf:label binding="#{Page1.label2}" id="label2" text="Job Title:"/>
                                <webuijsf:staticText binding="#{Page1.jobTitle}" id="jobTitle" text="#{Page1.personDataProvider.value['PERSON.JOBTITLE']}"/>
                                <webuijsf:staticText binding="#{Page1.staticText4}" id="staticText4"/>
                                <webuijsf:message binding="#{Page1.message1}" for="personId" id="message1" showDetail="false" showSummary="true"/>
                            </h:panelGrid>
                            <h:panelGrid binding="#{Page1.tripsPanel}" id="tripsPanel" style="width: 100%; height: 100%;">
                                <webuijsf:table augmentTitle="false" binding="#{Page1.trips}" id="trips" title="Trips Taken" width="480">
                                    <webuijsf:tableRowGroup binding="#{Page1.tableRowGroup1}" id="tableRowGroup1" rows="10"
                                        sourceData="#{Page1.tripDataProvider}" sourceVar="currentRow">
                                        <webuijsf:tableColumn binding="#{Page1.tableColumn8}" headerText="Select" id="tableColumn8">
                                            <webuijsf:checkbox binding="#{Page1.selectedTripCheckbox}" id="selectedTripCheckbox" selected="#{Page1.selectedTrip}"/>
                                        </webuijsf:tableColumn>
                                        <webuijsf:tableColumn binding="#{Page1.tableColumn3}" headerText="Departure Date" id="tableColumn3" sort="TRIP.DEPDATE">
                                            <webuijsf:textField binding="#{Page1.tripsDepartureDate}" id="tripsDepartureDate" text="#{currentRow.value['TRIP.DEPDATE']}"/>
                                            <webuijsf:message binding="#{Page1.message7}" for="tripsDepartureDate" id="message7" showDetail="false" showSummary="true"/>
                                        </webuijsf:tableColumn>
                                        <webuijsf:tableColumn binding="#{Page1.tableColumn4}" headerText="Departure City" id="tableColumn4" sort="TRIP.DEPCITY">
                                            <webuijsf:textField binding="#{Page1.tripsDepartureCity}" id="tripsDepartureCity" text="#{currentRow.value['TRIP.DEPCITY']}"/>
                                            <webuijsf:message binding="#{Page1.message8}" for="tripsDepartureCity" id="message8" showDetail="false" showSummary="true"/>
                                        </webuijsf:tableColumn>
                                        <webuijsf:tableColumn binding="#{Page1.tableColumn5}" headerText="Destination City" id="tableColumn5" sort="TRIP.DESTCITY">
                                            <webuijsf:textField binding="#{Page1.tripsDestinationCity}" id="tripsDestinationCity" text="#{currentRow.value['TRIP.DESTCITY']}"/>
                                            <webuijsf:message binding="#{Page1.message9}" for="tripsDestinationCity" id="message9" showDetail="false" showSummary="true"/>
                                        </webuijsf:tableColumn>
                                        <webuijsf:tableColumn binding="#{Page1.tableColumn6}" headerText="Trip Type" id="tableColumn6" sort="TRIP.TRIPTYPEID">
                                            <webuijsf:dropDown binding="#{Page1.tripsTripType}" converter="#{Page1.tripsTripTypeConverter}" id="tripsTripType"
                                                items="#{Page1.triptypeDataProvider.options['TRIPTYPE.TRIPTYPEID,TRIPTYPE.NAME']}" selected="#{currentRow.value['TRIP.TRIPTYPEID']}"/>
                                            <webuijsf:message binding="#{Page1.message6}" for="tripsTripType" id="message6" showDetail="false" showSummary="true"/>
                                        </webuijsf:tableColumn>
                                    </webuijsf:tableRowGroup>
                                </webuijsf:table>
                                <h:panelGrid binding="#{Page1.buttonsPanel}" columns="2" id="buttonsPanel" style="height: 100%" width="383">
                                    <webuijsf:button actionExpression="#{Page1.updateButton_action}" binding="#{Page1.updateButton}" id="updateButton" text="Update"/>
                                    <webuijsf:button actionExpression="#{Page1.deleteButton_action}" binding="#{Page1.deleteButton}" id="deleteButton" text="Delete"/>
                                </h:panelGrid>
                            </h:panelGrid>
                            <h:panelGrid binding="#{Page1.addTripParentPanel}" id="addTripParentPanel" style="width: 100%">
                                <h:panelGrid binding="#{Page1.headerPanel}" id="headerPanel" style="width: 100%">
                                    <webuijsf:label binding="#{Page1.label3}" id="label3" text="Add new trip information"/>
                                </h:panelGrid>
                                <h:panelGrid binding="#{Page1.newTripInfoPanel}" columns="3" id="newTripInfoPanel" style="width: 100%">
                                    <webuijsf:label binding="#{Page1.label4}" id="label4" text="Date:"/>
                                    <webuijsf:calendar binding="#{Page1.departureDate}" id="departureDate"/>
                                    <webuijsf:message binding="#{Page1.message2}" for="departureDate" id="message2" showDetail="false" showSummary="true"/>
                                    <webuijsf:label binding="#{Page1.label5}" id="label5" text="Departure:"/>
                                    <webuijsf:textField binding="#{Page1.departureCity}" id="departureCity" validatorExpression="#{Page1.departureCity_validate}"/>
                                    <webuijsf:message binding="#{Page1.message3}" for="departureCity" id="message3" showDetail="false" showSummary="true"/>
                                    <webuijsf:label binding="#{Page1.label7}" id="label7" text="Destination:"/>
                                    <webuijsf:textField binding="#{Page1.destinationCity}" id="destinationCity"/>
                                    <webuijsf:message binding="#{Page1.message4}" for="destinationCity" id="message4" showDetail="false" showSummary="true"/>
                                    <webuijsf:label binding="#{Page1.label6}" id="label6" text="Trip Type:"/>
                                    <webuijsf:dropDown binding="#{Page1.tripType}" converter="#{Page1.tripTypeConverter}" id="tripType" items="#{Page1.triptypeDataProvider.options['TRIPTYPE.TRIPTYPEID,TRIPTYPE.NAME']}"/>
                                    <webuijsf:message binding="#{Page1.message5}" for="tripType" id="message5" showDetail="false" showSummary="true"/>
                                    <webuijsf:staticText binding="#{Page1.staticText5}" id="staticText5"/>
                                    <webuijsf:button actionExpression="#{Page1.addButton_action}" binding="#{Page1.addButton}" id="addButton" text="Add"/>
                                </h:panelGrid>
                                <h:panelGrid binding="#{Page1.footerPanel}" id="footerPanel" style="width: 100%"/>
                            </h:panelGrid>
                        </h:panelGrid>
                    </webuijsf:form>
                </webuijsf:body>
            </webuijsf:html>
        </webuijsf:page>
    </f:view>
</jsp:root>

<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright (c) 2010, Oracle. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the name of Oracle nor the names of its contributors
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
                    <webuijsf:form id="form1">
                        <div style="position: absolute; left: 0px; top: 0px">
                            <jsp:directive.include file="Header.jspf"/>
                            <h:panelGrid id="content" style="left: -; top: 240px; position: absolute; width: 800px" styleClass="contents">
                                <f:verbatim>
                                    <h3>Vehicle Incident Report Application</h3>
                                    <h4>Pages available to all users</h4>
                                    <ul>
                                        <li>
                                            <b>Find Vehicle Page</b>: Search the database for the specified vehicle to report an incident. 
                                            Use % for wildcard search.</li>
                                        <li>
                                            <b>Login Page</b>: Login to the application to managed owned vehicles and profile. Use 1/johndoe, 
                                            2/janedoe, 3/jackdoe, 4/jilldoe, 5/sallyable, 6/zoezack, 7/suejacobs to log in to existing accounts or create a new user.</li>
                                        <li>
                                            <b>New User Page</b>: Register as a new user.</li>
                                        <li>
                                            <b>Help Page</b> - This page </li>
                                    </ul>
                                    <h4>Pages available to logged in users</h4>
                                    <ul>
                                        <li>
                                            <b>Choose Vehicle Page</b>: Select a vehicle to report an incident for.</li>
                                        <li>
                                            <b>Report Incident Page</b>: Enter the specifics of the incident</li>
                                        <li>
                                            <b>Incident Reported Page</b>: Informational indicating notification of the incident to the vehicle's owner.</li>
                                        <li>
                                            <b>Vehicles Page</b>: View the vehicles owned by logged in user. Also allow deleting an owned vehicle. </li>
                                        <li>
                                            <b>Add Vehicle Page</b>: Add a vehicle to list of owned vehicles</li>
                                    </ul>
                                    <p>The Vehicle Incident Report Application was created using NetBeans 6.0 IDE.</p>
                                </f:verbatim>
                            </h:panelGrid>
                        </div>
                        <div style="position: absolute; left: 0px; top: 600px">
                            <jsp:directive.include file="Footer.jspf"/>
                        </div>
                    </webuijsf:form>
                </webuijsf:body>
            </webuijsf:html>
        </webuijsf:page>
    </f:view>
</jsp:root>

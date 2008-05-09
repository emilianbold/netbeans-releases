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
                    <webuijsf:form id="form1">
                        <div style="position: absolute; left: 0px; top: 0px">
                            <jsp:directive.include file="Header.jspf"/>
                        </div>
                        <h:panelGrid id="content" style="left: 0px; top: 240px; position: absolute; width: 800px" styleClass="contents">
                            <h:panelGrid id="contentGrid" style="width: 100%; height: 100%;">
                                <h:panelGrid id="messageGrid" style="width: 100%; height: 100%;">
                                    <webuijsf:messageGroup id="messageGroup1"/>
                                </h:panelGrid>
                                <h:panelGrid id="paddingPanel" style="height: 20px; width: 100%"/>
                                <h:panelGrid cellpadding="5" columns="3" id="dataGrid" style="width: 100%; height: 100%;">
                                    <webuijsf:label id="label1" text="User ID:"/>
                                    <webuijsf:staticText id="userId" text="#{SessionBean1.loggedInUserId}"/>
                                    <webuijsf:message id="message1" showDetail="false" showSummary="true"/>
                                    <webuijsf:label id="label2" text="First Name:"/>
                                    <webuijsf:textField binding="#{Profile.firstName}" id="firstName" text="#{Profile.employeeDataProvider.value['EMPLOYEE.FIRSTNAME']}"/>
                                    <webuijsf:message for="firstName" id="message2" showDetail="false" showSummary="true"/>
                                    <webuijsf:label id="label3" text="Last Name:"/>
                                    <webuijsf:textField binding="#{Profile.lastName}" id="lastName" text="#{Profile.employeeDataProvider.value['EMPLOYEE.LASTNAME']}"/>
                                    <webuijsf:message for="lastName" id="message3" showDetail="false" showSummary="true"/>
                                    <webuijsf:label id="label4" text="Email Address:"/>
                                    <webuijsf:textField binding="#{Profile.emailAddress}" id="emailAddress"
                                        text="#{Profile.employeeDataProvider.value['EMPLOYEE.EMAIL']}" validatorExpression="#{Profile.emailAddress_validate}"/>
                                    <webuijsf:message for="emailAddress" id="message4" showDetail="false" showSummary="true"/>
                                    <webuijsf:label id="label7" text="Current Password:"/>
                                    <webuijsf:passwordField binding="#{Profile.currentPassword}" id="currentPassword" validatorExpression="#{Profile.passwordLengthValidator.validate}"/>
                                    <webuijsf:message for="currentPassword" id="message7" showDetail="false" showSummary="true"/>
                                    <webuijsf:label id="label5" text="New Password:"/>
                                    <webuijsf:passwordField binding="#{Profile.newPassword}" id="newPassword" validatorExpression="#{Profile.passwordLengthValidator.validate}"/>
                                    <webuijsf:message for="newPassword" id="message5" showDetail="false" showSummary="true"/>
                                    <webuijsf:label id="label6" text="Retype New Password:"/>
                                    <webuijsf:passwordField binding="#{Profile.retypeNewPassword}" id="retypeNewPassword" validatorExpression="#{Profile.passwordLengthValidator.validate}"/>
                                    <webuijsf:message for="retypeNewPassword" id="message6" showDetail="false" showSummary="true"/>
                                </h:panelGrid>
                                <webuijsf:button actionExpression="#{Profile.update_action}" id="update" text="Update"/>
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

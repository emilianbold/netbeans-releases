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
        <webuijsf:page binding="#{NewUser.page1}" id="page1">
            <webuijsf:html binding="#{NewUser.html1}" id="html1">
                <webuijsf:head binding="#{NewUser.head1}" id="head1">
                    <webuijsf:link binding="#{NewUser.link1}" id="link1" url="/resources/stylesheet.css"/>
                </webuijsf:head>
                <webuijsf:body binding="#{NewUser.body1}" id="body1" style="-rave-layout: grid">
                    <webuijsf:form binding="#{NewUser.form1}" id="form1">
                        <div style="position: absolute; left: 0px; top: 0px">
                            <jsp:directive.include file="Header.jspf"/>
                            <h:panelGrid binding="#{NewUser.content}" id="content" style="left: 0px; top: 240px; position: absolute; width: 800px" styleClass="contents">
                                <h:panelGrid binding="#{NewUser.contentGrid}" id="contentGrid" style="width: 100%; height: 100%;">
                                    <h:panelGrid binding="#{NewUser.messageGrid}" id="messageGrid" style="height: 100%; width: 100%">
                                        <webuijsf:messageGroup binding="#{NewUser.messageGroup1}" id="messageGroup1"/>
                                    </h:panelGrid>
                                    <h:panelGrid binding="#{NewUser.dataGrid}" cellpadding="5" columns="3" id="dataGrid" style="height: 100%; width: 100%">
                                        <webuijsf:label binding="#{NewUser.label1}" id="label1" text="User ID:"/>
                                        <webuijsf:textField binding="#{NewUser.userId}" converter="#{NewUser.integerConverter1}" id="userId"/>
                                        <webuijsf:message binding="#{NewUser.message1}" for="userId" id="message1" showDetail="false" showSummary="true"/>
                                        <webuijsf:label binding="#{NewUser.label2}" id="label2" text="First Name:"/>
                                        <webuijsf:textField binding="#{NewUser.firstName}" id="firstName"/>
                                        <webuijsf:message binding="#{NewUser.message2}" for="firstName" id="message2" showDetail="false" showSummary="true"/>
                                        <webuijsf:label binding="#{NewUser.label3}" id="label3" text="Last Name:"/>
                                        <webuijsf:textField binding="#{NewUser.lastName}" id="lastName"/>
                                        <webuijsf:message binding="#{NewUser.message3}" for="lastName" id="message3" showDetail="false" showSummary="true"/>
                                        <webuijsf:label binding="#{NewUser.label4}" id="label4" text="Email Address:"/>
                                        <webuijsf:textField binding="#{NewUser.emailAddress}" id="emailAddress" validatorExpression="#{NewUser.emailAddress_validate}"/>
                                        <webuijsf:message binding="#{NewUser.message4}" for="emailAddress" id="message4" showDetail="false" showSummary="true"/>
                                        <webuijsf:label binding="#{NewUser.label5}" id="label5" text="Password:"/>
                                        <webuijsf:passwordField binding="#{NewUser.password}" id="password" validatorExpression="#{NewUser.passwordLengthValidator.validate}"/>
                                        <webuijsf:message binding="#{NewUser.message5}" for="password" id="message5" showDetail="false" showSummary="true"/>
                                        <webuijsf:label binding="#{NewUser.label6}" id="label6" text="Retype Password:"/>
                                        <webuijsf:passwordField binding="#{NewUser.retypePassword}" id="retypePassword" validatorExpression="#{NewUser.passwordLengthValidator.validate}"/>
                                        <webuijsf:message binding="#{NewUser.message6}" for="retypePassword" id="message6" showDetail="false" showSummary="true"/>
                                    </h:panelGrid>
                                    <h:panelGrid binding="#{NewUser.paddingPanel}" id="paddingPanel" style="height: 20px; width: 100%"/>
                                    <webuijsf:button actionExpression="#{NewUser.register_action}" binding="#{NewUser.register}" id="register" text="Register"/>
                                </h:panelGrid>
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

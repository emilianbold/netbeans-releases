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
        <webuijsf:page binding="#{Preview.page1}" id="page1">
            <webuijsf:html binding="#{Preview.html1}" id="html1">
                <webuijsf:head binding="#{Preview.head1}" id="head1">
                    <webuijsf:link binding="#{Preview.link1}" id="link1" url="/resources/stylesheet.css"/>
                </webuijsf:head>
                <webuijsf:body binding="#{Preview.body1}" id="body1" style="-rave-layout: grid">
                    <webuijsf:form binding="#{Preview.form1}" id="form1">
                        <div style="position: absolute; left: 0px; top: 0px">
                            <jsp:directive.include file="Masthead.jspf"/>
                        </div>
                        <h:panelGrid binding="#{Preview.mainPanel}" id="mainPanel" style="margin: 5px; padding: 5px; height: 100%; left: 0px; top: 160px; position: absolute; width: 760px">
                            <h:panelGrid binding="#{Preview.navigationPanel}" id="navigationPanel" style="width: 50%">
                                <webuijsf:hyperlink actionExpression="#{Preview.returnLink_action}" binding="#{Preview.returnLink}" id="returnLink" text="Return to main page"/>
                            </h:panelGrid>
                            <h:panelGrid binding="#{Preview.genrePanel}" columns="3" id="genrePanel" style="width: 50%">
                                <webuijsf:label binding="#{Preview.genre1}" id="genre1" text="Revise Genre"/>
                                <webuijsf:dropDown binding="#{Preview.genre}" id="genre" items="#{ApplicationBean1.genreOptions}" valueChangeListenerExpression="#{Preview.genre_processValueChange}"/>
                                <webuijsf:message binding="#{Preview.message1}" for="genre" id="message1" showDetail="false" showSummary="true"/>
                            </h:panelGrid>
                            <h:panelGrid binding="#{Preview.moviePanel}" id="moviePanel" style="width: 100%; height: 100%;">
                                <webuijsf:staticText binding="#{Preview.title}" id="title" style="font-size: 18px; font-weight: bold" text="#{SessionBean1.movieListDataProvider.value['title']}"/>
                                <webuijsf:staticText binding="#{Preview.year}" id="year" text="#{SessionBean1.movieListDataProvider.value['year']}"/>
                                <webuijsf:image binding="#{Preview.image}" id="image" url="#{SessionBean1.movieListDataProvider.value['image']}"/>
                                <h:panelGrid binding="#{Preview.detailsPanel}" columns="2" id="detailsPanel" style="width: 50%; height: 100%;">
                                    <webuijsf:label binding="#{Preview.label1}" id="label1" text="Genre:"/>
                                    <webuijsf:staticText binding="#{Preview.detailsGenre}" id="detailsGenre" text="#{SessionBean1.movieListDataProvider.value['genre']}"/>
                                    <webuijsf:label binding="#{Preview.label2}" id="label2" text="Rating:"/>
                                    <webuijsf:staticText binding="#{Preview.detailsRating}" id="detailsRating" text="#{SessionBean1.movieListDataProvider.value['rating']}"/>
                                    <webuijsf:label binding="#{Preview.label3}" id="label3" text="Length:"/>
                                    <webuijsf:staticText binding="#{Preview.detailsLength}" id="detailsLength" text="#{SessionBean1.movieListDataProvider.value['length']}"/>
                                    <webuijsf:label binding="#{Preview.label4}" id="label4" text="Description:"/>
                                    <webuijsf:staticText binding="#{Preview.detailsDescription}" id="detailsDescription" text="#{SessionBean1.movieListDataProvider.value['description']}"/>
                                </h:panelGrid>
                            </h:panelGrid>
                        </h:panelGrid>
                    </webuijsf:form>
                </webuijsf:body>
            </webuijsf:html>
        </webuijsf:page>
    </f:view>
</jsp:root>

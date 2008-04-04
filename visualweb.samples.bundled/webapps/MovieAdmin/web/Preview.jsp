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
                            <jsp:directive.include file="Masthead.jspf"/>
                        </div>
                        <h:panelGrid id="mainPanel" style="margin: 5px; padding: 5px; height: 100%; left: 0px; top: 160px; position: absolute; width: 760px">
                            <h:panelGrid id="navigationPanel" style="width: 50%">
                                <webuijsf:hyperlink actionExpression="#{Preview.returnLink_action}" id="returnLink" text="Return to main page"/>
                            </h:panelGrid>
                            <h:panelGrid columns="3" id="genrePanel" style="width: 50%">
                                <webuijsf:label id="genre1" text="Revise Genre"/>
                                <webuijsf:dropDown binding="#{Preview.genre}" id="genre" items="#{ApplicationBean1.genreOptions}" valueChangeListenerExpression="#{Preview.genre_processValueChange}"/>
                                <webuijsf:message for="genre" id="message1" showDetail="false" showSummary="true"/>
                            </h:panelGrid>
                            <h:panelGrid id="moviePanel" style="width: 100%; height: 100%;">
                                <webuijsf:staticText id="title" style="font-size: 18px; font-weight: bold" text="#{SessionBean1.movieListDataProvider.value['title']}"/>
                                <webuijsf:staticText id="year" text="#{SessionBean1.movieListDataProvider.value['year']}"/>
                                <webuijsf:image id="image" url="#{SessionBean1.movieListDataProvider.value['image']}"/>
                                <h:panelGrid columns="2" id="detailsPanel" style="width: 50%; height: 100%;">
                                    <webuijsf:label id="label1" text="Genre:"/>
                                    <webuijsf:staticText id="detailsGenre" text="#{SessionBean1.movieListDataProvider.value['genre']}"/>
                                    <webuijsf:label id="label2" text="Rating:"/>
                                    <webuijsf:staticText id="detailsRating" text="#{SessionBean1.movieListDataProvider.value['rating']}"/>
                                    <webuijsf:label id="label3" text="Length:"/>
                                    <webuijsf:staticText id="detailsLength" text="#{SessionBean1.movieListDataProvider.value['length']}"/>
                                    <webuijsf:label id="label4" text="Description:"/>
                                    <webuijsf:staticText id="detailsDescription" text="#{SessionBean1.movieListDataProvider.value['description']}"/>
                                </h:panelGrid>
                            </h:panelGrid>
                        </h:panelGrid>
                    </webuijsf:form>
                </webuijsf:body>
            </webuijsf:html>
        </webuijsf:page>
    </f:view>
</jsp:root>

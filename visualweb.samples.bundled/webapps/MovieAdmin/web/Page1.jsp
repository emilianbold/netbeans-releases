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
        <webuijsf:page binding="#{Page1.page1}" id="page1">
            <webuijsf:html binding="#{Page1.html1}" id="html1">
                <webuijsf:head binding="#{Page1.head1}" id="head1">
                    <webuijsf:link binding="#{Page1.link1}" id="link1" url="/resources/stylesheet.css"/>
                </webuijsf:head>
                <webuijsf:body binding="#{Page1.body1}" id="body1" style="-rave-layout: grid">
                    <webuijsf:form binding="#{Page1.form1}" id="form1" virtualFormsConfig="genre | mainPanel:currentViewPanel:currentGenre | mainPanel:currentViewPanel:currentGenre , add | mainPanel:addMoviePanel:addYear mainPanel:addMoviePanel:addImage mainPanel:addMoviePanel:addRating mainPanel:addMoviePanel:addGenre mainPanel:addMoviePanel:addLength mainPanel:addMoviePanel:addDescription mainPanel:addMoviePanel:addTitle | mainPanel:addMoviePanel:add , update | mainPanel:moviesTablePanel:movies:tableRowGroup1:tableColumn6:textArea1 mainPanel:moviesTablePanel:movies:tableRowGroup1:tableColumn2:dropDown1 mainPanel:moviesTablePanel:movies:tableRowGroup1:tableColumn1:textField1 mainPanel:moviesTablePanel:movies:tableRowGroup1:tableColumn3:textField2 mainPanel:moviesTablePanel:movies:tableRowGroup1:tableColumn4:textField3 mainPanel:moviesTablePanel:movies:tableRowGroup1:tableColumn5:textField4 | mainPanel:moviesTablePanel:update , preview/review | | mainPanel:moviesTablePanel:movies:tableRowGroup1:tableColumn7:preview mainPanel:moviesTablePanel:movies:tableRowGroup1:tableColumn7:remove">
                        <div style="position: absolute; left: 0px; top: 0px">
                            <jsp:directive.include file="Masthead.jspf"/>
                        </div>
                        <h:panelGrid binding="#{Page1.mainPanel}" id="mainPanel" style="margin: 5px; padding: 5px; height: 100%; left: 0px; top: 160px; position: absolute; width: 760px">
                            <h:panelGrid binding="#{Page1.currentViewPanel}" columns="3" id="currentViewPanel" style="height: 100%; width: 50%">
                                <webuijsf:label binding="#{Page1.label1}" id="label1" text="Genre:"/>
                                <webuijsf:dropDown binding="#{Page1.currentGenre}" id="currentGenre" items="#{ApplicationBean1.genreOptions}"
                                    onChange="webui.suntheme.common.timeoutSubmitForm(this.form, 'mainPanel:currentViewPanel:currentGenre');" valueChangeListenerExpression="#{Page1.currentGenre_processValueChange}"/>
                                <webuijsf:message binding="#{Page1.message7}" for="currentGenre" id="message7" showDetail="false" showSummary="true"/>
                            </h:panelGrid>
                            <h:panelGrid binding="#{Page1.moviesTablePanel}" id="moviesTablePanel" style="width: 100%; height: 100%;">
                                <webuijsf:table augmentTitle="false" binding="#{Page1.movies}" id="movies" title="Movies" width="840">
                                    <webuijsf:tableRowGroup binding="#{Page1.tableRowGroup1}" id="tableRowGroup1" rows="10"
                                        sourceData="#{SessionBean1.movieListDataProvider}" sourceVar="currentRow">
                                        <webuijsf:tableColumn binding="#{Page1.tableColumn1}" headerText="Title" id="tableColumn1" sort="title">
                                            <webuijsf:textField binding="#{Page1.textField1}" id="textField1" text="#{currentRow.value['title']}"/>
                                        </webuijsf:tableColumn>
                                        <webuijsf:tableColumn binding="#{Page1.tableColumn2}" headerText="Rating" id="tableColumn2">
                                            <webuijsf:dropDown binding="#{Page1.dropDown1}" id="dropDown1" items="#{ApplicationBean1.ratingOptions}" selected="#{SessionBean1.movieListDataProvider.value['rating']}"/>
                                        </webuijsf:tableColumn>
                                        <webuijsf:tableColumn binding="#{Page1.tableColumn3}" headerText="Year" id="tableColumn3" sort="year">
                                            <webuijsf:textField binding="#{Page1.textField2}" id="textField2" text="#{currentRow.value['year']}"/>
                                        </webuijsf:tableColumn>
                                        <webuijsf:tableColumn binding="#{Page1.tableColumn4}" headerText="Length" id="tableColumn4" sort="length">
                                            <webuijsf:textField binding="#{Page1.textField3}" id="textField3" text="#{currentRow.value['length']}"/>
                                        </webuijsf:tableColumn>
                                        <webuijsf:tableColumn binding="#{Page1.tableColumn5}" headerText="Image" id="tableColumn5" sort="image">
                                            <webuijsf:textField binding="#{Page1.textField4}" id="textField4" text="#{currentRow.value['image']}"/>
                                        </webuijsf:tableColumn>
                                        <webuijsf:tableColumn binding="#{Page1.tableColumn6}" headerText="Description" id="tableColumn6" sort="description">
                                            <webuijsf:textArea binding="#{Page1.textArea1}" id="textArea1" text="#{currentRow.value['description']}"/>
                                        </webuijsf:tableColumn>
                                        <webuijsf:tableColumn binding="#{Page1.tableColumn7}" id="tableColumn7">
                                            <webuijsf:button actionExpression="#{Page1.preview_action}" binding="#{Page1.preview}" id="preview" text="Preview"/>
                                            <webuijsf:button actionExpression="#{Page1.remove_action}" binding="#{Page1.remove}" id="remove" text="Remove"/>
                                        </webuijsf:tableColumn>
                                    </webuijsf:tableRowGroup>
                                </webuijsf:table>
                                <webuijsf:button actionExpression="#{Page1.update_action}" binding="#{Page1.update}" id="update" text="Update"/>
                            </h:panelGrid>
                            <h:panelGrid binding="#{Page1.addMoviePanel}" columns="3" id="addMoviePanel" style="height: 100%; width:75%">
                                <webuijsf:label binding="#{Page1.label2}" id="label2" text="Genre:"/>
                                <webuijsf:dropDown binding="#{Page1.addGenre}" id="addGenre" items="#{ApplicationBean1.genreOptions}"/>
                                <webuijsf:message binding="#{Page1.message1}" for="addGenre" id="message1" showDetail="false" showSummary="true"/>
                                <webuijsf:label binding="#{Page1.label3}" id="label3" text="Title:"/>
                                <webuijsf:textField binding="#{Page1.addTitle}" id="addTitle"/>
                                <webuijsf:message binding="#{Page1.message2}" for="addTitle" id="message2" showDetail="false" showSummary="true"/>
                                <webuijsf:label binding="#{Page1.label4}" id="label4" text="Year:"/>
                                <webuijsf:textField binding="#{Page1.addYear}" converter="#{Page1.yearIntegerConverter}" id="addYear" validatorExpression="#{Page1.yearRangeValidator.validate}"/>
                                <webuijsf:message binding="#{Page1.message3}" for="addYear" id="message3" showDetail="false" showSummary="true"/>
                                <webuijsf:label binding="#{Page1.label5}" id="label5" text="Minutes:"/>
                                <webuijsf:textField binding="#{Page1.addLength}" converter="#{Page1.lengthIntegerConverter}" id="addLength" validatorExpression="#{Page1.lengthRangeValidator.validate}"/>
                                <webuijsf:message binding="#{Page1.message4}" for="addLength" id="message4" showDetail="false" showSummary="true"/>
                                <webuijsf:label binding="#{Page1.label6}" id="label6" text="Rating:"/>
                                <webuijsf:dropDown binding="#{Page1.addRating}" id="addRating" items="#{ApplicationBean1.ratingOptions}"/>
                                <webuijsf:message binding="#{Page1.message5}" for="addRating" id="message5" showDetail="false" showSummary="true"/>
                                <webuijsf:label binding="#{Page1.label9}" id="label9" text="Image:"/>
                                <webuijsf:textField binding="#{Page1.addImage}" id="addImage"/>
                                <webuijsf:message binding="#{Page1.message8}" for="addImage" id="message8" showDetail="false" showSummary="true"/>
                                <webuijsf:label binding="#{Page1.label7}" id="label7" text="Upload Image:"/>
                                <webuijsf:upload binding="#{Page1.uploadIImage}" id="uploadIImage"/>
                                <webuijsf:message binding="#{Page1.message6}" for="uploadIImage" id="message6" showDetail="false" showSummary="true"/>
                                <webuijsf:staticText binding="#{Page1.imagePad1}" id="imagePad1"/>
                                <webuijsf:button actionExpression="#{Page1.uploadImage_action}" binding="#{Page1.uploadImage}" id="uploadImage" text="Upload Image"/>
                                <webuijsf:staticText binding="#{Page1.imagePad2}" id="imagePad2"/>
                                <webuijsf:label binding="#{Page1.label8}" id="label8" text="Description:"/>
                                <webuijsf:textArea binding="#{Page1.addDescription}" id="addDescription"/>
                                <webuijsf:staticText binding="#{Page1.staticText6}" id="staticText6"/>
                                <webuijsf:staticText binding="#{Page1.staticText4}" id="staticText4"/>
                                <webuijsf:button actionExpression="#{Page1.add_action}" binding="#{Page1.add}" id="add" text="Add"/>
                                <webuijsf:staticText binding="#{Page1.staticText5}" id="staticText5"/>
                            </h:panelGrid>
                        </h:panelGrid>
                    </webuijsf:form>
                </webuijsf:body>
            </webuijsf:html>
        </webuijsf:page>
    </f:view>
</jsp:root>

<?xml version="1.0" encoding="UTF-8"?>
<!-- 
    Document   : Page1
    Created on : 14.12.2007, 14:41:09
    Author     : Administrator
-->
<jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <webuijsf:page binding="#{Page1_53.page1}" id="page1">
            <webuijsf:html binding="#{Page1_53.html1}" id="html1">
                <webuijsf:head binding="#{Page1_53.head1}" id="head1">
                    <webuijsf:link binding="#{Page1_53.link1}" id="link1" url="/resources/stylesheet.css"/>
                </webuijsf:head>
                <webuijsf:body binding="#{Page1_53.body1}" id="body1" style="-rave-layout: grid">
                    <webuijsf:form binding="#{Page1_53.form1}" id="form1">
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table1}" id="table1"
                            style="left: 24px; top: 24px; position: absolute; width: 450px" title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup1}" id="tableRowGroup1" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn1}" headerText="column1" id="tableColumn1" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText1}" id="staticText1" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn2}" headerText="column2" id="tableColumn2" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText2}" id="staticText2" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn3}" headerText="column3" id="tableColumn3" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText3}" id="staticText3" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table2}" id="table2" style="position: absolute; left: 24px; top: 192px"
                            title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup2}" id="tableRowGroup2" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn4}" headerText="column1" id="tableColumn4" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText4}" id="staticText4" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn5}" headerText="column2" id="tableColumn5" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText5}" id="staticText5" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn6}" headerText="column3" id="tableColumn6" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText6}" id="staticText6" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table3}" id="table3" style="position: absolute; left: 24px; top: 360px"
                            title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup3}" id="tableRowGroup3" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn7}" headerText="column1" id="tableColumn7" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText7}" id="staticText7" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn8}" headerText="column2" id="tableColumn8" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText8}" id="staticText8" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn9}" headerText="column3" id="tableColumn9" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText9}" id="staticText9" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table4}" id="table4" style="position: absolute; left: 24px; top: 528px"
                            title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup4}" id="tableRowGroup4" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn10}" headerText="column1" id="tableColumn10" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText10}" id="staticText10" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn11}" headerText="column2" id="tableColumn11" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText11}" id="staticText11" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn12}" headerText="column3" id="tableColumn12" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText12}" id="staticText12" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table5}" id="table5"
                            style="left: 24px; top: 696px; position: absolute; width: 450px" title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup5}" id="tableRowGroup5" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn13}" headerText="column1" id="tableColumn13" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText13}" id="staticText13" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn14}" headerText="column2" id="tableColumn14" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText14}" id="staticText14" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn15}" headerText="column3" id="tableColumn15" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText15}" id="staticText15" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table6}" id="table6" style="position: absolute; left: 504px; top: 24px"
                            title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup6}" id="tableRowGroup6" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn16}" headerText="column1" id="tableColumn16" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText16}" id="staticText16" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn17}" headerText="column2" id="tableColumn17" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText17}" id="staticText17" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn18}" headerText="column3" id="tableColumn18" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText18}" id="staticText18" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table7}" id="table7" style="position: absolute; left: 504px; top: 192px"
                            title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup7}" id="tableRowGroup7" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn19}" headerText="column1" id="tableColumn19" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText19}" id="staticText19" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn20}" headerText="column2" id="tableColumn20" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText20}" id="staticText20" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn21}" headerText="column3" id="tableColumn21" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText21}" id="staticText21" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table8}" id="table8" style="position: absolute; left: 504px; top: 360px"
                            title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup8}" id="tableRowGroup8" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn22}" headerText="column1" id="tableColumn22" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText22}" id="staticText22" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn23}" headerText="column2" id="tableColumn23" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText23}" id="staticText23" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn24}" headerText="column3" id="tableColumn24" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText24}" id="staticText24" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table9}" id="table9" style="position: absolute; left: 504px; top: 528px"
                            title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup9}" id="tableRowGroup9" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn25}" headerText="column1" id="tableColumn25" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText25}" id="staticText25" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn26}" headerText="column2" id="tableColumn26" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText26}" id="staticText26" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn27}" headerText="column3" id="tableColumn27" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText27}" id="staticText27" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table10}" id="table10"
                            style="left: 504px; top: 696px; position: absolute; width: 450px" title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup10}" id="tableRowGroup10" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn28}" headerText="column1" id="tableColumn28" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText28}" id="staticText28" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn29}" headerText="column2" id="tableColumn29" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText29}" id="staticText29" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn30}" headerText="column3" id="tableColumn30" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText30}" id="staticText30" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table11}" id="table11"
                            style="left: 984px; top: 24px; position: absolute; width: 450px" title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup11}" id="tableRowGroup11" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn31}" headerText="column1" id="tableColumn31" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText31}" id="staticText31" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn32}" headerText="column2" id="tableColumn32" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText32}" id="staticText32" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn33}" headerText="column3" id="tableColumn33" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText33}" id="staticText33" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table12}" id="table12" style="position: absolute; left: 984px; top: 192px"
                            title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup12}" id="tableRowGroup12" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn34}" headerText="column1" id="tableColumn34" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText34}" id="staticText34" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn35}" headerText="column2" id="tableColumn35" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText35}" id="staticText35" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn36}" headerText="column3" id="tableColumn36" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText36}" id="staticText36" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table13}" id="table13" style="position: absolute; left: 984px; top: 360px"
                            title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup13}" id="tableRowGroup13" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn37}" headerText="column1" id="tableColumn37" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText37}" id="staticText37" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn38}" headerText="column2" id="tableColumn38" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText38}" id="staticText38" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn39}" headerText="column3" id="tableColumn39" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText39}" id="staticText39" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table14}" id="table14" style="position: absolute; left: 984px; top: 528px"
                            title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup14}" id="tableRowGroup14" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn40}" headerText="column1" id="tableColumn40" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText40}" id="staticText40" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn41}" headerText="column2" id="tableColumn41" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText41}" id="staticText41" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn42}" headerText="column3" id="tableColumn42" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText42}" id="staticText42" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table15}" id="table15" style="position: absolute; left: 984px; top: 696px"
                            title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup15}" id="tableRowGroup15" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn43}" headerText="column1" id="tableColumn43" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText43}" id="staticText43" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn44}" headerText="column2" id="tableColumn44" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText44}" id="staticText44" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn45}" headerText="column3" id="tableColumn45" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText45}" id="staticText45" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table16}" id="table16"
                            style="height: 24px; left: 1464px; top: 24px; position: absolute; width: 0px" title="Table" width="0">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup16}" id="tableRowGroup16" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn46}" headerText="column1" id="tableColumn46" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText46}" id="staticText46" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn47}" headerText="column2" id="tableColumn47" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText47}" id="staticText47" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn48}" headerText="column3" id="tableColumn48" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText48}" id="staticText48" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table17}" id="table17" style="position: absolute; left: 1464px; top: 192px"
                            title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup17}" id="tableRowGroup17" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn49}" headerText="column1" id="tableColumn49" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText49}" id="staticText49" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn50}" headerText="column2" id="tableColumn50" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText50}" id="staticText50" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn51}" headerText="column3" id="tableColumn51" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText51}" id="staticText51" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table18}" id="table18"
                            style="left: 1464px; top: 360px; position: absolute; width: 450px" title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup18}" id="tableRowGroup18" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn52}" headerText="column1" id="tableColumn52" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText52}" id="staticText52" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn53}" headerText="column2" id="tableColumn53" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText53}" id="staticText53" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn54}" headerText="column3" id="tableColumn54" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText54}" id="staticText54" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table19}" id="table19" style="position: absolute; left: 1464px; top: 528px"
                            title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup19}" id="tableRowGroup19" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn55}" headerText="column1" id="tableColumn55" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText55}" id="staticText55" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn56}" headerText="column2" id="tableColumn56" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText56}" id="staticText56" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn57}" headerText="column3" id="tableColumn57" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText57}" id="staticText57" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                        <webuijsf:table augmentTitle="false" binding="#{Page1_53.table20}" id="table20" style="position: absolute; left: 1464px; top: 696px"
                            title="Table" width="450">
                            <webuijsf:tableRowGroup binding="#{Page1_53.tableRowGroup20}" id="tableRowGroup20" rows="10"
                                sourceData="#{Page1_53.defaultTableDataProvider}" sourceVar="currentRow">
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn58}" headerText="column1" id="tableColumn58" sort="column1">
                                    <webuijsf:staticText binding="#{Page1_53.staticText58}" id="staticText58" text="#{currentRow.value['column1']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn59}" headerText="column2" id="tableColumn59" sort="column2">
                                    <webuijsf:staticText binding="#{Page1_53.staticText59}" id="staticText59" text="#{currentRow.value['column2']}"/>
                                </webuijsf:tableColumn>
                                <webuijsf:tableColumn binding="#{Page1_53.tableColumn60}" headerText="column3" id="tableColumn60" sort="column3">
                                    <webuijsf:staticText binding="#{Page1_53.staticText60}" id="staticText60" text="#{currentRow.value['column3']}"/>
                                </webuijsf:tableColumn>
                            </webuijsf:tableRowGroup>
                        </webuijsf:table>
                    </webuijsf:form>
                </webuijsf:body>
            </webuijsf:html>
        </webuijsf:page>
    </f:view>
</jsp:root>

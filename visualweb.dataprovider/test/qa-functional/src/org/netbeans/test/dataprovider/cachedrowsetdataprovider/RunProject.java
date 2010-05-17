/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
*/
package org.netbeans.test.dataprovider.cachedrowsetdataprovider;

import org.netbeans.modules.visualweb.gravy.*;
import com.meterware.httpunit.*;
import java.util.*;
import java.util.regex.*;
import org.netbeans.test.dataprovider.common.*;

public class RunProject implements Constants {
    private String webAppURL = TestPropertiesHandler.getServerProperty(
        "Application_Server_URL") + ProjectTests.getCurrentPrjName();
    private int webResponseCode;
    
    public String runProject() {
        String errMsg = null;
        webResponseCode = -1;
        try {
            // if web-application with the same name was deployed already, undeploy it first
            WebUtils.undeployProject(ProjectTests.getCurrentPrjName());

            WebUtils.runProject(ProjectTests.getCurrentPrjName());
            WebResponse response = checkStatusWebAppDeployedFirst();
            //response = changePersonDropDownListValue(response, "New Value, for example: Donaldson, Sue"); 
        } catch (Throwable e) {
            e.printStackTrace(Utils.logStream);
            errMsg = (e.getMessage() == null ? e.toString() : e.getMessage());
        }
        return errMsg;
    }
    
    private WebResponse checkStatusWebAppDeployedFirst() {
        try {
            Utils.logMsg("+++ webAppURL = " + webAppURL);
            
            WebResponse response = WebUtils.getWebResponseAfterDeployment(webAppURL);
            String contentData = response.getText();
            Utils.logMsg("+++ Web response after project deployment: " +
                "content type = [" + response.getContentType() + "], " +
                "response code = [" + response.getResponseCode() + "], " +
                "content data = [" + contentData + "]");

            if (!WebUtils.isWebResponseOK(response)) {
                throw new RuntimeException(
                    "Result of deployment: web application isn't running correctly (web response code " +
                    "is " + response.getResponseCode() + " instead of " + WEB_RESPONSE_CODE_OK + ")");
            }
            Utils.logMsg("+++ Web-application [" + webAppURL + "] response is correct");
            webResponseCode = WEB_RESPONSE_CODE_OK;
            checkContentDataAfterDeployment(contentData);
            return response;
        } catch (Throwable t) {
            t.printStackTrace(Utils.logStream);
            throw new RuntimeException(t);
        }
    }

    public int getWebResponseCode() {return webResponseCode;}
    
    private void checkContentDataAfterDeployment(String contentData) {
        checkPersonDropDownListDataAfterDeployment(contentData);
        checkTripTableDataAfterDeployment(contentData);
        checkTextFieldDataAfterDeployment(contentData, TestPropertiesHandler.getTestProperty(
            "ID_TextField_DataBinding"), "1");
        checkTextFieldDataAfterDeployment(contentData, TestPropertiesHandler.getTestProperty(
            "ID_TextField_Binding_DataProvider"), "1");
    }
    
    private void checkPersonDropDownListDataAfterDeployment(String contentData) {
        String dropDownID = TestPropertiesHandler.getTestProperty(
            "ID_DropDownList_For_DBTablePerson");
        String pattern = getPatternPersonDropDownListDataAfterDeployment(dropDownID);
        boolean result = Pattern.matches(pattern, contentData);
        if (!result) {
            throw new RuntimeException("Content data, related to Drop Down List [" + dropDownID + "], " +
                "don't match the pattern: [" + pattern + "]");
        }
        Utils.logMsg("Content data, related to Drop Down List [" + dropDownID + "], " +
            "match the pattern: [" + pattern + "]");
    }

    private String getPatternPersonDropDownListDataAfterDeployment(String dropDownID) {
        String patternPrefix = null, patternPostfix = null;
        if (Utils.isUsedJ2EELevel_5()) {
            patternPrefix = PATTERN_PREFIX_DEPLOYMENT_DATA_J2EE_5_PERSON_DROPDOWNLIST;
            patternPostfix = PATTERN_POSTFIX_DEPLOYMENT_DATA_J2EE_5_PERSON_DROPDOWNLIST;
        } else if (Utils.isUsedJ2EELevel_14()) {
            patternPrefix = PATTERN_PREFIX_DEPLOYMENT_DATA_J2EE_14_PERSON_DROPDOWNLIST;
            patternPostfix = PATTERN_POSTFIX_DEPLOYMENT_DATA_J2EE_14_PERSON_DROPDOWNLIST;
        }
        if ((Utils.isStringEmpty(patternPrefix)) || 
            (Utils.isStringEmpty(patternPostfix))) {
            return null;
        }
        return (patternPrefix + dropDownID + patternPostfix);
    }
    
    private void checkTripTableDataAfterDeployment(String contentData) {
        Set<TableRowData> tripDataSet = getTripTableData(contentData);
        Utils.logMsg("+++ Trip Table Data = " + tripDataSet);
        
        Set<TableRowData> controlTripDataSet = getControlTripTableData();
        Utils.logMsg("+++ Control Trip Table Data = " + controlTripDataSet);
        
        boolean result = controlTripDataSet.equals(tripDataSet);
        if (!result) {
            throw new RuntimeException("Trip Table Data are not equal to Control Trip Table Data");
        }
        Utils.logMsg("Trip Table Data are equal to Control Trip Table Data");
    }
    
    private Set<TableRowData> getTripTableData(String contentData) {
        if (Utils.isUsedJ2EELevel_5()) {
            return getTripTableData_J2EELevel_5(contentData);
        } else if (Utils.isUsedJ2EELevel_14()) {
            return getTripTableData_J2EELevel_14(contentData);
        } else {
            return null;
        }
    }
    
    private Set<TableRowData> getTripTableData_J2EELevel_14(String contentData) {
        Set<TableRowData> tripDataSet = new TreeSet<TableRowData>();
                
        for (int i = 0; i < AMOUNT_TRIPS_PERSON_ID_1; ++i) {
            TableRowData tableRowData = new TableRowData();
            int posRowGroup = contentData.indexOf(WEB_PAGE_TABLE_ROW_GROUP_PREFIX + i);
            int posClosingTagSpan = posRowGroup;
            for (int column = 0; column < 3; ++column) {
                posClosingTagSpan = contentData.indexOf(WEB_PAGE_CLOSING_TAG_SPAN,
                    ++posClosingTagSpan);
                int posLastTagChar = contentData.lastIndexOf(WEB_PAGE_LAST_TAG_CHAR,
                    posClosingTagSpan);
                
                String data = contentData.substring(posLastTagChar + 1, posClosingTagSpan).trim();
                
                if (column == 0) tableRowData.setDepDate(data);
                if (column == 1) tableRowData.setDepCity(data);
                if (column == 2) tableRowData.setDestCity(data);
                
                tripDataSet.add(tableRowData);
            }
        }
        return tripDataSet;
    }
    
    private Set<TableRowData> getTripTableData_J2EELevel_5(String contentData) {
        Set<TableRowData> tripDataSet = new TreeSet<TableRowData>();
                
        int posComma = 0;
        for (int i = 0; i < AMOUNT_TRIPS_PERSON_ID_1; ++i) {
            TableRowData tableRowData = new TableRowData();
            for (int column = 0; column < 3; ++column) {
                int posRowGroup = contentData.indexOf(WEB_PAGE_TABLE_ROW_GROUP_ID_PREFIX + i, 
                    posComma);
                int posValue = contentData.indexOf(WEB_PAGE_COMPONENT_VALUE_PREFIX, posRowGroup);
                posComma = contentData.indexOf(",", posValue);
                
                String data = contentData.substring(posValue, posComma).replace(
                    WEB_PAGE_COMPONENT_VALUE_PREFIX, "").replace("\"", "").replace(":", "").trim();
                
                if (column == 0) tableRowData.setDepDate(data);
                if (column == 1) tableRowData.setDepCity(data);
                if (column == 2) tableRowData.setDestCity(data);
                
                tripDataSet.add(tableRowData);
            }
        }
        return tripDataSet;
    }

    private Set<TableRowData> getControlTripTableData() {
        Set<TableRowData> controlTripDataSet = new TreeSet<TableRowData>();
        String year = TestPropertiesHandler.getDatabaseProperty("DB_Data_Year");
        controlTripDataSet.add(new TableRowData("16.06." + year, "Oakland", "New York"));
        controlTripDataSet.add(new TableRowData("14.09." + year, "San Francisco", "New York"));
        controlTripDataSet.add(new TableRowData("22.10." + year, "Oakland", "Toronto"));
        controlTripDataSet.add(new TableRowData("23.11." + year, "San Francisco", "Tokyo"));
        controlTripDataSet.add(new TableRowData("12.12." + year, "San Francisco", "Chicago"));
        return controlTripDataSet;
    }

    private void checkTextFieldDataAfterDeployment(String contentData, 
        String textFieldID, String textFieldValue) {
        String textFieldData = getTextFieldData(contentData, textFieldID);
        boolean result = textFieldData.equals(textFieldValue);
        if (!result) {
            throw new RuntimeException("Value [" + textFieldData + "] of text field [" + 
            textFieldID + "] doesn't equal to control value [" + textFieldValue + "]");
        }
        Utils.logMsg("+++ Value of text field [" + 
            textFieldID + "] equals to control value [" + textFieldValue + "]");
    }
    
    private String getTextFieldData(String contentData, String textFieldID) {
        if (Utils.isUsedJ2EELevel_5()) {
            return getTextFieldData_J2EELevel_5(contentData, textFieldID);
        } else if (Utils.isUsedJ2EELevel_14()) {
            return getTextFieldData_J2EELevel_14(contentData, textFieldID);
        } else {
            return null;
        }
    }
    
    private String getTextFieldData_J2EELevel_14(String contentData, String textFieldID) {
        int posTextFieldID = contentData.indexOf(WEB_PAGE_COMPONENT_ID_J2EE14_PREFIX + "\"" +
            WEB_PAGE_FORM_PREFIX + textFieldID),
            posValue = contentData.indexOf(WEB_PAGE_COMPONENT_VALUE_PREFIX, posTextFieldID),
            posCommaFirst = contentData.indexOf("\"", posValue),
            posCommaLast = contentData.indexOf("\"", posCommaFirst + 1);

        String data = contentData.substring(posCommaFirst, posCommaLast).replace(
            "\"", "").trim();
        return data;
    }
    
    private String getTextFieldData_J2EELevel_5(String contentData, String textFieldID) {
        int posTextFieldID = contentData.indexOf(WEB_PAGE_COMPONENT_ID_J2EE5_PREFIX + "\"" +
            WEB_PAGE_FORM_PREFIX + textFieldID),
            posValue = contentData.indexOf(WEB_PAGE_COMPONENT_VALUE_PREFIX, posTextFieldID),
            posComma = contentData.indexOf(",", posValue);
        
        String data = contentData.substring(posValue, posComma).replace(
            WEB_PAGE_COMPONENT_VALUE_PREFIX, "").replace("\"", "").replace(":", "").trim();
        return data;
    }
    
    private WebResponse changePersonDropDownListValue(WebResponse response, String newValue) throws Throwable {
        String dropDownID = TestPropertiesHandler.getTestProperty(
            "ID_DropDownList_For_DBTablePerson");
        WebForm webForm = response.getForms()[0];
        
        // change value of Drop Down List "personDD"

        WebResponse newResponse = webForm.submitNoButton(); //webForm.submit();
        String contentData = response.getText();
        Utils.logMsg("+++ Web response after changing DropDownList value: " +
            "content type = [" + response.getContentType() + "], " +
            "response code = [" + response.getResponseCode() + "], " +
            "content data = [" + contentData + "]");
        return newResponse;
    }
    //========================================================================//
    private class TableRowData implements Comparable {
        private String depDate = "", depCity = "", destCity = "";

        public TableRowData() {}
        public TableRowData(String depDate, String depCity, String destCity) {
            setDepDate(depDate);
            setDepCity(depCity);
            setDestCity(destCity);
        }
        @Override
        public boolean equals(Object obj) {
            boolean retValue;
            TableRowData newTableRowData = (TableRowData) obj;
            retValue = (this.toString().equals(newTableRowData.toString()));
            return retValue;
        }
        @Override
        public int hashCode() { // was generated by NetBeans Wizard (Insert Code)
            int hash = 3;
            hash = 97 * hash + (this.depDate != null ? this.depDate.hashCode() : 0);
            hash = 97 * hash + (this.depCity != null ? this.depCity.hashCode() : 0);
            hash = 97 * hash + (this.destCity != null ? this.destCity.hashCode() : 0);
            return hash;
        }
        
        public int compareTo(Object obj) {
            int retValue;
            TableRowData newTableRowData = (TableRowData) obj;
            retValue = (this.toString().compareTo(newTableRowData.toString()));
            return retValue;
        }
        
        public String getDepCity() {return depCity;}
        public void setDepCity(String depCity) {
            this.depCity = depCity.replace("\"", "").trim();
        }
        public String getDepDate() {return depDate;}
        public void setDepDate(String depDate) {
            this.depDate = depDate.replace("\"", "").trim();
        }
        public String getDestCity() {return destCity;}
        public void setDestCity(String destCity) {
            this.destCity = destCity.replace("\"", "").trim();
        }

        @Override
        public String toString() {
            String retValue;
            retValue = "DepDate: [" + getDepDate() + "], " +
                       "DepCity: [" + getDepCity() + "], " +
                       "DestCity: [" + getDestCity() + "] ";
            return retValue;
        }
    }
}

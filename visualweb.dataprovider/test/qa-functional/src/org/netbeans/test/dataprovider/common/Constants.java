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
package org.netbeans.test.dataprovider.common;

public interface Constants {
    String
        TEST_LOG_FILE_PATH = System.getProperty("xtest.workdir") + "/..",
        TEST_LOG_FILE_NAME = "test_log.txt",
        
        MAIN_MENU_ITEM_WINDOW_SAVE_ALL = "File|Save All",
        MAIN_MENU_ITEM_WINDOW = "Window",
        MAIN_MENU_ITEM_EDIT_COPY = "Edit|Copy",
        MAIN_MENU_ITEM_WINDOW_SERVICES = MAIN_MENU_ITEM_WINDOW + "|Services",
        MAIN_MENU_ITEM_WINDOW_PALETTE = MAIN_MENU_ITEM_WINDOW + "|Palette",
        MAIN_MENU_ITEM_WINDOW_NAVIGATOR = MAIN_MENU_ITEM_WINDOW + "|Navigating|Navigator",
        MAIN_MENU_ITEM_WINDOW_PROPERTIES = MAIN_MENU_ITEM_WINDOW + "|Properties",
        MAIN_MENU_ITEM_WINDOW_PROJECTS = MAIN_MENU_ITEM_WINDOW + "|Projects",
        MAIN_MENU_ITEM_WINDOW_CLOSE_WINDOW = MAIN_MENU_ITEM_WINDOW + "|Close Window",
        MAIN_MENU_ITEM_WINDOW_RESET_WINDOWS = MAIN_MENU_ITEM_WINDOW + "|Reset Windows",
        MAIN_MENU_ITEM_SOURCE_FIX_IMPORTS = "Source|Fix Imports",           
        MAIN_MENU_ITEM_TOOLS_JAVA_DB_DATABASE_SETTINGS = "Tools|Java DB Database|Settings",           
    
        WINDOW_NAVIGATOR_TITLE = "Navigator",    
        WINDOW_PROPERTIES_TITLE = "Properties",    
        
        PROJECT_CATEGORY_WEB="Web",
        PROJECT_TYPE_WEB_APP="Web Application",
       
        JAVA_EDITOR_TITLE = "Page1.java",
            
        WEB_PAGE_FORM_NAME = "form1",
        WEB_PAGE_FORM_PREFIX = WEB_PAGE_FORM_NAME + ":",
            
        WEB_PAGE_COMPONENT_ID_J2EE5_PREFIX = "\"id\":",
        WEB_PAGE_COMPONENT_ID_J2EE14_PREFIX = "id=",
        
        WEB_PAGE_TABLE_ROW_GROUP_PREFIX = WEB_PAGE_FORM_PREFIX + "table1:tableRowGroup1:",
        WEB_PAGE_TABLE_ROW_GROUP_ID_PREFIX = WEB_PAGE_COMPONENT_ID_J2EE5_PREFIX + "\"" + 
                                             WEB_PAGE_TABLE_ROW_GROUP_PREFIX,
        
        WEB_PAGE_COMPONENT_VALUE_PREFIX = "value",
        WEB_PAGE_CLOSING_TAG_SPAN = "</span>",
        WEB_PAGE_LAST_TAG_CHAR = ">",
            
        NAVIGATOR_COMBOBOX_ITEM_NAVIGATOR = "Navigator",
        NAVIGATOR_COMBOBOX_ITEM_OUTLINE = "Outline",
        NAVIGATOR_TREE_NODE_PAGE_PREFIX = "Page1|",
        NAVIGATOR_TREE_NODE_SESSION_PREFIX = "SessionBean1|",
        NAVIGATOR_TREE_NODE_FORM_PREFIX = NAVIGATOR_TREE_NODE_PAGE_PREFIX + "page1|html1|body1|" + 
            WEB_PAGE_FORM_NAME + "|",
        NAVIGATOR_TREE_NODE_TABLE1_ROWGROUP_PREFIX = NAVIGATOR_TREE_NODE_FORM_PREFIX + 
            "table1|tableRowGroup1",
            
        SERVICES_TREE_NODE_SERVERS = "Servers",
        SERVICES_TREE_NODE_APPLICATIONS = "Applications",
        SERVICES_TREE_NODE_WEB_APPLICATIONS = "Web Applications",
        SERVICES_TREE_NODE_DATABASES = "Databases",
            
        DB_TREE_NODE_DRIVERS = "Drivers",
        DB_TREE_NODE_ORACLE_JDBC_DRIVER_NAME = "Oracle",
        DB_TREE_NODE_DERBY_JDBC_DRIVER_NAME = "Java DB (Network)",
        DB_TREE_NODE_TABLES = "Tables",

        DB_NAME_DERBY = "Derby",
        DB_NAME_ORACLE = "Oracle",
        DB_NAME_MYSQL = "MySQL",
        DB_NAME_POSTGRES = "Postgres",
            
        DB_SCHEMA_NAME_TRAVEL = "TRAVEL",
        DB_TABLE_PERSON = "PERSON",
        DB_TABLE_TRIP = "TRIP",
        DB_TABLE_TRIPTYPE = "TRIPTYPE",
        DB_TABLE_VALIDATION_TABLE = "VALIDATION_TABLE",
            
        DB_TABLE_TRIP_TRIPID = "TRIPID",
        DB_TABLE_TRIP_PERSONID = "PERSONID",
        DB_TABLE_TRIP_DEPDATE = "DEPDATE",
        DB_TABLE_TRIP_TRIPTYPEID = "TRIPTYPEID",
        DB_TABLE_TRIP_LASTUPDATED = "LASTUPDATED",
        DB_TABLE_TRIP_DEPCITY = "DEPCITY",
        DB_TABLE_TRIP_DESTCITY = "DESTCITY",
            
        DB_POPUP_MENU_ITEM_LABEL_CONNECT = "Connect",
        DB_POPUP_MENU_ITEM_LABEL_NEW_DRIVER = "New Driver",
        DB_POPUP_MENU_ITEM_LABEL_NEW_CONNECTION = "New Connection",
            
        DB_DIALOG_CONNECT_TITLE = "Connect",
        DB_DIALOG_NEW_DRIVER_TITLE = "New JDBC Driver",
        DB_DIALOG_NEW_CONNECTION_TITLE = "New Database Connection",
        DB_DIALOG_JAV_DB_SETTINGS_TITLE = "Java DB Settings",
    
        PALETTE_NAME_BASIC = "Basic",
        PALETTE_NAME_CONVERTERS = "Converters",

        COMPONENT_BUTTON_NAME = "Button",
        COMPONENT_DROP_DOWN_LIST_NAME = "Drop Down List",
        COMPONENT_TABLE_NAME = "Table",    
        COMPONENT_TEXT_FIELD_NAME = "Text Field",    

        COMPONENT_SQL_TIMESTAMP_CONVERTER_NAME = "SQL Timestamp Converter",    
        SQL_TIMESTAMP_CONVERTER_ID = "sqlTimestampConverter1",    
        SQL_TIMESTAMP_CONVERTER_PATTERN = "dd.MM.yyyy",    
            
        POPUP_MENU_ITEM_AUTO_SUBMIT = "Auto-Submit on Change",
        POPUP_MENU_ITEM_CONFIGURE_VIRTUAL_FORMS = "Configure Virtual Forms",
        POPUP_MENU_ITEM_EDIT_SQL_STATEMENT = "Edit SQL Statement",
        POPUP_MENU_ITEM_PROCESS_VALUE_CHANGE = "Edit Event Handler|processValueChange",           
        POPUP_MENU_ITEM_TABLE_LAYOUT = "Table Layout",           
        POPUP_MENU_ITEM_BIND_DATA = "Bind to Data",
        POPUP_MENU_ITEM_DESIGN_QUERY = "Design Query",           
        POPUP_MENU_ITEM_START = "Start",           
        POPUP_MENU_ITEM_STOP = "Stop",           
        POPUP_MENU_ITEM_REFRESH = "Refresh",           
        POPUP_MENU_ITEM_RUN = "Run",           
        POPUP_MENU_ITEM_UNDEPLOY = "Undeploy",           
        POPUP_MENU_ITEM_CLOSE = "Close",           
        POPUP_MENU_ITEM_PASTE = "Paste",           
            
        DIALOG_TITLE_CONFIGURE_VIRTUAL_FORMS = "Configure Virtual Forms",    
        DIALOG_TITLE_TABLE_LAYOUT = "Table Layout",           
        DIALOG_TITLE_BIND_DATA = "Bind to Data",
        DIALOG_TITLE_ADD_NEW_DATAPROVIDER_WITH_ROWSET = "Add New Data Provider with RowSet",           
        DIALOG_TITLE_SQL_PARSING_ERROR = "Parse Error",           
            
        BUTTON_LABEL_ADD = "Add",
        BUTTON_LABEL_OPEN = "Open",
        BUTTON_LABEL_NEW = "New",
        BUTTON_LABEL_OK = "OK",
        BUTTON_LABEL_CANCEL = "Cancel",
        BUTTON_LABEL_TO_LEFT = "<",
            
        PROPERTY_NAME_ID = "id",
        PROPERTY_NAME_STYLE = "style",
        PROPERTY_NAME_COMMAND = "command",
        PROPERTY_NAME_PATTERN = "pattern",
        PROPERTY_NAME_CONVERTER = "converter",
            
        ROW_SET_SUFFIX = "RowSet",
        DATA_PROVIDER_SUFFIX = "DataProvider",
            
        METHOD_DECLARATION_PRERENDER = "public void prerender() {",
            
        VIRTUAL_FORM_YES = "Yes",    
        VIRTUAL_FORM_NO = "No",

        PROPERTY_NAME_SERVER_SETTINGS = "server_settings",
        PROPERTY_NAME_DATABASE_SETTINGS = "database_settings",
        
        J2EE_LEVEL_14 = "J2EE 1.4",
        J2EE_LEVEL_5  = "Java EE 5",
        J2EE_LEVEL_14_COMPATIBILITY_KIT = "Visual Web JSF Backwards Compatibility Kit",
        
        LEFT_ROUND_BRACKET  = "(",
        RIGHT_ROUND_BRACKET = ")",
        LEFT_CURLY_BRACKET  = "{",
        RIGHT_CURLY_BRACKET = "}",
        LEFT_ANGLE_BRACKET  = "<",
        RIGHT_ANGLE_BRACKET = ">",
        EQUAL_SIGN = "=",
        
        PATTERN_ANY_CHARS = "[\\u0000-\\uffff]*",
        
        PROP_SPEC_CHAR_SLASH = "\\",
        PROP_SPEC_CHAR_DOUBLE_PERCENT = "%%",
        
        PROP_NAME_PATTERN = "[[\\s]*[\\w\\-]*[\\s]*]",
        PROP_VALUE_PATTERN = "[[\\s]*[%@\\*;><:/ \\w\\.\\-]*[\\s]*]",
        PROP_DATA_PATTERN = "[" + PROP_NAME_PATTERN + "=" + PROP_VALUE_PATTERN + "]",
        PROP_ITEM_PATTERN = "[\\{" + PROP_DATA_PATTERN + "\\}]",
        PROP_ITEM_LIST_PATTERN = "[\\{" + PROP_DATA_PATTERN + "\\}]+",
        PROP_ITEM_SET_PATTERN = "[[\\s]*" + PROP_ITEM_PATTERN + "[\\s]*[,]?[\\s]*&&[^\\(\\)]]+",
        PROP_SETTINGS_PATTERN = "\\(" + PROP_ITEM_SET_PATTERN + "\\)",
        
        // using: PATTERN_PREFIX + dropDownID + PATTERN_POSTFIX
        PATTERN_PREFIX_DEPLOYMENT_DATA_J2EE_5_PERSON_DROPDOWNLIST = 
            PATTERN_ANY_CHARS + 
            "<script type=\"text/javascript\">" + PATTERN_ANY_CHARS + 
            WEB_PAGE_COMPONENT_ID_J2EE5_PREFIX + "\"" + WEB_PAGE_FORM_PREFIX,
        PATTERN_POSTFIX_DEPLOYMENT_DATA_J2EE_5_PERSON_DROPDOWNLIST = 
            "\"" + PATTERN_ANY_CHARS +
            "\"options\":\\[\\{\"selected\":true,\"" + PATTERN_ANY_CHARS + "\"label\":\"Able, Tony\"" + PATTERN_ANY_CHARS +   
            "\\{\"selected\":false,\"" + PATTERN_ANY_CHARS + "\"label\":\"Black, John\"" + PATTERN_ANY_CHARS +   
            "\\{\"selected\":false,\"" + PATTERN_ANY_CHARS + "\"label\":\"Kent, Richard\"" + PATTERN_ANY_CHARS +   
            "\\{\"selected\":false,\"" + PATTERN_ANY_CHARS + "\"label\":\"Chen, Larry\"" + PATTERN_ANY_CHARS +   
            "\\{\"selected\":false,\"" + PATTERN_ANY_CHARS + "\"label\":\"Donaldson, Sue\"" + PATTERN_ANY_CHARS +   
            "\\{\"selected\":false,\"" + PATTERN_ANY_CHARS + "\"label\":\"Murrell, Tony\"" + PATTERN_ANY_CHARS +   
            "</script>" +
            PATTERN_ANY_CHARS,
        
        // using: PATTERN_PREFIX + dropDownID + PATTERN_POSTFIX
        PATTERN_PREFIX_DEPLOYMENT_DATA_J2EE_14_PERSON_DROPDOWNLIST =             
            PATTERN_ANY_CHARS +
            "<select" + PATTERN_ANY_CHARS + 
            WEB_PAGE_COMPONENT_ID_J2EE14_PREFIX + "\"" + WEB_PAGE_FORM_PREFIX,
        PATTERN_POSTFIX_DEPLOYMENT_DATA_J2EE_14_PERSON_DROPDOWNLIST = 
            "\"" + PATTERN_ANY_CHARS +
            "<option" + PATTERN_ANY_CHARS + "selected=\"selected\">Able, Tony</option>" + PATTERN_ANY_CHARS +   
            "<option" + PATTERN_ANY_CHARS + "Black, John</option>" + PATTERN_ANY_CHARS +   
            "<option" + PATTERN_ANY_CHARS + "Kent, Richard</option>" + PATTERN_ANY_CHARS +   
            "<option" + PATTERN_ANY_CHARS + "Chen, Larry</option>" + PATTERN_ANY_CHARS +   
            "<option" + PATTERN_ANY_CHARS + "Donaldson, Sue</option>" + PATTERN_ANY_CHARS +   
            "<option" + PATTERN_ANY_CHARS + "Murrell, Tony</option>" + PATTERN_ANY_CHARS +   
            "</select>" +
            PATTERN_ANY_CHARS,
            
        GOLDEN_FILE_LINE_SEPARATOR = "#";
    
    int
        VIRTUAL_FORM_COL_NAME = 1,    
        VIRTUAL_FORM_COL_PARTICIPATE = 2,    
        VIRTUAL_FORM_COL_SUBMIT = 3,
        
        // amount of trips for person with smallest ID (id = 1, Able Tony)
        AMOUNT_TRIPS_PERSON_ID_1 = 5, 
           
        WEB_RESPONSE_CODE_OK = 200;
}

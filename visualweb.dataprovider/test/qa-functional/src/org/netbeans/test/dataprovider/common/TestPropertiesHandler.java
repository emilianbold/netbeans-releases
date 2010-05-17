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

import java.util.regex.*;
import java.util.*;
import java.io.*;

public class TestPropertiesHandler implements Constants {
    private static Properties testProperties;
    private static List<Properties> listServerProperties, listDatabaseProperties;
    private static int serverPropertiesIndex = 0, databasePropertiesIndex = 0;

    public static String getTestProperty(String propertyKey) {
        return testProperties.getProperty(propertyKey);
    }
    public static String getServerProperty(String propertyKey) {
        //Utils.logMsg("serverPropertiesIndex = " + serverPropertiesIndex);
        return listServerProperties.get(serverPropertiesIndex).getProperty(propertyKey);
    }
    public static String getDatabaseProperty(String propertyKey) {
        //Utils.logMsg("databasePropertiesIndex = " + databasePropertiesIndex);
        return listDatabaseProperties.get(databasePropertiesIndex).getProperty(propertyKey);
    }
    
    public static void readTestProperties() {
        testProperties = getTestWorkdirProperties();
        printProperties(testProperties);
        printProperties(System.getProperties());
        listServerProperties = handleServerProperties(
            testProperties.getProperty(PROPERTY_NAME_SERVER_SETTINGS), 
            testProperties);
        listDatabaseProperties = handleDatabaseProperties(
            testProperties.getProperty(PROPERTY_NAME_DATABASE_SETTINGS),
            listServerProperties, testProperties);
        resetPropertiesIndexes();
    }

    public static List<String> getTestMethodNames(String[] baseTestMethodNames) {
        if (listServerProperties.isEmpty()) {
            throw new RuntimeException("Server properties aren't defined");
        }
        if (listDatabaseProperties.isEmpty()) {
            throw new RuntimeException("Database properties aren't defined");
        }
        List<String> methodNames = new ArrayList<String>();
        do {
            for (String methodName : baseTestMethodNames) {
                methodNames.add(methodName);
            }
        } while (nextTestProperties());
        resetPropertiesIndexes();
        return methodNames;
    }
    
    public static void resetPropertiesIndexes() {
        databasePropertiesIndex = 0;
        serverPropertiesIndex = 0;
    }
    public static boolean nextTestProperties() {
        boolean isAllPropertiesHandled = false;
        ++databasePropertiesIndex;
        if (databasePropertiesIndex == listDatabaseProperties.size()) {
            databasePropertiesIndex = 0;
            ++ serverPropertiesIndex;
        }
        if (serverPropertiesIndex == listServerProperties.size()) {
            resetPropertiesIndexes();
            isAllPropertiesHandled = true;
        }
        return (! isAllPropertiesHandled);
    }
        
    public static List<Properties> handleServerProperties(String serverSettings, 
        Properties commonProperties) {
        List<String> listSeparatedProperties = separateServerProperties(serverSettings);
        List<Properties> listSettingsProperties = parseServerProperties(listSeparatedProperties);
        List<Properties> listResolvedProperties = resolveServerProperties(
            listSettingsProperties, commonProperties);
        
        Utils.logMsg("=== Server Settings: List of Properties ===");
        for (Properties resolvedProperties : listResolvedProperties) {
            printProperties(resolvedProperties);
            Utils.logMsg("-------------------------------------------");
        }
        return listResolvedProperties;
    }
    
    private static List<String> separateServerProperties(String serverSettings) {
        return separateSettingsProperties(serverSettings);
    }
    
    private static List<Properties> parseServerProperties(List<String> listSettingsProperties) {
        return parseItemsProperties(listSettingsProperties);
    }

    private static List<Properties> resolveServerProperties(
        List<Properties> listUnresolvedProperties, Properties commonTestProperties) {
        List<Properties> listRefProperties = new ArrayList<Properties>();
        listRefProperties.add(System.getProperties());
        listRefProperties.add(commonTestProperties);
        return resolveProperies(listUnresolvedProperties, listRefProperties);
    }
    
    public static List<Properties> handleDatabaseProperties(String databaseSettings, 
        List<Properties> listServerProperties, Properties commonTestProperties) {
        List<String> listSeparatedProperties = separateDatabaseProperties(databaseSettings);
        List<Properties> listSettingsProperties = parseDatabaseProperties(listSeparatedProperties);
        List<Properties> listResolvedProperties = resolveDatabaseProperties(
            listSettingsProperties, listServerProperties, commonTestProperties);
        
        Utils.logMsg("=== Database Settings: List of Properties ===");
        for (Properties resolvedProperties : listResolvedProperties) {
            printProperties(resolvedProperties);
            Utils.logMsg("---------------------------------------------");
        }
        return listResolvedProperties;
    }
    
    private static List<String> separateDatabaseProperties(String databaseSettings) {
        return separateSettingsProperties(databaseSettings);
    }
    
    private static List<Properties> parseDatabaseProperties(List<String> listSettingsProperties) {
        return parseItemsProperties(listSettingsProperties);
    }

    private static List<Properties> resolveDatabaseProperties(List<Properties> listUnresolvedProperties,
        List<Properties> listServerProperties, Properties commonTestProperties) {
        List<Properties> listRefProperties = new ArrayList<Properties>();
        listRefProperties.add(System.getProperties());
        listRefProperties.add(commonTestProperties);
        listRefProperties.addAll(listServerProperties);
        return resolveProperies(listUnresolvedProperties, listRefProperties);
    }
    
    private static List<String> separateSettingsProperties(String dataSettings) {
        dataSettings = dataSettings.replace(PROP_SPEC_CHAR_SLASH, PROP_SPEC_CHAR_DOUBLE_PERCENT);
        //Utils.debugOutput("+++ modified dataSettings = [" + dataSettings + "]");
        
        String propertyPattern = PROP_SETTINGS_PATTERN;
        //Utils.debugOutput("+++ propertyPattern = [" + propertyPattern + "]");
        
        List<String> propDataList = new ArrayList<String>();
        try {
            Matcher matcher = (Pattern.compile(propertyPattern)).matcher(dataSettings);
            while(matcher.find()) {
                String data = matcher.group();
                propDataList.add(data);
                //Utils.debugOutput("\t+++ data = " + data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        //Utils.debugOutput("\t+++ propDataList = " + propDataList);
        return propDataList;
    }
    
    private static List<Properties> parseItemsProperties(List<String> listItemData) {
        String propertyPattern = PROP_ITEM_LIST_PATTERN;
        //Utils.debugOutput("+++ propertyPattern = " + propertyPattern);

        List<Properties> propDataList = new ArrayList<Properties>();
        try {
            for (String itemData : listItemData) {
                Properties itemsProperties = new Properties();
                
                //Utils.debugOutput("+++ itemData = " + itemData);
                Matcher matcher = (Pattern.compile(propertyPattern)).matcher(itemData);
                while(matcher.find()) {
                    String data = matcher.group();
                    //Utils.debugOutput("\t+++ data = " + data);
                    addItemProperties(data, itemsProperties);
                }
                propDataList.add(itemsProperties);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        //Utils.debugOutput("\t+++ propDataList = " + propDataList);

        for (Properties propData : propDataList) {
            Enumeration enumPropNames = propData.propertyNames();
            while (enumPropNames.hasMoreElements()) {
                String propName = (String) enumPropNames.nextElement(),
                       propValue = propData.getProperty(propName);
                propData.setProperty(propName, propValue.replace(
                    PROP_SPEC_CHAR_DOUBLE_PERCENT, PROP_SPEC_CHAR_SLASH));
            }
        }
        //Utils.debugOutput("\t+++ modified propDataList = " + propDataList);
        return propDataList;
    }

    private static void addItemProperties(String itemData, Properties itemsProperties) {
        itemData = itemData.replace(LEFT_CURLY_BRACKET, "").replace(RIGHT_CURLY_BRACKET, "").trim();
        int equalPos = itemData.indexOf(EQUAL_SIGN);
        if (equalPos == 0) {
            throw new RuntimeException("Property data [" + itemData + "] is incorrect: " +
            "it doesn't contain the property name before the character [" + EQUAL_SIGN + "]");
        }
        if (equalPos > -1) {
            String name = itemData.substring(0, equalPos).trim();
            String value =(equalPos == itemData.length() - 1 ? "" : 
                itemData.substring(equalPos + 1).trim());
            //Utils.debugOutput("equalPos = " + equalPos, "name = [" + name + "]", "value = [" + value + "]");
            itemsProperties.setProperty(name, value);
        } else {
            throw new RuntimeException("Property data [" + itemData + "] is incorrect: " +
            "it must contain the character [" + EQUAL_SIGN + "]");
        }
    }
    
    private static List<Properties> resolveProperies(List<Properties> listHandledProperties, 
        List<Properties> listRefProperies) {
        for (Properties modifiedProperties : listHandledProperties) {
            Set<Map.Entry<Object, Object>> entrySet = modifiedProperties.entrySet();
            for (Map.Entry<Object, Object> mapEntry : entrySet) {
                String currentPpropertyValue = null, refKey = null;
                while ((refKey = getReferenceKey(
                        currentPpropertyValue = ((String) mapEntry.getValue()))) != null) {
                    String refValue = modifiedProperties.getProperty(refKey);
                    if (Utils.isStringEmpty(refValue)) {
                        for (Properties refProperties : listRefProperies) {
                            refValue = refProperties.getProperty(refKey);
                            if (! Utils.isStringEmpty(refValue)) break;
                        }
                    }
                    if (! Utils.isStringEmpty(refValue)) {
                        mapEntry.setValue(replaceReferenceValue(currentPpropertyValue, refValue));
                    } else {
                        throw new RuntimeException(mapEntry.getKey() + " = " + currentPpropertyValue + 
                        ": reference value for reference key [" + refKey + "] isn't found");
                    }
                }
            }
        }
        return listHandledProperties;
    }

    private static String getReferenceKey(String value) {
        int leftBracketPos  = value.indexOf(LEFT_ANGLE_BRACKET),
            rightBracketPos = value.indexOf(RIGHT_ANGLE_BRACKET);
        if ((leftBracketPos < 0) && (rightBracketPos < 0)) return null;
        
        String refKey = value.substring(leftBracketPos + 1, rightBracketPos).trim();
        return refKey;
    }
    
    private static String replaceReferenceValue(String value, String refValue) {
        int leftBracketPos  = value.indexOf(LEFT_ANGLE_BRACKET),
            rightBracketPos = value.indexOf(RIGHT_ANGLE_BRACKET);
        String newValue = value.substring(0, leftBracketPos) + refValue + 
            value.substring(rightBracketPos + 1);
        //Utils.debugOutput("value = " + value, "refValue = " + refValue, "newValue = " + newValue);
        return newValue;
    }

    public static Properties getTestWorkdirProperties() {
        Properties properties = null;
        String xtestWorkDir = System.getProperty("xtest.workdir");
        String[] propertyFileNames = new File(xtestWorkDir).list();
        for (String propertyFileName : propertyFileNames) {
            if (propertyFileName.toLowerCase().endsWith("properties")) {
                properties = addPropertiesFromFile(
                    xtestWorkDir + "/" + propertyFileName, properties);
            }
        }
        return properties;
    }
    
    public static Properties addPropertiesFromFile(String propertiesFilePath, 
        Properties baseProperties) {
        if (baseProperties == null) {
            baseProperties = new Properties();
        }
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFilePath));
        } catch (Exception e) {
            e.printStackTrace(Utils.logStream);
            return null;
        }
        if (! properties.isEmpty()) {
            for (Object key : properties.keySet()) {
                baseProperties.put(key, properties.getProperty((String) key));
            }
        }
        return baseProperties;
    }
    
    public static void printProperties(Properties properties) {
        printProperties(Utils.logStream, properties);
    }
    public static void printProperties(PrintStream out, Properties properties) {
        Enumeration propNames = properties.propertyNames();
        java.util.List<String> buf = new ArrayList<String>();
        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            buf.add(propName + " = [" + properties.getProperty(propName) + "]");
        }
        Utils.debugOutput(out, buf.toArray());
    }
}

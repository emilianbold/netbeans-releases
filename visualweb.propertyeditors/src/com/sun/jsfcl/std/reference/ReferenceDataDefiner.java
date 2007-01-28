/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.jsfcl.std.reference;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import com.sun.rave.designtime.DesignProperty;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class ReferenceDataDefiner {

    /*
         public static List stringListFromString(String string) {

        List result = new ArrayList(16);
        if (string == null) {
            return result;
        }
        StringReader stringReader = new StringReader(string);
        BufferedReader reader = new BufferedReader(stringReader);
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                result.add(line);
            }
        } catch (IOException e) {
            assert ReferenceDataManager.loggerUtil.warning("Got IO error, should never really happen", e); // NOI18N
        }
        return result;
         }*/

    /*
         public static String stringListToString(List strings) {

        StringWriter stringWriter = new StringWriter(1024);
        BufferedWriter writer = new BufferedWriter(stringWriter);
        Iterator iterator = strings.iterator();
        try {
            while (iterator.hasNext()) {
                String string = (String) iterator.next();
                writer.write(string);
                writer.newLine();
            }
            writer.flush();
            stringWriter.flush();
        } catch (IOException e) {
            assert ReferenceDataManager.loggerUtil.warning("Got IO error, should never really happen", e); // NOI18N
        }
        return stringWriter.toString();
         }*/

    public void addBaseItems(List list) {
    }

    public void addDesignPropertyItems(DesignProperty liveProperty, List list) {
    }

    public void addProjectItems(String itemsString, List list) {

        if (!definesProjectItems()) {
            return;
        }
        if (itemsString == null) {
            return;
        }

        StringReader stringReader;
        BufferedReader reader;
        ReferenceDataItem item;

        stringReader = new StringReader(itemsString);
        reader = new BufferedReader(stringReader);
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                item = newItem(line, line, null, false, true);
                list.add(item);
            }
        } catch (IOException e) {
            assert ReferenceDataManager.loggerUtil.warning(
                "Got IO error, should never really happen", e); // NOI18N
        }
    }

    public abstract boolean canAddRemoveItems();
    
    public boolean canOrderItems(){
        return true;
    }

    public ReferenceDataItem newItem(String name, String value, boolean isUnsetMarker,
        boolean isRemoveable) {

        return newItem(name, value, null, isUnsetMarker, isRemoveable, null);
    }

    public boolean definesBaseItems() {

        return true;
    }

    public boolean definesDesignPropertyItems() {

        return false;
    }

    public boolean definesProjectItems() {

        return canAddRemoveItems();
    }

    public String getProjectItemsString(List items) {
        StringWriter stringWriter;
        BufferedWriter writer;
        Iterator iterator;

        stringWriter = new StringWriter(1024);
        writer = new BufferedWriter(stringWriter);
        iterator = items.iterator();
        try {
            while (iterator.hasNext()) {
                ReferenceDataItem item;

                item = (ReferenceDataItem)iterator.next();
                writer.write(item.getName());
                writer.newLine();
            }
            writer.flush();
            stringWriter.flush();
        } catch (IOException e) {
            assert ReferenceDataManager.loggerUtil.warning(
                "Got IO error, should never really happen", e); // NOI18N
        }
        return stringWriter.toString();
    }

    public abstract boolean isValueAString();

    public ReferenceDataItem newItem(String name, Object value, String javaInitializationString,
        boolean isUnsetMarker, boolean isRemoveable) {

        return newItem(name, value, javaInitializationString, isUnsetMarker, isRemoveable, null);
    }

    public ReferenceDataItem newItem(String name, Object value, String javaInitializationString,
        boolean isUnsetMarker, boolean isRemoveable, ReferenceDataItem aliasFor) {

        return new ReferenceDataItem(name, value, javaInitializationString, isUnsetMarker,
            isRemoveable, aliasFor);
    }

    public boolean shouldPersistProjectItems() {

        return true;
    }

    public boolean supportsDesignPropertyItems() {

        return false;
    }

    public boolean userMultipleColumnsToDisplay() {

        return false;
    }

}

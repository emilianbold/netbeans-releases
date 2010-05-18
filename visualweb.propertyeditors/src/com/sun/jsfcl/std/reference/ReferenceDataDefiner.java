/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.types.Time;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mt154047
 */
public final class DataTableMetadataFactory {
     static final HashMap<String, Class<?>> stringToClass = new HashMap<String, Class<?>>();
    static{
        stringToClass.put("byte", Byte.class); // NOI18N
        stringToClass.put("short", Short.class); // NOI18N
        stringToClass.put("int", Integer.class); //NOI18N
        stringToClass.put("long", Long.class); //NOI18N
        stringToClass.put("double", Double.class); //NOI18N
        stringToClass.put("float", Float.class); //NOI18N
        stringToClass.put("string", String.class); //NOI18N
        stringToClass.put("timestamp", Time.class); //NOI18N
    }
    
    static DataTableMetadata create(Map map){
        try{
            FileObject rootFolder = FileUtil.getConfigRoot();
            String columnsFolderPath = (String)map.get("columns");//NOI18N
            FileObject columnsFolder = rootFolder.getFileObject(columnsFolderPath);
            @SuppressWarnings("unchecked")
            List<Column> columns = (List<Column>)columnsFolder.getAttribute("instanceCreate");//NOI18N
            if (columns == null){
                return null;
            }
            return new DataTableMetadata((String)map.get("name"), columns, null);//NOI18N
        }catch(Throwable e){
            e.printStackTrace();
        }
        return null;
    }

    static List<Column> createColumnsList(Map<?, ?> map){
        try{
            FileObject rootFolder = FileUtil.getConfigRoot();
            String columnsFolderPath = (String)map.get("columns");//NOI18N
            FileObject columnsFolder = rootFolder.getFileObject(columnsFolderPath);
            FileObject[] columnFiles = columnsFolder.getChildren();
            List<Column> columns = new ArrayList<Column>();
            for (int i = 0; i < columnFiles.length; i++){
                Object obj = columnFiles[i].getAttribute("instanceCreate");//NOI18N
                if ((obj != null && obj instanceof Column)) {
                    columns.add((Column)obj);
                }

            }
            return columns;
        }catch(Throwable e){
            e.printStackTrace();
        }
        return null;
    }

    static Column createColumn(Map map){
        try{
            String name = getStringValue(map, "name");//NOI18N
            String shortName = getStringValue(map, "shortname");//NOI18N
            String expression = getStringValue(map, "expression");//NOI18N
            Class clazz;
            try {
                clazz = Class.forName(getStringValue(map, "class"));//NOI18N
            } catch (ClassNotFoundException ex) {
                clazz = stringToClass.get(getStringValue(map, "class")); //NOI18N
                if (clazz == null){
                    clazz = String.class;
                }
            }
            return new Column(name, clazz, shortName, shortName, expression);
        }catch(Throwable e){
            e.printStackTrace();
        }
        return null;
    }

     private static String getStringValue(Map map, String key) {
        return (String) map.get(key);

    }
}

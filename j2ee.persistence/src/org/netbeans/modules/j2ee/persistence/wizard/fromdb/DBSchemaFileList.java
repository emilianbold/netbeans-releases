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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class DBSchemaFileList {

    private final Map<FileObject,String> dbschema2DisplayName = new HashMap<FileObject,String>();
    private final List dbschemaList;

    public DBSchemaFileList(Project project, FileObject configFilesFolder) {
        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);

        // XXX this recursive search is a potential performance problem
        for (int i = 0; i < sourceGroups.length; i++) {
            searchRoot(sourceGroups[i].getRootFolder(), sourceGroups[i].getDisplayName());
        }

        if (configFilesFolder != null) {
            String configFilesDisplayName = NbBundle.getMessage(DBSchemaFileList.class, "LBL_Node_DocBase");
            searchRoot(configFilesFolder, configFilesDisplayName);
        }

        List tempDBSchemaList = new ArrayList(dbschema2DisplayName.keySet());
        Collections.sort(tempDBSchemaList, new DBSchemaComparator());

        dbschemaList = Collections.unmodifiableList(tempDBSchemaList);
    }

    private void searchRoot(FileObject root, String rootDisplayName) {
        Enumeration ch = root.getChildren(true);
        while (ch.hasMoreElements()) {
            FileObject f = (FileObject) ch.nextElement();
            if (f.getExt().equals(DBSchemaManager.DBSCHEMA_EXT) && !f.isFolder()) {
                if (!dbschema2DisplayName.containsKey(f)) {
                    String relativeParent = FileUtil.getRelativePath(root, f.getParent()) + File.separator;
                    if (relativeParent.startsWith("/")) { // NOI18N
                        relativeParent = relativeParent.substring(1);
                    }
                    String relative = relativeParent + f.getName();
                    String displayName = NbBundle.getMessage(DBSchemaFileList.class,
                            "LBL_SchemaLocation", rootDisplayName, relative);
                    dbschema2DisplayName.put(f, displayName);
                }
            }
        }
    }

    public List<FileObject> getFileList() {
        return dbschemaList;
    }

    public String getDisplayName(FileObject dbschemaFile) {
        return dbschema2DisplayName.get(dbschemaFile);
    }

    private final class DBSchemaComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            FileObject f1 = (FileObject)o1;
            FileObject f2 = (FileObject)o2;

            String displayName1 = dbschema2DisplayName.get(f1);
            String displayName2 = dbschema2DisplayName.get(f2);

            return displayName1.compareTo(displayName2);
        }
    }
}

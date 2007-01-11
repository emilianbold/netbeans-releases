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

package org.netbeans.upgrade.systemoptions;

import java.io.*;
import java.util.*;
import org.openide.filesystems.*;

/**
 *
 * @author Radek Matous
 */
public class Importer {
    private static final String DEFINITION_OF_FILES =  "systemoptionsimport";//NOI18N
            
    private static FileObject getRootOfSystemFileSystem() {
        return Repository.getDefault().getDefaultFileSystem().getRoot();
    }
    
    public static void doImport() throws IOException  {
        Set<FileObject> files = getImportFiles(loadImportFilesDefinition());
        for (Iterator<DefaultResult> it = parse(files).iterator(); it.hasNext();) {
            saveResult(it.next());
        }
        for (Iterator it = files.iterator(); it.hasNext();) {
            FileObject fo = (FileObject) it.next();
            FileLock fLock = fo.lock();
            try {
                fo.rename(fLock, fo.getName(), "imported");//NOI18N
            } finally {
                fLock.releaseLock();
            }
        }
    }
    
    private static void saveResult(final DefaultResult result) throws IOException {
        String absolutePath = "/"+result.getModuleName();
        PropertiesStorage ps = PropertiesStorage.instance(absolutePath);
        Properties props = ps.load();
        String[] propertyNames = result.getPropertyNames();
        for (int i = 0; i < propertyNames.length; i++) {
            props.put(propertyNames[i], result.getProperty(propertyNames[i]));
        }
        if (props.size() > 0) {
            ps.save(props);
        }
    }
    
    private static Set<DefaultResult> parse(final Set<FileObject> files) {
        Set<DefaultResult> retval = new HashSet<DefaultResult>();
        for (FileObject f: files) {
            try {
                retval.add(SystemOptionsParser.parse(f, false));
            } catch (ClassNotFoundException ex) {
                continue;
            } catch (IOException ex) {
                continue;
            }
        }
        return retval;
    }
    

    static Properties loadImportFilesDefinition() throws IOException {
        Properties props = new Properties();
        InputStream is = Importer.class.getResourceAsStream(DEFINITION_OF_FILES);
        try {
            props.load(is);
        } finally {
            is.close();
        }
        return props;
    }

    private static Set<FileObject> getImportFiles(final Properties props) {
        Set<FileObject> fileobjects = new HashSet<FileObject>();        
        for (Iterator it = props.keySet().iterator(); it.hasNext();) {
            String path = (String) it.next();
            FileObject f = getRootOfSystemFileSystem().getFileObject(path);
            if (f != null) {
                fileobjects.add(f);
            }
        }
        return fileobjects;
    }
    
    /** Creates a new instance of SettingsReadSupport */
    private Importer() {}    
}

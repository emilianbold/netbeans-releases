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
package org.netbeans.modules.vmd.api.model.utils;

import java.io.IOException;

import javax.swing.undo.UndoableEdit;

import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DocumentInterface;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Karol Harezlak
 */
public class ModelTestUtil {

    public final static String PRESENTER_1_ID = "PRESENTER1"; // NOI18N
    public final static String PRESENTER_2_ID = "PRESENTER2"; // NOI18N
    public final static String PRESENTER_3_ID = "PRESENTER3"; // NOI18N
    public final static String PRESENTER_4_ID = "PRESENTER4"; // NOI18N
    
    public final static String PROJECT_TYPE = "vmd"; //NOI18N
    public final static String FOLDER_PATH_COMPONENTS = "/components"; // NOI18N
    public final static String FOLDER_PATH_PRODUCERS = "/producers"; // NOI18N
    
    public final static String FIRST_FILE_NAME_CD = "org-netbeans-modules-vmd-api-model-descriptors-FirstCD.instance"; // NOI18N
    public final static String SECOND_FILE_NAME_CD = "org-netbeans-modules-vmd-api-model-descriptors-SecondCD.instance"; // NOI18N
    public final static String SUPER_FIRST_FILE_NAME_CD = "org-netbeans-modules-vmd-api-model-descriptors-SuperFirstCD.instance"; // NOI18N
    public final static String ENUM_FILE_NAME_CD = "org-netbeans-modules-vmd-api-model-descriptors-EnumCD.instance"; // NOI18N
    public final static String CANT_INSTANTIATE_FILE_NAME_CD = "org-netbeans-modules-vmd-api-model-descriptors-CantInstantiateCD.instance"; // NOI18N
    public final static String CANT_DERIVE_FILE_NAME_CD = "org-netbeans-modules-vmd-api-model-descriptors-CantDeriveCD.instance"; // NOI18N
    public final static String SUPER_CANT_DERIVE_FILE_NAME_CD = "org-netbeans-modules-vmd-api-model-descriptors-SuperCantDeriveCD.instance"; // NOI18N
    
    public final static String PROJECT_ID = "TEST_PROJECT"; // NOI18N
    
    public static DesignDocument createTestDesignDocument(String projectID){
        return new DesignDocument(createTestDocumentInterface(projectID));
    }
    
    /**
     * Creates new project
     * @return project
     */
    public static DocumentInterface  createTestDocumentInterface(String projectID){
        return new DocumentInterface(){
            
            public String getProjectID(){
                return PROJECT_ID;
            }
            
            public String getProjectType(){
                return "vmd"; // NOI18N
            }
            
            public void undoableEditHappened(UndoableEdit edit){
                //TODO empty method
            }
            
            public void notifyModified() {
            }
            
            public void discardAllEdits() {
            }
            
        };
    }
    
    /**
     * Returns folders if exists or creates new ones
     * @param folder path example ("vmd/components")
     * @return folder
     */
    public static FileObject getTestFolder(String folderPath){
        FileObject folder = null;
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        
        try {
            folder = FileUtil.createFolder(fs.getRoot(),folderPath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return folder;
    }
    
    public static FileObject getTestFileObject(FileObject folder, String fileName){
        FileObject file = folder.getFileObject(fileName);
        
        if (file==null){
            try {
                file = folder.createData(fileName);
            } catch (IOException ex) {
                ex.printStackTrace();
                file = null;
            }
        }
        
        return file;
    }
    /**
     * Returns Desgign document and registering descriptor components.
     * @return document
     */
    public static DesignDocument createTestDesignDocument(){
        //have to create additional folder for producers
        getTestFolder(ModelTestUtil.PROJECT_TYPE+FOLDER_PATH_PRODUCERS);
        getTestFileObject(getTestFolder(ModelTestUtil.PROJECT_TYPE+FOLDER_PATH_COMPONENTS),SUPER_FIRST_FILE_NAME_CD);
        getTestFileObject(getTestFolder(ModelTestUtil.PROJECT_TYPE+FOLDER_PATH_COMPONENTS),SUPER_CANT_DERIVE_FILE_NAME_CD);
        getTestFileObject(getTestFolder(ModelTestUtil.PROJECT_TYPE+FOLDER_PATH_COMPONENTS),FIRST_FILE_NAME_CD);
        getTestFileObject(getTestFolder(ModelTestUtil.PROJECT_TYPE+FOLDER_PATH_COMPONENTS),SECOND_FILE_NAME_CD);
        getTestFileObject(getTestFolder(ModelTestUtil.PROJECT_TYPE+FOLDER_PATH_COMPONENTS),CANT_INSTANTIATE_FILE_NAME_CD);
        getTestFileObject(getTestFolder(ModelTestUtil.PROJECT_TYPE+FOLDER_PATH_COMPONENTS),CANT_DERIVE_FILE_NAME_CD);
        
        return ModelTestUtil.createTestDesignDocument(ModelTestUtil.SUPER_FIRST_FILE_NAME_CD);
    }
}

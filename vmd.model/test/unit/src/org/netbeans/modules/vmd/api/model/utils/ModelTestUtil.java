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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.api.model.utils;

import java.io.IOException;

import javax.swing.undo.UndoableEdit;

import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DocumentInterface;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

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

        try {
            folder = FileUtil.createFolder(FileUtil.getConfigRoot(),folderPath);
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

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
package org.netbeans.modules.apisupport.refactoring;

import java.io.File;
import org.netbeans.api.mdr.MDRepository;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.UnresolvedClass;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;


public class TestUtility {

    public static void beginTrans(boolean writeAccess) {
        getDefaultRepository().beginTrans(writeAccess);
    }
        
    public static void endTrans(boolean rollback) {
        getDefaultRepository().endTrans(rollback);
    }
        
    public static void endTrans() {
        getDefaultRepository().endTrans();
    }
        
    public static MDRepository getDefaultRepository() {
        return JavaMetamodel.getDefaultRepository();
    }
        
    public static File getFile(File dataDir,String projectName, String fileName) throws FileStateInvalidException {
        String result = dataDir.getAbsolutePath() +"/" + projectName + "/" + fileName;
        System.out.println("looking for file: " + result);
        return new File(result);
    }

    public static JavaModelPackage getJavaModelPackage(String pattern) {
        throw new UnsupportedOperationException("Method is no longer supported. " +
            "Rewrite your test to new project infrastructure!");
    }
    
    public static FileSystem findFileSystem(String pattern) {
        throw new UnsupportedOperationException("Method is no longer supported. " +
            "Rewrite your test to new project infrastructure!");
    }
    
    public static JavaClass findClass(String s) {
        JavaClass result;
        int i = 20;
        do {
            result = (JavaClass) JavaMetamodel.getManager().getDefaultExtent().getType().resolve(s);
            if (result instanceof UnresolvedClass) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            i--;
        } while ((result instanceof UnresolvedClass) && i > 0);
        if (result instanceof UnresolvedClass) {
            throw new IllegalStateException("Class " + s + " not found.");
        }
        return result;
    }
}

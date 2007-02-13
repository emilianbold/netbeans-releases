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

/*
 * deleteDir.java
 *
 * Created on 30 January 2007, 22:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.qa.form;

import java.io.File;

/**
 *
 * @author Jana Maleckova
 */
public class DeleteDir {
    
    /** Creates a new instance of deleteDir */
       
    public static void delDir(String dirPath) {
        File f = new File(dirPath);
        System.out.println(dirPath);
        if (f.exists()) {
            if (f.delete()== false) {
                File[] files = f.listFiles();
                for (int i=0;i<files.length;i++){
                    File deletedFile = files[i];
                    if (deletedFile.delete()== false){
                        delDir(files[i].getAbsolutePath());
                    }
                }
                f.delete();
            }         
        }
    }
    
 
}

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

package org.netbeans.modules.compapp.projects.jbi;

import java.io.File;
import org.netbeans.modules.compapp.projects.jbi.util.MyFileUtil;

/**
 *
 * @author jqian
 */
public class MigrationHelper {
    
    public static void migrateCasaWSDL(String projSrcDir, String projName) {
        
        String jbiasaDir = projSrcDir + File.separator + "jbiasa"; // NOI18N
        String confDir = projSrcDir + File.separator + "conf"; // NOI18N
        
        String oldCasaWSDLFileName = "casa.wsdl";   // NOI18N
        File oldCasaWSDLFile = new File(jbiasaDir, oldCasaWSDLFileName);
        if (oldCasaWSDLFile.exists()) {
            
            String newCasaWSDLFileName = projName + ".wsdl"; // NOI18N
            File newCasaWSDLFile = new File(jbiasaDir, newCasaWSDLFileName);
            try {
                // move casa.wsdl to <Proj>.wsdl in src/jbiasa
                MyFileUtil.move(oldCasaWSDLFile, newCasaWSDLFile);
                
                // fix casa wsdl references inside casa
                File casaFile = new File(confDir, projName + ".casa"); // NOI18N
                MyFileUtil.replaceAll(casaFile,
                        "../jbiasa/casa.wsdl#xpointer(",  // NOI18N
                        "../jbiasa/" + newCasaWSDLFileName + "#xpointer(",  // NOI18N
                        false);
            } catch (Exception e) {
                System.out.println("Problem migrating casa.wsdl.");
            }
        }
    }
    
}

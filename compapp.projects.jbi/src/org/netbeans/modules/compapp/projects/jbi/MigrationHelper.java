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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.projects.jbi.util.MyFileUtil;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 *
 * @author jqian
 */
public class MigrationHelper {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator"); // NOI18N

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
                System.out.println("Problem migrating casa.wsdl."); // NOI18N
            }
        }
    }

    // 6/29/07 IZ #101033
    public static void migrateCompAppProperties(String projDir, EditableProperties ep) {
//        System.out.println("Migrating CompApp Properties:");
        String propFileLoc = projDir + File.separator + "nbproject" + 
                File.separator + "project.properties"; // NOI18N
//        System.out.println("propFileLoc=" + propFileLoc);
        File propertyFile = new File(propFileLoc);
//        System.out.println("property file " + propertyFile.getAbsolutePath() + ": " + propertyFile.exists());
        if (propertyFile.exists()) {
            try {
                // fix deprecated properties
                
                // The followings are defined in SE project which we are not touching:
                //com.sun.jbi.ui.devtool.jbi.alias.application-sub-assembly=This Application Sub-Assembly
                //com.sun.jbi.ui.devtool.jbi.alias.assembly-unit=This Assembly Unit
                //com.sun.jbi.ui.devtool.jbi.description.application-sub-assembly=This represents the Application Sub-Assembly
                //com.sun.jbi.ui.devtool.jbi.description.assembly-unit=Represents this Assembly Unit
                //com.sun.jbi.ui.devtool.jbi.setype.prefix=sun-bpel-engine
                
                // The followings are defined in the old CompApp project:
                //org.netbeans.modules.compapp.jbiserver.alias.application-sub-assembly=This Service Unit
                //org.netbeans.modules.compapp.jbiserver.alias.assembly-unit=This Service Assembly
                //org.netbeans.modules.compapp.jbiserver.description.application-sub-assembly=Represents this Service Unit
                //org.netbeans.modules.compapp.jbiserver.description.assembly-unit=Represents the Service Assembly of SynchronousSample35Application
                //org.netbeans.modules.compapp.projects.jbi.descriptor.uuid.assembly-unit=SynchronousSample35Application
                //org.netbeans.modules.compapp.jbiserver.component.conf.root=nbproject/private
                //org.netbeans.modules.compapp.jbiserver.deployment.conf.root=nbproject/deployment
                
                String fileName = propertyFile.getName();
                BufferedReader reader = new BufferedReader(new FileReader(propertyFile));

                File tempFile = File.createTempFile(fileName, "tmp"); // NOI18N
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (upgradeDeprecatedProperty(line, ep,
                            JbiProjectProperties.ASSEMBLY_UNIT_DESCRIPTION, 
                            JbiProjectProperties.SERVICE_ASSEMBLY_DESCRIPTION) 
                            ||
                            upgradeDeprecatedProperty(line, ep,
                            JbiProjectProperties.APPLICATION_SUB_ASSEMBLY_DESCRIPTION, 
                            JbiProjectProperties.SERVICE_UNIT_DESCRIPTION) 
                            ||
                            upgradeDeprecatedProperty(line, ep,
                            JbiProjectProperties.ASSEMBLY_UNIT_UUID, 
                            JbiProjectProperties.SERVICE_ASSEMBLY_ID)) {
                        ;
                    } else if (removeDeprecatedProperty(line, ep, 
                            JbiProjectProperties.ASSEMBLY_UNIT_ALIAS) 
                            ||
                            removeDeprecatedProperty(line, ep, 
                            JbiProjectProperties.APPLICATION_SUB_ASSEMBLY_ALIAS) 
                            ||
                            removeDeprecatedProperty(line, ep, 
                            JbiProjectProperties.JBI_COMPONENT_CONF_ROOT) 
                            ||
                            removeDeprecatedProperty(line, ep, 
                            JbiProjectProperties.JBI_DEPLOYMENT_CONF_ROOT)) {
                        continue;
                    } 
                    
                    writer.write(line + LINE_SEPARATOR);
                }
                reader.close();
                writer.close();

//                System.out.println("Updating property file " + propertyFile.getAbsolutePath()); // NOI18N
                MyFileUtil.move(tempFile, propertyFile);
            } catch (Exception e) {
                System.out.println("Problem migrating CompApp project properties: " + e); // NOI18N
            }
        }
    }
    
    private static boolean upgradeDeprecatedProperty(String line, 
            EditableProperties ep, 
            String oldPropertyName, String newPropertyName) {
        
        if (line.startsWith(oldPropertyName + "=")) { // NOI18N
//            System.out.println("    Migrating from " + oldPropertyName + " to " + newPropertyName);  // NOI18N
            line = line.replaceFirst(oldPropertyName, newPropertyName);
            if (ep != null) {
                ep.setProperty(newPropertyName, ep.getProperty(oldPropertyName));
                ep.remove(oldPropertyName);
            }
            return true;
        } else {
            return false;
        }
    }

    private static boolean removeDeprecatedProperty(String line, 
            EditableProperties ep, String oldPropertyName) {
        
        if (line.startsWith(oldPropertyName + "=")) { // NOI18N
//            System.out.println("    Removing " + oldPropertyName);  // NOI18N
            ep.remove(oldPropertyName);
            return true;
        } else {
            return false;
        }
    }
}

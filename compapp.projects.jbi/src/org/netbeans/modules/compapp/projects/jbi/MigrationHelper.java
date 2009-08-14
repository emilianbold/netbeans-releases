/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
    @SuppressWarnings("deprecation")
    public static void migrateCompAppProperties(String projDir, EditableProperties ep) {
        String propFileLoc = projDir + File.separator + "nbproject" + 
                File.separator + "project.properties"; // NOI18N
        File propertyFile = new File(propFileLoc);
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

                boolean changed = false;

                String fileName = propertyFile.getName();
                BufferedReader reader = new BufferedReader(new FileReader(propertyFile));

                File tempFile = File.createTempFile(fileName, "tmp"); // NOI18N
                tempFile.deleteOnExit();
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
                        changed = true;
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
                        changed = true;
                        continue;
                    } 
                    
                    writer.write(line + LINE_SEPARATOR);
                }
                reader.close();
                writer.close();

                if (changed) {
                    MyFileUtil.copy(tempFile, propertyFile);
                }
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

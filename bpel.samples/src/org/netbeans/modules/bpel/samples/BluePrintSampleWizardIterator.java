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
package org.netbeans.modules.bpel.samples;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public abstract class BluePrintSampleWizardIterator extends SampleWizardIterator {
    private static final long serialVersionUID = 1L;
    
    public static final String BLUE_PRINT1 = "BluePrint1"; // NOI18N
    public static final String BLUE_PRINT1_COMP_APP = "BluePrint1Application.zip"; // NOI18N
    public static final String BLUE_PRINT1_APP = "BluePrint1Application"; // NOI18N
    public static final String BLUE_PRINT2 = "BluePrint2"; // NOI18N
    public static final String BLUE_PRINT2_COMP_APP = "BluePrint2Application.zip"; // NOI18N
    public static final String BLUE_PRINT2_APP = "BluePrint2Application"; // NOI18N
    public static final String BLUE_PRINT3 = "BluePrint3"; // NOI18N
    public static final String BLUE_PRINT3_COMP_APP = "BluePrint3Application.zip"; // NOI18N
    public static final String BLUE_PRINT3_APP = "BluePrint3Application"; // NOI18N
    public static final String BLUE_PRINT4 = "BluePrint4"; // NOI18N
    public static final String BLUE_PRINT4_COMP_APP = "BluePrint4Application.zip"; // NOI18N
    public static final String BLUE_PRINT4_APP = "BluePrint4Application"; // NOI18N
    public static final String BLUE_PRINT5 = "BluePrint5"; // NOI18N
    public static final String BLUE_PRINT5_COMP_APP = "BluePrint5Application.zip"; // NOI18N
    public static final String BLUE_PRINT5_APP = "BluePrint5Application"; // NOI18N
    
    public BluePrintSampleWizardIterator() {}
    
    protected abstract String[] createSteps();
   
    public abstract String getCompositeApplicationName();
    public abstract String getCompositeApplicationArchiveName();
    
    protected Set<FileObject> createCompositeApplicationProject(FileObject projectDir, String name) throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        FileObject compAppProjectDir = projectDir.createFolder(name);                
        
        FileObject bluePrintCompositeApp = FileUtil.getConfigFile("org-netbeans-modules-bpel-samples-resources-zip/" + getCompositeApplicationArchiveName()); // NOI18N
        
        Util.unZipFile(bluePrintCompositeApp.getInputStream(), compAppProjectDir);
        Util.setProjectName(compAppProjectDir, Util.COMPAPP_PROJECT_CONFIGURATION_NAMESPACE, name, getCompositeApplicationName());

        Util.addJbiModule(compAppProjectDir, getProjectDir());
        resultSet.add(compAppProjectDir);               
        
        return resultSet;
    }
}

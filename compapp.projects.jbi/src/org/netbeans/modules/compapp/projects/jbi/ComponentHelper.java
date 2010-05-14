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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.sun.manager.jbi.management.model.ComponentInformationParser;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentDocument;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jqian
 */
public class ComponentHelper {
    
    private static Map<String, String> seNameChangeMap;
    
    static {
        seNameChangeMap = new HashMap<String, String>(); // FIXME: include bcs?
        
        seNameChangeMap.put("com.sun.aspect.aspectse", "sun-aspect-engine");    // NOI18N
        seNameChangeMap.put("com.sun.aspect.cachese", "sun-aspectcache-engine");    // NOI18N
        seNameChangeMap.put("com.sun.bpelse", "sun-bpel-engine");    // NOI18N
        seNameChangeMap.put("com.sun.dtelse", "sun-dtel-engine");    // NOI18N
        seNameChangeMap.put("com.sun.etlse", "sun-etl-engine");    // NOI18N
        seNameChangeMap.put("com.sun.iepse", "sun-iep-engine");    // NOI18N
        seNameChangeMap.put("com.sun.workflowse", "sun-workflow-engine");    // NOI18N
        seNameChangeMap.put("com.sun.xsltse", "sun-xslt-engine");    // NOI18N
        seNameChangeMap.put("com.sun.sqlse", "sun-sql-engine");    // NOI18N
        seNameChangeMap.put("JavaEEServiceEngine", "sun-javaee-engine");    // NOI18N
    }
    
    private List<String> componentNames = new ArrayList<String>();
    
    public ComponentHelper(Project jbiProject) {
        JbiProjectProperties projProperties =
                ((ProjectPropertyProvider) jbiProject).getProjectProperties();
        
        List os = (List) projProperties.get(JbiProjectProperties.META_INF);
        
        File pf = FileUtil.toFile(jbiProject.getProjectDirectory());
        String projPath = pf.getAbsolutePath() + File.separator;
        
        String compFile = projPath + os.get(0).toString() + File.separator +
                JbiProject.COMPONENT_INFO_FILE_NAME; 
        
        loadComponentInfo(compFile);
    }
    
    private void loadComponentInfo(String compFileDst) {        
        // todo: reading the cache config data if any...
        File dst = new File(compFileDst);
        
        try {
            if (dst.exists()) {
                List<JBIComponentStatus> compList = 
                        ComponentInformationParser.parse(dst);
                
                for (JBIComponentStatus comp : compList) {
                    componentNames.add(comp.getName());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public String getDefaultTarget(String asaType) {
        
        if (seNameChangeMap.containsKey(asaType)) {
            // Make the name change transition less painful.
            // Convert the name automatically in compapp project.
            // Leave the component project untouched.
            asaType = seNameChangeMap.get(asaType);
        }
        
        for (String val : componentNames) {
            if (val.equals(asaType)) {
                return val;
            }
        }
        
        return null;
    }    
}

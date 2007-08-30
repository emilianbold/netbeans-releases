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
                "ComponentInformation.xml"; // NOI18N
        
        loadComponentInfo(compFile);
    }
    
    private void loadComponentInfo(String compFileDst) {        
        // todo: reading the cache config data if any...
        File dst = new File(compFileDst);
        
        try {
            if (dst.exists()) {
                JBIComponentDocument compDoc = ComponentInformationParser.parse(dst);
                List<JBIComponentStatus> compList = compDoc.getJbiComponentList();
                
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

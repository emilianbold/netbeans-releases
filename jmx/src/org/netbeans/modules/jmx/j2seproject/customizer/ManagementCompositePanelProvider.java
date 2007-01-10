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

package org.netbeans.modules.jmx.j2seproject.customizer;

import java.util.ResourceBundle;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jmx.runtime.J2SEProjectType;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class ManagementCompositePanelProvider
        implements ProjectCustomizer.CompositeCategoryProvider {
    
    public static final String POLLING_PERIOD_KEY = "jmx.jconsole.period"; // NOI18N
    public static final String ATTACH_JCONSOLE_KEY = "jmx.jconsole.enabled"; // NOI18N
    public static final String ENABLE_RMI_KEY = "jmx.rmi.enabled"; // NOI18N
    public static final String RMI_USE_PORT_KEY = "jmx.rmi.use.port"; // NOI18N
    public static final String RMI_PORT_KEY = "jmx.rmi.port"; // NOI18N
    public static final String CONFIG_FILE_KEY = "jmx.config.file"; // NOI18N
    public static final String RESOLVE_CLASSPATH_KEY = "jmx.jconsole.use.classpath"; // NOI18N
    
    public static final String PLUGINS_CLASSPATH_KEY = "jmx.jconsole.classpath.plugins"; // NOI18N
    public static final String PLUGINS_PATH_KEY = "jmx.jconsole.plugins.path"; // NOI18N
    
    private static final String MANAGEMENT = "Management"; // NOI18N
    
    private String name;
    
    /** Creates a new instance of J2SECompositePanelProvider */
    public ManagementCompositePanelProvider(String name) {
        this.name = name;
    }
    
    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle(ManagementCompositePanelProvider.class);
        ProjectCustomizer.Category toReturn = null;
        if (MANAGEMENT.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    MANAGEMENT,
                    bundle.getString("LBL_Config_Management"),// NOI18N
                    null,
                    null);
        }
        assert toReturn != null : "No category for name:" + name;// NOI18N
        return toReturn;
    }
    
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        
        if (MANAGEMENT.equals(nm)) {
            Project project = context.lookup(Project.class);
            MonitoringPanel mp = 
                    new MonitoringPanel(project, 
                    J2SEProjectType.isPlatformGreaterThanJDK15(project));
            
            category.setOkButtonListener(mp);
            return mp;
        }
        return null;
    }
    
    public static ManagementCompositePanelProvider createManagement() {
        return new ManagementCompositePanelProvider(MANAGEMENT);
    }
}

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

package org.netbeans.modules.websvc.jaxrpc.project.customizer;

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class JaxRpcCompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String WEBSERVICECLIENTS = "JaxRpcClients"; //NOI18N

    private String name;
    
    /** Creates a new instance of J2SECompositePanelProvider */
    public JaxRpcCompositePanelProvider(String name) {
        this.name = name;
    }

    public ProjectCustomizer.Category createCategory(Lookup context) {
        //hide WEBSERVICECLIENTS if project doe not support jaxrpc client bug 112675
        Project project = context.lookup(Project.class);
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sgs.length > 0) {
            ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
            if (classPath != null) {
                if(classPath.findResource("com/sun/xml/rpc/tools/ant/Wscompile.class")==null)
                    return null; //NOI18N
            }
        }
        ResourceBundle bundle = NbBundle.getBundle( JaxRpcCompositePanelProvider.class );
        ProjectCustomizer.Category toReturn = null;
        if (WEBSERVICECLIENTS.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    WEBSERVICECLIENTS,
                    bundle.getString( "LBL_Config_WebServiceClients" ), // NOI18N
                    null);
        }
        assert toReturn != null : "No category for name:" + name; // NOI18N
        return toReturn;
    }

    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        if (WEBSERVICECLIENTS.equals(nm)) {
            List serviceClientsSettings = null;
            Project project = (Project)context.lookup(Project.class);
            WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory());
            if (clientSupport != null) {
                serviceClientsSettings = clientSupport.getServiceClients();
            }
            if(serviceClientsSettings != null && serviceClientsSettings.size() > 0) {
                return new CustomizerWSClientHost(serviceClientsSettings );
            } else {
                return new NoWebServiceClientsPanel();
            }
        }
        return new JPanel();

    }

    public static JaxRpcCompositePanelProvider createJaxRpcClients() {
        return new JaxRpcCompositePanelProvider(WEBSERVICECLIENTS);
    }

}

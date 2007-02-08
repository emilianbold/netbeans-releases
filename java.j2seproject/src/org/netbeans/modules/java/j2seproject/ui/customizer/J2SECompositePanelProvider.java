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

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.j2seproject.wsclient.CustomizerWSClientHost;
import org.netbeans.modules.java.j2seproject.wsclient.NoWebServiceClientsPanel;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class J2SECompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    private static final String SOURCES = "Sources";
    static final String LIBRARIES = "Libraries";
    
    private static final String BUILD = "Build";
//    private static final String BUILD_TESTS = "BuildTests";
    private static final String JAR = "Jar";
    private static final String JAVADOC = "Javadoc";
    public static final String RUN = "Run";
//    private static final String RUN_TESTS = "RunTests";
    private static final String APPLICATION = "Application";
    
    private static final String WEBSERVICECLIENTS = "WebServiceClients";
    private static final String WEBSERVICE_CATEGORY = "WebServiceCategory";

    private String name;
    
    /** Creates a new instance of J2SECompositePanelProvider */
    public J2SECompositePanelProvider(String name) {
        this.name = name;
    }

    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle( CustomizerProviderImpl.class );
        ProjectCustomizer.Category toReturn = null;
        if (SOURCES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    SOURCES,
                    bundle.getString("LBL_Config_Sources"),
                    null,
                    null);
        } else if (LIBRARIES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    LIBRARIES,
                    bundle.getString( "LBL_Config_Libraries" ), // NOI18N
                    null,
                    null );
        } else if (BUILD.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    BUILD,
                    bundle.getString( "LBL_Config_Build" ), // NOI18N
                    null,
                    null);
        } else if (JAR.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    JAR,
                    bundle.getString( "LBL_Config_Jar" ), // NOI18N
                    null,
                    null );
        } else if (JAVADOC.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    JAVADOC,
                    bundle.getString( "LBL_Config_Javadoc" ), // NOI18N
                    null,
                    null );
        } else if (RUN.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    RUN,
                    bundle.getString( "LBL_Config_Run" ), // NOI18N
                    null,
                    null );
        } else if (WEBSERVICECLIENTS.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    WEBSERVICECLIENTS,
                    bundle.getString( "LBL_Config_WebServiceClients" ), // NOI18N
                    null,
                    null);
        } else if (APPLICATION.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    APPLICATION,
                    bundle.getString( "LBL_Config_Application" ), // NOI18N,
                    null,
                    null);
        }
        assert toReturn != null : "No category for name:" + name;
        return toReturn;
    }

    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        J2SEProjectProperties uiProps = (J2SEProjectProperties)context.lookup(J2SEProjectProperties.class);
        if (SOURCES.equals(nm)) {
            return new CustomizerSources(uiProps);
        } else if (LIBRARIES.equals(nm)) {
            CustomizerProviderImpl.SubCategoryProvider prov = (CustomizerProviderImpl.SubCategoryProvider)context.lookup(CustomizerProviderImpl.SubCategoryProvider.class);
            assert prov != null : "Assuming CustomizerProviderImpl.SubCategoryProvider in customizer context";
            return new CustomizerLibraries(uiProps, prov);
        } else if (BUILD.equals(nm)) {
            return new CustomizerCompile(uiProps);
        } else if (JAR.equals(nm)) {
            return new CustomizerJar(uiProps);
        } else if (JAVADOC.equals(nm)) {
            return new CustomizerJavadoc(uiProps);
        } else if (RUN.equals(nm)) {
            return new CustomizerRun(uiProps);
        } else if (WEBSERVICECLIENTS.equals(nm)) {
            List serviceClientsSettings = null;
            Project project = (Project)context.lookup(Project.class);
            WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory());
            if (clientSupport != null) {
                serviceClientsSettings = clientSupport.getServiceClients();
            }

            if(serviceClientsSettings != null && serviceClientsSettings.size() > 0) {
                return new CustomizerWSClientHost( uiProps, serviceClientsSettings );
            } else {
                return new NoWebServiceClientsPanel();
            }
        } else if (APPLICATION.equals(nm)) {
            return new CustomizerApplication(uiProps);
        }
        return new JPanel();

    }

    public static J2SECompositePanelProvider createSources() {
        return new J2SECompositePanelProvider(SOURCES);
    }

    public static J2SECompositePanelProvider createLibraries() {
        return new J2SECompositePanelProvider(LIBRARIES);
    }

    public static J2SECompositePanelProvider createBuild() {
        return new J2SECompositePanelProvider(BUILD);
    }

    public static J2SECompositePanelProvider createJar() {
        return new J2SECompositePanelProvider(JAR);
    }

    public static J2SECompositePanelProvider createJavadoc() {
        return new J2SECompositePanelProvider(JAVADOC);
    }

    public static J2SECompositePanelProvider createRun() {
        return new J2SECompositePanelProvider(RUN);
    }

    public static J2SECompositePanelProvider createWebServiceClients() {
        return new J2SECompositePanelProvider(WEBSERVICECLIENTS);
    }
    
    public static J2SECompositePanelProvider createApplication() {
        return new J2SECompositePanelProvider(APPLICATION);
    }
}

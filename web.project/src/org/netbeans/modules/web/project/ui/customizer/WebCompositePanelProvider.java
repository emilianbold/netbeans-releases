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

package org.netbeans.modules.web.project.ui.customizer;

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.project.ProjectWebModule;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint, rnajman
 */
public class WebCompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    private static final String SOURCES = "Sources";
    static final String LIBRARIES = "Libraries";
    private static final String FRAMEWORKS = "Frameworks";

    private static final String BUILD = "Build";
    private static final String WAR = "War";
    private static final String JAVADOC = "Javadoc";
    public static final String RUN = "Run";
    
    private static final String WEBSERVICES = "WebServices";
    private static final String WEBSERVICECLIENTS = "WebServiceClients";

    private String name;
    
    /** Creates a new instance of WebCompositePanelProvider */
    public WebCompositePanelProvider(String name) {
        this.name = name;
    }

    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle( CustomizerProviderImpl.class );
        ProjectCustomizer.Category toReturn = null;
        
        if (SOURCES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    SOURCES,
                    bundle.getString("LBL_Config_Sources"), //NOI18N
                    null,
                    null);
        } else if (FRAMEWORKS.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    FRAMEWORKS,
                    bundle.getString( "LBL_Config_Frameworks" ), // NOI18N
                    null,
                    null );
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
        } else if (WAR.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    WAR,
                    bundle.getString( "LBL_Config_War" ), // NOI18N
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
        } else if (WEBSERVICES.equals(name) || WEBSERVICECLIENTS.equals(name)) {
            WebProject project = (WebProject) context.lookup(WebProject.class);
            ProjectWebModule wm = (ProjectWebModule) project.getLookup().lookup(ProjectWebModule.class);
            FileObject docBase = wm.getDocumentBase();
            if (WEBSERVICES.equals(name)) {
                List servicesSettings = null;
                if (docBase != null) {
                    WebServicesSupport servicesSupport = WebServicesSupport.getWebServicesSupport(docBase);
                    if (servicesSupport != null) {
                        servicesSettings = servicesSupport.getServices();
                    }
                }
                if ((servicesSettings!=null && servicesSettings.size()>0)) {
                    toReturn = ProjectCustomizer.Category.create(
                            WEBSERVICES,
                            bundle.getString( "LBL_Config_WebServices" ), // NOI18N
                            null,
                            null );
                }
            } else if (WEBSERVICECLIENTS.equals(name)) {
                List serviceClientsSettings = null;
                if (docBase != null) {
                    WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(docBase);
                    if (clientSupport != null) {
                        serviceClientsSettings = clientSupport.getServiceClients();
                    }
                }            
                if(serviceClientsSettings != null && serviceClientsSettings.size() > 0) {
                    toReturn = ProjectCustomizer.Category.create(
                            WEBSERVICECLIENTS,
                            bundle.getString( "LBL_Config_WebServiceClients" ), // NOI18N
                            null,
                            null );
                }
            }
        }
        
//        assert toReturn != null : "No category for name:" + name;
        return toReturn;
    }

    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        WebProjectProperties uiProps = (WebProjectProperties)context.lookup(WebProjectProperties.class);
        if (SOURCES.equals(nm)) {
            return new CustomizerSources(uiProps);
        } else if (FRAMEWORKS.equals(nm)) {
            return new CustomizerFrameworks(category, uiProps);
        } else if (LIBRARIES.equals(nm)) {
            CustomizerProviderImpl.SubCategoryProvider prov = (CustomizerProviderImpl.SubCategoryProvider)context.lookup(CustomizerProviderImpl.SubCategoryProvider.class);
            assert prov != null : "Assuming CustomizerProviderImpl.SubCategoryProvider in customizer context";
            return new CustomizerLibraries(uiProps, prov);
        } else if (BUILD.equals(nm)) {
            return new CustomizerCompile(uiProps);
        } else if (WAR.equals(nm)) {
            return new CustomizerWar(uiProps);
        } else if (JAVADOC.equals(nm)) {
            return new CustomizerJavadoc(uiProps);
        } else if (RUN.equals(nm)) {
            return new CustomizerRun(uiProps);
        } else if (WEBSERVICES.equals(nm) || WEBSERVICECLIENTS.equals(nm)) {
            ProjectWebModule wm = (ProjectWebModule) uiProps.getProject().getLookup().lookup(ProjectWebModule.class);
            FileObject docBase = wm.getDocumentBase();
            if (WEBSERVICES.equals(nm)) {
                List servicesSettings = null;
                if (docBase != null) {
                    WebServicesSupport servicesSupport = WebServicesSupport.getWebServicesSupport(docBase);
                    if (servicesSupport != null) {
                        servicesSettings = servicesSupport.getServices();
                    }
                }
                if(servicesSettings != null && servicesSettings.size() > 0) {
                    return new CustomizerWSServiceHost( uiProps, servicesSettings );
                } else {
                    return new NoWebServicesPanel();
                }            
            } else if (WEBSERVICECLIENTS.equals(nm)) {
                List serviceClientsSettings = null;
                if (docBase != null) {
                    WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(docBase);
                    if (clientSupport != null) {
                        serviceClientsSettings = clientSupport.getServiceClients();
                    }
                }
                if(serviceClientsSettings != null && serviceClientsSettings.size() > 0) {
                    return new CustomizerWSClientHost( uiProps, serviceClientsSettings );
                } else {
                    return new NoWebServiceClientsPanel();
                }
            }
        }
        
        return new JPanel();
    }

    public static WebCompositePanelProvider createSources() {
        return new WebCompositePanelProvider(SOURCES);
    }
    
    public static WebCompositePanelProvider createFrameworks() {
        return new WebCompositePanelProvider(FRAMEWORKS);
    }

    public static WebCompositePanelProvider createLibraries() {
        return new WebCompositePanelProvider(LIBRARIES);
    }

    public static WebCompositePanelProvider createBuild() {
        return new WebCompositePanelProvider(BUILD);
    }

    public static WebCompositePanelProvider createWar() {
        return new WebCompositePanelProvider(WAR);
    }

    public static WebCompositePanelProvider createJavadoc() {
        return new WebCompositePanelProvider(JAVADOC);
    }

    public static WebCompositePanelProvider createRun() {
        return new WebCompositePanelProvider(RUN);
    }

    public static WebCompositePanelProvider createWebServices() {
        return new WebCompositePanelProvider(WEBSERVICES);
    }
    
    public static WebCompositePanelProvider createWebServiceClients() {
        return new WebCompositePanelProvider(WEBSERVICECLIENTS);
    }
    
}

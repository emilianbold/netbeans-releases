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

package org.netbeans.modules.j2ee.ejbjarproject.ui.customizer;

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProvider;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint, rnajman
 */
public class EjbJarCompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    private static final String SOURCES = "Sources";
    static final String LIBRARIES = "Libraries";

    private static final String BUILD = "Build";
    private static final String JAR = "Jar";
    private static final String JAVADOC = "Javadoc";
    public static final String RUN = "Run";
    
    private static final String WEBSERVICES = "WebServices";
    
    private String name;
    
    /** Creates a new instance of EjbJarCompositePanelProvider */
    public EjbJarCompositePanelProvider(String name) {
        this.name = name;
    }

    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle( EjbJarCompositePanelProvider.class );
        ProjectCustomizer.Category toReturn = null;
        
        if (SOURCES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    SOURCES,
                    bundle.getString("LBL_Config_Sources"), //NOI18N
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
        } else if (WEBSERVICES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    WEBSERVICES,
                    bundle.getString( "LBL_Config_WebServices" ), // NOI18N
                    null,
                    null );
        }
        
//        assert toReturn != null : "No category for name:" + name;
        return toReturn;
    }

    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        EjbJarProjectProperties uiProps = (EjbJarProjectProperties) context.lookup(EjbJarProjectProperties.class);
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
        } else if (WEBSERVICES.equals(nm)) {
            EjbJarProvider ejbJarProvider = (EjbJarProvider) uiProps.getProject().getLookup().lookup(EjbJarProvider.class);
            FileObject metaInf = ejbJarProvider.getMetaInf();
            List servicesSettings = null;
            if (metaInf != null) {
                WebServicesSupport servicesSupport = WebServicesSupport.getWebServicesSupport(metaInf);
                if (servicesSupport != null) {
                    servicesSettings = servicesSupport.getServices();
                }
            }
            if(servicesSettings != null && servicesSettings.size() > 0) {
                return new CustomizerWSServiceHost( uiProps, servicesSettings );
            } else {
                return new NoWebServicesPanel();
            }
        }
        
        return new JPanel();
    }

    public static EjbJarCompositePanelProvider createSources() {
        return new EjbJarCompositePanelProvider(SOURCES);
    }
    
    public static EjbJarCompositePanelProvider createLibraries() {
        return new EjbJarCompositePanelProvider(LIBRARIES);
    }

    public static EjbJarCompositePanelProvider createBuild() {
        return new EjbJarCompositePanelProvider(BUILD);
    }

    public static EjbJarCompositePanelProvider createJar() {
        return new EjbJarCompositePanelProvider(JAR);
    }

    public static EjbJarCompositePanelProvider createJavadoc() {
        return new EjbJarCompositePanelProvider(JAVADOC);
    }

    public static EjbJarCompositePanelProvider createRun() {
        return new EjbJarCompositePanelProvider(RUN);
    }

    public static EjbJarCompositePanelProvider createWebServices() {
        return new EjbJarCompositePanelProvider(WEBSERVICES);
    }
    
}

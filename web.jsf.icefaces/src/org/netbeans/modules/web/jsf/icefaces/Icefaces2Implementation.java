/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.icefaces;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.JsfComponentUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.web.jsf.icefaces.ui.Icefaces2CustomizerPanelVisual;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentImplementation;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class Icefaces2Implementation implements JsfComponentImplementation {

    private static final Logger LOGGER = Logger.getLogger(Icefaces2Implementation.class.getName());

    /**
     * Name of the node in NetBeans preferences.
     */
    public static final String PREFERENCES_NODE = "icefaces2";
    /**
     * Framework name used also for statistics.
     */
    public static final String ICEFACES_NAME = "ICEfaces 2.0"; //NOI18N
    /**
     * Base class for which is searched by detecting ICEfaces2 on the classpath of the project.
     */
    public static final String ICEFACES_CORE_CLASS = "org.icefaces.impl.facelets.tag.icefaces.core.ConfigHandler"; //NOI18N
    /**
     * Name of preferred library which was used for last time.
     */
    public static final String PREF_LIBRARY_NAME = "preffered-library";

    private Icefaces2Customizer customizer;

    // Constants for web.xml
    private static final String FACES_SAVING_METHOD = "javax.faces.STATE_SAVING_METHOD";
    private static final String FACES_SKIP_COMMENTS = "javax.faces.FACELETS_SKIP_COMMENTS";
    private static final String icefacesPom ="http://anonsvn.icefaces.org/repo/maven2/releases/org/icefaces/icefaces/2.0.2/icefaces-2.0.2.pom";

    @Override
    public String getName() {
        return ICEFACES_NAME;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(Icefaces2Implementation.class, "DESC_Icefaces2Implementation"); //NOI18N
    }

    @Override
    public Set<FileObject> extend(WebModule webModule, JsfComponentCustomizer jsfComponentCustomizer) {
        // Add library to webmodule classpath
        try {
            List<Library> libraries = new ArrayList<Library>(1);
            Library ifLibrary = null;

            // get the ICEfaces library from customizer
            if (jsfComponentCustomizer != null) {
                Icefaces2CustomizerPanelVisual icefacesPanel =
                        ((Icefaces2CustomizerPanelVisual) jsfComponentCustomizer.getComponent());
                String chosenLibrary = icefacesPanel.getIcefacesLibrary();
                ifLibrary = LibraryManager.getDefault().getLibrary(chosenLibrary);
            }

            // search for library stored in ICEfaces2 preferences
            if (ifLibrary == null) {
                Preferences preferences = getIcefacesPreferences();
                ifLibrary = LibraryManager.getDefault().getLibrary(
                        preferences.get(Icefaces2Implementation.PREF_LIBRARY_NAME, "")); //NOI18N
            }

            // otherwise search for any registered ICEfaces library in IDE
            if (ifLibrary == null) {
                ifLibrary = Icefaces2Customizer.getIcefacesLibraries().get(0);
            }

            if (ifLibrary != null) {
                FileObject[] javaSources = webModule.getJavaSources();
                Project project = FileOwnerQuery.getOwner(webModule.getDocumentBase());
                AntArtifactProvider antArtifactProvider = project.getLookup().lookup(AntArtifactProvider.class);

                // in cases of Maven, update library to contains maven-pom references if needed
                if (antArtifactProvider == null) {
                    List<URI> pomArtifacts;
                    try {
                        pomArtifacts = Arrays.asList(new URI(icefacesPom));
                        ifLibrary = JsfComponentUtils.enhanceLibraryWithPomContent(ifLibrary, pomArtifacts);
                    } catch (URISyntaxException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }

                libraries.add(ifLibrary);
                ProjectClassPathModifier.addLibraries(
                        libraries.toArray(new Library[1]),
                        javaSources[0],
                        ClassPath.COMPILE);
            } else {
                LOGGER.log(Level.SEVERE, "No ICEfaces library was found.");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Exception during extending an web project", ex); //NOI18N
        } catch (UnsupportedOperationException ex) {
            LOGGER.log(Level.WARNING, "Exception during extending an web project", ex); //NOI18N
        }

        // Update web.xml DD if required
        try {
            WebApp ddRoot = DDProvider.getDefault().getDDRoot(webModule.getDeploymentDescriptor());

            // add context-param - javax.faces.STATE_SAVING_METHOD
            InitParam savingMethodParam = (InitParam) ddRoot.createBean("InitParam");    //NOI18N
            savingMethodParam.setParamName(FACES_SAVING_METHOD);
            savingMethodParam.setParamValue("server"); //NOI18N
            ddRoot.addContextParam(savingMethodParam);

            // add context-param - javax.faces.FACELETS_SKIP_COMMENTS
            InitParam skipCommentsParam = (InitParam) ddRoot.createBean("InitParam");    //NOI18N
            skipCommentsParam.setParamName(FACES_SKIP_COMMENTS);
            skipCommentsParam.setParamValue("true"); //NOI18N
            ddRoot.addContextParam(skipCommentsParam);

            ddRoot.write(webModule.getDeploymentDescriptor());
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Exception during updating web.xml DD", ex); //NOI18N
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Exception during updating web.xml DD", ex); //NOI18N
        }

        return Collections.<FileObject>emptySet();
    }

    @Override
    public JSFVersion getJsfVersion() {
        return JSFVersion.JSF_2_0;
    }

    @Override
    public boolean isInWebModule(WebModule webModule) {
        ClassPath classpath = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
        if (classpath.findResource(ICEFACES_CORE_CLASS.replace('.', '/') + ".class") != null) { //NOI18N
            return true;
        }
        return false;
    }

    @Override
    public JsfComponentCustomizer createJsfComponentCustomizer(WebModule webModule) {
        if (customizer == null) {
            customizer = new Icefaces2Customizer();
        }
        return customizer;
    }

    @Override
    public void remove(WebModule webModule) {
    }

    /**
     * Gets {@code NbPreferences} for ICEfaces plugin.
     *
     * @return Preferences of the ICEfaces
     */
    public static Preferences getIcefacesPreferences() {
        return NbPreferences.forModule(Icefaces2Customizer.class).node(Icefaces2Implementation.PREFERENCES_NODE);
    }
}

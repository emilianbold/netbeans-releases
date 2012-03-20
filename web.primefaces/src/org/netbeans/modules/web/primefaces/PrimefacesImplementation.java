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
package org.netbeans.modules.web.primefaces;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.JsfComponentUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class PrimefacesImplementation implements JsfComponentImplementation {

    private PrimefacesCustomizer customizer;

    private final String name;
    private final String description;

    private static final Logger LOGGER = Logger.getLogger(PrimefacesImplementation.class.getName());
    private static final String PRIMEFACES_SPECIFIC_CLASS = "org.primefaces.application.PrimeResource"; //NOI18N
    private static final String PRIMEFACES_LIBRARY_NAME = "primefaces"; //NOI18N
    private static final String PREFERENCES_NODE = "primefaces"; //NOI18N

    public static final String PROP_PREFERRED_LIBRARY = "preferred-library"; //NOI18N

    public PrimefacesImplementation() {
        this.name = NbBundle.getMessage(PrimefacesProvider.class, "LBL_PrimeFaces");  //NOI18N
        this.description = NbBundle.getMessage(PrimefacesProvider.class, "LBL_PrimeFaces_Description"); //NOI18N
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<FileObject> extend(WebModule webModule, JsfComponentCustomizer jsfComponentCustomizer) {
        try {
            ProjectClassPathModifier.addLibraries(
                    new Library[]{LibraryManager.getDefault().getLibrary(PRIMEFACES_LIBRARY_NAME)},
                    webModule.getJavaSources()[0],
                    ClassPath.COMPILE);

            // generate PrimeFaces welcome page
            FileObject welcomePage = generateWelcomePage(webModule);
            return Collections.singleton(welcomePage);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Exception during extending an web project", ex); //NOI18N
        } catch (UnsupportedOperationException ex) {
            LOGGER.log(Level.WARNING, "Exception during extending an web project", ex); //NOI18N
        }
        return Collections.<FileObject>emptySet();
    }

    private static FileObject generateWelcomePage(WebModule webModule) throws IOException {
        FileObject templateFO = FileUtil.getConfigFile("Templates/Other/welcomePrimefaces.xhtml"); //NOI18N
        DataObject templateDO = DataObject.find(templateFO);
        DataObject generated = templateDO.createFromTemplate(
                DataFolder.findFolder(webModule.getDocumentBase()),
                "welcomePrimefaces"); //NOI18N
        JsfComponentUtils.reformat(generated);
        
        // update and reformat index page
        updateIndexPage(webModule);
        
        return generated.getPrimaryFile();
    }

    private static void updateIndexPage(WebModule webModule) throws DataObjectNotFoundException {
        FileObject indexFO = webModule.getDocumentBase().getFileObject("index.xhtml"); //NOI18N
        DataObject indexDO = DataObject.find(indexFO);
        JsfComponentUtils.enhanceFileBody(indexDO, "</h:body>", "<br />\n<h:link outcome=\"welcomePrimefaces\" value=\"Primefaces welcome page\" />"); //NOI18N
        if (indexFO.isValid() && indexFO.canWrite()) {
            JsfComponentUtils.reformat(indexDO);
        }
    }

    @Override
    public Set<JSFVersion> getJsfVersion() {
        return EnumSet.of(JSFVersion.JSF_2_0, JSFVersion.JSF_2_1);
    }

    @Override
    public boolean isInWebModule(WebModule webModule) {
        ClassPath classpath = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
        return classpath.findResource(PRIMEFACES_SPECIFIC_CLASS.replace('.', '/') + ".class") != null; //NOI18N
    }

    @Override
    public JsfComponentCustomizer createJsfComponentCustomizer(WebModule webModule) {
        if (customizer == null) {
            customizer = new PrimefacesCustomizer();
        }
        return customizer;
    }

    @Override
    public void remove(WebModule webModule) {
        try {
            List<Library> allRegisteredPrimefaces2 = getAllRegisteredPrimefaces();
            ProjectClassPathModifier.removeLibraries(
                    allRegisteredPrimefaces2.toArray(new Library[allRegisteredPrimefaces2.size()]),
                    webModule.getJavaSources()[0],
                    ClassPath.COMPILE);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Exception during removing JSF suite from an web project", ex); //NOI18N
        } catch (UnsupportedOperationException ex) {
            LOGGER.log(Level.WARNING, "Exception during removing JSF suite from an web project", ex); //NOI18N
        }
    }

     /**
     * Returns {@code List} of all Primefaces2 libraries registered in the IDE.
     *
     * @return {{@code List} of libraries
     */
    public static List<Library> getAllRegisteredPrimefaces() {
        List<Library> libraries = new ArrayList<Library>();
        List<URL> content;
        for (Library library : LibraryManager.getDefault().getLibraries()) {
            if (!"j2se".equals(library.getType())) { //NOI18N
                continue;
            }

            content = library.getContent("classpath"); //NOI18N
            if (isValidPrimefacesLibrary(content)) {
                libraries.add(library);
            }
        }
        return libraries;
    }

    /**
     * Checks if given library content contains mandatory class in cases of
     * Primefaces2 library.
     *
     * @param libraryContent library content
     * @return {@code true} if the given content contains Primefaces2 library,
     * {@code false} otherwise
     */
    public static boolean isValidPrimefacesLibrary(List<URL> libraryContent) {
        try {
            return Util.containsClass(libraryContent, PRIMEFACES_SPECIFIC_CLASS);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return false;
        }
    }

    /**
     * Gets {@code NbPreferences} for Primefaces plugin.
     * @return Preferences of the Primefaces
     */
    public static Preferences getPrimefacesPreferences() {
        return NbPreferences.forModule(PrimefacesImplementation.class).node(PrimefacesImplementation.PREFERENCES_NODE);
    }
}

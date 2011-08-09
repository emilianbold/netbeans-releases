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
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class Primefaces2Implementation implements JsfComponentImplementation {

    private final String name;
    private final String description;
    
    private static final Logger LOGGER = Logger.getLogger(Primefaces2Implementation.class.getName());
    private static final String PRIMEFACES_SPECIFIC_CLASS = "org.primefaces.application.PrimeResource"; //NOI18N
    private static final String PRIMEFACES_LIBRARY_NAME = "primefaces"; //NOI18N
    
    public Primefaces2Implementation() {
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
                    new Library[]{ LibraryManager.getDefault().getLibrary(PRIMEFACES_LIBRARY_NAME) }, 
                    webModule.getJavaSources()[0], 
                    ClassPath.COMPILE);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Exception during extending an web project", ex); //NOI18N
        } catch (UnsupportedOperationException ex) {
            LOGGER.log(Level.WARNING, "Exception during extending an web project", ex); //NOI18N
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
        return classpath.findResource(PRIMEFACES_SPECIFIC_CLASS.replace('.', '/') + ".class") != null; //NOI18N
    }

    @Override
    public JsfComponentCustomizer createJsfComponentCustomizer(WebModule webModule) {
        return null;
    }

    @Override
    public void remove(WebModule wm) {
    }
    
}

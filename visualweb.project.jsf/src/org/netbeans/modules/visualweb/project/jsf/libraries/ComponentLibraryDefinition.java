/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.visualweb.project.jsf.libraries;

import org.netbeans.modules.visualweb.webui.jsf.defaulttheme.libraries.provider.ComponentLibraryTypeProvider;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.libraries.LibraryFactory;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;

/**
 *
 * @author Po-Ting Wu
 */
public class ComponentLibraryDefinition extends LibraryDefinition {
    /**
     * Create a library definition with specified resources.
     * @param name Internal name of the library from which the library definition file name will be derived as
     * as well as the display name for the library
     * @param description  Description key to look up a text description from the localizingBundle
     * @param localizingBundle Sets the localizing bundle. The bundle is used for localizing the name and
     * description. The bundle is located using the system ClassLoader. This resource name will look
     * something like: org.netbeans.modules.visualweb.mymodule.Bundle
     * @param classPaths List of classpath references: jar, zip, or folder. Can be empty or null.
     * @param javadocs List of javadoc references, jar zip, or folder. Can be empty or null.
     * @param sources, List of jar, zip, or folder. Can be empty or null;
     * @return Library The new Library instance registered with the NetBeans Library Manager
     * @throws IOException if the library definition already exists or could not be created
     */
    public static Library create(   String name,
                                    String description,
                                    String localizingBundle,
                                    List<URL> classPaths,
                                    List<URL> sources,
                                    List<URL> javadocs,
                                    List<URL> designtimes) throws IOException {
        LibraryImplementation impl = LibrariesSupport.createLibraryImplementation(ComponentLibraryTypeProvider.LIBRARY_TYPE, ComponentLibraryTypeProvider.VOLUME_TYPES);
        impl.setName (name);
        impl.setDescription (description);
        impl.setLocalizingBundle (localizingBundle);
        if (classPaths != null) {
            ArrayList a = new ArrayList(classPaths.size());
            for (Iterator i = classPaths.iterator(); i.hasNext(); ) {
                a.add(toResourceURL((URL)i.next()));
            }
            impl.setContent("classpath", a); // NOI18N
        }
        if (sources != null) {
            ArrayList a = new ArrayList(sources.size());
            for (Iterator i = sources.iterator(); i.hasNext(); ) {
                a.add(toResourceURL((URL)i.next()));
            }
            impl.setContent("src", a);  // NOI18N
        }
        if (javadocs != null) {
            ArrayList a = new ArrayList(javadocs.size());
            for (Iterator i = javadocs.iterator(); i.hasNext(); ) {
                a.add(toResourceURL((URL)i.next()));
            }
            impl.setContent("javadoc", a);  // NOI18N
        }
        if (designtimes != null) {
            ArrayList a = new ArrayList(designtimes.size());
            for (Iterator i = designtimes.iterator(); i.hasNext(); ) {
                a.add(toResourceURL((URL)i.next()));
            }
            impl.setContent("visual-web-designtime", a);  // NOI18N
        }
                
        Library lib = LibraryFactory.createLibrary(impl);
        LibraryManager.getDefault().addLibrary(lib);

        return lib;
    }    

    /**
     * Convenience method to update an existing library definition with specified resources.
     * @param name Internal name of the existing library from which the library definition file name 
     * will be derived as as well as the display name for the library
     * @param description  Description key to look up a text description from the localizingBundle
     * @param localizingBundle Sets the localizing bundle. The bundle is used for localizing the name and 
     * description. The bundle is located using the system ClassLoader. This resource name will look
     * something like: org.netbeans.modules.visualweb.mymodule.Bundle
     * @param classPaths List of classpath references: jar, zip, or folder. Can be empty or null.
     * @param javadocs List of javadoc references, jar zip, or folder. Can be empty or null.
     * @param sources, List of jar, zip, or folder. Can be empty or null;
     * @return Library The new Library instance registered with the NetBeans Library Manager
     * @throws IOException if the library definition did not already exist or could not be updated
     */

    public static Library update(   String name, 
                                    String description,
                                    String localizingBundle,
                                    List<URL> classPaths, 
                                    List<URL> sources, 
                                    List<URL> javadocs, 
                                    List<URL> designtimes) throws IOException {
        remove(name);
        return create(name, description, localizingBundle, classPaths, sources, javadocs, designtimes);
    }
}

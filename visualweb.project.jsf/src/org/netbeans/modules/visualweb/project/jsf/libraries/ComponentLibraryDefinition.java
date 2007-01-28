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

package org.netbeans.modules.visualweb.project.jsf.libraries;

import org.netbeans.modules.visualweb.project.jsf.api.LibraryDefinition;
import org.netbeans.modules.visualweb.project.jsf.api.LibraryDefinition.LibraryDomain;
import org.netbeans.modules.visualweb.project.jsf.libraries.provider.ComponentLibraryTypeProvider;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
// TODO Can't hack NB anymore.
//import org.netbeans.api.project.libraries.RaveLibraryTypeRegistry;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;

/**
 *
 * @author dey
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
     * @param domain Location where this library definition should live (tool, user, project)
     * <i>Not implemented. Until NetBeans supports this feature, all library definitions are
     * perssisted in the user domain (i.e. userdir) </i>
     * @param classPaths List of classpath references: jar, zip, or folder. Can be empty or null.
     * @param javadocs List of javadoc references, jar zip, or folder. Can be empty or null.
     * @param sources, List of jar, zip, or folder. Can be empty or null;
     * @return Library The new Library instance registered with the NetBeans Library Manager
     * @throws IOException if the library definition already exists or could not be created
     */
    public static Library create(   String name,
                                    String description,
                                    String localizingBundle,
                                    LibraryDomain domain,
                                    List /* <URL> */ classPaths,
                                    List /* <URL> */ sources,
                                    List /* <URL> */ javadocs,
                                    List /* <URL> */ designtimes) throws IOException {

        /* TODO Can't hack NB anymore.
        LibraryTypeProvider provider = RaveLibraryTypeRegistry.getLibraryTypeProvider (ComponentLibraryTypeProvider.LIBRARY_TYPE);

        if (provider == null) {
            return null;
        }

        LibraryImplementation impl = provider.createLibrary();
        */

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
                
        addLibrary(impl);
        
        Library[] libs = LibraryManager.getDefault().getLibraries();
        Library lib = null;
        
        for (int i = 0; i < libs.length && lib == null; i++) {
            if (libs[i].getName().equals(name)) {
                lib = libs[i];
            }
        }
        
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
     * @param domain Location where this library definition should live (tool, user, project) 
     * <i>Not implemented. Until NetBeans supports this feature, all library definitions are 
     * perssisted in the user domain (i.e. userdir) </i> 
     * @param classPaths List of classpath references: jar, zip, or folder. Can be empty or null.
     * @param javadocs List of javadoc references, jar zip, or folder. Can be empty or null.
     * @param sources, List of jar, zip, or folder. Can be empty or null;
     * @return Library The new Library instance registered with the NetBeans Library Manager
     * @throws IOException if the library definition did not already exist or could not be updated
     */

    public static Library update(   String name, 
                                    String description,
                                    String localizingBundle,
                                    LibraryDomain domain,
                                    List /* <URL> */ classPaths, 
                                    List /* <URL> */ sources, 
                                    List /* <URL> */ javadocs, 
                                    List /* <URL> */ designtimes) throws IOException {
        remove(name, domain);
        return create(name, description, localizingBundle, domain, classPaths, sources, javadocs, designtimes);
    }

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.netbeans.modules.web.jsf.editor.facelets;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import org.netbeans.modules.web.jsf.editor.tld.LibraryDescriptor;
import org.netbeans.modules.web.jsf.editor.tld.LibraryDescriptorException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

public class ClassBasedFaceletsLibrary extends FaceletsLibrary {

    private final Collection<NamedComponent> components = new ArrayList<NamedComponent>();
    private final URL libraryDescriptorSourceURL;
    private FaceletsLibraryDescriptor libraryDescriptor;

    public ClassBasedFaceletsLibrary(URL libraryDescriptorSourceURL, FaceletsLibrarySupport support, String namespace) {
        super(support, namespace);
        if(libraryDescriptorSourceURL == null) {
            throw new NullPointerException("libraryDescriptorSourceURL cannot be null!"); //NOI18N
        }
        this.libraryDescriptorSourceURL = libraryDescriptorSourceURL;
    }

    //for default libraries
    void setComponents(Collection<NamedComponent> components) {
        this.components.addAll(components);
    }

    public Collection<NamedComponent> getComponents() {
        return Collections.unmodifiableCollection(components);
    }

    public LibraryDescriptor getLibraryDescriptor() {
        LibraryDescriptor ld = support.getJsfSupport().getLibraryDescriptor(getNamespace());
        if(ld == null) {
            //Indexed library descriptor cannot be found,
            //lets create the descriptor from the source url
            if(libraryDescriptor == null) {
                FileObject fo = URLMapper.findFileObject(libraryDescriptorSourceURL);
                if(fo != null) {
                    try {
                        libraryDescriptor = FaceletsLibraryDescriptor.create(fo);
                    } catch (LibraryDescriptorException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    Logger.getAnonymousLogger().info(
                            String.format("Cannot convert facelets library descriptor's URL %s into a FileObject?!?!",
                            libraryDescriptorSourceURL.toString())); //NOI18N
                }

            }
            ld = libraryDescriptor;
        }

        return ld;

    }

    public void putConverter(String name, String id) {
        components.add(new Converter(name, id, null));
    }

    public void putConverter(String name, String id, Class handlerClass) {
        components.add(new Converter(name, id, handlerClass));
    }

    public void putValidator(String name, String id) {
        components.add(new Validator(name, id, null));
    }

    public void putValidator(String name, String id, Class handlerClass) {
        components.add(new Validator(name, id, handlerClass));
    }

    public void putBehavior(String name, String id) {
        components.add(new Behavior(name, id, null));
    }

    public void putBehavior(String name, String id, Class handlerClass) {
        components.add(new Behavior(name, id, handlerClass));
    }

    public void putTagHandler(String name, Class type) {
        components.add(new TagHandler(name, type));
    }

    public void putComponent(String name, String componentType,
            String rendererType) {
        components.add(new Component(name, componentType, rendererType, null));
    }

    public void putComponent(String name, String componentType,
            String rendererType, Class handlerClass) {
        components.add(new Component(name, componentType, rendererType, handlerClass));
    }

    public void putUserTag(String name, URL source) {
        components.add(new UserTag(name, source));
    }

    public void putFunction(String name, Method method) {
        components.add(new Function(name, method));
    }

    public NamedComponent createNamedComponent(String name) {
        return new NamedComponent(name);
    }

    public Function createFunction(String name, Method method) {
        return new Function(name, method);
    }

   
}

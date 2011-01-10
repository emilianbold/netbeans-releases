/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
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
 * as provided by Oracle in the GPL Version 2 section of the License file that
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
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.web.jsfapi.api.LibraryType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

public class ClassBasedFaceletsLibrary extends FaceletsLibrary {

    private final Collection<NamedComponent> components = new ArrayList<NamedComponent>();
    private FaceletsLibraryDescriptor libraryDescriptor;
    private final String defaultPrefix;
    private final URL libraryDescriptorSource;

    public ClassBasedFaceletsLibrary(URL libraryDescriptorSourceURL, final FaceletsLibrarySupport support, String namespace) {
        super(support, namespace);
        assert libraryDescriptorSourceURL != null;
        
        this.defaultPrefix = generateDefaultPrefix();
        this.libraryDescriptorSource = libraryDescriptorSourceURL;

        FileObject libraryDescriptorFile = URLMapper.findFileObject(libraryDescriptorSourceURL);
        try {
            libraryDescriptor = FaceletsLibraryDescriptor.create(libraryDescriptorFile);
        } catch (LibraryDescriptorException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    //for default libraries
    void setComponents(Collection<NamedComponent> components) {
        this.components.addAll(components);
    }

    @Override
    public URL getLibraryDescriptorSource() {
        return libraryDescriptorSource;
    }

    @Override
    public LibraryType getType() {
        return LibraryType.CLASS;
    }

    @Override
    public String getDefaultNamespace() {
        return null;
    }

    @Override
    public String getDefaultPrefix() {
        //non standard library will use a prefix generated from the library namespace
        String superdefaultPrefix = super.getDefaultPrefix();
        return superdefaultPrefix != null ? superdefaultPrefix : defaultPrefix;
    }
    
    @Override
    public Collection<NamedComponent> getComponents() {
        return Collections.unmodifiableCollection(components);
    }

    @Override
    public FaceletsLibraryDescriptor getLibraryDescriptor() {
        return libraryDescriptor;
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

    private String generateDefaultPrefix() {
        //generate a default prefix from the namespace
        String ns = getNamespace();
        final String HTTP_PREFIX = "http://"; //NOI18N
        if(ns.startsWith(HTTP_PREFIX)) {
            ns = ns.substring(HTTP_PREFIX.length());
        }
        StringTokenizer st = new StringTokenizer(ns, "/.");
        List<String> tokens = new LinkedList<String>();
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            if(token.length() > 0) {
                tokens.add(token);
            }
        }
        if(tokens.isEmpty()) {
            //shoult not happen for normal URLs
            return "lib"; //NOI18N
        }

        if(tokens.size() == 1) {
            return tokens.iterator().next();
        } else {
            StringBuilder buf = new StringBuilder();
            for(String token : tokens) {
                buf.append(token.charAt(0));
            }
            return buf.toString();
        }
    }

   
}

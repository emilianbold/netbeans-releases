/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.api.components;

import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.openide.util.Parameters;

/**
 *
 * @author alexey butenko
 */
final public class JsfComponentDescriptor {
    private final String name;
    private final String libraryName;
    private final String description;
    private final JSFVersion jsfVersion;
    private final String welcomeBody;
    private final String namespace;
    private final String nsPrefix;
    private final String defaultRenderKitId;

    public JsfComponentDescriptor(String libraryName, String name, JSFVersion jsfVersion, String description, String welcomeBody, String namespace, String nsPrefix) {
        this (libraryName, name, jsfVersion, description, welcomeBody, namespace, nsPrefix, null);
    }
    public JsfComponentDescriptor(String libraryName, String name, JSFVersion jsfVersion, 
            String description, String welcomeBody, String namespace, String nsPrefix, String defaultRenderKitId) {
        Parameters.notNull("name", name); // NOI18N
        Parameters.notNull("libraryName", libraryName); // NOI18N
        this.libraryName = libraryName;
        this.name = name;
        this.jsfVersion = jsfVersion;
        this.description = description;
        this.welcomeBody = welcomeBody;
        this.namespace = namespace;
        this.nsPrefix = nsPrefix;
        this.defaultRenderKitId = defaultRenderKitId;
    }

    public JSFVersion getJsfVersion() {
        return jsfVersion;
    }

    @Override
    public int hashCode() {
        return getLibraryName().hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JsfComponentDescriptor) {
            JsfComponentDescriptor descriptor = (JsfComponentDescriptor) obj;
            if (descriptor.getLibraryName().equals(getLibraryName())) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        if (description !=  null)
            return description;
        return getName();
    }

    public String getLibraryName() {
        return libraryName;
    };
    
    public Library getLibrary() {
        return LibraryManager.getDefault().getLibrary(getLibraryName());
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getWelcomeBody() {
        return welcomeBody;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getNsPrefix() {
        return nsPrefix;
    }
    
    /** Returns the value of default-render-kit-id element that should be placed 
     * in the application element in faces-config.xml. If <code>null</code> is 
     * returned, no default-render-kit-id element is written.
     */
    public String getDefaultRenderKitId() {
        return defaultRenderKitId;
    }
}

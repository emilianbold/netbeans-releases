/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import java.io.IOException;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import java.net.URI;
import java.util.HashMap;
import java.io.File;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Jar Catalgo Model
 *
 * @author tli
 */
public class JarCatalogModel implements CatalogModel {

    private HashMap srcs = new HashMap();
    private HashMap nss = new HashMap();

    /**
     * Add model source with key
     *
     * @param loc source uri key
     * @param ms model source
     */
    public void addModelSource(URI loc, ModelSource ms) {
        srcs.put(loc, ms);
    }

    public void addNSModelSource(String ns, ModelSource ms) {
        nss.put(ns, ms);
    }

    private Object lookup(URI uri) {
        Object ms = srcs.get(uri);
        if (ms == null) {
            ms = nss.get(uri.toString());
        }
        return ms;
    }

    private URI normalizeURI(URI uri, ModelSource ms) {
        if (!uri.isAbsolute()) {
            // if a relative uri, try to normalize using referencing model's file path
            try {
                File f = (File) ms.getLookup().lookup(File.class);
                String path = f.getParentFile().getPath().replace('\\', '/') + "/" + uri.toString();
                int idx = path.indexOf("build/SEDeployment.jar"); // NOI18N
                if (idx > 0) { // OK, needs to strip off Chris W. fixes..
                    path = f.getParentFile().getParentFile().getPath().replace('\\', '/') + "/" + uri.toString(); // NOI18N
                    path = path.substring(idx+23);
                }
                return new URI(path);
            } catch (Exception ex) {
                // failed to generate normalized uri..
            }
        }
        return uri;
    }

    public ModelSource getModelSource(URI locationURI, ModelSource modelSourceOfSourceDocument) throws CatalogModelException {
        // try lookup using the uri as is..
        Object ms = lookup(locationURI);

        // if not found, try normalize the uri.
        if (ms == null) {
            ms = lookup(normalizeURI(locationURI, modelSourceOfSourceDocument));
        }
        return ((ms != null) ? ((ModelSource) ms) : null);
    }

    public ModelSource getModelSource(URI locationURI) throws CatalogModelException {
        return getModelSource(locationURI, null);
    }

    
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        //TODO FIXME: this is to fix a build break. Please implement this method.
        return null;
    }

    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        //TODO FIXME: this is to fix a build break. Please implement this method.
        return null;
    }

}

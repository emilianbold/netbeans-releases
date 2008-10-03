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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.wsdl.config;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openide.util.ImageUtilities;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;

/** Catalog for webservice related schemas that enables completion support in
 *  editor.
 *
 *  Original code before webservice modifications taken from DDCatalog.java in web/core
 *
 * @author Peter Williams
 *
 */
public class JaxRpcSchemaCatalog implements CatalogReader, CatalogDescriptor, EntityResolver  {

    public static final String JAXRPC_CONFIG_1_1 = "http://java.sun.com/xml/ns/jax-rpc/ri/config"; // NOI18N
    private static final String URL_JAXRPC_CONFIG_1_1 = "nbres:/org/netbeans/modules/websvc/wsdl/config/resources/jax-rpc-ri-config_1_1.xsd"; // NOI18N
    
    public JaxRpcSchemaCatalog() {
    }

    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public Iterator<String> getPublicIDs() {
        List<String> list = new ArrayList<String>();
        //list.add(JAXRPC_CONFIG_1_1_ID);
        list.add(JAXRPC_CONFIG_1_1);
        return list.listIterator();
    }

    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId) {
        if(JAXRPC_CONFIG_1_1.equals(publicId)) {
            return URL_JAXRPC_CONFIG_1_1;
        } else {
            return null;
        }
    }

    /**
     * Refresh content according to content of mounted catalog.
     */
    public void refresh() {
    }

    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void addCatalogListener(CatalogListener l) {
    }

    /**
     * Optional operation couled with addCatalogListener.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void removeCatalogListener(CatalogListener l) {
    }

    /** Registers new listener.  */
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    /**
     * @return I18N display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage(JaxRpcSchemaCatalog.class, "LBL_JaxRpcSchemaCatalog"); // NOI18N
    }

    /**
     * Return visualized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    public java.awt.Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/websvc/wsdl/config/resources/JaxRpcSchemaCatalog.png"); // NOI18N
    }

    /**
     * @return I18N short description
     */
    public String getShortDescription() {
        return NbBundle.getMessage (JaxRpcSchemaCatalog.class, "DESC_JaxRpcSchemaCatalog");
    }

    /** Unregister the listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    /**
     * Resolves schema definition file for deployment descriptor (spec.2_4)
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if(JAXRPC_CONFIG_1_1.equals(publicId)) {
            return new InputSource(URL_JAXRPC_CONFIG_1_1);
        } else {
            return null;
        }
    }

    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    public String resolveURI(String name) {
        if(JAXRPC_CONFIG_1_1.equals(name)) {
            return URL_JAXRPC_CONFIG_1_1;
        }

        return null;
    }
    /**
     * Get registered URI for the given publicId or null if not registered.
     * @return null if not registered
     */
    public String resolvePublic(String publicId) {
        return null;
    }
}

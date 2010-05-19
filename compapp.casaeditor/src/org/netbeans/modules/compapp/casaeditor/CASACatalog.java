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
package org.netbeans.modules.compapp.casaeditor;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.util.Map;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Copied from DDCatalog
 *
 * Public ID is an identifier with the "SCHEMA:" prefix.
 * Actually it can be anything, but it's required to have the prefix because
 * of the "XML Entity Catalog" module. See the method
 * org.netbeans.modules.xml.catalog.CatalogEntry.getSystemIDValue();.
 * According to the method, a PublicID is considered as SystemID if it starts with
 * the "SHEMA:" prefix!
 * <p>
 * System ID for a Schema files should be the same as its targetNamespace. 
 *
 * @author ads
 * @author Nikita Krjukov
 */
public class CASACatalog implements CatalogReader, CatalogDescriptor, EntityResolver {


    private static final String CASA_SYSTEM_ID =
            "http://java.sun.com/xml/ns/casa"; // NOI18N

    private static final String CASA_XSD_URL =
            "nbres:/org/netbeans/modules/compapp/casaeditor/resources/casa.xsd"; // NOI18N

    public static final String SCHEMA = "SCHEMA:"; // NOI18N
    //
    // pseudo DTD for code-completion?
    //
    private static final String CASA_ID = SCHEMA + CASA_SYSTEM_ID;

    private static final String CASA_IMAGE_PATH =
            "org/netbeans/modules/compapp/casaeditor/resources/service_composition_16.png"; // NOI18N

    public CASACatalog() {
    }

    /**
     * 
     * @return CASACatalog instance founded in Lookup. Could be null
     */
    public static CASACatalog getDefault() {
        CASACatalog casaCatalog = null;

        Lookup.Template templ = new Lookup.Template(CatalogReader.class);
        Lookup userCatalogLookup = Lookups.forPath("Plugins/XML/UserCatalogs"); // NOI18N

        Lookup.Result res = userCatalogLookup.lookup(templ);
        Collection impls = res.allInstances();

        for (Object obj : impls) {
            if (obj instanceof CASACatalog) {
                casaCatalog = (CASACatalog) obj;
                break;
            }
        }
        casaCatalog.refresh();

        return casaCatalog;
    }

    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public Iterator<String> getPublicIDs() {

        List<String> list = new ArrayList<String>();
        list.add(CASA_ID);

        return list.listIterator();
    }

    /**
     * Returns publicId by systemId
     * @param systemId
     * @return
     */
    public String getPublicId(String systemId) {
        if (CASA_SYSTEM_ID.equals(systemId)) {
            return CASA_ID;
        }
        return null;
    }

    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId) {

        if (CASA_ID.equals(publicId)) {
            return CASA_SYSTEM_ID;
        }

        return null;
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

    /** 
     * Registers new listener
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    /** 
     * Unregisters the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    /**
     * @return I18N display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage(CASACatalog.class, "LBL_CASACatalog");
    }

    /**
     * Return visuaized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(CASA_IMAGE_PATH);
    }

    /**
     * @return I18N short description
     */
    public String getShortDescription() {
        return NbBundle.getMessage(CASACatalog.class, "DESC_CASACatalog");
    }

    /**
     * Resolves schema definition file for deployment descriptor (spec.2_4)
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for 
     */
    public InputSource resolveEntity(String publicId, String systemId) 
            throws SAXException, IOException {

        if (CASA_SYSTEM_ID.equals(systemId)) {
            return new org.xml.sax.InputSource(CASA_XSD_URL);
        }
        return null;
    }

    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    public String resolveURI(String name) {
        
        if (CASA_SYSTEM_ID.equals(name)) {
            return CASA_SYSTEM_ID;
        }
        
        return null;
    }

    public String resolvePublic(String publicId) {
        return null;
    }
}

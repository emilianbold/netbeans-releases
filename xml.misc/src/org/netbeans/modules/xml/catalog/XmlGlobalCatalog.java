/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xml.catalog;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;

import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2010.01.25
 */
public abstract class XmlGlobalCatalog implements CatalogReader, CatalogDescriptor, EntityResolver {

    protected XmlGlobalCatalog(String name, String description, String icon) {
        myIcon = new ImageIcon(getClass().getResource(icon)).getImage();
        myName = i18n(getClass(), name);
        myDescription = i18n(getClass(), description);
        myEntries = new ArrayList<XmlGlobalCatalogEntry>();
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (systemId == null) {
            return null;
        }
        for (XmlGlobalCatalogEntry entry : myEntries) {
            if (systemId.equals(entry.getSystemId()) || systemId.equals(entry.getLocation())) {
                return new InputSource(entry.getSource());
            }
        }
        return null;
    }

    public Iterator<String> getPublicIDs() {
        List<String> publicIDs = new ArrayList<String>();

        for (XmlGlobalCatalogEntry entry : myEntries) {
            publicIDs.add(entry.getPublicId());
        }
        return publicIDs.listIterator();
    }

    public String getSystemID(String publicId) {
        if (publicId == null) {
            return null;
        }
        for (XmlGlobalCatalogEntry entry : myEntries) {
            if (publicId.equals(entry.getPublicId())) {
                return entry.getSystemId();
            }
        }
        return null;
    }

    public String resolveURI(String name) {
        if (name == null) {
            return null;
        }
        for (XmlGlobalCatalogEntry entry : myEntries) {
            if (name.equals(entry.getSystemId())) {
                return entry.getLocation();
            }
        }
        return null;
    }

    public String resolvePublic(String publicId) {
        if (publicId == null) {
            return null;
        }
        for (XmlGlobalCatalogEntry entry : myEntries) {
            if (publicId.equals(entry.getPublicId())) {
                return entry.getLocation();
            }
        }
        return null;
    }

    public String getDisplayName() {
        return myName;
    }

    public String getShortDescription() {
        return myDescription;
    }

    public Image getIcon(int type) {
        return myIcon;
    }

    public void refresh() {}
    
    public void addCatalogListener(CatalogListener l) {}
    
    public void removeCatalogListener(CatalogListener l) {}
    
    public void addPropertyChangeListener(PropertyChangeListener l) {}

    public void removePropertyChangeListener(PropertyChangeListener l) {}

    public static final CatalogReader getBpelGlobalCatalog() {
        return getGlobalCatalog(".bpel."); // NOI18N
    }

    @SuppressWarnings("unchecked") // NOI18N
    public static final CatalogReader getGlobalCatalog(String infix) {
        Lookup lookup = Lookups.forPath("Plugins/XML/UserCatalogs"); // NOI18N
        Lookup.Template template = new Lookup.Template(CatalogReader.class);
        Lookup.Result result = lookup.lookup(template);
        Collection catalogs = result.allInstances();

        for (Object catalog : catalogs) {
            if ( !(catalog instanceof CatalogReader)) {
                continue;
            }
            if (catalog.getClass().getName().contains(infix)) {
                return (CatalogReader) catalog;
            }
        }
        return null;
    }

    protected final void registerEntry(String publicId, String systemId, String location, String source) {
        myEntries.add(new XmlGlobalCatalogEntry(publicId, systemId, location, source));
    }

    private Image myIcon;
    private String myName;
    private String myDescription;
    private List<XmlGlobalCatalogEntry> myEntries;
    public static final String SCHEMA = "SCHEMA:"; // NOI18N
}

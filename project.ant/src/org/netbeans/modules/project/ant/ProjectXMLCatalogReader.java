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

package org.netbeans.modules.project.ant;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Supplies a catalog which lets users validate against project-related XML schemas.
 * @author Jesse Glick
 * @see "issue #49976"
 */
public class ProjectXMLCatalogReader implements CatalogReader, CatalogDescriptor {
    
    private static final String PREFIX = "http://www.netbeans.org/ns/"; // NOI18N
    private static final String EXTENSION = "xsd"; // NOI18N
    private static final String CATALOG = "ProjectXMLCatalog"; // NOI18N
    
    /** Default constructor for use from layer. */
    public ProjectXMLCatalogReader() {}

    public String resolveURI(String name) {
        if (name.startsWith(PREFIX)) {
            FileObject rsrc = Repository.getDefault().getDefaultFileSystem().findResource(CATALOG + "/" + name.substring(PREFIX.length()) + "." + EXTENSION);
            if (rsrc != null) {
                try {
                    return rsrc.getURL().toString();
                } catch (FileStateInvalidException x) {
                    Exceptions.printStackTrace(x);
                }
            }
        }
        return null;
    }

    public String resolvePublic(String publicId) {
        return null;
    }

    public String getSystemID(String publicId) {
        return null;
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {}

    public void addPropertyChangeListener(PropertyChangeListener l) {}

    public void removeCatalogListener(CatalogListener l) {}

    public void addCatalogListener(CatalogListener l) {}

    public Image getIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/project/ui/resources/projectTab.png", true);
    }

    public void refresh() {}

    public String getShortDescription() {
        return NbBundle.getMessage(ProjectXMLCatalogReader.class, "HINT_project_xml_schemas");
    }

    public Iterator getPublicIDs() {
        return Collections.EMPTY_SET.iterator();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ProjectXMLCatalogReader.class, "LBL_project_xml_schemas");
    }

    /**
     * Validate according to all *.xsd found in catalog.
     * @param dom DOM fragment to validate
     * @throws SAXException if schemas were malformed or the document was invalid
     */
    public static void validate(Element dom) throws SAXException {
        if (Repository.getDefault().getDefaultFileSystem().findResource(CATALOG) == null) {
            // Probably running from inside a unit test which overrides the system filesystem.
            // Safest and simplest to just skip validation in this case.
            return;
        }
        XMLUtil.validate(dom, projectXmlCombinedSchema());
    }

    private static Schema LAST_USED_SCHEMA;
    private static int LAST_USED_SCHEMA_HASH;
    /** Load ProjectXMLCatalog/**.xsd. Cache the combined schema between runs if the content has not changed. */
    private static synchronized Schema projectXmlCombinedSchema() {
        int hash = 0; // compute hash regardless of ordering of schemas, hence XOR
        List<FileObject> schemas = new ArrayList<FileObject>();
        FileObject root = Repository.getDefault().getDefaultFileSystem().findResource(CATALOG);
        if (root != null) {
            for (FileObject f : NbCollections.iterable(root.getChildren(true))) {
                if (f.isData() && f.hasExt(EXTENSION)) {
                    schemas.add(f);
                    hash ^= f.getPath().hashCode();
                    hash ^= f.getSize(); // probably close enough
                }
            }
        }
        if (LAST_USED_SCHEMA == null || hash != LAST_USED_SCHEMA_HASH) {
            List<Source> sources = new ArrayList<Source>();
            // nbfs URLs don't seem to work from unit tests, so need to use InputStream constructor
            List<InputStream> streams = new ArrayList<InputStream>();
            try {
                for (FileObject f : schemas) {
                    try {
                        InputStream is = f.getInputStream();
                        streams.add(is);
                        sources.add(new StreamSource(is, f.getURL().toString()));
                    } catch (IOException x) {
                        Exceptions.printStackTrace(x);
                    }
                }
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                try {
                    LAST_USED_SCHEMA = schemaFactory.newSchema(sources.toArray(new Source[sources.size()]));
                    LAST_USED_SCHEMA_HASH = hash;
                } catch (SAXException x) {
                    // Try to determine the culprit and report appropriately.
                    for (FileObject f : schemas) {
                        try {
                            schemaFactory.newSchema(new StreamSource(f.getURL().toString()));
                        } catch (Exception x2) {
                            Exceptions.attachMessage(x2, "While parsing: " + f.getPath()); // NOI18N
                            Exceptions.printStackTrace(x2);
                        }
                    }
                    // Report whole problem, just in case it is due to e.g. merging of schemas together.
                    Exceptions.printStackTrace(x);
                    // Suppress schema validation until fixed.
                    try {
                        return schemaFactory.newSchema();
                    } catch (SAXException x2) {
                        throw new AssertionError(x2);
                    }
                }
            } finally {
                for (InputStream is : streams) {
                    try {
                        is.close();
                    } catch (IOException x) {
                        Exceptions.printStackTrace(x);
                    }
                }
            }
        }
        return LAST_USED_SCHEMA;
    }
    
}

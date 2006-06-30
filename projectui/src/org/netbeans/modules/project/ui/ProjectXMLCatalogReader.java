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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Supplies a catalog which lets users validate against project-related XML schemas.
 * @author Jesse Glick
 * @see "issue #49976"
 */
public class ProjectXMLCatalogReader implements CatalogReader, CatalogDescriptor {
    
    private static final String PREFIX = "http://www.netbeans.org/ns/"; // NOI18N
    private static final String SUFFIX = ".xsd"; // NOI18N
    
    /** Default constructor for use from layer. */
    public ProjectXMLCatalogReader() {}

    public String resolveURI(String name) {
        if (name.startsWith(PREFIX)) {
            return name + SUFFIX;
        } else {
            return null;
        }
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
        return Utilities.loadImage("org/netbeans/modules/project/ui/resources/projectTab.gif", true);
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
    
}

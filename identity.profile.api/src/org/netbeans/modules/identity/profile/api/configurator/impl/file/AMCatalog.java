/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.identity.profile.api.configurator.impl.file;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Copied from DDCatalog
 *
 * @author ads
 */
public class AMCatalog implements CatalogReader, CatalogDescriptor,
    EntityResolver  
{
    private static final String RESOURCES_DIR = 
            "nbres:/org/netbeans/modules/identity/profile/api/configurator/impl/file/resources/"; //NOI18N
    
    private static final String URI_AMCONFIG_1_0 = 
            RESOURCES_DIR + "AccessManagerConfig_1_0.xsd"; //NOI18N

    private static final String AMCONFIG_1_0 = 
            "http://identity.netbeans.org/access_manager_config_1_0"; //NOI18N
    
    private static final String SCHEMA = "SCHEMA:"; //NOI18N
    
    private static final String AMCONFIG_1_0_ID = SCHEMA + AMCONFIG_1_0;
    
    private static final String IMAGE_PATH = 
            "org/netbeans/modules/identity/profile/api/configurator/impl/file/resources/ServerManager.png"; //NOI18N
    
	
    public AMCatalog() {
    }
    
    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public java.util.Iterator getPublicIDs() {
        ArrayList<String> ids = new ArrayList<String>();
        ids.add(AMCONFIG_1_0_ID);
        
        return ids.iterator();
    }
    
    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId) {
        if (AMCONFIG_1_0_ID.equals(publicId)) {
            return URI_AMCONFIG_1_0;
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
    
    /** Registers new listener.  */
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }
    
    /**
     * @return I18N display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage (AMCatalog.class, "LBL_AMCatalog");
    }
    
    /**
     * Return visuaized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    public Image getIcon(int type) {
        return Utilities.loadImage(IMAGE_PATH);
    }
    
    /**
     * @return I18N short description
     */
    public String getShortDescription() {
        return NbBundle.getMessage (AMCatalog.class, "DESC_AMCatalog");
    }
    
    /** Unregister the listener.  */
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }
    
    /**
     * Resolves schema definition file for deployment descriptor (spec.2_4)
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for 
     */    
    public InputSource resolveEntity(String publicId, String systemId) 
        throws SAXException, IOException 
    {
	return null;
    }
    
    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    public String resolveURI(String name) {
        if (AMCONFIG_1_0.equals(name)) {
            return URI_AMCONFIG_1_0;
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

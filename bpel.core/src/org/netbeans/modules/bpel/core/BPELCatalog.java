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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.core;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.resources.ResourcePackageMarker;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.schema.resources.ResourceMarker;
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
public class BPELCatalog implements CatalogReader, CatalogDescriptor,
    EntityResolver  
{
	    
    private static final String URL_BPEL_1_1 =
		"nbres:/" +
        ResourcePackageMarker.class.getPackage().getName().
        replace( '.', '/')+ "/" +  
        ResourcePackageMarker.WS_BPEL_1_1_SCHEMA;
    
    private static final String URL_BPEL_1_1_DTD =
		"nbres:/org/netbeans/modules/bpel/core/resources/" +        // NOI18N
        "bpel4ws_1_1.dtd";                                          // NOI18N
    
	private static final String BPEL_1_1 =
		"http://schemas.xmlsoap.org/ws/2003/03/business-process/";  // NOI18N

    private static final String URL_BPEL_2_0 =
		"nbres:/"+                                                  // NOI18N
        ResourcePackageMarker.class.getPackage().getName().
        replace( '.', '/')+ "/" +                                   // NOI18N
        ResourcePackageMarker.WS_BPEL_SCHEMA; 
    
	private static final String BPEL_2_0 = BpelEntity.BUSINESS_PROCESS_NS_URI;
		
	private static final String SCHEMA = "SCHEMA:";                 // NOI18N
	//
	// pseudo DTD for code-completion?
	// MCF - I am not doing pseudo DTD for BPEL 2.0
	//
	private static final String BPEL_1_1_ID = SCHEMA + BPEL_1_1;
	private static final String BPEL_2_0_ID = SCHEMA + BPEL_2_0;
	
    private static final String URL_BPEL_PLT_1_1 =
		"nbres:/org/netbeans/modules/bpel/core/resources/"          // NOI18N
        +"bpel4ws_1_1_plinkType.xsd";                               // NOI18N
    
	private static final String BPEL_PLT_1_1 =
		"http://schemas.xmlsoap.org/ws/2003/05/partner-link/";      // NOI18N
    
	private static final String BPEL_PLT_1_1_ID = SCHEMA + BPEL_PLT_1_1;
	
    private static final String URL_BPEL_PLT_2_0 =
		"nbres:/" + ResourceMarker.class.getPackage().getName().     // NOI18N 
        replace( '.', '/')+ "/" + ResourceMarker.PLNK_SCHEMA;

    
	private static final String BPEL_PLT_2_0 = BPELQName.PLNK_NS;  
    
	private static final String BPEL_PLT_2_0_ID = SCHEMA + BPEL_PLT_2_0;	
	
    private static final String IMAGE_PATH = 
        "org/netbeans/modules/bpel/core/resources/" +               // NOI18N
        "bpel_catalog.gif";                                         // NOI18N
	
    public BPELCatalog() {
    }
    
    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public Iterator getPublicIDs() {
        List<String> list = new ArrayList<String>();
        list.add(BPEL_1_1_ID);
        list.add(BPEL_PLT_1_1_ID);
        list.add(BPEL_2_0_ID);
        list.add(BPEL_PLT_2_0_ID);
        return list.listIterator();
    }
    
    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId) {
        if (BPEL_2_0_ID.equals(publicId)) {
			return URL_BPEL_2_0;
            // FOR Code Complete? return URL_BPEL_2_0_DTD;
        }
		else if (BPEL_PLT_2_0_ID.equals(publicId)) {
			return URL_BPEL_PLT_2_0;
        }
        else if (BPEL_1_1_ID.equals(publicId)) {
			return URL_BPEL_1_1;
            // FOR Code Complete? return URL_BPEL_1_1_DTD;
        }
		else if (BPEL_PLT_1_1_ID.equals(publicId)) {
			return URL_BPEL_PLT_1_1;
        }
        else {
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
        return NbBundle.getMessage (BPELCatalog.class, "LBL_BPELCatalog");
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
        return NbBundle.getMessage (BPELCatalog.class, "DESC_BPELCatalog");
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
        if (BPEL_2_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_BPEL_2_0);
        }
		else if (BPEL_PLT_2_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_BPEL_PLT_2_0);
        }
        else if (BPEL_1_1.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_BPEL_1_1);
        }
		else if (BPEL_PLT_1_1.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_BPEL_PLT_1_1);
        }
        else {
            return null;
        }
    }
    
    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    public String resolveURI(String name) {
		if(BPEL_2_0.equals(name)) {
			return BPEL_2_0;
        }
		else if(BPEL_PLT_2_0.equals(name)) {
			return BPEL_PLT_2_0;
        }
		else if(BPEL_1_1.equals(name)) {
			return BPEL_1_1;
        }
		else if(BPEL_PLT_1_1.equals(name)) {
			return BPEL_PLT_1_1;
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

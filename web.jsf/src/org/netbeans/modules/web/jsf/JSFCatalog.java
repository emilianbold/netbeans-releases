/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * StrutsCatalog.java
 *
 * Created on April 24, 2002, 10:38 PM
 */

package org.netbeans.modules.web.jsf;

import org.netbeans.modules.xml.catalog.spi.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
/**
 *
 * @author  Petr Pisl
 */
public class JSFCatalog implements CatalogReader, CatalogDescriptor, org.xml.sax.EntityResolver {
    
    private static final String JSF_ID_1_0 = "-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.0//EN"; // NOI18N
    private static final String JSF_ID_1_1 = "-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.1//EN"; // NOI18N
    
    private static final String URL_JSF_1_0 ="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_1_0.dtd";
    private static final String URL_JSF_1_1 ="nbres:/org/netbeans/modules/web/jsf/resources/web-facesconfig_1_1.dtd";
    
    
    /** Creates a new instance of StrutsCatalog */
    public JSFCatalog() {
    }
    
    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public java.util.Iterator getPublicIDs() {
        java.util.List list = new java.util.ArrayList();
        list.add(JSF_ID_1_0);
        list.add(JSF_ID_1_1);
        return list.listIterator();
    }
    
    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId) {
        if (JSF_ID_1_0.equals(publicId))
            return URL_JSF_1_0;
        else if (JSF_ID_1_1.equals(publicId))
            return URL_JSF_1_1;
        else return null;
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
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    }
    
     /** Unregister the listener.  */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }
    
    /**
     * @return I18N display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage (JSFCatalog.class, "LBL_JSFCatalog");
    }
    
    /**
     * Return visuaized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    public java.awt.Image getIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/web/jsf/resources/JSFCatalog.gif"); // NOI18N
    }
    
    /**
     * @return I18N short description
     */
    public String getShortDescription() {
        return NbBundle.getMessage (JSFCatalog.class, "DESC_JSFCatalog");
    }
    
   /**
     * Resolves schema definition file for taglib descriptor (spec.1_1, 1_2, 2_0)
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for publisId, 
     */    
    public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) throws org.xml.sax.SAXException, java.io.IOException {
       if (JSF_ID_1_0.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_JSF_1_0);
        } else if (JSF_ID_1_1.equals(systemId)) {
            return new org.xml.sax.InputSource(URL_JSF_1_1);
        } else {
            return null;
        }
    }
    
    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    public String resolveURI(String name) {
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

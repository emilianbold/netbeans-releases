/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.catalog.spi;

import java.awt.Image;
import java.beans.*;

/**
 * It provides information about a catalog instance.
 * Information about the class can be provided as BeanInfo.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public interface CatalogDescriptor {

    /**
     * Name of icon property if fired.
     */
    public static final String PROP_CATALOG_ICON = "ca-icon"; // NOI18N
    
    /**
     * Name of name property
     */
    public static final String PROP_CATALOG_NAME = "ca-name"; // NOI18N
    
    /**
     * Name of short description property
     */
    public static final String PROP_CATALOG_DESC = "ca-desc"; // NOI18N
    
    /**
     * Return visuaized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    public Image getIcon(int type);
    
    /**
     * @return I18N display name
     */
    public String getDisplayName();
    
    /**
     * @return I18N short description
     */
    public String getShortDescription();
    
    /** Registers new listener. */
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    /** Unregister the listener. */
    public void removePropertyChangeListener(PropertyChangeListener l);
}

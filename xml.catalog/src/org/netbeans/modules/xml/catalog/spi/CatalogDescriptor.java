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

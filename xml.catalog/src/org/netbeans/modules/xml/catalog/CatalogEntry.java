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
package org.netbeans.modules.xml.catalog;

/**
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class CatalogEntry extends Object {

    private String systemID;
    private String publicID;
            
    /** Creates new CatalogEntry */
    public CatalogEntry(String publicID, String systemID) {
        this.systemID = systemID;
        this.publicID = publicID;
    }
    
    public String getSystemID() {
        return systemID;
    }
    
    public String getPublicID() {
        return publicID;
    }
    
    public String getName() {
        return publicID;
    }
    
    public String toString() {
        return publicID + " => " + systemID; // NOI18N
    }
}

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
package org.netbeans.tax.io;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public abstract class TreeEntityResolver {
    
    /** Resolve entity.
     * @param publicId Public Identifier.
     * @param systemId System Identifier.
     * @param baseSystemId Base System Identifier.
     * @return Resolved entity or <CODE>null</CODE>.
     */
    public abstract TreeInputSource resolveEntity (String publicId, String systemId, String baseSystemId);
    
    /** Resolve entity.
     * @param publicId Public Identifier.
     * @param systemId System Identifier.
     * @return Resolved entity or <CODE>null</CODE>.
     */
    public TreeInputSource resolveEntity (String publicId, String systemId) {
        return resolveEntity (publicId, systemId, null);
    }
    
    /** Expand system identifier.
     * @param systemId System Identifier.
     * @param baseSystemId Base System Identifier.
     * @return Resolver system identifier.
     */
    public String expandSystemId (String systemId, String baseSystemId) {
        Util.THIS.debug ("[PENDING]: TreeEntityResolver.expandSystemId ( " + systemId + " , " + baseSystemId + " ) : " + systemId); // NOI18N
        
        return systemId;
    }
    
    /** Expand system identifier.
     * @param systemId System Identifier.
     * @return Expanded system identifier.
     */
    public String expandSystemId (String systemId) {
        return expandSystemId (systemId, null);
    }
    
}

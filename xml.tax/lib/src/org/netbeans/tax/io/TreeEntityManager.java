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
package org.netbeans.tax.io;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public final class TreeEntityManager extends TreeEntityResolver {

    /** Creates new TreeEntityManager */
    public TreeEntityManager () {
    }

    /** Resolve entity.
     * @param publicId Public Identifier.
     * @param systemId System Identifier.
     * @param baseSystemId Base System Identifier
     * @return Resolved entity or <CODE>null</CODE>.
     */
    public TreeInputSource resolveEntity (String publicId, String systemId, String baseSystemId) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeEntityManager.resolveEntity ( " + publicId + " , " + systemId + " , " + baseSystemId + " ) : null"); // NOI18N
        return null;
    }
    
    /** Add entity resolver to list of used resolvers.
     * @param entityResolver entity resolver to add
     */
    public void addEntityResolver (TreeEntityResolver entityResolver) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeEntityManager.addEntityResolver ( " + entityResolver.getClass ().getName () + " )"); // NOI18N
    }
    
    /** Remove entity resolver from list of used.
     * @param entityResolver entity resolver to remove
     */
    public void removeEntityResolver (TreeEntityResolver entityResolver) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeEntityManager.removeEntityResolver ( " + entityResolver.getClass ().getName () + " )"); // NOI18N
    }
    
    /** Resolve entity.
     * @param publicId Public Identifier.
     * @param systemId System Identifier.
     * @return Resolved entity or <CODE>null</CODE>.
     */
    public TreeInputSource resolveEntity (String publicId, String systemId) {
        TreeInputSource retValue;
        
        retValue = super.resolveEntity (publicId, systemId);
        return retValue;
    }
    
    /** Expand system identifier.
     * @param systemId System Identifier.
     * @param baseSystemId Base System Identifier.
     * @return Resolver system identifier.
     */
    public String expandSystemId (String systemId, String baseSystemId) {
        String retValue;
        
        retValue = super.expandSystemId (systemId, baseSystemId);
        return retValue;
    }
    
    /** Expand system identifier.
     * @param systemId System Identifier.
     * @return Expanded system identifier.
     */
    public String expandSystemId (String systemId) {
        String retValue;
        
        retValue = super.expandSystemId (systemId);
        return retValue;
    }
    
}

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
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeEntityResolver.expandSystemId ( " + systemId + " , " + baseSystemId + " ) : " + systemId); // NOI18N
        
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

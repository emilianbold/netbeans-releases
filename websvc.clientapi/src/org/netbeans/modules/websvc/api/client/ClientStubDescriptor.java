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

package org.netbeans.modules.websvc.api.client;

/** Note: this class is not final, so as to allow implementors to derive from
 *  it to add additional implementation dependent properties such as wscompile
 *  features.
 *
 * @author Martin Grebac
 */
public class ClientStubDescriptor {

    /** Client types.  Not to be intermixed with service types.
     */
    /** Key to represent jsr-109 static stub clients
     *
     *  Note: This string may be embedded in build-impl.xsl for the projects
     *  that implement web service client support.  Change with care.
     */
    public static final String JSR109_CLIENT_STUB = "jsr-109_client";
    
    /** Key to represent jaxrpc static stub clients.
     *
     *  Note: This string may be embedded in build-impl.xsl for the projects
     *  that implement web service client support.  Change with care.
     */
    public static final String JAXRPC_CLIENT_STUB = "jaxrpc_static_client";
    
    // Private data
    private final String name;
    private final String displayName;

    public ClientStubDescriptor(final String name, final String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String toString() {
        return displayName;
    }
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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

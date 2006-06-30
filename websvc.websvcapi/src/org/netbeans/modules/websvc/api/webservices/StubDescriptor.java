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

package org.netbeans.modules.websvc.api.webservices;

/** Note: this class is not final, so as to allow implementors to derive from
 *  it to add additional implementation dependent properties such as wscompile
 *  features.
 *
 * @author Peter Williams, Martin Grebac
 */
public class StubDescriptor {

    /** Key to represent services generated from interface, e.g. SEI is under
     *  source control and the WSDL file is generated via wscompile.
     */
    public static final String SEI_SERVICE_STUB = "sei_service";

    /** Key to represent services generated from a preexisting WSDL file, e.g.
     *  wsdl file is under source control and the interface files are generated
     *  from it.
     */
    public static final String WSDL_SERVICE_STUB = "wsdl_service";
    
    
    // Private data
    private final String name;
    private final String displayName;

    public StubDescriptor(final String name, final String displayName) {
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

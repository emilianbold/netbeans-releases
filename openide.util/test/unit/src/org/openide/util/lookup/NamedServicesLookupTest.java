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

package org.openide.util.lookup;

import org.openide.util.Lookup;


/** Test finding services from manifest.
 * @author Jaroslav Tulach
 */
public class NamedServicesLookupTest extends MetaInfServicesLookupTest {
    public NamedServicesLookupTest(String name) {
        super(name);
    }
    
    protected String prefix() {
        return "META-INF/namedservices/sub/path/";
    }
    
    protected Lookup createLookup(ClassLoader c) {
        ClassLoader prev = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(c);
        Lookup l = Lookups.forPath("sub/path");
        Thread.currentThread().setContextClassLoader(prev);
        return l;
    }
    
    //
    // this is not much inheriting test, as we mask most of the tested methods
    // anyway, but the infrastructure to generate the JAR files is useful
    //
    
    public void testLoaderSkew() throws Exception {
    }

    public void testStability() throws Exception {
    }

    public void testMaskingOfResources() throws Exception {
    }

    public void testOrdering() throws Exception {
    }

    public void testNoCallToGetResourceForObjectIssue65124() throws Exception {
    }

    public void testListenersAreNotifiedWithoutHoldingALockIssue36035() throws Exception {
    }
    
    public void testWrongOrderAsInIssue100320() throws Exception {
    }    
    
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Nokia. Portions Copyright 2006 Nokia. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.jnlplauncher;

import java.net.URL;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import junit.framework.*;
import java.security.*;

/** 
 * Test whether the system property netbeans.jnlp.fixPolicy
 * really grants everything to everybody.
 * @author David Strupl
 */
public class FixPolicyTest extends TestCase {
    
    private URL url = null;
    
    public FixPolicyTest(String testName) {
        super(testName);
    }

    public void testFixPolicy() throws Exception {
        
        url = new URL("http://boo.baa");
        System.setProperty("netbeans.jnlp.fixPolicy", "true");
        
        Main.fixPolicy();
        
        assertTrue(Policy.getPolicy().implies(
            new ProtectionDomain(
                new CodeSource(
                    url,
                    new java.security.cert.Certificate[0]
                ),
                new AllPermission().newPermissionCollection()),
                new AllPermission()
        ));
    }

}

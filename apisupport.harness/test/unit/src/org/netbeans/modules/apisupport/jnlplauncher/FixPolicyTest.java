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
 * Software is Nokia. Portions Copyright 2006 Nokia. All Rights Reserved.
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

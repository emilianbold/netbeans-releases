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
package org.netbeans.core.startup;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class CLIOptionsTest extends NbTestCase {

    public CLIOptionsTest(String testName) {
        super(testName);
    }

    public void testNoSplash() {
        new CLIOptions().cli(new String[] { "--branding", "noexiting" });
        assertFalse("Splash is on in default branding", CLIOptions.isNoSplash());

	CLIOptions.defaultsLoaded = false;
        new CLIOptions().cli(new String[] { "--branding", "nosplash" });
        assertTrue("this branding disables the splash", CLIOptions.isNoSplash());
        
	CLIOptions.defaultsLoaded = false;
        new CLIOptions().cli(new String[] { "--branding", "noexiting", "--nosplash"});
        assertTrue("Splash is explicitly disabled", CLIOptions.isNoSplash());
    }
    
}

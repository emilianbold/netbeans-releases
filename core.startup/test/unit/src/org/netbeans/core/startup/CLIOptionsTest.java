/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        new CLIOptions().cli(new String[] { "--branding", "nosplash" });
        assertTrue("this branding disables the splash", CLIOptions.isNoSplash());

        new CLIOptions().cli(new String[] { "--branding", "noexiting" });
        assertFalse("Splash is on in default branding", CLIOptions.isNoSplash());
        
        new CLIOptions().cli(new String[] { "--branding", "noexiting", "--nosplash"});
        assertTrue("Splash is explicitly disabled", CLIOptions.isNoSplash());
    }
    
}

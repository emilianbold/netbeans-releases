/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.wizards.shortcut;

import org.netbeans.junit.NbTestCase;

/**
 * Test functionality of ShortcutWizard.
 * @author Jesse Glick
 */
public class ShortcutWizardTest extends ShortcutWizardTestBase {
    
    public ShortcutWizardTest(String name) {
        super(name);
    }
    
    public void testGetTargetBaseName() throws Exception {
        assertEquals("correct target name", "my-proj-targ1", wiz.getTargetBaseName());
    }
    
    // XXX test getContents(), finish(), etc.
    
}

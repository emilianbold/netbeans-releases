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

package org.openide.awt;

import java.awt.event.KeyEvent;
import javax.swing.JButton;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

/** Test use of mnemonics.
 * @author Jesse Glick
 */
public class MnemonicsTest extends NbTestCase {
    
    public MnemonicsTest(String name) {
        super(name);
    }
    
    // XXX testSetLocalizedText, testFindMnemonicAmpersand
    
    /** @see #31093 */
    public void testMnemonicAfterParens() throws Exception {
        JButton b = new JButton();
        Mnemonics.setLocalizedText(b, "Execute (&Force Reload)");
        assertEquals("Execute (Force Reload)", b.getText());
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            assertEquals(0, b.getMnemonic());
            assertEquals(-1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals(KeyEvent.VK_F, b.getMnemonic());
            assertEquals(9, b.getDisplayedMnemonicIndex());
        }
        assertEquals("Execute (Force Reload)", Actions.cutAmpersand("Execute (&Force Reload)"));
        // XXX test that actual Japanese mnemonics work as expected...
    }
    
}

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
    
    public void testMnemonicHTML() throws Exception {
        JButton b = new JButton();
        Mnemonics.setLocalizedText(b, "<html><b>R&amp;D</b> department");
        assertEquals("<html><b>R&amp;D</b> department", b.getText());
        assertEquals(0, b.getMnemonic());
        assertEquals(-1, b.getDisplayedMnemonicIndex());
        Mnemonics.setLocalizedText(b, "<html><b>R&amp;D</b> departmen&t");
        assertEquals("<html><b>R&amp;D</b> department", b.getText());
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            assertEquals(0, b.getMnemonic());
            assertEquals(-1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals(KeyEvent.VK_T, b.getMnemonic());
        }
        
        Mnemonics.setLocalizedText(b, "<html>Smith &amp; &Wesson");
        assertEquals("<html>Smith &amp; Wesson", b.getText());
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            assertEquals(0, b.getMnemonic());
            assertEquals(-1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals(KeyEvent.VK_W, b.getMnemonic());
        }
        // <html>&Advanced Mode <em>(experimental)</em></html>
        Mnemonics.setLocalizedText(b, "<html>&Advanced Mode <em>(experimental)</em></html>");
        assertEquals("<html>Advanced Mode <em>(experimental)</em></html>", b.getText());
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            assertEquals(0, b.getMnemonic());
            assertEquals(-1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals(KeyEvent.VK_A, b.getMnemonic());
        }
        
        assertEquals("Execute (Force Reload)", Actions.cutAmpersand("Execute (&Force Reload)"));
    }
    
}

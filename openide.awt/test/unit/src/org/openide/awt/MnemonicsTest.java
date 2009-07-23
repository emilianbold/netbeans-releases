/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.awt;

import java.awt.event.KeyEvent;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
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

    // XXX testFindMnemonicAmpersand

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
        String underStart = Utilities.isMac() ? "" : "<u>";
        String underEnd = Utilities.isMac() ? "" : "</u>";
        Mnemonics.setLocalizedText(b, "<html><b>R&amp;D</b> departmen&t");
        assertEquals("<html><b>R&amp;D</b> departmen" + underStart + "t" + underEnd, b.getText());
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            assertEquals(0, b.getMnemonic());
            assertEquals(-1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals(KeyEvent.VK_T, b.getMnemonic());
        }
        
        Mnemonics.setLocalizedText(b, "<html>Smith &amp; &Wesson");
        assertEquals("<html>Smith &amp; " + underStart + "W" + underEnd + "esson", b.getText());
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            assertEquals(0, b.getMnemonic());
            assertEquals(-1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals(KeyEvent.VK_W, b.getMnemonic());
        }
        Mnemonics.setLocalizedText(b, "<html>&Advanced Mode <em>(experimental)</em></html>");
        assertEquals("<html>" + underStart + "A" + underEnd + "dvanced Mode <em>(experimental)</em></html>", b.getText());
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            assertEquals(0, b.getMnemonic());
            assertEquals(-1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals(KeyEvent.VK_A, b.getMnemonic());
            assertEquals('A', b.getText().charAt(b.getDisplayedMnemonicIndex()));
        }
    }
    
    public void testSetLocalizedTextWithModel() throws Exception {
        ButtonModel m = new DefaultButtonModel();
        JButton b = new JButton();
        Mnemonics.setLocalizedText(b, "Hello &There");
        assertEquals("Hello There", b.getText());
        assertEquals('T', b.getMnemonic());
        assertEquals(6, b.getDisplayedMnemonicIndex());
        b.setModel(m);
        assertEquals("Hello There", b.getText());
        assertEquals('T', b.getMnemonic());
        assertEquals(6, b.getDisplayedMnemonicIndex());
    }
    
}

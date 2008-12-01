/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.openide;

import java.awt.Dialog;
import javax.swing.JButton;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/** Tests implementation #148730: Add helper class to simplify dealing with error/warning/info messages in dialogs
 *
 * @author Jiri Rechtacek
 */
public class NotificationLineSupportTest {
    private JButton closeButton = new JButton ("Close action");
    private JButton [] options = new JButton [] {closeButton};

    public NotificationLineSupportTest() {
    }

    @BeforeClass
    public static void setUpClass () throws Exception {
    }

    @AfterClass
    public static void tearDownClass () throws Exception {
    }

    @Test
    public void testAppendNotificationLine () {
        DialogDescriptor dd = new DialogDescriptor ("Test", "Test dialog", false, options,
                closeButton, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        assertNull ("No NotificationLineSupport created.", dd.getNotificationLineSupport ());
        NotificationLineSupport supp = dd.createNotificationLineSupport ();
        assertNotNull ("NotificationLineSupport is created.", dd.getNotificationLineSupport ());

        Dialog d = DialogDisplayer.getDefault ().createDialog (dd);
        d.setVisible (true);
        assertNotNull ("NotificationLineSupport not null", supp);
        testSetInformationMessage (supp, "Hello");
        testSetWarningMessage (supp, "Hello");
        testSetErrorMessage (supp, "Hello");
        testEmpty (supp);
        closeButton.doClick ();
    }

    private void testSetInformationMessage (NotificationLineSupport supp, String msg) {
        supp.setInformationMessage (msg);
    }

    private void testSetWarningMessage (NotificationLineSupport supp, String msg) {
        supp.setWarningMessage (msg);
    }

    private void testSetErrorMessage (NotificationLineSupport supp, String msg) {
        supp.setErrorMessage (msg);
    }

    private void testEmpty (NotificationLineSupport supp) {
        supp.clearMessages ();
    }

    @Test
    public void testNonAppendNotificationLine () {
        DialogDescriptor dd = new DialogDescriptor ("Test", "Test dialog", false, options,
                closeButton, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        assertNull ("No NotificationLineSupport created.", dd.getNotificationLineSupport ());

        Dialog d = DialogDisplayer.getDefault ().createDialog (dd);
        d.setVisible (true);
        try {
            // !! It's package-private
            dd.setInformationMessage ("Hello");
            fail ();
        } catch (IllegalStateException x) {
            // must be throw because no NotificationLineSupport created
        }
        closeButton.doClick ();
    }

}

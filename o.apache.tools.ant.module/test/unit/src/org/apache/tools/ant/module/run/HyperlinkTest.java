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

package org.apache.tools.ant.module.run;

import java.awt.EventQueue;
import java.io.PrintStream;
import javax.swing.JEditorPane;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditor;
import org.openide.windows.OutputListener;
import org.openide.windows.TopComponent;

/**
 * Check that hyperlinks go to the right place.
 * @author Jesse Glick
 */
@RandomlyFails
public class HyperlinkTest extends NbTestCase {

    public HyperlinkTest(String n) {
        super(n);
    }

    private FileObject f1;
    private OutputListener h11;
    private OutputListener h12;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        FileObject root = FileUtil.toFileObject(getWorkDir());
        f1 = root.createData("f1");
        PrintStream ps = new PrintStream(f1.getOutputStream());
        ps.println("#comment 1");
        ps.println("bug #1");
        ps.println("#COMMENT 2");
        ps.println("bug #2");
        ps.println("#CoMmEnT 3");
        ps.flush();
        ps.close();
        h11 = new Hyperlink(f1.getURL(), "f1 #1", 2, 5, -1, -1);
        h12 = new Hyperlink(f1.getURL(), "f1 #2", 4, 5, -1, -1);
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Hyperlink.hyperlinks.clear();
    }

    public void testMovingHyperlinkWithoutSave() throws Exception {
        doTestMovingHyperlink(false);
    }

    public void testMovingHyperlinkWithSave() throws Exception { // #62623
        doTestMovingHyperlink(true);
    }

    private void doTestMovingHyperlink(boolean save) throws Exception {
        click(h11);
        final JEditorPane[] _ep1 = {null};
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                _ep1[0] = ((CloneableEditor) TopComponent.getRegistry().getActivated()).getEditorPane();
            }
        });
        JEditorPane ep1 = _ep1[0];
        assertEquals("#1\n", ep1.getDocument().getText(ep1.getCaretPosition(), 3));
        ep1.getDocument().insertString(ep1.getCaretPosition() + 3, "fixstuff\n", null);
        if (save) {
            DataObject.find(f1).getLookup().lookup(SaveCookie.class).save();
        }
        click(h11);
        assertEquals("#1\n", ep1.getDocument().getText(ep1.getCaretPosition(), 3));
        ep1.getDocument().insertString(ep1.getCaretPosition() + 3, "fixstuff\n", null);
        click(h12);
        assertEquals("#2\n", ep1.getDocument().getText(ep1.getCaretPosition(), 3));
        click(h11);
        assertEquals("#1\n", ep1.getDocument().getText(ep1.getCaretPosition(), 3));
    }

    private void click(OutputListener hyperlink) throws Exception {
        hyperlink.outputLineAction(null);
        // Need to wait for CloneableEditorSupport.openAt.Selector.run to finish:
        EventQueue.invokeAndWait(new Runnable() {public void run() {}});
    }

}

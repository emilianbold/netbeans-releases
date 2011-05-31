/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.editor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.html.editor.gsf.HtmlLanguage;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class NavigationSideBarTest extends CslTestBase {

    public NavigationSideBarTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(MockMimeLookup.class);
        super.setUp();
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new HtmlLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/html";
    }

    public void testMergedNavigationLine() throws DataObjectNotFoundException, IOException, BadLocationException, InterruptedException, InvocationTargetException {
        FileObject file = getTestFile("testfiles/navigationbar/test.html");
        DataObject dobj = DataObject.find(file);
        final EditorCookie editor = dobj.getCookie(EditorCookie.class);
        editor.open();
        Document doc = editor.openDocument();

        //find pipe
        final int pipe = doc.getText(0, doc.getLength()).indexOf('|');
        assertTrue("bad testing file - no pipe char in the source", pipe != -1);

        final AtomicReference<JEditorPane> paneRef = new AtomicReference<JEditorPane>();
        SwingUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                paneRef.set(editor.getOpenedPanes()[0]);
            }

        });

        final JEditorPane pane = paneRef.get();
        assertNotNull(pane);

        NavigationSideBar nav = new NavigationSideBar(pane);
        final AtomicReference<List<AstNode>> ref = new AtomicReference<List<AstNode>>();

        nav.regicterTestAccess(new NavigationSideBar.TestAccess() {
            @Override
            public void updated(List<AstNode> path) {
                ref.set(path);
                synchronized (ref) {
                    ref.notifyAll();
                }
            }
        });

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    //this should cause the navigation side bar to update
                    pane.setCaretPosition(pipe);
                    //strange, the document needs to be modified to force the caret aware scheduler tast to run
                    pane.getDocument().insertString(pipe, "x", null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        synchronized (ref) {
            ref.wait(10000);
        }

        List<AstNode> content = ref.get();
        assertNotNull(content);
        assertEquals(5, content.size());

        List<String> nodeNames = new ArrayList<String>();
        for(AstNode node : content) {
            nodeNames.add(node.name());
        }

        assertEquals("html", nodeNames.get(0));
        assertEquals("body", nodeNames.get(1));
        assertEquals("div", nodeNames.get(2));
        assertEquals("wicket:tag", nodeNames.get(3));
        assertEquals("a", nodeNames.get(4));

    }



}
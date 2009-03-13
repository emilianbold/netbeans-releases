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
package org.netbeans.modules.css.gsf;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.css.editor.test.TestBase;
import org.netbeans.modules.editor.NbEditorKit;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * @author marek.fukala@sun.com
 */
public class CssBracketCompleterTest extends TestBase {

    private Document doc;
    private JEditorPane pane;
    private BaseAction defaultKeyTypedAction;
    private BaseAction backspaceAction;

    public CssBracketCompleterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupEditor();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanUpEditor();
    }

    private void setupEditor() throws IOException {
        // this doesn't work since the JTextPane doesn't like our Kits since they aren't StyleEditorKits.
        //            Document doc = createDocument();
        //            JTextPane pane = new JTextPane((StyledDocument)doc);
        //            EditorKit kit = CloneableEditorSupport.getEditorKit("text/x-css");
        //            pane.setEditorKit(kit);

        File tmpFile = new File(getWorkDir(), "bracketCompleterTest.css");
        tmpFile.createNewFile();
        FileObject fo = FileUtil.createData(tmpFile);
        DataObject dobj = DataObject.find(fo);
        EditorCookie ec = dobj.getCookie(EditorCookie.class);
        this.doc = ec.openDocument();
        ec.open();
        this.pane = ec.getOpenedPanes()[0];

        this.defaultKeyTypedAction = (BaseAction) pane.getActionMap().get(NbEditorKit.defaultKeyTypedAction);
        this.backspaceAction = (BaseAction) pane.getActionMap().get(NbEditorKit.deletePrevCharAction);
    }

    private void cleanUpEditor() {
        this.pane.setVisible(false);
        this.pane = null;
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testCurlyBrackets() throws BadLocationException, IOException {
        //test pair autocomplete
        type('{');
        assertEquals("{}", getText());

        //test generated pair autodelete
//        backspace();
//        assertEquals("", getText());

        clear(doc);

        //test not generated pair NOT deleted
        doc.insertString(0, "{}", null);
        //                   01
        pane.setCaretPosition(1);
        backspace();
//        assertEquals("}", getText());

        clear(doc);

        //test skipping closing curly bracket
        String text = "h1 { color: red; } ";
        //             0123456789012345678        
        doc.insertString(0, text , null);

        pane.setCaretPosition(17);
        type('}');

        assertEquals(text, getText()); //no change in the text
        assertEquals(18, pane.getCaretPosition()); //+1

    }

    //------- utilities -------

    private void type(char ch) {
        ActionEvent ae = new ActionEvent(doc, 0, ""+ch);
        defaultKeyTypedAction.actionPerformed(ae, pane);
    }

    private void backspace() {
        backspaceAction.actionPerformed(new ActionEvent(doc, 0, null));
    }

    private String getText() throws BadLocationException {
        return doc.getText(0, doc.getLength());
    }

    private void clear(Document doc) throws BadLocationException {
        doc.remove(0, doc.getLength());
    }
    
}

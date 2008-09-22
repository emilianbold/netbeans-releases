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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rhtml;

import javax.swing.JEditorPane;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.ruby.RubyFormatter;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tor Norbye
 */
public class RhtmlFormattingTest extends RhtmlTestBase {
    public RhtmlFormattingTest(String testName) {
        super(testName);
    }

//    @SuppressWarnings("unchecked")
//    public String format(BaseDocument doc, int startPos, int endPos, IndentPrefs preferences) throws Exception {
//
//        String text = doc.getText(0, doc.getLength());
//        JEditorPane pane = getPane(text, startPos, endPos);
//        assertEquals(RubyInstallation.RHTML_MIME_TYPE, pane.getDocument().getProperty("mimeType"));
//
//        runKitAction(pane, BaseKit.formatAction, "");
//
//        BaseDocument bdoc = (BaseDocument) pane.getDocument();
//
//        RubyFormatter formatter = getFormatter(preferences);
//        String formatted = bdoc.getText(0, bdoc.getLength());
//
//        doc.remove(0, doc.getLength());
//        doc.insertString(0, formatted, null);
//
//        // Apply Ruby formatting separately; can't get the indent task factory to
//        // work (see RhtmlTestBase) because GsfIndentTask needs to find the RubyLanguage
//        // and the system file system doesn't seem to include it
//
//        //CompilationInfo info = getInfo(fileObject);
//        CompilationInfo info = null;
//        formatter.reformat(doc, startPos, endPos, info);
//
//        formatted = doc.getText(0, doc.getLength());
//
//        return formatted;
//    }
    
    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

//    @Override
//    public void format(String source, String reformatted, IndentPrefs preferences) throws Exception {
//        // Must run in AWT thread (BaseKit.install() checks for that)
//        String BEGIN = "%<%"; // NOI18N
//        int startPos = source.indexOf(BEGIN);
//        if (startPos != -1) {
//            source = source.substring(0, startPos) + source.substring(startPos+BEGIN.length());
//        } else {
//            startPos = 0;
//        }
//        
//        String END = "%>%"; // NOI18N
//        int endPos = source.indexOf(END);
//        if (endPos != -1) {
//            source = source.substring(0, endPos) + source.substring(endPos+END.length());
//        }
//
//        BaseDocument doc = getDocument(source);
//
//        if (endPos == -1) {
//            endPos = doc.getLength();
//        }
//        
//        String formatted = format(doc, startPos, endPos, preferences);
//        assertEquals(reformatted, formatted);
//    }
//    
    public void reformatFileContents(String file) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);

        //IndentPrefs preferences = new IndentPrefs(2,2);

        format(doc, new RubyFormatter(), null, 0, doc.getLength(), false);
        
        String formatted = doc.getText(0, doc.getLength());
        
        assertDescriptionMatches(file, formatted, false, ".formatted");
    }
    

    public void testDummy() throws Exception {
    }
    
//    public void testFormat1() throws Exception {
//        reformatFileContents("testfiles/format1.rhtml");
//    }
//    
//    public void testFormat2() throws Exception {
//        reformatFileContents("testfiles/format2.rhtml");
//    }
//    
//    public void testFormat2b() throws Exception {
//        // Same as format2.rhtml, but flushed left to ensure that
//        // we're not reformatting correctly just by luck
//        reformatFileContents("testfiles/format2b.rhtml");
//    }
//    
//    public void testFormat3() throws Exception {
//        reformatFileContents("testfiles/format3.rhtml");
//    }
//    
//    public void testFormat4() throws Exception {
//        reformatFileContents("testfiles/format4.rhtml");
//    }
    
//    public void testFormat5() throws Exception {
//        format("<%\ndef foo\nwhatever\nend\n%>\n",
//                "<%\ndef foo\n  whatever\nend\n%>\n", null);
//    }
    
//    public void testFormat6() throws Exception {
//        format("<% if true %>\nhello\n%<= foo %>\n<% end %>\n",
//                "<% if true %>\n  hello\n%  <= foo %>\n<% end %>\n", null);
//    }
//
//    public void testFormat7() throws Exception {
//        format("<% foo %><% if true %>\nhello\n%<= foo %>\n<% end %>\n",
//                "<% foo %><% if true %>\n  hello\n%  <= foo %>\n<% end %>\n", null);
//    }
}

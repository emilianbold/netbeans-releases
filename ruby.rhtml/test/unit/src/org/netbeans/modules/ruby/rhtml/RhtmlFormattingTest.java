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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rhtml;

import org.netbeans.api.gsf.FormattingPreferences;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.Formatter;
import org.netbeans.modules.ruby.IndentPrefs;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;

/**
 *
 * @author Tor Norbye
 */
public class RhtmlFormattingTest extends RubyTestBase {

    public RhtmlFormattingTest(String testName) {
        super(testName);
    }

    @Override
    protected BaseDocument getDocument(String s) {
        BaseDocument doc = super.getDocument(s);
        doc.putProperty(org.netbeans.api.lexer.Language.class, RhtmlTokenId.language());
        
        return doc;
    }
    
    public void format(String source, String reformatted, FormattingPreferences preferences) throws Exception {
        Formatter formatter = new Formatter();
        if (preferences == null) {
            preferences = new IndentPrefs(2,2);
        }
        
        String BEGIN = "%<%"; // NOI18N
        int startPos = source.indexOf(BEGIN);
        if (startPos != -1) {
            source = source.substring(0, startPos) + source.substring(startPos+BEGIN.length());
        } else {
            startPos = 0;
        }
        
        String END = "%>%"; // NOI18N
        int endPos = source.indexOf(END);
        if (endPos != -1) {
            source = source.substring(0, endPos) + source.substring(endPos+END.length());
        }

        BaseDocument doc = getDocument(source);

        if (endPos == -1) {
            endPos = doc.getLength();
        }
        
        //ParserResult result = parse(fo);
        ParserResult result = null;
        formatter.reindent(doc, startPos, endPos, result, preferences);

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);
    }
    
    public void testFormat1() throws Exception {
        format("<%\ndef foo\nwhatever\nend\n%>\n",
                "<%\ndef foo\n  whatever\nend\n%>\n", null);
    }
    
    // Not yet working
    //public void testFormat2() throws Exception {
    //    format("<% if true %>\nhello\n%<= foo %>\n<% end %>\n",
    //            "<% if true %>\n  hello\n%  <= foo %>\n<% end %>\n", null);
    //}
}

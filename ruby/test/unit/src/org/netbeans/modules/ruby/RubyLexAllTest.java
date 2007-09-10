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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby;

import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.filesystems.FileObject;

/**
 * Lex all files in the ruby distribution to make sure there are no problems
 * 
 * @author Tor Norbye
 */
public class RubyLexAllTest extends RubyTestBase {
    public RubyLexAllTest(String testName) {
        super(testName);
    }

    public void testLexAll() throws BadLocationException {
        // Find ruby files
        List<FileObject> files = findJRubyRubyFiles();
        assertTrue(files.size() > 0);

        // indent each one
        for (FileObject fo : files) {
            //System.out.println("Formatting file " /*+ count*/ + " : " + FileUtil.getFileDisplayName(fo));
            
            // check that we end up at indentation level 0
            BaseDocument doc = getDocument(fo);
            
            String text = doc.getText(0, doc.getLength());
            TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
            @SuppressWarnings("unchecked")
            TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
            // Just iterate through the sequence to make sure it's okay - this throws an exception because of bug 93990
            while (ts.moveNext()) {
                ;
            }
        }
    }
}

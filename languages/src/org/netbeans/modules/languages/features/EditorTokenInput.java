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

package org.netbeans.modules.languages.features;

import org.netbeans.modules.languages.parser.TokenInput;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.languages.ASTToken;
import org.openide.text.NbDocument;

import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 *
 * @author Jan Jancura
 */
public class EditorTokenInput extends TokenInput {

    private TokenSequence   tokenSequence;
    private List            tokens = new ArrayList ();
    private int             index = 0;
    private Set             filter;
    private StyledDocument  doc;
    private String          mimeType;
    

    public static EditorTokenInput create (
        Set             filter,
        StyledDocument  doc
    ) {
        return new EditorTokenInput (filter, doc);
    }

    private EditorTokenInput (
        Set             filter,
        StyledDocument  doc
    ) {
        tokenSequence = TokenHierarchy.get (doc).tokenSequence ();
        this.filter = filter;
        this.doc = doc;
        mimeType = tokenSequence.language ().mimeType ();
    }

    public ASTToken next (int i) {
        while (index + i - 1 >= tokens.size ()) {
            ASTToken token = nextToken ();
            if (token == null) return null;
            tokens.add (token);
        }
        return (ASTToken) tokens.get (index + i - 1);
    }
    
    private ASTToken nextToken () {
        do {
            if (!tokenSequence.moveNext ()) return null;
        } while (
            filter.contains (
                tokenSequence.token ().id ().name ()
            )
        );
        Token token = tokenSequence.token ();
        return ASTToken.create (
            tokenSequence.language ().mimeType (),
            token.id ().name (),
            token.text ().toString (),
            tokenSequence.offset ()
        );
    }

    public boolean eof () {
        return next (1) == null;
    }

    public int getIndex () {
        return index;
    }

    public int getOffset () {
        ASTToken t = null;
        if (eof ()) {
            if (getIndex () == 0) return 0;
            t = ((ASTToken) tokens.get (tokens.size () - 1));
            return t.getOffset () + t.getLength ();
        } else {
            t = (ASTToken) next (1);
            return t.getOffset ();
        }
    }

    public ASTToken read () {
        ASTToken next = next (1);
        index++;
        return next;
    }

    public void setIndex (int index) {
        this.index = index;
    }

    public String getString (int from) {
        throw new InternalError ();
    }
    
    public String toString () {
        int offset = next (1) == null ?
            doc.getLength () : next (1).getOffset ();
        int lineNumber = NbDocument.findLineNumber (doc, offset);
        return (String) doc.getProperty ("title") + ":" + 
            (lineNumber + 1) + "," + 
            (offset - NbDocument.findLineOffset (doc, lineNumber) + 1);
//        StringBuffer sb = new StringBuffer ();
//        TokenItem t = next;
//        int i = 0;
//        while (i < 10) {
//            if (t == null) break;
//            EditorToken et = (EditorToken) t.getTokenID ();
//            sb.append (Token.create (
//                et.getMimeType (),
//                et.getType (),
//                t.getImage (),
//                null
//            ));
//            t = t.getNext ();
//            i++;
//        }
//        return sb.toString ();
    }
}

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

package org.netbeans.modules.groovy.editor;

import java.util.HashSet;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.DeclarationFinder;
import org.netbeans.modules.gsf.api.OffsetRange;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.groovy.editor.elements.IndexedClass;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.Index.SearchScope;
import org.netbeans.modules.gsf.api.NameKind;
import org.openide.util.Exceptions;

/**
 *
 * @author schmidtm
 */
public class GroovyDeclarationFinder implements DeclarationFinder{

    private final Logger LOG = Logger.getLogger(GroovyDeclarationFinder.class.getName());
    Token<GroovyTokenId> tok;
    
    Document lastDoc = null;
    int lastOffset = -1;
    OffsetRange lastRange = OffsetRange.NONE;
    
    public GroovyDeclarationFinder() {
        // LOG.setLevel(Level.FINEST);
    }

    public DeclarationLocation findDeclaration(CompilationInfo info, int offset) {
        LOG.log(Level.FINEST, "findDeclaration()");
        
        assert info != null;
        assert offset >= 0;
        
        Set<IndexedClass> idxClasses;
        String text = "";

        Index ix = info.getIndex(GroovyTokenId.GROOVY_MIME_TYPE);
        
        if (ix == null) {
            LOG.log(Level.FINEST, "Could not retrieve Index from CompilationInfo");
            return DeclarationLocation.NONE;
        }
        
        GroovyIndex index = new GroovyIndex(ix);
        
        // is the caret inbetween the last Range?
        
        if(offset < lastRange.getStart() || offset > lastRange.getEnd())
            return DeclarationLocation.NONE;
        
        // get the text from the last range ...
        try {
            text = lastDoc.getText(lastRange.getStart(), lastRange.getLength());
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        if(text == null || text.length() < 1)
            return DeclarationLocation.NONE;
        
        LOG.log(Level.FINEST, "We are looking for: " + text);
        
        Set<SearchScope> searchScope = new HashSet<SearchScope>();
        
        searchScope.add(SearchScope.SOURCE);
        searchScope.add(SearchScope.DEPENDENCIES);
                
        idxClasses = index.getClasses(text, NameKind.EXACT_NAME, 
                false, false, false, searchScope, null);
        
        if (idxClasses == null || idxClasses.isEmpty()) {
            LOG.log(Level.FINEST, "Found nothing");
            return DeclarationLocation.NONE;
        }        
        
        for (IndexedClass icl : idxClasses) {
            LOG.log(Level.FINEST, "Found : " + icl.getName());
            return new DeclarationLocation(icl.getFileObject(), 1);
        }
        
        return DeclarationLocation.NONE;
    }

    public OffsetRange getReferenceSpan(Document doc, int offset) {
        LOG.log(Level.FINEST, "getReferenceSpan()");
        
        assert doc != null;
        assert offset >= 0;

        /*
        Don't calculate this range over and over again!
        We might need to register ourselves as DocumentListener
        for the document to figure out wether the document
        was changed inbetween calls. But this is unlikely.
        Therefore this trivial approach should do the trick
        as well.
         */
        
//        if (doc == lastDoc && offset == lastOffset) {
//            return lastRange;
//        } else {
//            lastDoc = doc;
//            lastOffset = offset;
//        }
        
        
        lastDoc = doc;
        lastOffset = offset;
        
        JTextComponent target = EditorRegistry.lastFocusedComponent();
        final StyledDocument styledDoc = (StyledDocument) target.getDocument();
        if (styledDoc == null) {
            return OffsetRange.NONE;
        }
        
        // Work only with the open editor 
        //and the editor has to be the active component:
        if ((target == null) || (target.getDocument() != doc)) {
            return OffsetRange.NONE;
        }
        
        TokenSequence<GroovyTokenId> ts = getSequenceForDoc(doc);
        tok = getTokenForPosition(ts, offset);
        
        if (tok != null) {
            LOG.log(Level.FINEST, "Token   : " + tok);
            LOG.log(Level.FINEST, "TokenID : " + tok.id());
            
            GroovyTokenId tkid = tok.id();
            
            if((tkid == GroovyTokenId.IDENTIFIER)) {
                LOG.log(Level.FINEST, "GroovyTokenId.IDENTIFIER");
                int startOffset = ts.offset();
                int endOffset = startOffset + tok.text().length();
                
                lastRange = new OffsetRange(startOffset, endOffset);
                return lastRange;
            }
        }
        LOG.log(Level.FINEST, "Token was null");
        return OffsetRange.NONE;
    }

    private TokenSequence<GroovyTokenId> getSequenceForDoc(Document doc) {

        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<GroovyTokenId> ts = hi.tokenSequence(GroovyTokenId.language());

        return ts;
    }

    private Token<GroovyTokenId> getTokenForPosition(TokenSequence<GroovyTokenId> ts, int offset) {

        ts.move(offset);
        ts.moveNext();

        return ts.token();
    }
    
    
}

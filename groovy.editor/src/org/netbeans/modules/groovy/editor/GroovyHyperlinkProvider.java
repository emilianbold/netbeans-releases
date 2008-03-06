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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;
import org.netbeans.api.lexer.Token;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author schmidtm
 */
public class GroovyHyperlinkProvider implements HyperlinkProvider {
    
    private Reference<Document> lastDocument;

    private int startOffset;
    private int endOffset;
    
    private  final Logger LOG = Logger.getLogger(GroovyHyperlinkProvider.class.getName());

    private Document getLastDocument() {
        return lastDocument == null ? null : lastDocument.get();
    }

    private void setLastDocument(Document doc) {
        lastDocument = new WeakReference<Document>(doc);
    }

    public GroovyHyperlinkProvider() {
        this.lastDocument = null;
    }

    
    public boolean isHyperlinkPoint(Document doc, int offset) {
        // this method is stolen from:
        // http://platform.netbeans.org/tutorials/60/nbm-hyperlink.html
        
        // LOG.setLevel(Level.FINEST);
        LOG.log(Level.FINEST, "isHyperlinkPoint: " + offset);
        
        JTextComponent target = EditorRegistry.lastFocusedComponent();
        final StyledDocument styledDoc = (StyledDocument) target.getDocument();
        if (styledDoc == null) {
            return false;
        }

        // Work only with the open editor 
        //and the editor has to be the active component:
        if ((target == null) || (target.getDocument() != doc)) {
            return false;
        }
        
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<GroovyTokenId> ts = hi.tokenSequence(GroovyTokenId.language());
        
        ts.move(offset);
        ts.moveNext();
        
        Token<GroovyTokenId> tok = ts.token();
        
        if (tok != null) {
            LOG.log(Level.FINEST, "Token   : " + tok);
            LOG.log(Level.FINEST, "TokenID : " + tok.id());
            
            GroovyTokenId tkid = tok.id();
            
            if((tkid == GroovyTokenId.IDENTIFIER)) {
                LOG.log(Level.FINEST, "GroovyTokenId.IDENTIFIER");
                setLastDocument(doc);
                startOffset = ts.offset();
                endOffset = startOffset + tok.text().length();
                return true;
            }
        }
        LOG.log(Level.FINEST, "Token was null");
        return false;
        
    }

    public int[] getHyperlinkSpan(Document doc, int offset) {
        LOG.log(Level.FINEST, "getHyperlinkSpan: " + offset);
        JTextComponent target = EditorRegistry.lastFocusedComponent();
        final StyledDocument styledDoc = (StyledDocument) target.getDocument();
        if (styledDoc == null) {
            return null;
        }

        // Return the position, which was set in the isHyperlink method:
        return new int[]{startOffset, endOffset};
    }

    public void performClickAction(Document doc, int offset) {
    }

}

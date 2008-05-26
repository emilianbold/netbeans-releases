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

import java.io.IOException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.gsf.api.CodeCompletionContext;
import org.netbeans.modules.gsf.api.CodeCompletionResult;
import org.netbeans.modules.ruby.CodeCompleter;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.openide.util.Exceptions;

/**
 * RHTML code completer
 * 
 * @author Tor Norbye
 */
public class RhtmlCompleter extends CodeCompleter {
    /**
     *  @todo Pass in the completion type? (Smart versus documentation etc.)
     *  @todo Pass in the line offsets? Nah, just make the completion provider figure those out.
     */
    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {
        CompilationInfo info = context.getInfo();
        int caretOffset = context.getCaretOffset();
        try {
            Document doc = info.getDocument();
            if (isWithinRuby(doc, caretOffset)) {
                return super.complete(context);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return CodeCompletionResult.NONE;
    }

    /**
     * Consider a keystroke and decide whether it should automatically invoke some type
     * of completion. If so, return the desired type, otherwise return QueryType.NONE.
     * @return A QueryType if automatic completion should be initiated, or {@link QueryType.NONE}
     *   if it should be left alon, or {@link QueryType.STOP} if completion should be terminated
     */
    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        Document doc = component.getDocument();
        int caretOffset =  component.getCaret().getDot();
        if (isWithinRuby(component.getDocument(), caretOffset)) {
            return super.getAutoQuery(component, typedText);
        }
        
        return QueryType.NONE;
    }
     
    static boolean isWithinRuby(Document doc, int offset){
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
        
        tokenSequence.move(offset);
        if (tokenSequence.moveNext() || tokenSequence.movePrevious()) {
            Object tokenID = tokenSequence.token().id();
            if (tokenID == RhtmlTokenId.RUBY || tokenID == RhtmlTokenId.RUBY_EXPR) {
                return true;
            } else if (tokenID == RhtmlTokenId.DELIMITER) {
                // maybe the caret is placed just before the ending script delimiter?
                tokenSequence.movePrevious();
                
                if (tokenSequence.token().id() == RhtmlTokenId.RUBY || 
                        tokenSequence.token().id() == RhtmlTokenId.RUBY_EXPR){
                    return true;
                }
            }
        }
        
        return false;
    }
    
}

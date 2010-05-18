/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.core.syntax.indent;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.web.core.syntax.formatting.JspJavaFormatter;

public class JspJavaIndentTask implements IndentTask {

    private Context context;
    
    JspJavaIndentTask(Context context) {
        this.context = context;
    }

    public void reindent() throws BadLocationException {
        if (context.isIndent()){
            enterPressed(context);
        }
    }

    public void enterPressed(Context context) {
        TokenHierarchy<Document> hi = TokenHierarchy.get(context.document());
        List<TokenSequence<?>> sequences = hi.embeddedTokenSequences(context.caretOffset(), true);
        if (!sequences.isEmpty()) {
            TokenSequence mostEmbedded = sequences.get(sequences.size() - 1);
            if(mostEmbedded.language() == JavaTokenId.language()) {
                ExtFormatter formatter = new JspJavaFormatter(JavaKit.class);
                try {
                    formatter.reformat((BaseDocument)context.document(), context.caretOffset(), context.caretOffset() + 1, true);
                }catch(BadLocationException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
                }catch(IOException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, null, e);
                }
            }
        }
    }
    
    public ExtraLock indentLock() {
        return null;
    }  
}

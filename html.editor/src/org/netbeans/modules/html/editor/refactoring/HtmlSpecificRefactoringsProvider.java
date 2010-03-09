/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.editor.refactoring;

import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;


/**
 *
 * @author marekfukala
 */
@org.openide.util.lookup.ServiceProvider(service=HtmlSpecificActionsImplementationProvider.class, position=150)
public class HtmlSpecificRefactoringsProvider extends HtmlSpecificActionsImplementationProvider {

    @Override
    public boolean canExtractInlineStyle(Lookup lookup) {
        //the editor cookie is in the lookup only if the file is opened in the editor and is active
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if(ec == null) {
            return false;
        }
        JEditorPane[] panes = ec.getOpenedPanes();
        if(panes == null || panes.length == 0) {
            return false;
        }
        JEditorPane pane = panes[0]; //get first pane, I hope the activated one is first
        Document doc = ec.getDocument();

        OffsetRange adjusted = adjustContextRange(doc, pane.getSelectionStart(), pane.getSelectionEnd());

        //enable the action if there are some inlined styles in the selection or at the caret position
        return !RefactoringContext.findInlinedStyles(doc, adjusted.getStart(), adjusted.getEnd()).isEmpty();
    }

    @Override
    public void doExtractInlineStyle(Lookup lookup) {
        //the editor cookie is in the lookup only if the file is opened in the editor and is active
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if(ec == null) {
            return;
        }
        JEditorPane[] panes = ec.getOpenedPanes();
        if(panes == null || panes.length == 0) {
            return;
        }
        JEditorPane pane = panes[0]; //get first pane, I hope the activated one is first
        if(pane == null) {
            return ;
        }
        Document doc = ec.getDocument();

//        FileObject file = lookup.lookup(FileObject.class);
        FileObject file = DataLoadersBridge.getDefault().getFileObject(doc);
        assert file != null;

        //widen the context range to tags' start/end if only partially selected
        OffsetRange adjusted = adjustContextRange(doc, pane.getSelectionStart(), pane.getSelectionEnd());

        try {
            RefactoringContext context = RefactoringContext.create(file, doc, adjusted.getStart(), adjusted.getEnd());
            UI.openRefactoringUI(new ExtractInlinedStyleRefactoringUI(context));
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }

    private static OffsetRange adjustContextRange(final Document doc, final int from, final int to) {
        final AtomicReference<OffsetRange> ret = new AtomicReference<OffsetRange>();
        ret.set(new OffsetRange(from, to)); //return the same pair by default
        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(doc, from);
                Token<HTMLTokenId> openTag = Utils.findTagOpenToken(ts);
                if(openTag == null) {
                    return ;
                }
                int adjustedFrom = ts.offset();

                //now try to find the tag's end
                ts.move(to);
                int adjustedTo = -1;
                while(ts.moveNext()) {
                    Token<HTMLTokenId> t = ts.token();
                    if(t.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                        adjustedTo = ts.offset() + t.length();
                        break;
                    } else if(t.id() == HTMLTokenId.TEXT) {
                        //do not go too far - out of the tag
                        break;
                    }
                }

                if(adjustedTo == -1) {
                    return ;
                }

                //we found the adjusted range
                ret.set(new OffsetRange(adjustedFrom, adjustedTo));

            }

        });
        return ret.get();
    }

    
}

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
package org.netbeans.modules.web.frameworks.facelets.editor;

import java.util.ArrayList;
import java.util.Map;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.frameworks.facelets.parser.Parser;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class FaceletsCompletionProvider implements CompletionProvider {

    /** Creates a new instance of FaceletsCodeCompletionProvider */
    public FaceletsCompletionProvider() {
    }

    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == COMPLETION_QUERY_TYPE) {
            return new AsyncCompletionTask(new CCQuery(component.getCaret().getDot()), component);
        }
        return null;
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    static final class CCQuery extends AsyncCompletionQuery {
        //private int creationCaretOffset;

        private JTextComponent component;

        CCQuery(int caretOffset) {
            //this.creationCaretOffset = caretOffset;
        }

        protected void query(CompletionResultSet resultSet, Document doc, int offset) {
            FileObject fObject = NbEditorUtilities.getFileObject(doc);
            WebModule webModule = null;
            if (fObject != null) {
                webModule = WebModule.getWebModule(fObject);
            }

            //XXX quick old syntax to tokes fix, no locking on document since a suspicious
            //call to a parser inside the loop, no threading/locking is clear now.
            //TODO clarify embeddings!!!!
            CharSequence source;
            try {
                source = doc.getText(0, doc.getLength());
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                return ;
            }
            
            TokenHierarchy th = TokenHierarchy.create(source, HTMLTokenId.language());
            TokenSequence ts = th.tokenSequence();
            int diff = ts.move(offset);
            if (ts.moveNext()) {
                if (diff == 0 && (ts.token().id() == HTMLTokenId.TEXT || ts.token().id() == HTMLTokenId.WS)) {
                    //looks like we are on a boundary of a text or whitespace, need the previous token
                    if (!ts.movePrevious()) {
                        //we cannot get previous token
                        return;
                    }
                }
            } else {
                if (!ts.movePrevious()) {
                    //can't get previous token
                    return;
                }
            }
            ArrayList complItems = new ArrayList();
            Token token = ts.token();

            //TODO: fix the code, write tests, for now I just make it compilable
            //w/o even trying to understand the logic

            if(token.id() == HTMLTokenId.ARGUMENT) {
                if(token.text().toString().startsWith("jsfc")) {
                    complItems.add(new FaceletsResultItems.AttributeItem("jsfc", ts.offset(), token.text().length()));
                }
            } else if(token.id() == HTMLTokenId.WS) {
                complItems.add(new FaceletsResultItems.AttributeItem("jsfc", ts.offset() + 1, 0));
            } else if(token.id() == HTMLTokenId.VALUE) {
                Token remToken = token;
                String tokenImage = token.text().toString();
                    if (!(tokenImage.indexOf('"') < tokenImage.lastIndexOf('"') && tokenImage.lastIndexOf('"') < (offset - ts.offset()))) {
                        if (tokenImage.length() < 2) {
                            tokenImage = "";
                        } else {
                            tokenImage = tokenImage.substring(1, tokenImage.length() - 1).trim();
                        }
                        int tokenLength = tokenImage.length();

                        //find argument name
                        while (ts.movePrevious() && token.id() != HTMLTokenId.ARGUMENT);
                        
                        if (token.text().toString().equals("jsfc")) {
                            Map<String, TagLibraryInfo> libraries = Parser.getParser(webModule).getLibraries(webModule);
                            Map<String, String> prefixes = Parser.getParser(webModule).getPrefixes(webModule, doc);

                            for (String prefix : prefixes.keySet()) {
                                String uri = prefixes.get(prefix);
                                TagLibraryInfo lib = libraries.get(uri);
                                for (TagInfo tag : lib.getTags()) {
                                    String text = prefix + ":" + tag.getTagName();
                                    if (tokenLength == 0 || text.startsWith(tokenImage)) {
                                        complItems.add(new FaceletsResultItems.TagValueAttributeItem(prefix, tag.getTagName(), uri, remToken.offset(th) + 1, tokenImage.length()));
                                    }
                                }
                            }
                        }
                    }
                }
            

            resultSet.setTitle(NbBundle.getMessage(FaceletsCompletionProvider.class, "Completion_Title")); //NOI18N
            resultSet.addAllItems(complItems);
            resultSet.finish();
        }

        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
    }
}

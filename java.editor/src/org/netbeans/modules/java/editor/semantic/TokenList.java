/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.java.editor.semantic;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Jan Lahoda
 */
public class TokenList {

    private CompilationInfo info;
    private SourcePositions sourcePositions;
    private Document doc;
    private AtomicBoolean cancel;

    private boolean topLevelIsJava;
    private TokenSequence topLevel;
    private TokenSequence ts;
        
    public TokenList(CompilationInfo info, final Document doc, AtomicBoolean cancel) {
        this.info = info;
        this.doc = doc;
        this.cancel = cancel;
        
        this.sourcePositions = info.getTrees().getSourcePositions();
        
        doc.render(new Runnable() {
            public void run() {
                if (TokenList.this.cancel.get())
                    return ;
                
                topLevel = TokenHierarchy.get(doc).tokenSequence();
                
                topLevelIsJava = topLevel.language() == JavaTokenId.language();
                
                if (topLevelIsJava) {
                    ts = topLevel;
                    ts.moveStart();
                    ts.moveNext(); //XXX: what about empty document
                }
            }
        });
    }
    
    public void moveToOffset(long inputOffset) {
        final int offset = info.getPositionConverter().getOriginalPosition((int) inputOffset);

        if (offset < 0)
            return ;
        doc.render(new Runnable() {
            public void run() {
                if (cancel.get())
                    return ;
                
                if (ts != null && !ts.isValid()) {
                    cancel.set(true);
                    return ;
                }
                
                if (topLevelIsJava) {
                    while (ts.offset() < offset) {
                        if (!ts.moveNext())
                            return ;
                    }
                } else {
                    Iterator<? extends TokenSequence> embeddedSeqs = null;
                    if (ts == null) {
                        List<? extends TokenSequence> seqs = new ArrayList<TokenSequence>(embeddedTokenSequences(TokenHierarchy.get(doc), offset));
                        Collections.reverse(seqs);
                        embeddedSeqs = seqs.iterator();
                        while (embeddedSeqs.hasNext()) {
                            TokenSequence tseq = embeddedSeqs.next();
                            if (tseq.language() == JavaTokenId.language()) {
                                ts = tseq;
                                break;
                            }
                        }
                    }

                    while (ts != null && ts.offset() < offset) {
                        if (!ts.moveNext()) {
                            ts = null;
                            if (embeddedSeqs == null) {
                                List<? extends TokenSequence> seqs = new ArrayList<TokenSequence>(embeddedTokenSequences(TokenHierarchy.get(doc), offset));
                                Collections.reverse(seqs);
                                embeddedSeqs = seqs.iterator();
                            }
                            while (embeddedSeqs.hasNext()) {
                                TokenSequence tseq = embeddedSeqs.next();
                                if (tseq.language() == JavaTokenId.language()) {
                                    ts = tseq;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public void moveToEnd(Tree t) {
        if (t == null)
            return ;

        long end = sourcePositions.getEndPosition(info.getCompilationUnit(), t);

        if (end == (-1))
            return ;

        if (t.getKind() == Kind.ARRAY_TYPE) {
            moveToEnd(((ArrayTypeTree) t).getType());
            return ;
        }
        moveToOffset(end);
    }

    public void moveToEnd(Collection<? extends Tree> trees) {
        if (trees == null)
            return ;

        for (Tree t : trees) {
            moveToEnd(t);
        }
    }

    public void firstIdentifier(final TreePath tp, final String name, final Map<Tree, Token> tree2Token) {
        doc.render(new Runnable() {
            public void run() {
                if (cancel.get())
                    return ;
                
                if (ts != null && !ts.isValid()) {
                    cancel.set(true);
                    return ;
                }
                
                if (ts == null)
                    return ;
                
                boolean next = true;

                while (ts.token().id() != JavaTokenId.IDENTIFIER && (next = ts.moveNext()))
                    ;

                if (next) {
                    if (name.equals(info.getTreeUtilities().decodeIdentifier(ts.token().text()).toString())) {
                        tree2Token.put(tp.getLeaf(), ts.token());
                    } else {
//                            System.err.println("looking for: " + name + ", not found");
                    }
                }
            }
        });
    }

    public void identifierHere(final IdentifierTree tree, final Map<Tree, Token> tree2Token) {
        doc.render(new Runnable() {
            public void run() {
                if (cancel.get())
                    return ;
                
                if (ts != null && !ts.isValid()) {
                    cancel.set(true);
                    return ;
                }
                
                if (ts == null)
                    return ;
                
                Token t = ts.token();

                if (t.id() == JavaTokenId.IDENTIFIER && tree.getName().toString().equals(info.getTreeUtilities().decodeIdentifier(t.text()).toString())) {
    //                System.err.println("visit ident 1");
                    tree2Token.put(tree, ts.token());
                } else {
    //                System.err.println("visit ident 2");
                }
            }
        });
    }
    
    public void moveBefore(final List<? extends Tree> tArgs) {
        doc.render(new Runnable() {
            public void run() {
                if (cancel.get())
                    return ;
                
                if (ts != null && !ts.isValid()) {
                    cancel.set(true);
                    return ;
                }
                
                if (ts == null)
                    return ;
                
                if (!tArgs.isEmpty()) {
                    int offset = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tArgs.get(0));
                    
                    offset = info.getPositionConverter().getOriginalPosition(offset);
                    
                    if (offset < 0)
                        return ;
                    
                    while (ts.offset() >= offset) {
                        if (!ts.movePrevious()) {
                            return;
                        }
                    }
                }
            }
        });
    }

    public void moveNext() {
        doc.render(new Runnable() {
            public void run() {
                if (cancel.get())
                    return ;
                
                if (ts != null && !ts.isValid()) {
                    cancel.set(true);
                    return ;
                }
                
                if (ts == null)
                    return ;
                
                ts.moveNext();
            }
        });
    }
    
    private static List<TokenSequence<?>> embeddedTokenSequences(TokenHierarchy<Document> th, int offset) {
        TokenSequence<?> embedded = th.tokenSequence();
        List<TokenSequence<?>> sequences = new ArrayList<TokenSequence<?>>();

        do {
            TokenSequence<?> seq = embedded;
            embedded = null;

            seq.move(offset);
            if (seq.moveNext()) {
                sequences.add(seq);
                embedded = seq.embedded();
            }
        } while (embedded != null);
        
        return sequences;
    }
    
}

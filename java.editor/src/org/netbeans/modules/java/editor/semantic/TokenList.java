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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
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
                
                if (topLevelIsJava) {
                    while (ts.offset() < offset) {
                        if (!ts.moveNext())
                            return ;
                    }
                } else {
                    OUTER: while (true) {
                        if (ts == null) {
                            List<? extends TokenSequence> seqs = new ArrayList<TokenSequence>(TokenHierarchy.get(doc).embeddedTokenSequences(offset, false));

                            Collections.reverse(seqs);

                            for (TokenSequence tseq : seqs) {
                                if (tseq.language() == JavaTokenId.language()) {
                                    ts = tseq;
                                }
                            }
                        }

                        if (ts == null) {
                            return;
                        }

                        while (ts.offset() < offset) {
                            if (!ts.moveNext()) {
                                ts = null;
                                continue OUTER;
                            }
                        }
                        
                        return;
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
                
                if (ts == null)
                    return ;
                
                boolean next = true;

                while (ts.token().id() != JavaTokenId.IDENTIFIER && (next = ts.moveNext()))
                    ;

                if (next) {
                    if (name.equals(ts.token().text().toString())) {
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
                
                if (ts == null)
                    return ;
                
                Token t = ts.token();

                if (t.id() == JavaTokenId.IDENTIFIER && tree.getName().toString().equals(t.text().toString())) {
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
}

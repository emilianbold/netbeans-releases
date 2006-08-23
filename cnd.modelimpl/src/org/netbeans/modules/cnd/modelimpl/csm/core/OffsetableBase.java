/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.antlr2.CsmAST;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 * Abstracr Base class for CsmOffsetable
 * @author Vladimir Kvashin
 */
public class OffsetableBase implements CsmOffsetable, CsmObject {

    private static abstract class PositionBase  implements CsmOffsetable.Position {
        public String toString() {
            return "" + getLine() + ':' + getColumn() + '/' + getOffset();
        }
    }
    
    private static class StartPositionImpl extends PositionBase {
        
        private CsmAST ast;
        
        public StartPositionImpl(CsmAST ast) {
            this.ast = ast;
        }

        public int getOffset() {
            return ast.getOffset();
        }
        
        public int getLine() {
            return ast.getLine();
        }

        public int getColumn() {
            return ast.getColumn();
        }
    }
    
    private static class EndPositionImpl extends PositionBase {

        private CsmAST ast;
        
        public EndPositionImpl(CsmAST ast) {
            this.ast = ast;
        }

        public int getOffset() {
            String text = ast.getText();
            return ast.getOffset() + (text == null ? 0 : text.length());
        }
        
        public int getLine() {
            return ast.getLine();
        }

        public int getColumn() {
            String text = ast.getText();
            return ast.getColumn() + (text == null ? 0 : text.length());
        }
    }
    
    private static class DummyPositionImpl extends PositionBase {
        
        private int offset;
        
        public DummyPositionImpl(int offset) {
            this.offset = offset;
        }

        public int getOffset() {
            return offset;
        }
        
        public int getLine() {
            return 0;
        }

        public int getColumn() {
            return 0;
        }
    }
    
    private CsmFile file;
    private int start;
    private int end;
    private AST ast;

    public OffsetableBase(AST ast, CsmFile file) {
        this.file = file;
        this.ast = ast;
    }
    
    public OffsetableBase(CsmFile file, int start, int end) {
        this.file = file;
        this.start = start;
        this.end = end;
    }
    
    public AST getAst() {
        return ast;
    }
    
    protected void setAst(AST ast) {
        this.ast = ast;
    }

    public int getStartOffset() {
        CsmAST ast = getStartAst();
        return (ast == null) ? start : ast.getOffset();
    }

    public int getEndOffset() {
        CsmAST ast = getEndAst();
        if( ast == null ) return 0;
        String text = ast.getText();
        return (ast == null) ? end : ast.getOffset() + (text == null ? 0 : text.length());
    }

    public Position getStartPosition() {
        CsmAST ast = getStartAst();
        return (ast == null) ? (Position) new DummyPositionImpl(start) : (Position) new StartPositionImpl(ast);
    }
    
    public Position getEndPosition() {
        CsmAST ast = getEndAst();
        return (ast == null) ? (Position) new DummyPositionImpl(end) : (Position) new EndPositionImpl(ast);
    }
    
    public CsmAST getStartAst() {
        if( ast != null ) {
            CsmAST csmAst = getFirstCsmAST(ast);
            if( csmAst != null ) {
                return csmAst;
            }
        }
        return null;
    }

    public CsmAST getEndAst() {
        if( ast != null ) {
            /**
            AST node = ast.getFirstChild();
            if( node == null ) {
                return (CsmAST) ast;
            }
            while( node.getNextSibling() != null ) {
                node = node.getNextSibling();
            }
            if( node instanceof CsmAST ) {
                return ((CsmAST) node);
            }
             **/
            AST lastChild = AstUtil.getLastChildRecursively(ast);
            if( lastChild instanceof CsmAST ) {
                return ((CsmAST) lastChild);
            }
        }
        return null;
    }
    
    protected CsmAST getFirstCsmAST(AST node) {
        if( node != null ) {
            if( node instanceof CsmAST ) {
                return (CsmAST) node;
            }
            else {
                return getFirstCsmAST(node.getFirstChild());
            }
        }
        return null;
    }
    
    public CsmFile getContainingFile() {
        return file;
    }

    public String getText() {
        return getContainingFile().getText(getStartOffset(), getEndOffset());
    }
    
}

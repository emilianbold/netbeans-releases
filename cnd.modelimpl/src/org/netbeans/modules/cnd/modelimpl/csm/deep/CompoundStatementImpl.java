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

package org.netbeans.modules.cnd.modelimpl.csm.deep;

import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.modelimpl.antlr2.CsmToken;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.generated.APTTokenTypes;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTToken;
import org.netbeans.modules.cnd.modelimpl.apt.utils.ListBasedTokenStream;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.antlr2.CPPParserEx;
import org.netbeans.modules.cnd.modelimpl.antlr2.CsmAST;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.Diagnostic;

/**
 * Common ancestor for all ... statements
 * @author Vladimir Kvashin
 */
public class CompoundStatementImpl extends StatementBase implements CsmCompoundStatement {
    
    private List/*<CsmStatement>*/ statements;
    private int firstTokenOffset;
    
    public CompoundStatementImpl(AST ast, CsmFile file) {
        super(ast, file);
        
        // remember start offset of compound statement
        firstTokenOffset = getStartOffset();
        
        // we need to throw away the compound statement AST under this element
        if (ast != null && ast.getType() == CPPTokenTypes.CSM_COMPOUND_STATEMENT_LAZY) {
            ast.setFirstChild(null);
        }        
    }
    
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.COMPOUND;
    }
    
    public List/*<CsmStatement>*/ getStatements() {
        if( statements == null ) {
            statements = new ArrayList();
            if( ! renderStatements() ) {
		statements = null;
		return Collections.EMPTY_LIST;
	    }
        }
        return statements;
    }
    
    protected boolean renderStatements() {
        AST curAst = getAst();
        if (curAst != null && curAst.getType() == CPPTokenTypes.CSM_COMPOUND_STATEMENT_LAZY) {
            TokenStream tokenStream = getTokenStream();
            if( tokenStream == null ) {
		return false;
	    }
	    AST resolvedAst = resolveLazyCompoundStatement(curAst, tokenStream);
	    // change AST kind from lazy to normal and change the lazy subtree 
	    // with new resolved tree
	    curAst.setType(CPPTokenTypes.CSM_COMPOUND_STATEMENT);
	    curAst.setText("CSM_COMPOUND_STATEMENT_LAZY (RESOLVED)");
	    curAst.setFirstChild(resolvedAst == null ? null : resolvedAst.getFirstChild());
        }
        renderStatements(curAst);
	return true;
    }
    
    private static class PushBackTokenStream implements TokenStream {
	private TokenStream stream;
	private Token first;
	public PushBackTokenStream(TokenStream stream, Token first) {
	    this.stream = stream;
	    this.first = first;
	}
	public Token nextToken() throws TokenStreamException {
	    if( first == null ) {
		return stream.nextToken();
	    }
	    else {
		Token result = first;
		first = null;
		return result;
	    }
	}
    };
    
    protected TokenStream getTokenStream() {
        FileImpl file = (FileImpl) getContainingFile();
        TokenStream  stream = file.getTokenStream();
        if( stream == null ) {
	    Utils.LOG.severe("Can't create compound statement: can't create token stream for file " + file.getAbsolutePath());
        }
	else {
	    try {
		for( Token next = stream.nextToken(); next != null && next.getType() != APTTokenTypes.EOF; next = stream.nextToken() ) {
		    assert (next instanceof APTToken ) : "we have only APTTokens in token stream";
                    int currOffset = ((APTToken) next).getOffset();
                    if( currOffset == firstTokenOffset ) {
                        return new PushBackTokenStream(stream, next);
                    }
		}
	        Utils.LOG.severe("Can't find token at offset " + firstTokenOffset + " in file: " + getContainingFile().getAbsolutePath() + " while restoring function body");
	    } catch (TokenStreamException ex) {
		Utils.LOG.severe("Can't create compound statement: " + ex.getMessage());
		return null;
	    }
	}
        return null;
    }
    
    protected void renderStatements(AST ast) {
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            CsmStatement stmt = AstRenderer.renderStatement(token, getContainingFile());
            if( stmt != null ) {
                statements.add(stmt);
            }
        }
    }

    public List getScopeElements() {
        return getStatements();
    }

    private AST resolveLazyCompoundStatement(AST curAst, TokenStream tokenStream) {
        AST out = curAst;
        if (curAst != null) {
            int flags = CPPParserEx.CPP_CPLUSPLUS;
            if( ! TraceFlags.REPORT_PARSING_ERRORS || Diagnostic.DEBUG ) {
                flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
            }            
            CPPParserEx parser = CPPParserEx.getInstance(getContainingFile().getName(), tokenStream, flags);
            parser.setLazyCompound(false);
            try {
                parser.compound_statement(false);
                if (false) {
                    throw new RecognitionException();
                }
            } catch (RecognitionException ex) {
                // it is OK to fail on uncompleted code
                // but report about problem
                Utils.LOG.severe(ex.toString());
            } catch (TokenStreamException ex) {
                // it is OK to fail on uncompleted code
                ex.printStackTrace(System.err);
            } finally {
                // try to get ast always
                out = parser.getAST();
            }
        }
        return out;
    }
}

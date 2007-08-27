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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm.deep;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import java.lang.ref.SoftReference;
import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Lazy statements
 * @author Vladimir Kvashin
 */
public final class LazyCompoundStatementImpl extends StatementBase implements CsmCompoundStatement {
    
    private SoftReference<List/*<CsmStatement>*/> statements = null;
    private final int firstTokenOffset;
    
    public LazyCompoundStatementImpl(AST ast, CsmFile file, CsmFunction scope) {
        super(ast, file, scope);
        assert(ast.getType() == CPPTokenTypes.CSM_COMPOUND_STATEMENT_LAZY);
        // remember start offset of compound statement
        firstTokenOffset = getStartOffset();
        // we need to throw away the compound statement AST under this element
        ast.setFirstChild(null);
    }
    
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.COMPOUND;
    }
    
    public List<CsmStatement> getStatements() {
	if( statements == null ) {
	    return createStatements();
	}
	else {
	    List<CsmStatement> list = statements.get();
	    return (list == null) ? createStatements() : list;
	}
    }
    
    /**
     * 1) Creates a list of statements
     * 2) If it is created successfully, stores a soft reference to this list
     *	  and returns this list,
     *    otherwise just returns empty list
     */
    public List/*<CsmStatement>*/ createStatements() {
	List list = new ArrayList();
	if( renderStatements(list) ) {
	    statements = new SoftReference(list);
	    return list;
	}
	else {
	    return Collections.EMPTY_LIST;
	}
    }
    
    private boolean renderStatements(List list) {
        TokenStream tokenStream = getTokenStream();
        if( tokenStream == null ) {
            return false;
        }
        AST resolvedAst = resolveLazyCompoundStatement(tokenStream);
        renderStatements(resolvedAst, list);
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
    
    private TokenStream getTokenStream() {
        FileImpl file = (FileImpl) getContainingFile();
        TokenStream  stream = file.getTokenStream();
        if( stream == null ) {
	    Utils.LOG.severe("Can't create compound statement: can't create token stream for file " + file.getAbsolutePath()); // NOI18N
        }
	else {
	    try {
		for( Token next = stream.nextToken(); next != null && next.getType() != CPPTokenTypes.EOF; next = stream.nextToken() ) {
		    assert (next instanceof APTToken ) : "we have only APTTokens in token stream";
                    int currOffset = ((APTToken) next).getOffset();
                    if( currOffset == firstTokenOffset ) {
                        return new PushBackTokenStream(stream, next);
                    }
		}
	        Utils.LOG.severe("Can't find token at offset " + firstTokenOffset + " in file: " + getContainingFile().getAbsolutePath() + " while restoring function body"); // NOI18N
	    } catch (TokenStreamException ex) {
		Utils.LOG.severe("Can't create compound statement: " + ex.getMessage());
		return null;
	    }
	}
        return null;
    }
    
    private void renderStatements(AST ast, List list) {
        for(ast = (ast == null ? null : ast.getFirstChild()); ast != null; ast = ast.getNextSibling() ) {
            CsmStatement stmt = AstRenderer.renderStatement(ast, getContainingFile(), this);
            if( stmt != null ) {
                list.add(stmt);
            }
        }
    }

    public List<CsmScopeElement> getScopeElements() {
        return (List)getStatements();
    }

    private AST resolveLazyCompoundStatement(TokenStream tokenStream) {
        int flags = CPPParserEx.CPP_CPLUSPLUS;
        if( ! TraceFlags.REPORT_PARSING_ERRORS || TraceFlags.DEBUG ) {
            flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
        }            
        CPPParserEx parser = CPPParserEx.getInstance(getContainingFile().getName(), tokenStream, flags);
        parser.setLazyCompound(false);
        parser.compound_statement();
        AST out = parser.getAST();
        return out;
    }
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
        output.writeInt(this.firstTokenOffset);
    }
    
    public LazyCompoundStatementImpl(DataInput input) throws IOException {
        super(input);
        this.firstTokenOffset = input.readInt();
        this.statements = null;
    }      
}

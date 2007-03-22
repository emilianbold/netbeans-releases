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

import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Abstracr Base class for CsmOffsetable
 * @author Vladimir Kvashin
 */
public abstract class OffsetableBase implements CsmOffsetable, CsmObject {
    // only one of fileRef/fileUID must be used (based on USE_REPOSITORY/USE_UID_TO_CONTAINER)
    private final CsmFile fileRef;
    private final CsmUID<CsmFile> fileUID;
    
    private final LineColOffsPositionImpl startPosition;
    private final LineColOffsPositionImpl endPosition;

    protected OffsetableBase(AST ast, CsmFile file) {
        if (TraceFlags.USE_REPOSITORY && TraceFlags.USE_UID_TO_CONTAINER) {
            this.fileUID = UIDCsmConverter.fileToUID(file);
            this.fileRef = null;// to prevent error with "final"
        } else {
            this.fileRef = file;
            this.fileUID = null;// to prevent error with "final"
        }
        //this.ast = ast;
        CsmAST startAST = getStartAst(ast);
        startPosition = (startAST == null) ? 
            new LineColOffsPositionImpl(0,0,0) :
            new LineColOffsPositionImpl(startAST.getLine(), startAST.getColumn(), startAST.getOffset());
        CsmAST endAST = getEndAst(ast);
        endPosition = (endAST == null) ? 
            new LineColOffsPositionImpl(0,0,0) : 
            new LineColOffsPositionImpl(endAST.getEndLine(), endAST.getEndColumn(), endAST.getEndOffset());
    }
    
    protected OffsetableBase(CsmFile file) {
        this(null, file);
    }
    
    protected OffsetableBase(CsmFile containingFile, CsmOffsetable pos) {
        this(containingFile, 
             pos != null ? pos.getStartPosition() : null, 
             pos != null ? pos.getEndPosition() : null);
    }
    
    protected OffsetableBase(CsmFile file, CsmOffsetable.Position start, CsmOffsetable.Position end) {
        if (TraceFlags.USE_REPOSITORY && TraceFlags.USE_UID_TO_CONTAINER) {
            this.fileUID = UIDCsmConverter.fileToUID(file);
            this.fileRef = null;// to prevent error with "final"
        } else {
            this.fileRef = file;
            this.fileUID = null;// to prevent error with "final"
        }        
        this.startPosition = new LineColOffsPositionImpl(start);
        this.endPosition = new LineColOffsPositionImpl(end);
    }
    
    public int getStartOffset() {
        return getStartPosition().getOffset();
    }
    
    public int getEndOffset() {
        return getEndPosition().getOffset();
    }

    public Position getStartPosition() {
        return startPosition;
    }
    
    public Position getEndPosition() {
        return endPosition;
    }
    
    private CsmAST getStartAst(AST node) {
        if( node != null ) {
            CsmAST csmAst = getFirstCsmAST(node);
            if( csmAst != null ) {
                return csmAst;
            }
        }
        return null;
    }

    protected CsmAST getEndAst(AST node) {
        if( node != null ) {
            AST lastChild = AstUtil.getLastChildRecursively(node);
            if( lastChild instanceof CsmAST ) {
                return ((CsmAST) lastChild);
            }
        }
        return null;
    }
    
    private static CsmAST getFirstCsmAST(AST node) {
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
        return _getFile();
    }

    public String getText() {
        return getContainingFile().getText(getStartOffset(), getEndOffset());
    }

    private CsmFile _getFile() {
        if (TraceFlags.USE_REPOSITORY && TraceFlags.USE_UID_TO_CONTAINER) {
            CsmFile file = UIDCsmConverter.UIDtoFile(fileUID);
            assert file != null : "no object for UID " + fileUID;
            return file;
        } else {
            return fileRef;
        }
    }

    protected void write(DataOutput output) throws IOException {
        startPosition.toStream(output);
        endPosition.toStream(output);
        CsmUID<CsmFile> writeFileUID;
        if (TraceFlags.USE_UID_TO_CONTAINER) {
            writeFileUID = this.fileUID;
        } else {
            // save reference
            assert this.fileRef != null;
            writeFileUID = UIDCsmConverter.fileToUID(this.fileRef);
        }        
        // not null UID
        assert writeFileUID != null;
        UIDObjectFactory.getDefaultFactory().writeUID(writeFileUID, output);
    }
    
    protected OffsetableBase(DataInput input) throws IOException {
        startPosition = new LineColOffsPositionImpl(input);
        endPosition = new LineColOffsPositionImpl(input);

        CsmUID<CsmFile> readFileUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // not null UID
        assert readFileUID != null;
        if (TraceFlags.USE_UID_TO_CONTAINER) {
            this.fileUID = readFileUID;
            
            this.fileRef = null;
        } else {
            // restore reference
            this.fileRef = UIDCsmConverter.UIDtoFile(readFileUID);
            assert this.fileRef != null || readFileUID == null : "no object for UID " + readFileUID;
            
            this.fileUID = null;
        }
        
        assert TraceFlags.USE_REPOSITORY;
    }
}

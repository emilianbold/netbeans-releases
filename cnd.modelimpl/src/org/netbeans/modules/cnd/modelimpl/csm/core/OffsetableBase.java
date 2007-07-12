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
public abstract class OffsetableBase implements CsmOffsetable, Disposable, CsmObject {
    // only one of fileRef/fileUID must be used (based on USE_REPOSITORY/USE_UID_TO_CONTAINER)
    private /*final*/ CsmFile fileRef; // can be set in onDispose or contstructor only
    private final CsmUID<CsmFile> fileUID;
    
    private final int startPosition;
    private final int endPosition;
    
    protected OffsetableBase(AST ast, CsmFile file) {
        if (TraceFlags.USE_REPOSITORY && TraceFlags.UID_CONTAINER_MARKER) {
            this.fileUID = UIDCsmConverter.fileToUID(file);
            this.fileRef = null;// to prevent error with "final"
        } else {
            this.fileRef = file;
            this.fileUID = null;// to prevent error with "final"
        }
        //this.ast = ast;
        CsmAST startAST = getStartAst(ast);
        startPosition = (startAST == null) ? 0 : startAST.getOffset();
        CsmAST endAST = getEndAst(ast);
        endPosition = (endAST == null) ? 0 : endAST.getEndOffset();
    }
    
    protected OffsetableBase(CsmFile containingFile, CsmOffsetable pos) {
        this(containingFile, 
                pos != null ? pos.getStartOffset() : 0,
                pos != null ? pos.getEndOffset() : 0);      
    }
    
    protected OffsetableBase(CsmFile file, int start, int end) {
        if (TraceFlags.USE_REPOSITORY && TraceFlags.UID_CONTAINER_MARKER) {
            this.fileUID = UIDCsmConverter.fileToUID(file);
            this.fileRef = null;// to prevent error with "final"
        } else {
            this.fileRef = file;
            this.fileUID = null;// to prevent error with "final"
        }        
        this.startPosition = start;
        this.endPosition = end;
    }
    
    public int getStartOffset() {
        return startPosition;
    }
    
    public int getEndOffset() {
        return endPosition;
    }

    public Position getStartPosition() {
        return new LazyOffsPositionImpl((FileImpl) this.getContainingFile(), startPosition);
    }
    
    public Position getEndPosition() {
        return new LazyOffsPositionImpl((FileImpl) this.getContainingFile(), endPosition);
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

    public void dispose() {
        onDispose();
    }
    
    private void onDispose() {
        if (TraceFlags.RESTORE_CONTAINER_FROM_UID) {
            // restore container from it's UID
            this.fileRef = UIDCsmConverter.UIDtoFile(fileUID);
            assert this.fileRef != null : "no object for UID " + fileUID;
        }
    }
    
    private CsmFile _getFile() {
        CsmFile file = this.fileRef;
        if (file == null) {
            if (TraceFlags.USE_REPOSITORY) {
                file = UIDCsmConverter.UIDtoFile(fileUID);
                assert file != null : "no object for UID " + fileUID;
            }            
        }
        return file;
    }

    protected void write(DataOutput output) throws IOException {
        output.writeInt(startPosition);
        output.writeInt(endPosition);
        // not null UID
        assert this.fileUID != null;
        UIDObjectFactory.getDefaultFactory().writeUID(this.fileUID, output);
    }
    
    protected OffsetableBase(DataInput input) throws IOException {
        startPosition = input.readInt();
        endPosition = input.readInt();

        this.fileUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // not null UID
        assert this.fileUID != null;          
        this.fileRef = null;
        
        assert TraceFlags.USE_REPOSITORY;
    }
    
    // test trace method
    protected String getOffsetString() {
        return "[" + getStartOffset() + "-" + getEndOffset() + "]"; // NOI18N
    }
}

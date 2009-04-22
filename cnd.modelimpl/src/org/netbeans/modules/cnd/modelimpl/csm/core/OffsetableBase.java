/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.openide.util.Parameters;

/**
 * Base class for CsmOffsetable
 * @author Vladimir Kvashin
 */
public class OffsetableBase implements CsmOffsetable, Disposable {
    // only one of fileRef/fileUID must be used (USE_UID_TO_CONTAINER)
    private /*final*/ CsmFile fileRef; // can be set in onDispose or contstructor only
    private final CsmUID<CsmFile> fileUID;
    
    private final int startPosition;
    private final int endPosition;
    
    protected OffsetableBase(AST ast, CsmFile file) {
        this(file, getStartOffset(ast), getEndOffset(ast));
    }
    
    protected OffsetableBase(CsmFile containingFile, CsmOffsetable pos) {
        this(containingFile, 
                pos != null ? pos.getStartOffset() : 0,
                pos != null ? pos.getEndOffset() : 0);      
    }
    
    public OffsetableBase(CsmFile file, int start, int end) {
        Parameters.notNull("file can not be null", file); // NOI18N
        this.fileUID = UIDCsmConverter.fileToUID(file);
        this.fileRef = null;// to prevent error with "final"
        this.startPosition = start;
        this.endPosition = end;
    }
    
    public int getStartOffset() {
        return startPosition;
    }
    
    public int getEndOffset() {
        return endPosition != 0 ? endPosition : startPosition;
    }

    public Position getStartPosition() {
        return new LazyOffsPositionImpl((FileImpl) this.getContainingFile(), getStartOffset());
    }
    
    public Position getEndPosition() {
        return new LazyOffsPositionImpl((FileImpl) this.getContainingFile(), getEndOffset());
    }
    
    public static int getStartOffset(AST node) {
        if( node != null ) {
            CsmAST csmAst = AstUtil.getFirstCsmAST(node);
            if( csmAst != null ) {
                return csmAst.getOffset();
            }
        }
        return 0;
    }

    public static int getEndOffset(AST node) {
        if( node != null ) {
            AST lastChild = AstUtil.getLastChildRecursively(node);
            if( lastChild instanceof CsmAST ) {
                return ((CsmAST) lastChild).getEndOffset();
            }
        }
        return 0;
    }
    
    public CsmFile getContainingFile() {
        return _getFile();
    }

    public CharSequence getText() {
        return getContainingFile().getText(getStartOffset(), getEndOffset());
    }

    public void dispose() {
        onDispose();
    }
    
    private synchronized void onDispose() {
        if (fileRef == null) {
            // restore container from it's UID
            this.fileRef = UIDCsmConverter.UIDtoFile(fileUID);
            assert this.fileRef != null : "no object for UID " + fileUID;
        }
    }
    
    private synchronized CsmFile _getFile() {
        CsmFile file = this.fileRef;
        if (file == null) {
            file = UIDCsmConverter.UIDtoFile(fileUID);
            assert file != null : "no object for UID " + fileUID;
        }
        return file;
    }

    public void write(DataOutput output) throws IOException {
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
    }
    
    // test trace method
    protected String getOffsetString() {
        return "[" + getStartOffset() + "-" + getEndOffset() + "]"; // NOI18N
    }

    @Override
    public String toString() {
        return getOffsetString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OffsetableBase other = (OffsetableBase) obj;
        if (this.fileUID != other.fileUID && (this.fileUID == null || !this.fileUID.equals(other.fileUID))) {
            return false;
        }
        if (this.startPosition != other.startPosition) {
            return false;
        }
        if (this.endPosition != other.endPosition) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.fileUID != null ? this.fileUID.hashCode() : 0);
        hash = 47 * hash + this.startPosition;
        hash = 47 * hash + this.endPosition;
        return hash;
    }
}

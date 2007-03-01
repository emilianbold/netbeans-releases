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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableIdentifiableBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.textcache.FileNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;

/**
 * Implements CsmInclude
 * @author Vladimir Kvasihn,
 *         Vladimir Voskresensky
 */
public class IncludeImpl extends OffsetableIdentifiableBase<CsmInclude> implements CsmInclude {
    private final String name;
    private final boolean system;
    
    // only one of includeFileOLD/includeFileUID must be used (based on USE_REPOSITORY)   
    private final CsmFile includeFileOLD;
    private final CsmUID<CsmFile> includeFileUID;
    
    public IncludeImpl(String name, boolean system, CsmFile includeFile, CsmFile containingFile, CsmOffsetable inclPos) {
        super(containingFile, inclPos);
        this.name = FileNameCache.getString(name);
        this.system = system;
        if (TraceFlags.USE_REPOSITORY) {
            this.includeFileUID = UIDCsmConverter.fileToUID(includeFile);
            assert (includeFileUID != null || includeFile == null);
            this.includeFileOLD = null;// to prevent error with "final"            
        } else {
            this.includeFileOLD = includeFile;
            this.includeFileUID = null;// to prevent error with "final"            
        }
    }
    
    public CsmFile getIncludeFile() {
        return _getIncludeFile();
    }

    public String getIncludeName() {
        return name;
    }

    public boolean isSystem() {
        return system;
    }
    
    public String toString() {
        char beg = isSystem() ? '<' : '"';
        char end = isSystem() ? '>' : '"';
        String error = "";
        if (getContainingFile() == null) {
            error = "<NO CONTAINER INFO> "; // NOI18N
        }
        return error + beg + getIncludeName() + end + 
                (getIncludeFile() == null ? " <FAILED inclusion>" : "") + // NOI18N
                " [" + getStartPosition() + "-" + getEndPosition() + "]"; // NOI18N
    }

    public boolean equals(Object obj) {
        boolean retValue;
        if (obj == null || !(obj instanceof IncludeImpl)) {
            retValue = false;
        } else {
            IncludeImpl other = (IncludeImpl)obj;
            retValue = IncludeImpl.equals(this, other);
        }
        return retValue;
    }
    
    private static final boolean equals(IncludeImpl one, IncludeImpl other) {
        // compare only name, type and start offset
        return (one.getIncludeName().compareTo(other.getIncludeName()) == 0) &&
                (one.system == other.system) && 
                (one.getStartOffset() == other.getStartOffset());
    }
    
    public int hashCode() {
        int retValue = 17*(isSystem() ? 1 : -1);
        retValue = 31*retValue + getStartOffset();
        retValue = 31*retValue + getIncludeName().hashCode();
        return retValue;
    }

    private CsmFile _getIncludeFile() {
        if (TraceFlags.USE_REPOSITORY) {
            CsmFile file = UIDCsmConverter.UIDtoFile(includeFileUID);
            assert (file != null || includeFileUID == null);
            return file;
        } else {
            return includeFileOLD;
        }
    }

    protected CsmUID createUID() {
        return UIDUtilities.createIncludeUID(this);
    }
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        output.writeUTF(this.name);
        output.writeBoolean(this.system);
        UIDObjectFactory.getDefaultFactory().writeUID(this.includeFileUID, output);
    }

    public IncludeImpl(DataInput input) throws IOException {
        super(input);
        this.name = FileNameCache.getString(input.readUTF());
        assert this.name != null;
        this.system = input.readBoolean();
        this.includeFileUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        
        assert TraceFlags.USE_REPOSITORY;
        this.includeFileOLD = null;// to prevent error with "final"        
    }    
}

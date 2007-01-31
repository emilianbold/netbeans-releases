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

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.LineColOffsetableBase;

/**
 * Implements CsmInclude
 * @author Vladimir Kvasihn,
 *         Vladimir Voskresensky
 */
public class IncludeImpl extends LineColOffsetableBase implements CsmInclude {

    private String text;
    private String name;
    private boolean system;
    private CsmFile includeFile;

    public IncludeImpl(String name, boolean system, CsmFile includeFile) {
        this(name, system, includeFile, null, null);
    }

    public IncludeImpl(String name, boolean system, CsmFile includeFile, CsmOffsetable inclPos) {
        this(name, system, includeFile, null, inclPos);
    }
    
    public IncludeImpl(CsmInclude incl, CsmFile containingFile) {
        this(incl.getIncludeName(), incl.isSystem(), incl.getIncludeFile(), containingFile, incl);
    }
    
    public IncludeImpl(String name, boolean system, CsmFile includeFile, CsmFile containingFile, CsmOffsetable inclPos) {
        super(containingFile, inclPos);
        this.name = name;
        this.system = system;
        this.includeFile = includeFile;
    }
    
    public CsmFile getIncludeFile() {
        return includeFile;
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
//
//    public String getText() {
//        if (text == null) {
//            char beg = isSystem() ? '<' : '"';
//            char end = isSystem() ? '>' : '"';
//            text = beg + getIncludeName() + end;   
//        }
//        return text;
//    }
}

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

package org.netbeans.modules.cnd.completion.csm;

import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import java.util.Iterator;
import java.util.List;

/**
 * utilities method for working with offsets of Csm objects
 * and CsmOffsetable objects
 * @author vv159170
 */
public class CsmOffsetUtilities {

    /** Creates a new instance of CsmOffsetUtils */
    private CsmOffsetUtilities() {
    }

    ////////////////////////////////////////////////////////////////////////////
    
    public static boolean isInObject(CsmObject obj, int offset) {
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return false;
        }
        CsmOffsetable offs = (CsmOffsetable)obj;
        if ((offs.getStartOffset() <= offset) &&
                (offset <= offs.getEndOffset())) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isBeforeObject(CsmObject obj, int offset) {
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return false;
        }
        CsmOffsetable offs = (CsmOffsetable)obj;
        if (offset < offs.getStartOffset()) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isAfterObject(CsmObject obj, int offset) {
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return false;
        }
        CsmOffsetable offs = (CsmOffsetable)obj;
        if (offset > offs.getEndOffset()) {
            return true;
        } else {
            return false;
        }
    }
    
    // list is ordered by offsettable elements
    public static CsmObject findObject(List/*<CsmObject>*/ list, int offset) {
        assert (list != null) : "expect not null list";
        for (Iterator it = list.iterator(); it.hasNext();) {
            Object obj = it.next();
            assert (obj != null) : "can't be null declaration";
            if (CsmKindUtilities.isCsmObject(obj)) {
                if (CsmOffsetUtilities.isInObject((CsmObject)obj, offset)) {
                    // we are inside csm element
                    return (CsmObject)obj;
                }
            }
        }
        return null;
    }
}

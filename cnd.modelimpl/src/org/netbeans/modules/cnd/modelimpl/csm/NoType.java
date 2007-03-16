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

/**
 * Used as return type for constructor and destructor
 * @author vk155633
 */
public class NoType implements CsmType {

    private static final NoType instance = new NoType();

    private Position position = new Position() {

        public int getOffset() {
            return 0;
        }

        public int getLine() {
            return 0;
        }

        public int getColumn() {
            return 0;
        }
    };
    
    /** prevents external creation */
    private NoType() {
    }
    
    public boolean isReference() {
        return false;
    }
    
    public boolean isPointer() {
        return false;
    }
    
    public boolean isConst() {
        return false;
    }
    
    public String getText() {
        return "";
    }
    
    public Position getStartPosition() {
        return null;
    }

    public int getStartOffset() {
        return 0;
    }

    public int getPointerDepth() {
        return 0;
    }

    public Position getEndPosition() {
        return null;
    }

    public int getEndOffset() {
        return 0;
    }

    public CsmFile getContainingFile() {
        return null;
    }

    public CsmClassifier getClassifier() {
        return null;
    }

    public int getArrayDepth() {
        return 0;
    }
    
    public static NoType instance() {
        return instance;
    }

    public boolean isBuiltInBased(boolean resolveTypeChain) {
        return false;
    }
}

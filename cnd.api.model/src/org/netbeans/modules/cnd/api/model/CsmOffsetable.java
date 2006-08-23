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

package org.netbeans.modules.cnd.api.model;

/**
 * An object, which has correspondent file and a pair of offsets (start and end)
 * @author Vladimir Kvashin
 */
public interface CsmOffsetable {

    interface Position {
        int getOffset();
        int getLine();
        int getColumn();
    }

    /** gets the file, which contains the given object */
    CsmFile getContainingFile();

    /** gets the offset of the 1-st character of the object */
    int getStartOffset();

    /** gets the offset of the character, following by the last character of the object */
    int getEndOffset();
    
    /** gets the position of the 1-st character of the object */ 
    Position getStartPosition();
    
    /** gets the position of the character, following by the last character of the object */
    Position getEndPosition();
    
    /** gets this object's text */
    String getText();
}

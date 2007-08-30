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

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;

/**
 * Trivial CsmOffsetable implementation
 * @author Vladimir Kvashin
 */
public class SimpleOffsetableImpl implements CsmOffsetable {
    
        private LineColOffsPositionImpl stPos = null;
        private LineColOffsPositionImpl endPos = null;

        public SimpleOffsetableImpl(int line, int col, int offset) {
            stPos = new LineColOffsPositionImpl(line, col, offset);
        }

        public void setEndPosition(Position startPosition) {
            endPos = new LineColOffsPositionImpl(startPosition);
        }
        
        public void setEndPosition(int line, int col, int offset) {
            endPos = new LineColOffsPositionImpl(line, col, offset);
        }
    
        public CsmFile getContainingFile() {
            return null;
        }

        public int getStartOffset() {
            return stPos.getOffset();
        }

        public int getEndOffset() {
            return endPos.getOffset();
        }

        public CsmOffsetable.Position getStartPosition() {
            return stPos;
        }

        public CsmOffsetable.Position getEndPosition() {
            return endPos;
        }    

        public String getText() {
            return null;
        }
    
}

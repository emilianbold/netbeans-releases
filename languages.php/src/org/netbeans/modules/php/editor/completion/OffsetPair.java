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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.php.editor.completion;

/**
 * Container to store a pair of offsets related to a document.
 * A pair of offsets is able to describe a part of the text into the document,
 * e.g. selected text, token location and so on. 
 * 
 * @author Victor G. Vasilyev
 */
public class OffsetPair {

    private int start;
    private int end;

    /**
     * Creates <code>OffsetPair</code> instance and sort the specified 
     * offsets so that getStartOffset() &lt;= getEndOffset().
     * 
     * @param startOffset the offset from the beginning of the document
     * @param endOffset the offset from the beginning of the document
     */
    public OffsetPair(int startOffset, int endOffset) {
        if(endOffset > startOffset) {
            this.start = startOffset;
            this.end = endOffset;
        }
        else {
            this.start = endOffset;
            this.end = startOffset;                
        }
    }

    /**
     * Fetches the offset from the beginning of the document that this 
     * selection begins at.
     * 
     * @return the starting offset &gt;= 0 and &lt;= getEndOffset()
     */
    public int getStartOffset() {
        return start;
    }

    /**
     * Fetches the offset from the beginning of the document that this 
     * selection ends at.
     * 
     * @return the ending offset &lt;= getStartOffset()
     */
    public int getEndOffset() {
        return end;
    }

    /**
     * Returns <code>true</code> if both offsets are valid, otherwise 
     * <code>false</code>.
     * 
     * @return <code>true</code> if both getStartOffset() and getEndOffset() 
     * &gt; 0, otherwise <code>false</code>.
     */
    public boolean isValid() {
       return start >= 0 && end >= 0; 
    }

    /**
     * Returns length of the entity (e.g. text) that is specified by the
     * underlying <code>OffsetPair</code>.
     * 
     * @return difference between <code>getEndOffset()</code> and 
     * <code>getStartOffset()</code>.
     */
    public int length() {
       return end - start; 
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(isValid() ? "Valid" : "Invalid");
        sb.append(" ");
        sb.append(OffsetPair.class.getName());
        sb.append(" [");
        sb.append("StartOffset=");
        sb.append(getStartOffset());
        sb.append(";");
        sb.append("EndOffset=");
        sb.append(getEndOffset());
        sb.append(";]");
        return sb.toString();
    }
    
    
}

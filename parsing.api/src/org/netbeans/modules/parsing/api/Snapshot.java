/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.api;


/**
 * Snapshot represents some part of text. Snapshot can be created from 
 * {@link Source} representing file or document, or from some other Snapshot.
 * So Snapshot can represent some block of code written 
 * in different language embedded inside some top level language too. It can contain
 * some generated parts of code that is not contained in the original 
 * file. Snapshot is immutable. It means that Snapshot created 
 * from document opened in editor contains some copy of original text. 
 * You do not need to call Snapshot methods under 
 * any locks, but on other hand Snapshot may not be up to date.
 * 
 * @author Jan Jancura
 */
public final class Snapshot {
    
    private CharSequence    text;
    private String          mimeType;
    int[][]                 positions;
    private Source          source;
    
   
    Snapshot (
        CharSequence        text, 
        Source              source,
        String              mimeType,
        int[][]             positions
    ) {
        this.text =         text;
        this.source =       source;
        this.mimeType =     mimeType;
        this.positions =    positions;
    }
    
    /**
     * Creates a new embedding form part of this snapshot defined by offset and length.
     * 
     * @param offset        A start offset of the new embedding. Start offset
     *                      is relative to the current snapshot.
     * @param length        A length of the new embedding.
     * @param mimeType      Mime type of the new embedding.
     * @return              The new embedding.
     * @throws IndexOutOfBoundsException when bounds of the new embedding exceeds 
     *                      original snapshot.
     */
    public Embedding create (
        int                 offset, 
        int                 length, 
        String              mimeType
    ) {
        int originalOffset = getOriginalOffset (offset);
        Snapshot snapshot = new Snapshot (
            getText ().subSequence (offset, offset + length),
            source,
            mimeType,
            new int[][] {new int[] {0, originalOffset}}
        );
        return new Embedding (
            snapshot, 
            mimeType
        );
    }
    
    /**
     * Creates a new embedding for given charSequence. 
     * 
     * @param charSequence  A text of new embedding.
     * @param mimeType      Mime type of the new embedding.
     * @return              The new embedding.
     */
    public Embedding create (
        CharSequence        charSequence, 
        String              mimeType
    ) {
        return new Embedding (
            new Snapshot (charSequence, source, mimeType, new int[][] {new int[] {}}),
            mimeType
        
        );
    }
    
    /**
     * Returns content of this snapshot.
     * 
     * @return              text of this snapshot
     */
    public CharSequence getText (
    ) {
        return text;
    }

    /**
     * Returns this snapshot's mime type.
     * 
     * @return              this snapshot mime type.
     */
    public String getMimeType (
    ) {
        return mimeType;
    }
    
    /**
     * Returns offset in original source corresponding to given offset related 
     * to this snapshot or <code>-1</code>. <code>-1</code> is returned if the text on 
     * the given position is "virtual" - generated by some preprocessor, 
     * and it has no representation in the top level code.
     * 
     * @param offset        a offset related to this snapshot
     * @return              position of given offset in original source
     */
    public int getOriginalOffset (
        int                 offset
    ) {
	int low = 0;
	int high = positions.length - 1;

	while (low <= high) {
	    int mid = (low + high) >> 1;
	    int cmp = positions [mid] [0];
            
            if (cmp > offset) 
		high = mid - 1;
            else
            if (mid == positions.length - 1 ||
                positions [mid + 1] [0] > offset
            )
                return offset - positions [mid] [0] + positions [mid] [1];
            else
		low = mid + 1;
	}
	return -1;
    }
    
    /**
     * Returns position in this snapshot corresponding to given offset 
     * in original source, or <code>-1</code>. <code>-1</code> is returned 
     * if the text on the given position is not a part of this snapshot.
     * 
     * @param originalOffset
     *                      a offset in original source
     * @return              position in this snapshot corresponding to given 
     *                      offset in original source, or <code>-1</code>
     */
    public int getEmbeddedOffset (
        int                 originalOffset
    ) {
	int low = 0;
	int high = positions.length - 1;

	while (low <= high) {
	    int mid = (low + high) >> 1;
	    int cmp = positions [mid] [1];
            
            if (cmp > originalOffset) 
		high = mid - 1;
            else
            if (mid == positions.length - 1 ||
                positions [mid + 1] [1] > originalOffset
            )
                if (originalOffset < positions [mid + 1] [0] - positions [mid] [0] + positions [mid] [1])
                    return originalOffset - positions [mid] [1] + positions [mid] [0];
                else
                    return -1;
            else
		low = mid + 1;
	}
	return -1;
    }
    
    /**
     * Returns source this shapshot has originally been created from.
     * 
     * @return              a source this shapshot has originally been created from.
     */
    public Source getSource () {
        return source;
    }
}




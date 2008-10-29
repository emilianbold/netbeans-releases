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

import java.util.ArrayList;
import java.util.List;


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
    int[][]                 currentToOriginal;
    int[][]                 originalToCurrent;
    private Source          source;
    
   
    Snapshot (
        CharSequence        text, 
        Source              source,
        String              mimeType,
        int[][]             currentToOriginal,
        int[][]             originalToCurrent
    ) {
        this.text =         text;
        this.source =       source;
        this.mimeType =     mimeType;
        this.currentToOriginal =    
                            currentToOriginal;
        this.originalToCurrent = 
                            originalToCurrent;
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
        if (offset < 0 || length < 0)
            throw new ArrayIndexOutOfBoundsException ();
        if (offset + length > getText ().length ())
            throw new ArrayIndexOutOfBoundsException ();
        List<int[]> newCurrentToOriginal = new ArrayList<int[]> ();
        List<int[]> newOriginalToCurrent = new ArrayList<int[]> ();
        int i = 1;
        while (i < currentToOriginal.length && currentToOriginal [i] [0] <= offset) i++;
        if (currentToOriginal [i - 1] [1] < 0)
            newCurrentToOriginal.add (new int[] {
                0, currentToOriginal [i - 1] [1]
            });
        else {
            newCurrentToOriginal.add (new int[] {
                0, currentToOriginal [i - 1] [1] + offset
            });
            newOriginalToCurrent.add (new int[] {
                currentToOriginal [i - 1] [1] + offset, 0
            });
        }
        for (; i < currentToOriginal.length && currentToOriginal [i] [0] < offset + length; i++) {
            newCurrentToOriginal.add (new int[] {
                currentToOriginal [i] [0] - offset, currentToOriginal [i] [1]
            });
            if (currentToOriginal [i] [1] >= 0)
                newOriginalToCurrent.add (new int[] {
                    currentToOriginal [i] [1], currentToOriginal [i] [0] - offset
                });
            else
                newOriginalToCurrent.add (new int[] {
                    newOriginalToCurrent.get (i - 1) [0] + newCurrentToOriginal.get (i) [0] - newCurrentToOriginal.get (i - 1) [0], -1
                });
        }
        if (newOriginalToCurrent.get (newOriginalToCurrent.size () - 1) [1] >= 0)
            newOriginalToCurrent.add (new int[] {
                newOriginalToCurrent.get (newOriginalToCurrent.size () - 1) [0] + 
                    length - 
                    newOriginalToCurrent.get (newOriginalToCurrent.size () - 1) [1], 
                -1
            });
        Snapshot snapshot = new Snapshot (
            getText ().subSequence (offset, offset + length),
            source,
            mimeType,
            (int[][]) newCurrentToOriginal.toArray (new int [newCurrentToOriginal.size ()][]),
            (int[][]) newOriginalToCurrent.toArray (new int [newOriginalToCurrent.size ()][])
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
            new Snapshot (charSequence, source, mimeType, new int[][] {new int[] {0, -1}}, new int[][] {}),
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
        if (offset > getText ().length ())
            throw new ArrayIndexOutOfBoundsException ();
	int low = 0;
	int high = currentToOriginal.length - 1;

	while (low <= high) {
	    int mid = (low + high) >> 1;
	    int cmp = currentToOriginal [mid] [0];
            
            if (cmp > offset) 
		high = mid - 1;
            else
            if (mid == currentToOriginal.length - 1 ||
                currentToOriginal [mid + 1] [0] > offset
            )
                if (currentToOriginal [mid] [1] < 0)
                    return currentToOriginal [mid] [1];
                else
                    return offset - currentToOriginal [mid] [0] + currentToOriginal [mid] [1];
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
	int high = originalToCurrent.length - 1;

	while (low <= high) {
	    int mid = (low + high) >> 1;
	    int cmp = originalToCurrent [mid] [0];
            
            if (cmp > originalOffset) 
		high = mid - 1;
            else
            if (mid == originalToCurrent.length - 1 ||
                originalToCurrent [mid + 1] [0] > originalOffset
            )
                if (originalToCurrent [mid] [1] < 0)
                    return originalToCurrent [mid] [1];
                else
                    return originalOffset - originalToCurrent [mid] [0] + originalToCurrent [mid] [1];
            else
		low = mid + 1;
	}
	return -1;
    }
    
    /**
     * Returns source this snapshot has originally been created from.
     * 
     * @return              a source this snapshot has originally been created from.
     */
    public Source getSource () {
        return source;
    }
}




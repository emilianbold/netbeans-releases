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
 * Represents one block of code embedded in some other source. Performance is the only
 * purpose of this class. You can obtain some basic inforation about embedded
 * {@link Source} before it is really created.
 * 
 * @author Jan Jancura
 */
public final class Embedding {
    
    /**
     * Creates source from a parts of this source. New source contains one 
     * or more blocks of text from this source. All sources must have the same
     * DataObject and Document like this document. All sources must have the
     * same mime type, but this mime type have to be different than current
     * source mime type.
     * 
     * @param sources       A list of some sources created from this source.
     * @return              A new source compound from given pieces.
     */
    public static Embedding create (
        List<Embedding>        embeddings
    ) {
        String mimeType = null;
        Source source = null;
        StringBuilder sb = new StringBuilder ();
        List<int[]> positions = new ArrayList<int[]> ();
        List<int[]> indexes = new ArrayList<int[]> ();
        int offset = 0;
        for (Embedding embedding : embeddings) {
            Snapshot snapshot = embedding.getSnapshot ();
            if (mimeType != null) {
                if (!mimeType.equals (embedding.mimeType))
                    throw new IllegalArgumentException ();
                if (source != snapshot.getSource ())
                    throw new IllegalArgumentException ();
            } else {
                mimeType = embedding.mimeType;
                source = snapshot.getSource ();
            }
            sb.append (snapshot.getText ());
            int[][] p = snapshot.positions;
            for (int i = 0; i < p.length; i++) {
                positions.add (new int[] {p [i] [0] + offset, p [i] [1] - offset});
            }
            p = embedding.positions;
            for (int i = 0; i < p.length; i++) {
                indexes.add (p [i]);
            }
            offset +=snapshot.getText ().length ();
        }
        Snapshot snapshot = new Snapshot (
            sb,
            source,
            mimeType,
            positions.toArray (new int [positions.size ()] [])
        );
        return new Embedding (
            snapshot, 
            mimeType, 
            indexes.toArray (new int [indexes.size ()] [])
        );
    }
    
    private Snapshot        snapshot;
    private String          mimeType;
    private int[][]         positions;
                
    Embedding (
        Snapshot            snapshot,
        String              mimeType,
        int[][]             positions
    ) {
        this.snapshot =     snapshot;
        this.mimeType =     mimeType;
        this.positions =    positions;
    }
    
    /**
     * Returns {@link Source} for embedded block of code.
     * 
     * @return              A {@link Source} for embedded block of code..
     */
    public final Snapshot getSnapshot () {
        return snapshot;
    }
    
    /**
     * Returns mime type of embedded source.
     * 
     * @return              A mime type of embedded source.
     */
    public final String getMimeType () {
        return mimeType;
    }
    
    /**
     * Returns <code>true</code> if embeddid source contains given offset.
     * 
     * @param offset        A offset.
     * @return              <code>true</code> if embeddid source contains given offset.
     */
    public final boolean containsOffset (int offset) {
	int low = 0;
	int high = positions.length - 1;

	while (low <= high) {
	    int mid = (low + high) >> 1;
	    //Comparable<? super T> midVal = list.get(mid);
	    int cmp = positions [mid] [0];//midVal.compareTo(key);
            
            if (cmp > offset) 
		high = mid - 1;
            else
            if (cmp == offset)
                return true;
            else
            if (positions [mid] [1] > offset)
                return true;
            else
		low = mid + 1;
	}
	return false;
    }
}





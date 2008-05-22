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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.SourceFlags;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;


/**
 * Source represents some part of text. Source can be created from file or document
 * opened in editor. And source can represent some block of code written 
 * in different language embedded inside some top level language too. It can contain
 * some generated parts of code that is not contained in the original 
 * file. Source is read only and immutable. It means that source created 
 * from document opened in editor contains some copy of original text. 
 * You do not need to call source methods under 
 * any locks, but on other hand source may not be up to date.
 *
 * Following example shows how to create Source for block of code 
 * embedded in other source:
 * {@link EmbeddingProvider}:
 * 
 * <pre> 
 *           int start = findStartJavaOffset (source);
 *           int length = getJavaBlockLength (source, start);
 *           return source.create (Arrays.asList (new Source[] {
 *               source.create ("some prefix code", "text/x-java"),
 *               source.create (start, length, "text/x-java"),
 *               source.create ("some postfix code", "text/x-java")
 *           })));
 * </pre>
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
     * Creates a new embedding form part of this source defined by offset and length.
     * The new embedding has the same Document and FileObjet property values, 
     * but mimeType should be different than original one. 
     * 
     * @param offset        A start offset of the new source. Start offset
     *                      is relative to the current source.
     * @param length        A length of the new source.
     * @param mimeType      Mime type of the new source.
     * @return              The new source.
     * @throws IndexOutOfBoundsException when bounds of the new source exceeds 
     *                      original source.
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
            mimeType, 
            new int[][] {new int[] {
                offset + originalOffset, 
                offset + originalOffset + length
            }}
        );
    }
    
    /**
     * Creates a new source for given charSequence. 
     * The new source has the same Document and FileObjet property values, 
     * but mimeType should be different than original one. 
     * 
     * @param charSequence  A text of new source.
     * @param mimeType      Mime type of the new source.
     * @return              The new source.
     */
    public Embedding create (
        CharSequence        charSequence, 
        String              mimeType
    ) {
        return new Embedding (
            new Snapshot (charSequence, source, mimeType, new int[][] {new int[] {}}),
            mimeType,
            new int[][] {}
        
        );
    }
    
    /**
     * Returns content of source.
     * 
     * @return              text of this source
     */
    public CharSequence getText (
    ) {
        return text;
    }

    /**
     * Returns source mime type.
     * 
     * @return              this source mime type.
     */
    public String getMimeType (
    ) {
        return mimeType;
    }
    
    /**
     * Returns position of given offset in the original document or file, 
     * or <code>-1</code>. <code>-1</code> is returned if the text on 
     * the given position is "virtual" - generated by some preprocessor, 
     * and it has no representation in the top level code.
     * 
     * @param offset        a offset related to this source
     * @return              position of given offset in original document or file
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
            if (cmp == offset)
                return offset + positions [mid] [1];
            else
            if (positions [mid] [1] > offset)
                return offset + positions [mid] [1];
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




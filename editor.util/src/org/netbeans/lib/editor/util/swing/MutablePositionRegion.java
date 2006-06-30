/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.lib.editor.util.swing;

import java.util.Comparator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;

/**
 * A pair of positions delimiting a text region in a swing document.
 * <br/>
 * At all times it should be satisfied that
 * {@link #getStartOffset()} &lt;= {@link #getEndOffset()}.
 *
 * @author Miloslav Metelka
 * @since 1.6
 */

public class MutablePositionRegion extends PositionRegion {

    /**
     * Construct new mutable position region.
     *
     * @param startPosition non-null start position of the region &lt;= end position.
     * @param endPosition non-null end position of the region &gt;= start position.
     */
    public MutablePositionRegion(Position startPosition, Position endPosition) {
        super(startPosition, endPosition);
    }
    
    /**
     * Construct new mutable position region based on the knowledge
     * of the document and starting and ending offset.
     */
    public MutablePositionRegion(Document doc, int startOffset, int endOffset) throws BadLocationException {
        this(doc.createPosition(startOffset), doc.createPosition(endOffset));
    }
    
    /**
     * Set a new start position of this region.
     * <br/>
     * It should satisfy
     * {@link #getStartOffset()} &lt;= {@link #getEndOffset()}.
     *
     * @param startPosition non-null new start position of this region.
     */
    public void setStartPosition(Position startPosition) {
        setStartPositionImpl(startPosition);
    }

    /**
     * Set a new end position of this region.
     * <br/>
     * It should satisfy
     * {@link #getStartOffset()} &lt;= {@link #getEndOffset()}.
     *
     * @param endPosition non-null new start position of this region.
     */
    public void setEndPosition(Position endPosition) {
        setEndPositionImpl(endPosition);
    }

}

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

package org.netbeans.api.java.source;

import static org.netbeans.modules.java.source.save.PositionEstimator.*;

/**
 * An individual comment, consisting of a style, begin and end source
 * file position, the indention (column) of its first character, and its text.
 */
public final class Comment {
    private Style style;
    private int pos;
    private int endPos;
    private int indent;
    private String text;

    /**
     * The set of different comment types.
     */
    public enum Style {
        /**
         * A line (double-slash) comment.
         */
        LINE,
        
        /**
         * A block comment.
         */
        BLOCK,
        
        /**
         * A JavaDoc comment.
         */
        JAVADOC,
        
        /**
         * Whitespace
         * TODO: not comment, but requested by another teams to preserve
         * empty lines etc.
         */
        WHITESPACE;
    }

    /**
     * Define a new block comment from a string.  This comment does not
     * have source file positions.
     */
    public static Comment create(String s) {
        return new Comment(Style.BLOCK, NOPOS, NOPOS, NOPOS, s);
    }
    
    public static Comment create(Style style, int pos, int endPos, int indent, String text) {
        return new Comment(style, pos, endPos, indent, text);
    }
    
    /**
     * Define a comment, using source file positions.
     */
    private Comment(Style style, int pos, int endPos, int indent, String text) {
        this.style = style;
        this.pos = pos;
        this.endPos = endPos;
        this.indent = indent;
        this.text = text;
    }
    
    public Style style() {
        return style;
    }

    /**
     * The start position in the source file, or NOPOS if the
     * comment was added by a translation operation.
     */
    public int pos() {
        return pos;
    }

    /**
     * The end position in the source file, or NOPOS if the
     * comment was added by a translation operation.
     */
    public int endPos() {
        return endPos;
    }

    /**
     * Returns the line indention for this comment, or NOPOS if the
     * comment was added by a translation operation.
     */
    public int indent() {
        return indent;
    }
    
    /** Returns true if this is a JavaDoc comment. */
    public boolean isDocComment() {
        return style == Style.JAVADOC;
    }

    /**
     * Returns the comment text.
     */
    public String getText() {
        return text;
    }
    
    public boolean isNew() {
        return pos == NOPOS;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder(style.toString());
        sb.append(" pos=");
        sb.append(pos);
        sb.append(" endPos=");
        sb.append(endPos);
        sb.append(" indent=");
        sb.append(indent);
        sb.append(' ');
        sb.append(text);
        return sb.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Comment))
            return false;
        Comment c = (Comment)obj;
        return c.style == style && c.pos == pos && c.endPos == endPos &&
            c.indent == indent && c.text.equals(text);
    }

    public int hashCode() {
        return style.hashCode() + pos + endPos + indent + text.hashCode();
    }
}

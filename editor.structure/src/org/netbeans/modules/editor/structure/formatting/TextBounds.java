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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.structure.formatting;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class TextBounds {

    private int absoluteStart; // start offset regardless of white spaces
    private int absoluteEnd; // end --
    private int startPos = -1;
    private int endPos = -1;
    private int startLine = -1;
    private int endLine = -1;

    public TextBounds(int absoluteStart, int absoluteEnd) {
        this.absoluteStart = absoluteStart;
        this.absoluteEnd = absoluteEnd;
    }

    public TextBounds(int absoluteStart, int absoluteEnd, int startPos, int endPos, int startLine, int endLine) {
        this.absoluteStart = absoluteStart;
        this.absoluteEnd = absoluteEnd;
        this.startPos = startPos;
        this.endPos = endPos;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public int getEndPos() {
        return endPos;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getAbsoluteEnd() {
        return absoluteEnd;
    }

    public int getAbsoluteStart() {
        return absoluteStart;
    }

    @Override
    public String toString() {
        return "pos " + startPos + "-" + endPos + ", lines " + startLine + "-" + endLine; //NOI18N
    }
}
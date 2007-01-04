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

package org.netbeans.modules.cnd.editor.parser;

import org.openide.ErrorManager;

public class CppFoldRecord {

    // Fold types
    public final static int INITIAL_COMMENT_FOLD = CppFile.INITIAL_COMMENT_FOLD;
    public final static int BLOCK_COMMENT_FOLD = CppFile.BLOCK_COMMENT_FOLD;
    public final static int COMMENTS_FOLD = CppFile.COMMENTS_FOLD;    
    public final static int INCLUDES_FOLD = CppFile.INCLUDES_FOLD;
    public final static int IFDEF_FOLD = CppFile.IFDEF_FOLD;
    public final static int CLASS_FOLD = CppFile.CLASS_FOLD;
    public final static int FUNCTION_FOLD = CppFile.FUNCTION_FOLD;
    public final static int CONSTRUCTOR_FOLD = CppFile.CONSTRUCTOR_FOLD;
    public final static int DESTRUCTOR_FOLD = CppFile.DESTRUCTOR_FOLD;
    public final static int NAMESPACE_FOLD = CppFile.NAMESPACE_FOLD;
    
    private int type;
    private int startLnum;
    private int endLnum;
    private int startOffset;
    private int endOffset;

    private static final ErrorManager log =
		ErrorManager.getDefault().getInstance("CppFoldTracer"); // NOI18N

    public CppFoldRecord(int type, int startLnum, int startOffset, int endLnum, int endOffset)
    {
	this.type = type;
	this.startLnum = startLnum;
	this.startOffset = startOffset;
	this.endLnum = endLnum;
	this.endOffset = endOffset;
	log.log(toString() + " [" + Thread.currentThread().getName() + "]");
    }

    public CppFoldRecord(int type, int startOffset, int endOffset)
    {
	this.type = type;
	this.startLnum = startLnum;
	this.startOffset = startOffset;
	this.endLnum = endLnum;
	this.endOffset = endOffset;
    }
    
    public void setLines(int start, int end) {
        this.startLnum = start;
        this.endLnum = end;
    }
    
    public int getType() {
	return type;
    }

    public int getStartOffset() {
	return startOffset;
    }

    public int getEndOffset() {
	return endOffset;
    }

    public int getStartLine() {
        return startLnum;
    }
    
    public int getEndLine() {
        return endLnum;
    }
    
    public String toString() {
        // I'm considering this as a debug function and making all
	// strings NOI18N
        
        String kind = "Unknown Fold"; // NOI18N
        switch (type) {
            case INITIAL_COMMENT_FOLD:
                kind = "INITIAL_COMMENT_FOLD"; // NOI18N
                break;
            case BLOCK_COMMENT_FOLD:
                kind = "BLOCK_COMMENT_FOLD"; // NOI18N
                break;
            case COMMENTS_FOLD:
                kind = "COMMENTS_FOLD"; // NOI18N
                break;
            case INCLUDES_FOLD:
                kind = "INCLUDES_FOLD"; // NOI18N
                break;
            case IFDEF_FOLD:
                kind = "IFDEF_FOLD"; // NOI18N
                break;
            case CLASS_FOLD:
                kind = "CLASS_FOLD"; // NOI18N
                break;
            case FUNCTION_FOLD:
                kind = "FUNCTION_FOLD"; // NOI18N
                break;
            case CONSTRUCTOR_FOLD:
                kind = "CONSTRUCTOR_FOLD"; // NOI18N
                break;
            case DESTRUCTOR_FOLD:
                kind = "DESTRUCTOR_FOLD"; // NOI18N
                break;
            case NAMESPACE_FOLD:
                kind = "NAMESPACE_FOLD"; // NOI18N
                break;
            default:
        }
	return kind + " (" + startLnum + // NOI18N
		", " + startOffset + ", " + endLnum + ", " + endOffset + ")"; // NOI18N
    }
}

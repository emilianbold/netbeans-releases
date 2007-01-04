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

package org.netbeans.modules.cnd.editor;

import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.fold.FoldManager;

/**
 *  Fold maintainer/manager base class for C and C++ (not yet supporting Fortran).
 *  This code is derived from the NetBeans 4.1 versions of the JavaFoldManager
 *  in the java/editor module.
 */

public abstract class CppFoldManagerBase implements FoldManager {

    public static final FoldType INITIAL_COMMENT_FOLD_TYPE =
		    new FoldType("initial-comment"); // NOI18N

    public static final FoldType INCLUDES_FOLD_TYPE = new FoldType("includes"); // NOI18N
    
    public static final FoldType COMMENT_FOLD_TYPE = new FoldType("block-comments"); // NOI18N
    
    public static final FoldType LINE_COMMENT_FOLD_TYPE = new FoldType("line-comments"); // NOI18N

    public static final FoldType CODE_BLOCK_FOLD_TYPE = new FoldType("code-block"); // NOI18N
    
    public static final FoldType IFDEF_FOLD_TYPE = new FoldType("#ifdef"); // NOI18N

    private static final String INCLUDES_FOLD_DESCRIPTION = "..."; // NOI18N

    private static final String COMMENT_FOLD_DESCRIPTION = "/*...*/"; // NOI18N
    
    private static final String LINE_COMMENT_FOLD_DESCRIPTION = "//..."; // NOI18N

    private static final String CODE_BLOCK_FOLD_DESCRIPTION = "{...}"; // NOI18N
    
    private static final String IFDEF_FOLD_DESCRIPTION = "..."; // NOI18N
    
    public static final FoldTemplate INITIAL_COMMENT_FOLD_TEMPLATE
        = new FoldTemplate(INITIAL_COMMENT_FOLD_TYPE, COMMENT_FOLD_DESCRIPTION, 2, 2);

    public static final FoldTemplate INCLUDES_FOLD_TEMPLATE
        = new FoldTemplate(INCLUDES_FOLD_TYPE, INCLUDES_FOLD_DESCRIPTION, 1, 0);

    public static final FoldTemplate COMMENT_FOLD_TEMPLATE
        = new FoldTemplate(COMMENT_FOLD_TYPE, COMMENT_FOLD_DESCRIPTION, 2, 2);
    
    public static final FoldTemplate LINE_COMMENT_FOLD_TEMPLATE
        = new FoldTemplate(LINE_COMMENT_FOLD_TYPE, LINE_COMMENT_FOLD_DESCRIPTION, 2, 0);    

    public static final FoldTemplate CODE_BLOCK_FOLD_TEMPLATE
        = new FoldTemplate(CODE_BLOCK_FOLD_TYPE, CODE_BLOCK_FOLD_DESCRIPTION, 1, 1);

    public static final FoldTemplate IFDEF_FOLD_TEMPLATE
        = new FoldTemplate(IFDEF_FOLD_TYPE, IFDEF_FOLD_DESCRIPTION, 0, 0);

    /* Copied from JavaFoldManger in java/editor/lib */
    protected static final class FoldTemplate {
        
        private FoldType type;
        
        private String description;
        
        private int startGuardedLength;
        
        private int endGuardedLength;
        
        protected FoldTemplate(FoldType type, String description,
			int startGuardedLength, int endGuardedLength) {
            this.type = type;
            this.description = description;
            this.startGuardedLength = startGuardedLength;
            this.endGuardedLength = endGuardedLength;
        }
        
        public FoldType getType() {
            return type;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getStartGuardedLength() {
            return startGuardedLength;
        }
        
        public int getEndGuardedLength() {
            return endGuardedLength;
        }
    }

}

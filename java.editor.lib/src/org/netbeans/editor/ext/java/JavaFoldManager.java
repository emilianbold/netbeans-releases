/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.ext.java;

import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.fold.FoldManager;

/**
 * Java fold maintainer creates and updates folds for java sources.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class JavaFoldManager implements FoldManager {
    
    public static final FoldType INITIAL_COMMENT_FOLD_TYPE = new FoldType("initial-comment"); // NOI18N
    
    public static final FoldType IMPORTS_FOLD_TYPE = new FoldType("imports"); // NOI18N
    
    public static final FoldType JAVADOC_FOLD_TYPE = new FoldType("javadoc"); // NOI18N

    public static final FoldType CODE_BLOCK_FOLD_TYPE = new FoldType("code-block"); // NOI18N
    
    private static final String IMPORTS_FOLD_DESCRIPTION = "..."; // NOI18N

    private static final String COMMENT_FOLD_DESCRIPTION = "/*...*/"; // NOI18N

    private static final String JAVADOC_FOLD_DESCRIPTION = "/**...*/"; // NOI18N
    
    private static final String CODE_BLOCK_FOLD_DESCRIPTION = "{...}"; // NOI18N
    
    public static final FoldTemplate INITIAL_COMMENT_FOLD_TEMPLATE
        = new FoldTemplate(INITIAL_COMMENT_FOLD_TYPE, COMMENT_FOLD_DESCRIPTION, 2, 2);

    public static final FoldTemplate IMPORTS_FOLD_TEMPLATE
        = new FoldTemplate(IMPORTS_FOLD_TYPE, IMPORTS_FOLD_DESCRIPTION, 7, 0);

    public static final FoldTemplate JAVADOC_FOLD_TEMPLATE
        = new FoldTemplate(JAVADOC_FOLD_TYPE, JAVADOC_FOLD_DESCRIPTION, 3, 2);

    public static final FoldTemplate CODE_BLOCK_FOLD_TEMPLATE
        = new FoldTemplate(CODE_BLOCK_FOLD_TYPE, CODE_BLOCK_FOLD_DESCRIPTION, 1, 1);

    
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

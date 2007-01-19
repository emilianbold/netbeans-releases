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

package org.netbeans.api.editor.settings;

/**
 * Fonts and Colors settings names
 *
 * @author Martin Roskanin
 */
public final class FontColorNames {

    /** Default coloring for the drawing. */
    public static final String DEFAULT_COLORING = "default"; // NOI18N

    /**
     * Coloring that will be used for line numbers displayed on the left
     * side on the screen.
     */
    public static final String LINE_NUMBER_COLORING = "line-number"; // NOI18N

    /** Coloring used for guarded blocks */
    public static final String GUARDED_COLORING = "guarded"; // NOI18N

    /**
     * Coloring that will be used for code folding icons displayed in editor
     */
    public static final String CODE_FOLDING_COLORING = "code-folding"; // NOI18N

    /** Coloring that will be used for code folding side bar */
    public static final String CODE_FOLDING_BAR_COLORING = "code-folding-bar"; // NOI18N
    
    /** Coloring used for selection */
    public static final String SELECTION_COLORING = "selection"; // NOI18N

    /** Coloring used for highlight search */
    public static final String HIGHLIGHT_SEARCH_COLORING = "highlight-search"; // NOI18N

    /** Coloring used for incremental search */
    public static final String INC_SEARCH_COLORING = "inc-search"; // NOI18N

    /** Coloring used for block search */
    public static final String BLOCK_SEARCH_COLORING = "block-search"; // NOI18N
    
    /** Coloring used for the status bar */
    public static final String STATUS_BAR_COLORING = "status-bar"; // NOI18N

    /** Coloring used to mark important text in the status bar */
    public static final String STATUS_BAR_BOLD_COLORING = "status-bar-bold"; // NOI18N
    
    /** Coloring used to highlight the row where the caret resides */
    public static final String CARET_ROW_COLORING = "highlight-caret-row"; // NOI18N
    
    private FontColorNames() {
        // to prevent instantialization
    }
}

/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */
package org.netbeans.installer.infra.utils.comment.handlers;

import java.io.File;
import java.util.regex.Pattern;
import org.netbeans.installer.infra.utils.comment.utils.Utils;

/**
 * A {@link FileHandler} implementation capable of handling Java, C, C++ source and
 * header files.
 *
 * @author Kirill Sorokin
 */
public class SourcesFileHandler extends BlockFileHandler {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Creates a new instance of {@link SourcesFileHandler}. The constuctor
     * simply falls back to the
     * {@link BlockFileHandler#BlockFileHandler(Pattern, String, String, String)}
     * passing in the parameters relevant to source files.
     */
    public SourcesFileHandler() {
        super(COMMENT_PATTERN,
                COMMENT_START,
                COMMENT_PREFIX,
                COMMENT_END);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean accept(final File file) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "The 'file' parameter cannot be null.");                // NOI18N
        }
        
        if (!file.isFile()) {
            return false;
        }
        
        return file.getName().endsWith(".java") ||                          // NOI18N
                file.getName().endsWith(".c") ||                            // NOI18N
                file.getName().endsWith(".cpp") ||                          // NOI18N
                file.getName().endsWith(".h");                              // NOI18N
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * The regular expression pattern which matches the initial comment.
     */
    private static final Pattern COMMENT_PATTERN = Pattern.compile(
            "\\A\\s*(/\\*.*?\\*/)",                                         // NOI18N
            Pattern.MULTILINE | Pattern.DOTALL);
    
    /**
     * The comment opening string.
     */
    private static final String COMMENT_START =
            "/*" + Utils.NL;                                                // NOI18N
    
    /**
     * The prefix which should be used for each line in the comment.
     */
    private static final String COMMENT_PREFIX =
            " * ";                                                          // NOI18N
    
    /**
     * The comment closing string.
     */
    private static final String COMMENT_END =
            " */";                                                          // NOI18N
}

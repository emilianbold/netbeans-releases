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

/**
 * A {@link FileHandler} implementation capable of handling java-style properties
 * files.
 *
 * @author Kirill Sorokin
 */
public class PropertiesFileHandler extends LineFileHandler {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Creates a new instance of {@link PropertiesFileHandler}. The constuctor 
     * simply falls back to the
     * {@link LineFileHandler#LineFileHandler(Pattern, Pattern, String)} passing in
     * the parameters relevant to properties files.
     */
    public PropertiesFileHandler() {
        super(COMMENT_PATTERN,
                IGNORE_PATTERN,
                COMMENT_PREFIX);
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
        
        return file.getName().endsWith(".properties");                      // NOI18N
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * The regular expression pattern which matches the line which is a comment.
     */
    private static final Pattern COMMENT_PATTERN = Pattern.compile(
            "^\\s*#.*");                                                    // NOI18N
    
    /**
     * The regular expression pattern which matches the line which should be
     * ignored.
     */
    private static final Pattern IGNORE_PATTERN = Pattern.compile(
            "^$");                                                          // NOI18N
    
    /**
     * The prefix which should be used for each line in the comment.
     */
    private static final String COMMENT_PREFIX =
            "# ";                                                           // NOI18N
}

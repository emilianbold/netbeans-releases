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
import java.io.IOException;

/**
 * The interface that all file handlers, i.e. classes handling comment correction 
 * for concrete file types, should implement. 
 * 
 * <p>
 * It requires the implementing classes to provide functionality of loading/saving a 
 * file, extracting the current initial comment, iserting a new one and updating 
 * (replacing) the current.
 * 
 * @author Kirill Sorokin
 */
public interface FileHandler {
    /**
     * Checks whether the given ile can be processed by this file handler.
     * 
     * @param file The file for which to run the compatibility check.
     * @return <code>true</code> if the current file handler is capable of handling 
     *      the file, <code>false</code> otherwise.
     */
    boolean accept(final File file);
    
    /**
     * Loads the file into file handler's cache.
     * 
     * @param file The file to load.
     * @throws java.io.IOException if an I/O error occurs.
     * @throws java.lang.IllegalArgumentException if the parameter validation fails.
     */
    void load(final File file) throws IOException;
    
    /**
     * Saves the cached file contents to the given file on disk.
     * 
     * @param file The file to which the cache should be saved.
     * @throws java.io.IOException if an I/O error occurs.
     * @throws java.lang.IllegalArgumentException if the parameter validation fails.
     * @throws java.lang.IllegalStateException if the file contents cache 
     *      is <code>null</code>.
     */
    void save(final File file) throws IOException;
    
    /**
     * Extracts the current initial comment from the cached file contents.
     * 
     * @return Teh current initial comment or <code>null</code> if the initial 
     *      comment does not exist.
     * @throws java.lang.IllegalStateException if the file handler does not have 
     *      anything loaded.
     */
    String getComment();
    
    /**
     * Inserts the initial comment to the cached file contents. If an intiial 
     * comment already exists in the file it is prepended by the new one.
     * 
     * @param text The text of the new initial comment.
     * @param lineLength The desired line length for the comment.
     * @throws java.lang.IllegalArgumentException if the parameters validation 
     *      fails.
     * @throws java.lang.IllegalStateException if the file handler does not have 
     *      anything loaded.
     */
    void insertComment(final String text, final int lineLength);
    
    /**
     * Updates the current initial comment in the cached file contents. If there is
     * no initia comment, then this method falls back to 
     * {@link #insertComment(String, int)}.
     * 
     * @param text The text of the new initial comment.
     * @param lineLength The desired line length for the comment.
     * @throws java.lang.IllegalArgumentException if the parameters validation 
     *      fails.
     * @throws java.lang.IllegalStateException if the file handler does not have 
     *      anything loaded.
     */
    void updateComment(final String text, final int lineLength);
}

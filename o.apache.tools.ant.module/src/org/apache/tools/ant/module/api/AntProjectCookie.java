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

package org.apache.tools.ant.module.api;

import javax.swing.event.ChangeListener;
import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;

/**
 * Cookie containing the state of an Ant Project.
 * Note that there is a document, and also a parse exception.
 * At least one must be non-null; it is possible for both to be
 * non-null in case there was a valid parse before, and some changes
 * are now invalid.
 */
public interface AntProjectCookie extends Node.Cookie {
    /** Get the disk file for the build script.
     * @return the disk file, or null if none (but must be a file object)
     */
    File getFile ();
    /** Get the file object for the build script.
     * @return the file object, or null if none (but must be a disk file)
     */
    FileObject getFileObject ();
    /** Get the DOM document for the build script.
     * @return the document, or null if it could not be parsed
     */
    Document getDocument ();
    /** Get the DOM root element (<code>&lt;project&gt;</code>) for the build script.
     * @return the root element, or null if it could not be parsed
     */
    Element getProjectElement ();
    /** Get the last parse-related exception, if there was one.
     * @return the parse exception, or null if it is valid
     */
    Throwable getParseException ();
    /** Add a listener to changes in the document.
     * @param l the listener to add
     */
    void addChangeListener (ChangeListener l);
    /** Remove a listener to changes in the document.
     * @param l the listener to remove
     */
    void removeChangeListener (ChangeListener l);
    
    /** Extended cookie permitting queries of parse status.
     * If only the basic cookie is available, you cannot
     * determine if a project is already parsed or not, and
     * methods which require it to be parsed for them to return
     * may block until a parse is complete.
     * @since 2.10
     */
    interface ParseStatus extends AntProjectCookie {
        /** Check whether the project is currently parsed.
         * Note that "parsed in error" is still considered parsed.
         * <p>If not parsed, then if and when it does later become
         * parsed, a change event should be fired. A project
         * might become unparsed after being parsed, due to e.g.
         * garbage collection; this need not fire any event.
         * <p>If the project is currently parsed, the methods
         * {@link AntProjectCookie#getDocument},
         * {@link AntProjectCookie#getProjectElement}, and
         * {@link AntProjectCookie#getParseException} should
         * not block.
         * @return true if this project is currently parsed
         */
        boolean isParsed();
    }
    
}

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

package org.netbeans.lib.editor.util.swing;

/**
* Priorities of firing of document listeners being added to a document.
*
* @author Miloslav Metelka
* @since 1.4
*/

public final class DocumentListenerPriority {

    /**
     * Fold update gets notified first (prior view updates etc.).
     */
    public static final DocumentListenerPriority FOLD_UPDATE
            = new DocumentListenerPriority(3, "fold-update"); // NOI18N

    /**
     * Default level is used for all listeners added
     * by regular {@link javax.swing.text.Document#addDocumentListener(
     * javax.swing.event.DocumentListener)} method.
     */
    public static final DocumentListenerPriority DEFAULT
            = new DocumentListenerPriority(2, "default"); // NOI18N

    /**
     * Caret udpate gets notified as last.
     */
    public static final DocumentListenerPriority CARET_UPDATE
            = new DocumentListenerPriority(1, "caret-update"); // NOI18N

    /**
     * Udpate that follows caret update.
     * @since 1.6
     */
    public static final DocumentListenerPriority AFTER_CARET_UPDATE
            = new DocumentListenerPriority(0, "after-caret-update"); // NOI18N

    
    private int priority;
    
    private String description;

    /**
     * Construct new DocumentListenerPriority.
     *
     * @param priority higher priority means sooner firing.
     * @param description textual description of the priority.
     */
    private DocumentListenerPriority(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public String getDescription() {
        return description;
    }

    public String toString() {
        return getDescription();
    }

}

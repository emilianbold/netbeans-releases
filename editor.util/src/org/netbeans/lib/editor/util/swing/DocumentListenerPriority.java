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
     * Lexer gets notified early to allow other levels to use the udpated
     * token list.
     */
    public static final DocumentListenerPriority LEXER
            = new DocumentListenerPriority(5, "lexer"); // NOI18N

    /**
     * Fold update gets notified prior default level.
     */
    public static final DocumentListenerPriority FOLD_UPDATE
            = new DocumentListenerPriority(4, "fold-update"); // NOI18N

    /**
     * Default level is used for all listeners added
     * by regular {@link javax.swing.text.Document#addDocumentListener(
     * javax.swing.event.DocumentListener)} method.
     */
    public static final DocumentListenerPriority DEFAULT
            = new DocumentListenerPriority(3, "default"); // NOI18N

    /**
     * Views are updated after default level and prior caret gets updated.
     * @since 1.11
     */
    public static final DocumentListenerPriority VIEW
            = new DocumentListenerPriority(2, "view"); // NOI18N

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
    
    public String getDescription() {
        return description;
    }

    public String toString() {
        return getDescription();
    }

    /**
     * Get the integer priority for the purpose of adding/removing
     * of the listener with this priority.
     */
    int getPriority() {
        return priority;
    }

}

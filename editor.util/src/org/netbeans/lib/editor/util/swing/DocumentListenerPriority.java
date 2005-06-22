/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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

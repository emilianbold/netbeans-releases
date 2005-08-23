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

package org.netbeans.lib.editor.util.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * Various utility methods related to swing text documents.
 *
 * @author Miloslav Metelka
 * @since 1.4
 */

public final class DocumentUtilities {
    
    private static final Object TYPING_MODIFICATION_DOCUMENT_PROPERTY = new Object();
    
    private DocumentUtilities() {
        // No instances
    }

    /**
     * Add document listener to document with given priority.
     * 
     * @param doc document to which the listener should be added.
     * @param listener document listener to add.
     * @param priority priority with which the listener should be added.
     *  If the document does not support document listeners ordering
     *  then the listener is added in a regular way by using
     *  {@link javax.swing.text.Document#addDocumentListener(
     *  javax.swing.event.DocumentListener)} method.
     */
    public static void addDocumentListener(Document doc, DocumentListener listener,
    DocumentListenerPriority priority) {
        
        PriorityDocumentListenerList priorityDocumentListenerList
                = (PriorityDocumentListenerList)doc.getProperty(PriorityDocumentListenerList.class);
        if (priorityDocumentListenerList != null) {
            priorityDocumentListenerList.add(listener, priority.getPriority());
        } else { // default to regular adding
            doc.addDocumentListener(listener);
        }
    }

    /**
     * Remove document listener that was previously added to the document
     * with given priority.
     * 
     * @param doc document from which the listener should be removed.
     * @param listener document listener to remove.
     * @param priority priority with which the listener should be removed.
     *  It should correspond to the priority with which the listener
     *  was added originally.
     */
    public static void removeDocumentListener(Document doc, DocumentListener listener,
    DocumentListenerPriority priority) {
        
        PriorityDocumentListenerList priorityDocumentListenerList
                = (PriorityDocumentListenerList)doc.getProperty(PriorityDocumentListenerList.class);
        if (priorityDocumentListenerList != null) {
            priorityDocumentListenerList.remove(listener, priority.getPriority());
        } else { // default to regular removing
            doc.removeDocumentListener(listener);
        }
    }

    /**
     * This method should be used by swing document implementations that
     * want to support document listeners prioritization.
     * <br>
     * It should be called from document's constructor in the following way:<pre>
     *
     * class MyDocument extends AbstractDocument {
     *
     *     MyDocument() {
     *         super.addDocumentListener(DocumentUtilities.initPriorityListening(this));
     *     }
     *
     *     public void addDocumentListener(DocumentListener listener) {
     *         DocumentUtilities.addDocumentListener(this, listener, DocumentListenerPriority.DEFAULT);
     *     }
     *
     *     public void removeDocumentListener(DocumentListener listener) {
     *         DocumentUtilities.removeDocumentListener(this, listener, DocumentListenerPriority.DEFAULT);
     *     }
     *
     * }</pre>
     *
     *
     * @param doc document to be initialized.
     * @return the document listener instance that should be added as a document
     *   listener typically by using <code>super.addDocumentListener()</code>
     *   in document's constructor.
     * @throws IllegalStateException when the document already has
     *   the property initialized.
     */
    public static DocumentListener initPriorityListening(Document doc) {
        if (doc.getProperty(PriorityDocumentListenerList.class) != null) {
            throw new IllegalStateException(
                    "PriorityDocumentListenerList already initialized for doc=" + doc); // NOI18N
        }
        PriorityDocumentListenerList instance = new PriorityDocumentListenerList();
        doc.putProperty(PriorityDocumentListenerList.class, instance);
        return instance;
    }
    
    /**
     * Mark that the ongoing document modification(s) will be caused
     * by user's typing.
     * It should be used by default-key-typed-action and the actions
     * for backspace and delete keys.
     * <br/>
     * The document listeners being fired may
     * query it by using {@link #isTypingModification(Document)}.
     * This method should always be used in the following pattern:
     * <pre>
     * DocumentUtilities.setTypingModification(doc, true);
     * try {
     *     doc.insertString(offset, typedText, null);
     * } finally {
     *    DocumentUtilities.setTypingModification(doc, false);
     * }
     * </pre>
     *
     * @see #isTypingModification(Document)
     */
    public static void setTypingModification(Document doc, boolean typingModification) {
        doc.putProperty(TYPING_MODIFICATION_DOCUMENT_PROPERTY, Boolean.valueOf(typingModification));
    }
    
    /**
     * This method should be used by document listeners to check whether
     * the just performed document modification was caused by user's typing.
     * <br/>
     * Certain functionality such as code completion or code templates
     * may benefit from that information. For example the java code completion
     * should only react to the typed "." but not if the same string was e.g.
     * pasted from the clipboard.
     *
     * @see #setTypingModification(Document, boolean)
     */
    public static boolean isTypingModification(DocumentEvent evt) {
        Boolean b = (Boolean)evt.getDocument().getProperty(TYPING_MODIFICATION_DOCUMENT_PROPERTY);
        return (b != null) ? b.booleanValue() : false;
    }

}

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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import org.netbeans.lib.editor.util.AbstractCharSequence;

/**
 * Various utility methods related to swing text documents.
 *
 * @author Miloslav Metelka
 * @since 1.4
 */

public final class DocumentUtilities {
    
    private static final Object TYPING_MODIFICATION_DOCUMENT_PROPERTY = new Object();
    
    /**
     * Instance of an element that can be used to obtain the text removed/inserted
     * by a document modification.
     * <br>
     * The text is obtained by doing
     * <code>DocumentEvent.getChange(MODIFICATION_TEXT_ELEMENT).toString()</code>.
     * <br>
     * The documents that want to support this need to insert the element change
     * for the given element into the created document event.
     */
    public static final Element MODIFICATION_TEXT_ELEMENT = ModificationTextElement.INSTANCE;
    
    
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
     * @deprecated
     * @see #isTypingModification(Document)
     */
    public static boolean isTypingModification(DocumentEvent evt) {
        Boolean b = (Boolean)evt.getDocument().getProperty(TYPING_MODIFICATION_DOCUMENT_PROPERTY);
        return (b != null) ? b.booleanValue() : false;
    }

    /**
     * This method should be used to check whether
     * the lastly performed document modification was caused by user's typing.
     * <br/>
     * Certain functionality such as code completion or code templates
     * may benefit from that information. For example the java code completion
     * should only react to the typed "." but not if the same string was e.g.
     * pasted from the clipboard.
     *
     * @see #setTypingModification(Document, boolean)
     */
    public static boolean isTypingModification(Document doc) {
        Boolean b = (Boolean)doc.getProperty(TYPING_MODIFICATION_DOCUMENT_PROPERTY);
        return (b != null) ? b.booleanValue() : false;
    }

    /**
     * Get text of the given document as char sequence.
     * <br>
     *
     * @param doc document for which the charsequence is being obtained.
     * @return non-null character sequence.
     *  <br>
     *  The returned character sequence should only be accessed under
     *  document's readlock (or writelock).
     */
    public static CharSequence getText(Document doc) {
        CharSequence text = (CharSequence)doc.getProperty(CharSequence.class);
        if (text == null) {
            text = new DocumentCharSequence(doc);
            doc.putProperty(CharSequence.class, text);
        }
        return text;
    }
    
    /**
     * Get a portion of text of the given document as char sequence.
     * <br>
     *
     * @param doc document for which the charsequence is being obtained.
     * @param offset starting offset of the charsequence to obtain.
     * @param length length of the charsequence to obtain
     * @return non-null character sequence.
     * @exception BadLocationException  some portion of the given range
     *   was not a valid part of the document.  The location in the exception
     *   is the first bad position encountered.
     *  <br>
     *  The returned character sequence should only be accessed under
     *  document's readlock (or writelock).
     */
    public static CharSequence getText(Document doc, int offset, int length) throws BadLocationException {
        CharSequence text = (CharSequence)doc.getProperty(CharSequence.class);
        if (text == null) {
            text = new DocumentCharSequence(doc);
            doc.putProperty(CharSequence.class, text);
        }
        try {
            return text.subSequence(offset, offset + length);
        } catch (IndexOutOfBoundsException e) {
            int badOffset = offset;
            if (offset >= 0 && offset + length > text.length()) {
                badOffset = length;
            }
            throw new BadLocationException(e.getMessage(), badOffset);
        }
    }
    
    /**
     * Get text of the given document modification.
     *
     * @param evt document event describing either document insertion or removal
     *  (change event type events will produce null result).
     * @return text that was inserted/removed from the document by the given
     *  document modification or null if that information is not provided
     *  by that document event.
     */
    public static String getModificationText(DocumentEvent evt) {
        DocumentEvent.ElementChange change = evt.getChange(MODIFICATION_TEXT_ELEMENT);
        return (change != null) ? change.toString() : null;
    }

    /**
     * Implementation of the character sequence for a generic document
     * that does not provide its own implementation of character sequence.
     */
    private static final class DocumentCharSequence extends AbstractCharSequence.StringLike {
        
        private final Segment segment = new Segment();
        
        private final Document doc;
        
        DocumentCharSequence(Document doc) {
            this.doc = doc;
        }

        public int length() {
            return doc.getLength();
        }

        public synchronized char charAt(int index) {
            try {
                doc.getText(index, 1, segment);
            } catch (BadLocationException e) {
                throw new IndexOutOfBoundsException(e.getMessage()
                    + " at offset=" + e.offsetRequested()); // NOI18N
            }
            return segment.array[segment.offset];
        }

    }
    
    /**
     * Helper element to used for notification about removed/inserted text
     * into the document.
     */
    private static final class ModificationTextElement implements Element {
        
        static final ModificationTextElement INSTANCE = new ModificationTextElement();
        
        public int getStartOffset() {
            return 0;
        }

        public int getEndOffset() {
            return 0;
        }

        public int getElementCount() {
            return 0;
        }

        public int getElementIndex(int offset) {
            return -1;
        }

        public Element getElement(int index) {
            return null;
        }

        public boolean isLeaf() {
            return true;
        }

        public Element getParentElement() {
            return null;
        }

        public String getName() {
            return "Helper element for modification text providing"; // NOI18N
        }

        public Document getDocument() {
            return null;
        }

        public javax.swing.text.AttributeSet getAttributes() {
            return null;
        }
        
        public String toString() {
            return getName();
        }

    }
    
}

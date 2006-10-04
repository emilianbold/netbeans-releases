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

import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.lib.editor.util.AbstractCharSequence;
import org.netbeans.lib.editor.util.CompactMap;

/**
 * Various utility methods related to swing text documents.
 *
 * @author Miloslav Metelka
 * @since 1.4
 */

public final class DocumentUtilities {
    
    private static final Object TYPING_MODIFICATION_DOCUMENT_PROPERTY = new Object();
    
    private static final Object TYPING_MODIFICATION_KEY = new Object();
    
    
    private DocumentUtilities() {
        // No instances
    }

    /**
     * Add document listener to document with given priority
     * or default to using regular {@link Document#addDocumentListener(DocumentListener)}
     * if the given document is not listener priority aware.
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
        if (!addPriorityDocumentListener(doc, listener, priority))
            doc.addDocumentListener(listener);
    }
    
    /**
     * Suitable for document implementations - adds document listener
     * to document with given priority and does not do anything
     * if the given document is not listener priority aware.
     * <br/>
     * Using this method in the document impls and defaulting
     * to super.addDocumentListener() in case it returns false
     * will ensure that there won't be an infinite loop in case the super constructors
     * would add some listeners prior initing of the priority listening.
     * 
     * @param doc document to which the listener should be added.
     * @param listener document listener to add.
     * @param priority priority with which the listener should be added.
     * @return true if the priority listener was added or false if the document
     *  does not support priority listening.
     */
    public static boolean addPriorityDocumentListener(Document doc, DocumentListener listener,
    DocumentListenerPriority priority) {
        PriorityDocumentListenerList priorityDocumentListenerList
                = (PriorityDocumentListenerList)doc.getProperty(PriorityDocumentListenerList.class);
        if (priorityDocumentListenerList != null) {
            priorityDocumentListenerList.add(listener, priority.getPriority());
            return true;
        } else
            return false;
    }

    /**
     * Remove document listener that was previously added to the document
     * with given priority or use default {@link Document#removeDocumentListener(DocumentListener)}
     * if the given document is not listener priority aware.
     * 
     * @param doc document from which the listener should be removed.
     * @param listener document listener to remove.
     * @param priority priority with which the listener should be removed.
     *  It should correspond to the priority with which the listener
     *  was added originally.
     */
    public static void removeDocumentListener(Document doc, DocumentListener listener,
    DocumentListenerPriority priority) {
        if (!removePriorityDocumentListener(doc, listener, priority))
            doc.removeDocumentListener(listener);
    }

    /**
     * Suitable for document implementations - removes document listener
     * from document with given priority and does not do anything
     * if the given document is not listener priority aware.
     * <br/>
     * Using this method in the document impls and defaulting
     * to super.removeDocumentListener() in case it returns false
     * will ensure that there won't be an infinite loop in case the super constructors
     * would remove some listeners prior initing of the priority listening.
     * 
     * @param doc document from which the listener should be removed.
     * @param listener document listener to remove.
     * @param priority priority with which the listener should be removed.
     * @return true if the priority listener was removed or false if the document
     *  does not support priority listening.
     */
    public static boolean removePriorityDocumentListener(Document doc, DocumentListener listener,
    DocumentListenerPriority priority) {
        PriorityDocumentListenerList priorityDocumentListenerList
                = (PriorityDocumentListenerList)doc.getProperty(PriorityDocumentListenerList.class);
        if (priorityDocumentListenerList != null) {
            priorityDocumentListenerList.remove(listener, priority.getPriority());
            return true;
        } else
            return false;
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
     *         if (!DocumentUtilities.addDocumentListener(this, listener, DocumentListenerPriority.DEFAULT))
     *             super.addDocumentListener(listener);
     *     }
     *
     *     public void removeDocumentListener(DocumentListener listener) {
     *         if (!DocumentUtilities.removeDocumentListener(this, listener, DocumentListenerPriority.DEFAULT))
     *             super.removeDocumentListener(listener);
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
        PriorityDocumentListenerList listener = new PriorityDocumentListenerList();
        doc.putProperty(PriorityDocumentListenerList.class, listener);
        return listener;
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
    public static boolean isTypingModification(Document doc) {
        Boolean b = (Boolean)doc.getProperty(TYPING_MODIFICATION_DOCUMENT_PROPERTY);
        return (b != null) ? b.booleanValue() : false;
    }

    /**
     * @deprecated
     * @see #isTypingModification(Document)
     */
    public static boolean isTypingModification(DocumentEvent evt) {
        return isTypingModification(evt.getDocument());
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
     * Document provider should call this method to allow for document event
     * properties being stored in document events.
     *
     * @param evt document event to which the storage should be added.
     *   It must be an undoable edit allowing to add an edit.
     */
    public static void addEventPropertyStorage(DocumentEvent evt) {
        // Parameter is DocumentEvent because it's more logical
        if (!(evt instanceof UndoableEdit)) {
            throw new IllegalStateException("evt not instanceof UndoableEdit: " + evt); // NOI18N
        }
        ((UndoableEdit)evt).addEdit(new EventPropertiesElementChange());
    }
    
    /**
     * Get a property of a given document event.
     *
     * @param evt non-null document event from which the property should be retrieved.
     * @param key non-null key of the property.
     * @return value for the given property.
     */
    public static Object getEventProperty(DocumentEvent evt, Object key) {
        EventPropertiesElementChange change = (EventPropertiesElementChange)
                evt.getChange(EventPropertiesElement.INSTANCE);
        return (change != null) ? change.getProperty(key) : null;
    }
    
    /**
     * Set a property of a given document event.
     *
     * @param evt non-null document event to which the property should be stored.
     * @param key non-null key of the property.
     * @param value for the given property.
     */
    public static void putEventProperty(DocumentEvent evt, Object key, Object value) {
        EventPropertiesElementChange change = (EventPropertiesElementChange)
                evt.getChange(EventPropertiesElement.INSTANCE);
        if (change == null) {
            throw new IllegalStateException("addEventPropertyStorage() not called for evt=" + evt); // NOI18N
        }
        change.putProperty(key, value);
    }
    
    /**
     * Set a property of a given document event by using the given map entry.
     * <br/>
     * The present implementation is able to directly store instances
     * of <code>CompactMap.MapEntry</code>. Other map entry implementations
     * will be delegated to {@link #putEventProperty(DocumentEvent, Object, Object)}.
     *
     * @param evt non-null document event to which the property should be stored.
     * @param mapEntry non-null map entry which should be stored.
     *  Generally after this method finishes the {@link #getEventProperty(DocumentEvent, Object)}
     *  will return <code>mapEntry.getValue()</code> for <code>mapEntry.getKey()</code> key.
     */
    public static void putEventProperty(DocumentEvent evt, Map.Entry mapEntry) {
        if (mapEntry instanceof CompactMap.MapEntry) {
            EventPropertiesElementChange change = (EventPropertiesElementChange)
                    evt.getChange(EventPropertiesElement.INSTANCE);
            if (change == null) {
                throw new IllegalStateException("addEventPropertyStorage() not called for evt=" + evt); // NOI18N
            }
            change.putEntry((CompactMap.MapEntry)mapEntry);

        } else {
            putEventProperty(evt, mapEntry.getKey(), mapEntry.getValue());
        }
    }
    
    /**
     * Fix the given offset according to the performed modification.
     * 
     * @param offset >=0 offset in a document.
     * @param evt document event describing change in the document.
     * @return offset updated by applying the document change to the offset.
     */
    public static int fixOffset(int offset, DocumentEvent evt) {
        int modOffset = evt.getOffset();
        if (evt.getType() == DocumentEvent.EventType.INSERT) {
            if (offset >= modOffset) {
                offset += evt.getLength();
            }
        } else if (evt.getType() == DocumentEvent.EventType.REMOVE) {
            if (offset > modOffset) {
                offset = Math.min(offset - evt.getLength(), modOffset);
            }
        }
        return offset;
    }
    
    /**
     * Get text of the given document modification.
     * <br/>
     * It's implemented as retrieving of a <code>String.class</code>.
     *
     * @param evt document event describing either document insertion or removal
     *  (change event type events will produce null result).
     * @return text that was inserted/removed from the document by the given
     *  document modification or null if that information is not provided
     *  by that document event.
     */
    public static String getModificationText(DocumentEvent evt) {
        return (String)getEventProperty(evt, String.class);
    }
    
    /**
     * Get the paragraph element for the given document.
     *
     * @param doc non-null document instance.
     * @param offset offset in the document >=0
     * @return paragraph element containing the given offset.
     */
    public static Element getParagraphElement(Document doc, int offset) {
        Element paragraph;
        if (doc instanceof StyledDocument) {
            paragraph = ((StyledDocument)doc).getParagraphElement(offset);
        } else {
            Element rootElem = doc.getDefaultRootElement();
            int index = rootElem.getElementIndex(offset);
            paragraph = rootElem.getElement(index);
            if ((offset < paragraph.getStartOffset()) || (offset >= paragraph.getEndOffset())) {
                paragraph = null;
            }
        }
        return paragraph;
    }
    
    /**
     * Get the root of the paragraph elements for the given document.
     *
     * @param doc non-null document instance.
     * @return root element of the paragraph elements.
     */
    public static Element getParagraphRootElement(Document doc) {
        if (doc instanceof StyledDocument) {
            return ((StyledDocument)doc).getParagraphElement(0).getParentElement();
        } else {
            return doc.getDefaultRootElement().getElement(0).getParentElement();
        }
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
     * Helper element used as a key in searching for an element change
     * being a storage of the additional properties in a document event.
     */
    private static final class EventPropertiesElement implements Element {
        
        static final EventPropertiesElement INSTANCE = new EventPropertiesElement();
        
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
    
    private static final class EventPropertiesElementChange
    implements DocumentEvent.ElementChange, UndoableEdit  {
        
        private CompactMap eventProperties = new CompactMap();
        
        public synchronized Object getProperty(Object key) {
            return (eventProperties != null) ? eventProperties.get(key) : null;
        }

        @SuppressWarnings("unchecked")
        public synchronized Object putProperty(Object key, Object value) {
            return eventProperties.put(key, value);
        }

        @SuppressWarnings("unchecked")
        public synchronized CompactMap.MapEntry putEntry(CompactMap.MapEntry entry) {
            return eventProperties.putEntry(entry);
        }

        public int getIndex() {
            return -1;
        }

        public Element getElement() {
            return EventPropertiesElement.INSTANCE;
        }

        public Element[] getChildrenRemoved() {
            return null;
        }

        public Element[] getChildrenAdded() {
            return null;
        }

        public boolean replaceEdit(UndoableEdit anEdit) {
            return false;
        }

        public boolean addEdit(UndoableEdit anEdit) {
            return false;
        }

        public void undo() throws CannotUndoException {
            // do nothing
        }

        public void redo() throws CannotRedoException {
            // do nothing
        }

        public boolean isSignificant() {
            return false;
        }

        public String getUndoPresentationName() {
            return "";
        }

        public String getRedoPresentationName() {
            return "";
        }

        public String getPresentationName() {
            return "";
        }

        public void die() {
            // do nothing
        }

        public boolean canUndo() {
            return true;
        }

        public boolean canRedo() {
            return true;
        }

    }
    
}

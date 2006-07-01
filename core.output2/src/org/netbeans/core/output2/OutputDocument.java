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
/*
 * OutputDocument.java
 *
 * Created on March 20, 2004, 8:35 PM
 */

package org.netbeans.core.output2;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Mutex;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openide.util.Exceptions;


/** An implementation of Document directly over a memory mapped file such that
 * no (or nearly no) memory copies are required to fetch data to display.
 *
 * @author  Tim Boudreau, Jesse Glick
 */
class OutputDocument implements Document, Element, ChangeListener, ActionListener, Runnable {
    private List dlisteners = new ArrayList();
    private volatile Timer timer = null;

    private OutWriter writer;
   
    /** Creates a new instance of OutputDocument */
    OutputDocument(OutWriter writer) {
        if (Controller.log) {
            Controller.log ("Creating a Document for " + writer);
        }
        this.writer = writer;
        getLines().addChangeListener(this);
    }

    /**
     * Destroy this OutputDocument and its backing storage.  The document should not be visible
     * in the UI when this method is called.
     */
    public void dispose() {
        if (Controller.log) Controller.log ("Disposing document and backing storage for " + getLines().readLock());
        disposeQuietly();
        writer.dispose();
        writer = null;
    }

    /**
     * Destory this OutputDocument, but not its backing storage.  The document should not be
     * visible in the UI when this method is called.
     */
    public void disposeQuietly() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        dlisteners.clear();
        lastEvent = null;
        getLines().removeChangeListener(this);
    }

    public synchronized void addDocumentListener(DocumentListener documentListener) {
        dlisteners.add (documentListener);
        lastEvent = null;
    }
    
    public void addUndoableEditListener(UndoableEditListener l) {
        //do nothing
    }
    
    public Position createPosition(int offset) throws BadLocationException {
        if (offset < 0 || offset > getLines().getCharCount()) {
            throw new BadLocationException ("Bad position", offset); //NOI18N
        }
        return new ODPosition (offset);
    }
    
    public Element getDefaultRootElement() {
        return this;
    }
    
    public Position getEndPosition() {
        return new ODEndPosition();
    }
    
    public int getLength() {
        return getLines().getCharCount();
    }
    
    public Object getProperty(Object obj) {
        return null;
    }

    public Element[] getRootElements() {
        return new Element[] {this};
    }
    
    public Position getStartPosition() {
        return new ODStartPosition();
    }
    
    public String getText(int offset, int length) throws BadLocationException {
        if (offset < 0 || offset > getLines().getCharCount() || length < 0) {
            throw new BadLocationException ("Bad: " + offset + "," +  //NOI18N
                length, offset);
        }
        if (length == 0) {
            return ""; //NOI18N
        }
        String result1;
        synchronized (getLines().readLock()) {
            result1 = getLines().getText(offset,offset + length);
        }
        String result = result1;
        return result;
    }
    
    private char[] reusableSubrange = new char [256];
    public void getText(int offset, int length, Segment txt) throws BadLocationException {
        if (length < 0) {
            //document is empty
            txt.array = new char[0];
            txt.offset=0;
            txt.count = 0;
            return;
        }
        
        if (offset < 0) {
            throw new BadLocationException ("Negative offset", offset); //NOI18N
        }
        if (getLines().getLineCount() == 0) {
            txt.array = new char[] {'\n'};
            txt.offset = 0;
            txt.count = 1;
            return;
        }
        if (length > reusableSubrange.length) {
            reusableSubrange = new char[length];
        }
        try {
            char[] chars = getLines().getText(offset, offset + length, reusableSubrange);
            txt.array = chars;
            txt.offset = 0;
            txt.count = Math.min(length, chars.length);
        } catch (OutOfMemoryError error) {
            //#50189 - try to salvage what we can
            OutWriter.lowDiskSpace = true;
            //Sets the error flag and releases the storage
            writer.dispose();
            Logger.getAnonymousLogger().log(Level.WARNING,
                "OOME while reading output.  Cleaning up.", //NOI18N
            error);
        }
    }
    
    public void insertString(int param, String str, AttributeSet attributeSet) throws BadLocationException {
        throw new UnsupportedOperationException();
    }
    
    public void putProperty(Object obj, Object obj1) {
        //do nothing
    }
    
    public void remove(int param, int param1) throws BadLocationException {
        throw new UnsupportedOperationException ("Read only buffer"); //NOI18N
    }
    
    public synchronized void removeDocumentListener(DocumentListener documentListener) {
        dlisteners.remove(documentListener);
        lastEvent = null;
        if (dlisteners.isEmpty() && timer != null) {
            timer.stop();
            timer = null;
        }
    }

    public Lines getLines() {
        //Unit test will check for null to determine if dispose succeeded
        return writer != null ? writer.getLines() : null;
    }

    public int getLineStart (int line) {
        return getLines().getLineCount() > 0 ? getLines().getLineStart(line) : 0;
    }

    public int getLineEnd (int lineIndex) {
        if (getLines().getLineCount() == 0) {
            return 0;
        }
        int endOffset;
        if (lineIndex >= getLines().getLineCount()-1) {
            endOffset = getLines().getCharCount();
        } else {
            endOffset = getLines().getLineStart(lineIndex+1);
        }
        return endOffset;
    }

    public void removeUndoableEditListener(UndoableEditListener undoableEditListener) {
        //do nothing
    }
    
    public void render(Runnable runnable) {
        getElementCount(); //Force a refresh of lastPostedLine
        runnable.run();
    }
    
    public AttributeSet getAttributes() {
        return SimpleAttributeSet.EMPTY;
    }
    
    public Document getDocument() {
        return this;
    }
    
    public Element getElement(int index) {
        //Thanks to Mila Metelka for pointing out that Swing documents always
        //are expected to have a trailing empty element
        if (getLines().getLineCount() == 0) {
            return new EmptyElement(OutputDocument.this);
        }
        synchronized (getLines().readLock()) {
            if (index > lastPostedLine) {
                lastPostedLine = index;
            }
        }
        return new ODElement (index);
    }
    
    public int getElementCount() {
        int result;
        synchronized (getLines().readLock()) {
            result = getLines().getLineCount();
            lastPostedLine = result;
        }
        if (result == 0) {
            result = 1;
        }
        return result;
    }
    
    public int getElementIndex(int offset) {
        return getLines().getLineAt(offset);
    }
    
    public int getEndOffset() {
        return getLength();
    }
    
    public String getName() {
        return "foo"; //XXX
    }
    
    public Element getParentElement() {
        return null;
    }
    
    public int getStartOffset() {
        return 0;
    }
    
    public boolean isLeaf() {
        return getLines().getLineCount() == 0;
    }

    private volatile DO lastEvent = null;
    private int lastPostedLine = -1;
    private int lastFiredLine = -1;
    public void stateChanged(ChangeEvent changeEvent) {
        if (Controller.verbose) Controller.log (changeEvent != null ? "Document got change event from writer" : "Document timer polling");
        if (dlisteners.isEmpty()) {
            if (Controller.verbose) Controller.log ("listeners empty, not firing");
            return;
        }
        if (getLines().checkDirty(true)) {
            if (lastEvent != null && !lastEvent.isConsumed()) {
                if (Controller.verbose) Controller.log ("Last event not consumed, not firing");
                return;
            }
            boolean noPostedLine = lastPostedLine == - 1;

            int lineCount = getLines().getLineCount();
            lastPostedLine = lineCount;
            
            if (Controller.verbose) Controller.log ("Document may fire event - last fired getLine=" + lastFiredLine + " getLine count now " + lineCount);
            if (lastFiredLine != lastPostedLine || noPostedLine) {
                lastEvent = new DO(Math.max(0, lastFiredLine), noPostedLine);
//                evts.add (lastEvent);
                Mutex.EVENT.readAccess (new Runnable() {
                    public void run() {
                        if (Controller.verbose) Controller.log("Firing document event on EQ with start index " + lastEvent.start);
                        fireDocumentEvent (lastEvent);
                    }
                });
                lastFiredLine = lastPostedLine;
            } else {
                if (Controller.verbose) Controller.log ("Line count is still " + lineCount + " - not firing");
            }
        } else {
            if (Controller.verbose) Controller.log ("Writer says it is not dirty, firing no change");
        }
        updateTimerState();
    }    
    
    private boolean updatingTimerState = false;
    private synchronized void updateTimerState() {
        if (updatingTimerState) {
            return;
        }
        updatingTimerState = true;
        long newTime = System.currentTimeMillis();
        if (timer == null && getLines().isGrowing()) {
            if (Controller.log) Controller.log("Starting timer");
            //Run the timer fast and furious at first, slowing down after
            //the initial output has been captured
            timer = new javax.swing.Timer(50, this);
            timer.setRepeats(true);
            timer.start();
        } else if (!getLines().isGrowing()) {
            if (timer != null) {
                timer.stop();
            }
            if (getLines().checkDirty(false) && timer != null) {
                //There's still some output we haven't displayed - 
                //fire a change one last time.
                Mutex.EVENT.readAccess(this);
            }
//            logInfo();
            timer = null;
        } else if (lastFireTime != 0 && timer != null) {
            if (newTime - lastFireTime > 15000) {
                //Probably we're done, but someone forgot to close the stream.
                //Slow down the timer to a dull roar.
                timer.setDelay (10000);
            }
        }
        if (timer != null && timer.getDelay() < 350) {
            timer.setDelay (timer.getDelay() + 20);
            if (Controller.verbose) Controller.log ("Decreased timer interval to " + timer.getDelay());
        }
        lastFireTime = newTime;
        updatingTimerState = false;
    }
    
    public void run() {
        stateChanged(null);
    }

    private long lastFireTime = 0;
    public void actionPerformed(ActionEvent actionEvent) {
        if (!getLines().isGrowing()) {
            updateTimerState();
        }
        stateChanged(null);
    }    
    
    private void fireDocumentEvent (DocumentEvent de) {
        Iterator i = new ArrayList(dlisteners).iterator();
        while (i.hasNext()) {
            DocumentListener dl = (DocumentListener) i.next();
            dl.insertUpdate(de);
        }
    }

    static final class ODPosition implements Position {
        private int offset;
        
        ODPosition (int offset) {
            this.offset = offset;
        }
        
        public int getOffset() {
            return offset;
        }
        
        public int hashCode() {
            return offset * 11;
        }
        
        public boolean equals (Object o) {
            return (o instanceof ODPosition) && 
                ((ODPosition) o).getOffset() == offset;
        }
    }
    
    final class ODEndPosition implements Position {
        public int getOffset() {
            int result = getLines().getCharCount();
            return result;
        }
        
        private Document doc() {
            return OutputDocument.this;
        }
        
        public boolean equals (Object o) {
            return (o instanceof ODEndPosition) && ((ODEndPosition) o).doc() == 
                doc();
        }
        
        public int hashCode() {
            return -2390481;
        }
    }
    
    final class ODStartPosition implements Position {
        public int getOffset() {
            return 0;
        }
        
        private Document doc() {
            return OutputDocument.this;
        }
        
        public boolean equals (Object o) {
            return (o instanceof ODStartPosition) && ((ODStartPosition) o).doc() == 
                doc();
        }
        
        public int hashCode() {
            return 2190481;
        }
    }
    
    final class ODElement implements Element {
        private int lineIndex;
        private int startOffset = -1;
        private int endOffset = -1;
        ODElement (int lineIndex) {
            this.lineIndex = lineIndex;
        }
        
        public int hashCode() {
            return lineIndex;
        }
        
        public boolean equals (Object o) {
            return (o instanceof ODElement) && ((ODElement) o).lineIndex == lineIndex &&
                ((ODElement) o).getDocument() == getDocument();
        }
        
        public AttributeSet getAttributes() {
            return SimpleAttributeSet.EMPTY;
        }
        
        public Document getDocument() {
            return OutputDocument.this;
        }
        
        public Element getElement(int param) {
            return null;
        }
        
        public int getElementCount() {
            return 0;
        }
        
        public int getElementIndex(int param) {
            return -1;
        }
        
        public int getEndOffset() {
            calc();
            return endOffset;
        }
        
        public String getName() {
            return null;
        }
        
        public Element getParentElement() {
            return OutputDocument.this;
        }
        
        public int getStartOffset() {
            calc();
            return startOffset;
        }
        
        void calc() {
            synchronized (getLines().readLock()) {
                if (startOffset == -1) {
                    startOffset = getLines().getLineCount() > 0 ? getLines().getLineStart(lineIndex) : 0;
                    if (lineIndex >= getLines().getLineCount()-1) {
                        endOffset = getLines().getCharCount();
                    } else {
                        endOffset = getLines().getLineStart(lineIndex+1);
                    }
                    assert endOffset >= getStartOffset() : "Illogical getLine #" + lineIndex
                        + " with lines " + getLines() + " or writer has been reset";
                }
            }
        }
        
        public boolean isLeaf() {
            return true;
        }
        
        public String toString() {
            try {
                return OutputDocument.this.getText(getStartOffset(), getEndOffset() 
                    - getStartOffset());
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
                return "";
            }
        }
    }
    
    /**
     * Bug in javax.swing.text.PlainView - even if the element count is 0,
     * it tries to fetch the 0th element.
     */
    private static class EmptyElement implements Element {
        private final OutputDocument doc;

        EmptyElement (OutputDocument doc) {
            this.doc = doc;
        }

        public javax.swing.text.AttributeSet getAttributes() {
            return SimpleAttributeSet.EMPTY;
        }
        
        public javax.swing.text.Document getDocument() {
            return doc;
        }
        
        public javax.swing.text.Element getElement(int param) {
            return null;
        }
        
        public int getElementCount() {
            return 0;
        }
        
        public int getElementIndex(int param) {
            return 0;
        }
        
        public int getEndOffset() {
            return 0;
        }
        
        public String getName() {
            return "empty";
        }
        
        public javax.swing.text.Element getParentElement() {
            return doc;
        }
        
        public int getStartOffset() {
            return 0;
        }
        
        public boolean isLeaf() {
            return true;
        }
    }

    private class DO implements DocumentEvent, DocumentEvent.ElementChange {
        private int start;
        private int offset = -1;
        private int length = -1;
        private int lineCount = -1;
        private boolean consumed = false;
        private boolean initial = false;
        private int first = -1;
        DO (int start, boolean initial) {
            this.start = start;
            this.initial = initial;
            if (start < 0) {
                throw new IllegalArgumentException ("Illogical start: " + start);
            }
        }
        
        private void calc() {
            //#60414 related assertion. The exceptions in the bug can only happen
            // when this method is called from 2 threads? but that should not be happening
            assert SwingUtilities.isEventDispatchThread() : "Should be accessed from AWT only or we have a synchronization problem"; //NOI18N
            if (!consumed) {
//                synchronized (writer) {
                    consumed = true;
                    if (Controller.verbose) Controller.log ("EVENT CONSUMED: " + start);
                int charsWritten = getLines().getCharCount();
                    if (initial) {
                        first = 0;
                        offset = 0;
                        lineCount = getLines().getLineCount();
                        length = charsWritten;
                    } else {
                        first = start;
                        if (first == getLines().getLineCount()) {
                            throw new IllegalStateException ("Out of bounds");
                        }

                        offset = getLines().getLineStart(first);
                        lineCount = getLines().getLineCount() - first;
                        length = charsWritten - offset;
                    }
//                }
            }
        }
        
        public boolean isConsumed() {
            return consumed;
        }
        
        public String toString() {
            boolean wasConsumed = isConsumed();
            calc();
            return "Event: start=" + start + " first=" + first + " linecount=" + lineCount + " offset=" + offset + " length=" + length + " consumed=" + wasConsumed;
        }
        
        public DocumentEvent.ElementChange getChange(Element element) {
            if (element == OutputDocument.this) {
                return this;
            } else {
                return null;
            }
        }
        
        public Document getDocument() {
            return OutputDocument.this;
        }
        
        public int getLength() {
            calc();
            return length;
        }
        
        public int getOffset() {
            calc();
            return offset;
        }
        
        public DocumentEvent.EventType getType() {
            return start == 0 ? DocumentEvent.EventType.CHANGE : 
                DocumentEvent.EventType.INSERT;
        }
        
        public Element[] getChildrenAdded() {
            calc();
            Element[] e = new Element[lineCount];
            if (e.length == 0) {
                return new Element[] { new EmptyElement(OutputDocument.this) };
            } else {
                for (int i=0; i < lineCount; i++) {
                    e[i] = new ODElement(first + i);
                    if (first + i >= getLines().getLineCount()) {
                        throw new IllegalStateException ("UGH!!!");
                    }
                }
            }
            return e;
        }
        
        public Element[] getChildrenRemoved() {
            if (start == 0) {
                return new Element[] { new EmptyElement(OutputDocument.this) };
            } else {
                return new Element[0];
            }
        }
        
        public Element getElement() {
            return OutputDocument.this;
        }
        
        public int getIndex() {
            calc();
            return start;
        }
    }
    
    public String toString() {
        return "OD@" + System.identityHashCode(this) + " for " + getLines().readLock();
    }
}

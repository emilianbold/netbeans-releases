/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * OutputDocument.java
 *
 * Created on March 20, 2004, 8:35 PM
 */

package org.netbeans.core.output2;

import org.openide.ErrorManager;
import org.openide.util.Mutex;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TooManyListenersException;


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
    public OutputDocument(OutWriter writer) {
        if (Controller.log) {
            Controller.log ("Creating a Document for " + writer);
        }
        this.writer = writer;
        try {
            writer.addChangeListener (this);
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
    }

    public void dispose() {
        if (Controller.log) Controller.log ("Disposing document for " + writer);
        if (timer != null) {
            timer.stop();
            dlisteners.clear();
            lastEvent = null;
            writer.removeChangeListener(this);
            writer = null;
        }
    }

    public synchronized void addDocumentListener(DocumentListener documentListener) {
        dlisteners.add (documentListener);
        lastEvent = null;
    }
    
    public void addUndoableEditListener(UndoableEditListener l) {
        //do nothing
    }
    
    public Position createPosition(int offset) throws BadLocationException {
        if (offset < 0 || offset > writer.charsWritten()) {
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
        return writer.charsWritten();
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
        if (offset < 0 || offset > writer.charsWritten() || length < 0) {
            throw new BadLocationException ("Bad: " + offset + "," +  //NOI18N
                length, offset);
        }
        if (length == 0) {
            return ""; //NOI18N
        }
        String result = writer.substring (offset, offset + length);
        return result;
    }
    
    private char[] reusableSubrange = new char [2048];
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
        if (writer.lineCount() == 0) {
            txt.array = new char[] {'\n'};
            txt.offset = 0;
            txt.count = 1;
            return;
        }
        if (length > reusableSubrange.length) {
            reusableSubrange = new char[length];
        }
        char[] chars = writer.subrange (offset, offset + length, reusableSubrange);
        txt.array = chars;
        txt.offset = 0;
        txt.count = Math.min(length, chars.length);
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

    /**
     * Get the length in characters of the longest line in the document
     *
     * @return The number of characters in the longest line
     */
    public int getLongestLineLength() {
        return writer.getLongestLineLength();
    }

    /**
     * Get the number of logical lines in this document if wrapped at <code>length</code>
     * (this assumes a fixed width font).
     *
     * @param length The number of characters per line
     * @return The number of logical lines needed to fit the wrapped text
     */
    public int getLogicalLineCountIfWrappedAt (int length) {
        return writer.getLogicalLineCountIfWrappedAt(length);
    }

    /**
     * Get the number of logical lines appearing above a given line if wrapped at
     * <code>charsPerLine</code> (this assumes a fixed width font).
     *
     * @param line The line to calculate for
     * @param charsPerLine The number of characters per line
     * @return The number of logical lines above this one
     */
    public int getLogicalLineCountAbove (int line, int charsPerLine) {
        return writer.getLogicalLineCountAbove(line, charsPerLine);
    }

    /**
     * Get a logical line index for a given point in the display space.
     * This is to accomodate word wrapping using fixed width fonts - this
     * method answers the question "What line of output does the nth row
     * of lines correspond to, given <code>charsPerLine</code> characters
     * per line?".  If the logical line in question is itself wrapped, it
     * will also return how many wrapped lines down from the beginning of
     * the logical line the passed row index is, and the total number of
     * wraps for this logical line to fit inside <code>charsPerLine</code>.
     *
     * @param physIdx A 3 entry array.  Element 0 should be the physical line
     *        (the line position if no wrapping were happening).  On return,
     *        it contains: <ul>
     *         <li>[0] The logical line index for the passed line</li>
     *         <li>[1] The number of line wraps below the logical line
     *             index for this physical line</li>
     *         <li>[2] The total number of line wraps for the logical line</li>
     *         </ul>
     */
    public void toLogicalLineIndex (final int[] physIdx, int charsPerLine) {
        physIdx[1] = 0;
        if (charsPerLine >= getLongestLineLength()) {
            return;
        }
        if (writer.lineCount() <= 1) {
            return;
        }
        int line = physIdx[0] + 1;
        int lcount = 0;

        int max = getElementCount();

        for (int i=0; i < max; i++) {
            int len = writer.length(i);
            int logicalCount = len > charsPerLine ? (len / charsPerLine) + 1 : 1;
            lcount += logicalCount;
            if (lcount >= line) {
                physIdx[0] = i;
                physIdx[1] = (logicalCount - (lcount - line)) - 1;
                physIdx[2] = logicalCount;
                break;
            }
        }
    }

    public int getLineStart (int line) {
        return writer.lineCount() > 0 ? writer.positionOfLine(line) : 0;
    }

    public int getLineEnd (int lineIndex) {
        if (writer.lineCount() == 0) {
            return 0;
        }
        int endOffset;
        if (lineIndex >= writer.lineCount()-1) {
            endOffset = writer.charsWritten();
        } else {
            endOffset = writer.positionOfLine (lineIndex+1);
        }
        return endOffset;
    }

    public int getLineLength (int line) {
        return writer.length(line);
    }

    public void removeUndoableEditListener(UndoableEditListener undoableEditListener) {
        //do nothing
    }
    
    private boolean rendering = false;
    public void render(Runnable runnable) {
        try {
            rendering = true;
            getElementCount(); //Force a refresh of lastPostedLine
            runnable.run();
        } finally {
            rendering = false;
        }
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
        if (writer.lineCount() == 0) {
            return EMPTY;
        }
        synchronized (writer) {
            if (index > lastPostedLine) {
                lastPostedLine = index;
            }
        }
        return new ODElement (index);
    }
    
    public int getElementCount() {
        int result;
        synchronized (writer) {
            result = writer.lineCount();
            lastPostedLine = result;
        }
        if (result == 0) {
            result = 1;
        }
        return result;
    }
    
    public int getElementIndex(int offset) {
        return writer.lineForPosition (offset);
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
        return writer.lineCount() == 0;
    }
    
    public boolean isLineStart (int chpos) {
        return writer.isLineStart(chpos);
    }

    private volatile DO lastEvent = null;
    private int lastPostedLine = -1;
    public void stateChanged(ChangeEvent changeEvent) {
        if (Controller.log) Controller.log (changeEvent != null ? "Document got change event from writer" : "Document timer polling");
        if (dlisteners.isEmpty()) {
            if (Controller.log) Controller.log ("listeners empty, not firing");
            return;
        }
        if (writer.checkDirty()) {
            if (lastEvent != null && !lastEvent.isConsumed()) {
                if (Controller.log) Controller.log ("Last event not consumed, not firing");
                return;
            }
            boolean initial = false; 
            initial = lastPostedLine == - 1;
            
            int lineCount = writer.lineCount();
            int lastLine = lastPostedLine;
            lastPostedLine = lineCount;
            
            if (Controller.log) Controller.log ("Document may fire event - last fired line=" + lastLine + " line count now " + lineCount);
            if (lastLine != lineCount || initial) {
                lastEvent = new DO (Math.max(0, lastLine), initial);
//                evts.add (lastEvent);
                Mutex.EVENT.readAccess (new Runnable() {
                    public void run() {
                        if (Controller.log) Controller.log("Firing document event on EQ with start index " + lastEvent.start);
                        fireDocumentEvent (lastEvent);
                    }
                });
            } else {
                if (Controller.log) Controller.log ("Line count is still " + lineCount + " - not firing");
            }
        } else {
            if (Controller.log) Controller.log ("Writer says it is not dirty, firing no change");
        }
        updateTimerState();
    }    
    
/*    private Vector evts = new Vector();
    private void logInfo() {
        if (log && Controller.log) {
            Controller.log("STREAM CLOSED.  EVENTS FIRED:");
            for (Iterator i = evts.iterator(); i.hasNext();) {
                Controller.log(i.next().toString());
            }
        }
    }
 */
    private boolean updatingTimerState = false;
    private synchronized void updateTimerState() {
        if (updatingTimerState) {
            return;
        }
        updatingTimerState = true;
        long newTime = System.currentTimeMillis();
        if (timer == null && !writer.isClosed()) {
            if (Controller.log) Controller.log("Starting timer");
            //Run the timer fast and furious at first, slowing down after
            //the initial output has been captured
            timer = new javax.swing.Timer(50, this);
            timer.setRepeats(true);
            timer.start();
        } else if (writer.isClosed()) {
            if (timer != null) {
                timer.stop();
            }
            if (writer.peekDirty() && timer != null) {
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
            if (Controller.log) Controller.log ("Decreased timer interval to " + timer.getDelay());
        }
        lastFireTime = newTime;
        updatingTimerState = false;
    }
    
    public void run() {
        stateChanged(null);
    }

    private long lastFireTime = 0;
    public void actionPerformed(ActionEvent actionEvent) {
        if (writer.isClosed()) {
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
    
    
    private static final boolean log = System.getProperty("netbeans.user") == null; //NOI18N //XXX delete
    private void log (String s) { //XXX delete
        //Cannot log from inside NetBeans, we'll end up modifying our own contents
        if (Controller.log) {
            Controller.log(s);
        }
    }
    
    //**************  StyledDocument implementation ****************************
    
    public boolean isHyperlink (int line) {
        return writer.listenerForLine(line) != null;
    }
    
    public boolean isErr (int line) {
        return writer.isErr(line);
    }
    
    final class ODPosition implements Position {
        private int offset;
        
        public ODPosition (int offset) {
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
            int result = writer.charsWritten();
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
        public ODElement (int lineIndex) {
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
            synchronized (writer) {
                if (startOffset == -1) {
                    startOffset = writer.lineCount() > 0 ? writer.positionOfLine(lineIndex) : 0;
                    if (lineIndex >= writer.lineCount()-1) {
                        endOffset = writer.charsWritten();
                    } else {
                        endOffset = writer.positionOfLine (lineIndex+1);
                    }
                    assert endOffset >= getStartOffset() : "Illogical line #" + lineIndex
                        + " with lines " + writer.lineStartList + " or writer has been reset";
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
                ErrorManager.getDefault().notify(ble);
                return "";
            }
        }
    }
    
    private final Element EMPTY = new EmptyElement();
    /**
     * Bug in javax.swing.text.PlainView - even if the element count is 0,
     * it tries to fetch the 0th element.
     */
    private class EmptyElement implements Element {
        
        public javax.swing.text.AttributeSet getAttributes() {
            return SimpleAttributeSet.EMPTY;
        }
        
        public javax.swing.text.Document getDocument() {
            return OutputDocument.this;
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
            return "foo";
        }
        
        public javax.swing.text.Element getParentElement() {
            return OutputDocument.this;
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
        public DO (int start, boolean initial) {
            this.start = start;
            this.initial = initial;
            if (start < 0) {
                throw new IllegalArgumentException ("Illogical start: " + start);
            }
        }
        
        private void calc() {
            if (!consumed) {
//                synchronized (writer) {
                    consumed = true;
                    if (Controller.log) Controller.log ("EVENT CONSUMED: " + start);
                    int charsWritten = writer.charsWritten();
                    if (initial) {
                        first = 0;
                        offset = 0;
                        lineCount = writer.lineCount();
                        length = charsWritten;
                    } else {
                        first = start;
                        if (first == writer.lineCount()) {
                            throw new IllegalStateException ("Out of bounds");
                        }

                        offset = writer.positionOfLine(first);
                        lineCount = writer.lineCount() - first;
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
                return new Element[] { EMPTY };
            } else {
                for (int i=0; i < lineCount; i++) {
                    e[i] = new ODElement(first + i);
                    if (first + i >= writer.lineCount()) {
                        throw new IllegalStateException ("UGH!!!");
                    }
                }
            }
            return e;
        }
        
        public Element[] getChildrenRemoved() {
            if (start == 0) {
                return new Element[] { EMPTY };
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
        return "OD@" + System.identityHashCode(this) + " for " + writer;
    }
}

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
    
    //The original version of this method is left in as it demonstrates clearly
    //what toLogicalLineIndex should do - convert a physical position when
    //wrapped at a certain number of characters into the line index in the
    //document that should appear there.
    
    //The version below is quite simple, but for a 300000 line file, it must
    //loop 300000 times to find the logical position of the last line.
    
    //On the contrary, the divide and conquer approach used below is called
    //recursively an average of 19 times to do the same thing for a 300000 line
    //file - it is much, much faster.
/*    
    public void toLogicalLineIndex (final int[] physIdx, int charsPerLine) {
        physIdx[1] = 0;
        if (charsPerLine >= getLongestLineLength() || (writer.lineCount() <= 1)) {
            physIdx[1] = 0;
            physIdx[2] = 1;
            return;
        }
        int line = physIdx[0] + 1;
        int lcount = 0;

        int max = writer.lineCount();

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
 */    

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
     *        (the line position if no wrapping were happening) when called;
     *        the other two elements are ignored.  On return,
     *        it contains: <ul>
     *         <li>[0] The logical line index for the passed line</li>
     *         <li>[1] The number of line wraps below the logical line
     *             index for this physical line</li>
     *         <li>[2] The total number of line wraps for the logical line</li>
     *         </ul>
     */
    public void toLogicalLineIndex (final int[] physIdx, int charsPerLine) {
        int physicalLine = physIdx[0];
        physIdx[1] = 0;
        int linecount = writer.lineCount();
        
        if (physicalLine == 0) {
            //First line never has lines above it
            physIdx[1] = 0;
            physIdx[2] = (writer.length(physicalLine) / charsPerLine);
        }
        
        if (charsPerLine >= getLongestLineLength() || (writer.lineCount() <= 1)) {
            //The doc is empty, or there are no lines long enough to wrap anyway
            physIdx[1] = 0;
            physIdx[2] = 1;
            return;
        }
        
        int logicalLine = 
            findFirstLineWithoutMoreLinesAboveItThan (physicalLine, charsPerLine);
        
        int linesAbove = writer.getLogicalLineCountAbove(logicalLine, charsPerLine);

        int len = writer.length(logicalLine);
        
        int wrapCount = len > charsPerLine ? (len / charsPerLine) + 1 : 1;
        
        physIdx[0] = logicalLine;
        int lcount = linesAbove + wrapCount;
        physIdx[1] = (wrapCount - (lcount - physicalLine));
          
        physIdx[2] = wrapCount;
    }
    
    /**
     * Uses a divide-and-conquer approach to quickly locate a line which has
     * the specified number of logical lines above it.  For large output, this
     * data is cached in OutWriter in a sparse int array.  This method is called
     * from viewToModel, so it must be very, very fast - it may be called once
     * every time the mouse is moved, to determine if the cursor should be 
     * updated.
     */
    private int findFirstLineWithoutMoreLinesAboveItThan (int target, int charsPerLine) {
        int start = 0;
        int end = writer.lineCount();
        int midpoint = start + ((end - start) / 2);
        int linesAbove = writer.getLogicalLineCountAbove(midpoint, charsPerLine);
        int result = divideAndConquer (target, start, midpoint, end, charsPerLine, linesAbove);
        
        return Math.min(end, result) -1;
    }
    /**
     * Recursively search for the line number with the smallest number of lines
     * above it, greater than the passed target number of lines.  This is 
     * effectively a binary search - divides the range of lines in half and
     * checks if the middle value is greater than the target; then recurses on
     * itself with whatever half of the range of lines has a better chance at
     * containing a smaller value.
     * <p>
     * It is primed with an initial call with the start, midpoint and end values.
     */
    private int divideAndConquer (int target, int start, int midpoint, int end, int charsPerLine, int midValue) {
        //We have an exact match - we're done
        if (midValue == target) {
            return midpoint + 1;
        }
        
        //In any of these conditions, the search has run out of gas - the
        //end value must be the match
        if (end - start <= 1 || midpoint == start || midpoint == end) {
            return end;
        }

        if (midValue > target) {
            //The middle value is greater than the target - look for a closer
            //match between the first and the middle line
            
            int upperMidPoint = upperMidPoint = start + ((midpoint - start) / 2);
            if ((midpoint - start) % 2 != 0) {
                upperMidPoint++;
            }
            int upperMidValue = writer.getLogicalLineCountAbove (upperMidPoint, charsPerLine);
            return divideAndConquer (target, start, upperMidPoint, midpoint, charsPerLine, upperMidValue);
        } else {
            //The middle value is less than the target - look for a match
            //between the midpoint and the last line
            
            int lowerMidPoint = ((end - start) / 4) + midpoint;
            if ((end - midpoint) % 2 != 0) {
                lowerMidPoint++;
            }
            int lowerMidValue = writer.getLogicalLineCountAbove (lowerMidPoint, charsPerLine);
            return divideAndConquer (target, midpoint, lowerMidPoint, end, charsPerLine, lowerMidValue);
        }
    }
    
    boolean stillGrowing() {
        return !writer.isClosed();
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
        if (Controller.verbose) Controller.log (changeEvent != null ? "Document got change event from writer" : "Document timer polling");
        if (dlisteners.isEmpty()) {
            if (Controller.verbose) Controller.log ("listeners empty, not firing");
            return;
        }
        if (writer.checkDirty()) {
            if (lastEvent != null && !lastEvent.isConsumed()) {
                if (Controller.verbose) Controller.log ("Last event not consumed, not firing");
                return;
            }
            boolean initial = false; 
            initial = lastPostedLine == - 1;
            
            int lineCount = writer.lineCount();
            int lastLine = lastPostedLine;
            lastPostedLine = lineCount;
            
            if (Controller.verbose) Controller.log ("Document may fire event - last fired line=" + lastLine + " line count now " + lineCount);
            if (lastLine != lineCount || initial) {
                lastEvent = new DO (Math.max(0, lastLine), initial);
//                evts.add (lastEvent);
                Mutex.EVENT.readAccess (new Runnable() {
                    public void run() {
                        if (Controller.verbose) Controller.log("Firing document event on EQ with start index " + lastEvent.start);
                        fireDocumentEvent (lastEvent);
                    }
                });
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

    //**************  StyledDocument implementation ****************************
    
    public boolean isHyperlink (int line) {
        return writer.listenerForLine(line) != null;
    }
    
    public boolean hasHyperlinks() {
        return writer.firstListenerLine() != -1;
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
                    if (Controller.verbose) Controller.log ("EVENT CONSUMED: " + start);
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

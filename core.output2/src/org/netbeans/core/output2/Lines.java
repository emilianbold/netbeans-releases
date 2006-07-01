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
package org.netbeans.core.output2;

import org.openide.windows.OutputListener;

import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.util.regex.Matcher;

/**
 * An interface representing the data written to an OutWriter, in terms of lines of text, with
 * methods for handling line wrapping.
 */
public interface Lines {
    /**
     * Get an array of all line numbers which have associated OutputListeners
     * @return an array of line numbers
     */
    int[] allListenerLines();

    /**
     * Get the length, in characters, of a given line index
     *
     * @param idx the line number
     * @return the length
     */
    int length (int idx);

    /**
     * Get the character position corresponding to the start of a line
     *
     * @param line A line number
     * @return The character in the total text at which this line starts
     */
    int getLineStart (int line);

    /**
     * Get the line index of the nearest line start to this character position in the
     * entire stored text
     * @param position
     * @return The line on which this character position occurs
     */
    int getLineAt (int position);

    /**
     * Get the number of lines in the stored text
     * @return A line count
     */
    int getLineCount();

    /**
     * Get the output listener associated with this line
     *
     * @param line A line number
     * @return An OutputListener, as passed to OutputWriter.println(), or null if no listener
     *  is associated with this line
     */
    OutputListener getListenerForLine (int line);

    /**
     * Get the index of the first line which has a listener
     * @return A line number, or -1 if there are no listeners
     */
    int firstListenerLine ();
    
    /**
     * Get the index of the first line which has an important listener
     * @return A line number, or -1 if there are no important listeners
     */
    
    int firstImportantListenerLine();
    
    boolean isImportantHyperlink(int line);

    /**
     * Get the nearest listener to the passed line index
     * @param line A line number
     * @param backward If it should search up the text
     * @return A line number, or -1
     */
    int nearestListenerLine (int line, boolean backward);

    /**
     * Get the length of the longest line in the storage
     * @return The longest line's length
     */
    int getLongestLineLength();

    /**
     * Get the count of logical (wrapped) lines above the passed index.  The passed index should be a line
     * index in a physical coordinate space in which lines are wrapped at charCount.  It will return the
     * number of logical (wrapped) lines above this line.
     *
     * @param logicalLine A line index in wrapped, physical space
     * @param charCount The number of characters at which line wrapping happens
     * @return The number of  logical lines above this one.
     */
    int getLogicalLineCountAbove (int logicalLine, int charCount);

    /**
     * Get the total number of logical lines required to display the stored text if wrapped at the specified
     * character count.
     * @param charCount The number of characters at which line wrapping happens
     * @return The number of logical lines needed to fit all of the text
     */
    int getLogicalLineCountIfWrappedAt (int charCount);

    /**
     * Determine if a character position indicates the first character of a line.
     *
     * @param chpos A character index in the stored text
     * @return Whether or not it's the first character of a line
     */
    boolean isLineStart (int chpos);

    /**
     * Get a line of text
     *
     * @param idx A line number
     * @return The text
     * @throws IOException If an error occurs and the text cannot be read
     */
    String getLine (int idx) throws IOException;

    /**
     * Determine if there are any hyperlinked lines (lines with associated output listeners)
     * @return True if there are any listeners
     */
    boolean hasHyperlinks();

    /**
     * Determine if this line has an associated OutputListener
     * @param line A line number
     * @return If it has a listener
     */
    boolean isHyperlink (int line);

    /**
     * Count the total number of characters in the stored text
     *
     * @return The number of characters that have been written
     */
    int getCharCount();

    /**
     * Fetch a getText of the entire text
     * @param start A character position < end
     * @param end A character position > start
     * @return A String representation of the text between these points, including newlines
     */
    String getText (int start, int end);

    /**
     * Fetch a getText of the entire text into a character array
     * @param start A character position < end
     * @param end A character position >  start
     * @param chars A character array at least as large as end-start, or null
     * @return A character array containing the range of text specified
     */
    char[] getText (int start, int end, char[] chars);
    /**
      * Get a logical line index for a given point in the display space.
      * This is to accomodate line wrapping using fixed width fonts - this
      * method answers the question "What line of output does the nth row
      * of lines correspond to, given <code>charsPerLine</code> characters
      * per line?".  If the logical line in question is itself wrapped, it
      * will also return how many wrapped lines down from the beginning of
      * the logical line the passed row index is, and the total number of
      * wraps for this logical line to fit inside <code>charsPerLine</code>.
      *
      * @param info A 3 entry array.  Element 0 should be the physical line
      *        (the line position if no wrapping were happening) when called;
      *        the other two elements are ignored.  On return,
      *        it contains: <ul>
      *         <li>[0] The logical line index for the passed line</li>
      *         <li>[1] The number of line wraps below the logical line
      *             index for this physical line</li>
      *         <li>[2] The total number of line wraps for the logical line</li>
      *         </ul>
      */
    void toLogicalLineIndex (int[] info, int charsPerLine);
    /**
     * Save the contents of the buffer to a file, in platform default encoding.
     *
     * @param path The file to save to
     * @throws IOException If there is a problem writing or encoding the data, or if overwrite is false and the
     *    specified file exists
     */
    void saveAs(String path) throws IOException;

    /**
     * Do a forward search for the last matched text (from a call to find()).
     *
     * @return A matcher, if there has been a successful call to find
     */
    Matcher getForwardMatcher();
    /**
     * Do a reverse search for the last matched text.  Using this is a little tricky -
     * since there is no direct support for reverse searches in java.util.regex, what
     * we do is take the last pattern and the entire buffer text, and reverse them,
     * and produce a matcher based on that.
     * <p>
     * What this means is that the caller gets the job of flipping things back around,
     * as follows:
     * <pre>
     * int matchStart = getLength() - matcher.end();
     * int matchEnd = getLength() - matcher.start()
     *
     * will return the actual positions in the data of the match.
     *
     * @return A matcher over a reversed version of the data
     */
    Matcher getReverseMatcher();
    /**
     * Get a regular expression matcher over the backing storage.  Note that the resulting Matcher object
     * should not be held across reset or other events that can destroy the contents of the buffer.
     *
     * @param s A pattern as defined in javax.regex
     * @return A pattern matcher object, or null if the pattern is invalid or there is a problem with the
     *   backing storage
     */
    Matcher find(String s);

    /**
     * Indicates a line was written to the stderr, not stdout
     *
     * @param line A line number
     * @return True if it was written to stderr
     */
    boolean isErr(int line);

    /**
     * Acquire a read lock - while held, other threads cannot modify this Lines object.
     *
     * @return
     */
    Object readLock();

    /**
     * Add a change listener, which will detect when lines are written. <strong>Changes are
     * <u>not</u> fired for every write; they should be fired when an initial line is written,
     * when the writer is flushed, or when it is closed.</strong>  Clients which respond to ongoing
     * writes should use a timer and poll via <code>checkDirty()</code> to see if new data has
     * been written.
     *
     * @param cl A change listener
     */
    void addChangeListener (ChangeListener cl);

    /**
     * Remove a change listener.
     *
     * @param cl
     */
    void removeChangeListener (ChangeListener cl);

    /**
     * Allows clients that wish to poll to see if there is new output to do
     * so.  When any thread writes to the output, the dirty flag is set.
     * Calling this method returns its current value and clears it.  If it
     * returns true, a view of the data may need to repaint itself or something
     * such.  This mechanism can be used in preference to listener based
     * notification, by running a timer to poll as long as the output is
     * open, for cases where otherwise the event queue would be flooded with
     * notifications for small writes.
     *
     * @param clear Whether or not to clear the dirty flag
     */
    boolean checkDirty(boolean clear);

    /**
     * Determine whether or not the storage backing this Lines object is being actively written to.
     * @return True if there is still an open stream which may write to the backing storage and no error has occured
     */
    boolean isGrowing();
}

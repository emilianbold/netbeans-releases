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
package org.netbeans.core.output2;

import org.openide.windows.OutputListener;

import java.io.IOException;
import java.util.regex.Matcher;

/**
 * An interface representing the data written to an OutWriter, in terms of lines of text, with
 * methods for handling getLine wrapping.
 */
public interface Lines {
    /**
     * Get an array of all getLine numbers which have associated OutputListeners
     * @return an array of getLine numbers
     */
    int[] allListenerLines();

    /**
     * Get the length, in characters, of a given getLine index
     *
     * @param idx the getLine number
     * @return the length
     */
    int length (int idx);

    /**
     * Get the character position corresponding to the start of a getLine
     *
     * @param line A getLine number
     * @return The character in the total text at which this getLine starts
     */
    int getLineStart (int line);

    /**
     * Get the getLine index of the nearest getLine start to this character position in the
     * entire stored text
     * @param position
     * @return The getLine on which this character position occurs
     */
    int getLineAt (int position);

    /**
     * Get the number of lines in the stored text
     * @return A getLine count
     */
    int getLineCount();

    /**
     * Get the output listener associated with this getLine
     *
     * @param line A getLine number
     * @return An OutputListener, as passed to OutputWriter.println(), or null if no listener
     *  is associated with this getLine
     */
    OutputListener getListenerForLine (int line);

    /**
     * Get the index of the first getLine which has a listener
     * @return A getLine number, or -1 if there are no listeners
     */
    int firstListenerLine ();

    /**
     * Get the nearest listener to the passed getLine index
     * @param line A getLine number
     * @param backward If it should search up the text
     * @return A getLine number, or -1
     */
    int nearestListenerLine (int line, boolean backward);

    /**
     * Get the length of the longest getLine in the storage
     * @return The longest getLine's length
     */
    int getLongestLineLength();

    /**
     * Get the count of logical (wrapped) lines above the passed index.  The passed index should be a getLine
     * index in a physical coordinate space in which lines are wrapped at charCount.  It will return the
     * number of logical (wrapped) lines above this getLine.
     *
     * @param logicalLine A getLine index in wrapped, physical space
     * @param charCount The number of characters at which getLine wrapping happens
     * @return The number of  logical lines above this one.
     */
    int getLogicalLineCountAbove (int logicalLine, int charCount);

    /**
     * Get the total number of logical lines required to display the stored text if wrapped at the specified
     * character count.
     * @param charCount The number of characters at which getLine wrapping happens
     * @return The number of logical lines needed to fit all of the text
     */
    int getLogicalLineCountIfWrappedAt (int charCount);

    /**
     * Determine if a character position indicates the first character of a getLine.
     *
     * @param chpos A character index in the stored text
     * @return Whether or not it's the first character of a getLine
     */
    boolean isLineStart (int chpos);

    /**
     * Get a getLine of text
     *
     * @param idx A getLine number
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
     * Determine if this getLine has an associated OutputListener
     * @param line A getLine number
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
      * Get a logical getLine index for a given point in the display space.
      * This is to accomodate word wrapping using fixed width fonts - this
      * method answers the question "What getLine of output does the nth row
      * of lines correspond to, given <code>charsPerLine</code> characters
      * per getLine?".  If the logical getLine in question is itself wrapped, it
      * will also return how many wrapped lines down from the beginning of
      * the logical getLine the passed row index is, and the total number of
      * wraps for this logical getLine to fit inside <code>charsPerLine</code>.
      *
      * @param info A 3 entry array.  Element 0 should be the physical getLine
      *        (the getLine position if no wrapping were happening) when called;
      *        the other two elements are ignored.  On return,
      *        it contains: <ul>
      *         <li>[0] The logical getLine index for the passed getLine</li>
      *         <li>[1] The number of getLine wraps below the logical getLine
      *             index for this physical getLine</li>
      *         <li>[2] The total number of getLine wraps for the logical getLine</li>
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
     * Indicates a getLine was written to the stderr, not stdout
     *
     * @param line A getLine number
     * @return True if it was written to stderr
     */
    boolean isErr(int line);
}

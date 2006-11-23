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
package org.openide.text;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import java.io.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


/** Represents one line in a text document.
 * The line number may change
* when the text is modified, but the identity of the line is retained. It is designed to allow line-dependent
* modules of the IDE (such as the compiler and debugger) to make use of a line consistently even as the text is modified.
*
* @author Ales Novak, Petr Hamernik, Jan Jancura, Jaroslav Tulach, David Konecny
*/
public abstract class Line extends Annotatable implements Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 9113186289600795476L;

    /** Property name of the line number */
    public static final String PROP_LINE_NUMBER = "lineNumber"; // NOI18N

    /** Shows the line only if the editor is open.
     * @see #show(int) <code>show</code>
     */
    public final static int SHOW_TRY_SHOW = 0;

    /** Opens the editor if necessary and shows the line.
     * @see #show(int) <code>show</code>
     */
    public final static int SHOW_SHOW = 1;

    /** Opens the editor if necessary, shows the line, and takes the focus.
     * @see #show(int) <code>show</code>
     */
    public final static int SHOW_GOTO = 2;

    /** Same as SHOW_GOTO except that the Window Manager attempts to front the
     * editor window (i.e. make it the top most window).
     * @see #show(int) <code>show</code>
     * @see org.openide.windows.TopComponent#toFront()
     * @since 5.8
     */
    public final static int SHOW_TOFRONT = 3;

    /** Instance of null implementation of Line.Part */
    static final private Line.Part nullPart = new Line.NullPart();

    /** context of this line */
    private org.openide.util.Lookup dataObject;

    /** Create a new line object based on a given data object.
     * This implementation is abstract, so the specific line number is not used here.
     * Subclasses should somehow specify the position.
     * <P>
     * The context argument shall contain information about the
     * producer of the Line, that can be then extracted by {@link Line#getLookup} call.
     *
     * @param context the context for this line
     */
    public Line(Lookup context) {
        if (context == null) {
            throw new NullPointerException();
        }

        dataObject = context;
    }

    /**
     * Create a new line object based on a given data object.
     * This implementation is abstract, so the specific line number is not used here. Subclasses should somehow specify the position.
     * @param source the object that is producing the Line
     */
    public Line(Object source) {
        this((source instanceof Lookup) ? (Lookup) source : Lookups.singleton(source));

        if (source == null) {
            throw new NullPointerException();
        }
    }

    /** Composes a human presentable name for the line. The default
    * implementation uses the name of data object and the line number
    * to create the display name.
    *
    * @return human presentable name that should identify the line
    */
    public String getDisplayName() {
        return getClass().getName() + ":" + getLineNumber(); // NOI18N
    }

    /** Provides access to the context passed into the line constructor.
     * For example lines produced by <code>DataEditorSupport</code>
     * provide <code>DataObject</code> as the content of the lookup.
     * One can use:
     * <PRE>
     *   dataObjectOrNull = (DataObject)line.getLookup ().lookup (DataObject.class);
     * </PRE>
     * to get the access.
     *
     * @return context associated with the line
     * @since 4.3
     */
    public final org.openide.util.Lookup getLookup() {
        return dataObject;
    }

    /** Get the line number. The last condition in following should
    * always be true:
    * <PRE>
    *   Line.Set lineSet = <line set>
    *   Line l = <some line from line set lineSet>
    *
    *   l.equals (lineSet.getCurrent (l.getLineNumber ()))
    * </PRE>
    *
    * @return current line number (may change as text is edited)
    */
    public abstract int getLineNumber();

    /** Show the line.
    * @param kind one of {@link #SHOW_TRY_SHOW}, {@link #SHOW_SHOW}, or {@link #SHOW_GOTO}
    * @param column the column of this line which should be selected
    */
    public abstract void show(int kind, int column);

    /** Shows the line (at the first column).
    * @param kind one of {@link #SHOW_TRY_SHOW}, {@link #SHOW_SHOW}, or {@link #SHOW_GOTO}
    * @see #show(int, int)
    */
    public void show(int kind) {
        show(kind, 0);
    }

    /** Set or clear a (debugger) breakpoint at this line.
     * @param b <code>true</code> to turn on
     * @deprecated Deprecated since 1.20. Use {@link Annotation#attach} instead.
     */
    @Deprecated
    public abstract void setBreakpoint(boolean b);

    /** Test if there is a breakpoint set at this line.
     * @return <code>true</code> is there is
     * @deprecated Deprecated since 1.20. Use {@link Annotation} instead.
     */
    @Deprecated
    public abstract boolean isBreakpoint();

    /** Mark an error at this line.
     * @deprecated Deprecated since 1.20. Use {@link Annotation#attach} instead.
     */
    @Deprecated
    public abstract void markError();

    /** Unmark error at this line.
     * @deprecated Deprecated since 1.20. Use {@link Annotation#detach} instead.
     */
    @Deprecated
    public abstract void unmarkError();

    /** Mark this line as current.
     * @deprecated Deprecated since 1.20. Use {@link Annotation#attach} instead.
     */
    @Deprecated
    public abstract void markCurrentLine();

    /** Unmark this line as current.
     * @deprecated Deprecated since 1.20. Use {@link Annotation#detach} instead.
     */
    @Deprecated
    public abstract void unmarkCurrentLine();

    /** Method that should allow the debugger to communicate with lines that
    * wants to have a control over the current line of debugger. It allows the
    * line to refuse the current status and force the debugger to continue
    * over this line.
    * <P>
    * The default implementation simply returns true.
    *
    * @param action type of action that is trying to mark this line as current
    *    one of constants (Debugger.ACTION_BREAKPOINT_HIT,
    *    Debugger.ACTION_TRACE_OVER, etc.)
    * @param previousLine previous line (if any) or null
    *
    * @return true if this line accepts the "current" state or false if the
    *    line wants the debugger to proceed with next instruction
    *
    * @deprecated Deprecated since 1.20, as {@link #markCurrentLine} is deprecated by {@link Annotation#attach}.
    */
    @Deprecated
    public boolean canBeMarkedCurrent(int action, Line previousLine) {
        return true;
    }

    /** Create object which represent part of the text on the line. This part
     * of the line can be used for attaching of annotations.
     * @param column starting column of the part of the text
     * @param length length of the part of the text
     * @return instance of the Line.Part which represent the part of the text
     * @since 1.20
     */
    public Line.Part createPart(int column, int length) {
        return nullPart;
    }

    public String getText() {
        return null;
    }

    /** Representation of the part of the Line's text. The part of the text is defined by
     * the starting column, length of the part and reference to Line. The length of the
     * part never cross the end of the line.
     * @since 1.20
     */
    public static abstract class Part extends Annotatable {
        /** Property name for the line attribute */
        public static final String PROP_LINE = "line"; // NOI18N

        /** Property name for the column attribute */
        public static final String PROP_COLUMN = "column"; // NOI18N

        /** Property name for the length attribute */
        public static final String PROP_LENGTH = "length"; // NOI18N

        /** Start column of annotation
         * @return column at which this part begining
         */
        public abstract int getColumn();

        /** Length of the annotated text. The length does not cross line end. If the annotated text is
         * split during the editing, the annotation is shorten till the end of the line. Modules can listen on
         * changes of this value
         * @return length of the part
         */
        public abstract int getLength();

        /** Line can change during editting
         * @return reference to the Line to which this part belongs
         */
        public abstract Line getLine();
    }

    /** Implementation of Line.Part which is presenting empty part */
    static final private class NullPart extends Part {
        NullPart() {
        }

        public int getColumn() {
            return 0;
        }

        public int getLength() {
            return 0;
        }

        public Line getLine() {
            return null;
        }

        public String getText() {
            return null;
        }
    }

    /** Object that represents a snapshot of lines at the time it was created.
    * It is used to create a mapping from line
    * numbers to line objects, for example when the file is saved.
    * Such a mapping can then be used by the compiler, e.g., to find
    * the correct {@link Line} object, assuming it has a line number.
    * <P>
    * Mappings of line numbers to line objects will survive modifications
    * of the text, and continue to represent the original lines as close as possible.
    * For example: if a new line is inserted at the 10th line of a document
    * and the compiler module asks for the 25th line (because the compiler reports an error at line 25 in the saved file) via the line set, the 26th line
    * of the current document will be marked as being in error.
    */
    public static abstract class Set extends Object {
        /** date when the object has been created */
        private Date date;

        /** <code>Map</code> which contains all lines as keys and
         * values weakReferences on itself. There woudl be better use
         * set but there is missing get method, returning equal object.
         * belonging to this <code>Line.Set</code>.
         * @see DocumentLine#hashCode
         * @see DocumentLine#equals
         * @see #registerLine */
        private Map<Line,Reference<Line>> whm;

        /** Create a new snapshot. Remembers the date when it was created. */
        public Set() {
            date = new Date();
        }

        /** Returns a set of line objects sorted by their
        * line numbers. This immutable list will contains all lines held by this
        * line set.
        *
        * @return list of lines
        */
        public abstract List<? extends Line> getLines();

        /** Get creation time for this line set.
         * @return time
        */
        public final Date getDate() {
            return date;
        }

        /** Find line object in the line set corresponding to original line number.
         * That is, finds the line in the current document which originally had the indicated line number.
         * If there have been modifications of that line, find one as close as possible.
        *
        * @param line number of the line
        * @return line object
        * @exception IndexOutOfBoundsException if <code>line</code> is an invalid index for the original set of lines
        */
        public abstract Line getOriginal(int line) throws IndexOutOfBoundsException;

        /** Find line object representing the line in current document.
        *
        *
        * @param line number of the line in current state of the document
        * @return line object
        * @exception IndexOutOfBoundsException if <code>line</code> is an invalid index for the original set of lines
        */
        public abstract Line getCurrent(int line) throws IndexOutOfBoundsException;

        /** Finds an original line number for given line in this line set.
         * @param line the line to look for
         * @return the number that best matches the line number of the line or -1
         *    if the line does seem to be produced by this line set
         * @since 4.38
         */
        public int getOriginalLineNumber(Line line) {
            return computeOriginal(this, line);
        }

        /** Lazyly creates or finds already created map for internal use.
         */
        Map<Line,Reference<Line>> findWeakHashMap() {
            synchronized (date) {
                if (whm != null) {
                    return whm;
                }

                whm = new WeakHashMap<Line,Reference<Line>>();

                return whm;
            }
        }

        /** Registers the line to this <code>Line.Set</code>.
         * @param line <code>Line</code> to register
         * @return registered <code>Line</code>. <em>Note:</em> the retruned
         * <code>Line</code> could be different (identityHashCode not equal)
         * from the one passed in */
        final Line registerLine(Line line) {
            // beware of null argument
            if (line == null) {
                throw new NullPointerException();
            }

            Map<Line,Reference<Line>> lines = findWeakHashMap();

            synchronized (lines) {
                Reference<Line> r = lines.get(line);
                Line in = r != null ? r.get() : null;

                if (in == null) {
                    if (line instanceof DocumentLine) {
                        ((DocumentLine) line).init();
                    }

                    lines.put(line, new WeakReference<Line>(line));
                    in = line;
                }

                return in;
            }
        }

        /** Finds whether a line equal to provided is already registered.
         * @param line the line to register
         * @return the registered line equal to line or null
         */
        final Line findLine(Line line) {
            Map<Line,Reference<Line>> lines = findWeakHashMap();

            synchronized (lines) {
                Reference<Line> r = lines.get(line);
                Line in = r != null ? r.get() : null;

                return in;
            }
        }

        /** A method that for a given Line.Set and a line computes the best
         * original line number based on the querying the set. This is called
         * in default implementation of getOriginal (Line) to provide
         * inefficient (but better then most people would write) way to
         * compute the number. It is static so it can be tested from
         * tests working on DocumentLine objects that override the
         * getOriginal (Line) method.
         *
         * @param set the set to search in
         * @param line the line to look for
         * @return closest possible line number for given line
         */
        static int computeOriginal(Line.Set set, Line line) {
            int n = line.getLineNumber();
            Line current = null;

            try {
                current = set.getOriginal(n);

                if (line.equals(current)) {
                    return n;
                }
            } catch (IndexOutOfBoundsException ex) {
                // ok, few lines have been added and this one is now
                // bellow the end of the document
            }

            if (current == null) {
                return binarySearch(set, n, 0, findMaxLine(set));
            }

            if (n < current.getLineNumber()) {
                return binarySearch(set, n, 0, current.getLineNumber());
            } else {
                return binarySearch(set, n, current.getLineNumber(), findMaxLine(set));
            }
        }

        /** Does a search for a given line number in a given Line.Set.
         */
        private static int binarySearch(Line.Set set, int number, int from, int to) {
            while (from < to) {
                int middle = (from + to) / 2;

                Line l = set.getOriginal(middle);
                int ln = l.getLineNumber();

                if (ln == number) {
                    return middle;
                }

                if (ln < number) {
                    // try after the middle
                    from = middle + 1;
                } else {
                    // try before the middle
                    to = middle - 1;
                }
            }

            return from;
        }

        private static int findMaxLine(Line.Set set) {
            int from = 0;
            int to = 32000;

            // probably larger than any existing document
            for (;;) {
                try {
                    set.getOriginal(to);

                    // if the line exists, double the max number, but keep
                    // for reference that it exists
                    from = to;
                    to *= 2;
                } catch (IndexOutOfBoundsException ex) {
                    break;
                }
            }

            while (from < to) {
                int middle = (from + to + 1) / 2;

                try {
                    set.getOriginal(middle);

                    // line exists
                    from = middle;
                } catch (IndexOutOfBoundsException ex) {
                    // line does not exists, we have to search lower
                    to = middle - 1;
                }
            }

            return from;
        }
    }
     // End of class Line.Set.
}

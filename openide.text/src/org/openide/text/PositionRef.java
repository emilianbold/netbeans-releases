/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.openide.text;

import org.openide.util.RequestProcessor;

import java.io.*;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import java.util.*;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;


/** Reference to one position in a document.
* This position is held as an integer offset, or as a {@link Position} object.
* There is also support for serialization of positions.
*
* @author Petr Hamernik
*/
public final class PositionRef extends Object implements Serializable {
    static final long serialVersionUID = -4931337398907426948L;

    /** Which type of position is currently holded - int X Position */
    transient private Manager.Kind kind;

    /** Manager for this position */
    private Manager manager;

    /** insert after? */
    private boolean insertAfter;

    /** Creates new <code>PositionRef</code> using the given manager at the specified
    * position offset.
    * @param manager manager for the position
    * @param offset - position in the document
    * @param bias the bias for the position
    */
    PositionRef(Manager manager, int offset, Position.Bias bias) {
        this(manager, new Manager.OffsetKind(offset, manager), bias);
    }

    /** Creates new <code>PositionRef</code> using the given manager at the specified
    * line and column.
    * @param manager manager for the position
    * @param line line number
    * @param column column number
    * @param bias the bias for the position
    */
    PositionRef(Manager manager, int line, int column, Position.Bias bias) {
        this(manager, new Manager.LineKind(line, column, manager), bias);
    }

    /** Constructor for everything.
    * @param manager manager that we are refering to
    * @param kind kind of position we hold
    * @param bias bias for the position
    */
    private PositionRef(Manager manager, Manager.Kind kind, Position.Bias bias) {
        this.manager = manager;
        this.kind = kind;
        insertAfter = (bias == Position.Bias.Backward);
        init();
    }

    /** Initialize variables after construction and after deserialization. */
    private void init() {
        kind = manager.addPosition(this);
    }

    /** Writes the manager and the offset (int). */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeBoolean(insertAfter);
        out.writeObject(manager);
        kind.write(out);
    }

    /** Reads the manager and the offset (int). */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        insertAfter = in.readBoolean();
        manager = (Manager) in.readObject();
        kind = manager.readKind(in);
        init();
    }

    /** @return the appropriate manager for this position ref.
    */
    public CloneableEditorSupport getCloneableEditorSupport() {
        return manager.getCloneableEditorSupport();
    }

    /** @return the bias of the position
    */
    public Position.Bias getPositionBias() {
        return insertAfter ? Position.Bias.Backward : Position.Bias.Forward;
    }

    /** @return the position as swing.text.Position object.
    * @exception IOException when an exception occured during reading the file.
    */
    public Position getPosition() throws IOException {
        // Hold the document reference to prevent document to be closed
        StyledDocument doc = manager.getCloneableEditorSupport().getDocument();
        if (doc == null) {
            doc = manager.getCloneableEditorSupport().openDocument();
        }

        synchronized (manager.getLock()) {
            // Fix for IZ#67761 - ClassCastException: org.openide.text.PositionRef$Manager$OffsetKind
            Manager.PositionKind p = kind.toMemory( insertAfter );

            return p.pos;
        }
    }

    /** @return the position as offset index in the file.
    */
    public int getOffset() {
        return kind.getOffset();
    }

    /** Get the line number where this position points to.
    * @return the line number for this position
    * @throws IOException if the document could not be opened to check the line number
    */
    public int getLine() throws IOException {
        return kind.getLine();
    }

    /** Get the column number where this position points to.
    * @return the column number within a line (counting starts from zero)
    * @exception IOException if the document could not be opened to check the column number
    */
    public int getColumn() throws IOException {
        return kind.getColumn();
    }

    public String toString() {
        return "Pos[" + getOffset() + "]" + ", kind=" + kind; // NOI18N
    }

    /** This class is responsible for the holding the Document object
    * and the switching the status of PositionRef (Position X offset)
    * objects which depends to this manager.
    * It has one abstract method for the creating the StyledDocument.
    */
    static final class Manager extends Object implements Runnable, Serializable {
        /** document that this thread should use */
        // XXX never read, does it have some purpose?
        private static ThreadLocal<Object> DOCUMENT = new ThreadLocal<Object>();
        static final long serialVersionUID = -4374030124265110801L;

        /** Head item of data structure replacing linked list here.
         * @see ChainItem */
        private transient ChainItem head;

        /** ReferenceQueue where all <code>ChainedItem</code>'s will be enqueued to. */
        private transient ReferenceQueue<PositionRef> queue;

        /** Counter which counts enqued items and after reaching
         * number 100 schedules sweepTask. */
        private transient int counter;

        /** Task which is run in RequestProcessor thread and provides
         * full pass sweep, i.e. removes items with garbaged referents from
         * data strucure. */
        private transient RequestProcessor.Task sweepTask;
        private static final RequestProcessor RP = new RequestProcessor(PositionRef.class);

        /** support for the editor */
        transient private CloneableEditorSupport support;

        /** the document for this manager or null if the manager is not in memory */
        transient private Reference<StyledDocument> doc;

        /** Creates new manager
        * @param supp support to work with
        */
        public Manager(CloneableEditorSupport supp) {
            support = supp;
            init();
        }

        /** Initialize the variables to the default values. */
        protected void init() {
            queue = new ReferenceQueue<PositionRef>();

            // A stable mark used to simplify operations with the list
            head = new ChainItem(null, queue, null);
        }

        private Object getLock() {
            return support.getLock();
        }

        /** Reads the object and initialize */
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            Object firstObject = in.readObject();

            /* Get rid of backward compatibility

            if (firstObject instanceof DataObject) {
                DataObject obj = (DataObject)firstObject;
                support = (CloneableEditorSupport) obj.getCookie(CloneableEditorSupport.class);
            } else */
            {
                // first object is environment
                CloneableEditorSupport.Env env = (CloneableEditorSupport.Env) firstObject;
                support = (CloneableEditorSupport) env.findCloneableOpenSupport();
            }

            if (support == null) {
                //PENDING - what about now ? does exist better way ?
                throw new IOException();
            }
        }

        final Object readResolve() {
            return support.getPositionManager();
        }

        private void writeObject(ObjectOutputStream out)
        throws IOException {
            // old serialization version            out.writeObject(support.findDataObject());
            out.writeObject(support.cesEnv());
        }

        /** @return the styled document or null if the document is not loaded.
        */
        public CloneableEditorSupport getCloneableEditorSupport() {
            return support;
        }

        /** Converts all positions into document one.
        */
        void documentOpened(Reference<StyledDocument> doc) {
            this.doc = doc;

            processPositions(true);
        }

        /** Closes the document and switch all positionRefs to the offset (int)
        * holding status (Position objects willbe forgotten.
        */
        void documentClosed() {
            processPositions(false);

            doc = null;
        }

        /** Gets the document this object should work on.
         * @return docoument or null
         */
        private StyledDocument getDoc() {
            Object d = DOCUMENT.get();

            if (d instanceof StyledDocument) {
                return (StyledDocument) d;
            }

            if (d == this) {
                return null;
            }
            Reference<StyledDocument> w = this.doc;
            StyledDocument x = w == null ? null : w.get();
            return x;
        }

        /** Puts/gets positions to/from memory. It also provides full
         * pass sweep of the data structure (inlined in the code).
         * @param toMemory puts positions to memory if <code>true</code>,
         * from memory if <code>false</code> */
        private void processPositions(final boolean toMemory) {
            // clear the queue, we'll do the sweep inline anyway
            while (queue.poll() != null)
                ;

            counter = 0;

            /* pre-33165
                        synchronized(this) {
                            ChainItem previous = head;
                            ChainItem ref = previous.next;

                            while(ref != null) {
                                PositionRef pos = (PositionRef)ref.get();
                                if(pos == null) {
                                    // Remove the item from data structure.
                                    previous.next = ref.next;
                                } else {
                                    // Process the PostionRef.
                                    if(toMemory) {
                                        pos.kind = pos.kind.toMemory(pos.insertAfter);
                                    } else {
                                        pos.kind = pos.kind.fromMemory();
                                    }

                                    previous = ref;
                                }

                                ref = ref.next;
                            }
                       }
             */
            new DocumentRenderer(this, DocumentRenderer.PROCESS_POSITIONS, toMemory).render();
        }

        /** Polls queue and increases the <code>counter</code> accordingly.
         * Schedule full sweep task if counter exceedes 100. */
        private void checkQueue() {
            while (queue.poll() != null) {
                counter++;
            }

            if (counter > 100) {
                counter = 0;

                if (sweepTask == null) {
                    sweepTask = RP.post(this);
                } else if (sweepTask.isFinished()) {
                    sweepTask.schedule(0);
                }
            }
        }

        /** Implements <code>Runnable</code> interface.
         * Does full pass sweep in <code>RequestProcessor</code> thread. */
        public synchronized void run() {
            synchronized (getLock()) {
                ChainItem previous = head;
                ChainItem ref = previous.next;

                while (ref != null) {
                    if (ref.get() == null) {
                        // Remove the item from data structure.
                        previous.next = ref.next;
                    } else {
                        previous = ref;
                    }

                    ref = ref.next;
                }
            }
        }

        /** Adds the position to this manager. */
        Kind addPosition(final PositionRef pos) {
            Kind kind;

            /* pre-33165
                        synchronized(this) {
                            head.next = new ChainItem(pos, queue, head.next);

                            kind = (doc == null ?
                                        pos.kind :
                                        pos.kind.toMemory(pos.insertAfter));
                        }
             */
            kind = (Kind) new DocumentRenderer(this, DocumentRenderer.ADD_POSITION, pos).renderToObject();
            checkQueue();

            return kind;
        }

        //
        // Kinds
        //

        /** Loads the kind from the stream */
        Kind readKind(DataInput is) throws IOException {
            int offset = is.readInt();
            int line = is.readInt();
            int column = is.readInt();

            if (offset == -1) {
                // line and column must be valid
                return new LineKind(line, column, this);
            }

            if ((line == -1) || (column == -1)) {
                // offset kind
                return new OffsetKind(offset, this);
            }

            // out of memory representation
            return new OutKind(offset, line, column, this);
        }

        // #19694. Item of special data structure replacing
        // for our purposed LinkedList due to performance reasons.

        /** One item which chained instanced provides data structure
         * keeping positions for this Manager. */
        private static class ChainItem extends WeakReference<PositionRef> {
            /** Next reference keeping the position. */
            ChainItem next;

            /** Cointructs chanined item.
             * @param position <code>PositionRef</code> as referent for this
             * instance
             * @param queue <code>ReferenceQueue</code> to be used for this instance
             * @param next next chained item */
            public ChainItem(PositionRef position, ReferenceQueue<PositionRef> queue, ChainItem next) {
                super(position, queue);

                this.next = next;
            }
        }
         // End of class ChainItem.

        /** Base kind with all methods */
        private static abstract class Kind extends Object {
            protected final PositionRef.Manager mgr;

            Kind(PositionRef.Manager mgr) {
                this.mgr = mgr;
            }

            /** Offset */
            public abstract int getOffset();

            /** Get the line number */
            public abstract int getLine() throws IOException;

            /** Get the column number */
            public abstract int getColumn() throws IOException;

            /** Writes the kind to stream */
            public abstract void write(DataOutput os) throws IOException;

            /** Converts the kind to representation in memory */
            public PositionKind toMemory(boolean insertAfter) {
                /* pre-33165
                                // try to find the right position
                                Position p;
                                try {
                                    p = NbDocument.createPosition (doc, getOffset (), insertAfter ? Position.Bias.Forward : Position.Bias.Backward);
                                } catch (BadLocationException e) {
                                    p = doc.getEndPosition ();
                                }
                                return new PositionKind (p);
                 */
                return (PositionKind) new DocumentRenderer(mgr, DocumentRenderer.KIND_TO_MEMORY, this, insertAfter).renderToObject();
            }

            /** Converts the kind to representation out from memory */
            public Kind fromMemory() {
                return this;
            }
        }

        /** Kind for representing position when the document is
        * in memory.
        */
        private static final class PositionKind extends Kind {
            /** position */
            private Position pos;

            /** Constructor */
            public PositionKind(Position pos, PositionRef.Manager mgr) {
                super(mgr);
                this.pos = pos;
            }

            /** Offset */
            public int getOffset() {
                return pos.getOffset();
            }

            /** Get the line number */
            public int getLine() {
                // pre-33165                return NbDocument.findLineNumber(doc, getOffset());
                return new DocumentRenderer(mgr, DocumentRenderer.POSITION_KIND_GET_LINE, this).renderToInt();
            }

            /** Get the column number */
            public int getColumn() {
                // pre-33165                return NbDocument.findLineColumn(doc, getOffset());
                return new DocumentRenderer(mgr, DocumentRenderer.POSITION_KIND_GET_COLUMN, this).renderToInt();
            }

            /** Writes the kind to stream */
            public void write(DataOutput os) throws IOException {
                /* pre-33165
                                int offset = getOffset();
                                int line = getLine();
                                int column = getColumn();
                 */
                DocumentRenderer renderer = new DocumentRenderer(mgr, DocumentRenderer.POSITION_KIND_WRITE, this);

                int offset = renderer.renderToIntIOE();
                int line = renderer.getLine();
                int column = renderer.getColumn();

                if ((offset < 0) || (line < 0) || (column < 0)) {
                    throw new IOException(
                        "Illegal PositionKind: " + pos + "[offset=" // NOI18N
                         +offset + ",line=" // NOI18N
                         +line + ",column=" + column + "] in " // NOI18N
                         +mgr.getDoc() + " used by " + mgr.support + "." // NOI18N
                        
                    );
                }

                os.writeInt(offset);
                os.writeInt(line);
                os.writeInt(column);
            }

            /** Converts the kind to representation in memory */
            public PositionKind toMemory(boolean insertAfter) {
                return this;
            }

            /** Converts the kind to representation out from memory */
            public Kind fromMemory() {
                return new OutKind(this, mgr);
            }
        }

        /** Kind for representing position when the document is
        * out from memory. There are all infomation about the position,
        * including offset, line and column.
        */
        private static final class OutKind extends Kind {
            private int offset;
            private int line;
            private int column;

            /** Constructs the out kind from the position kind.
            */
            public OutKind(PositionKind kind, PositionRef.Manager mgr) {
                super(mgr);

                /* pre-33165
                                int offset = kind.getOffset();
                                int line = kind.getLine();
                                int column = kind.getColumn();
                 */
                DocumentRenderer renderer = new DocumentRenderer(mgr, DocumentRenderer.OUT_KIND_CONSTRUCTOR, kind);

                int offset = renderer.renderToInt();
                int line = renderer.getLine();
                int column = renderer.getColumn();

                if ((offset < 0) || (line < 0) || (column < 0)) {
                    throw new IndexOutOfBoundsException(
                        "Illegal OutKind[offset=" // NOI18N
                         +offset + ",line=" // NOI18N
                         +line + ",column=" + column + "] in " // NOI18N
                         +mgr.getDoc() + " used by " + mgr.support + "." // NOI18N
                        
                    );
                }

                this.offset = offset;
                this.line = line;
                this.column = column;
            }

            /** Constructs the out kind.
            */
            OutKind(int offset, int line, int column, PositionRef.Manager mgr) {
                super(mgr);
                this.offset = offset;
                this.line = line;
                this.column = column;
            }

            /** Offset */
            public int getOffset() {
                return offset;
            }

            /** Get the line number */
            public int getLine() {
                return line;
            }

            /** Get the column number */
            public int getColumn() {
                return column;
            }

            /** Writes the kind to stream */
            public void write(DataOutput os) throws IOException {
                if ((offset < 0) || (line < 0) || (column < 0)) {
                    throw new IOException(
                        "Illegal OutKind[offset=" // NOI18N
                         +offset + ",line=" // NOI18N
                         +line + ",column=" + column + "] in " // NOI18N
                         +mgr.getDoc() + " used by " + mgr.support + "." // NOI18N
                        
                    );
                }

                os.writeInt(offset);
                os.writeInt(line);
                os.writeInt(column);
            }
        }
         // OutKind

        /** Kind for representing position when the document is
        * out from memory. Represents only offset in the document.
        */
        private static final class OffsetKind extends Kind {
            private int offset;

            /** Constructs the out kind from the position kind.
            */
            public OffsetKind(int offset, PositionRef.Manager mgr) {
                super(mgr);

                if (offset < 0) {
                    throw new IndexOutOfBoundsException(
                        "Illegal OffsetKind[offset=" // NOI18N
                         +offset + "] in " + mgr.getDoc() + " used by " // NOI18N
                         +mgr.support + "." // NOI18N
                        
                    );
                }

                this.offset = offset;
            }

            /** Offset */
            public int getOffset() {
                return offset;
            }

            /** Get the line number */
            public int getLine() throws IOException {
                // pre-33165                return NbDocument.findLineNumber(getCloneableEditorSupport().openDocument(), offset);
                mgr.getCloneableEditorSupport().openDocument(); // make sure document is fully read

                return new DocumentRenderer(mgr, DocumentRenderer.OFFSET_KIND_GET_LINE, this, offset).renderToIntIOE();
            }

            /** Get the column number */
            public int getColumn() throws IOException {
                // pre-33165                return NbDocument.findLineColumn (getCloneableEditorSupport().openDocument(), offset);
                mgr.getCloneableEditorSupport().openDocument(); // make sure document fully read

                return new DocumentRenderer(mgr, DocumentRenderer.OFFSET_KIND_GET_COLUMN, this, offset).renderToIntIOE();
            }

            /** Writes the kind to stream */
            public void write(DataOutput os) throws IOException {
                if (offset < 0) {
                    throw new IOException(
                        "Illegal OffsetKind[offset=" // NOI18N
                         +offset + "] in " + mgr.getDoc() + " used by " // NOI18N
                         +mgr.support + "." // NOI18N
                        
                    );
                }

                os.writeInt(offset);
                os.writeInt(-1);
                os.writeInt(-1);
            }
        }

        /** Kind for representing position when the document is
        * out from memory. Represents only line and column in the document.
        */
        private static final class LineKind extends Kind {
            private int line;
            private int column;

            /** Constructor.
            */
            public LineKind(int line, int column, PositionRef.Manager mgr) {
                super(mgr);

                if ((line < 0) || (column < 0)) {
                    throw new IndexOutOfBoundsException(
                        "Illegal LineKind[line=" // NOI18N
                         +line + ",column=" + column + "] in " // NOI18N
                         +mgr.getDoc() + " used by " + mgr.support + "." // NOI18N
                        
                    );
                }

                this.line = line;
                this.column = column;
            }

            /** Offset */
            public int getOffset() {
                /* pre-33165
                                try {
                                    StyledDocument doc = getCloneableEditorSupport().getDocument();
                                    if (doc == null) {
                                        doc = getCloneableEditorSupport().openDocument();
                                    }
                                    return NbDocument.findLineOffset (doc, line) + column;
                                } catch (IOException e) {
                                    // what to do? hopefully unlikelly
                                    return 0;
                                }
                 */
                try {
                    StyledDocument doc = mgr.getCloneableEditorSupport().getDocument();

                    if (doc == null) {
                        doc = mgr.getCloneableEditorSupport().openDocument();
                    }

                    int retOffset = new DocumentRenderer(
                            mgr, DocumentRenderer.LINE_KIND_GET_OFFSET, this, line, column, doc
                        ).renderToInt();

                    return retOffset;
                } catch (IOException e) {
                    // what to do? hopefully unlikelly
                    return 0;
                }
            }

            /** Get the line number */
            public int getLine() throws IOException {
                return line;
            }

            /** Get the column number */
            public int getColumn() throws IOException {
                return column;
            }

            /** Writes the kind to stream */
            public void write(DataOutput os) throws IOException {
                if ((line < 0) || (column < 0)) {
                    throw new IOException(
                        "Illegal LineKind[line=" // NOI18N
                         +line + ",column=" + column + "] in " // NOI18N
                         +mgr.getDoc() + " used by " + mgr.support + "." // NOI18N
                        
                    );
                }

                os.writeInt(-1);
                os.writeInt(line);
                os.writeInt(column);
            }

            /** Converts the kind to representation in memory */
            public PositionKind toMemory(boolean insertAfter) {
                /* pre-33165
                                // try to find the right position
                                Position p;
                                try {
                                    p = NbDocument.createPosition (doc, NbDocument.findLineOffset (doc, line) + column, insertAfter ? Position.Bias.Forward : Position.Bias.Backward);
                                } catch (BadLocationException e) {
                                    p = doc.getEndPosition ();
                                }
                 */
                Position p = (Position) new DocumentRenderer(
                        mgr, DocumentRenderer.LINE_KIND_TO_MEMORY, this, line, column, insertAfter
                    ).renderToObject();

                return new PositionKind(p, mgr);
            }
        }

        /**
         * Helper class ensuring that critical parts will run under document's read lock
         * by using {@link javax.swing.text.Document#render(Runnable)}.
         */
        private static final class DocumentRenderer implements Runnable {
            private static final int KIND_TO_MEMORY = 0;
            private static final int POSITION_KIND_GET_LINE = KIND_TO_MEMORY + 1;
            private static final int POSITION_KIND_GET_COLUMN = POSITION_KIND_GET_LINE + 1;
            private static final int POSITION_KIND_WRITE = POSITION_KIND_GET_COLUMN + 1;
            private static final int OUT_KIND_CONSTRUCTOR = POSITION_KIND_WRITE + 1;
            private static final int OFFSET_KIND_GET_LINE = OUT_KIND_CONSTRUCTOR + 1;
            private static final int OFFSET_KIND_GET_COLUMN = OFFSET_KIND_GET_LINE + 1;
            private static final int LINE_KIND_GET_OFFSET = OFFSET_KIND_GET_COLUMN + 1;
            private static final int LINE_KIND_TO_MEMORY = LINE_KIND_GET_OFFSET + 1;
            private static final int PROCESS_POSITIONS = LINE_KIND_TO_MEMORY + 1;
            private static final int ADD_POSITION = PROCESS_POSITIONS + 1;
            private final Manager mgr;
            private final int opCode;
            private Kind argKind;
            private boolean argInsertAfter;
            private boolean argToMemory;
            private int argInt;
            private Object retObject;
            private int retInt;
            private int argLine;
            private int argColumn;
            private PositionRef argPos;
            private StyledDocument argDoc;
            private IOException ioException;

            DocumentRenderer(Manager mgr, int opCode, Kind argKind) {
                this.mgr = mgr;
                this.opCode = opCode;
                this.argKind = argKind;
            }

            DocumentRenderer(Manager mgr, int opCode, Kind argKind, boolean argInsertAfter) {
                this(mgr, opCode, argKind);
                this.argInsertAfter = argInsertAfter;
            }

            DocumentRenderer(Manager mgr, int opCode, Kind argKind, int argInt) {
                this(mgr, opCode, argKind);
                this.argInt = argInt;
            }

            DocumentRenderer(Manager mgr, int opCode, Kind argKind, int argLine, int argColumn) {
                this(mgr, opCode, argKind);
                this.argLine = argLine;
                this.argColumn = argColumn;
            }

            DocumentRenderer(Manager mgr, int opCode, Kind argKind, int argLine, int argColumn, StyledDocument argDoc) {
                this(mgr, opCode, argKind, argLine, argColumn);
                this.argDoc = argDoc;
            }

            DocumentRenderer(Manager mgr, int opCode, Kind argKind, int argLine, int argColumn, boolean argInsertAfter) {
                this(mgr, opCode, argKind, argLine, argColumn);
                this.argInsertAfter = argInsertAfter;
            }

            DocumentRenderer(Manager mgr, int opCode, boolean toMemory) {
                this.mgr = mgr;
                this.opCode = opCode;
                this.argToMemory = toMemory;
            }

            DocumentRenderer(Manager mgr, int opCode, PositionRef argPos) {
                this.mgr = mgr;
                this.opCode = opCode;
                this.argPos = argPos;
            }

            void render() {
                StyledDocument d = mgr.getDoc();
                Object prev = DOCUMENT.get();

                try {
                    if (d != null) {
                        DOCUMENT.set(d);
                        d.render(this);
                    } else {
                        DOCUMENT.set(mgr);
                        this.run();
                    }
                } finally {
                    DOCUMENT.set(prev);
                }
            }

            Object renderToObjectIOE() throws IOException {
                Object o = renderToObject();

                if (ioException != null) {
                    throw ioException;
                }

                return o;
            }

            Object renderToObject() {
                render();

                return retObject;
            }

            int renderToIntIOE() throws IOException {
                int i = renderToInt();

                if (ioException != null) {
                    throw ioException;
                }

                return i;
            }

            int renderToInt() {
                render();

                return retInt;
            }

            int getLine() {
                return argLine;
            }

            int getColumn() {
                return argColumn;
            }

            public void run() {
                try {
                    switch (opCode) {
                    case KIND_TO_MEMORY: {
                        // try to find the right position
                        Position p;

                        int offset = argKind.getOffset();

                        // #33165
                        // Try to use line:column instead
                        // Following code can be commented out to retain old behavior
                        if (argKind.getClass() == OutKind.class) {
                            try {
                                int line = argKind.getLine();
                                int col = argKind.getColumn();
                                Element lineRoot = NbDocument.findLineRootElement(mgr.getDoc());

                                if (line < lineRoot.getElementCount()) {
                                    Element lineElem = lineRoot.getElement(line);
                                    int lineStartOffset = lineElem.getStartOffset();
                                    int lineLen = lineElem.getEndOffset() - lineStartOffset;

                                    if (lineLen >= 1) { // should always be at least '\n'
                                        col = Math.min(col, lineLen - 1);
                                        offset = lineStartOffset + col;
                                    }
                                }
                            } catch (IOException e) {
                                // use offset in that case
                            }
                        }

                        try {
                            p = NbDocument.createPosition(
                                    mgr.getDoc(), offset,
                                    argInsertAfter ? Position.Bias.Backward : Position.Bias.Forward
                                );
                        } catch (BadLocationException e) {
                            p = mgr.getDoc().getEndPosition();
                        }

                        retObject = new PositionKind(p, mgr);

                        break;
                    }

                    case POSITION_KIND_GET_LINE: {
                        retInt = NbDocument.findLineNumber(mgr.getDoc(), argKind.getOffset());

                        break;
                    }

                    case POSITION_KIND_GET_COLUMN: {
                        retInt = NbDocument.findLineColumn(mgr.getDoc(), argKind.getOffset());

                        break;
                    }

                    case POSITION_KIND_WRITE:
                    case OUT_KIND_CONSTRUCTOR: {
                        retInt = argKind.getOffset();
                        argLine = argKind.getLine();
                        argColumn = argKind.getColumn();

                        break;
                    }

                    case OFFSET_KIND_GET_LINE: {
                        retInt = NbDocument.findLineNumber(mgr.getCloneableEditorSupport().openDocument(), argInt);

                        break;
                    }

                    case OFFSET_KIND_GET_COLUMN: {
                        retInt = NbDocument.findLineColumn(mgr.getCloneableEditorSupport().openDocument(), argInt);

                        break;
                    }

                    case LINE_KIND_GET_OFFSET: {
                        retInt = NbDocument.findLineOffset(argDoc, argLine) + argColumn;

                        break;
                    }

                    case LINE_KIND_TO_MEMORY: {
                        // try to find the right position
                        try {
                            retObject = NbDocument.createPosition(
                                    mgr.getDoc(), NbDocument.findLineOffset(mgr.getDoc(), argLine) + argColumn,
                                    argInsertAfter ? Position.Bias.Backward : Position.Bias.Forward
                                );
                        } catch (BadLocationException e) {
                            retObject = mgr.getDoc().getEndPosition();
                        } catch (IndexOutOfBoundsException e) {
                            retObject = mgr.getDoc().getEndPosition();
                        }

                        break;
                    }

                    case PROCESS_POSITIONS: {
                        synchronized (mgr.getLock()) {
                            ChainItem previous = mgr.head;
                            ChainItem ref = previous.next;

                            while (ref != null) {
                                PositionRef pos = ref.get();

                                if (pos == null) {
                                    // Remove the item from data structure.
                                    previous.next = ref.next;
                                } else {
                                    // Process the PostionRef.
                                    if (argToMemory) {
                                        pos.kind = pos.kind.toMemory(pos.insertAfter);
                                    } else {
                                        pos.kind = pos.kind.fromMemory();
                                    }

                                    previous = ref;
                                }

                                ref = ref.next;
                            }
                        }

                        break;
                    }

                    case ADD_POSITION: {
                        // [pnejedly] these are testability hooks
                        mgr.support.howToReproduceDeadlock40766(true);

                        synchronized (mgr.getLock()) {
                            mgr.support.howToReproduceDeadlock40766(false);
                            mgr.head.next = new ChainItem(argPos, mgr.queue, mgr.head.next);

                            retObject = ((mgr.getDoc() == null) ? argPos.kind : argPos.kind.toMemory(
                                    argPos.insertAfter
                                ));
                        }

                        break;
                    }

                    default:
                        throw new IllegalStateException(); // Unknown opcode
                    }
                } catch (IOException e) {
                    ioException = e;
                }
            }
        }
    }
}

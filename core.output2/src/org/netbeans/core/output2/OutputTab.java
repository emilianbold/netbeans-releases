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

import java.awt.Component;
import java.awt.Point;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.core.output2.ui.AbstractOutputPane;
import org.netbeans.core.output2.ui.AbstractOutputTab;

/**
 * A component representing one tab in the output window.
 */
final class OutputTab extends AbstractOutputTab {
    private NbIO io;

    OutputTab (NbIO io) {
        this.io = io;
        if (Controller.log) Controller.log ("Created an output component for " + io);
        OutputDocument doc = new OutputDocument (((NbWriter) io.getOut()).out());
        setDocument (doc);
    }

    public void addNotify() {
        super.addNotify();
        if (io != null) io.setClosed(false);
    }

    public void removeNotify() {
        if (io != null) io.setClosed(true);
        super.removeNotify();
    }

    public void setDocument (Document doc) {
        if (Controller.log) Controller.log ("Set document on " + this + " with " + io);
        assert SwingUtilities.isEventDispatchThread();
        Document old = getDocument();
        hasOutputListeners = false;
        firstNavigableListenerLine = -1;
        super.setDocument(doc);
        if (old != null && old instanceof OutputDocument) {
            ((OutputDocument) old).dispose();
        }
    }

    public void setIO (NbIO io) {
        if (Controller.log) Controller.log ("Replacing io on " + this + " with " + io + " out is " + (io != null ? io.getOut() : null));
        if (io != null) {
            setDocument (new OutputDocument(((NbWriter) io.getOut()).out()));
            io.setClosed(false);
        } else {
            if (this.io != null) this.io.setClosed(true);
            this.io = null;
            setDocument(null);
        }
    }

    public OutputDocument getDocument() {
        Document d = getOutputPane().getDocument();
        if (d instanceof OutputDocument) {
            return (OutputDocument) d;
        }
        return null;
    }

    protected AbstractOutputPane createOutputPane() {
        return new OutputPane();
    }

    protected void inputSent(String txt) {
        if (Controller.log) Controller.log("Input sent on OutputTab: " + txt);
        getOutputPane().lockScroll();
        findOutputWindow().inputSent(this, txt);
    }

    protected void inputEof() {
        if (Controller.log) Controller.log ("Input EOF on OutputTab: ");
        findOutputWindow().inputEof(this);
    }

    public void hasSelectionChanged(boolean val) {
        OutputWindow win = findOutputWindow();
        if (win != null) {
            win.hasSelectionChanged(this, val);
        }
    }

    public NbIO getIO() {
        return io;
    }

    private long timestamp = 0;
    void updateTimestamp() {
        timestamp = System.currentTimeMillis();
    }

    long getTimestamp() {
        return timestamp;
    }

    private OutputWindow findOutputWindow() {
        if (getParent() != null) {
            return (OutputWindow) SwingUtilities.getAncestorOfClass(OutputWindow.class, this);
        } else {
            return (OutputWindow) getClientProperty ("outputWindow"); //NOI18N
        }
    }
    
    void requestActive() {
        findOutputWindow().requestActive();
    }

    public void lineClicked(int line) {
        findOutputWindow().lineClicked (this, line);
    }
    
    boolean linePressed (int line, Point p) {
        OutWriter out = getIO().out();
        if (out != null) {
            return out.getLines().getListenerForLine(line) != null;
        } else {
            return false;
        }
    }    

    public void postPopupMenu(Point p, Component src) {
        findOutputWindow().postPopupMenu(this, p, src);
    }

    public void caretEnteredLine(int line) {
        findOutputWindow().caretEnteredLine(this, line);
    }
    
    private int firstNavigableListenerLine = -1;
    /**
     * Do not unlock scrollbar unless there is a bona-fide error to 
     * show - deprecation warnings should be ignored.
     */
    public int getFirstNavigableListenerLine() {
        if (firstNavigableListenerLine != -1) {
            return firstNavigableListenerLine;
        }
        
        int result = -1;
        OutWriter out = io.out();
        if (out != null) {
            if (Controller.log) Controller.log ("Looking for first appropriate" +
                " listener line to send the caret to");
            result = out.getLines().firstImportantListenerLine();
        }
        return result;
    }
    
    public String toString() {
        return "OutputTab@" + System.identityHashCode(this) + " for " + io;
    }

    private boolean hasOutputListeners = false;
    public void documentChanged() {
        OutputWindow win = findOutputWindow();
        if (win != null) {
            boolean hadOutputListeners = hasOutputListeners;
            if (getFirstNavigableListenerLine() == -1) {
                return;
            }
            hasOutputListeners = getIO().out() != null && getIO().out().getLines().firstListenerLine() >= 0;
            if (hasOutputListeners != hadOutputListeners) {
                win.hasOutputListenersChanged(this, hasOutputListeners);
            }
            win.documentChanged(this);
        }
    }

    /**
     * Determine if the new caret position is close enough that the scrollbar should be re-locked
     * to the end of the document.
     *
     * @param dot The caret position
     * @return if it should be locked
     */
    public boolean shouldRelock(int dot) {
        if (io != null) {
            OutWriter w = io.out();
            if (w != null && !w.isClosed()) {
                int dist =  Math.abs(w.getLines().getCharCount() - dot);
                return dist < 100;
            }
        }
        return false;
    }
}

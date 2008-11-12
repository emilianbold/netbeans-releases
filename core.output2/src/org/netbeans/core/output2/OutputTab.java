/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.core.output2;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import javax.swing.Action;
import javax.swing.JComponent;
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
        if (Controller.LOG) Controller.log ("Created an output component for " + io);
        OutputDocument doc = new OutputDocument (((NbWriter) io.getOut()).out());
        setDocument (doc);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (io != null) io.setClosed(false);
    }

    @Override
    public void removeNotify() {
        if (io != null) io.setClosed(true);
        super.removeNotify();
    }

    @Override
    public void setDocument (Document doc) {
        if (Controller.LOG) Controller.log ("Set document on " + this + " with " + io);
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
        if (Controller.LOG) Controller.log ("Replacing io on " + this + " with " + io + " out is " + (io != null ? io.getOut() : null));
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
        if (Controller.LOG) Controller.log("Input sent on OutputTab: " + txt);
        getOutputPane().lockScroll();
        findOutputWindow().inputSent(this, txt);
    }

    protected void inputEof() {
        if (Controller.LOG) Controller.log ("Input EOF on OutputTab: ");
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
        OutputWindow ow = findOutputWindow();
        if (ow != null) {
            ow.caretEnteredLine(this, line);
        }
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
            if (Controller.LOG) Controller.log ("Looking for first appropriate" +
                " listener line to send the caret to");
            result = out.getLines().firstImportantListenerLine();
        }
        return result;
    }
    
    @Override
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
    
    ActionListener getFindActionListener(Action next, Action prev, Action copy) {
        if (findActionListener == null) {
            findActionListener = new FindActionListener(this, next, prev, copy);
        }
        return findActionListener;
    }
    
    private ActionListener findActionListener;
    
    /**
     * An action listener which listens to the default button of the find
     * dialog.
     */
    static class FindActionListener implements ActionListener {
        OutputTab tab;
        Action findNextAction;
        Action findPreviousAction;
        Action copyAction;
        FindActionListener(OutputTab tab, Action findNextAction, Action findPreviousAction, Action copyAction) {
            this.tab = tab;
            this.findNextAction = findNextAction;
            this.findPreviousAction = findPreviousAction;
            this.copyAction = copyAction;
        }

        public void actionPerformed(ActionEvent e) {
            FindDialogPanel panel = (FindDialogPanel)
                SwingUtilities.getAncestorOfClass(FindDialogPanel.class,
                (JComponent) e.getSource());
            if (panel == null) {
                //dialog disposed
                panel = (FindDialogPanel) ((JComponent)
                    e.getSource()).getClientProperty("panel"); //NOI18N
            }

            String s = panel.getPattern();
            if (s == null || s.length() == 0) {
                Toolkit.getDefaultToolkit().beep();

                return;
            }
            OutWriter out = tab.getIO().out();
            if (out != null && !out.isDisposed()) {
                Matcher matcher = out.getLines().find(s);
                if (matcher != null && matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    tab.getOutputPane().setSelection(start, end);
                    findNextAction.setEnabled(true);
                    findPreviousAction.setEnabled(true);
                    copyAction.setEnabled(true);
                    panel.getTopLevelAncestor().setVisible(false);
                    tab.requestFocus();
                }
            }
        }
    }    
}

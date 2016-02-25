/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.env;

import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.netbeans.modules.jshell.model.ConsoleEvent;
import org.netbeans.modules.jshell.model.ConsoleListener;
import org.netbeans.modules.jshell.model.ConsoleModel;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.model.Rng;
import org.netbeans.modules.jshell.support.ShellSession;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author sdedic
 */
@TopComponent.Description(
        preferredID = "REPLTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "org.netbeans.modules.java.repl.REPLTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@NbBundle.Messages({
    "CTL_REPLAction=Java REPL",
    "CTL_REPLTopComponent=Java REPL",
    "CTL_REPLTopComponentProject=Java REPL for {0}",
    "HINT_REPLTopComponent=This is a Java REPL window"
})
public class ConsoleEditor extends CloneableEditor {

    private ShellSession session;
    private ConsoleListener cl = new CL();
    
    public ConsoleEditor(CloneableEditorSupport support) {
        super(support);
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
    }
    
    private volatile JEditorPane pane;

    @Override
    protected void componentShowing() {
        super.componentShowing();
        if (session == null) {
            initialize();
            if (session == null) {
                pane.addPropertyChangeListener("document", 
                        (e) -> initialize());
            }
        }
        if (session != null) {
            session.getIO().show();
        }
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        pane = null;
        if (cloneableEditorSupport().getOpenedPanes() == null && session != null) {
            try {
                // terminate the JShell
                session.closeSession();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private void initialize() {
        Document d = getEditorPane().getDocument();
        ShellSession s  = ShellSession.get(d);
        if (s == null || s == this.session) {
            // some default document, not interesting
            return;
        }
        if (s != this.session && this.session != null) {
            this.session.getModel().removeConsoleListener(cl);
        }
        this.session = s;
        
        session.getModel().addConsoleListener(cl);
        // post in order to synchronize after initial shell startup
        session.post(() -> {
            try {
                resetEditableArea(true);
            } catch (BadLocationException ex) {
            }
        });
        
        (pane = getEditorPane()).setNavigationFilter(new NavFilter());
    }

    private void resetEditableArea(boolean caret) throws BadLocationException {
        if (pane == null || pane.getDocument() == null) {
            // probably closed
            return;
        }
        final LineDocument ld = LineDocumentUtils.as(pane.getDocument(), LineDocument.class);
        if (ld == null) {
            return;
        }
        ConsoleModel model = session.getModel();
        ConsoleSection input = model.getInputSection();
        if (input != null) {
            int commandStart = input.getPartBegin();
//            pane.setCaretPosition(commandStart);
            Position s = ld.createPosition(commandStart, Position.Bias.Backward);
            Position e = ld.createPosition(ld.getLength(), Position.Bias.Forward);
            PositionRegion editRegion = new PositionRegion(
                    s, e
            );
//            pane.putClientProperty(OverrideEditorActions.PROP_NAVIGATE_BOUNDARIES, 
//                    editRegion);
            if (caret) {
                SwingUtilities.invokeLater(() -> pane.setCaretPosition(s.getOffset()));
            }
        }
    }
    
    /**
     * Modifies the movement of the caret ot strongly prefer movement within the
     * editable section.
     * If the caret is currently in non-editable section of the console, the filter does nothing
     * When in editable section, and the filter would escape it (either leave input section
     * entirely, or go to a prompt part of it), the filter will catch the caret and
     * <ul>
     * <li>if the caret is already at the first or last row of the input section, pass the navigation on.
     * <li>If the caret goes up or down, magicPosition is used to compute offset at the first/last
     * line of the input section; caret will go to that place.
     * </ul>
     * <ul>
     * <li>if the caret would enter the prompt but remain on the line (i.e. left), the caret will be moved at
     * the end of the preceding line, as if movement was made at the beginning of line.
     * <li>if the caret goes to the start of prompt (begin-line), nothing happens.
     * </ul>
     */
    private class NavFilter extends NavigationFilter {

        @Override
        public void moveDot(FilterBypass fb, int dot, Position.Bias bias) {
            if (handle(fb, dot, bias, true)) {
                super.moveDot(fb, dot, bias);
            }
        }
        
        private void setMoveDot(FilterBypass fb, int dot, Position.Bias bias, boolean setOrMove) {
            if (setOrMove) {
                super.moveDot(fb, dot, bias);
            } else {
                super.setDot(fb, dot, bias);
            }
        }
        
        private boolean handle(FilterBypass fb, int dot, Position.Bias bias, boolean setOrMove) {
            // sadly actions must set up a flag in order to be 'compatible':
            JEditorPane p = pane;
            if (p == null || p.getClientProperty("navigational.action") == null) {
                return true;
            }
            int current = fb.getCaret().getDot();
            ConsoleSection input = session.getModel().getInputSection();
            if (input == null) {
                return true;
            }
            int s = input.getStart();
            int e = input.getEnd();

            try {
                if (current >= s && current <= e) {
                    // move from within the area
                    if (dot >= s && dot <= e) {
                        return filterWithinSection(input, fb, dot, bias, setOrMove);
                    } else {
                        return filterOutOfSection(input, fb, dot, bias, setOrMove);
                    }
                }
                return true;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                // permit to navigate
                return true;
            }
        }
        
        /**
         * Handles movement within section. Essentialy just ensures that the caret remains after the prompts.
         * @param fb
         * @param dot
         * @param bias
         * @param setOrMove
         * @return B
         */
        private boolean filterWithinSection(ConsoleSection s, FilterBypass fb, int dot, Position.Bias bias, boolean setOrMove) {
            Caret c = fb.getCaret();
            int curPos = c.getDot();
            LineDocument ld = LineDocumentUtils.as(getEditorPane().getDocument(), LineDocument.class);
            if (ld == null) {
                return true;
            }
            int curLine =  LineDocumentUtils.getLineStart(ld, c.getDot());
            int dotLine =  LineDocumentUtils.getLineStart(ld, dot);
            // I want to avoid positioning into the prompts:
            for (Rng range : s.getPartRanges()) {
                if (range.start > dot) {
                    if (curLine == dotLine) {
                        // if the previous position was also in this range, just relax -
                        // positioned by e.g. mouse, let's do any movement necessary:
                        if (curPos == curLine) {
                            // exception, go to start
                            setMoveDot(fb, range.start, bias, setOrMove);
                            return false;
                        }
                        if (curPos < range.start) {
                            return true;
                        }
                    }
                    if (dot == dotLine) { 
                        // jump at the beginning of the line, assuming HOME
                        if (curPos == range.start) {
                            return true;
                        } else {
                            setMoveDot(fb, range.start, bias, setOrMove);
                            return false;
                        }
                    } else if (dotLine == curLine) {
                        // assuming LEFT, WORD-LEFT etc
                        setMoveDot(fb, Math.max(0, dot = dotLine - 1), bias, setOrMove);
                        return false;
                    }
                    setMoveDot(fb, range.start, bias, setOrMove);
                    return false;
                }
                if (range.end >= dot) {
                    break;
                }
            }
            return true;
        }

        private boolean filterOutOfSection(ConsoleSection s, FilterBypass fb, int dot, Position.Bias bias, boolean setOrMove) throws BadLocationException {
            Caret c = fb.getCaret();
            Point magPosition = c.getMagicCaretPosition();
            int curPos = c.getDot();
            if (magPosition == null) {
                // some other action that move-up / move-down, i.e. start of document / end document, navigation
                if (curPos == s.getPartBegin()|| curPos == s.getEnd()) {
                    return true;
                }
                int nDot;
                if (dot < s.getPartBegin()&& curPos != s.getStart()) {
                    nDot = s.getPartBegin();
                } else if (dot > s.getEnd() && curPos != s.getEnd()) {
                    nDot = s.getEnd();
                } else {
                    return true;
                }
                setMoveDot(fb, nDot, bias, setOrMove);
                return false;
            }
            
            // magicPosition is set, so the move is accross the lines (in Y axis)
            LineDocument ld = LineDocumentUtils.as(getEditorPane().getDocument(), LineDocument.class);
            if (ld == null) {
                return true;
            }
            int curLine =  LineDocumentUtils.getLineStart(ld, c.getDot());
            int ref;
            if (dot < s.getStart()) {
                // measure Y; get X from magicPosition.
                ref  = LineDocumentUtils.getLineStart(ld, s.getStart());
            } else if (dot >= s.getEnd()) {
                ref  = LineDocumentUtils.getLineStart(ld, s.getEnd());
            } else {
                return true;
            }
            if (curLine == ref) {
                return true;
            }
            Rectangle rect = getEditorPane().getUI().modelToView(getEditorPane(), ref);
            rect.x = magPosition.x;
            int pos = getEditorPane().getUI().viewToModel(getEditorPane(), rect.getLocation());
            if (pos < 0) {
                return true;
            }
            // I want to avoid positioning into the prompts:
            for (Rng range : s.getPartRanges()) {
                if (range.start > pos) {
                    pos = range.start;
                    break;
                }
                if (range.end >= pos) {
                    break;
                }
            }
            setMoveDot(fb, pos, bias, setOrMove);
            return false;
        }

        @Override
        public void setDot(FilterBypass fb, int dot, Position.Bias bias) {
            if (handle(fb, dot, bias, false)) {
                super.setDot(fb, dot, bias);
            }
        }
        
    }
    
    private class CL implements ConsoleListener, Runnable {
        private boolean caret;
        
        public void run() {
            try {
                resetEditableArea(caret);
            } catch (BadLocationException ex) {
                // should not happen
                Exceptions.printStackTrace(ex);
            }
        }
        
        @Override
        public void sectionCreated(ConsoleEvent e) {
            if (e.containsInput()) {
                caret = true;
                SwingUtilities.invokeLater(this);
            }
        }

        @Override
        public void sectionUpdated(ConsoleEvent e) {
            if (e.containsInput()) {
                caret = false;
                SwingUtilities.invokeLater(this);
            }
        }
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
}

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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.Document;
import org.netbeans.core.output2.Controller.ControllerOutputEvent;
import org.netbeans.core.output2.ui.AbstractOutputPane;
import org.netbeans.core.output2.ui.AbstractOutputTab;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.FindAction;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.IOContainer;
import org.openide.windows.OutputListener;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;

/**
 * A component representing one tab in the output window.
 */
final class OutputTab extends AbstractOutputTab implements IOContainer.CallBacks {
    private final NbIO io;

    OutputTab(NbIO io) {
        this.io = io;
        if (Controller.LOG) Controller.log ("Created an output component for " + io);
        OutputDocument doc = new OutputDocument (((NbWriter) io.getOut()).out());
        setDocument(doc);

        installKBActions();
        getActionMap().put("jumpPrev", this.prevErrorAction); // NOI18N
        getActionMap().put("jumpNext", this.nextErrorAction); // NOI18N
        getActionMap().put(FindAction.class.getName(), this.findAction);
        getActionMap().put(javax.swing.text.DefaultEditorKit.copyAction, this.copyAction);
    }

    private void installKBActions() {
        installKeyboardAction(copyAction);
        installKeyboardAction(selectAllAction);
        installKeyboardAction(findAction);
        installKeyboardAction(findNextAction);
        installKeyboardAction(findPreviousAction);
        installKeyboardAction(wrapAction);
        installKeyboardAction(largerFontAction);
        installKeyboardAction(smallerFontAction);
        installKeyboardAction(saveAsAction);
        installKeyboardAction(closeAction);
        installKeyboardAction(copyAction);
        installKeyboardAction(navToLineAction);
        installKeyboardAction(postMenuAction);
        installKeyboardAction(clearAction);
        installKeyboardAction(filterAction);
    }

    @Override
    public void setDocument (Document doc) {
        if (Controller.LOG) Controller.log ("Set document on " + this + " with " + io);
        assert SwingUtilities.isEventDispatchThread();
        Document old = getDocument();
        hasOutputListeners = false;
        super.setDocument(doc);
        if (old != null && old instanceof OutputDocument) {
            ((OutputDocument) old).dispose();
        }
    }

    public void reset() {
        if (origPane != null) {
            setFilter(null, false, false);
        }
        setDocument(new OutputDocument(((NbWriter) io.getOut()).out()));
    }

    public OutputDocument getDocument() {
        Document d = getOutputPane().getDocument();
        if (d instanceof OutputDocument) {
            return (OutputDocument) d;
        }
        return null;
    }

    protected AbstractOutputPane createOutputPane() {
        return new OutputPane(this);
    }

    protected void inputSent(String txt) {
        if (Controller.LOG) Controller.log("Input sent on OutputTab: " + txt);
        getOutputPane().lockScroll();
        NbIO.IOReader in = io.in();
        if (in != null) {
            if (Controller.LOG) Controller.log("Sending input to " + in);
            in.pushText(txt + "\n");
            io.getOut().println(txt);
        }
    }

    protected void inputEof() {
        if (Controller.LOG) Controller.log ("Input EOF");
        NbIO.IOReader in = io.in();
        if (in != null) {
            in.eof();
        }
    }

    public void hasSelectionChanged(boolean val) {
        if (isShowing()) {
            copyAction.setEnabled(val);
            selectAllAction.setEnabled(!getOutputPane().isAllSelected());
        }
    }

    public NbIO getIO() {
        return io;
    }
    
    void requestActive() {
        io.getIOContainer().requestActive();
    }

    public void lineClicked(int line, int pos) {
        OutWriter out = getOut();
        if (out == null) {
            return;
        }
        int range[] = new int[2];
        OutputListener l = out.getLines().getListener(pos, range);
        if (l != null) {
            ControllerOutputEvent oe = new ControllerOutputEvent(io, out, line);
            l.outputLineAction(oe);
            //Select the text on click
            getOutputPane().sendCaretToPos(range[0], range[1], true);
        }
    }

    @Override
    public String toString() {
        return "OutputTab@" + System.identityHashCode(this) + " for " + io;
    }

    private boolean hasOutputListeners = false;

    public void documentChanged(OutputPane pane) {
        if (filtOut != null && pane == origPane) {
            filtOut.readFrom(io.out());
        }
        boolean hadOutputListeners = hasOutputListeners;
        hasOutputListeners = getOut() != null && getOut().getLines().firstListenerLine() >= 0;
        if (hasOutputListeners != hadOutputListeners) {
            hasOutputListenersChanged(hasOutputListeners);
        }

        IOContainer ioContainer = io.getIOContainer();
        if (io.isFocusTaken()) {
            ioContainer.select(this);
            ioContainer.requestVisible();
        }
        Controller.getDefault().updateName(this);
        if (this == ioContainer.getSelected() && ioContainer.isActivated()) {
            updateActions();
        }
    }

    /**
     * Called when the output stream has been closed, to navigate to the
     * first line which shows an error (if any).
     */
    private void navigateToFirstErrorLine() {
        OutWriter out = getOut();
        if (out != null) {
            int line = out.getLines().firstImportantListenerLine();
            if (Controller.LOG) {
                Controller.log("NAV TO FIRST LISTENER LINE: " + line);
            }
            if (line >= 0) {
                getOutputPane().sendCaretToLine(line, false);
                if (isSDI()) {
                    requestActive();
                }
            }
        }
    }

    private boolean isSDI() {
        Container c = getTopLevelAncestor();
        return (c != WindowManager.getDefault().getMainWindow());
    }

     void hasOutputListenersChanged(boolean hasOutputListeners) {
        if (hasOutputListeners && getOutputPane().isScrollLocked()) {
            navigateToFirstErrorLine();
        }
    }

    public void activated() {
        updateActions();
    }

    public void closed() {
        io.setClosed(true);
        io.setStreamClosed(true);
        Controller.getDefault().removeTab(io);
        NbWriter w = io.writer();
        if (w != null && w.isClosed()) {
            //Will dispose the document
            setDocument(null);
        } else if (w != null) {
            //Something is still writing to the stream, but we're getting rid of the tab.  Don't dispose
            //the writer, just kill the tab's document
            getDocument().disposeQuietly();
        }
    }

    public void deactivated() {
    }

    public void selected() {
    }

    /**
     * Determine if the new caret position is close enough that the scrollbar should be re-locked
     * to the end of the document.
     *
     * @param dot The caret position
     * @return if it should be locked
     */
    public boolean shouldRelock(int dot) {
        OutWriter w = getOut();
        if (w != null && !w.isClosed()) {
            int dist = Math.abs(w.getLines().getCharCount() - dot);
            return dist < 100;
        }
        return false;
    }

    /**
     * Flag used to block navigating the editor to the first error line when
     * selecting the error line in the output window after a build (or maybe
     * it should navigate the editor there?  Could be somewhat rude...)
     */
    boolean ignoreCaretChanges = false;

    /**
     * Called when the text caret has changed position - will call OutputListener.outputLineSelected if
     * there is a listener for that position.
     *
     * @param pos The line the caret is in
     */
    int lastCaretListenerRange[];
    void caretPosChanged(int pos) {
        if (!ignoreCaretChanges) {
            if (lastCaretListenerRange != null && pos >= lastCaretListenerRange[0] && pos < lastCaretListenerRange[1]) {
                return;
            }
            OutWriter out = getOut();
            if (out != null) {
                int[] range = new int[2];
                OutputListener l = out.getLines().getListener(pos, range);
                if (l != null) {
                    ControllerOutputEvent oe = new ControllerOutputEvent(io, out.getLines().getLineAt(pos));
                    l.outputLineSelected(oe);
                    lastCaretListenerRange = range;
                } else {
                    lastCaretListenerRange = null;
                }
            }
        }
    }

    private void sendCaretToError(boolean backward) {
        OutWriter out = getOut();
        if (out != null) {
            AbstractOutputPane op = getOutputPane();
            int selStart = op.getSelectionStart();
            int selEnd = op.getSelectionEnd();
            int pos = op.getCaretPos();

            // check if link is selected
            if (selStart != selEnd && pos == selStart && out.getLines().isListener(selStart, selEnd)) {
                pos = backward ? selStart - 1 : selEnd + 1;
            }
            int[] lpos = new int[2];
            OutputListener l = out.getLines().nearestListener(pos, backward, lpos);
            if (l != null) {
                op.sendCaretToPos(lpos[0], lpos[1], true);
                if (!io.getIOContainer().isActivated()) {
                    ControllerOutputEvent ce = new ControllerOutputEvent(io,  out.getLines().getLineAt(lpos[0]));
                    l.outputLineAction(ce);
                }
            }
        }
    }

    /**
     * Searching from current position
     * @param reversed true for reverse search
     * @return true if found
     */
    private boolean find(boolean reversed) {
        OutWriter out = getOut();
        if (out != null) {
            String lastPattern = FindDialogPanel.result();
            if (lastPattern == null) {
                return false;
            }
            int pos = reversed ? getOutputPane().getSelectionStart() : getOutputPane().getCaretPos();
            if (pos > getOutputPane().getLength() || pos < 0) {
                pos = 0;
            }
            boolean regExp = FindDialogPanel.regExp();
            boolean matchCase = FindDialogPanel.matchCase();
            int[] sel = reversed ? out.getLines().rfind(pos, lastPattern, regExp, matchCase)
                    : out.getLines().find(pos, lastPattern, regExp, matchCase);
            String appendMsg = null;
            if (sel == null) {
                sel = reversed ? out.getLines().rfind(out.getLines().getCharCount(), lastPattern, regExp, matchCase)
                        : out.getLines().find(0, lastPattern, regExp, matchCase);
                if (sel != null) {
                    appendMsg = NbBundle.getMessage(OutputTab.class, reversed ? "MSG_SearchFromEnd" : "MSG_SearchFromBeg");
                }
            }
            String msg;
            if (sel != null) {
                getOutputPane().unlockScroll();
                getOutputPane().setSelection(sel[0], sel[1]);
                int line = out.getLines().getLineAt(sel[0]);
                int col = sel[0] - out.getLines().getLineStart(line);
                msg = NbBundle.getMessage(OutputTab.class, "MSG_Found", lastPattern, line + 1, col + 1);
                if (appendMsg != null) {
                    msg = msg + "; " + appendMsg;
                }
            } else {
                msg = NbBundle.getMessage(OutputTab.class, "MSG_NotFound", lastPattern);
            }
            StatusDisplayer.getDefault().setStatusText(msg);
            return sel != null;
        }
        return false;
    }

    /**
     * Holds the last written to directory for the save as file chooser.
     */
    private static String lastDir = null;

    /**
     * Invokes a file dialog and if a file is chosen, saves the output to that file.
     */
    void saveAs() {
        OutWriter out = getOut();
        if (out == null) {
            return;
        }
        File f = showFileChooser(this);
        if (f != null) {
            try {
                synchronized (out) {
                    out.getLines().saveAs(f.getPath());
                }
            } catch (IOException ioe) {
                NotifyDescriptor notifyDesc = new NotifyDescriptor(
                        NbBundle.getMessage(OutputTab.class, "MSG_SaveAsFailed", f.getPath()),
                        NbBundle.getMessage(OutputTab.class, "LBL_SaveAsFailedTitle"),
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.ERROR_MESSAGE,
                        new Object[]{NotifyDescriptor.OK_OPTION},
                        NotifyDescriptor.OK_OPTION);

                DialogDisplayer.getDefault().notify(notifyDesc);
            }
        }
    }

    /**
     * Shows a file dialog and an overwrite dialog if the file exists, returning
     * null if the user chooses not to overwrite.  Will use an AWT FileDialog for
     * Aqua, per Apple UI guidelines.
     *
     * @param owner A parent component for the dialog - the top level ancestor will
     *        actually be used so positioning is correct
     * @return A file to write to
     */
    private static File showFileChooser(JComponent owner) {
        File f = null;
        String dlgTtl = NbBundle.getMessage(Controller.class, "TITLE_SAVE_DLG"); //NOI18N

        boolean isAqua = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N

        if (isAqua) {
            //Apple UI guidelines recommend against ever using JFileChooser
            FileDialog fd = new FileDialog((Frame) owner.getTopLevelAncestor(), dlgTtl, FileDialog.SAVE);
            if (lastDir != null && new File(lastDir).exists()) {
                fd.setDirectory(lastDir);
            }
            fd.setModal(true);
            fd.setVisible(true);
            String s = fd.getDirectory() + fd.getFile();
            f = new File(s);
            if (f.exists() && f.isDirectory()) {
                f = null;
            }
        } else {
            JFileChooser jfc = new JFileChooser();
            if (lastDir != null && new File(lastDir).exists()) {
                File dir = new File(lastDir);
                if (dir.exists()) {
                    jfc.setCurrentDirectory(dir);
                }
            }
            jfc.setName(dlgTtl);
            jfc.setDialogTitle(dlgTtl);

            if (jfc.showSaveDialog(owner.getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION) {
                f = jfc.getSelectedFile();
            }
        }

        if (f != null && f.exists() && !isAqua) { //Aqua's file dialog takes care of this
            String msg = NbBundle.getMessage(Controller.class,
                    "FMT_FILE_EXISTS", new Object[]{f.getName()}); //NOI18N
            String title = NbBundle.getMessage(Controller.class,
                    "TITLE_FILE_EXISTS"); //NOI18N
            if (JOptionPane.showConfirmDialog(owner.getTopLevelAncestor(), msg, title,
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                f = null;
            }
        }
        if (f != null) {
            lastDir = f.getParent();
        }
        return f;
    }

    /**
     * Called when a line is clicked - if an output listener is listening on that
     * line, it will be sent <code>outputLineAction</code>.
     */
    private void openHyperlink() {
        OutWriter out = getOut();
        if (out != null) {
            int pos = getOutputPane().getCaretPos();
            int[] range = new int[2];
            OutputListener l = out.getLines().getListener(pos, range);
            if (l != null) {
                ignoreCaretChanges = true;
                getOutputPane().sendCaretToPos(range[0], range[1], true);
                ignoreCaretChanges = false;
                ControllerOutputEvent coe = new ControllerOutputEvent(io, out.getLines().getLineAt(pos));
                l.outputLineAction(coe);
            }
        }
    }

    /**
     * Post the output window's popup menu
     *
     * @param p The point clicked
     * @param src The source of the click event
     */
    void postPopupMenu(Point p, Component src) {
        JPopupMenu popup = new JPopupMenu();
        Action[] a = getToolbarActions();
        if (a.length > 0) {
            boolean added = false;
            for (int i = 0; i < a.length; i++) {
                if (a[i].getValue(Action.NAME) != null) {
                    // add the proxy that doesn't show icons #67451
                    popup.add(new ProxyAction(a[i]));
                    added = true;
                }
            }
            if (added) {
                popup.add(new JSeparator());
            }
        }

        List<TabAction> activeActions = new ArrayList<TabAction>(popupItems.length);
        for (int i = 0; i < popupItems.length; i++) {
            if (popupItems[i] instanceof JSeparator) {
                popup.add((JSeparator) popupItems[i]);
            } else {
                if (popupItems[i] == wrapAction) {
                    JCheckBoxMenuItem item = new JCheckBoxMenuItem((Action) popupItems[i]);
                    item.setSelected(getOutputPane().isWrapped());
                    activeActions.add((TabAction) popupItems[i]);
                    popup.add(item);
                } else if (popupItems[i] == filterAction) {
                    JCheckBoxMenuItem item = new JCheckBoxMenuItem((Action) popupItems[i]);
                    item.setSelected(origPane != null);
                    activeActions.add((TabAction) popupItems[i]);
                    popup.add(item);
                } else {
                    if ((popupItems[i] == closeAction && !io.getIOContainer().isCloseable(this))
                            || (popupItems[i] == fontTypeAction && getOutputPane().isWrapped())) {
                        continue;
                    }
                    JMenuItem item = popup.add((Action) popupItems[i]);
                    activeActions.add((TabAction) popupItems[i]);
                    if (popupItems[i] == findAction) {
                        item.setMnemonic(KeyEvent.VK_F);
                    }
                }
            }
        }
        // hack to remove the esc keybinding when doing popup..
        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JComponent c = getOutputPane().getTextView();
        Object escHandle = c.getInputMap().get(esc);
        c.getInputMap().remove(esc);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(esc);

        popup.addPopupMenuListener(new PMListener(activeActions, escHandle));
        popup.show(src, p.x, p.y);

    }

    void updateActions() {
        OutputPane pane = (OutputPane) getOutputPane();
        int len = pane.getLength();
        boolean enable = len > 0;
        OutWriter out = getOut();
        saveAsAction.setEnabled(enable);
        selectAllAction.setEnabled(enable);
        copyAction.setEnabled(pane.hasSelection());
        boolean hasErrors = out == null ? false : out.getLines().hasListeners();
        nextErrorAction.setEnabled(hasErrors);
        prevErrorAction.setEnabled(hasErrors);
    }

    private void showFontChooser() {
        PropertyEditor pe = PropertyEditorManager.findEditor(Font.class);
        if (pe != null) {
            pe.setValue(getOutputPane().getViewFont());
            DialogDescriptor dd = new DialogDescriptor(pe.getCustomEditor(), NbBundle.getMessage(OutputTab.class, "LBL_Font_Chooser_Title"));  // NOI18N
            String defaultFont = NbBundle.getMessage(OutputTab.class, "BTN_Defaul_Font");
            dd.setOptions(new Object[]{DialogDescriptor.OK_OPTION, defaultFont, DialogDescriptor.CANCEL_OPTION});  // NOI18N
            DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                Font f = (Font) pe.getValue();
                Controller.getDefault().changeFont(f);
            } else if (dd.getValue() == defaultFont) {
                Controller.getDefault().changeFont(null);
            }
        }
    }

    FilteredOutput filtOut;
    AbstractOutputPane origPane;

    private void setFilter(String pattern, boolean regExp, boolean matchCase) {
        if (pattern == null) {
            assert origPane != null;
            setOutputPane(origPane);
            origPane = null;
            filtOut.dispose();
            filtOut = null;
        } else {
            assert origPane == null;
            origPane = getOutputPane();
            filtOut = new FilteredOutput(pattern, regExp, matchCase);
            setOutputPane(filtOut.getPane());
            try {
                waitCursor(true);
                filtOut.readFrom(io.out());
                installKBActions();
            } finally {
                waitCursor(false);
            }
        }
        validate();
        getOutputPane().repaint();
        requestFocus();
    }

    private void waitCursor(boolean enable) {
        RootPaneContainer root = ((RootPaneContainer) getTopLevelAncestor());
        Cursor cursor = Cursor.getPredefinedCursor(enable ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR);
        root.getGlassPane().setCursor(cursor);
        root.getGlassPane().setVisible(enable);
    }

    OutWriter getOut() {
        return origPane != null ? filtOut.getWriter() : io.out();
    }

    private void disableHtmlName() {
        Controller.getDefault().removeFromUpdater(this);
        String escaped;
        try {
            escaped = XMLUtil.toAttributeValue(io.getName() + " ");
        } catch (CharConversionException e) {
            escaped = io.getName() + " ";
        }
        //#88204 apostophes are escaped in xm but not html
        io.getIOContainer().setTitle(this, escaped.replace("&apos;", "'"));
    }

    private static final int ACTION_COPY = 0;
    private static final int ACTION_WRAP = 1;
    private static final int ACTION_SAVEAS = 2;
    private static final int ACTION_CLOSE = 3;
    private static final int ACTION_NEXTERROR = 4;
    private static final int ACTION_PREVERROR = 5;
    private static final int ACTION_SELECTALL = 6;
    private static final int ACTION_FIND = 7;
    private static final int ACTION_FINDNEXT = 8;
    private static final int ACTION_NAVTOLINE = 9;
    private static final int ACTION_POSTMENU = 10;
    private static final int ACTION_FINDPREVIOUS = 11;
    private static final int ACTION_CLEAR = 12;
    private static final int ACTION_NEXTTAB = 13;
    private static final int ACTION_PREVTAB = 14;
    private static final int ACTION_LARGERFONT = 15;
    private static final int ACTION_SMALLERFONT = 16;
    private static final int ACTION_FONTTYPE = 17;
    private static final int ACTION_FILTER = 18;
    private static final int ACTION_PASTE = 19;

    Action copyAction = new TabAction(ACTION_COPY, "ACTION_COPY"); //NOI18N
    Action pasteAction = new TabAction(ACTION_PASTE, "ACTION_PASTE"); //NOI18N
    Action wrapAction = new TabAction(ACTION_WRAP, "ACTION_WRAP"); //NOI18N
    Action saveAsAction = new TabAction(ACTION_SAVEAS, "ACTION_SAVEAS"); //NOI18N
    Action closeAction = new TabAction(ACTION_CLOSE, "ACTION_CLOSE"); //NOI18N
    Action nextErrorAction = new TabAction(ACTION_NEXTERROR, "ACTION_NEXT_ERROR"); //NOI18N
    Action prevErrorAction = new TabAction(ACTION_PREVERROR, "ACTION_PREV_ERROR"); //NOI18N
    Action selectAllAction = new TabAction(ACTION_SELECTALL, "ACTION_SELECT_ALL"); //NOI18N
    Action findAction = new TabAction(ACTION_FIND, "ACTION_FIND"); //NOI18N
    Action findNextAction = new TabAction(ACTION_FINDNEXT, "ACTION_FIND_NEXT"); //NOI18N
    Action findPreviousAction = new TabAction(ACTION_FINDPREVIOUS, "ACTION_FIND_PREVIOUS"); //NOI18N
    Action filterAction = new TabAction(ACTION_FILTER, "ACTION_FILTER"); //NOI18N
    Action largerFontAction = new TabAction(ACTION_LARGERFONT, "ACTION_LARGER_FONT"); //NOI18N
    Action smallerFontAction = new TabAction(ACTION_SMALLERFONT, "ACTION_SMALLER_FONT"); //NOI18N
    Action fontTypeAction = new TabAction(ACTION_FONTTYPE, "ACTION_FONT_TYPE"); //NOI18N

    Action navToLineAction = new TabAction(ACTION_NAVTOLINE, "navToLine", //NOI18N
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    Action postMenuAction = new TabAction(ACTION_POSTMENU, "postMenu", //NOI18N
            KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK));
    Action clearAction = new TabAction(ACTION_CLEAR, "ACTION_CLEAR");
    Action nextTabAction = new TabAction(ACTION_NEXTTAB, "NextViewAction", //NOI18N
            (KeyStroke) null);
    Action prevTabAction = new TabAction(ACTION_PREVTAB, "PreviousViewAction", //NOI18N
            (KeyStroke) null);
    private Object[] popupItems = new Object[]{
        copyAction, pasteAction, new JSeparator(), findAction, findNextAction, findPreviousAction, filterAction, new JSeparator(),
        wrapAction, largerFontAction, smallerFontAction, fontTypeAction, new JSeparator(), saveAsAction,
        clearAction, closeAction,};

    /**
     * A stateless action which will find the owning OutputTab's controller and call
     * actionPerformed with its ID as an argument.
     */
    class TabAction extends AbstractAction {

        private int id;

        /**
         * Create a ControllerAction with the specified action ID (constants defined in Controller),
         * using the specified bundle key.  Expects the following contents in the bundle:
         * <ul>
         * <li>A name for the action matching the passed key</li>
         * <li>An accelerator for the action matching [key].accel</li>
         * </ul>
         * @param id An action ID
         * @param bundleKey A key for the bundle associated with the Controller class
         * @see org.openide.util.Utilities#stringToKey
         */
        TabAction(int id, String bundleKey) {
            if (bundleKey != null) {
                String name = NbBundle.getMessage(OutputTab.class, bundleKey);
                KeyStroke accelerator = getAcceleratorFor(bundleKey);
                this.id = id;
                putValue(NAME, name);
                putValue(ACCELERATOR_KEY, accelerator);
            }
        }

        /**
         * Create a ControllerAction with the specified ID, name and keystroke.  Actions created
         * using this constructor will not be added to the popup menu of the component.
         *
         * @param id The ID
         * @param name A programmatic name for the item
         * @param stroke An accelerator keystroke
         */
        TabAction(int id, String name, KeyStroke stroke) {
            this.id = id;
            putValue(NAME, name);
            putValue(ACCELERATOR_KEY, stroke);
        }

        void clearListeners() {
            PropertyChangeListener[] l = changeSupport.getPropertyChangeListeners();
            for (int i = 0; i < l.length; i++) {
                removePropertyChangeListener(l[i]);
            }
        }

        /**
         * Get a keyboard accelerator from the resource bundle, with special handling
         * for the mac keyboard layout.
         *
         * @param name The bundle key prefix
         * @return A keystroke
         */
        private KeyStroke getAcceleratorFor(String name) {
            String key = name + ".accel"; //NOI18N
            if (Utilities.isMac()) {
                key += ".mac"; //NOI18N
            }
            return Utilities.stringToKey(NbBundle.getMessage(OutputTab.class, key));
        }

        public int getID() {
            return id;
        }

        public void actionPerformed(ActionEvent e) {
            switch (id) {
                case ACTION_COPY:
                    getOutputPane().copy();
                    break;
                case ACTION_PASTE:
                    getOutputPane().paste();
                    break;
                case ACTION_WRAP:
                    boolean wrapped = getOutputPane().isWrapped();
                    getOutputPane().setWrapped(!wrapped);
                    break;
                case ACTION_SAVEAS:
                    saveAs();
                    break;
                case ACTION_CLOSE:
                    io.getIOContainer().remove(OutputTab.this);
                    break;
                case ACTION_NEXTERROR:
                    sendCaretToError(false);
                    break;
                case ACTION_PREVERROR:
                    sendCaretToError(true);
                    break;
                case ACTION_SELECTALL:
                    getOutputPane().selectAll();
                    break;
                case ACTION_FIND:
                     {
                        String pattern = getFindDlgResult(getOutputPane().getSelectedText(), "LBL_Find_Title", "LBL_Find_What", "BTN_Find"); //NOI18N
                        if (pattern != null && find(false)) {
                            findNextAction.setEnabled(true);
                            findPreviousAction.setEnabled(true);
                            requestFocus();
                        }
                    }
                    break;
                case ACTION_FINDNEXT:
                    find(false);
                    break;
                case ACTION_FINDPREVIOUS:
                    find(true);
                    break;
                case ACTION_NAVTOLINE:
                    openHyperlink();
                    break;
                case ACTION_POSTMENU:
                    postPopupMenu(new Point(0, 0), OutputTab.this);
                    break;
                case ACTION_CLEAR:
                    NbWriter writer = io.writer();
                    if (writer != null) {
                        try {
                            boolean vis = isInputVisible();
                            boolean closed = io.isStreamClosed();
                            writer.reset();
                            setInputVisible(vis);
                            io.setStreamClosed(closed);
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                    break;
                case ACTION_SMALLERFONT:
                    Controller.getDefault().changeFontSizeBy(-1, getOutputPane().isWrapped());
                    break;
                case ACTION_LARGERFONT:
                    Controller.getDefault().changeFontSizeBy(1, getOutputPane().isWrapped());
                    break;
                case ACTION_FONTTYPE:
                    showFontChooser();
                    break;
                case ACTION_FILTER:
                    if (origPane != null) {
                        setFilter(null, false, false);
                    } else {
                        String pattern = getFindDlgResult(getOutputPane().getSelectedText(),
                                "LBL_Filter_Title", "LBL_Filter_What", "BTN_Filter"); //NOI18N
                        if (pattern != null) {
                            setFilter(pattern, FindDialogPanel.regExp(), FindDialogPanel.matchCase());
                        }
                    }
                    break;
                default:
                    assert false;
            }
        }
    }

    @Override
    public void setInputVisible(boolean val) {
        super.setInputVisible(val);
        pasteAction.setEnabled(val);
    }

    private boolean validRegExp(String pattern) {
        try {
            Pattern.compile(pattern);
            return true;
        } catch (PatternSyntaxException ex) {
            JOptionPane.showMessageDialog(getTopLevelAncestor(), 
                    NbBundle.getMessage(OutputTab.class, "FMT_Invalid_RegExp", pattern),
                    NbBundle.getMessage(OutputTab.class, "LBL_Invalid_RegExp"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private String getFindDlgResult(String selection, String title, String label, String button) {
        String pattern = FindDialogPanel.getResult(selection, title, label, button); //NOI18N
        while (pattern != null && FindDialogPanel.regExp()) {
            if (validRegExp(pattern)) {
                break;
            }
            pattern = FindDialogPanel.getResult(pattern, title, label, button); //NOI18N
        }
        return pattern;
    }

    private static class ProxyAction implements Action {

        private Action orig;

        ProxyAction(Action original) {
            orig = original;
        }

        public Object getValue(String key) {
            if (Action.SMALL_ICON.equals(key)) {
                return null;
            }
            return orig.getValue(key);
        }

        public void putValue(String key, Object value) {
            orig.putValue(key, value);
        }

        public void setEnabled(boolean b) {
            orig.setEnabled(b);
        }

        public boolean isEnabled() {
            return orig.isEnabled();
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            orig.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            orig.removePropertyChangeListener(listener);
        }

        public void actionPerformed(ActionEvent e) {
            orig.actionPerformed(e);
        }
    }

    /**
     * #47166 - a disposed tab which has had its popup menu shown remains
     * referenced through PopupItems->JSeparator->PopupMenu->Invoker->OutputPane->OutputTab
     */
    private class PMListener implements PopupMenuListener {

        private List<TabAction> popupItems;
        private Object handle;

        PMListener(List<TabAction> popupItems, Object escHandle) {
            this.popupItems = popupItems;
            handle = escHandle;
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            JPopupMenu popup = (JPopupMenu) e.getSource();
            popup.removeAll();
            popup.setInvoker(null);
            // hack
            KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            JComponent c = getOutputPane().getTextView();
            c.getInputMap().put(esc, handle);
            getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(esc, handle);

            //hack end
            popup.removePopupMenuListener(this);
            for (TabAction action : popupItems) {
                action.clearListeners();
            }
        }

        public void popupMenuCanceled(PopupMenuEvent e) {
            popupMenuWillBecomeInvisible(e);
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            //do nothing
        }
    }

    private class FilteredOutput {
        String pattern;
        OutWriter out;
        OutputPane pane;
        OutputDocument doc;
        int readCount;
        Pattern compPattern;
        boolean regExp;
        boolean matchCase;

        public FilteredOutput(String pattern, boolean regExp, boolean matchCase) {
            this.pattern = (regExp || matchCase) ? pattern : pattern.toLowerCase();
            this.regExp = regExp;
            this.matchCase = matchCase;
            out = new OutWriter();
            pane = new OutputPane(OutputTab.this);
            doc = new OutputDocument(out);
            pane.setDocument(doc);
        }

        boolean passFilter(String str) {
            if (regExp) {
                if (compPattern == null) {
                    compPattern = matchCase ? Pattern.compile(pattern) : Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                }
                return compPattern.matcher(str).find();
            } else {
                return matchCase ? str.contains(pattern) : str.toLowerCase().contains(pattern);
            }
        }

        OutputPane getPane() {
            return pane;
        }

        OutWriter getWriter() {
            return out;
        }

        synchronized void readFrom(OutWriter orig) {
            AbstractLines lines = (AbstractLines) orig.getLines();
            while (readCount < lines.getLineCount()) {
                try {
                    int line = readCount++;
                    String str = lines.getLine(line);
                    if (!passFilter(str)) {
                        continue;
                    }
                    LineInfo info = lines.getExistingLineInfo(line);
                    out.print(str, info, lines.isImportantLine(line));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        void dispose() {
            out.dispose();
        }
    }
}

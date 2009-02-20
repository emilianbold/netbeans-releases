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
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.core.output2.Controller.ControllerOutputEvent;
import org.netbeans.core.output2.ui.AbstractOutputPane;
import org.netbeans.core.output2.ui.AbstractOutputTab;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.FindAction;
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

        installKeyboardAction(copyAction);
        installKeyboardAction(selectAllAction);
        installKeyboardAction(findAction);
        installKeyboardAction(findNextAction);
        installKeyboardAction(findPreviousAction);
        installKeyboardAction(wrapAction);
        installKeyboardAction(saveAsAction);
        installKeyboardAction(closeAction);
        installKeyboardAction(copyAction);
        installKeyboardAction(navToLineAction);
        installKeyboardAction(postMenuAction);
        installKeyboardAction(clearAction);

        getActionMap().put("jumpPrev", this.prevErrorAction); // NOI18N
        getActionMap().put("jumpNext", this.nextErrorAction); // NOI18N
        getActionMap().put(FindAction.class.getName(), this.findAction);
        getActionMap().put(javax.swing.text.DefaultEditorKit.copyAction, this.copyAction);

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

    public void reset() {
        setDocument(new OutputDocument(((NbWriter) io.getOut()).out()));
        io.setClosed(false);
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

    public void lineClicked(int line) {
        OutWriter out = io.out();
        if (out == null) {
            return;
        }
        OutputListener l = out.getLines().getListenerForLine(line);
        if (l != null) {
            ControllerOutputEvent oe = new ControllerOutputEvent (io, line);
            l.outputLineAction(oe);
            //Select the text on click
            getOutputPane().sendCaretToLine(line, true);
        }
    }

    boolean linePressed(int line, Point p) {
        OutWriter out = io.out();
        if (out != null) {
            return out.getLines().getListenerForLine(line) != null;
        } else {
            return false;
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
        boolean hadOutputListeners = hasOutputListeners;
        if (getFirstNavigableListenerLine() == -1) {
            return;
        }
        hasOutputListeners = io.out() != null && io.out().getLines().firstListenerLine() >= 0;
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
        OutWriter out = io.out();
        if (out != null) {
            int line = getFirstNavigableListenerLine();
            if (Controller.LOG) {
                Controller.log("NAV TO FIRST LISTENER LINE: " + line);
            }
            if (line > 0) {
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
        Controller.getDefault().removeTab(io);
        io.setClosed(true);
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
        OutWriter w = io.out();
        if (w != null && !w.isClosed()) {
            int dist = Math.abs(w.getLines().getCharCount() - dot);
            return dist < 100;
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

    /**
     * Fetch the output listener for a given line
     *
     * @param line The line to find a listener on
     * @return An output listener or null
     */
    private OutputListener listenerForLine(int line) {
        OutWriter out = io.out();
        if (out != null) {
            return out.getLines().getListenerForLine(line);
        }
        return null;
    }
    /**
     * Flag used to block navigating the editor to the first error line when
     * selecting the error line in the output window after a build (or maybe
     * it should navigate the editor there?  Could be somewhat rude...)
     */
    boolean ignoreCaretChanges = false;

    /**
     * Called when the text caret has changed lines - will call OutputListener.outputLineSelected if
     * there is a listener for that line.
     *
     * @param line The line the caret is in
     */
    void caretEnteredLine(int line) {
        if (!ignoreCaretChanges) {
            OutputListener l = listenerForLine(line);
            if (l != null) {
                ControllerOutputEvent oe = new ControllerOutputEvent(io, line);
                l.outputLineSelected(oe);
            }
        }
    }

    /**
     * Sends the caret in a tab to the nearest error line to its current position, selecting
     * that line.
     *
     * @param backward If the search should be done in reverse
     */
    private void sendCaretToError(boolean backward) {
        OutWriter out = io.out();
        if (out != null) {
            int line = getOutputPane().getCaretLine();
            if (!getOutputPane().isLineSelected(line)) {
                line += backward ? 1 : -1;
            }

            if (line >= getOutputPane().getLineCount() - 1) {
                line = 0;
            }
            //FirstF12: #48485 - caret is already on the first listener line,
            //so F12 jumps to the second error.  So search from 0 the first time after a reset
            int newline = out.getLines().nearestListenerLine(line, backward);
            if (newline == line) {
                if (!backward && line != getOutputPane().getLineCount()) {
                    newline = out.getLines().nearestListenerLine(line + 1, backward);
                } else if (backward && line > 0) {
                    newline = out.getLines().nearestListenerLine(line - 1, backward);
                } else {
                    return;
                }
            }
            if (newline != -1) {
                getOutputPane().sendCaretToLine(newline, true);
                if (!io.getIOContainer().isActivated()) {
                    OutputListener l = out.getLines().getListenerForLine(newline);
                    ControllerOutputEvent ce = new ControllerOutputEvent(io, newline);
                    l.outputLineAction(ce);
                }
            }
        }
    }

    /**
     * Find the next match for the previous search contents, starting at
     * the current caret position.
     *
     */
    private void findNext() {
        OutWriter out = io.out();
        if (out != null) {
            String lastPattern = FindDialogPanel.getPanel().getPattern();
            if (lastPattern != null) {
                out.getLines().find(lastPattern);
            }
            Matcher matcher = out.getLines().getForwardMatcher();
            int pos = getOutputPane().getCaretPos();
            if (pos >= getOutputPane().getLength() || pos < 0) {
                pos = 0;
            }

            if (matcher != null && matcher.find(pos)) {
                getOutputPane().setSelection(matcher.start(), matcher.end());
                copyAction.setEnabled(true);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    /**
     * Find the match before the current caret position, using the previously
     * searched for value.
     *
     * @param tab The tab
     */
    private void findPrevious() {
        OutWriter out = io.out();
        if (out != null) {
            String lastPattern = FindDialogPanel.getPanel().getPattern();
            if (lastPattern != null) {
                out.getLines().find(lastPattern);
            }
            Matcher matcher = out.getLines().getReverseMatcher();

            int length = getOutputPane().getLength();
            int pos = length - getOutputPane().getSelectionStart();

            if (pos >= getOutputPane().getLength() - 1 || pos < 0) {
                pos = 0;
            }
            if (matcher != null && matcher.find(pos)) {
                int start = length - matcher.end();
                int end = length - matcher.start();
                getOutputPane().setSelection(start, end);
                copyAction.setEnabled(true);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    /**
     * Holds the last written to directory for the save as file chooser.
     */
    private static String lastDir = null;

    /**
     * Invokes a file dialog and if a file is chosen, saves the output to that file.
     */
    void saveAs() {
        OutWriter out = io.out();
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
    private void openLineIfError() {
        OutWriter out = io.out();
        if (out != null) {
            int line = getOutputPane().getCaretLine();
            OutputListener lis = out.getLines().getListenerForLine(line);
            if (lis != null) {
                ignoreCaretChanges = true;
                getOutputPane().sendCaretToLine(line, true);
                ignoreCaretChanges = false;
                ControllerOutputEvent coe = new ControllerOutputEvent(io, line);
                lis.outputLineAction(coe);
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

        for (int i = 0; i < popupItems.length; i++) {
            if (popupItems[i] instanceof JSeparator) {
                popup.add((JSeparator) popupItems[i]);
            } else {
                if (popupItems[i] != wrapAction) {
                    if (popupItems[i] == closeAction && !io.getIOContainer().isCloseable(this)) {
                        continue;
                    }
                    JMenuItem item = popup.add((Action) popupItems[i]);
                    if (popupItems[i] == findAction) {
                        item.setMnemonic(KeyEvent.VK_F);
                    }
                } else {
                    JCheckBoxMenuItem item =
                            new JCheckBoxMenuItem((Action) popupItems[i]);

                    item.setSelected(getOutputPane().isWrapped());
                    popup.add(item);
                }
            }
        }
        // hack to remove the esc keybinding when doing popup..
        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JComponent c = getOutputPane().getTextView();
        Object escHandle = c.getInputMap().get(esc);
        c.getInputMap().remove(esc);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(esc);

        popup.addPopupMenuListener(new PMListener(popupItems, escHandle));
        popup.show(src, p.x, p.y);

    }

    void updateActions() {
        OutputPane pane = (OutputPane) getOutputPane();
        int len = pane.getLength();
        boolean enable = len > 0;
        findAction.setEnabled(enable);
        OutWriter out = io.out();
        saveAsAction.setEnabled(enable);
        selectAllAction.setEnabled(enable);
        copyAction.setEnabled(pane.hasSelection());
        boolean hasErrors = out == null ? false : out.getLines().firstListenerLine() != -1;
        nextErrorAction.setEnabled(hasErrors);
        prevErrorAction.setEnabled(hasErrors);
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

    Action copyAction = new TabAction(ACTION_COPY, "ACTION_COPY"); //NOI18N
    Action wrapAction = new TabAction(ACTION_WRAP, "ACTION_WRAP"); //NOI18N
    Action saveAsAction = new TabAction(ACTION_SAVEAS, "ACTION_SAVEAS"); //NOI18N
    Action closeAction = new TabAction(ACTION_CLOSE, "ACTION_CLOSE"); //NOI18N
    Action nextErrorAction = new TabAction(ACTION_NEXTERROR, "ACTION_NEXT_ERROR"); //NOI18N
    Action prevErrorAction = new TabAction(ACTION_PREVERROR, "ACTION_PREV_ERROR"); //NOI18N
    Action selectAllAction = new TabAction(ACTION_SELECTALL, "ACTION_SELECT_ALL"); //NOI18N
    Action findAction = new TabAction(ACTION_FIND, "ACTION_FIND"); //NOI18N
    Action findNextAction = new TabAction(ACTION_FINDNEXT, "ACTION_FIND_NEXT"); //NOI18N
    Action findPreviousAction = new TabAction(ACTION_FINDPREVIOUS, "ACTION_FIND_PREVIOUS"); //NOI18N
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
        copyAction, new JSeparator(), findAction, findNextAction, new JSeparator(),
        wrapAction, new JSeparator(), saveAsAction, clearAction, closeAction,};

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
                    int start = getOutputPane().getSelectionStart();
                    int end = getOutputPane().getSelectionEnd();
                    String str = null;
                    if (start > 0 && end > start) {
                        try {
                            str = getOutputPane().getDocument().getText(start, end - start);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }
                    }
                    FindDialogPanel.showFindDialog(getFindActionListener(findNextAction, findPreviousAction, copyAction), str);
                    break;
                case ACTION_FINDNEXT:
                    findNext();
                    break;
                case ACTION_FINDPREVIOUS:
                    findPrevious();
                    break;
                case ACTION_NAVTOLINE:
                    openLineIfError();
                    break;
                case ACTION_POSTMENU:
                    postPopupMenu(new Point(0, 0), OutputTab.this);
                    break;
                case ACTION_CLEAR:
                    NbWriter writer = io.writer();
                    if (writer != null) {
                        try {
                            writer.reset();
                            disableHtmlName();
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                    break;
                default:
                    assert false;
            }
        }
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

        private Object[] popupItems;
        private Object handle;

        PMListener(Object[] popupItems, Object escHandle) {
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
            for (int i = 0; i < popupItems.length; i++) {
                if (popupItems[i] instanceof OutputTab.TabAction) {
                    ((OutputTab.TabAction) popupItems[i]).clearListeners();
                }
            }
        }

        public void popupMenuCanceled(PopupMenuEvent e) {
            popupMenuWillBecomeInvisible(e);
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            //do nothing
        }
    }
}

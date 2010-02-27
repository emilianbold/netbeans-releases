/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.terminal.ioprovider;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.openide.util.NbPreferences;
import org.openide.windows.IOContainer;

import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.lib.terminalemulator.StreamTerm;

import org.netbeans.lib.terminalemulator.support.DefaultFindState;
import org.netbeans.lib.terminalemulator.support.FindState;
import org.netbeans.lib.terminalemulator.support.TermOptions;

import org.netbeans.modules.terminal.ui.TermAdvancedOption;

/**
 * A {@link org.netbeans.lib.terminalemulator.Term}-based terminal component for
 * inside NetBeans.
 * <p>
 * The most straightforward way of using it is as follows:
 * <pre>
    import org.netbeans.lib.richexecution.Command;
    import org.netbeans.lib.richexecution.Program;
 *
    public void actionPerformed(ActionEvent evt) {
        // Ask user what command they want to run
        String cmd = JOptionPane.showInputDialog("Command");
        if (cmd == null || cmd.trim().equals(""))
            return;

        TerminalProvider terminalProvider = TerminalProvider.getDefault();
        Terminal terminal = terminalProvider.createTerminal("command: " + cmd);
        Program program = new Command(cmd);
        terminal.startProgram(program, true);
    }
 * </pre>
 * @author ivan
 */
public final class Terminal extends JComponent {

//    /**
//     * Communicates state changes to container (TermTopComponent).
//     */
//    interface TerminalListener {
//        void reaped(Terminal who);
//        void setTitle(Terminal who, String title);
//    }

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final TerminalContainer terminalContainer;
    private final IOContainer ioContainer;
    private final CallBacks callBacks = new CallBacks();

    private final StreamTerm term;
    private final FindState findState;

    private static final Preferences prefs =
        NbPreferences.forModule(TermAdvancedOption.class);
    private final TermOptions termOptions;
    private final TermOptionsPCL termOptionsPCL = new TermOptionsPCL();

    private static final String PROP_ACTIONS = "Terminal_ACTIONS";
    private static final String PROP_TITLE = "Terminal_TITLE";

    private String title;
    // OLD private PtyProcess termProcess;
    private Action[] actions = new Action[0];

    // OLD private Program lastProgram;

    private boolean closing;
    private boolean closed;

    private class TermOptionsPCL implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            applyTermOptions(false);
        }
    }

    /**
     * These are messages from IOContainer we are obligated to handle.
     */
    private class CallBacks implements IOContainer.CallBacks {

        public void closed() {
            // System.out.printf("Terminal.CallBacks.closed()\n");
	    // Causes assertion error in IOContainer/IOWindow.
            // OLD close();
        }

        public void selected() {
            // System.out.printf("Terminal.CallBacks.selected()\n");
        }

        public void activated() {
            // System.out.printf("Terminal.CallBacks.activated()\n");
        }

        public void deactivated() {
            // System.out.printf("Terminal.CallBacks.deactivated()\n");
        }
    }

    public Terminal(IOContainer ioContainer, Action[] actions, String name) {
        termOptions = TermOptions.getDefault(prefs);

        this.terminalContainer = null;
        this.ioContainer = ioContainer;
        // this.term = new StreamTerm();
        this.term = new ActiveTerm();

        this.term.setCursorVisible(true);

        findState = new DefaultFindState(term);

        term.setHorizontallyScrollable(false);
        term.setEmulation("ansi");
        term .setBackground(Color.white);
        term.setHistorySize(4000);

        termOptions.addPropertyChangeListener(termOptionsPCL);
        applyTermOptions(true);

        term.getScreen().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point p = SwingUtilities.convertPoint((Component) e.getSource(),
                                                          e.getPoint(),
                                                          term.getScreen());
                    postPopupMenu(p);
                }
            }
        } );

        // Tell term about keystrokes we use for menu accelerators so
        // it passes them through.
        /* LATER
         * A-V brings up the main View menu.
        term.getKeyStrokeSet().add(copyAction.getValue(Action.ACCELERATOR_KEY));
        term.getKeyStrokeSet().add(pasteAction.getValue(Action.ACCELERATOR_KEY))
;
        term.getKeyStrokeSet().add(closeAction.getValue(Action.ACCELERATOR_KEY))
;
        */

        setLayout(new BorderLayout());
        add(term, BorderLayout.CENTER);

        ioContainer.add(this, callBacks());
        ioContainer.open();
        ioContainer.requestActive();
        if (name != null)
            setTitle(name);
	setActions(actions);
    }


    Terminal(TerminalContainer termTopComponent, String name) {
        termOptions = TermOptions.getDefault(prefs);

        this.terminalContainer = termTopComponent;
        this.ioContainer = null;
        // this.term = new StreamTerm();
        this.term = new ActiveTerm();

        this.term.setCursorVisible(true);

        findState = new DefaultFindState(term);

        term.setHorizontallyScrollable(false);
        term.setEmulation("ansi");
        term .setBackground(Color.white);
        term.setHistorySize(4000);

        termOptions.addPropertyChangeListener(termOptionsPCL);
        applyTermOptions(true);

        term.getScreen().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point p = SwingUtilities.convertPoint((Component) e.getSource(),
                                                          e.getPoint(),
                                                          term.getScreen());
                    postPopupMenu(p);
                }
            }
        } );

        // Tell term about keystrokes we use for menu accelerators so
        // it passes them through.
        /* LATER
         * A-V brings up the main View menu.
        term.getKeyStrokeSet().add(copyAction.getValue(Action.ACCELERATOR_KEY));
        term.getKeyStrokeSet().add(pasteAction.getValue(Action.ACCELERATOR_KEY));
        term.getKeyStrokeSet().add(closeAction.getValue(Action.ACCELERATOR_KEY));
        */

        setLayout(new BorderLayout());
        add(term, BorderLayout.CENTER);

        termTopComponent.add(this);
        termTopComponent.topComponent().open();
        termTopComponent.topComponent().requestActive();
        if (name != null)
            setTitle(name);
    }

    public IOContainer.CallBacks callBacks() {
        return callBacks;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    private void applyTermOptions(boolean initial) {
        Font font = term.getFont();
        /* OLD
        if (font != null) {
            Font newFont = new Font(font.getName(),
                                    font.getStyle(),
                                    termOptions.getFontSize());
            term.setFont(newFont);
        } else {
            Font newFont = new Font("monospaced",
                                    java.awt.Font.PLAIN,
                                    termOptions.getFontSize());
            term.setFont(newFont);
        }
        */
        term.setFixedFont(true);
        term.setFont(termOptions.getFont());

        term.setBackground(termOptions.getBackground());
        term.setForeground(termOptions.getForeground());
        term.setHighlightColor(termOptions.getSelectionBackground());
        term.setHistorySize(termOptions.getHistorySize());
        term.setTabSize(termOptions.getTabSize());

        term.setClickToType(termOptions.getClickToType());
        term.setScrollOnInput(termOptions.getScrollOnInput());
        term.setScrollOnOutput(termOptions.getScrollOnOutput());
        if (initial)
            term.setHorizontallyScrollable(!termOptions.getLineWrap());

        // If we change the font from smaller to bigger, the size
        // calculations go awry and the last few lines are forever hidden.
        setSize(getPreferredSize());
        validate();

    }

    /**
     * Make this Terminal and the TopComponent containing it be visible.
     */
    public void select() {
        if (terminalContainer != null)
            terminalContainer.select(this);
        else if (ioContainer != null)
            ioContainer.select(this);
    }
    
    /**
     * Return the underlying Term.
     * @return the underlying StreamTerm.
     */
    public StreamTerm term() {
        return term;
    }

    public void setTitle(String title) {
        String oldTitle = title;
        this.title = title;
        if (terminalContainer != null)
            terminalContainer.setTitle(this, title);
        else if (ioContainer != null)
            ioContainer.setTitle(this, title);
        pcs.firePropertyChange(PROP_TITLE, oldTitle, title);
    }

    public String getTitle() {
        return title;
    }

    FindState getFindState() {
        return findState;
    }

    private void setActions(Action[] actions) {
        Action[] oldActions = actions;
        this.actions = actions;
        if (terminalContainer != null)
	    terminalContainer.setToolbarActions(this, actions);
        else if (ioContainer != null)
	    ioContainer.setToolbarActions(this, actions);
        pcs.firePropertyChange(PROP_ACTIONS, oldActions, actions);
    }

    Action[] getActions() {
        return actions;
    }

    /* OLD
    private final class RerunAction extends AbstractAction {
        public RerunAction() {
            setEnabled(false);
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(Action.SMALL_ICON)) {
                return new ImageIcon(Terminal.class.getResource("rerun.png"));
            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
                return "Re-run";
            } else {
                return super.getValue(key);
            }
        }

        public void actionPerformed(ActionEvent e) {
            System.out.printf("Re-run pressed!\n");
            if (!isEnabled())
                return;
            if (termProcess != null)
                return;     // still someone running
            if (lastProgram == null)
                return;
            startProgram(lastProgram, true);
        }
    }

    private final class StopAction extends AbstractAction {
        public StopAction() {
            setEnabled(false);
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(Action.SMALL_ICON)) {
                return new ImageIcon(Terminal.class.getResource("stop.png"));
            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
                return "Stop";
            } else {
                return super.getValue(key);
            }
        }

        public void actionPerformed(ActionEvent e) {
            System.out.printf("Stop pressed!\n");
            if (!isEnabled())
                return;
            if (termProcess == null)
                return;
            termProcess.terminate();
        }
    }
     */

    private final class ClearAction extends AbstractAction {
        public ClearAction() {
            super("Clear");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_E,
                                                           InputEvent.ALT_MASK);
            putValue(ACCELERATOR_KEY, accelerator);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            term.clear();
        }
    }

    private final class CloseAction extends AbstractAction {
        public CloseAction() {
            super("Close");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F4,
                                                           InputEvent.ALT_MASK);
            putValue(ACCELERATOR_KEY, accelerator);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            close();
        }
    }

    private final class CopyAction extends AbstractAction {
        public CopyAction() {
            super("Copy");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                                           InputEvent.ALT_MASK);
            // System.out.printf("Accelerator for Copy: %s\n", accelerator);
            putValue(ACCELERATOR_KEY, accelerator);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            term.copyToClipboard();
        }
    }

    private final class PasteAction extends AbstractAction {
        public PasteAction() {
            super("Paste");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                                           InputEvent.ALT_MASK);
            // System.out.printf("Accelerator for Paste: %s\n", accelerator);
            putValue(ACCELERATOR_KEY, accelerator);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            term.pasteFromClipboard();
        }
    }

    private final class FindAction extends AbstractAction {
        public FindAction() {
            super("Find");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                                           InputEvent.ALT_MASK);
            // System.out.printf("Accelerator for Find: %s\n", accelerator);
            putValue(ACCELERATOR_KEY, accelerator);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            if (terminalContainer != null)
                terminalContainer.find(Terminal.this);
        }
    }

    private static final String BOOLEAN_STATE_ACTION_KEY = "boolean_state_action";
    private static final String BOOLEAN_STATE_ENABLED_KEY = "boolean_state_enabled";

    private final class WrapAction extends AbstractAction {
        public WrapAction() {
            super("Wrap lines");
            // LATER KeyStroke accelerator = Utilities.stringToKey("A-R");
            putValue(BOOLEAN_STATE_ACTION_KEY, true);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            boolean hs = term.isHorizontallyScrollable();
            term.setHorizontallyScrollable(!hs);
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(BOOLEAN_STATE_ENABLED_KEY))
                return ! term.isHorizontallyScrollable();
            else
                return super.getValue(key);

        }
    }

    /* OLD
    private final Action stopAction = new StopAction();
    private final Action rerunAction = new RerunAction();
     */

    private final Action copyAction = new CopyAction();
    private final Action pasteAction = new PasteAction();
    private final Action findAction = new FindAction();
    private final Action wrapAction = new WrapAction();
    private final Action clearAction = new ClearAction();
    private final Action closeAction = new CloseAction();

    /**
     * Start a {@link Program} under this terminal.
     * It may be easier to pass it a
     * {@link org.netbeans.lib.richexecution.Command} or a
     * {@link org.netbeans.lib.richexecution.Shell}.
     * <p>
     * Perhaps it SHOULD return {@link PtyProcess}, but if the program is
     * restarted then the instance in hand will be OOD.
     * <br>
     * Perhaps a "process" property with creation/demise notification might be
     * more useful?
     *
     * @param program The Program to run.
     * @param restartable If true the Restart/Stop actions will be present.
     */
    /* OLD
    private void startProgram(Program program, final boolean restartable) {

        if (termProcess != null)
            throw new IllegalStateException("Process already running");

        setTitle(program.name());

        if (restartable) {
            lastProgram = program;
            setActions(new Action[] {rerunAction, stopAction});
        } else { 
            lastProgram = null;
            setActions(new Action[0]);
        }

        TermExecutor executor = new TermExecutor();
        termProcess = executor.start(program, term());

        if (restartable) {
            stopAction.setEnabled(true);
            rerunAction.setEnabled(false);
        }

        Thread reaperThread = new Thread() {
            @Override
            public void run() {
                termProcess.waitFor();
                if (restartable && !closing) {
                    stopAction.setEnabled(false);
                    rerunAction.setEnabled(true);
                } else {
                    closing = true;
                    closeWork();
                }
                // This doesn't yield the desired result because we need to
                // wait for all the output to be processed:
                // LATER tprintf("Exited with %d\n\r", termProcess.exitValue());
                termProcess = null;
            }
        };
        reaperThread.start();
    }
    */

    private void closeWork() {
        assert closing;
        if (closed)
            return;
        if (terminalContainer != null) {
            terminalContainer.removeTerminal(this);
            closed = true;
        } else if (ioContainer != null) {
            closed = true;
            ioContainer.remove(this);
        }
        termOptions.removePropertyChangeListener(termOptionsPCL);
    }

    public void close() {
        // This makes sure, in the reaping pathway, that we don't hang
        // about even if we're restartable.
        closing = true;

	/* LATER
        if (termProcess != null && !termProcess.isFinished()) {
            termProcess.hangup();
            // This should eventually end up in reaperThread.
        } else
	 */
	{
            closeWork();
        }
    }

    public boolean isClosed() {
        return closed;
    }

    private boolean isBooleanStateAction(Action a) {
        Boolean isBooleanStateAction = (Boolean) a.getValue(BOOLEAN_STATE_ACTION_KEY);
        return isBooleanStateAction != null && isBooleanStateAction;
    }

    private void addMenuItem(JPopupMenu menu, Object o) {
        if (o instanceof JSeparator) {
            menu.add((JSeparator) o);
        } else if (o instanceof Action) {
            Action a = (Action) o;
            if (isBooleanStateAction(a)) {
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(a);
                item.setSelected((Boolean) a.getValue(BOOLEAN_STATE_ENABLED_KEY));
                menu.add(item);
            } else {
                menu.add((Action) o);
            }
        }
    }

    private void postPopupMenu(Point p) {
        JPopupMenu menu = new JPopupMenu();
        if (terminalContainer != null)
            menu.putClientProperty("container", terminalContainer); // NOI18N
        else if (ioContainer != null)
            menu.putClientProperty("container", ioContainer); // NOI18N
        menu.putClientProperty("component", this);             // NOI18N

        Action[] acts = getActions();
        if (acts.length > 0) {
            for (Action a : acts) {
                if (a.getValue(Action.NAME) != null)
                    menu.add(a);
            }
            if (menu.getSubElements().length > 0)
                menu.add(new JSeparator());
        }
        addMenuItem(menu, copyAction);
        addMenuItem(menu, pasteAction);
        addMenuItem(menu, new JSeparator());
        addMenuItem(menu, findAction);
        addMenuItem(menu, new JSeparator());
        addMenuItem(menu, wrapAction);
        addMenuItem(menu, new JSeparator());
        addMenuItem(menu, clearAction);
        addMenuItem(menu, closeAction);

        findAction.setEnabled(! findState.isVisible());

        // just to get it echoed
        term.setKeyStrokeSet(term.getKeyStrokeSet());

        menu.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        } );
        menu.show(term.getScreen(), p.x, p.y);
    }

    /**
     * Callback for when a hyperlink in a Terminal is clicked.
     * <p>
     * A hyperlink can be created by outputting a sequence like this:
     * <br>
     * <b>ESC</b>]10;<i>clientData</i>;<i>text</i><b>BEL</b>
     * @author ivan
     */
    /* LATER
    public interface HyperlinkListener {
	public void action(String clientData);
    }

    public void setHyperlinkListener(final HyperlinkListener hyperlinkListener) {
	((ActiveTerm) term).setActionListener(new ActiveTermListener() {
	    public void action(ActiveRegion r, InputEvent e) {
		if (r.isLink()) {
		    String url = (String) r.getUserObject();
		    hyperlinkListener.action(url);
		}
	    }
	});
    }
     */
}

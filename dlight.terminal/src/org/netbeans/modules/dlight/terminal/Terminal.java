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
package org.netbeans.modules.dlight.terminal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.lib.terminalemulator.StreamTerm;
import org.openide.windows.IOContainer;

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
    private final TermOptions termOptions;
    private final TerminalContainer terminalContainer;
    private final IOContainer ioContainer;
    private final CallBacks callBacks = new CallBacks();
    private final StreamTerm term;
    private static final String PROP_ACTIONS = "Terminal_ACTIONS"; // NOI18N
    private static final String PROP_TITLE = "Terminal_TITLE"; // NOI18N
    private String title;
    private Action[] actions = new Action[0];
    private boolean closing;
    private boolean closed;

    /**
     * These are messages from IOContainer we are obligated to handle.
     */
    private class CallBacks implements IOContainer.CallBacks {

        @Override
        public void closed() {
            System.out.printf("Terminal.CallBacks.closed()\n");
            // Causes assertion error in IOContainer/IOWindow.
            // OLD close();
        }

        @Override
        public void selected() {
            System.out.printf("Terminal.CallBacks.selected()\n");
        }

        @Override
        public void activated() {
            System.out.printf("Terminal.CallBacks.activated()\n");
        }

        @Override
        public void deactivated() {
            System.out.printf("Terminal.CallBacks.deactivated()\n");
        }
    }

    Terminal(IOContainer ioContainer, String name) {
        this.terminalContainer = null;
        this.ioContainer = ioContainer;
        // this.term = new StreamTerm();
        this.term = new ActiveTerm();

        this.term.setCursorVisible(true);

        term.setHorizontallyScrollable(false);
        term.setEmulation("ansi"); // NOI18N
        term.setBackground(Color.white);
        term.setHistorySize(4000);

        // kav - only defaults...
        termOptions = TermOptions.getDefaults();

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
        });

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
        if (name != null) {
            setTitle(name);
        }
    }

    Terminal(TerminalContainer termTopComponent, String name) {
        termOptions = TermOptions.getDefaults();

        this.terminalContainer = termTopComponent;
        this.ioContainer = null;
        // this.term = new StreamTerm();
        this.term = new ActiveTerm();

        this.term.setCursorVisible(true);

        term.setHorizontallyScrollable(false);
        term.setEmulation("ansi"); // NOI18N
        term.setBackground(Color.white);
        term.setHistorySize(4000);

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
        });

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
        if (name != null) {
            setTitle(name);
        }
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
        term.setFixedFont(true);
        term.setFont(termOptions.getFont());

        term.setBackground(termOptions.getBackground());
        term.setForeground(termOptions.getForeground());
        term.setHighlightColor(termOptions.getSelectionBackground());
        term.setHistorySize(termOptions.getHistorySize());
        term.setTabSize(termOptions.getTabSize());

        term.setClickToType(termOptions.isClickToType());
        term.setScrollOnInput(termOptions.isScrollOnInput());
        term.setScrollOnOutput(termOptions.isScrollOnOutput());

        if (initial) {
            term.setHorizontallyScrollable(!termOptions.isLineWrap());
        }

        // If we change the font from smaller to bigger, the size
        // calculations go awry and the last few lines are forever hidden.
        setSize(getPreferredSize());
        validate();
    }

    /**
     * Make this Terminal and the TopComponent containing it be visible.
     */
    public void select() {
        if (terminalContainer != null) {
            terminalContainer.select(this);
        } else if (ioContainer != null) {
            ioContainer.select(this);
        }
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
        if (terminalContainer != null) {
            terminalContainer.setTitle(this, title);
        } else if (ioContainer != null) {
            ioContainer.setTitle(this, title);
        }
        pcs.firePropertyChange(PROP_TITLE, oldTitle, title);
    }

    public String getTitle() {
        return title;
    }

    public void setActions(Action[] actions) {
        Action[] oldActions = actions;
        this.actions = actions;
        if (terminalContainer != null) {
            terminalContainer.setActions(this, actions);
        } else if (ioContainer != null) {
            ioContainer.setToolbarActions(this, actions);
        }
        pcs.firePropertyChange(PROP_ACTIONS, oldActions, actions);
    }

    public Action[] getActions() {
        return actions;
    }

//    private final class RerunAction extends AbstractAction {
//
//        public RerunAction() {
//            setEnabled(false);
//        }
//
//        @Override
//        public Object getValue(String key) {
//            if (key.equals(Action.SMALL_ICON)) {
//                return new ImageIcon(Terminal.class.getResource("rerun.png"));
//            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
//                return "Re-run";
//            } else {
//                return super.getValue(key);
//            }
//        }
//    }
//
//    private final class StopAction extends AbstractAction {
//
//        public StopAction() {
//            setEnabled(false);
//        }
//
//        @Override
//        public Object getValue(String key) {
//            if (key.equals(Action.SMALL_ICON)) {
//                return new ImageIcon(Terminal.class.getResource("stop.png"));
//            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
//                return "Stop";
//            } else {
//                return super.getValue(key);
//            }
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            System.out.printf("Stop pressed!\n");
//            if (!isEnabled()) {
//                return;
//            }
//            if (termProcess == null) {
//                return;
//            }
//            termProcess.terminate();
//        }
//    }
    private final class ClearAction extends AbstractAction {

        public ClearAction() {
            super("Clear");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_E,
                    InputEvent.ALT_MASK);
            putValue(ACCELERATOR_KEY, accelerator);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
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

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            close();
        }
    }

    private final class CopyAction extends AbstractAction {

        public CopyAction() {
            super("Copy");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_C,
                    InputEvent.ALT_MASK);
            System.out.printf("Accelerator for Copy: %s\n", accelerator);
            putValue(ACCELERATOR_KEY, accelerator);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            term.copyToClipboard();
        }
    }

    private final class PasteAction extends AbstractAction {

        public PasteAction() {
            super("Paste");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.ALT_MASK);
            putValue(ACCELERATOR_KEY, accelerator);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            term.pasteFromClipboard();
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

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            boolean hs = term.isHorizontallyScrollable();
            term.setHorizontallyScrollable(!hs);
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(BOOLEAN_STATE_ENABLED_KEY)) {
                return !term.isHorizontallyScrollable();
            } else {
                return super.getValue(key);
            }

        }
    }
    private final Action copyAction = new CopyAction();
    private final Action pasteAction = new PasteAction();
    private final Action wrapAction = new WrapAction();
    private final Action clearAction = new ClearAction();
    private final Action closeAction = new CloseAction();

    private void closeWork() {
        assert closing;
        if (closed) {
            return;
        }
        if (terminalContainer != null) {
            terminalContainer.removeTerminal(this);
            closed = true;
        } else if (ioContainer != null) {
            closed = true;
            ioContainer.remove(this);
        }
    }

    public void close() {
        // This makes sure, in the reaping pathway, that we don't hang
        // about even if we're restartable.
        closing = true;
        closeWork();
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
        if (terminalContainer != null) {
            menu.putClientProperty("container", terminalContainer); // NOI18N
        } else if (ioContainer != null) {
            menu.putClientProperty("container", ioContainer); // NOI18N
        }
        menu.putClientProperty("component", this);             // NOI18N

        Action[] acts = getActions();
        if (acts.length > 0) {
            for (Action a : acts) {
                if (a.getValue(Action.NAME) != null) {
                    menu.add(a);
                }
            }
            if (menu.getSubElements().length > 0) {
                menu.add(new JSeparator());
            }
        }
        addMenuItem(menu, copyAction);
        addMenuItem(menu, pasteAction);
        addMenuItem(menu, new JSeparator());
        addMenuItem(menu, wrapAction);
        addMenuItem(menu, new JSeparator());
        addMenuItem(menu, clearAction);
        addMenuItem(menu, closeAction);

        // just to get it echoed
        term.setKeyStrokeSet(term.getKeyStrokeSet());
        menu.show(term.getScreen(), p.x, p.y);
    }
}

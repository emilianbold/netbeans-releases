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

    private final IOContainer ioContainer;
    private final Action[] actions;
    private final String name;

    private final CallBacks callBacks = new CallBacks();

    private final StreamTerm term;
    private final FindState findState;

    private static final Preferences prefs =
        NbPreferences.forModule(TermAdvancedOption.class);
    private final TermOptions termOptions;
    private final TermOptionsPCL termOptionsPCL = new TermOptionsPCL();

    private String title;

    private boolean closing;
    private boolean closed;

    private boolean visibleInContainer;

    private class TermOptionsPCL implements PropertyChangeListener {
	@Override
        public void propertyChange(PropertyChangeEvent evt) {
            applyTermOptions(false);
        }
    }

    /**
     * These are messages from IOContainer we are obligated to handle.
     */
    private class CallBacks implements IOContainer.CallBacks {

	@Override
        public void closed() {
            // System.out.printf("Terminal.CallBacks.closed()\n");
	    // Causes assertion error in IOContainer/IOWindow.
            // OLD close();
	    setVisibleInContainer(false);
        }

	@Override
        public void selected() {
            // System.out.printf("Terminal.CallBacks.selected()\n");
        }

	@Override
        public void activated() {
            // System.out.printf("Terminal.CallBacks.activated()\n");
        }

	@Override
        public void deactivated() {
            // System.out.printf("Terminal.CallBacks.deactivated()\n");
        }
    }

    /* package */ Terminal(IOContainer ioContainer, Action[] actions, String name) {
	if (ioContainer == null)
	    throw new IllegalArgumentException("ioContainer cannot be null");	// NOI18N

        this.ioContainer = ioContainer;
        this.actions = (actions == null)? new Action[0]: actions;
	this.name = name;

        termOptions = TermOptions.getDefault(prefs);

        // this.term = new StreamTerm();
        this.term = new ActiveTerm();

        this.term.setCursorVisible(true);

        findState = new DefaultFindState(term);

        term.setHorizontallyScrollable(false);
        term.setEmulation("ansi");	// NOI18N
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

        this.setLayout(new BorderLayout());
        add(term, BorderLayout.CENTER);
	setFocusable(false);
	// OLD setActions(actions);
    }

    public IOContainer.CallBacks callBacks() {
        return callBacks;
    }

    public String name() {
	return name;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void requestFocus() {
	// redirect focus into terminal's screen
	term.getScreen().requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
	// redirect focus into terminal's screen
	return term.getScreen().requestFocusInWindow();
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
     * Return the underlying Term.
     * @return the underlying StreamTerm.
     */
    public StreamTerm term() {
        return term;
    }

    public void setTitle(String title) {
        this.title = title;
	ioContainer.setTitle(this, title);
    }

    public String getTitle() {
        return title == null ? name : title;
    }

    FindState getFindState() {
        return findState;
    }

    Action[] getActions() {
        return actions;
    }

    private final class ClearAction extends AbstractAction {
        public ClearAction() {
            super(Catalog.get("CTL_Clear"));	// NOI18N
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_E,
                                                           InputEvent.ALT_MASK);
            putValue(ACCELERATOR_KEY, accelerator);
        }

	@Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            term.clear();
        }
    }

    private final class CloseAction extends AbstractAction {
        public CloseAction() {
            super(Catalog.get("CTL_Close"));	// NOI18N
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F4,
                                                           InputEvent.ALT_MASK);
            putValue(ACCELERATOR_KEY, accelerator);
        }

	@Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            close();
        }
    }

    private final class CopyAction extends AbstractAction {
        public CopyAction() {
            super(Catalog.get("CTL_Copy"));	// NOI18N
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                                           InputEvent.ALT_MASK);
            // System.out.printf("Accelerator for Copy: %s\n", accelerator);
            putValue(ACCELERATOR_KEY, accelerator);
        }

	@Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            term.copyToClipboard();
        }
    }

    private final class PasteAction extends AbstractAction {
        public PasteAction() {
            super(Catalog.get("CTL_Paste"));	// NOI18N
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                                           InputEvent.ALT_MASK);
            // System.out.printf("Accelerator for Paste: %s\n", accelerator);
            putValue(ACCELERATOR_KEY, accelerator);
        }

	@Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            term.pasteFromClipboard();
        }
    }

    private final class FindAction extends AbstractAction {
        public FindAction() {
            super(Catalog.get("CTL_Find"));	// NOI18N
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                                           InputEvent.ALT_MASK);
            // System.out.printf("Accelerator for Find: %s\n", accelerator);
            putValue(ACCELERATOR_KEY, accelerator);
        }

	@Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
	    /* LATER
	    ioContainer.find(Terminal.this);
	     */
        }
    }

    private static final String BOOLEAN_STATE_ACTION_KEY = "boolean_state_action";	// NOI18N
    private static final String BOOLEAN_STATE_ENABLED_KEY = "boolean_state_enabled";	// NOI18N

    private final class WrapAction extends AbstractAction {
        public WrapAction() {
            super(Catalog.get("CTL_Wrap"));	// NOI18N
            // LATER KeyStroke accelerator = Utilities.stringToKey("A-R");
            putValue(BOOLEAN_STATE_ACTION_KEY, true);
        }

	@Override
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

    private final Action copyAction = new CopyAction();
    private final Action pasteAction = new PasteAction();
    private final Action findAction = new FindAction();
    private final Action wrapAction = new WrapAction();
    private final Action clearAction = new ClearAction();
    private final Action closeAction = new CloseAction();

    private void closeWork() {
        assert closing;
        if (closed)
            return;
	closed = true;
	ioContainer.remove(this);
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

    public void setVisibleInContainer(boolean visible) {
	this.visibleInContainer = visible;
    }

    public boolean isVisibleInContainer() {
	return visibleInContainer;
    }

    private boolean isBooleanStateAction(Action a) {
        Boolean isBooleanStateAction = (Boolean) a.getValue(BOOLEAN_STATE_ACTION_KEY);	//
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
	    @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }
	    @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }
	    @Override
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

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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.AttributeSet;

import org.openide.util.NbPreferences;
import org.openide.windows.IOContainer;

import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.lib.terminalemulator.Term;

import org.netbeans.lib.terminalemulator.support.DefaultFindState;
import org.netbeans.lib.terminalemulator.support.FindState;
import org.netbeans.lib.terminalemulator.support.TermOptions;
import org.netbeans.lib.terminalemulator.Coord;
import org.netbeans.modules.terminal.api.IOConnect;
import org.netbeans.modules.terminal.api.IOVisibility;
import org.netbeans.modules.terminal.api.IOVisibilityControl;

import org.netbeans.modules.terminal.TermAdvancedOption;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.lib.terminalemulator.ActiveRegion;
import org.netbeans.lib.terminalemulator.ActiveTermListener;
import org.netbeans.lib.terminalemulator.Extent;
import org.netbeans.lib.terminalemulator.TermListener;
import org.netbeans.modules.terminal.api.IOResizable;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

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
/* package */ final class Terminal extends JComponent {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final IOContainer ioContainer;
    private final TerminalInputOutput tio;	// back pointer
    private final String name;
    private final MouseAdapter mouseAdapter;

    private final CallBacks callBacks = new CallBacks();

    // Not final so we can dispose of them
    private ActiveTerm term;
    private final TermListener termListener;
    private FindState findState;

    private static final Preferences prefs =
        NbPreferences.forModule(TermAdvancedOption.class);
    private final TermOptions termOptions;
    private final TermOptionsPCL termOptionsPCL = new TermOptionsPCL();

    private String title;

    // AKA ! weak closed
    private boolean visibleInContainer;

    // AKA ! stream closed
    private boolean outConnected;
    private boolean errConnected;
    private boolean extConnected;

    // AKA strong closed
    private boolean disposed;

    // properties managed by IOvisibility
    private boolean closable = true;

    private boolean closedUnconditionally;

    private class TermOptionsPCL implements PropertyChangeListener {
	@Override
        public void propertyChange(PropertyChangeEvent evt) {
            applyTermOptions(false);
        }
    }

    /**
     * These are messages from IOContainer we are obligated to handle.
     */
    private class CallBacks implements IOContainer.CallBacks, Lookup.Provider {

	private final Lookup lookup = Lookups.fixed(new MyIOVisibilityControl());

	@Override
	public Lookup getLookup() {
	    return lookup;
	}

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

	private class MyIOVisibilityControl extends IOVisibilityControl {

	    @Override
	    protected boolean okToClose() {
		if (Terminal.this.isClosedUnconditionally())
		    return true;
		return Terminal.this.okToHide();
	    }

	    @Override
	    protected boolean isClosable() {
		if (Terminal.this.isClosedUnconditionally())
		    return true;
		return Terminal.this.isClosable();
	    }
	}
    }

    /**
     * Adapter to forward Term size change events as property changes.
     */
    private class MyTermListener implements TermListener {
	@Override
	public void sizeChanged(Dimension cells, Dimension pixels) {
	    IOResizable.Size size = new IOResizable.Size(cells, pixels);
	    tio.pcs().firePropertyChange(IOResizable.PROP_SIZE, null, size);
	}
    }

    private static class TerminalOutputEvent extends OutputEvent {
        private final String text;

        public TerminalOutputEvent(InputOutput io, String text) {
            super(io);
            this.text = text;
        }

        @Override
        public String getLine() {
            return text;
        }
    }

    /* package */ Terminal(IOContainer ioContainer, TerminalInputOutput tio, String name) {
	if (ioContainer == null)
	    throw new IllegalArgumentException("ioContainer cannot be null");	// NOI18N

        this.ioContainer = ioContainer;
	this.tio = tio;
	this.name = name;

        termOptions = TermOptions.getDefault(prefs);

        // this.term = new StreamTerm();
        this.term = new ActiveTerm();

	applyDebugFlags();

        this.term.setCursorVisible(true);

        findState = new DefaultFindState(term);

        term.setHorizontallyScrollable(false);
        term.setEmulation("ansi");	// NOI18N
        term .setBackground(Color.white);
        term.setHistorySize(4000);
        term.setRenderingHints(getRenderingHints());
	
        termOptions.addPropertyChangeListener(termOptionsPCL);
        applyTermOptions(true);

	final Set<Action> actions = new HashSet<Action>();
	actions.add(copyAction);
	actions.add(pasteAction);
	actions.add(findAction);
	actions.add(wrapAction);
	actions.add(clearAction);
	actions.add(closeAction);
	setupKeymap(actions);

        mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point p = SwingUtilities.convertPoint((Component) e.getSource(),
                                                          e.getPoint(),
                                                          term.getScreen());
                    postPopupMenu(p);
                }
            }
        };
        term.getScreen().addMouseListener(mouseAdapter);

	termListener = new MyTermListener();
	term.addListener(termListener);

        // Set up to convert clicks on active regions, created by OutputWriter.
        // println(), to outputLineAction notifications.
        term.setActionListener(new ActiveTermListener() {
	    @Override
            public void action(ActiveRegion r, InputEvent e) {
                OutputListener ol = (OutputListener) r.getUserObject();
                if (ol == null)
                    return;
                Extent extent = r.getExtent();
                String text = term.textWithin(extent.begin, extent.end);
                OutputEvent oe =
                    new TerminalOutputEvent(Terminal.this.tio, text);
                ol.outputLineAction(oe);
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

        this.setLayout(new BorderLayout());
        add(term, BorderLayout.CENTER);
	setFocusable(false);
    }

    void dispose() {
	if (disposed)
	    return;
	disposed = true;

        term.getScreen().removeMouseListener(mouseAdapter);
	term.removeListener(termListener);
	term.setActionListener(null);
	findState = null;
	term = null;
        termOptions.removePropertyChangeListener(termOptionsPCL);
	tio.dispose();
	TerminalIOProvider.dispose(tio);
    }

    boolean isDisposed() {
	return disposed;
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

    private void applyDebugFlags() {
	String value = System.getProperty("Term.debug");
	if (value == null)
	    return;

	int flags = 0;
	StringTokenizer st = new StringTokenizer(value, ",");	// NOI18N
	while (st.hasMoreTokens()) {
	    String s = st.nextToken();
	    if (s.toLowerCase().equals("ops"))			// NOI18N
		flags |= Term.DEBUG_OPS;
	    else if (s.toLowerCase().equals("keys"))		// NOI18N
		flags |= Term.DEBUG_KEYS;
	    else if (s.toLowerCase().equals("input"))		// NOI18N
		flags |= Term.DEBUG_INPUT;
	    else if (s.toLowerCase().equals("output"))		// NOI18N
		flags |= Term.DEBUG_OUTPUT;
	    else if (s.toLowerCase().equals("wrap"))		// NOI18N
		flags |= Term.DEBUG_WRAP;
	    else if (s.toLowerCase().equals("margins"))		// NOI18N
		flags |= Term.DEBUG_MARGINS;
	    else
		;
	}
	term.setDebugFlags(flags);
    }

    private void applyTermOptions(boolean initial) {
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
    public ActiveTerm term() {
        return term;
    }

    public void setTitle(String title) {
        this.title = title;
	updateName();
    }

    public String getTitle() {
        return title == null ? name : title;
    }

    FindState getFindState() {
        return findState;
    }

    private final class ClearAction extends AbstractAction {
        public ClearAction() {
            super(Catalog.get("CTL_Clear"));	// NOI18N
	    /* OLD
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_E,
                                                           InputEvent.ALT_MASK);
            putValue(ACCELERATOR_KEY, accelerator);
	    */
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
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_W,
                                                           InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
            putValue(ACCELERATOR_KEY, accelerator);
        }

	@Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            close();
        }

	@Override
	public boolean isEnabled() {
	    return closable;
	}
    }

    private final class CopyAction extends AbstractAction {
        public CopyAction() {
            super(Catalog.get("CTL_Copy"));	// NOI18N
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                                           InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
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
                                                           InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
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
	    /* LATER
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                                           InputEvent.ALT_MASK);
            putValue(ACCELERATOR_KEY, accelerator);
	    */
        }

	@Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;

	    // OLD ioContainer.find(Terminal.this);
	    // the following is code that used to be in TerminalContainer.find():
	    FindState findState = getFindState();
	    if (findState.isVisible()) {
		return;
	    }
	    findState.setVisible(true);
	    /* LATER
	    findBar.setState(findState);
	    add(findBar, BorderLayout.SOUTH);
	    validate();
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



    // Ideally IOContainer.remove() would be unconditional and we could check
    // the isClosable() and vetoing here. However, Closing a tab via it's 'X'
    // is internal to IOContainer implementation and it calls IOCOntainer.remove()
    // directly. So we're stuck with it being conditional.
    //
    // But we can trick it into being unconditional by having MyIOVisibilityControl,
    // which gets called from IOCOntainer.remove(), return true if we're
    // closing unconditionally.

    /* package */ void setClosedUnconditionally(boolean closedUnconditionally) {
	this.closedUnconditionally = closedUnconditionally;
    }

    /* package */ boolean isClosedUnconditionally() {
	return closedUnconditionally;
    }

    public void closeUnconditionally() {
	setClosedUnconditionally(true);
	close();
    }

    public void close() {
	if (!isVisibleInContainer())
	    return;
	ioContainer.remove(this);
    }

    public void setVisibleInContainer(boolean visible) {
	boolean wasVisible = this.visibleInContainer;
	this.visibleInContainer = visible;
	if (visible != wasVisible)
	    tio.pcs().firePropertyChange(IOVisibility.PROP_VISIBILITY, wasVisible, visible);
    }

    public boolean isVisibleInContainer() {
	return visibleInContainer;
    }

    public void setOutConnected(boolean outConnected) {
	boolean wasConnected = isConnected();
	this.outConnected = outConnected;

	// closing out implies closing err.
	if (outConnected == false)
	    this.errConnected = false;

	if (isConnected() != wasConnected) {
	    updateName();
	    tio.pcs().firePropertyChange(IOConnect.PROP_CONNECTED, wasConnected, isConnected());
	}
    }

    public void setErrConnected(boolean errConnected) {
	boolean wasConnected = isConnected();
	this.errConnected = errConnected;
	if (isConnected() != wasConnected) {
	    updateName();
	    tio.pcs().firePropertyChange(IOConnect.PROP_CONNECTED, wasConnected, isConnected());
	}
    }

    public void setExtConnected(boolean extConnected) {
	boolean wasConnected = isConnected();
	this.extConnected = extConnected;
	if (isConnected() != wasConnected) {
	    updateName();
	    tio.pcs().firePropertyChange(IOConnect.PROP_CONNECTED, wasConnected, isConnected());
	}
    }

    public boolean isConnected() {
	return outConnected || errConnected || extConnected;
    }

    private boolean okToHide() {
	try {
	    tio.vcs().fireVetoableChange(IOVisibility.PROP_VISIBILITY, true, false);
	} catch (PropertyVetoException ex) {
	    return false;
	}
	return true;
    }

    public void setClosable(boolean closable) {
	this.closable = closable;
	putClientProperty(TabbedPaneFactory.NO_CLOSE_BUTTON, ! closable);
    }

    public boolean isClosable() {
	return closable;
    }

    private void updateName() {
	Task task = new Task.UpdateName(ioContainer, this);
	task.post();
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

    private void setupKeymap(Set<Action> actions) {
	// We need to do two things.
	// 1) bind various Actions' keystrokes via InputMap/ActionMap
	// 2_ Tell Term to ignore said keystrokes and not consume them.
	JComponent comp = term.getScreen();

	ActionMap actionMap = comp.getActionMap();
	ActionMap newActionMap = new ActionMap();
	newActionMap.setParent(actionMap);

	InputMap inputMap = comp.getInputMap();
	InputMap newInputMap = new InputMap();
	newInputMap.setParent(inputMap);

	Set<KeyStroke> passKeystrokes = new HashSet<KeyStroke>();

	for (Action a : actions) {
	    String n = (String) a.getValue(Action.NAME);
            KeyStroke accelerator = (KeyStroke) a.getValue(Action.ACCELERATOR_KEY);
	    if (accelerator == null)
		continue;
	    newInputMap.put(accelerator, n);
	    newActionMap.put(n, a);
	    passKeystrokes.add(accelerator);
	}

	comp.setActionMap(newActionMap);
	comp.setInputMap(JComponent.WHEN_FOCUSED, newInputMap);
        term.setKeyStrokeSet((HashSet) passKeystrokes);
    }

    private void postPopupMenu(Point p) {
        JPopupMenu menu = new JPopupMenu();
	menu.putClientProperty("container", ioContainer); // NOI18N
        menu.putClientProperty("component", this);             // NOI18N

	/* LATER?
	 * NB IO APIS don't add sidebar actions to menu
        Action[] acts = getActions();
        if (acts.length > 0) {
            for (Action a : acts) {
                if (a.getValue(Action.NAME) != null)
                    menu.add(a);
            }
            if (menu.getSubElements().length > 0)
                menu.add(new JSeparator());
        }
	 */
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

    private Map<?, ?> getRenderingHints() {
        Map<?, ?> renderingHints = null;
        // init hints if any
        Lookup lookup = MimeLookup.getLookup("text/plain"); // NOI18N
        if (lookup != null) {
            FontColorSettings fcs = lookup.lookup(FontColorSettings.class);
            if (fcs != null) {
                AttributeSet attributes = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
                if (attributes != null) {
                    renderingHints = (Map<?, ?>) attributes.getAttribute(EditorStyleConstants.RenderingHints);
                }
            }
        }
	return renderingHints;
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

    void scrollTo(Coord coord) {
        term.possiblyNormalize(coord);
    }
}

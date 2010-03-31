/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.terminal.ioprovider;

import java.util.HashMap;
import java.util.Map;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.lib.terminalemulator.support.FindBar;
import org.netbeans.lib.terminalemulator.support.FindState;
import org.netbeans.modules.terminal.api.IOVisibilityControl;

import org.netbeans.modules.terminal.api.TerminalContainer;

import org.openide.awt.TabbedPaneFactory;
import org.openide.windows.IOContainer;
import org.openide.windows.IOContainer.CallBacks;
import org.openide.windows.TopComponent;

/**
 * Corresponds to core.io.ui...IOWindow.
 * @author ivan
 */
final public class TerminalContainerImpl extends TerminalContainer implements IOContainer.Provider {

    private final TopComponent owner;
    private final String originalName;

    private IOContainer ioContainer;
    private JTabbedPane tabbedPane;
    private JComponent soleComponent;

    private JToolBar actionBar;
    private FindBar findBar;

    private boolean activated = false;

    private final Map<JComponent, CallBacks> tabToCb =
	new HashMap<JComponent, CallBacks>();

    /**
     * Utility for creating custom {@link Terminal}-based TopComponents.
     * See the class comment for {@link TerminalContainer} for a description of
     * how to do this.
     * @param tc TopComponent the Terminals will go into.
     * @param name The name of the TopComponent.
     *        Usually @{link TopComponent.getName()}
     * @return a TerminalContainer.
     */
    public static TerminalContainerImpl create(TopComponent tc, String name) {
        return new TerminalContainerImpl(tc, name);
    }

    public TerminalContainerImpl(TopComponent owner, String originalName) {
        super();
        this.owner = owner;
        this.originalName = originalName;
        initComponents();
    }

    void selectTab(JComponent t) {
        if (soleComponent == null)
            tabbedPane.setSelectedComponent(t);
	if (owner != null) {
	    owner.open();
	    owner.requestActive();
	}
    }

    TopComponent topComponent() {
        return owner;
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tabbedPane = TabbedPaneFactory.createCloseButtonTabbedPane();
        tabbedPane.addPropertyChangeListener(new PropertyChangeListener() {

	    @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(TabbedPaneFactory.PROP_CLOSE)) {
                    JComponent comp = (JComponent) evt.getNewValue();
		    removeTab(comp);
                }
            }
        });
        tabbedPane.addChangeListener(new ChangeListener() {

	    @Override
            public void stateChanged(ChangeEvent e) {
                Component component = tabbedPane.getSelectedComponent();
		updateBars(component);
            }
        });
        actionBar = new JToolBar();
        actionBar.setOrientation(JToolBar.VERTICAL);
        actionBar.setLayout(new BoxLayout(actionBar, BoxLayout.Y_AXIS));
        actionBar.setFloatable(false);
        fixSize(actionBar);
        add(actionBar, BorderLayout.WEST);
        findBar = new FindBar(new FindBar.Owner() {

	    @Override
            public void close(FindBar fb) {
                findBar.getState().setVisible(false);
                TerminalContainerImpl.super.remove(findBar);
                validate();
            }
        });
    }

    private void updateBars(Component component) {
	if (component instanceof Terminal) {
	    Terminal terminal = (Terminal) component;
	    setButtons(terminal.getActions());
	    setFindBar(terminal.getFindState());
	    terminal.callBacks().selected();
	} else {
	    setButtons(new Action[0]);
	    setFindBar(null);
	}
    }

    private void fixSize(JToolBar actionBar) {
        Insets ins = actionBar.getMargin();
        JButton dummy = new JButton();
        dummy.setBorderPainted(false);
        dummy.setOpaque(false);
        dummy.setText(null);
        dummy.setIcon(new Icon() {

	    @Override
            public int getIconHeight() {
                return 16;
            }

	    @Override
            public int getIconWidth() {
                return 16;
            }

            @SuppressWarnings(value = "empty-statement")
	    @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                ;
            }
        });
        actionBar.add(dummy);
        Dimension buttonPref = dummy.getPreferredSize();
        Dimension minDim = new Dimension(buttonPref.width + ins.left + ins.right, buttonPref.height + ins.top + ins.bottom);
        actionBar.setMinimumSize(minDim);
        actionBar.setPreferredSize(minDim);
        actionBar.remove(dummy);
    }

    private JButton adjustButton(JButton b) {
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setText(null);
        b.putClientProperty("hideActionText", Boolean.TRUE);	// NOI18N
        // NOI18N
        return b;
    }

    private void setFindBar(FindState findState) {
        findBar.setState(findState);
        if (findState != null && findState.isVisible()) {
            add(findBar, BorderLayout.SOUTH);
        } else {
            super.remove(findBar);
        }
        validate();
    }

    private void setButtons(Action[] actions) {
        JButton[] buttons = new JButton[actions.length];
        for (int ax = 0; ax < actions.length; ax++) {
            Action a = actions[ax];
            JButton b = new JButton(a);
            buttons[ax] = adjustButton(b);
        }

        actionBar.removeAll();
        for (JButton b : buttons) {
            actionBar.add(b);
        }
        actionBar.revalidate();
        actionBar.repaint();
    }

    /* OLD
    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        if (comp instanceof JTabbedPane) {
            assert comp == tabbedPane;
            super.addImpl(comp, BorderLayout.CENTER, index);
        } else if (comp instanceof Terminal) {
            Terminal terminal = (Terminal) comp;
	    setVisible(comp, true);
            nVisible++;
            if (nVisible() == 1) {
                assert component0 == null;
                component0 = terminal;
                super.addImpl(terminal, BorderLayout.CENTER, index);
            } else {
                if (nVisible() == 2) {
                    assert component0 != null;
                    super.remove(component0);
                    add(tabbedPane);
                    tabbedPane.addTab(component0.name(), component0);
                    setTitle(component0, component0.getTitle());
                    component0 = null;
                }
                tabbedPane.addTab(terminal.name(), terminal);
                tabbedPane.setSelectedComponent(terminal);
            }
            setTitle(terminal, terminal.getTitle());
            setButtons(terminal.getActions());
        } else {
            super.addImpl(comp, constraints, index);
        }
    }
     */

    /**
     * Remove who from this.
     * Mostly manages whether we have tabs and the TopComponents title and such
     */
    /* OLD
    void removeTerminal(final JComponent who) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

		@Override
                public void run() {
                    removeTerminal(who);
                }
            });
            return;
        }
        if (nVisible() <= 0) {
            throw new IllegalStateException("<= 0 Terminals");	// NOI18N
        }
        if (nVisible() > 1) {
            tabbedPane.remove(who);
        } else {
            assert component0 == who;
            super.remove(who);
            component0 = null;
        }

        if (nVisible() == 2) {
            Terminal last = (Terminal) tabbedPane.getComponentAt(0);
            tabbedPane.remove(0);
            super.remove(tabbedPane);
            // OLD nVisible = 0;
            add(last);
            setFindBar(last.getFindState());
            validate();
        } else if (nVisible() == 1) {
            setButtons(new Action[0]);
	    if (owner != null)
		owner.close();
        }

        nVisible--;
	setVisible(who, false);
    }
     */

    private void addTab(JComponent comp, CallBacks cb) {
	if (cb != null) {
	    tabToCb.put(comp, cb);
	}
	if (soleComponent != null) {
	    // only single tab, remove it from TopComp. and add it to tabbed pane
	    assert tabbedPane.getParent() == null;
	    assert tabbedPane.getTabCount() == 0;
	    super.remove(soleComponent);
	    tabbedPane.add(soleComponent);
	    // LATER set icon
	    // LATER set tooltip
	    soleComponent = null;
	    tabbedPane.add(comp);
	    super.add(tabbedPane);
	    updateWindowName(null);
	} else if (tabbedPane.getTabCount() > 0) {
	    // already several tabs
	    assert tabbedPane.getParent() != null;
	    assert soleComponent == null;
	    tabbedPane.add(comp);
	} else {
	    // nothing yet
	    assert tabbedPane.getParent() == null;
	    assert soleComponent == null;
	    setFocusable(false);
	    soleComponent = comp;
	    super.add(comp);
	    updateBars(soleComponent);
	    updateWindowName(soleComponent.getName());
	    // LATER checkTabSelChange();
	}
	revalidate();
    }

    private void removeTab(final JComponent comp) {
	// TMP
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

		@Override
                public void run() {
                    removeTab(comp);
                }
            });
            return;
        }

	CallBacks cb = tabToCb.get(comp);

	if (cb != null && IOVisibilityControl.isSupported(cb)) {
	    if (IOVisibilityControl.isClosable(cb)) {
		if (! IOVisibilityControl.okToClose(cb))
		    return;		// close got vetoed.
	    } else {
		// Should usually not get here because all relevant
		// actions or their peformers should've been disabled.
		// SHOULD emit a warning
		return;
	    }

	}


	// SHOULD check if callers of this function assume that it
	// always succeeds.

	if (soleComponent != null) {
	    // removing tha last one
	    assert soleComponent == comp;
	    super.remove(soleComponent);
	    soleComponent = null;
	    updateWindowName(null);
	    // LATER checkTabSelChange();
	    setFocusable(true);
	    revalidate();
	    repaint();	// otherwise term will still stay in view
	} else if (tabbedPane.getParent() == this) {
	    assert tabbedPane.getTabCount() > 1;
	    tabbedPane.remove(comp);
	    if (tabbedPane.getTabCount() == 1) {
		soleComponent  = (JComponent) tabbedPane.getComponentAt(0);
		tabbedPane.remove(soleComponent);
		super.remove(tabbedPane);
		super.add(soleComponent);
		updateBars(soleComponent);
		updateWindowName(soleComponent.getName());
	    }
	    revalidate();
	}
	if (cb != null)
	    cb.closed();
    }

    private boolean contains (JComponent comp) {
	return soleComponent == comp ||
	       tabbedPane.indexOfComponent(comp) != -1;
    }


    /**
     * Update out containing TC's window name.
     * @param title
     */
    private void updateWindowName(String title) {
	if (owner == null)
	    return;

	if (title == null) {
	    // sole or no component
	    owner.setDisplayName(originalName);
	    owner.setToolTipText(originalName);
	    owner.setHtmlDisplayName(null);

	} else {
	    String composite  = originalName + " - ";	// NOI18N
	    if (title.contains("<html>")) {		// NOI18N
		// pull the "<html>" to the beginning of the string
		title = title.replace("<html>", "");		// NOI18N
		composite = "<html> " + composite + title;// NOI18N
		owner.setHtmlDisplayName(composite);
	    } else {
		owner.setDisplayName(composite);
		owner.setHtmlDisplayName(null);
	    }
	    owner.setToolTipText(composite);
	}
    }

    @Override
    public void setTitle(JComponent who, String title) {
        if (title == null) {
            title = originalName;
        }

	// Use the name field of the component to remember title
	who.setName(title);

	if (!contains(who)) {
	    return;
	}

        if (soleComponent != null) {
	    assert soleComponent == who;
	    updateWindowName(title);
        } else {
	    assert tabbedPane.getParent() == this;
	    updateWindowName(null);
	    // write thru
            tabbedPane.setTitleAt(tabbedPane.indexOfComponent(who), title);
        }
    }

    void find(Terminal who) {
        FindState findState = who.getFindState();
        if (findState.isVisible()) {
            return;

        }
        findState.setVisible(true);
        findBar.setState(findState);
        add(findBar, BorderLayout.SOUTH);
        validate();
    }

    @Override
    public void requestFocus() {
	// redirect focus into terminal
	JComponent selected = getSelected();
	if (selected != null) {
	    selected.requestFocus();
	} else {
	    super.requestFocus();
	}
    }

    @Override
    public boolean requestFocusInWindow() {
	// redirect focus into terminal
	JComponent selected = getSelected();
	if (selected != null) {
	    return selected.requestFocusInWindow();
	} else {
	    return super.requestFocusInWindow();
	}
    }

    /**
     * Handle delegation from containing TopComponent.
     */
    @Override
    public void componentActivated() {
	activated = true;
        Component component;
        if (soleComponent != null)
            component = soleComponent;
        else
            component = tabbedPane.getSelectedComponent();
	// SHOULD use tabToCb
        if (component instanceof Terminal) {
            Terminal terminal = (Terminal) component;
            terminal.callBacks().activated();
        }
    }

    /**
     * Handle delegation from containing TopComponent.
     */

    @Override
    public void componentDeactivated() {
	activated = false;
        Component component;
        if (soleComponent != null)
            component = soleComponent;
        else
            component = tabbedPane.getSelectedComponent();
	// SHOULD use tabToCb
        if (component instanceof Terminal) {
            Terminal terminal = (Terminal) component;
            terminal.callBacks().deactivated();
        }
    }

    @Override
    public void open() {
	if (owner != null)
	    owner.open();
    }

    @Override
    public void requestActive() {
	if (owner != null)
	    owner.requestActive();
    }

    @Override
    public void requestVisible() {
	if (owner != null)
	    owner.requestVisible();
    }

    @Override
    public boolean isActivated() {
	return activated;
    }

    @Override
    public void add(JComponent comp, CallBacks cb) {
	addTab(comp, cb);
    }

    @Override
    public void remove(JComponent comp) {
	removeTab(comp);
    }

    @Override
    public void select(JComponent comp) {
	selectTab(comp);
    }

    @Override
    public JComponent getSelected() {
        if (soleComponent == null)
            return (JComponent) tabbedPane.getSelectedComponent();
	else
	    return soleComponent;
    }

    /* TMP
    public void setTitle(JComponent comp, String name) {
	throw new UnsupportedOperationException("Not supported yet.");
    }
     */

    @Override
    public void setToolTipText(JComponent comp, String text) {
	throw new UnsupportedOperationException("Not supported yet.");	// NOI18N
    }

    @Override
    public void setIcon(JComponent comp, Icon icon) {
	throw new UnsupportedOperationException("Not supported yet.");	// NOI18N
    }

    @Override
    public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
	// was: setActions()
        if (soleComponent != null) {
            setButtons(toolbarActions);
        } else {
            if (tabbedPane.getSelectedComponent() == comp) {
                setButtons(toolbarActions);
            }
        }
    }

    @Override
    public boolean isCloseable(JComponent comp) {
	CallBacks cb = tabToCb.get(comp);
	if (cb != null && IOVisibilityControl.isSupported(cb)) {
	    return IOVisibilityControl.isClosable(cb);
	} else {
	    return true;
	}
    }

    @Override
    public IOContainer ioContainer() {
	if (ioContainer == null)
	    ioContainer = IOContainer.create(this);
	return ioContainer;
    }
}

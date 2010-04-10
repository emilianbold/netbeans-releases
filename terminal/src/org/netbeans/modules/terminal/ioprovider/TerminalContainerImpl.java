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

    private final static String PROP_ATTRIBUTES =
	    "TerminalContainerImpl.ATTRBUTES";	// NOI18N
    private final TopComponent owner;
    private final String originalName;

    private IOContainer ioContainer;
    private JTabbedPane tabbedPane;
    private JComponent soleComponent;
    private JComponent lastSelection;

    private JToolBar actionBar;
    private FindBar findBar;

    private boolean activated = false;

    private static class Attributes {
	public CallBacks cb;
	public String title;
	public Action[] toolbarActions;
	public String toolTipText;
	public Icon icon;

	// LATER
	// public boolean isClosable;
	// public FindState findState;
    }

    /**
     * Return Attributes associated with 'comp'.
     * Create and attach of none exist.
     * @param comp
     * @return
     */
    private Attributes attributesFor(JComponent comp) {
	Object o = comp.getClientProperty(PROP_ATTRIBUTES);
	if (o == null) {
	    Attributes a = new Attributes();
	    comp.putClientProperty(PROP_ATTRIBUTES, a);
	    return a;
	} else {
	    return (Attributes) o;
	}
    }

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
		checkSelectionChange();
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
	if (buttons.length != 0) {
	    actionBar.setVisible(true);
	    for (JButton b : buttons) {
		actionBar.add(b);
	    }
	} else {
	    actionBar.setVisible(false);
	}
        actionBar.revalidate();
        actionBar.repaint();
    }

    /**
     * Restore attributes that are maintained by the tabbedPane.
     *
     * Called when a component is added to a tabbed pane.
     * No need to do anything (i.e. save) when we remove a component.
     *
     * Also called on individual attribute settings like
     * setIcon(JComponent, Icon). Note that this method is overkill
     * for this purpose. I.e. it will set title etc as well.
     * If this ever becomes an issue we can pass a mask to control
     * what exactly gets restored.
     * @param comp
     */
    private void restoreAttrsFor(JComponent comp) {
	int index = tabbedPane.indexOfComponent(comp);
	if (index == -1)
	    return;

	Attributes attrs = attributesFor(comp);

	tabbedPane.setTitleAt(index, attrs.title);

	tabbedPane.setIconAt(index, attrs.icon);
	tabbedPane.setDisabledIconAt(index, attrs.icon);

	// output2 "stores" toolTipText as the components
	// attribute
	tabbedPane.setToolTipTextAt(index, attrs.toolTipText);
    }

    private void addTab(JComponent comp, CallBacks cb) {
	Attributes attr = attributesFor(comp);
	attr.cb = cb;

	if (soleComponent != null) {
	    // only single tab, remove it from TopComp. and add it to tabbed pane
	    assert tabbedPane.getParent() == null;
	    assert tabbedPane.getTabCount() == 0;
	    super.remove(soleComponent);
	    super.add(tabbedPane);
	    tabbedPane.add(soleComponent);
	    restoreAttrsFor(soleComponent);
	    soleComponent = null;
	    updateWindowName(null);

	    // Add the window we're adding
	    tabbedPane.add(comp);
	    restoreAttrsFor(comp);


	} else if (tabbedPane.getTabCount() > 0) {
	    // already several tabs
	    assert tabbedPane.getParent() != null;
	    assert soleComponent == null;
	    tabbedPane.add(comp);
	    restoreAttrsFor(comp);

	} else {
	    // nothing yet
	    assert tabbedPane.getParent() == null;
	    assert soleComponent == null;
	    setFocusable(false);
	    soleComponent = comp;
	    super.add(comp);
	    updateWindowName(soleComponent.getName());
	    // for first component we act as if select was called
	    checkSelectionChange();
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

	CallBacks cb = attributesFor(comp).cb;

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
	    // removing the last one
	    assert soleComponent == comp;
	    super.remove(soleComponent);
	    soleComponent = null;
	    updateWindowName(null);
	    checkSelectionChange();
	    setFocusable(true);
	    revalidate();
	    repaint();	// otherwise term will still stay in view

	} else if (tabbedPane.getParent() == this) {
	    assert tabbedPane.getTabCount() > 1;
	    tabbedPane.remove(comp);
	    if (tabbedPane.getTabCount() == 1) {
		//  switch to no tabbed pane
		soleComponent  = (JComponent) tabbedPane.getComponentAt(0);
		tabbedPane.remove(soleComponent);
		super.remove(tabbedPane);
		super.add(soleComponent);
		updateWindowName(soleComponent.getName());
	    }
	    checkSelectionChange();
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
     * A new component has been selected.
     * Update anything that needs to be updated.
     */

    private void checkSelectionChange() {
	// outptu2 calls this checkTabSelChange().
	JComponent selection = getSelected();
	if (selection != lastSelection) {
	    lastSelection = selection;
	    updateBars(selection);
	    if (selection != null) {
		// This is the case when we remove the last component
		CallBacks cb = attributesFor(selection).cb;
		if (cb != null)
		    cb.selected();
	    }
	    // LATER update findstate
	    // LATER not sure what the following does:
	    // LATER getActionMap().setParent(sel != null ? sel.getActionMap() : null);
	}
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

    //
    // Overrides of TerminalContainer
    //

    @Override
    public IOContainer ioContainer() {
	if (ioContainer == null)
	    ioContainer = IOContainer.create(this);
	return ioContainer;
    }

    /**
     * Handle delegation from containing TopComponent.
     */
    @Override
    public void componentActivated() {
	// Up to the client of TerminalContainer:
	// owner.componentActivated();
	activated = true;
        JComponent comp = getSelected();
	if (comp != null) {
	    CallBacks cb = attributesFor(comp).cb;
	    if (cb != null)
		cb.activated();
	}
    }

    /**
     * Handle delegation from containing TopComponent.
     */

    @Override
    public void componentDeactivated() {
	// Up to the client of TerminalContainer:
	// owner.componentDeactivated();
	activated = false;
        JComponent comp = getSelected();
	if (comp != null) {
	    CallBacks cb = attributesFor(comp).cb;
	    if (cb != null)
		cb.deactivated();
	}
    }


    //
    // Overrides of IOContainer.Provider
    //
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
        if (soleComponent == null) {
	    // will call checkSelectinChange() via stateChanged()
            tabbedPane.setSelectedComponent(comp);
	} else {
	    checkSelectionChange();
	}

	if (owner != null) {
	    owner.open();
	    owner.requestActive();
	}
    }

    @Override
    public JComponent getSelected() {
        if (soleComponent != null)
            return soleComponent;
        else
            return (JComponent) tabbedPane.getSelectedComponent();
    }

    @Override
    public void setTitle(JComponent comp, String title) {
        if (title == null) {
            title = originalName;
        }

	// Remember title in attributes
	// It gets recalled when we switch from tabbed to soleComponent mode
	Attributes attrs = attributesFor(comp);
	attrs.title = title;

	// output2 uses the name property of the JComponent to
	// remember the title.
	// So do we for good measure.
	comp.setName(title);

	if (!contains(comp)) {
	    return;
	}

	// pass-through for currently visible component
	// SHOULD see if the following logic can be applied generically
	// after addTab() or removeTab()
        if (soleComponent != null) {
	    assert soleComponent == comp;
	    updateWindowName(title);
        } else {
	    assert tabbedPane.getParent() == this;
	    updateWindowName(null);
	    // write thru
	    restoreAttrsFor(comp);
        }
    }

    @Override
    public void setToolTipText(JComponent comp, String text) {
	// Remember tip text in attributes
	// It gets recalled when this comp is re-added to the tabbedPane
	//
	// output2 remembers the tip text in te toolTipText property of
	// the JComponent itself.
	Attributes attrs = attributesFor(comp);
	attrs.toolTipText = text;

	// pass-through for currently visible component
	restoreAttrsFor(comp);
    }

    @Override
    public void setIcon(JComponent comp, Icon icon) {
	// Remember icon in attributes
	// It gets recalled when this comp is re-added to the tabbedPane
	//
	// output2 remembers the icon in a client property.
	Attributes attrs = attributesFor(comp);
	attrs.icon = icon;

	// pass-through for currently visible component
	restoreAttrsFor(comp);
    }

    @Override
    public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
	// was: setActions()

	// Remember in attributes
	// They get recalled when this comp is selected
	//
	// output2 remembers the actions in a client property.
	// SHOULD consider migration of components from one type
	// of container to another?
	Attributes attrs = attributesFor(comp);
	attrs.toolbarActions = toolbarActions;

	// pass-through for currently visible component
	if (getSelected() == comp)
            setButtons(toolbarActions);
    }

    @Override
    public boolean isCloseable(JComponent comp) {
	CallBacks cb = attributesFor(comp).cb;
	if (cb != null && IOVisibilityControl.isSupported(cb)) {
	    return IOVisibilityControl.isClosable(cb);
	} else {
	    return true;
	}
    }
}

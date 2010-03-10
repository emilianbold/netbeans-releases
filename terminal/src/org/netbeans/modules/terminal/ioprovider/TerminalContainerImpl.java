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
import org.netbeans.modules.terminal.api.TerminalContainer;
import org.openide.awt.TabbedPaneFactory;
import org.openide.windows.IOContainer;
import org.openide.windows.IOContainer.CallBacks;
import org.openide.windows.TopComponent;

/**
 * Help a TopComponent be a {@link TerminalWindow} and an owner of {@link Terminal}'s.
 * <p>
 * Use {@link TerminalProvider#createTerminalContainer} to get one.
 * <p> 
 * Recipe for enahncing a TopComponent ...
 * <ul>
 * <li>
 * Create a stock TopComponent with the IDE.
 * <li>
 * Change it's Layout to be BorderLayout.
 * <li>
 * Have it <code>implements TerminalWindow</code>.
 * <li>
 * Add the following code to it:
 * <pre>
    private TerminalContainer tc;

    public TerminalContainer terminalContainer() {
        return tc;
    }

    private void initComponents2() {
        tc = TerminalProvider.createTerminalContainer(this, getName());
        add(tc);
    }
 * </pre>
 * <li>
 * Call <code>initComponents2()</code> at the end of the constructor of
 * your top component.
 * </ul>
 * @author ivan
 */
final public class TerminalContainerImpl extends TerminalContainer implements IOContainer.Provider {

    private final TopComponent owner;
    private final String originalName;

    private int nTerm;
    private Terminal component0;
    private JTabbedPane tabbedPane;
    private JToolBar actionBar;
    private FindBar findBar;

    private boolean activated = false;

    private IOContainer ioContainer;

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

    int nTerm() {
        return nTerm;
    }

    void select(Terminal t) {
        if (nTerm > 1)
            tabbedPane.setSelectedComponent(t);
        owner.open();
        owner.requestActive();
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
                    Object o = evt.getNewValue();
                    if (o instanceof Terminal) {
                        Terminal tt = (Terminal) o;
                        tt.close();
                    }
                }
            }
        });
        tabbedPane.addChangeListener(new ChangeListener() {

	    @Override
            public void stateChanged(ChangeEvent e) {
                Component component = tabbedPane.getSelectedComponent();
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

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
//        System.out.printf("TermTopComponent.addImpl(%s, %s, %s)\n", comp, constraints, index);
        if (comp instanceof JTabbedPane) {
            assert comp == tabbedPane;
            super.addImpl(comp, BorderLayout.CENTER, index);
        } else if (comp instanceof Terminal) {
            Terminal terminal = (Terminal) comp;
            nTerm++;
            if (nTerm == 1) {
                assert component0 == null;
                component0 = terminal;
                super.addImpl(terminal, BorderLayout.CENTER, index);
            } else {
                if (nTerm == 2) {
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

    /**
     * Remove who from this.
     * Mostly manages whether we have tabs and the TopComponents title and such
     */
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
        if (nTerm <= 0) {
            throw new IllegalStateException("<= 0 Terminals");	// NOI18N
        }
        nTerm--;
        if (nTerm >= 1) {
            tabbedPane.remove(who);
        } else {
            assert component0 == who;
            super.remove(who);
            component0 = null;
        }
        if (nTerm == 1) {
            Terminal last = (Terminal) tabbedPane.getComponentAt(0);
            tabbedPane.remove(0);
            super.remove(tabbedPane);
            nTerm = 0;
            add(last);
            setFindBar(last.getFindState());
            validate();
        } else if (nTerm == 0) {
            setButtons(new Action[0]);
            owner.close();
        }
    }

    @Override
    public void setTitle(JComponent who, String title) {
        if (title == null) {
            title = originalName;
        }
        if (nTerm == 1) {
            owner.setName(originalName + " - " + title);	// NOI18N
        } else {
            owner.setName(originalName);
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

    /**
     * Handle delegation from containing TopComponent.
     */
    @Override
    public void componentActivated() {
	activated = true;
        Component component;
        if (component0 != null)
            component = component0;
        else
            component = tabbedPane.getSelectedComponent();
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
        if (component0 != null)
            component = component0;
        else
            component = tabbedPane.getSelectedComponent();
        if (component instanceof Terminal) {
            Terminal terminal = (Terminal) component;
            terminal.callBacks().deactivated();
        }
    }

    @Override
    public void open() {
	owner.open();
    }

    @Override
    public void requestActive() {
	owner.requestActive();
    }

    @Override
    public void requestVisible() {
	owner.requestVisible();
    }

    @Override
    public boolean isActivated() {
	return activated;
    }

    @Override
    public void add(JComponent comp, CallBacks cb) {
	super.add(comp);
    }

    @Override
    public void remove(JComponent comp) {
	removeTerminal(comp);
    }

    @Override
    public void select(JComponent comp) {
	if (comp instanceof Terminal) {
	    select((Terminal) comp);
	} else {
	    throw new UnsupportedOperationException("Can't select non-Terminals");	// NOI18N
	}

    }

    @Override
    public JComponent getSelected() {
        if (nTerm > 1)
            return (JComponent) tabbedPane.getSelectedComponent();
	else
	    return component0;
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
        if (nTerm == 1) {
            setButtons(toolbarActions);
        } else {
            if (tabbedPane.getSelectedComponent() == comp) {
                setButtons(toolbarActions);
            }
        }
    }

    @Override
    public boolean isCloseable(JComponent comp) {
	throw new UnsupportedOperationException("Not supported yet.");	// NOI18N
    }

    @Override
    public IOContainer ioContainer() {
	if (ioContainer == null)
	    ioContainer = IOContainer.create(this);
	return ioContainer;
    }
}

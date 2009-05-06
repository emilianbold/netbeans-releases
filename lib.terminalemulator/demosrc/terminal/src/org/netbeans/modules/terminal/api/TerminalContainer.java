package org.netbeans.modules.terminal.api;

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
import org.netbeans.lib.termsupport.FindBar;
import org.netbeans.lib.termsupport.FindState;
import org.openide.awt.TabbedPaneFactory;
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
public final class TerminalContainer extends JComponent {

    private final TopComponent owner;
    private final String originalName;

    private int nTerm;
    private Terminal component0;
    private JTabbedPane tabbedPane;
    private JToolBar actionBar;
    private FindBar findBar;

    TerminalContainer(TopComponent owner, String originalName) {
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

            public void close(FindBar fb) {
                findBar.getState().setVisible(false);
                remove(findBar);
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

            public int getIconHeight() {
                return 16;
            }

            public int getIconWidth() {
                return 16;
            }

            @SuppressWarnings(value = "empty-statement")
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
        b.putClientProperty("hideActionText", Boolean.TRUE);
        // NOI18N
        return b;
    }

    void setActions(Terminal who, Action[] actions) {
        if (nTerm == 1) {
            setButtons(actions);
        } else {
            if (tabbedPane.getSelectedComponent() == who) {
                setButtons(actions);
            }
        }
    }

    private void setFindBar(FindState findState) {
        findBar.setState(findState);
        if (findState != null && findState.isVisible()) {
            add(findBar, BorderLayout.SOUTH);
        } else {
            remove(findBar);
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
                    tabbedPane.addTab("", component0);
                    setTitle(component0, component0.getTitle());
                    component0 = null;
                }
                tabbedPane.addTab("", terminal);
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
    void removeTerminal(final Terminal who) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    removeTerminal(who);
                }
            });
            return;
        }
        if (nTerm <= 0) {
            throw new IllegalStateException("<= 0 Terminals");
        }
        nTerm--;
        if (nTerm >= 1) {
            tabbedPane.remove(who);
        } else {
            assert component0 == who;
            remove(who);
            component0 = null;
        }
        if (nTerm == 1) {
            Terminal last = (Terminal) tabbedPane.getComponentAt(0);
            tabbedPane.remove(0);
            remove(tabbedPane);
            nTerm = 0;
            add(last);
            setFindBar(last.getFindState());
            validate();
        } else if (nTerm == 0) {
            setButtons(new Action[0]);
            owner.close();
        }
    }

    void setTitle(Terminal who, String title) {
        if (title == null) {
            title = originalName;
        }
        if (nTerm == 1) {
            owner.setName(originalName + " - " + title);
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
    public void componentActivated() {
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
    public void componentDeactivated() {
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
}

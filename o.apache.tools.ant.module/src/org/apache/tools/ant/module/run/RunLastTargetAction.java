/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.run;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.AntModule;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 * An action to run the last Ant build.
 * @see "#47925"
 */
public final class RunLastTargetAction extends AbstractAction implements ChangeListener, Presenter.Menu, Presenter.Toolbar {
    
    public RunLastTargetAction() {
        super(NbBundle.getMessage(RunLastTargetAction.class, "LBL_RunLastTargetAction_general"),
                new ImageIcon(Utilities.loadImage("org/apache/tools/ant/module/resources/AntIcon.gif", true)));
        LastTargetExecuted.addChangeListener(WeakListeners.change(this, LastTargetExecuted.class));
    }
    
    public boolean isEnabled() {
        return LastTargetExecuted.getLastBuildScript() != null;
    }
    
    public Object getValue(String key) {
        if (key.equals(Action.SHORT_DESCRIPTION)) {
            String display = LastTargetExecuted.getProcessDisplayName();
            if (display != null) {
                return NbBundle.getMessage(RunLastTargetAction.class, "TIP_RunLastTargetAction_specific", display);
            } else {
                return null;
            }
        } else {
            return super.getValue(key);
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    LastTargetExecuted.rerun();
                } catch (IOException ioe) {
                    AntModule.err.notify(ioe);
                }
            }
        });
    }

    public void stateChanged(ChangeEvent e) {
        firePropertyChange("enabled", null, Boolean.valueOf(isEnabled())); // NOI18N
        firePropertyChange(Action.SHORT_DESCRIPTION, null, null);
    }

    public JMenuItem getMenuPresenter() {
        class SpecialMenuItem extends JMenuItem implements DynamicMenuContent {
            public SpecialMenuItem() {
                super(RunLastTargetAction.this);
            }
            public JComponent[] getMenuPresenters() {
                JMenuItem menu = new JMenuItem(RunLastTargetAction.this);
                String label;
                String display = LastTargetExecuted.getProcessDisplayName();
                if (display != null) {
                    label = NbBundle.getMessage(RunLastTargetAction.class, "LBL_RunLastTargetAction_specific", display);
                } else {
                    label = (String) getValue(Action.NAME);
                }
                Mnemonics.setLocalizedText(menu, label);
                return new JComponent[] {menu};
            }
            public JComponent[] synchMenuPresenters(JComponent[] items) {
                return getMenuPresenters();
            }
        }
        return new SpecialMenuItem();
    }

    public Component getToolbarPresenter() {
        JButton button = new JButton();
        Actions.connect(button, this);
        return button;
    }

}

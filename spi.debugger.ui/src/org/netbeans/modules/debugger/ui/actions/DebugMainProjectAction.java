/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.openide.awt.Actions;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Entlicher
 */
public class DebugMainProjectAction implements Action, Presenter.Toolbar {
    
    private Action delegate;
    
    /** Creates a new instance of DebugMainProjectAction */
    public DebugMainProjectAction() {
        delegate = MainProjectSensitiveActions.mainProjectCommandAction(
                ActionProvider.COMMAND_DEBUG,
                NbBundle.getMessage(DebugMainProjectAction.class, "LBL_DebugMainProjectAction_Name" ),
                new ImageIcon(Utilities.loadImage( "org/netbeans/modules/debugger/resources/debugProject.png" ))); // NOI18N
        delegate.putValue("iconBase","org/netbeans/modules/debugger/resources/debugProject.png"); //NOI18N
    }
    
    public Object getValue(String arg0) {
        return delegate.getValue(arg0);
    }

    public void putValue(String arg0, Object arg1) {
        delegate.putValue(arg0, arg1);
    }

    public void setEnabled(boolean arg0) {
        delegate.setEnabled(arg0);
    }

    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    public void addPropertyChangeListener(PropertyChangeListener arg0) {
        delegate.addPropertyChangeListener(arg0);
    }

    public void removePropertyChangeListener(PropertyChangeListener arg0) {
        delegate.removePropertyChangeListener(arg0);
    }

    public void actionPerformed(ActionEvent arg0) {
        delegate.actionPerformed(arg0);
    }

    public Component getToolbarPresenter() {
        JPopupMenu menu = new JPopupMenu();
        JButton button = DropDownButtonFactory.createDropDownButton(
                new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)), menu);
        JMenuItem item = new JMenuItem(Actions.cutAmpersand((String) getValue(NAME)));
        menu.add(item);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DebugMainProjectAction.this.actionPerformed(e);
            }
        });
        try {
            final ConnectAction ca = (ConnectAction) Lookups.forPath("Actions/Debug").lookup(
                    new Lookup.Template(Action.class, "Actions/Debug/org-netbeans-modules-debugger-ui-actions-ConnectAction", null)) // NOI18N
                    .allInstances().iterator().next();
            item = new JMenuItem(Actions.cutAmpersand((String) ca.getValue(NAME)));
            menu.add(item);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ca.actionPerformed(e);
                }
            });
        } catch (java.util.NoSuchElementException nsee) {
            Exceptions.printStackTrace(nsee);
        }
        Actions.connect(button, this);
        return button;
    }

}

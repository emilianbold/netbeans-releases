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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.vmd.screen.resource;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;
import org.netbeans.modules.vmd.screen.ScreenViewController;
import org.openide.util.Utilities;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.vmd.screen.GradientBorder;

/**
 * @author breh
 */
public class ResourceItemPanel extends JLabel implements MouseListener {

    private static Border SELECTED_RESOURCE_BORDER = new GradientBorder(true);
//            BorderFactory.createCompoundBorder(
//            BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
//            BorderFactory.createLineBorder(Color.BLUE, 3));
    private static Border RESOURCE_BORDER = new GradientBorder(false);
//            BorderFactory.createCompoundBorder(
//            BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
//            BorderFactory.createEmptyBorder(3, 3, 3, 3));

    private DesignComponent component;

    public ResourceItemPanel(DesignComponent component) {
        this.component = component;
        addMouseListener (this);
    }

    // called from AWT and document transaction
    public void reload () {
        InfoPresenter presenter = component.getPresenter (InfoPresenter.class);
        assert presenter != null;

        setText (presenter.getDisplayName (InfoPresenter.NameType.PRIMARY));

        Image image = presenter.getIcon (InfoPresenter.IconType.COLOR_16x16);
        setIcon (image != null ? new ImageIcon (image) : null);

        if (component.getDocument ().getSelectedComponents ().contains (component))
            setBorder (SELECTED_RESOURCE_BORDER);
        else
            setBorder (RESOURCE_BORDER);
    }

    public JPopupMenu getComponentPopupMenu () {
        return Utilities.actionsToPopup (ActionsSupport.createActionsArray (component), this);
    }

    public void mouseClicked(final MouseEvent e) {
        // TODO selection should be implemented so it happens on mousePressed and confirmed by mouse released !!!
        // this one wants to be selected
        final DesignDocument doc = component.getDocument();
        final Collection<DesignComponent> newSelection = new ArrayList<DesignComponent> ();
        doc.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
                    // invert selection
                    Collection<DesignComponent> currentSelection = doc.getSelectedComponents();
                    newSelection.addAll(currentSelection);
                    if (currentSelection.contains(component)) {
                        newSelection.remove(component);
                    } else {
                        newSelection.add(component);
                    }
                } else {
                    newSelection.add(component);
                }
                doc.setSelectedComponents(ScreenViewController.SCREEN_ID, newSelection);
            }
        });
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

}
